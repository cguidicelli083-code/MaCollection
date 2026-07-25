package com.example.macollection.data

/**
 * Photos (1 par accessoire) issues de Wikimedia Commons.
 * Clé = nom exact de l'accessoire dans [accessoryPresets].
 */
object AccessoryImages {

    val byName: Map<String, String> = mapOf(
        "Zapper (NES)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Nintendo-Entertainment-System-NES-Zapper-Gray-R.jpg/500px-Nintendo-Entertainment-System-NES-Zapper-Gray-R.jpg",
        "R.O.B." to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6c/NES-ROB.jpg/500px-NES-ROB.jpg",
        "Light Phaser" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2b/SMS-Light-Phaser.jpg/500px-SMS-Light-Phaser.jpg",
        "3-D Glasses" to "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9f/Sega-Masters-Sys-3D-Glasses.jpg/500px-Sega-Masters-Sys-3D-Glasses.jpg",
        "Super Scope" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ad/Nintendo-SNES-Super-Scope-L.jpg/500px-Nintendo-SNES-Super-Scope-L.jpg",
        "Super Scope (Super Famicom)" to "file:///android_asset/accessory_photos/superscope_super_famicom.webp",
        "Menacer" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/63/Sega_Menacer_cropped.jpg/500px-Sega_Menacer_cropped.jpg",
        "Manette NES" to "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/Nintendo-Entertainment-System-NES-Controller-FL.jpg/500px-Nintendo-Entertainment-System-NES-Controller-FL.jpg",
        "Manette Famicom" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a4/Nintendo-Famicom-NES-Dogbone-Controller-FL.jpg/500px-Nintendo-Famicom-NES-Dogbone-Controller-FL.jpg",
        "Manette SNES" to "https://upload.wikimedia.org/wikipedia/commons/thumb/1/12/Nintendo-Super-NES-Controller.jpg/500px-Nintendo-Super-NES-Controller.jpg",
        "Joystick CX40" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/33/Atari-2600-Joystick.jpg/500px-Atari-2600-Joystick.jpg",
        "Manette Mega Drive" to "file:///android_asset/accessory_photos/megadrive_6button_pad.webp",
        "Manette Saturn" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7c/Sega-Saturn-Controller-Mk-I-NA-FL.jpg/500px-Sega-Saturn-Controller-Mk-I-NA-FL.jpg",
        "Manette Dreamcast" to "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Sega-Dreamcast-Controller-FL.jpg/500px-Sega-Dreamcast-Controller-FL.jpg",
        "Manette Nintendo 64" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/N64-Controller-Gray.jpg/500px-N64-Controller-Gray.jpg",
        "Manette ColecoVision" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f0/ColecoVision-Controller-FL.jpg/500px-ColecoVision-Controller-FL.jpg",
        "Manette Intellivision" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/21/Intellivision-Controller.jpg/500px-Intellivision-Controller.jpg",
        "Manette GameCube" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/GameCube_controller.png/500px-GameCube_controller.png",
        "Manette DualShock (PS1)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c7/PSX-DualShock-Controller.jpg/500px-PSX-DualShock-Controller.jpg",
        "Manette DualShock 2 (PS2)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7f/DualShock_2.jpg/500px-DualShock_2.jpg",
        "Manette DualShock 3 (PS3)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/49/DualShock_3.jpg/500px-DualShock_3.jpg",
        "Manette DualShock 4 (PS4)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/59/DualShock_4.jpg/500px-DualShock_4.jpg",
        "Manette DualSense (PS5)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3c/Playstation_DualSense_Controller.png/500px-Playstation_DualSense_Controller.png",
        "Manette Xbox 360" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f4/Xbox_360_wired_controller_1.jpg/500px-Xbox_360_wired_controller_1.jpg",
        "Manette Xbox One" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/67/Microsoft-Xbox-One-controller.jpg/500px-Microsoft-Xbox-One-controller.jpg",
        "Manette Xbox Series" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cb/Xbox_Series_Controller_Carbon_Black.jpg/500px-Xbox_Series_Controller_Carbon_Black.jpg",
        "Wiimote / Nunchuk (Wii)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Wii_Remote_%26_Nunchuk.jpg/500px-Wii_Remote_%26_Nunchuk.jpg",
        "Manette Switch Pro" to "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d5/Nintendo-Switch-Pro-Controller-FL.jpg/500px-Nintendo-Switch-Pro-Controller-FL.jpg",
        "Paire de Joy-Con (Switch)" to "file:///android_asset/accessory_photos/switch_joycon.webp",
        // Photos officielles (PlayStation Direct) pour les éditions limitées sans image libre
        // sur Wikimedia Commons — usage personnel uniquement, pas de redistribution publique.
        "Manette DualSense Édition The Last of Us Part II" to "https://media.direct.playstation.com/is/image/sierialto/TLOU-LE-DS-Hero-1-Front",
        "Manette DualSense Édition God of War Ragnarök" to "https://media.direct.playstation.com/is/image/sierialto/GOWR-DualSense-Controller-Main",
        "Manette DualSense Édition Marvel's Spider-Man 2" to "https://blog.playstation.com/tachyon/2023/07/70f8135da2d0ee510d8179450f03805505d0951c.jpg?fit=1024%2C1024",
        "Manette DualSense Édition 30e Anniversaire" to "file:///android_asset/accessory_photos/ps5_30th.webp",
        "Manette Xbox Édition Halo Infinite" to "file:///android_asset/accessory_photos/xbox_halo_infinite.webp",
        "Manette Switch Pro Édition The Legend of Zelda: Tears of the Kingdom" to "https://assets.nintendo.com/image/upload/ar_16:9,c_lpad,w_1240/b_white/f_auto/q_auto/ncom/My%20Nintendo%20Store/EN-US/Nintendo%20Switch%20Accessories/Controllers/pro-controller-legend-of-zelda-tears-of-the-kingdom-special-edition-117075/117075-legend-of-zelda-tears-of-the-kingdom-pro-controller-package-1200x675",
        // Base communautaire de collectionneurs (consolevariations.com), faute de mieux —
        // licence non clarifiée, usage personnel uniquement, pas de redistribution publique.
        "Manette Switch Pro Édition Splatoon 3" to "https://cdn.consolevariations.com/22294/nintendo-switch-splatoon-3-pro-controller-eu-front-1657127599-64.webp",

        // --- Photos fournies par l'utilisateur (2026-07-13) ---
        "Control Pad (Master System)" to "file:///android_asset/accessory_photos/ms_control_pad.webp",
        "Control Stick (Master System)" to "file:///android_asset/accessory_photos/ms_control_stick.webp",
        "Resident Evil Pad" to "file:///android_asset/accessory_photos/re_pad_ps1.webp",
        "Manette Neo Geo AES" to "file:///android_asset/accessory_photos/neogeo_aes_stick.webp",
        "Manette Pikachu (N64)" to "file:///android_asset/accessory_photos/n64_pikachu.webp",
        "Arc Stick Pro (NeoGeo X)" to "file:///android_asset/accessory_photos/neogeox_arcstick.webp",
        "Arcade Stick Noir (NeoGeo AES+)" to "file:///android_asset/accessory_photos/neogeo_aesplus_stick_black.webp",
        "Arcade Stick Blanc 35e Anniversaire (NeoGeo AES+)" to "file:///android_asset/accessory_photos/neogeo_aesplus_stick_white.webp",
        "Mallette de transport (N64)" to "file:///android_asset/accessory_photos/n64_carrying_case.webp",
        "Stunner / Virtua Gun (Saturn)" to "file:///android_asset/accessory_photos/saturn_virtua_gun.webp",
        "SF-7000" to "file:///android_asset/console_photos/sega_sf7000.webp",
        "Écran LCD PSone (Combo)" to "file:///android_asset/accessory_photos/psone_screen.webp",
        "Magnum Light Phaser" to "file:///android_asset/accessory_photos/magnum_light_phaser.webp"
    )

    fun urlFor(name: String): String? = byName[name]
}
