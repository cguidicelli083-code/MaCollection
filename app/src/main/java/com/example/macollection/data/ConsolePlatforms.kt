package com.example.macollection.data

/**
 * Correspondance console (nom de preset) -> identifiant de plateforme RAWG,
 * pour récupérer la liste des jeux de chaque console.
 * Les consoles absentes n'ont pas de liste de jeux en ligne.
 */
object ConsolePlatforms {

    val byName: Map<String, Int> = mapOf(
        "2600 (VCS)" to 23,
        "5200" to 31,
        "7800" to 28,
        "XEGS" to 50,
        "Lynx" to 46,
        "Jaguar" to 112,
        "NES" to 49,
        "Famicom" to 49,
        "Super Nintendo (SNES)" to 79,
        "Super Famicom" to 79,
        "Nintendo 64" to 83,
        "GameCube" to 105,
        "Wii" to 11,
        "Wii U" to 10,
        "Switch" to 7,
        "Switch OLED" to 7,
        "Game Boy" to 26,
        "Game Boy Pocket" to 26,
        "Game Boy Color" to 43,
        "Game Boy Advance" to 24,
        "Nintendo DS" to 9,
        "Nintendo DSi" to 13,
        "Nintendo 3DS" to 8,
        "Master System" to 74,
        "Mega Drive" to 167,
        "Game Gear" to 77,
        "Saturn" to 107,
        "32X" to 117,
        "Mega CD" to 119,
        "Dreamcast" to 106,
        "PlayStation" to 27,
        "PlayStation 2" to 15,
        "PlayStation 3" to 16,
        "PlayStation 4" to 18,
        "PlayStation 4 Pro" to 18,
        "PlayStation 5" to 187,
        "PSP" to 17,
        "PSP Go" to 17,
        "PlayStation Vita" to 19,
        "Xbox" to 80,
        "Xbox 360" to 14,
        "Xbox One" to 1,
        "Xbox One X" to 1,
        "Xbox Series S" to 186,
        "Xbox Series X" to 186,
        "Neo Geo AES" to 12,
        "Neo Geo CD" to 12,
        "3DO" to 111,
        "3DO (FZ-10)" to 111,
        "CDTV" to 166,
        "Amiga CD32" to 166,
        "Mega Drive 2" to 167,
        "Mega CD 2" to 119,
        "Jaguar CD" to 112,
        "Game Boy Advance SP" to 24,
        "Nintendo DSi XL" to 13,
        "Nintendo 3DS XL" to 8,
        "Nintendo 2DS" to 8,
        "New Nintendo 3DS XL" to 8,
        "New Nintendo 2DS XL" to 8,
        "GameCube (Panasonic Q)" to 105,
        "Twin Famicom" to 49,
        "PlayStation 4 Édition Uncharted 4" to 18,
        "PlayStation 4 Pro Édition Marvel's Spider-Man" to 18,
        "PlayStation 4 Pro Édition The Last of Us Part II" to 18,
        "Xbox 360 Édition Halo 3" to 14,
        "Xbox 360 Édition Gears of War" to 14,
        "Xbox 360 Édition Halo 4" to 14,
        "Xbox One Édition Halo 5 Guardians" to 1,
        "Xbox One X Édition Project Scorpio" to 1,
        "Xbox One S Édition Minecraft" to 1,
        "Xbox One X Édition Cyberpunk 2077" to 1,
        "Xbox Series X Édition Halo Infinite" to 186,
        "Nintendo 64 Édition Pikachu" to 83,
        "Game Gear Rouge" to 77,
        "Game Gear Jaune" to 77,
        "NeoGeo X Gold" to 12,
        "PSP Édition God of War: Chains of Olympus" to 17,
        "PSP Édition God of War: Ghost of Sparta" to 17,
        "PSP Édition Monster Hunter Portable 2nd G" to 17,
        "PlayStation Vita Édition Hatsune Miku" to 19,
        "Wii Édition 25e Anniversaire Mario" to 11,
        "Wii U Édition The Wind Waker HD" to 10,
        "Wii U Édition Super Smash Bros." to 10,
        "Atari 2600+ Édition Pac-Man" to 23,
        "1040 STF" to 34,
        "Amiga 500" to 166,
        "Nintendo 64 Jungle Green" to 83,
        "Nintendo 64 Fire Orange" to 83,
        "Nintendo 64 Ice Blue" to 83,
        "Nintendo 64 Watermelon Red" to 83,
        "Nintendo 64 Grape Purple" to 83,
        "Game Gear Magic Knight Rayearth" to 77
    )

    fun platformId(name: String): Int? = byName[name]

    /**
     * Correspondance identifiant plateforme RAWG -> identifiant plateforme IGDB. IGDB a ses
     * propres numéros de plateformes ; on les dérive de l'id RAWG déjà résolu par [byName]
     * (une seule table de noms à maintenir). Familles regroupées comme pour RAWG.
     */
    private val rawgToIgdb: Map<Int, Int> = mapOf(
        23 to 59, 31 to 66, 28 to 60, 50 to 65, 46 to 61, 112 to 62,   // Atari
        49 to 18, 79 to 19, 83 to 4, 105 to 21, 11 to 5, 10 to 41, 7 to 130, // Nintendo salon
        26 to 33, 43 to 22, 24 to 24, 9 to 20, 13 to 20, 8 to 37,      // Nintendo portable
        74 to 64, 167 to 29, 77 to 35, 107 to 32, 117 to 30, 119 to 78, 106 to 23, // Sega
        27 to 7, 15 to 8, 16 to 9, 18 to 48, 187 to 167, 17 to 38, 19 to 46, // PlayStation
        80 to 11, 14 to 12, 1 to 49, 186 to 169,                        // Xbox
        12 to 80, 111 to 50, 166 to 16, 34 to 63                        // Neo Geo / 3DO / Amiga / Atari ST
    )

    /** Identifiant plateforme IGDB pour un nom de console (ou null si non cartographié). */
    fun igdbPlatformId(name: String): Int? = byName[name]?.let { rawgToIgdb[it] }
}
