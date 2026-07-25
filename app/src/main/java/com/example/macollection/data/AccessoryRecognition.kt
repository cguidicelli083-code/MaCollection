package com.example.macollection.data

/**
 * Reconnaît un accessoire (manette, chargeur, mémoire, casque...) à partir du texte
 * lu sur une photo (OCR), en cherchant des mots-clés connus. Renvoie un nom canonique,
 * ou null si rien de fiable. Mêmes règles de spécificité que [ConsoleRecognition].
 */
object AccessoryRecognition {

    private val rules: List<Pair<List<String>, String>> = listOf(
        listOf("super scope") to "Super Scope",
        listOf("menacer") to "Menacer",
        listOf("light phaser") to "Light Phaser",
        listOf("control stick", "sega control stick") to "Control Stick (Master System)",
        listOf("control pad", "sega control pad", "master system control pad") to "Control Pad (Master System)",
        listOf("3-d glasses", "3 d glasses", "3dglasses", "3-dglasses", "lunettes 3d", "lunettes 3-d") to "3-D Glasses",
        listOf("zapper") to "Zapper (NES)",
        listOf("r.o.b.", " rob ", "robotic operating buddy") to "R.O.B.",
        listOf("dualsense") to "Manette DualSense (PS5)",
        listOf("dualshock 4", "dual shock 4") to "Manette DualShock 4 (PS4)",
        listOf("dualshock 3", "dual shock 3") to "Manette DualShock 3 (PS3)",
        listOf("dualshock 2", "dual shock 2") to "Manette DualShock 2 (PS2)",
        listOf("dualshock", "dual shock") to "Manette DualShock (PS1)",
        listOf("xbox series controller", "manette xbox series") to "Manette Xbox Series",
        listOf("xbox one controller", "manette xbox one") to "Manette Xbox One",
        listOf("xbox 360 controller", "manette xbox 360") to "Manette Xbox 360",
        listOf("joy-con", "joycon") to "Paire de Joy-Con (Switch)",
        listOf("pro controller", "manette switch pro") to "Manette Switch Pro",
        listOf("gamecube controller", "manette gamecube") to "Manette GameCube",
        listOf("nunchuk", "wiimote", "wii remote") to "Wiimote / Nunchuk (Wii)",
        listOf("n64 controller", "manette n64", "manette nintendo 64") to "Manette Nintendo 64",
        listOf("dreamcast controller", "manette dreamcast") to "Manette Dreamcast",
        listOf("saturn controller", "manette saturn") to "Manette Saturn",
        listOf("mega drive controller", "manette mega drive", "genesis controller", "manette genesis", "six button", "6 button", "メガコマンダー") to "Manette Mega Drive",
        listOf("cx40", "cx-40", "atari joystick") to "Joystick CX40",
        listOf("colecovision controller", "manette colecovision") to "Manette ColecoVision",
        listOf("intellivision controller", "manette intellivision") to "Manette Intellivision",
        listOf("snes controller", "manette snes", "super nintendo controller") to "Manette SNES",
        listOf("famicom controller", "manette famicom") to "Manette Famicom",
        listOf("nes controller", "manette nes") to "Manette NES",
        listOf("memory card", "carte mémoire", "carte memoire") to "Carte mémoire",
        listOf("multitap") to "Multitap",
        listOf("light gun", "pistolet optique") to "Pistolet optique",
        listOf("volant", "racing wheel", "steering wheel") to "Volant",
        listOf("casque", "headset") to "Casque",
        listOf("chargeur", "charging dock", "charging station") to "Station de charge",
        listOf("câble péritel", "cable peritel", "scart") to "Câble péritel",
        listOf("adaptateur secteur", "alimentation", "power supply", "ac adapter") to "Bloc d'alimentation",
        // Mots génériques japonais désignant une manette/un pad (souvent imprimés sur les boîtes
        // de pads compatibles tiers, sans le mot anglais « controller ») : reconnus en dernier
        // recours, mais priment quand même sur le simple nom de la console imprimé à côté.
        listOf("コントローラー", "ジョイパッド", "ゲームパッド", "パッド") to "Manette",
        listOf("manette") to "Manette"
    )

    /** Renvoie le nom canonique reconnu, ou null. */
    fun recognize(ocrText: String): String? {
        val t = ocrText.lowercase().replace(Regex("\\s+"), " ")
        for ((keys, name) in rules) {
            if (keys.any { t.contains(it) }) return name
        }
        return null
    }
}
