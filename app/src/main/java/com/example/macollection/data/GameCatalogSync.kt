package com.example.macollection.data

/**
 * Synchronise le catalogue de jeux d'une plateforme entière (voir [CachedGame]) : IGDB en source
 * principale (pagination complète via [IgdbCatalog.listByPlatform]), repli RAWG
 * ([GameCatalog.gamesForPlatformPage]) si IGDB n'est pas configuré ou ne renvoie rien.
 *
 * Déclenchée à la demande (première ouverture de l'onglet Jeux d'une console dans l'Encyclopédie,
 * voir [com.example.macollection.ui.AppViewModel.syncGamesIfNeeded]) — jamais en masse sur tout le
 * catalogue d'un coup, et jamais resynchronisée si déjà à jour depuis moins de 30 jours (même
 * logique que [com.example.macollection.ui.AppViewModel.cachedPresetPrice]) : les quotas gratuits
 * IGDB/RAWG, bien plus généreux que celui de Gemini mais non documentés dans ce projet, ne
 * doivent pas être épuisés par un simple parcours de l'Encyclopédie.
 */
object GameCatalogSync {

    private const val MAX_AGE_MS = 30L * 24 * 60 * 60 * 1000

    // Garde-fous contre une boucle interminable (plateforme mal cartographiée, réponse API
    // anormale...) : aucune console de ce catalogue ne dépasse ces volumes en pratique.
    private const val IGDB_PAGE_LIMIT = 40 // 40 x 500 = 20 000 jeux max
    private const val RAWG_PAGE_LIMIT = 20 // 20 x 40 = 800 jeux max (repli, sans pagination fine)

    /**
     * Synchronise [platformKey] si nécessaire (jamais synchronisé, ou périmé depuis >30 jours).
     * Renvoie vrai si une synchronisation a effectivement eu lieu (utile pour informer l'UI),
     * faux si le cache était déjà à jour OU si la synchronisation a échoué.
     */
    suspend fun syncIfNeeded(gameDao: CachedGameDao, stateDao: GameCatalogSyncStateDao, platformKey: Int): Boolean {
        val state = stateDao.get(platformKey)
        val now = System.currentTimeMillis()
        if (state?.status == "DONE" && state.lastSyncedAt != null && now - state.lastSyncedAt < MAX_AGE_MS) {
            return false
        }
        stateDao.upsert(GameCatalogSyncState(platformKey, "SYNCING", state?.gameCount ?: 0, state?.lastSyncedAt, null))
        return try {
            val games = fetchAll(platformKey, now)
            if (games.isEmpty()) {
                stateDao.upsert(
                    GameCatalogSyncState(
                        platformKey, "ERROR", state?.gameCount ?: 0, state?.lastSyncedAt,
                        "Aucun jeu trouvé (IGDB/RAWG indisponibles ou plateforme non prise en charge)"
                    )
                )
                false
            } else {
                gameDao.deleteForPlatform(platformKey)
                gameDao.insertAll(games)
                stateDao.upsert(GameCatalogSyncState(platformKey, "DONE", games.size, now, null))
                true
            }
        } catch (e: Exception) {
            stateDao.upsert(GameCatalogSyncState(platformKey, "ERROR", state?.gameCount ?: 0, state?.lastSyncedAt, e.message))
            false
        }
    }

    private suspend fun fetchAll(platformKey: Int, now: Long): List<CachedGame> {
        val igdbPlatformId = ConsolePlatforms.igdbPlatformIdFor(platformKey)
        if (IgdbCatalog.isConfigured() && igdbPlatformId != null) {
            val all = mutableListOf<IgdbCatalog.IgdbCatalogEntry>()
            var offset = 0
            var pages = 0
            while (pages < IGDB_PAGE_LIMIT) {
                val page = IgdbCatalog.listByPlatform(igdbPlatformId, offset)
                if (page.isEmpty()) break
                all += page
                pages++
                if (page.size < 500) break
                offset += 500
            }
            if (all.isNotEmpty()) {
                return all.map { it.toCachedGame(platformKey, now) }
            }
        }
        // Repli RAWG : uniquement si IGDB n'est pas configuré ou n'a rien trouvé pour cette
        // plateforme (jamais en complément — on ne veut pas de doublons entre les deux sources).
        if (GameCatalog.isConfigured()) {
            val all = mutableListOf<GameInfo>()
            var page = 1
            while (page <= RAWG_PAGE_LIMIT) {
                val batch = GameCatalog.gamesForPlatformPage(platformKey, page)
                if (batch.isEmpty()) break
                all += batch
                page++
            }
            return all.mapNotNull { it.toCachedGame(platformKey, now) }
        }
        return emptyList()
    }

    private fun IgdbCatalog.IgdbCatalogEntry.toCachedGame(platformKey: Int, importedAt: Long) = CachedGame(
        sourceId = id,
        source = "igdb",
        platformKey = platformKey,
        name = info.name,
        developer = info.developer,
        publisher = info.publisher,
        releaseYear = info.releaseYear,
        genres = info.genres,
        coverUrl = info.coverUrl,
        description = info.description,
        importedAt = importedAt
    )

    /** Null si [GameInfo.sourceId] est absent (nécessaire pour dédupliquer/rafraîchir par id). */
    private fun GameInfo.toCachedGame(platformKey: Int, importedAt: Long): CachedGame? {
        val id = sourceId?.toLong() ?: return null
        return CachedGame(
            sourceId = id,
            source = "rawg",
            platformKey = platformKey,
            name = name,
            developer = developer,
            publisher = publisher,
            releaseYear = releaseYear,
            genres = genres,
            coverUrl = coverUrl,
            description = description,
            importedAt = importedAt
        )
    }
}
