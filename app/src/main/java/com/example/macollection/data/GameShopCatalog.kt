package com.example.macollection.data

import com.example.macollection.BuildConfig

/** Catégorie d'item de la Boutique des Jeux. */
enum class ShopItemCategory { ARCADE_GAME, SKIN, FEATURE }

/** Un item achetable dans la Boutique avec les points gagnés en jouant (Quiz, Pong, pubs récompensées). */
data class ShopItem(
    val id: String,
    val category: ShopItemCategory,
    val title: String,
    val description: String,
    val cost: Int
)

/**
 * Catalogue de la Boutique des Jeux : les 4 jeux d'arcade à débloquer (Pong est offert dès le
 * départ), des thèmes visuels, et des fonctionnalités premium (export Excel, sauvegardes
 * supplémentaires, pub réduite, accès total).
 *
 * Tous les coûts ci-dessous sont des valeurs PROVISOIRES — l'utilisateur a explicitement
 * demandé à les ajuster plus tard une fois l'économie de points testée en conditions réelles.
 */
object GameShopCatalog {

    /** Nombre d'exports de sauvegarde gratuits avant qu'un pack supplémentaire soit requis. */
    const val FREE_BACKUP_EXPORTS = 5

    /** Nombre d'exports ajoutés par un pack "+5 sauvegardes" (rachetable plusieurs fois). */
    const val BACKUP_PACK_SIZE = 5

    /**
     * Nombre maximal de pubs récompensées (+points) comptabilisées par jour (voir
     * [AppPrefs.canWatchAdForPoints]). Sans ce plafond, la pub étant illimitée et opt-in, un
     * joueur motivé pourrait farmer le Premium en une seule session sans jamais payer ni générer
     * un volume de pub étalé dans le temps — ce plafond force un étalement sur plusieurs jours.
     */
    const val DAILY_AD_REWARD_CAP = 8

    /**
     * Nombre de scans code-barres gratuits par jour avant de devoir regarder une pub récompensée
     * pour continuer (voir [AppPrefs.canScanBarcodeFree]) — jamais Premium/V2SP, toujours
     * illimité pour eux. Sert surtout à répartir dans le temps le quota QUOTIDIEN PARTAGÉ (par
     * toute l'appli, pas par utilisateur) des bases de codes-barres gratuites (UPCitemdb, Barcode
     * Lookup, Barcode Spider — ~100 requêtes/jour chacune) : sans ce frein, quelques utilisateurs
     * actifs suffiraient à épuiser ce quota commun en quelques minutes le matin.
     */
    const val FREE_DAILY_BARCODE_SCANS = 8

    // Identifiants des jeux d'arcade (utilisés à la fois pour le déblocage et le high-score).
    const val PONG = "pong"
    const val ARKANOID = "arkanoid"
    const val SPACE_INVADERS = "space_invaders"
    const val CENTIPEDE = "centipede"
    const val PACMAN = "pacman"
    const val TETRIS = "tetris"
    const val SNAKE = "snake"
    const val SIMON = "simon"
    const val FROGGER = "frogger"
    const val QUIZ = "quiz"
    const val MINER = "miner"
    const val ORCHARD = "orchard"
    const val BOMB_HUNTER = "bomb_hunter"

    // Identifiants des items de la boutique (hors jeux d'arcade, gérés séparément ci-dessus).
    const val EXCEL_EXPORT = "excel_export"
    const val EXTRA_BACKUPS_5 = "extra_backups_5"
    const val ADS_REDUCED = "ads_reduced"
    const val PREMIUM_ALL_ACCESS = "premium_all_access"
    const val SKIN_BTTF = "skin_bttf"
    const val SKIN_STARWARS = "skin_starwars"
    const val SKIN_GOONIES = "skin_goonies"
    const val SKIN_GHOSTBUSTERS = "skin_ghostbusters"

    /** Jeux d'arcade débloqués gratuitement dès le premier lancement. */
    val defaultUnlockedGames = setOf(PONG)

    /** Jeux d'arcade à débloquer avec des points (Pong est déjà offert), classés par date de
     *  sortie officielle du jeu original. */
    // Noms de jeux et de thèmes GÉNÉRIQUES uniquement (aucune marque déposée : pas de « Tetris »,
    // « Pac-Man », « Star Wars »... — exigence légale pour la publication sur le Play Store).
    // Les mécaniques restent des implémentations 100 % originales de concepts du domaine public.
    /**
     * Mineur/Croque-Fruits/Artificier encore en test (l'utilisateur veut les valider plus
     * sérieusement avant de les exposer aux joueurs) : masqués de la Boutique/du Hub en
     * production (variantes full/noads), mais toujours visibles en variante test/debug pour
     * continuer à les tester sans avoir à les retirer puis remettre à chaque fois.
     */
    private val gamesUnderTest = listOf(
        ShopItem(MINER, ShopItemCategory.ARCADE_GAME, "Mineur", "1984 — Débloque le mineur qui creuse et évite les rochers.", 350),
        ShopItem(ORCHARD, ShopItemCategory.ARCADE_GAME, "Croque-Fruits", "1981 — Débloque le croqueur de fruits face aux insectes.", 350),
        ShopItem(BOMB_HUNTER, ShopItemCategory.ARCADE_GAME, "Artificier", "1984 — Débloque l'artificier qui vole plané entre les plateformes.", 350)
    )

    val lockedGames: List<ShopItem> = listOf(
        ShopItem(SNAKE, ShopItemCategory.ARCADE_GAME, "Serpent", "1976 — Débloque le serpent qui grandit.", 350),
        ShopItem(SPACE_INVADERS, ShopItemCategory.ARCADE_GAME, "Invasion", "1978 — Débloque le jeu de tir spatial.", 350),
        ShopItem(SIMON, ShopItemCategory.ARCADE_GAME, "Mémo Couleurs", "1978 — Débloque le jeu de mémoire des couleurs.", 350),
        ShopItem(PACMAN, ShopItemCategory.ARCADE_GAME, "Glouton", "1980 — Débloque le glouton du labyrinthe.", 350),
        ShopItem(CENTIPEDE, ShopItemCategory.ARCADE_GAME, "Mille-Pattes", "1981 — Débloque la chasse au mille-pattes.", 350),
        ShopItem(FROGGER, ShopItemCategory.ARCADE_GAME, "Grenouille", "1981 — Débloque la traversée de la grenouille.", 350),
        ShopItem(TETRIS, ShopItemCategory.ARCADE_GAME, "Blocs", "1984 — Débloque le puzzle de blocs qui tombent.", 350),
        ShopItem(ARKANOID, ShopItemCategory.ARCADE_GAME, "Casse-Briques", "1986 — Débloque le casse-briques à bonus.", 350)
    // Visibles sur "restricted" (IS_TEST) et "noads" (UNLOCK_ALL, usage perso/test au quotidien),
    // masqués uniquement sur "full" — la seule variante réellement publiée sur le Play Store.
    ) + (if (BuildConfig.IS_TEST || BuildConfig.UNLOCK_ALL) gamesUnderTest else emptyList())

    /**
     * Items non-jeu de la boutique (skins et fonctionnalités premium).
     * Coûts rééquilibrés le 2026-07-12 : le Premium (accès total, y compris la suppression des
     * pubs) doit rester nettement AU-DESSUS de la somme de tous les déblocages individuels
     * (350×8 + 200×4 + 400 + 3000 = 7 000), sinon grinder les items un par un revient moins cher
     * que le Premium et personne ne vise plus la voie payante — qui reste le levier le plus
     * rentable. Fixé à 15000 (2026-07-12) : assez dissuasif pour pousser vers l'achat réel sans
     * sembler totalement inatteignable (au-delà, risque de mauvais avis « prix gratuit mensonger »).
     */
    val shopItems: List<ShopItem> = listOf(
        ShopItem(SKIN_BTTF, ShopItemCategory.SKIN, "Thème Voyage temporel", "⚡ Voiture grise, trainées de feu rouges et lueur bleue du voyage dans le temps.", 200),
        ShopItem(SKIN_STARWARS, ShopItemCategory.SKIN, "Thème Galaxie", "🌠⚔️ Jaune légendaire sur l'espace noir, éclats d'énergie bleus. En route pour les étoiles !", 200),
        ShopItem(SKIN_GOONIES, ShopItemCategory.SKIN, "Thème Chasse au trésor", "🗺️💰 Or du trésor et vert lagon, cap sur la crique aux pirates.", 200),
        ShopItem(SKIN_GHOSTBUSTERS, ShopItemCategory.SKIN, "Thème Ectoplasme", "👻⚡ Vert ectoplasme et rouge d'alerte dans la nuit new-yorkaise.", 200),
        ShopItem(EXCEL_EXPORT, ShopItemCategory.FEATURE, "Export Excel", "Débloque l'export de la collection au format Excel.", 400),
        ShopItem(EXTRA_BACKUPS_5, ShopItemCategory.FEATURE, "+5 sauvegardes", "Ajoute 5 exports de sauvegarde supplémentaires (cumulable, rachetable).", 80),
        ShopItem(ADS_REDUCED, ShopItemCategory.FEATURE, "Pubs réduites", "Réduit la fréquence des publicités pendant les parties.", 3000),
        ShopItem(PREMIUM_ALL_ACCESS, ShopItemCategory.FEATURE, "Premium (accès total)", "Débloque tous les jeux et toutes les fonctionnalités d'un coup.", 15000)
    )

    /** Tous les items achetables avec des points (jeux d'arcade + boutique), pour une recherche par id. */
    val allItems: List<ShopItem> = lockedGames + shopItems

    fun find(itemId: String): ShopItem? = allItems.find { it.id == itemId }
}
