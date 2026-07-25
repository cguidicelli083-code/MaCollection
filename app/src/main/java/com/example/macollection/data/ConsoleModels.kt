package com.example.macollection.data

/**
 * Modèles 3D de consoles (intégrations Sketchfab, embed légal).
 * Clé = nom exact de la console dans [consolePresets].
 * Les consoles absentes affichent le modèle de démonstration.
 */
object ConsoleModels {

    private const val S = "https://sketchfab.com/models/"

    val byName: Map<String, String> = mapOf(
        "2600 (VCS)" to "${S}f9ec8aa8e4fa454cbbe821a23ca6e83d/embed",
        "NES" to "${S}ad1ba0a577c44f47be224084eea5965d/embed",
        "Famicom" to "${S}b33939368ad04640919570a9d3bdbeda/embed",
        "Super Nintendo (SNES)" to "${S}31f4914a42e14631aeba15a539161e62/embed",
        "Super Famicom" to "${S}610c0cd0c841486385495f53e3b68236/embed",
        "Nintendo 64" to "${S}0e6919aaa3c84a73830248ba4a8f709c/embed",
        "GameCube" to "${S}456f414a5f854d6cab5425ffb95d0a0d/embed",
        "Wii" to "${S}9552175840544217aa68a81574618425/embed",
        "Wii U" to "${S}a2e2e62fd88e43b1a5232daf34aed850/embed",
        "Switch" to "${S}b30e0a74899b4f9baf030d02f45ab599/embed",
        "Switch OLED" to "${S}36438fd911bb42138926cc82f7a2b522/embed",
        "Game Boy" to "${S}d041e45c70da438582e7e8bf4f65d175/embed",
        "Game Boy Color" to "${S}277998bca398436faaece92470d93372/embed",
        "Game Boy Advance" to "${S}faa74a9aad5f49bdbb8a5b90ddb0d58a/embed",
        "Nintendo DS" to "${S}ca529eb7208746e89b7a28fd2246659d/embed",
        "Nintendo 3DS" to "${S}fceffa4bd7064acd9e31cab9f7e27d89/embed",
        "Virtual Boy" to "${S}0086f3db0c21424ea910d9e5fa235170/embed",
        "Master System" to "${S}0ec998c9b9de4e7f956cab56bbca3c0f/embed",
        "Mega Drive" to "${S}bceba7d09cd147da8e25ff3f5c238b37/embed",
        "Game Gear" to "${S}f1f25f61df37448ebf11b1b36fed6a5d/embed",
        "Saturn" to "${S}a0eab4c5a35f4ff68e5b83690e4179cd/embed",
        "Dreamcast" to "${S}f82d99e4739e42f4abfc37cb97918cfc/embed",
        "PlayStation" to "${S}de8bb282a94b468c997e4b7d8c2aa2f5/embed",
        "PlayStation 2" to "${S}b53f858aa1584229a7f906239fc158a2/embed",
        "PlayStation 3" to "${S}d3eb52c6cf144601a52c99f9be78dfec/embed",
        "PlayStation 4" to "${S}b7LzIm8JrnPw4GBDOMBNGYc39qM/embed",
        "PlayStation 5" to "${S}54810f0b23a441ca93f44a8b06ec937b/embed",
        "PSP" to "${S}dca89d10ec304d0cab76837750df7761/embed",
        "PlayStation Vita" to "${S}b68ef4821c9c426da5d09decee4c259d/embed",
        "Xbox" to "${S}50a6590eff87403992facd761a9d8f24/embed",
        "Xbox 360" to "${S}f65ac4721fd34dd8a8b63adfa712f5b8/embed",
        "Xbox One" to "${S}8866e33768e84b27b69e07abcde8a593/embed",
        "Xbox Series X" to "${S}29460c063c7b4046aea8be633f90210e/embed",
        "Neo Geo AES" to "${S}e2b303532785471fbad04af56c205b5f/embed",
        "PC Engine" to "${S}694dd99905564d2b8f90d8b5f3ace002/embed",
        "Steam Deck" to "${S}3f777a4d9aee495c8fda9896df8a9c37/embed"
    )

    fun urlFor(name: String): String? = byName[name]
}
