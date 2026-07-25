package com.example.macollection.data

import java.net.URLEncoder

/**
 * Lien Wikipédia pour un accessoire (manette, pistolet optique, périphérique…) — même logique que
 * [ConsoleWikipediaLinks] : page exacte quand on la connaît, sinon repli sur une recherche
 * Wikipédia du nom. Beaucoup d'accessoires n'ont pas d'article FR dédié : on retombe alors sur
 * l'article EN (toujours pertinent pour un vrai nom de produit).
 *
 * Les clés reprennent les noms canoniques d'[accessoryPresets] / [AccessoryRecognition], avec en
 * plus quelques alias saisis à la main par l'utilisateur (ex. « Control Stick » pour la manette
 * de la Nintendo 64), pour que le bouton « Plus d'infos » pointe vers la bonne page.
 */
object AccessoryWikipediaLinks {

    /** Slug d'article français quand la fiche existe en FR. */
    private val byNameFr: Map<String, String> = mapOf(
        "Zapper (NES)" to "NES_Zapper",
        "Super Scope" to "Super_Scope",
        "R.O.B." to "R.O.B.",
        "Manette DualShock (PS1)" to "DualShock",
        "Manette DualShock 2 (PS2)" to "DualShock",
        "Manette DualShock 3 (PS3)" to "Sixaxis",
        "Manette DualShock 4 (PS4)" to "DualShock_4",
        "Manette DualSense (PS5)" to "DualSense",
        "Wiimote / Nunchuk (Wii)" to "Wiimote",
        "Paire de Joy-Con (Switch)" to "Joy-Con",
        "Manette Switch Pro" to "Manette_Switch_Pro"
    )

    /**
     * Slug d'article anglais (repli quand aucune fiche FR dédiée n'existe). Inclut les alias de
     * saisie utilisateur : « Control Stick » et « Manette Nintendo 64 » pointent vers l'article
     * dédié de la manette N64, dont le stick analogique (« Control Stick ») est la vedette.
     */
    private val byNameEn: Map<String, String> = mapOf(
        "Manette Nintendo 64" to "Nintendo_64_controller",
        "Control Stick" to "Nintendo_64_controller",
        "Manette GameCube" to "GameCube_controller",
        "Manette NES" to "NES_controller",
        "Manette SNES" to "Super_Nintendo_Entertainment_System#Controllers",
        "Manette Famicom" to "Nintendo_Entertainment_System#Controllers",
        "Manette Saturn" to "Saturn_controller",
        "Manette Dreamcast" to "Dreamcast_controller",
        "Manette Mega Drive" to "Sega_Genesis#Controllers",
        "Joystick CX40" to "Atari_CX40_joystick",
        "Light Phaser" to "Light_Phaser",
        "Menacer" to "Sega_Menacer",
        "3-D Glasses" to "SegaScope_3-D",
        "Manette Xbox 360" to "Xbox_360_controller",
        "Manette Xbox One" to "Xbox_One_controller",
        "Manette Xbox Series" to "Xbox_Wireless_Controller",
        "Manette ColecoVision" to "ColecoVision#Controllers",
        "Manette Intellivision" to "Intellivision#Hand_controllers",
        "Control Stick (Master System)" to "Master_System#Accessories",
        "Control Pad (Master System)" to "Master_System#Accessories"
    )

    fun urlFor(accessoryName: String): String {
        val lang = if (AppPrefs.language == "fr") "fr" else "en"
        // FR d'abord si l'appli est en français ET qu'une fiche FR existe ; sinon repli EN ; sinon recherche.
        val frSlug = if (lang == "fr") byNameFr[accessoryName] else null
        val enSlug = byNameEn[accessoryName]
        return when {
            frSlug != null -> "https://fr.wikipedia.org/wiki/$frSlug"
            enSlug != null -> "https://en.wikipedia.org/wiki/$enSlug"
            else -> "https://$lang.wikipedia.org/w/index.php?search=" + URLEncoder.encode(accessoryName, "UTF-8")
        }
    }
}
