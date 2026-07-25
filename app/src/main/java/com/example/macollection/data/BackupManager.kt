package com.example.macollection.data

import android.content.Context
import android.net.Uri
import com.example.macollection.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Contenu complet d'une sauvegarde : toutes les tables de la base. */
/**
 * Tous les champs liste sont déclarés nullables : Gson construit cet objet par réflexion sans
 * jamais appeler le constructeur Kotlin (donc sans appliquer ses valeurs par défaut), si bien
 * qu'un champ absent d'une ANCIENNE sauvegarde (créée avant son ajout) reste à `null` à
 * l'exécution malgré un type Kotlin non-nullable — d'où l'échec silencieux observé en
 * restaurant une sauvegarde plus ancienne. On neutralise ça avec `?: emptyList()` à l'usage.
 */
private data class BackupPayload(
    val version: Int = 1,
    val collectionItems: List<CollectionItem>?,
    val priceHistory: List<PriceHistory>?,
    val itemPhotos: List<ItemPhoto>?,
    val customPresets: List<CustomPreset>?,
    val photoOverrides: List<PresetPhotoOverride>? = null
)

/**
 * Sauvegarde/restauration complète de la collection (données + photos personnelles) dans un
 * unique fichier .zip choisi par l'utilisateur (Téléchargements, Drive, clé USB...). Comme ce
 * fichier vit hors du stockage interne de l'app, il survit à une désinstallation — il n'existe
 * aucun moyen pour une app Android d'intercepter sa propre désinstallation pour poser une
 * question à ce moment-là, donc cette approche (fichier externe géré par l'utilisateur) est la
 * seule façon fiable de ne rien perdre en cas de crash/réinstallation.
 */
object BackupManager {

    private val gson = Gson()

    /** Plafond d'objets possédés en variante TEST (voir com.example.macollection.ui.TEST_ITEM_LIMIT). */
    private const val TEST_MAX_COLLECTION_ITEMS = 10

    suspend fun export(context: Context, db: AppDatabase, destUri: Uri): Boolean = withContext(Dispatchers.IO) { try {
        val collectionItems = db.collectionDao().observeAll().first()
        val itemPhotos = db.itemPhotoDao().getAll()
        val customPresets = db.customPresetDao().observeAll().first()
        val priceHistory = db.priceHistoryDao().getAll()
        val photoOverrides = db.presetPhotoOverrideDao().observeAll().first()

        val localFiles = (collectionItems.mapNotNull { it.imageUri } +
            itemPhotos.map { it.uri } +
            customPresets.mapNotNull { it.photoUri } +
            photoOverrides.map { it.photoUri })
            .filter { it.startsWith("file://") }
            .distinct()

        val payload = BackupPayload(
            collectionItems = collectionItems,
            priceHistory = priceHistory,
            itemPhotos = itemPhotos,
            customPresets = customPresets,
            photoOverrides = photoOverrides
        )

        context.contentResolver.openOutputStream(destUri)?.use { out ->
            ZipOutputStream(out).use { zip ->
                zip.putNextEntry(ZipEntry("data.json"))
                zip.write(gson.toJson(payload).toByteArray())
                zip.closeEntry()

                for (uriString in localFiles) {
                    val path = Uri.parse(uriString).path ?: continue
                    val file = File(path)
                    if (!file.exists()) continue
                    zip.putNextEntry(ZipEntry("photos/${file.name}"))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        } ?: return@withContext false
        true
    } catch (e: Exception) {
        false
    } }

    /** Remplace entièrement le contenu actuel par celui de la sauvegarde. */
    suspend fun import(context: Context, db: AppDatabase, srcUri: Uri): Boolean = withContext(Dispatchers.IO) { try {
        var payload: BackupPayload? = null
        val restoredFileNames = mutableSetOf<String>()

        context.contentResolver.openInputStream(srcUri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    when {
                        entry.name == "data.json" -> {
                            val json = zip.readBytes().toString(Charsets.UTF_8)
                            payload = gson.fromJson(json, BackupPayload::class.java)
                        }
                        entry.name.startsWith("photos/") -> {
                            val fileName = entry.name.removePrefix("photos/")
                            val outFile = File(context.filesDir, fileName)
                            outFile.outputStream().use { out -> zip.copyTo(out) }
                            restoredFileNames += fileName
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: return@withContext false

        val data = payload ?: return@withContext false

        // Variante TEST : le plafond de 10 objets possédés ne doit pas pouvoir être contourné en
        // important une sauvegarde partagée plus grosse. On ne garde que les 10 premiers objets de
        // collection (les souhaits, hors plafond, sont conservés). Constante de compilation IS_TEST
        // = non contournable (le code du cap est carrément absent des éditions normales via R8).
        val allItems = data.collectionItems ?: emptyList()
        val itemsToInsert = if (BuildConfig.IS_TEST) {
            allItems.filter { !it.isWishlist }.take(TEST_MAX_COLLECTION_ITEMS) + allItems.filter { it.isWishlist }
        } else allItems

        db.clearAllTables()
        db.collectionDao().insertAll(itemsToInsert)
        db.priceHistoryDao().insertAll(data.priceHistory ?: emptyList())
        db.itemPhotoDao().insertAll(data.itemPhotos ?: emptyList())
        db.customPresetDao().insertAll(data.customPresets ?: emptyList())
        db.presetPhotoOverrideDao().insertAll(data.photoOverrides ?: emptyList())
        true
    } catch (e: Exception) {
        false
    } }
}
