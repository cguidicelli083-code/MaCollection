package com.example.macollection.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collection_items")
    fun observeAll(): Flow<List<CollectionItem>>

    @Insert suspend fun insert(item: CollectionItem): Long
    @Insert suspend fun insertAll(items: List<CollectionItem>)
    @Update suspend fun update(item: CollectionItem)
    @Delete suspend fun delete(item: CollectionItem)
}

@Dao
interface PriceHistoryDao {
    @Insert suspend fun insert(entry: PriceHistory)
    @Insert suspend fun insertAll(entries: List<PriceHistory>)

    @Query("SELECT * FROM price_history WHERE itemId = :itemId ORDER BY timestamp ASC")
    suspend fun forItem(itemId: Long): List<PriceHistory>

    @Query("SELECT * FROM price_history WHERE itemId = :itemId ORDER BY timestamp DESC LIMIT 1")
    suspend fun latest(itemId: Long): PriceHistory?

    @Query("SELECT * FROM price_history")
    suspend fun getAll(): List<PriceHistory>
}

@Dao
interface ItemPhotoDao {
    @Query("SELECT * FROM item_photos WHERE itemId = :itemId ORDER BY position ASC, id ASC")
    fun observeForItem(itemId: Long): Flow<List<ItemPhoto>>

    @Query("SELECT * FROM item_photos")
    suspend fun getAll(): List<ItemPhoto>

    @Insert suspend fun insert(photo: ItemPhoto): Long
    @Insert suspend fun insertAll(photos: List<ItemPhoto>)
    @Delete suspend fun delete(photo: ItemPhoto)

    @Query("DELETE FROM item_photos WHERE itemId = :itemId")
    suspend fun deleteForItem(itemId: Long)
}

@Dao
interface CustomPresetDao {
    @Query("SELECT * FROM custom_presets ORDER BY name ASC")
    fun observeAll(): Flow<List<CustomPreset>>

    @Insert suspend fun insert(preset: CustomPreset): Long
    @Insert suspend fun insertAll(presets: List<CustomPreset>)
    @Update suspend fun update(preset: CustomPreset)
    @Delete suspend fun delete(preset: CustomPreset)
}

@Dao
interface PresetPhotoOverrideDao {
    @Query("SELECT * FROM preset_photo_overrides")
    fun observeAll(): Flow<List<PresetPhotoOverride>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsert(override: PresetPhotoOverride)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAll(overrides: List<PresetPhotoOverride>)

    @Query("DELETE FROM preset_photo_overrides WHERE presetName = :name")
    suspend fun delete(name: String)
}

@Dao
interface PlayerProgressDao {
    @Query("SELECT * FROM player_progress WHERE id = 0")
    fun observe(): Flow<PlayerProgress?>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: PlayerProgress)
}

@Dao
interface UnlockedItemDao {
    @Query("SELECT * FROM unlocked_items")
    fun observeAll(): Flow<List<UnlockedItem>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insert(item: UnlockedItem)
}

@Dao
interface GameHighScoreDao {
    @Query("SELECT * FROM game_high_scores")
    fun observeAll(): Flow<List<GameHighScore>>

    @Query("SELECT * FROM game_high_scores WHERE gameId = :gameId")
    suspend fun get(gameId: String): GameHighScore?

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsert(score: GameHighScore)
}

/** Migration 3 -> 4 : ajoute la table d'historique de prix SANS effacer les données. */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `price_history` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`itemId` INTEGER NOT NULL, `priceCents` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)"
        )
    }
}

/** Migration 4 -> 5 : ajoute la table des photos de galerie par objet SANS effacer les données. */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `item_photos` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`itemId` INTEGER NOT NULL, `uri` TEXT NOT NULL, " +
                "`position` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)"
        )
    }
}

/**
 * Migration 5 -> 6 : ajoute `priceIsManual` (collection + souhaits) SANS effacer les données.
 * Les prix déjà enregistrés sont considérés comme automatiques (0/false) par défaut :
 * seules les prochaines saisies manuelles seront protégées contre l'actualisation eBay.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `collection_items` ADD COLUMN `priceIsManual` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `wishlist_items` ADD COLUMN `priceIsManual` INTEGER NOT NULL DEFAULT 0")
    }
}

/** Migration 6 -> 7 : ajoute `info` (message de cote) à la collection, SANS effacer les données. */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `collection_items` ADD COLUMN `info` TEXT")
    }
}

/** Migration 7 -> 8 : ajoute `rawgId` (collection + souhaits) SANS effacer les données. */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `collection_items` ADD COLUMN `rawgId` INTEGER")
        db.execSQL("ALTER TABLE `wishlist_items` ADD COLUMN `rawgId` INTEGER")
    }
}

/** Migration 8 -> 9 : ajoute la table des fiches de catalogue personnalisées, SANS effacer les données. */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `custom_presets` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`type` TEXT NOT NULL, `brand` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                "`year` INTEGER, `consoleOrKind` TEXT NOT NULL, `description` TEXT NOT NULL, " +
                "`photoUri` TEXT, `createdAt` INTEGER NOT NULL)"
        )
    }
}

/** Migration 9 -> 10 : ajoute la table des photos personnalisées de bibliothèque, SANS effacer les données. */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `preset_photo_overrides` " +
                "(`presetName` TEXT NOT NULL, `photoUri` TEXT NOT NULL, PRIMARY KEY(`presetName`))"
        )
    }
}

/**
 * Migration 10 -> 11 : les souhaits (acquisitions futures) utilisent désormais la même table/
 * fiche que la collection (`collection_items` + colonne `isWishlist`), pour avoir exactement le
 * même formulaire (photo, galerie, description...) qu'un objet possédé, simplement exclu de la
 * valeur totale. Les souhaits existants sont migrés (sans perte) puis l'ancienne table supprimée.
 */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `collection_items` ADD COLUMN `isWishlist` INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "INSERT INTO `collection_items` " +
                "(type, name, brand, region, condition, hasBox, hasManual, releaseYear, genre, " +
                "platform, priceCents, priceIsManual, barcode, description, imageUri, info, " +
                "rawgId, isWishlist, createdAt) " +
                "SELECT type, name, brand, region, 'BON', 1, 1, NULL, NULL, " +
                "NULL, priceCents, priceIsManual, NULL, NULL, NULL, info, " +
                "rawgId, 1, createdAt " +
                "FROM `wishlist_items`"
        )
        db.execSQL("DROP TABLE `wishlist_items`")
    }
}

/**
 * Migration 11 -> 12 : ajoute les tables de gamification (points, déblocages, meilleurs
 * scores) SANS effacer les données existantes de la collection.
 */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `player_progress` " +
                "(`id` INTEGER NOT NULL, `points` INTEGER NOT NULL, `lifetimePoints` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `unlocked_items` " +
                "(`itemId` TEXT NOT NULL, `unlockedAt` INTEGER NOT NULL, PRIMARY KEY(`itemId`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `game_high_scores` " +
                "(`gameId` TEXT NOT NULL, `bestScore` INTEGER NOT NULL, `playedAt` INTEGER NOT NULL, PRIMARY KEY(`gameId`))"
        )
    }
}

/** Migration 12 -> 13 : ajoute `descriptionLang` (langue de la description traduite) SANS effacer les données. */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `collection_items` ADD COLUMN `descriptionLang` TEXT")
    }
}

/** Migration 13 -> 14 : ajoute `sourceUrl` (URL source pour l'import automatique) SANS effacer les données. */
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `collection_items` ADD COLUMN `sourceUrl` TEXT")
    }
}

/**
 * Migration 14 -> 15 : ajoute `checksum` (protection anti-triche, voir [IntegrityGuard]) aux
 * tables de progression/déblocages. Recalcule une empreinte valide pour les lignes déjà
 * existantes (sinon les points/déblocages légitimes des utilisateurs actuels seraient vus comme
 * altérés au premier lancement après la mise à jour).
 */
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `player_progress` ADD COLUMN `checksum` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `unlocked_items` ADD COLUMN `checksum` TEXT NOT NULL DEFAULT ''")
        db.query("SELECT id, points, lifetimePoints FROM player_progress").use { c ->
            while (c.moveToNext()) {
                val id = c.getInt(0)
                val points = c.getInt(1)
                val lifetimePoints = c.getInt(2)
                db.execSQL(
                    "UPDATE player_progress SET checksum = ? WHERE id = ?",
                    arrayOf(IntegrityGuard.signProgress(points, lifetimePoints), id)
                )
            }
        }
        db.query("SELECT itemId, unlockedAt FROM unlocked_items").use { c ->
            while (c.moveToNext()) {
                val itemId = c.getString(0)
                val unlockedAt = c.getLong(1)
                db.execSQL(
                    "UPDATE unlocked_items SET checksum = ? WHERE itemId = ?",
                    arrayOf(IntegrityGuard.signUnlock(itemId, unlockedAt), itemId)
                )
            }
        }
    }
}

/** Migration 15 -> 16 : ajoute `priceIsAiEstimate` (cote estimée par IA, voir GeminiVision.estimatePrice) SANS effacer les données. */
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `collection_items` ADD COLUMN `priceIsAiEstimate` INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [CollectionItem::class, PriceHistory::class, ItemPhoto::class, CustomPreset::class, PresetPhotoOverride::class,
        PlayerProgress::class, UnlockedItem::class, GameHighScore::class],
    version = 16,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun itemPhotoDao(): ItemPhotoDao
    abstract fun customPresetDao(): CustomPresetDao
    abstract fun presetPhotoOverrideDao(): PresetPhotoOverrideDao
    abstract fun playerProgressDao(): PlayerProgressDao
    abstract fun unlockedItemDao(): UnlockedItemDao
    abstract fun gameHighScoreDao(): GameHighScoreDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "macollection.db"
            )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
        }
    }
}
