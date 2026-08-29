package com.example.macollection.data

/**
 * Photos (1 par accessoire) issues de Wikimedia Commons.
 * Clé = nom exact de l'accessoire dans [accessoryPresets].
 */
object AccessoryImages {

    val byName: Map<String, String> = mapOf(
        // Anciennement en liens directs Wikimedia Commons (dépendance réseau à chaque affichage,
        // sujette aux mêmes limitations de débit CDN que la note ci-dessous) — retéléchargées et
        // rebasculées en local le 2026-08-23, même traitement que le lot "recherche approfondie".
        "Zapper (NES)" to "file:///android_asset/accessory_photos/zapper_nes.webp",
        "R.O.B." to "file:///android_asset/accessory_photos/rob.webp",
        "Light Phaser" to "file:///android_asset/accessory_photos/light_phaser.webp",
        "3-D Glasses" to "file:///android_asset/accessory_photos/sega_3d_glasses.webp",
        "Super Scope" to "file:///android_asset/accessory_photos/super_scope.webp",
        "Super Scope (Super Famicom)" to "file:///android_asset/accessory_photos/superscope_super_famicom.webp",
        "Menacer" to "file:///android_asset/accessory_photos/menacer.webp",
        "Manette NES" to "file:///android_asset/accessory_photos/manette_nes.webp",
        "Manette Famicom" to "file:///android_asset/accessory_photos/manette_famicom.webp",
        "Manette SNES" to "file:///android_asset/accessory_photos/manette_snes.webp",
        "Joystick CX40" to "file:///android_asset/accessory_photos/joystick_cx40.webp",
        "Manette Mega Drive" to "file:///android_asset/accessory_photos/megadrive_6button_pad.webp",
        "Manette Saturn" to "file:///android_asset/accessory_photos/manette_saturn.webp",
        "Manette Dreamcast" to "file:///android_asset/accessory_photos/manette_dreamcast.webp",
        "Manette Nintendo 64" to "file:///android_asset/accessory_photos/manette_n64.webp",
        "Manette ColecoVision" to "file:///android_asset/accessory_photos/manette_colecovision.webp",
        "Manette Intellivision" to "file:///android_asset/accessory_photos/manette_intellivision.webp",
        "Manette GameCube" to "file:///android_asset/accessory_photos/manette_gamecube.webp",
        "Manette DualShock (PS1)" to "file:///android_asset/accessory_photos/manette_dualshock.webp",
        "Manette DualShock 2 (PS2)" to "file:///android_asset/accessory_photos/manette_dualshock2.webp",
        "Manette DualShock 3 (PS3)" to "file:///android_asset/accessory_photos/manette_dualshock3.webp",
        "Manette DualShock 4 (PS4)" to "file:///android_asset/accessory_photos/manette_dualshock4.webp",
        "Manette DualSense (PS5)" to "file:///android_asset/accessory_photos/manette_dualsense.webp",
        "Manette Xbox 360" to "file:///android_asset/accessory_photos/manette_xbox360.webp",
        "Manette Xbox One" to "file:///android_asset/accessory_photos/manette_xboxone.webp",
        "Manette Xbox Series" to "file:///android_asset/accessory_photos/manette_xbox_series.webp",
        "Wiimote / Nunchuk (Wii)" to "file:///android_asset/accessory_photos/wiimote_nunchuk.webp",
        "Manette Switch Pro" to "file:///android_asset/accessory_photos/manette_switch_pro.webp",
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
        "Magnum Light Phaser" to "file:///android_asset/accessory_photos/magnum_light_phaser.webp",

        // --- Accessoires complémentaires (recherche approfondie 2026-07-26), photos Wikimedia
        // Commons (licence libre) téléchargées en local pour éviter les limitations de débit du CDN. ---
        "Carte Mémoire (PlayStation)" to "file:///android_asset/accessory_photos/ps_memcard.webp",
        "Carte Mémoire 8 Mo (PS2)" to "file:///android_asset/accessory_photos/ps2_memcard.webp",
        "VMU (Dreamcast)" to "file:///android_asset/accessory_photos/dc_vmu.webp",
        "Carte Mémoire 251 (GameCube)" to "file:///android_asset/accessory_photos/gc_memcard.webp",
        "Unité de Mémoire (Xbox 360)" to "file:///android_asset/accessory_photos/xbox360_memunit.webp",
        "Multitap (PlayStation)" to "file:///android_asset/accessory_photos/ps_multitap.webp",
        "Volant Wii (Wii Wheel)" to "file:///android_asset/accessory_photos/wii_wheel.webp",
        "Volant sans fil Xbox 360 (Wireless Racing Wheel)" to "file:///android_asset/accessory_photos/xbox360_wheel.webp",
        "Canne à pêche Dreamcast (Sega Bass Fishing)" to "file:///android_asset/accessory_photos/dc_fishing.webp",
        "Wii Balance Board" to "file:///android_asset/accessory_photos/wii_balance.webp",
        "Wii MotionPlus" to "file:///android_asset/accessory_photos/wii_motionplus.webp",
        "Kinect (Xbox 360)" to "file:///android_asset/accessory_photos/kinect360.webp",
        "Kinect (Xbox One)" to "file:///android_asset/accessory_photos/kinectone.webp",
        "Manette PlayStation Move" to "file:///android_asset/accessory_photos/ps_move.webp",
        "PlayStation Move Sharp Shooter" to "file:///android_asset/accessory_photos/ps_move_sharpshooter.webp",
        "PS VR Aim Controller" to "file:///android_asset/accessory_photos/psvr_aim.webp",
        "Power Pad (NES)" to "file:///android_asset/accessory_photos/nes_powerpad.webp",
        "NES Advantage" to "file:///android_asset/accessory_photos/nes_advantage.webp",
        "NES Max" to "file:///android_asset/accessory_photos/nes_max.webp",
        "Imprimante Game Boy" to "file:///android_asset/accessory_photos/gb_printer.webp",
        "Game Boy Camera" to "file:///android_asset/accessory_photos/gb_camera.webp",
        "Rumble Pak (N64)" to "file:///android_asset/accessory_photos/n64_rumblepak.webp",
        "e-Reader (GBA)" to "file:///android_asset/accessory_photos/gba_ereader.webp",
        "Voice Recognition Unit (N64)" to "file:///android_asset/accessory_photos/n64_vru.webp",
        "Wii Zapper" to "file:///android_asset/accessory_photos/wii_zapper.webp",
        "Classic Controller Pro (Wii)" to "file:///android_asset/accessory_photos/wii_classicpro.webp",
        "Manette WaveBird (GameCube)" to "file:///android_asset/accessory_photos/gc_wavebird.webp",
        "DK Bongos (GameCube)" to "file:///android_asset/accessory_photos/gc_bongos.webp",
        "Manette Steel Battalion (Xbox)" to "file:///android_asset/accessory_photos/steel_battalion.webp",
        "3D Control Pad (Saturn)" to "file:///android_asset/accessory_photos/saturn_3dpad.webp",
        "EyeToy (PS2)" to "file:///android_asset/accessory_photos/ps2_eyetoy.webp",
        "Controller Pak (N64)" to "file:///android_asset/accessory_photos/n64_controllerpak.webp",
        "Mission Stick (Saturn)" to "file:///android_asset/accessory_photos/saturn_missionstick.webp",
        "Sega Activator" to "file:///android_asset/accessory_photos/sega_activator.webp"
    )

    fun urlFor(name: String): String? = byName[name]
}
