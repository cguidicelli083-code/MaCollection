package com.example.macollection.data

/**
 * Liens vers les pages du wiki Console5 (wiki.console5.com) : schémas, manuels de
 * service et pannes connues par console. On ne stocke QUE des liens (pas de copie
 * locale des fichiers, qui sont pour beaucoup des scans sous droits) — l'app ouvre
 * la page correspondante dans le navigateur.
 *
 * Clé = nom exact de la console dans [consolePresets].
 */
object ConsoleRepairLinks {

    private const val BASE = "https://wiki.console5.com/wiki/"

    private val byName: Map<String, String> = mapOf(
        "2600 (VCS)" to "Atari_2600",
        "5200" to "Atari_5200",
        "7800" to "Atari_7800",
        "Lynx" to "Atari_Lynx",
        "Jaguar" to "Atari_Jaguar",
        "Jaguar CD" to "Atari_Jaguar_CD",
        "XEGS" to "Atari_XEGS",
        "Channel F" to "Channel_F",
        "ColecoVision" to "Colecovision",
        "Coleco Gemini" to "Coleco_Gemini",
        "Vectrex" to "Vectrex",
        "Intellivision" to "Intellivision",
        "Intellivision II" to "Intellivision_II",
        "Famicom" to "Famicom",
        "Famicom Disk System" to "Famicom_Disk_System",
        "NES" to "NES",
        "Super Famicom" to "Super_Nintendo_Entertainment_System",
        "Super Nintendo (SNES)" to "Super_Nintendo_Entertainment_System",
        "Nintendo 64" to "Nintendo_64",
        "GameCube" to "GameCube",
        "Wii" to "Wii",
        "Game Boy" to "Game_Boy_DMG-01",
        "Game Boy Color" to "Game_Boy_Color",
        "Game Boy Advance" to "Game_Boy_Advance",
        "Master System" to "Sega_Master_System",
        "Mega Drive" to "Mega_Drive",
        "Mega Drive 2" to "Mega_Drive_2",
        "Game Gear" to "Game_Gear",
        "Mega CD" to "Sega_CD",
        "Mega CD 2" to "Sega_CD_v2",
        "32X" to "32X",
        "Saturn" to "Saturn",
        "PlayStation" to "PlayStation",
        "PlayStation 2" to "PlayStation_2",
        "Neo Geo AES" to "Neo_Geo_AES",
        "Neo Geo CD" to "Neo_Geo_CD",
        "Neo Geo Pocket" to "Neo_Geo_Pocket",
        "Neo Geo Pocket Color" to "Neo_Geo_Pocket_Color",
        "WonderSwan" to "WonderSwan",
        "PC Engine" to "NEC_PC_Engine",
        "PC Engine Duo" to "PC_Engine_Duo",
        "PC Engine LT" to "PC_Engine_LT",
        "SuperGrafx" to "SuperGrafx",
        "TurboGrafx-16" to "TurboGrafx_16",
        "TurboExpress" to "Turbo_Express",
        "Dreamcast" to "Dreamcast",
        "Xbox" to "Microsoft_Xbox",
        "Xbox 360" to "Microsoft_Xbox_360",
        "Xbox One" to "Xbox_One",
        "Amiga CD32" to "Amiga_CD32",
        "FM Towns Marty" to "FM_Towns_Marty",
        "RCA Studio II" to "RCA_Studio_II",
        "Astrocade" to "Astrocade",
        "PV-1000" to "Casio_PV-1000",
        "Arcadia 2001" to "Arcadia_2001",
        "Imagination Machine (MP1000)" to "APF_MP1000",
        "Videopac G7200" to "Videopac_G7200",
        "CDX" to "Sega_CDX",
        "Odyssey 2" to "Odyssey2"
    )

    /** Vrai s'il existe une page connue et vérifiée pour cette console. */
    fun hasDirectLink(name: String): Boolean = byName.containsKey(name)

    /**
     * URL à ouvrir pour l'aide au dépannage de cette console : la page exacte si on la
     * connaît, sinon une recherche sur le wiki (toujours utile, juste moins précise).
     */
    fun urlFor(name: String): String =
        byName[name]?.let { BASE + it } ?: "https://wiki.console5.com/index.php?search=" +
            java.net.URLEncoder.encode(name, "UTF-8")
}
