package com.example.macollection.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.text.Collator
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Export Excel du registre (collection + souhaits) en vrai fichier `.xlsx` (un onglet par
 * catégorie, AutoFiltre, cellules centrées, une photo par ligne ancrée exactement dans sa
 * cellule). Un `.xlsx` n'est qu'un ZIP de fichiers XML (format OOXML) : on l'écrit directement
 * avec `ZipOutputStream` et du texte, sans librairie externe — dans le même esprit que
 * [SpreadsheetImport], qui lit déjà un `.xlsx` importé de la même façon manuelle (ZIP + XML).
 * Volontairement pas d'Apache POI : fonctionne sur Android mais avec de vraies frictions connues
 * (API `javax.xml.stream` absente par défaut, d'où l'existence de forks Android dédiés) pour un
 * besoin qui ne demande qu'un sous-ensemble restreint et bien délimité d'OOXML.
 *
 * Multi-onglets et AutoFiltre (flèches de tri/filtre en en-tête) sont des fonctionnalités du
 * format `.xlsx` lui-même : l'ancienne version de cet export générait un document MHTML (tableau
 * HTML unique) qui ne peut techniquement produire ni plusieurs vrais onglets, ni de vraies
 * flèches de filtre Excel — d'où cette réécriture complète.
 */
object ExcelExport {

    private const val THUMBNAIL_MAX_DIMENSION = 200
    private const val THUMBNAIL_JPEG_QUALITY = 70

    // Photo | Statut | Nom | Marque | Région | État | Boîte | Notice | Année | Genre | Plateforme
    // | Prix | Description | Commentaires (colonne libre, toujours vide à l'export — laissée pour
    // que l'utilisateur y écrive ses propres notes dans Excel) — Type et Code-barres retirés (Type
    // devient implicite via l'onglet).
    private const val COLUMN_COUNT = 14
    private const val DESCRIPTION_COL_INDEX = 12
    // Nombre total de lignes mises en forme par onglet (en-tête compris), demandé explicitement
    // pour que la bordure/le centrage/le filtre couvrent toute la feuille et pas seulement les
    // lignes déjà occupées par un objet.
    private const val PADDED_ROW_COUNT = 1500
    // Largeurs de colonnes demandées explicitement par feuille (différentes d'une catégorie à
    // l'autre — ex. Description bien plus large sur "Jeux" que sur "Consoles"/"Accessoires").
    private val CONSOLE_COLUMN_WIDTHS = listOf(27.0, 11.0, 23.0, 16.0, 10.0, 10.0, 10.0, 10.0, 11.0, 11.0, 15.0, 10.0, 40.0, 30.0)
    private val ACCESSOIRE_COLUMN_WIDTHS = listOf(20.0, 11.0, 45.0, 16.0, 10.0, 10.0, 10.0, 10.0, 11.0, 11.0, 15.0, 10.0, 40.0, 30.0)
    private val JEU_COLUMN_WIDTHS = listOf(14.0, 11.0, 25.0, 16.0, 10.0, 10.0, 10.0, 10.0, 10.0, 15.0, 15.0, 10.0, 65.0, 44.0)
    private fun columnWidthsFor(type: ItemType): List<Double> = when (type) {
        ItemType.CONSOLE -> CONSOLE_COLUMN_WIDTHS
        ItemType.ACCESSOIRE -> ACCESSOIRE_COLUMN_WIDTHS
        ItemType.JEU -> JEU_COLUMN_WIDTHS
    }
    private val HEADERS = listOf(
        "Photo", "Statut", "Nom", "Marque", "Région", "État", "Boîte", "Notice",
        "Année", "Genre", "Plateforme", "Prix", "Description", "Commentaires"
    )
    // Hauteur minimale (un peu au-dessus des 3,2 cm ≈ 90,7 pt demandés pour la photo, marge de
    // sécurité pour ne pas la couper) ; grandit ensuite selon le texte de la Description (retour à
    // la ligne activé, colonne volontairement resserrée — voir [estimateRowHeight]) plutôt que de
    // rester figée et couper le texte.
    private const val MIN_ROW_HEIGHT_PT = 92.0
    private const val LINE_HEIGHT_PT = 14.0
    private const val ROW_HEIGHT_PADDING_PT = 12.0

    // Dimensions d'image demandées explicitement par feuille (hauteur commune 3,2 cm, largeur
    // variable) et conversion cm -> EMU (unité native OOXML pour les tailles de dessin ; 1 cm =
    // 360000 EMU, valeur exacte du standard, indépendante de toute résolution d'écran).
    private const val EMU_PER_CM = 360000.0
    private const val EMU_PER_PT = 12700.0
    private const val EMU_PER_PX = 9525.0
    private data class ImageSizeCm(val widthCm: Double, val heightCm: Double)
    private fun imageSizeFor(type: ItemType): ImageSizeCm = when (type) {
        ItemType.CONSOLE -> ImageSizeCm(widthCm = 5.0, heightCm = 3.2)
        ItemType.ACCESSOIRE -> ImageSizeCm(widthCm = 3.7, heightCm = 3.2)
        ItemType.JEU -> ImageSizeCm(widthCm = 2.7, heightCm = 3.2)
    }

    /** Estimation (approximative, sans les vraies métriques de police) du nombre de lignes que
     *  prendra [description] une fois repliée dans la colonne Description (largeur variable selon
     *  la feuille, voir [columnWidthsFor]), pour donner à la ligne une hauteur suffisante à ne pas
     *  couper le texte — jamais en dessous de [MIN_ROW_HEIGHT_PT] (place minimale pour la photo). */
    private fun estimateRowHeight(description: String, descriptionColWidth: Double): Double {
        if (description.isBlank()) return MIN_ROW_HEIGHT_PT
        val charsPerLine = (descriptionColWidth * 1.1).toInt().coerceAtLeast(10)
        val lineCount = description.split("\n").sumOf { segment ->
            if (segment.isEmpty()) 1 else kotlin.math.ceil(segment.length / charsPerLine.toDouble()).toInt().coerceAtLeast(1)
        }
        return maxOf(MIN_ROW_HEIGHT_PT, lineCount * LINE_HEIGHT_PT + ROW_HEIGHT_PADDING_PT)
    }

    // Index de style pour le corps normal des cellules (Nom, Marque, Prix, Description...).
    // Posé explicitement (`s=`) sur CHAQUE cellule de la plage A1:N1500, ligne par ligne, colonne
    // par colonne — y compris les lignes au-delà du dernier objet réel (voir [sheetXml]) — sur
    // demande explicite de l'utilisateur, en plus du style par défaut de colonne (`<col style="…">`,
    // conservé comme filet de sécurité redondant).
    private const val BODY_STYLE = 1
    private const val HEADER_STYLE = 2

    // État/Boîte/Notice (colonnes F/G/H) ne sont PLUS colorées via un style figé calculé à la
    // génération : demande explicite de vraies règles Excel natives (<conditionalFormatting>, voir
    // [CONDITIONAL_FORMATTING_XML]/[DXFS_XML]) qui se ré-évaluent dynamiquement si l'utilisateur
    // modifie le texte d'une cellule dans Excel après l'export — un style figé ne peut pas faire ça.
    // Ces cellules gardent BODY_STYLE comme base (bordure/alignement), et c'est la règle
    // conditionnelle qui superpose la couleur au moment de l'affichage.

    /** Redimensionne/compresse en miniature JPEG (fichier plus léger, chargement plus rapide). */
    private fun toThumbnailJpeg(bytes: ByteArray): ByteArray? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= THUMBNAIL_MAX_DIMENSION &&
            bounds.outHeight / (sampleSize * 2) >= THUMBNAIL_MAX_DIMENSION
        ) {
            sampleSize *= 2
        }
        val decoded = BitmapFactory.decodeByteArray(
            bytes, 0, bytes.size,
            BitmapFactory.Options().apply { inSampleSize = sampleSize }
        ) ?: return null

        val scale = THUMBNAIL_MAX_DIMENSION.toFloat() / maxOf(decoded.width, decoded.height)
        val thumbnail = if (scale < 1f) {
            Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt().coerceAtLeast(1), (decoded.height * scale).toInt().coerceAtLeast(1), true)
        } else decoded

        val out = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_JPEG_QUALITY, out)
        return out.toByteArray()
    }

    // Les photos "preset" des consoles/accessoires (ConsoleImages/AccessoryImages) pointent vers
    // des assets embarqués dans l'APK ("file:///android_asset/...", résolus normalement par Coil
    // via son AssetUriFetcher pour l'affichage à l'écran) — PAS des fichiers réels sur le disque.
    // Le traitement générique "file://" ci-dessous (File(uri.path)) échouait silencieusement dessus
    // (fichier introuvable), d'où la plupart des photos de consoles/accessoires manquantes à
    // l'export alors qu'elles s'affichent normalement dans l'appli.
    private fun readPhotoBytes(context: Context, uri: String): ByteArray? = try {
        when {
            uri.startsWith("file:///android_asset/") ->
                context.assets.open(uri.removePrefix("file:///android_asset/")).use { it.readBytes() }
            uri.startsWith("file://") -> {
                val path = Uri.parse(uri).path ?: return null
                val file = File(path)
                if (!file.exists()) null else file.readBytes()
            }
            else -> URL(uri).openConnection().apply {
                connectTimeout = 5000
                readTimeout = 5000
            }.getInputStream().use { it.readBytes() }
        }
    } catch (e: Exception) {
        null
    }

    private fun priceCell(cents: Int?): String {
        if (cents == null || cents == 0) return ""
        val rate = AppPrefs.currencyRates.value[AppPrefs.currency.value] ?: 1.0
        val symbol = CurrencyOptions.symbolFor(AppPrefs.currency.value)
        return String.format(Locale.FRANCE, "%.2f %s", (cents / 100.0) * rate, symbol)
    }

    private fun escapeXml(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    /** Colonne 0-based -> lettre(s) Excel ("A".."M" pour nos 13 colonnes). */
    private fun colLetter(index: Int): String {
        var n = index
        val sb = StringBuilder()
        do {
            sb.insert(0, 'A' + (n % 26))
            n = n / 26 - 1
        } while (n >= 0)
        return sb.toString()
    }

    private class SheetImage(val dataRowIndex: Int, val rId: String, val fileName: String, val bytes: ByteArray)

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    suspend fun export(context: Context, items: List<CollectionItem>, destUri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val collator = Collator.getInstance(Locale.FRANCE)
                // Un onglet par catégorie (CONSOLE/ACCESSOIRE/JEU), triés par nom A-Z par défaut —
                // l'AutoFiltre posé sur l'en-tête permet ensuite de retrier/filtrer sur n'importe
                // quelle colonne (Plateforme, État, Boîte, Notice compris). Ordre explicite
                // (Consoles, Accessoires, Jeux) demandé par le cahier des charges — distinct de
                // l'ordre de déclaration de ItemType (Console, Jeu, Accessoire).
                val sheetOrder = listOf(ItemType.CONSOLE, ItemType.ACCESSOIRE, ItemType.JEU)
                val sheets = sheetOrder.mapIndexed { sheetIndex, type ->
                    val sheetNum = sheetIndex + 1
                    val sheetItems = items.filter { it.type == type }.sortedWith(compareBy(collator) { it.name })
                    val images = mutableListOf<SheetImage>()
                    sheetItems.forEachIndexed { rowIndex, item ->
                        val raw = item.imageUri ?: return@forEachIndexed
                        val bytes = readPhotoBytes(context, raw)?.let { toThumbnailJpeg(it) } ?: return@forEachIndexed
                        val n = images.size + 1
                        images.add(SheetImage(rowIndex, "rId$n", "image${sheetNum}_$n.jpeg", bytes))
                    }
                    Triple(type, sheetItems, images)
                }

                context.contentResolver.openOutputStream(destUri)?.use { out ->
                    ZipOutputStream(out).use { zip ->
                        writeEntry(zip, "[Content_Types].xml", contentTypesXml(sheets.map { it.third.isNotEmpty() }))
                        writeEntry(zip, "_rels/.rels", ROOT_RELS_XML)
                        writeEntry(zip, "xl/workbook.xml", workbookXml(sheets.map { it.first }))
                        writeEntry(zip, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS_XML)
                        writeEntry(zip, "xl/styles.xml", STYLES_XML)

                        sheets.forEachIndexed { i, (type, sheetItems, images) ->
                            val sheetNum = i + 1
                            writeEntry(zip, "xl/worksheets/sheet$sheetNum.xml", sheetXml(sheetItems, images, type))
                            if (images.isNotEmpty()) {
                                writeEntry(zip, "xl/worksheets/_rels/sheet$sheetNum.xml.rels", sheetRelsXml(sheetNum))
                                writeEntry(zip, "xl/drawings/drawing$sheetNum.xml", drawingXml(images, type))
                                writeEntry(zip, "xl/drawings/_rels/drawing$sheetNum.xml.rels", drawingRelsXml(images))
                                for (img in images) {
                                    zip.putNextEntry(ZipEntry("xl/media/${img.fileName}"))
                                    zip.write(img.bytes)
                                    zip.closeEntry()
                                }
                            }
                        }
                    }
                } ?: return@withContext false
                true
            } catch (e: Exception) {
                false
            }
        }

    private fun contentTypesXml(sheetHasImages: List<Boolean>): String {
        val drawingOverrides = sheetHasImages.mapIndexedNotNull { i, has ->
            if (has) "<Override PartName=\"/xl/drawings/drawing${i + 1}.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.drawing+xml\"/>" else null
        }.joinToString("")
        val sheetOverrides = (1..sheetHasImages.size).joinToString("") {
            "<Override PartName=\"/xl/worksheets/sheet$it.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
            "<Default Extension=\"jpeg\" ContentType=\"image/jpeg\"/>" +
            "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
            "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
            sheetOverrides + drawingOverrides +
            "</Types>"
    }

    private val ROOT_RELS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
        "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
        "</Relationships>"

    private fun workbookXml(types: List<ItemType>): String {
        val sheetEls = types.mapIndexed { i, type ->
            "<sheet name=\"${escapeXml(type.labelPlural)}\" sheetId=\"${i + 1}\" r:id=\"rIdSheet${i + 1}\"/>"
        }.joinToString("")
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
            "<sheets>$sheetEls</sheets></workbook>"
    }

    private val WORKBOOK_RELS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
        "<Relationship Id=\"rIdSheet1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
        "<Relationship Id=\"rIdSheet2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/>" +
        "<Relationship Id=\"rIdSheet3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet3.xml\"/>" +
        "<Relationship Id=\"rIdStyles\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
        "</Relationships>"

    // Styles différentiels pour la mise en forme conditionnelle native (voir
    // [CONDITIONAL_FORMATTING_XML]) : contrairement à un style figé posé sur la cellule à la
    // génération, ces règles sont ré-évaluées par Excel à chaque changement de valeur — si
    // l'utilisateur retape "Bon" en "Neuf" dans Excel, la couleur suit automatiquement. Note OOXML :
    // dans un <dxf>, la couleur d'un remplissage "plein" se met dans <bgColor>, pas <fgColor>
    // (inversé par rapport à un <fill> normal de cellXfs) — c'est ainsi qu'Excel écrit lui-même ses
    // propres règles de mise en forme conditionnelle créées depuis le ruban.
    // 0=HS rouge 1=Mauvais orange 2=Bon jaune 3=Très bon (vert foncé, texte blanc) 4=Mint vert clair
    // 5=Neuf or 6=Oui vert 7=Non rouge
    private val DXFS_XML = "<dxfs count=\"8\">" +
        "<dxf><fill><patternFill><bgColor rgb=\"FFFF0000\"/></patternFill></fill></dxf>" +
        "<dxf><fill><patternFill><bgColor rgb=\"FFFFA500\"/></patternFill></fill></dxf>" +
        "<dxf><fill><patternFill><bgColor rgb=\"FFFFFF00\"/></patternFill></fill></dxf>" +
        "<dxf><font><color rgb=\"FFFFFFFF\"/></font><fill><patternFill><bgColor rgb=\"FF006400\"/></patternFill></fill></dxf>" +
        "<dxf><fill><patternFill><bgColor rgb=\"FF90EE90\"/></patternFill></fill></dxf>" +
        "<dxf><fill><patternFill><bgColor rgb=\"FFFFD700\"/></patternFill></fill></dxf>" +
        "<dxf><fill><patternFill><bgColor rgb=\"FF00B050\"/></patternFill></fill></dxf>" +
        "<dxf><fill><patternFill><bgColor rgb=\"FFFF0000\"/></patternFill></fill></dxf>" +
        "</dxfs>"

    // Styles (référencés par index depuis textCell/cols) : 0=inutilisé  1=corps  2=en-tête(gras) —
    // tous centrés horizontal+vertical, retour ligne, bordure moyenne (voir borderId 1 ci-dessous).
    // La coloration État/Boîte/Notice ne vit plus ici mais dans DXFS_XML (styles différentiels,
    // référencés uniquement par CONDITIONAL_FORMATTING_XML, jamais par un `s=` de cellule).
    private val STYLES_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
        "<fonts count=\"2\">" +
        "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
        "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
        "</fonts>" +
        "<fills count=\"3\">" +
        "<fill><patternFill patternType=\"none\"/></fill>" +
        "<fill><patternFill patternType=\"gray125\"/></fill>" +
        "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFD9E2F3\"/><bgColor indexed=\"64\"/></patternFill></fill>" + // 2 corps (bleu très clair)
        "</fills>" +
        // borderId 0 = aucun (requis par le schéma, non utilisé) ; borderId 1 = trait NOIR MOYEN
        // ("medium", demandé explicitement — un trait "thin" avait été jugé insuffisant) sur les 4
        // côtés de chaque cellule.
        "<borders count=\"2\">" +
        "<border><left/><right/><top/><bottom/><diagonal/></border>" +
        "<border>" +
        "<left style=\"medium\"><color rgb=\"FF000000\"/></left>" +
        "<right style=\"medium\"><color rgb=\"FF000000\"/></right>" +
        "<top style=\"medium\"><color rgb=\"FF000000\"/></top>" +
        "<bottom style=\"medium\"><color rgb=\"FF000000\"/></bottom>" +
        "<diagonal/>" +
        "</border>" +
        "</borders>" +
        // Le même alignement centré est aussi posé sur le style de base (cellStyleXfs), pas
        // seulement en override par cellule (applyAlignment="1" sur chaque xf ci-dessous) : filet de
        // sécurité pour un lecteur qui n'honorerait pas correctement ce drapeau d'override.
        "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf></cellStyleXfs>" +
        "<cellXfs count=\"3\">" +
        "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>" +
        "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>" + // 1 corps
        "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>" + // 2 en-tête
        "</cellXfs>" +
        DXFS_XML +
        "</styleSheet>"

    // Deux blocs <conditionalFormatting> partagés par les 3 feuilles (F/G/H désignent toujours
    // État/Boîte/Notice, quelle que soit la catégorie) : opérateur "equal" sur le texte exact de la
    // cellule, plage 2..PADDED_ROW_COUNT (ligne 1 = en-tête, non concernée). `priority` doit juste
    // être unique et positif au sein de la feuille ; l'ordre n'a pas d'incidence ici car les
    // libellés testés sont mutuellement exclusifs.
    private val CONDITIONAL_FORMATTING_XML = run {
        val last = PADDED_ROW_COUNT
        val etatLabels = listOf("HS" to 0, "Mauvais" to 1, "Bon" to 2, "Très bon" to 3, "Mint" to 4, "Neuf" to 5)
        val ouiNonLabels = listOf("Oui" to 6, "Non" to 7)
        val etatRules = etatLabels.mapIndexed { i, (label, dxfId) ->
            "<cfRule type=\"cellIs\" dxfId=\"$dxfId\" priority=\"${i + 1}\" operator=\"equal\"><formula>&quot;${escapeXml(label)}&quot;</formula></cfRule>"
        }.joinToString("")
        val ouiNonRules = ouiNonLabels.mapIndexed { i, (label, dxfId) ->
            "<cfRule type=\"cellIs\" dxfId=\"$dxfId\" priority=\"${etatLabels.size + i + 1}\" operator=\"equal\"><formula>&quot;${escapeXml(label)}&quot;</formula></cfRule>"
        }.joinToString("")
        "<conditionalFormatting sqref=\"F2:F$last\">$etatRules</conditionalFormatting>" +
            "<conditionalFormatting sqref=\"G2:G$last H2:H$last\">$ouiNonRules</conditionalFormatting>"
    }

    // [style] nul = pas d'attribut `s=` sur la cellule : elle hérite du style par défaut de sa
    // colonne (`<col style="…">`, voir [sheetXml]) plutôt que d'en référencer un explicitement.
    private fun textCell(col: Int, row: Int, text: String, style: Int?): String {
        val ref = "${colLetter(col)}$row"
        val styleAttr = if (style != null) " s=\"$style\"" else ""
        if (text.isBlank()) return "<c r=\"$ref\"$styleAttr/>"
        return "<c r=\"$ref\" t=\"inlineStr\"$styleAttr><is><t xml:space=\"preserve\">${escapeXml(text)}</t></is></c>"
    }

    private fun sheetXml(items: List<CollectionItem>, images: List<SheetImage>, type: ItemType): String {
        val lastDataRow = items.size + 1
        // Filtre/plage étendus à PADDED_ROW_COUNT lignes par feuille (demande explicite de
        // l'utilisateur), même au-delà du dernier objet — permet aussi d'ajouter des objets plus
        // tard sans perdre la plage de filtre.
        val lastRow = maxOf(lastDataRow, PADDED_ROW_COUNT)
        val columnWidths = columnWidthsFor(type)
        val descriptionColWidth = columnWidths[DESCRIPTION_COL_INDEX]
        // `style="…"` sur chaque `<col>` = filet de sécurité redondant (voir [BODY_STYLE)) : la
        // mise en forme réelle vient du `s=` posé explicitement sur chaque cellule ci-dessous.
        val cols = columnWidths.mapIndexed { i, w ->
            "<col min=\"${i + 1}\" max=\"${i + 1}\" width=\"$w\" customWidth=\"1\" style=\"$BODY_STYLE\"/>"
        }.joinToString("")
        val headerRow = "<row r=\"1\">" + HEADERS.mapIndexed { c, h -> textCell(c, 1, h, HEADER_STYLE) }.joinToString("") + "</row>"
        val dataRows = items.mapIndexed { i, item ->
            val row = i + 2
            val description = item.description.orEmpty()
            // BODY_STYLE explicite sur chaque cellule, y compris État/Boîte/Notice : leur couleur
            // vient d'une vraie règle Excel native superposée par-dessus (CONDITIONAL_FORMATTING_XML
            // plus bas), pas d'un style de cellule différent.
            val cells = listOf(
                textCell(0, row, "", BODY_STYLE),
                textCell(1, row, if (item.isWishlist) "Souhait" else "Collection", BODY_STYLE),
                textCell(2, row, item.name, BODY_STYLE),
                textCell(3, row, item.brand, BODY_STYLE),
                textCell(4, row, item.region.label, BODY_STYLE),
                textCell(5, row, item.condition.label, BODY_STYLE),
                textCell(6, row, if (item.hasBox) "Oui" else "Non", BODY_STYLE),
                textCell(7, row, if (item.hasManual) "Oui" else "Non", BODY_STYLE),
                textCell(8, row, item.releaseYear?.toString().orEmpty(), BODY_STYLE),
                textCell(9, row, item.genre.orEmpty(), BODY_STYLE),
                // Normalisation "au cas où" (déjà faite en amont à l'enregistrement et par la
                // migration au lancement, voir ConsoleRecognition/AppViewModel) — filet de sécurité
                // pour qu'un export ne montre jamais deux libellés différents pour la même console.
                textCell(10, row, ConsoleRecognition.canonicalize(item.platform.orEmpty()), BODY_STYLE),
                textCell(11, row, priceCell(item.priceCents), BODY_STYLE),
                textCell(DESCRIPTION_COL_INDEX, row, description, BODY_STYLE),
                textCell(13, row, "", BODY_STYLE) // Commentaires : toujours vide à l'export, stylée quand même.
            ).joinToString("")
            "<row r=\"$row\" ht=\"${estimateRowHeight(description, descriptionColWidth)}\" customHeight=\"1\">$cells</row>"
        }.joinToString("")
        // Bloc de remplissage explicite (une cellule stylée par colonne par ligne, jusqu'à
        // PADDED_ROW_COUNT) au-delà du dernier objet réel, demandé explicitement pour que la mise en
        // forme couvre visiblement toute la plage même sans données.
        val paddingRows = if (lastDataRow < lastRow) {
            (lastDataRow + 1..lastRow).joinToString("") { row ->
                val cells = (0 until COLUMN_COUNT).joinToString("") { col -> "<c r=\"${colLetter(col)}$row\" s=\"$BODY_STYLE\"/>" }
                "<row r=\"$row\">$cells</row>"
            }
        } else ""
        val lastCol = colLetter(COLUMN_COUNT - 1)
        val drawingEl = if (images.isNotEmpty()) "<drawing r:id=\"rIdDrawing\"/>" else ""
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
            "<dimension ref=\"A1:$lastCol$lastRow\"/>" +
            "<sheetFormatPr defaultRowHeight=\"15\"/>" +
            "<cols>$cols</cols>" +
            "<sheetData>$headerRow$dataRows$paddingRows</sheetData>" +
            "<autoFilter ref=\"A1:$lastCol$lastRow\"/>" +
            CONDITIONAL_FORMATTING_XML +
            drawingEl +
            "</worksheet>"
    }

    private fun sheetRelsXml(sheetNum: Int): String =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rIdDrawing\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing\" Target=\"../drawings/drawing$sheetNum.xml\"/>" +
            "</Relationships>"

    /**
     * Une image par ligne, ancrée en `oneCellAnchor` avec une taille EXPLICITE (`<xdr:ext cx cy>`,
     * en EMU) selon [imageSizeFor] — demandé explicitement pour que chaque photo ait une taille
     * physique fixe (cm) au lieu de simplement remplir sa cellule. `colOff`/`rowOff` décalent le
     * point d'ancrage pour centrer approximativement l'image dans la cellule Photo (colonne A) :
     * conversion largeur de colonne (unités "caractères" Excel) -> pixels via l'approximation
     * standard largeur_px ≈ largeur_car × 7 + 5 (police par défaut Calibri 11) ; hauteur de ligne
     * via [MIN_ROW_HEIGHT_PT], qui est justement calée juste au-dessus de la hauteur d'image.
     *
     * Pas de `<a:xfrm>` dans `<xdr:spPr>` : un essai précédent y avait mis un `<a:ext cx="0" cy="0"/>`
     * (extent à zéro, hors du type "coordonnée positive" attendu par le schéma) — cause probable
     * d'un bug observé où Excel réparait silencieusement le classeur en perdant l'ancrage des
     * photos. La taille faisant déjà foi via `<xdr:ext>` au niveau de l'ancre, `<a:xfrm>` reste
     * absent (facultatif pour un `<xdr:pic>`), évitant de raviver ce risque.
     */
    private fun drawingXml(images: List<SheetImage>, type: ItemType): String {
        val size = imageSizeFor(type)
        val cx = (size.widthCm * EMU_PER_CM).toLong()
        val cy = (size.heightCm * EMU_PER_CM).toLong()
        val colWidthPx = columnWidthsFor(type)[0] * 7 + 5
        val colOffset = (((colWidthPx * EMU_PER_PX) - cx) / 2).coerceAtLeast(0.0).toLong()
        val rowOffset = (((MIN_ROW_HEIGHT_PT * EMU_PER_PT) - cy) / 2).coerceAtLeast(0.0).toLong()
        val anchors = images.mapIndexed { i, img ->
            val sheetRow0 = img.dataRowIndex + 1 // ligne de données (0-based dans la feuille : header=0)
            "<xdr:oneCellAnchor>" +
                "<xdr:from><xdr:col>0</xdr:col><xdr:colOff>$colOffset</xdr:colOff><xdr:row>$sheetRow0</xdr:row><xdr:rowOff>$rowOffset</xdr:rowOff></xdr:from>" +
                "<xdr:ext cx=\"$cx\" cy=\"$cy\"/>" +
                "<xdr:pic>" +
                "<xdr:nvPicPr><xdr:cNvPr id=\"${i + 2}\" name=\"Photo ${i + 1}\"/><xdr:cNvPicPr><a:picLocks noChangeAspect=\"1\"/></xdr:cNvPicPr></xdr:nvPicPr>" +
                "<xdr:blipFill><a:blip r:embed=\"${img.rId}\"/><a:stretch><a:fillRect/></a:stretch></xdr:blipFill>" +
                "<xdr:spPr><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></xdr:spPr>" +
                "</xdr:pic><xdr:clientData/></xdr:oneCellAnchor>"
        }.joinToString("")
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<xdr:wsDr xmlns:xdr=\"http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing\" " +
            "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
            anchors + "</xdr:wsDr>"
    }

    private fun drawingRelsXml(images: List<SheetImage>): String {
        val rels = images.joinToString("") { img ->
            "<Relationship Id=\"${img.rId}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"../media/${img.fileName}\"/>"
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">$rels</Relationships>"
    }
}
