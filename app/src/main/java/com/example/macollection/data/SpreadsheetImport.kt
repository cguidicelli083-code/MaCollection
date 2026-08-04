package com.example.macollection.data

import android.content.Context
import android.net.Uri
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.text.Normalizer
import java.util.zip.ZipFile
import kotlin.math.roundToInt

/**
 * Import d'un fichier tableur externe (.xlsx ou .csv) vers des [CollectionItem], même si ses
 * colonnes ne correspondent pas au format d'export de l'appli — l'utilisateur fait correspondre
 * lui-même chaque colonne à un champ via [ImportField] (voir l'écran de correspondance).
 *
 * Pas de vraie librairie .xlsx (Apache POI est bien trop lourde sur Android, voir la même
 * remarque dans [ExcelExport]) : un fichier .xlsx est un simple ZIP contenant des XML, on lit
 * directement `xl/sharedStrings.xml` + la première feuille avec les classes standard du JDK/Android
 * (`java.util.zip`, `XmlPullParser`) sans dépendance supplémentaire.
 */
object SpreadsheetImport {

    data class ParsedSheet(val headers: List<String>, val rows: List<List<String>>)

    enum class ImportField {
        NAME, TYPE, BRAND, PLATFORM, CONDITION, HAS_BOX, HAS_MANUAL,
        YEAR, GENRE, PRICE, BARCODE, DESCRIPTION, REGION
    }

    /** Lit le fichier désigné par [uri] ; renvoie null si le format n'est pas reconnu/lisible. */
    fun parseFile(context: Context, uri: Uri): ParsedSheet? {
        val displayName = MediaUtils.displayName(context, uri).orEmpty().lowercase()
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        return when {
            displayName.endsWith(".csv") -> parseCsv(bytes)
            displayName.endsWith(".xlsx") -> parseXlsx(context, uri)
            // Signature ZIP ("PK\x03\x04") : très probablement un .xlsx malgré un nom/extension
            // absent (ex. certains partages cloud renomment le fichier téléchargé).
            bytes.size > 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() -> parseXlsx(context, uri)
            else -> parseCsv(bytes)
        }
    }

    // -----------------------------------------------------------------------
    // CSV
    // -----------------------------------------------------------------------

    private fun parseCsv(bytes: ByteArray): ParsedSheet? {
        val text = bytes.toString(Charsets.UTF_8).removePrefix("﻿")
        val lines = text.split("\r\n", "\n", "\r").filter { it.isNotBlank() }
        if (lines.isEmpty()) return null
        // Les exports français (Excel/Sheets en locale FR) utilisent souvent ";" plutôt que ",".
        val delimiter = if (lines.first().count { it == ';' } > lines.first().count { it == ',' }) ';' else ','
        val parsed = lines.map { splitCsvLine(it, delimiter) }
        val width = parsed.first().size
        return ParsedSheet(parsed.first(), parsed.drop(1).map { row -> row + List((width - row.size).coerceAtLeast(0)) { "" } })
    }

    /** Découpe une ligne CSV en respectant les champs entre guillemets (avec "" = guillemet littéral). */
    private fun splitCsvLine(line: String, delimiter: Char): List<String> {
        val cells = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes && c == '"' && i + 1 < line.length && line[i + 1] == '"' -> { current.append('"'); i++ }
                c == '"' -> inQuotes = !inQuotes
                c == delimiter && !inQuotes -> { cells.add(current.toString()); current.clear() }
                else -> current.append(c)
            }
            i++
        }
        cells.add(current.toString())
        return cells.map { it.trim() }
    }

    // -----------------------------------------------------------------------
    // XLSX (zip + xml minimal)
    // -----------------------------------------------------------------------

    private fun parseXlsx(context: Context, uri: Uri): ParsedSheet? {
        val temp = File.createTempFile("import", ".xlsx", context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { input.copyTo(it) }
            } ?: return null

            ZipFile(temp).use { zip ->
                val sharedStrings = zip.getEntry("xl/sharedStrings.xml")?.let { entry ->
                    zip.getInputStream(entry).use { parseSharedStrings(it) }
                } ?: emptyList()

                val sheetEntry = zip.entries().asSequence()
                    .filter { it.name.matches(Regex("xl/worksheets/sheet\\d+\\.xml")) }
                    .minByOrNull { it.name.substringAfter("sheet").substringBefore(".xml").toIntOrNull() ?: Int.MAX_VALUE }
                    ?: return null

                val rows = zip.getInputStream(sheetEntry).use { parseWorksheet(it, sharedStrings) }
                if (rows.isEmpty()) return null
                val width = rows.maxOf { it.size }
                val normalized = rows.map { row -> row + List((width - row.size).coerceAtLeast(0)) { "" } }
                return ParsedSheet(normalized.first(), normalized.drop(1))
            }
        } catch (e: Exception) {
            return null
        } finally {
            temp.delete()
        }
    }

    private fun parseSharedStrings(input: java.io.InputStream): List<String> {
        val result = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setInput(input, "UTF-8")
        var current: StringBuilder? = null
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> if (parser.name == "si") current = StringBuilder()
                XmlPullParser.TEXT -> current?.append(parser.text)
                XmlPullParser.END_TAG -> if (parser.name == "si") {
                    result.add(current?.toString().orEmpty())
                    current = null
                }
            }
            event = parser.next()
        }
        return result
    }

    /** Lettres de tête d'une référence de cellule ("B12" -> "B") converties en index 0-based. */
    private fun columnIndex(cellRef: String): Int {
        val letters = cellRef.takeWhile { it.isLetter() }
        return letters.fold(0) { acc, c -> acc * 26 + (c.uppercaseChar() - 'A' + 1) } - 1
    }

    private fun parseWorksheet(input: java.io.InputStream, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        var currentType: String? = null
        var currentCol = -1
        var currentValue: StringBuilder? = null
        var inInlineStr = false

        val parser = Xml.newPullParser()
        parser.setInput(input, "UTF-8")
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "row" -> currentRow = mutableListOf()
                    "c" -> {
                        currentType = parser.getAttributeValue(null, "t")
                        currentCol = parser.getAttributeValue(null, "r")?.let { columnIndex(it) } ?: (currentRow.size)
                        while (currentRow.size < currentCol) currentRow.add("")
                    }
                    "v" -> currentValue = StringBuilder()
                    "t" -> if (currentType == "inlineStr") { inInlineStr = true; currentValue = StringBuilder() }
                }
                XmlPullParser.TEXT -> {
                    if (currentValue != null) currentValue!!.append(parser.text)
                }
                XmlPullParser.END_TAG -> when (parser.name) {
                    "c" -> {
                        val raw = currentValue?.toString().orEmpty()
                        val resolved = if (currentType == "s") raw.toIntOrNull()?.let { sharedStrings.getOrNull(it) } ?: "" else raw
                        while (currentRow.size <= currentCol) currentRow.add("")
                        if (currentCol >= 0) currentRow[currentCol] = resolved
                        currentValue = null
                        inInlineStr = false
                    }
                    "row" -> rows.add(currentRow)
                }
            }
            event = parser.next()
        }
        return rows
    }

    // -----------------------------------------------------------------------
    // Correspondance automatique des colonnes (devinette best-effort)
    // -----------------------------------------------------------------------

    private val fieldKeywords: Map<ImportField, List<String>> = mapOf(
        ImportField.NAME to listOf("nom", "titre", "name", "title", "jeu", "game", "designation", "libelle"),
        ImportField.TYPE to listOf("type", "categorie", "category"),
        ImportField.BRAND to listOf("marque", "brand", "editeur", "publisher", "developpeur", "developer"),
        ImportField.PLATFORM to listOf("plateforme", "platform", "console", "support", "systeme", "system"),
        ImportField.CONDITION to listOf("etat", "condition", "state"),
        ImportField.HAS_BOX to listOf("boite", "box", "boxed"),
        ImportField.HAS_MANUAL to listOf("notice", "manual", "manuel"),
        ImportField.YEAR to listOf("annee", "year", "sortie"),
        ImportField.GENRE to listOf("genre"),
        ImportField.PRICE to listOf("prix", "cote", "valeur", "price", "value"),
        ImportField.BARCODE to listOf("codebarre", "codebarres", "barcode", "ean", "gtin", "upc"),
        ImportField.DESCRIPTION to listOf("description", "desc", "resume", "synopsis"),
        ImportField.REGION to listOf("region", "zone")
    )

    private fun normalize(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "")
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")

    /** Devine la colonne de chaque champ à partir des en-têtes ; null = pas de correspondance trouvée. */
    fun guessMapping(headers: List<String>): Map<ImportField, Int?> {
        val normalizedHeaders = headers.map { normalize(it) }
        val used = mutableSetOf<Int>()
        val result = mutableMapOf<ImportField, Int?>()
        for (field in ImportField.values()) {
            val keywords = fieldKeywords[field].orEmpty()
            val match = normalizedHeaders.indices.firstOrNull { idx ->
                idx !in used && keywords.any { normalizedHeaders[idx].contains(it) }
            }
            result[field] = match
            if (match != null) used.add(match)
        }
        return result
    }

    // -----------------------------------------------------------------------
    // Ligne -> CollectionItem (tolérant : valeurs libres, casse, accents...)
    // -----------------------------------------------------------------------

    private fun cell(row: List<String>, mapping: Map<ImportField, Int?>, field: ImportField): String? =
        mapping[field]?.let { row.getOrNull(it) }?.trim()?.takeIf { it.isNotEmpty() }

    private fun matchEnum(text: String?, options: List<Pair<String, String>>): String? {
        if (text.isNullOrBlank()) return null
        val n = normalize(text)
        return options.firstOrNull { normalize(it.second) == n || normalize(it.second).contains(n) }?.first
    }

    private fun parseBool(text: String?, default: Boolean): Boolean {
        if (text.isNullOrBlank()) return default
        val n = normalize(text)
        return when {
            listOf("oui", "yes", "true", "1", "x", "vrai").any { n == it } -> true
            listOf("non", "no", "false", "0", "faux").any { n == it } -> false
            else -> default
        }
    }

    private fun parsePriceCents(text: String?, currencyRate: Double): Int? {
        if (text.isNullOrBlank()) return null
        val cleaned = text.replace(Regex("[^0-9,.\\-]"), "").replace(",", ".")
        val value = cleaned.toDoubleOrNull() ?: return null
        if (currencyRate <= 0.0) return null
        return ((value / currencyRate) * 100).roundToInt()
    }

    /** Construit un [CollectionItem] depuis une ligne ; null si le nom (champ obligatoire) manque. */
    fun buildItem(
        row: List<String>,
        mapping: Map<ImportField, Int?>,
        isWishlist: Boolean,
        currencyRate: Double
    ): CollectionItem? {
        val name = cell(row, mapping, ImportField.NAME) ?: return null

        val typeMatch = matchEnum(cell(row, mapping, ImportField.TYPE), ItemType.values().map { it.name to it.label })
        val type = typeMatch?.let { ItemType.valueOf(it) } ?: run {
            // Sans colonne Type mappée (ou valeur non reconnue), on devine depuis la plateforme :
            // un objet avec une console associée mais aucune mention "console" est presque
            // toujours un jeu, cas de très loin le plus fréquent dans une collection rétro.
            val platformText = normalize(cell(row, mapping, ImportField.PLATFORM).orEmpty())
            if (platformText.contains("accessoire") || platformText.contains("accessory")) ItemType.ACCESSOIRE
            else ItemType.JEU
        }

        val conditionMatch = matchEnum(cell(row, mapping, ImportField.CONDITION), Condition.values().map { it.name to it.label })
        val condition = conditionMatch?.let { Condition.valueOf(it) } ?: Condition.BON

        val regionMatch = matchEnum(cell(row, mapping, ImportField.REGION), Region.values().map { it.name to it.label })
        val region = regionMatch?.let { Region.valueOf(it) } ?: Region.PAL

        val priceCents = parsePriceCents(cell(row, mapping, ImportField.PRICE), currencyRate)

        return CollectionItem(
            type = type,
            name = name,
            brand = cell(row, mapping, ImportField.BRAND).orEmpty(),
            region = region,
            condition = condition,
            hasBox = parseBool(cell(row, mapping, ImportField.HAS_BOX), default = true),
            hasManual = parseBool(cell(row, mapping, ImportField.HAS_MANUAL), default = true),
            releaseYear = cell(row, mapping, ImportField.YEAR)?.toIntOrNull(),
            genre = cell(row, mapping, ImportField.GENRE),
            platform = cell(row, mapping, ImportField.PLATFORM)?.let { ConsoleRecognition.canonicalizePlatformList(it) },
            priceCents = priceCents,
            priceIsManual = priceCents != null,
            barcode = cell(row, mapping, ImportField.BARCODE),
            description = cell(row, mapping, ImportField.DESCRIPTION),
            isWishlist = isWishlist
        )
    }
}
