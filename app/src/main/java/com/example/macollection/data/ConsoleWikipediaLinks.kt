package com.example.macollection.data

import java.net.URLEncoder

/**
 * Lien Wikipédia pour chaque console — page exacte quand on la connaît, sinon une recherche
 * Wikipédia sur le nom (toujours un résultat pertinent pour un vrai nom de produit). Langue
 * choisie selon la langue de l'appli (fiches FR disponibles, sinon EN par défaut).
 */
object ConsoleWikipediaLinks {

    private val byNameFr: Map<String, String> = mapOf(
        "2600 (VCS)" to "Atari_2600",
        "5200" to "Atari_5200",
        "7800" to "Atari_7800",
        "Famicom" to "Family_Computer",
        "NES" to "Nintendo_Entertainment_System",
        "Super Famicom" to "Super_Famicom",
        "Super Nintendo (SNES)" to "Super_Nintendo_Entertainment_System",
        "Nintendo 64" to "Nintendo_64",
        "GameCube" to "GameCube",
        "Wii" to "Wii",
        "Wii U" to "Wii_U",
        "Switch" to "Nintendo_Switch",
        "Master System" to "Master_System",
        "Mega Drive" to "Mega_Drive",
        "Saturn" to "Saturn_(console)",
        "Dreamcast" to "Dreamcast",
        "SG-1000" to "SG-1000",
        "SC-3000" to "SC-3000",
        "PlayStation" to "PlayStation_(console)",
        "PlayStation 2" to "PlayStation_2",
        "PlayStation 3" to "PlayStation_3",
        "PlayStation 4" to "PlayStation_4",
        "PlayStation 5" to "PlayStation_5",
        "Xbox" to "Xbox_(console)",
        "Xbox 360" to "Xbox_360",
        "Xbox One" to "Xbox_One",
        "Xbox Series X" to "Xbox_Series_X_et_Series_S",
        "Game Boy" to "Game_Boy",
        "Game Boy Pocket" to "Game_Boy_Pocket",
        "Game Boy Color" to "Game_Boy_Color",
        "Game Boy Advance" to "Game_Boy_Advance",
        "Nintendo DS" to "Nintendo_DS",
        "Nintendo 3DS" to "Nintendo_3DS",
        "Game Gear" to "Game_Gear",
        "Neo Geo AES" to "Neo_Geo",
        "Neo Geo Pocket" to "Neo_Geo_Pocket",
        "Neo Geo Pocket Color" to "Neo_Geo_Pocket_Color",
        "PC Engine" to "PC_Engine",
        "ColecoVision" to "ColecoVision",
        "Intellivision" to "Intellivision",
        "Vectrex" to "Vectrex",
        "3DO" to "3DO_Interactive_Multiplayer",
        "CD-i" to "CD-i",
        "Virtual Boy" to "Virtual_Boy",
        "Lynx" to "Atari_Lynx",
        "WonderSwan" to "WonderSwan"
    )

    /** Slug d'article anglais quand la fiche FR n'existe pas (clés absentes ci-dessus). */
    private val byNameEn: Map<String, String> = mapOf(
        "Odyssey" to "Magnavox_Odyssey",
        "Channel F" to "Fairchild_Channel_F",
        "Videopac G7200" to "Magnavox_Odyssey²"
    )

    // --- Modèles Game & Watch : une page/section dédiée par modèle plutôt que l'article général ---
    // Vérifié titre par titre (API MediaWiki) : aucun modèle n'a d'article FR dédié (l'article FR
    // "Liste de jeux Game & Watch" n'est qu'un index alphabétique A-Z, sans ancre par jeu), donc on
    // pointe systématiquement vers l'anglais — seule langue où existent (a) des articles dédiés à
    // certains titres (Ball, Manhole, Popeye, Mario Bros., Mario's Cement Factory, Balloon Fight,
    // Game & Watch: Super Mario Bros.), (b) une ancre par jeu dans « List of LCD games featuring
    // Mario » (titres Mario/Donkey Kong) ou « The Legend of Zelda LCD games » (les deux Zelda), et
    // (c) une ancre par jeu dans « List of Game & Watch games » pour tout le reste.
    private const val LO_GW = "https://en.wikipedia.org/wiki/List_of_Game_%26_Watch_games"
    private const val LO_MARIO = "https://en.wikipedia.org/wiki/List_of_LCD_games_featuring_Mario"
    private const val LO_ZELDA = "https://en.wikipedia.org/wiki/The_Legend_of_Zelda_LCD_games"

    private val gameAndWatchModelLinks: Map<String, String> = mapOf(
        "Game & Watch Ball (Silver)" to "https://en.wikipedia.org/wiki/Ball_(video_game)",
        "Game & Watch Flagman (Silver)" to "$LO_GW#Flagman",
        "Game & Watch Vermin (Silver)" to "$LO_GW#Vermin",
        "Game & Watch Fire (Silver)" to "$LO_GW#Fire",
        "Game & Watch Judge (Silver)" to "$LO_GW#Judge",
        "Game & Watch Manhole (Gold)" to "https://en.wikipedia.org/wiki/Manhole_(video_game)",
        "Game & Watch Helmet (Gold)" to "$LO_GW#Helmet",
        "Game & Watch Lion (Gold)" to "$LO_GW#Lion",
        "Game & Watch Parachute (Wide Screen)" to "$LO_GW#Parachute",
        "Game & Watch Octopus (Wide Screen)" to "$LO_GW#Octopus",
        "Game & Watch Popeye (Wide Screen)" to "https://en.wikipedia.org/wiki/Popeye_(video_game)",
        "Game & Watch Chef (Wide Screen)" to "$LO_GW#Chef",
        "Game & Watch Mickey Mouse (Wide Screen)" to "$LO_GW#Mickey_Mouse",
        "Game & Watch Egg (Wide Screen)" to "$LO_GW#Egg",
        "Game & Watch Fire (Wide Screen)" to "$LO_GW#Fire",
        "Game & Watch Turtle Bridge (Wide Screen)" to "$LO_GW#Turtle_Bridge",
        "Game & Watch Fire Attack (Wide Screen)" to "$LO_GW#Fire_Attack",
        "Game & Watch Snoopy Tennis (Wide Screen)" to "$LO_GW#Snoopy_Tennis",
        "Game & Watch Oil Panic (Multi Screen)" to "$LO_GW#Oil_Panic",
        "Game & Watch Donkey Kong (Multi Screen)" to "$LO_MARIO#Donkey_Kong",
        "Game & Watch Mickey and Donald (Multi Screen)" to "$LO_GW#Mickey_&_Donald",
        "Game & Watch Green House (Multi Screen)" to "$LO_GW#Green_House",
        "Game & Watch Donkey Kong II (Multi Screen)" to "$LO_MARIO#Donkey_Kong_II",
        "Game & Watch Mario Bros. (Multi Screen)" to "https://en.wikipedia.org/wiki/Mario_Bros.",
        "Game & Watch Rain Shower (Multi Screen)" to "$LO_GW#Rain_Shower",
        "Game & Watch Lifeboat (Multi Screen)" to "$LO_GW#Lifeboat",
        "Game & Watch Pinball (Multi Screen)" to "$LO_GW#Pinball",
        "Game & Watch Black Jack (Multi Screen)" to "$LO_GW#Black_Jack",
        "Game & Watch Squish (Multi Screen)" to "$LO_GW#Squish",
        "Game & Watch Bomb Sweeper (Multi Screen)" to "$LO_GW#Bomb_Sweeper",
        "Game & Watch Safebuster (Multi Screen)" to "$LO_GW#Safebuster",
        "Game & Watch Gold Cliff (Multi Screen)" to "$LO_GW#Gold_Cliff",
        "Game & Watch Zelda (Multi Screen)" to "$LO_ZELDA#Game_&_Watch:_Zelda",
        "Game & Watch Donkey Kong Jr. (Table Top)" to "$LO_MARIO#Donkey_Kong_Jr.",
        "Game & Watch Mario's Cement Factory (Table Top)" to "https://en.wikipedia.org/wiki/Mario%27s_Cement_Factory",
        "Game & Watch Snoopy (Table Top)" to "$LO_GW#Snoopy",
        "Game & Watch Popeye (Table Top)" to "https://en.wikipedia.org/wiki/Popeye_(video_game)",
        "Game & Watch Snoopy (Panorama)" to "$LO_GW#Snoopy",
        "Game & Watch Popeye (Panorama)" to "https://en.wikipedia.org/wiki/Popeye_(video_game)",
        "Game & Watch Donkey Kong Jr. (Panorama)" to "$LO_MARIO#Donkey_Kong_Jr.",
        "Game & Watch Mario's Bombs Away (Panorama)" to "$LO_MARIO#Mario's_Bombs_Away",
        "Game & Watch Mickey Mouse (Panorama)" to "$LO_GW#Mickey_Mouse",
        "Game & Watch Donkey Kong Circus (Panorama)" to "$LO_MARIO#Donkey_Kong_Circus",
        "Game & Watch Donkey Kong Jr. (New Wide Screen)" to "$LO_MARIO#Donkey_Kong_Jr.",
        "Game & Watch Mario's Cement Factory (New Wide Screen)" to "https://en.wikipedia.org/wiki/Mario%27s_Cement_Factory",
        "Game & Watch Manhole (New Wide Screen)" to "https://en.wikipedia.org/wiki/Manhole_(video_game)",
        "Game & Watch Tropical Fish (New Wide Screen)" to "$LO_GW#Tropical_Fish",
        "Game & Watch Super Mario Bros. (New Wide Screen)" to "$LO_MARIO#Super_Mario_Bros.",
        "Game & Watch Climber (New Wide Screen)" to "$LO_GW#Climber",
        "Game & Watch Balloon Fight (New Wide Screen)" to "https://en.wikipedia.org/wiki/Balloon_Fight",
        "Game & Watch Mario the Juggler (New Wide Screen)" to "$LO_MARIO#Mario_the_Juggler",
        "Game & Watch Spitball Sparky (Super Color)" to "$LO_GW#Spitball_Sparky",
        "Game & Watch Crab Grab (Super Color)" to "$LO_GW#Crab_Grab",
        "Game & Watch Boxing / Punch-Out!! (Micro Vs. System)" to "$LO_GW#Boxing",
        "Game & Watch Donkey Kong 3 (Micro Vs. System)" to "$LO_GW#Donkey_Kong_3",
        "Game & Watch Donkey Kong Hockey (Micro Vs. System)" to "$LO_MARIO#Donkey_Kong_Hockey",
        "Game & Watch Super Mario Bros. (Crystal Screen)" to "$LO_MARIO#Super_Mario_Bros.",
        "Game & Watch Climber (Crystal Screen)" to "$LO_GW#Climber",
        "Game & Watch Balloon Fight (Crystal Screen)" to "https://en.wikipedia.org/wiki/Balloon_Fight",
        "Game & Watch Super Mario Bros. (non commercialisé)" to "$LO_MARIO#Super_Mario_Bros.",
        "Game & Watch Ball (réédition 2010)" to "$LO_GW#Remasters_(2010–)",
        "Game & Watch: Super Mario Bros. (2020)" to "https://en.wikipedia.org/wiki/Game_%26_Watch:_Super_Mario_Bros.",
        "Game & Watch: The Legend of Zelda (2021)" to "$LO_ZELDA#Game_&_Watch:_The_Legend_of_Zelda"
    )

    fun urlFor(consoleName: String): String {
        val lang = if (AppPrefs.language == "fr") "fr" else "en"
        gameAndWatchModelLinks[consoleName]?.let { return it }
        // Fiche générique « Game & Watch » (sans modèle précis) ou modèle futur pas encore
        // ajouté à la map ci-dessus : repli sur l'article général de la gamme plutôt qu'un lien
        // mort ou une recherche qui tomberait à côté (personnage éponyme…).
        if (consoleName.startsWith("Game & Watch", ignoreCase = true)) {
            return if (lang == "fr") "https://fr.wikipedia.org/wiki/Game_and_Watch"
            else "https://en.wikipedia.org/wiki/Game_%26_Watch"
        }
        val slug = (if (lang == "fr") byNameFr[consoleName] else null) ?: byNameEn[consoleName]
        return if (slug != null) {
            "https://$lang.wikipedia.org/wiki/$slug"
        } else {
            "https://$lang.wikipedia.org/w/index.php?search=" + URLEncoder.encode(consoleName, "UTF-8")
        }
    }
}
