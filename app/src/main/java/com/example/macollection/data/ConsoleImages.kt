package com.example.macollection.data

/**
 * Image (1 par console) issue des vignettes de modèles 3D Sketchfab.
 * Clé = nom exact de la console dans [consolePresets].
 */
object ConsoleImages {

    val byName: Map<String, String> = mapOf(
        "Odyssey" to "file:///android_asset/console_photos/odyssey.webp",
        "Pong (Home)" to "file:///android_asset/console_photos/pong_home.webp",
        "Channel F" to "file:///android_asset/console_photos/channel_f.webp",
        // Photo fournie par l'utilisateur (2026-07-13) : remplace le rendu 3D Sketchfab par une
        // vraie photo du modèle « wood-grain » emblématique.
        "2600 (VCS)" to "file:///android_asset/console_photos/atari_2600_vcs.webp",
        "2600 (bois)" to "file:///android_asset/console_photos/atari_2600_vcs.webp",
        "2600 (noire, sans bois)" to "file:///android_asset/console_photos/2600_noire_sans_bois.webp",
        "TheC64 Mini" to "file:///android_asset/console_photos/thec64_mini.webp",
        "THE400 Mini" to "file:///android_asset/console_photos/the400_mini.webp",
        // Pas de photo libre de droits (Wikimedia Commons) trouvée pour ces 4 modèles : photo
        // produit officielle du revendeur/éditeur reprise à la place (accord explicite de
        // l'utilisateur, 2026-08-01, malgré le risque de droit d'auteur signalé au préalable).
        "TheA500 Mini" to "file:///android_asset/console_photos/thea500_mini.webp",
        "Astro City Mini" to "file:///android_asset/console_photos/astro_city_mini.webp",
        "Astro City Mini V" to "file:///android_asset/console_photos/astro_city_mini_v.webp",
        "Capcom Home Arcade" to "file:///android_asset/console_photos/capcom_home_arcade.webp",
        "Famicom" to "file:///android_asset/console_photos/famicom.webp",
        "NES" to "file:///android_asset/console_photos/nes.webp",
        "Master System" to "file:///android_asset/console_photos/master_system.webp",
        "PC Engine" to "file:///android_asset/console_photos/pc_engine_mini.webp",
        "PC Engine CoreGrafx" to "file:///android_asset/console_photos/pce_coregrafx.webp",
        "PC Engine CoreGrafx II" to "file:///android_asset/console_photos/pce_coregrafx2.webp",
        "Mega Drive" to "file:///android_asset/console_photos/mega_drive.webp",
        "SuperGrafx" to "file:///android_asset/console_photos/supergrafx.webp",
        "Game Boy" to "file:///android_asset/console_photos/game_boy.webp",
        "Game Boy Pocket" to "file:///android_asset/console_photos/game_boy_pocket.webp",
        "Lynx" to "file:///android_asset/console_photos/lynx.webp",
        "Game Gear" to "file:///android_asset/console_photos/game_gear_micro.webp",
        "Neo Geo AES" to "file:///android_asset/console_photos/neo_geo_aes.webp",
        "Super Famicom" to "file:///android_asset/console_photos/super_famicom.webp",
        "Super Nintendo (SNES)" to "file:///android_asset/console_photos/super_nintendo_snes.webp",
        "CD-i" to "file:///android_asset/console_photos/cd_i.webp",
        "3DO" to "file:///android_asset/console_photos/3do.webp",
        "3DO (FZ-10)" to "file:///android_asset/console_photos/3do_fz_10.webp",
        "Saturn" to "file:///android_asset/console_photos/saturn.webp",
        "PlayStation" to "file:///android_asset/console_photos/playstation.webp",
        "Loopy" to "file:///android_asset/console_photos/loopy.webp",
        "Virtual Boy" to "file:///android_asset/console_photos/virtual_boy.webp",
        "Nintendo 64" to "file:///android_asset/console_photos/n64.webp",
        "Neo Geo Pocket" to "file:///android_asset/console_photos/neogeo_pocket.webp",
        "Game Boy Color" to "file:///android_asset/console_photos/game_boy_color.webp",
        "Dreamcast" to "file:///android_asset/console_photos/dreamcast.webp",
        "Neo Geo Pocket Color" to "file:///android_asset/console_photos/neogeo_pocket_color.webp",
        "PlayStation 2" to "file:///android_asset/console_photos/playstation_2.webp",
        "Game Boy Advance" to "file:///android_asset/console_photos/game_boy_advance.webp",
        "Xbox" to "file:///android_asset/console_photos/xbox.webp",
        "GameCube" to "file:///android_asset/console_photos/gamecube.webp",
        "Nintendo DS" to "file:///android_asset/console_photos/nintendo_dsi.webp",
        "Nintendo DS Lite" to "file:///android_asset/console_photos/nintendo_ds_lite.webp",
        "PSP" to "file:///android_asset/console_photos/psp.webp",
        "Xbox 360" to "file:///android_asset/console_photos/xbox_360.webp",
        "PlayStation 3" to "file:///android_asset/console_photos/playstation_3.webp",
        "Wii" to "file:///android_asset/console_photos/wii.webp",
        "Nintendo DSi" to "file:///android_asset/console_photos/nintendo_dsi.webp",
        "Nintendo 3DS" to "file:///android_asset/console_photos/nintendo_3ds.webp",
        "New 3DS XL Édition Super Nintendo" to "file:///android_asset/console_photos/new_3ds_xl_edition_super_nintendo.webp",
        "PlayStation Vita" to "file:///android_asset/console_photos/playstation_vita.webp",
        "Wii U" to "file:///android_asset/console_photos/wii_u.webp",
        "Xbox One" to "file:///android_asset/console_photos/xbox_one.webp",
        "PlayStation 4" to "file:///android_asset/console_photos/playstation_4.webp",
        "PlayStation 4 Pro" to "file:///android_asset/console_photos/playstation_4_pro.webp",
        // Photos fournies par l'utilisateur (2026-07-14) : vraies photos de boîte pour chaque
        // édition Switch, remplacent les rendus 3D Sketchfab génériques.
        "Switch" to "file:///android_asset/console_photos/switch.webp",
        "Switch Édition Splatoon 2" to "file:///android_asset/console_photos/switch_splatoon2.webp",
        "Switch Édition Super Mario Odyssey" to "file:///android_asset/console_photos/switch_mario_odyssey.webp",
        "Switch Monster Hunter XX Special Pack" to "file:///android_asset/console_photos/switch_monster_hunter_xx.webp",
        "Switch Édition Pokémon: Let's Go, Pikachu! / Évoli!" to "file:///android_asset/console_photos/switch_pikachu_evoli.webp",
        "Switch Édition Diablo III: Eternal Collection" to "file:///android_asset/console_photos/switch_diablo3.webp",
        "Switch Édition Super Smash Bros. Ultimate" to "file:///android_asset/console_photos/switch_smash_bros_ultimate.webp",
        "Switch Dragon Quest XI S « Roto Edition »" to "file:///android_asset/console_photos/switch_dragon_quest_roto.webp",
        "Switch Édition Disney Tsum Tsum Festival" to "file:///android_asset/console_photos/switch_tsum_tsum.webp",
        "Switch Édition Animal Crossing: New Horizons" to "file:///android_asset/console_photos/switch_animal_crossing.webp",
        "Switch Édition Fortnite" to "file:///android_asset/console_photos/switch_fortnite.webp",
        "Switch Édition Mario Rouge & Bleu" to "file:///android_asset/console_photos/switch_mario_rouge_bleu.webp",
        "Switch Édition Monster Hunter Rise" to "file:///android_asset/console_photos/switch_monster_hunter_rise.webp",
        "Switch Édition Nintendo Switch Sports" to "file:///android_asset/console_photos/switch_nintendo_sports.webp",
        "Switch Lite Gris" to "file:///android_asset/console_photos/switch_lite_gris.webp",
        "Switch Lite Jaune" to "file:///android_asset/console_photos/switch_lite_jaune.webp",
        "Switch Lite Turquoise" to "file:///android_asset/console_photos/switch_lite_turquoise.webp",
        "Switch Lite Corail" to "file:///android_asset/console_photos/switch_lite_corail.webp",
        "Switch Lite Bleu" to "file:///android_asset/console_photos/switch_lite_bleu.webp",
        "Switch Lite Édition Zacian et Zamazenta" to "file:///android_asset/console_photos/switch_lite_zacian_zamazenta.webp",
        "Switch Lite Édition Dialga et Palkia" to "file:///android_asset/console_photos/switch_lite_dialga_palkia.webp",
        "Switch Lite Édition Hyrule" to "file:///android_asset/console_photos/switch_lite_hyrule.webp",
        "Switch Lite Édition Animal Crossing « Isabelle's Aloha »" to "file:///android_asset/console_photos/switch_lite_ac_isabelle.webp",
        "Switch Lite Édition Animal Crossing « Timmy & Tommy's Aloha »" to "file:///android_asset/console_photos/switch_lite_ac_timmy_tommy.webp",
        "Switch OLED Blanche" to "file:///android_asset/console_photos/switch_oled_blanche.webp",
        "Switch OLED Édition Splatoon 3" to "file:///android_asset/console_photos/switch_oled_splatoon3.webp",
        "Switch OLED Édition Pokémon Écarlate et Violet" to "file:///android_asset/console_photos/switch_oled_pokemon_ev.webp",
        "Switch OLED Édition The Legend of Zelda: Tears of the Kingdom" to "file:///android_asset/console_photos/switch_oled_zelda_totk.webp",
        "Switch OLED Édition Mario Rouge" to "file:///android_asset/console_photos/switch_oled_mario_rouge.webp",
        "Switch OLED Édition Super Mario Bros. Wonder" to "file:///android_asset/console_photos/switch_oled_mario_wonder.webp",
        "Switch 2" to "file:///android_asset/console_photos/switch2.webp",
        "Switch 2 Édition Pokémon Legends: Z-A" to "file:///android_asset/console_photos/switch2_pokemon_za.webp",
        "Xbox One X" to "file:///android_asset/console_photos/xbox_one_x_edition_project_scorpio.webp",
        "Xbox Series S" to "file:///android_asset/console_photos/xbox_series_s.webp",
        "Xbox Series X" to "file:///android_asset/console_photos/xbox_series_x.webp",
        "PlayStation 5" to "file:///android_asset/console_photos/playstation_5.webp",
        "Switch OLED" to "file:///android_asset/console_photos/switch_oled.webp",
        "Pocket" to "file:///android_asset/console_photos/pocket.webp",
        "Steam Deck" to "file:///android_asset/console_photos/steam_deck.webp",
        "Playdate" to "file:///android_asset/console_photos/playdate.webp",
        "PSP Go" to "file:///android_asset/console_photos/psp_go.webp",
        "Odyssey 2" to "file:///android_asset/console_photos/odyssey_2.webp",
        "Microvision" to "file:///android_asset/console_photos/microvision.webp",
        "Intellivision" to "file:///android_asset/console_photos/intellivision_ii.webp",
        "5200" to "file:///android_asset/console_photos/5200.webp",
        "ColecoVision" to "file:///android_asset/console_photos/colecovision.webp",
        "Vectrex" to "file:///android_asset/console_photos/vectrex.webp",
        "SG-1000" to "file:///android_asset/console_photos/sg_1000.webp",
        "SC-3000" to "file:///android_asset/console_photos/sc_3000.webp",
        "PlayStation 5 (Slim)" to "file:///android_asset/console_photos/ps5_slim.webp",
        "PlayStation Portal" to "file:///android_asset/console_photos/ps_portal.webp",
        "PlayStation 5 Pro" to "file:///android_asset/console_photos/ps5_pro.webp",
        "PlayStation 5 Édition 30e Anniversaire" to "file:///android_asset/console_photos/ps5_30th_anniversary.webp",
        "PlayStation Portal Édition 30e Anniversaire" to "file:///android_asset/console_photos/ps_portal_30th.webp",
        "PlayStation Portal Midnight Black" to "file:///android_asset/console_photos/ps_portal_black.webp",
        "NEOGEO AES+ Édition 35e Anniversaire" to "file:///android_asset/console_photos/neogeo_aesplus_35th.webp",
        "Neo Geo AES+" to "file:///android_asset/console_photos/neogeo_aesplus.webp",
        "Vectrex Mini" to "file:///android_asset/console_photos/vectrex_mini.webp",
        "PSone" to "file:///android_asset/console_photos/psone.webp",
        "PlayStation 5 Édition Disque God of War Ragnarök" to "file:///android_asset/console_photos/ps5_disc_god_of_war.webp",
        "PlayStation 5 Édition Numérique God of War Ragnarök" to "file:///android_asset/console_photos/ps5_digital_god_of_war.webp",
        "PlayStation 5 Édition Marvel's Spider-Man 2" to "file:///android_asset/console_photos/ps5_spiderman2.webp",
        "PlayStation 5 Édition Ghost of Yōtei (Or)" to "file:///android_asset/console_photos/ps5_ghost_of_yotei.webp",
        "PlayStation 5 Édition Ghost of Yōtei (Noir)" to "file:///android_asset/console_photos/ps5_ghost_of_yotei_black.webp",
        "Famicom Disk System" to "file:///android_asset/console_photos/famicom_disk_system.webp",
        "7800" to "file:///android_asset/console_photos/7800.webp",
        "XEGS" to "file:///android_asset/console_photos/xegs.webp",
        "Gamate" to "file:///android_asset/console_photos/gamate.webp",
        "Playdia" to "file:///android_asset/console_photos/playdia.webp",
        "Nintendo 64DD" to "file:///android_asset/console_photos/nintendo_64dd.webp",
        "N-Gage" to "file:///android_asset/console_photos/n_gage.webp",
        "TurboGrafx-16" to "file:///android_asset/console_photos/turbografx_16.webp",
        "Mega CD" to "file:///android_asset/console_photos/mega_cd.webp",
        "Supervision" to "file:///android_asset/console_photos/supervision.webp",
        "PC-FX" to "file:///android_asset/console_photos/pc_fx.webp",

        // --- Complétées via l'API Wikipédia/Commons (photos vérifiées) ---
        "32X" to "file:///android_asset/console_photos/32x.webp",
        "GX4000" to "file:///android_asset/console_photos/gx4000.webp",
        "TurboExpress" to "file:///android_asset/console_photos/turboexpress.webp",
        "CDTV" to "file:///android_asset/console_photos/cdtv.webp",
        "Jaguar" to "file:///android_asset/console_photos/jaguar.webp",
        "Neo Geo CD" to "file:///android_asset/console_photos/neo_geo_cd.webp",
        "Neo Geo CD (Front Loader)" to "file:///android_asset/console_photos/neogeo_cd_frontloader.webp",
        "Nomad" to "file:///android_asset/console_photos/nomad.webp",
        "Pippin" to "file:///android_asset/console_photos/pippin.webp",
        "Pico" to "file:///android_asset/console_photos/pico.webp",
        "Game.com" to "file:///android_asset/console_photos/game_com.webp",
        // Corrigé le 2026-07-13 : les deux fiches partageaient la même photo (WonderSwan Color) —
        // photos fournies par l'utilisateur, distinctes pour chaque modèle.
        "WonderSwan" to "file:///android_asset/console_photos/wonderswan.webp",
        "WonderSwan Color" to "file:///android_asset/console_photos/wonderswan_color.webp",
        "Zodiac" to "file:///android_asset/console_photos/zodiac.webp",
        "Evercade" to "file:///android_asset/console_photos/evercade.webp",
        "Studio II" to "file:///android_asset/console_photos/studio_ii.webp",
        "Astrocade" to "file:///android_asset/console_photos/astrocade.webp",
        "Cassette Vision" to "file:///android_asset/console_photos/cassette_vision.webp",
        "Arcadia 2001" to "file:///android_asset/console_photos/arcadia_2001.webp",
        "Intellivision II" to "file:///android_asset/console_photos/intellivision_ii.webp",
        "PV-1000" to "file:///android_asset/console_photos/pv_1000.webp",
        "Super Cassette Vision" to "file:///android_asset/console_photos/super_cassette_vision.webp",
        "Amiga CD32" to "file:///android_asset/console_photos/amiga_cd32.webp",
        "FM Towns Marty" to "file:///android_asset/console_photos/fm_towns_marty.webp",
        "CDX" to "file:///android_asset/console_photos/cdx.webp",
        "Jaguar CD" to "file:///android_asset/console_photos/jaguar_cd.webp",
        "R-Zone" to "file:///android_asset/console_photos/r_zone.webp",
        "Coleco Gemini" to "file:///android_asset/console_photos/coleco_gemini.webp",
        "Videopac G7200" to "file:///android_asset/console_photos/videopac_g7200.webp",
        "Mega Drive 2" to "file:///android_asset/console_photos/mega_drive_2.webp",
        "Mega CD 2" to "file:///android_asset/console_photos/mega_cd_2.webp",
        "PC Engine Duo" to "file:///android_asset/console_photos/pc_engine_duo.webp",
        "PC Engine LT" to "file:///android_asset/console_photos/pc_engine_lt.webp",
        "Imagination Machine (MP1000)" to "file:///android_asset/console_photos/imagination_machine_mp1000.webp",
        "Twin Famicom" to "file:///android_asset/console_photos/twin_famicom.webp",
        "PC Engine Duo-RX" to "file:///android_asset/console_photos/pc_engine_duo_rx.webp",
        "Game Boy Advance SP" to "file:///android_asset/console_photos/game_boy_advance_sp.webp",
        "Nintendo DSi XL" to "file:///android_asset/console_photos/nintendo_dsi_xl.webp",
        "Nintendo 3DS XL" to "file:///android_asset/console_photos/nintendo_3ds_xl.webp",
        "Nintendo 2DS" to "file:///android_asset/console_photos/nintendo_2ds.webp",
        "New Nintendo 3DS XL" to "file:///android_asset/console_photos/new_nintendo_3ds_xl.webp",
        "New Nintendo 2DS XL" to "file:///android_asset/console_photos/new_nintendo_2ds_xl.webp",
        // Photos fournies par l'utilisateur (2026-07-19) : éditions spéciales DS/3DS sans photo
        // libre disponible sur Commons (voir la même remarque pour les Game & Watch ci-dessous).
        "3DS XL Édition Pikachu" to "file:///android_asset/console_photos/3ds_xl_pikachu.webp",
        "3DS XL Édition Pokémon X et Y (Rouge)" to "file:///android_asset/console_photos/3ds_xl_pokemon_xy_rouge.webp",
        "3DS XL Édition Pokémon X et Y (Bleue)" to "file:///android_asset/console_photos/3ds_xl_pokemon_xy_bleue.webp",
        "3DS XL Édition Super Smash Bros. Rouge" to "file:///android_asset/console_photos/3ds_xl_smash_bros.webp",
        "3DS XL Édition Animal Crossing" to "file:///android_asset/console_photos/3ds_xl_animal_crossing_rose.webp",
        "3DS XL Édition Animal Crossing: New Leaf (Special Edition)" to "file:///android_asset/console_photos/3ds_xl_animal_crossing_special_edition.webp",
        "New 2DS XL Édition Animal Crossing: New Leaf Bienvenue Amiibo" to "file:///android_asset/console_photos/new_2ds_xl_animal_crossing.webp",
        "New 2DS XL Édition Minecraft (Creeper)" to "file:///android_asset/console_photos/new_2ds_xl_minecraft.webp",
        "New 2DS XL Édition Poké Ball" to "file:///android_asset/console_photos/new_2ds_xl_poke_ball.webp",
        "New 2DS XL Édition Tomodachi Life" to "file:///android_asset/console_photos/new_2ds_xl_tomodachi_life.webp",
        "New 2DS XL Édition Super Mario 3D Land" to "file:///android_asset/console_photos/new_2ds_xl_super_mario_3d_land.webp",
        "New 3DS XL Édition The Legend of Zelda: Majora's Mask" to "file:///android_asset/console_photos/new_3ds_xl_majoras_mask.webp",
        "New Nintendo 3DS XL Rose et Blanc" to "file:///android_asset/console_photos/new_3ds_xl_blanche_rose.webp",
        "New 3DS XL Édition Pokémon Solgaleo et Lunala" to "file:///android_asset/console_photos/new_3ds_xl_solgaleo_lunala.webp",
        "New 3DS XL Édition Samus (Metroid: Samus Returns)" to "file:///android_asset/console_photos/new_3ds_xl_samus.webp",
        "New Nintendo 3DS Blanche" to "file:///android_asset/console_photos/new_3ds_blanche.webp",
        "New Nintendo 3DS Noire" to "file:///android_asset/console_photos/new_3ds_noire.webp",
        "New Nintendo 3DS Édition Xenoblade Chronicles 3D" to "file:///android_asset/console_photos/new_3ds_xenoblade_chronicles.webp",
        "New 3DS XL Édition Monster Hunter Generations" to "file:///android_asset/console_photos/new_3ds_xl_monster_hunter_generations.webp",
        // Photos fournies par l'utilisateur (2026-07-19, 2e lot) : suite des éditions DS/3DS.
        "New 2DS XL Édition Pikachu" to "file:///android_asset/console_photos/new_2ds_xl_pikachu.webp",
        "2DS Édition Pokémon Lune" to "file:///android_asset/console_photos/2ds_pokemon_lune.webp",
        "2DS Édition Pokémon Soleil" to "file:///android_asset/console_photos/2ds_pokemon_soleil.webp",
        "New 3DS XL Édition Monster Hunter 4 Ultimate" to "file:///android_asset/console_photos/new_3ds_xl_monster_hunter_4_ultimate.webp",
        // Même photo que ci-dessus : c'est la seule image "MH4 blanche" fournie, même si elle
        // montre en réalité la New 3DS XL Ultimate plutôt que l'édition Airu 2013 d'origine.
        "3DS XL Édition Monster Hunter 4 Airu Blanche" to "file:///android_asset/console_photos/new_3ds_xl_monster_hunter_4_ultimate.webp",
        "3DS XL Édition Monster Hunter 4 Goa Magara Noire" to "file:///android_asset/console_photos/3ds_xl_monster_hunter_4_goa_magara.webp",
        "3DS Édition The Legend of Zelda 25e anniversaire" to "file:///android_asset/console_photos/3ds_zelda_25th.webp",
        "Nintendo 3DS Édition Paper Mario: Sticker Star" to "file:///android_asset/console_photos/3ds_paper_mario_sticker_star.webp",
        "3DS XL Édition Super Smash Bros. Bleue" to "file:///android_asset/console_photos/3ds_xl_smash_bros_bleue.webp",
        "3DS XL Édition Luigi's Mansion 2" to "file:///android_asset/console_photos/3ds_xl_luigis_mansion2.webp",
        "3DS XL Édition The Legend of Zelda: A Link Between Worlds" to "file:///android_asset/console_photos/3ds_xl_zelda_link_between_worlds.webp",
        "3DS XL Édition Charizard" to "file:///android_asset/console_photos/3ds_xl_charizard.webp",
        "3DS XL Édition Luigi (Year of Luigi)" to "file:///android_asset/console_photos/3ds_xl_luigi_year_of_luigi.webp",
        "3DS XL Édition Mario & Luigi: Dream Team" to "file:///android_asset/console_photos/3ds_xl_mario_luigi_dream_team.webp",
        "DSi Édition Pokémon Blanche" to "file:///android_asset/console_photos/dsi_pokemon_blanche.webp",
        "DSi Édition Pokémon Noire" to "file:///android_asset/console_photos/dsi_pokemon_noire.webp",
        "DS Lite Édition Pokémon Dialga et Palkia" to "file:///android_asset/console_photos/ds_lite_dialga_palkia.webp",
        "DS Lite Édition Pokémon Giratina" to "file:///android_asset/console_photos/ds_lite_giratina.webp",
        "DS Lite Édition Pokémon Pikachu" to "file:///android_asset/console_photos/ds_lite_pikachu.webp",
        "DS Lite Édition The Legend of Zelda: Phantom Hourglass" to "file:///android_asset/console_photos/ds_lite_zelda_phantom_hourglass.webp",
        "New 3DS XL Édition Pokémon Pikachu" to "file:///android_asset/console_photos/new_3ds_xl_pikachu.webp",
        "3DS XL Édition Persona Q: Shadow of the Labyrinth" to "file:///android_asset/console_photos/3ds_xl_persona_q.webp",
        "GameCube (Panasonic Q)" to "file:///android_asset/console_photos/gamecube_panasonic_q.webp",
        "Wii U Édition The Wind Waker HD" to "file:///android_asset/console_photos/wii_u_edition_the_wind_waker_hd.webp",
        "Nintendo 64 Jungle Green" to "file:///android_asset/console_photos/nintendo_64_jungle_green.webp",
        "Nintendo 64 Fire Orange" to "file:///android_asset/console_photos/nintendo_64_fire_orange.webp",
        "NES Classic Edition" to "file:///android_asset/console_photos/nes_classic_edition_2.webp",
        "Super NES Classic Edition" to "file:///android_asset/console_photos/super_nes_classic_edition.webp",
        "Mega Drive Mini" to "file:///android_asset/console_photos/mega_drive_mini.webp",
        "Neo Geo Mini" to "file:///android_asset/console_photos/neo_geo_mini.webp",
        "Neo Geo Mini Japon" to "file:///android_asset/console_photos/neogeo_mini_jp.webp",
        "Neo Geo Mini Christmas Limited Edition" to "file:///android_asset/console_photos/non_libre/neogeo_mini_xmas.webp",
        "Neo Geo Mini Samurai Shodown Haohmaru" to "file:///android_asset/console_photos/non_libre/neogeo_mini_ss_haohmaru.webp",
        "Neo Geo Mini Samurai Shodown Nakoruru" to "file:///android_asset/console_photos/non_libre/neogeo_mini_ss_nakoruru.webp",
        "Neo Geo Mini Samurai Shodown Ukyo Tachibana" to "file:///android_asset/console_photos/non_libre/neogeo_mini_ss_ukyo.webp",
        "PlayStation Classic" to "file:///android_asset/console_photos/playstation_classic.webp",
        "ZX Spectrum" to "file:///android_asset/console_photos/zx_spectrum.webp",
        "Commodore 64" to "file:///android_asset/console_photos/commodore_64.webp",
        "CPC 464" to "file:///android_asset/console_photos/cpc_464.webp",
        "CPC 6128" to "file:///android_asset/console_photos/cpc_6128.webp",
        "CPC 6128+" to "file:///android_asset/console_photos/cpc_6128_2.webp",
        "1040 STF" to "file:///android_asset/console_photos/1040_stf.webp",
        "Amiga 500" to "file:///android_asset/console_photos/amiga_500.webp",
        "MSX" to "file:///android_asset/console_photos/msx.webp",
        // Xbox Series X Édition Halo Infinite / PC Engine Mini : photos retirées (sites
        // officiels/CDN constructeurs, non redistribuables dans une app publiée ; aucune photo
        // libre trouvée sur Wikimedia Commons). L'utilisateur peut attacher sa propre photo à la
        // fiche. (Atari 2600+ Édition Pac-Man : voir photo embarquée plus bas.)

        // --- Photos Wikimedia Commons (licence libre) ---
        "Nintendo 64 Édition Pikachu" to "file:///android_asset/console_photos/nintendo_64_edition_pikachu.webp",
        "Nintendo 64 Ice Blue" to "file:///android_asset/console_photos/nintendo_64_ice_blue.webp",
        "Nintendo 64 Grape Purple" to "file:///android_asset/console_photos/nintendo_64_grape_purple.webp",
        "Nintendo 64 Watermelon Red" to "file:///android_asset/console_photos/nintendo_64_watermelon_red.webp",
        "Xbox 360 Édition Halo 3" to "file:///android_asset/console_photos/xbox360_halo3.webp",

        // --- Éditions limitées : SEULES les photos sous licence libre (Wikimedia Commons) sont
        // conservées. Les anciennes photos de presse/officielles (Flickr constructeur, Xbox Wire,
        // Minecraft.net) et celles de consolevariations.com (licence non clarifiée) ont été
        // RETIRÉES pour la publication de l'app — pas de redistribution publique possible.
        // Fiches concernées désormais sans photo intégrée (photo personnelle attachable) :
        // PS4 Uncharted 4, PS4 Pro Spider-Man, Xbox 360 Gears of War / Halo 4, Xbox One Halo 5,
        // Xbox One S Minecraft, Xbox One X Cyberpunk 2077, Game Gear Micro, PSP God of War (x2),
        // PSP Monster Hunter 2nd G, PS Vita Hatsune Miku.
        "NeoGeo X Gold" to "file:///android_asset/console_photos/neogeo_x_gold.webp",

        // --- Photos fournies par l'utilisateur, embarquées dans l'appli (dossier assets) ---
        "Game Gear Jaune" to "file:///android_asset/console_photos/game_gear_jaune.webp",
        "Game Gear Magic Knight Rayearth" to "file:///android_asset/console_photos/game_gear_magic_knight_rayearth.webp",
        "Xbox One X Édition Project Scorpio" to "file:///android_asset/console_photos/xbox_one_x_project_scorpio.webp",
        "Wii U Édition Super Smash Bros." to "file:///android_asset/console_photos/wii_u_super_smash_bros.webp",
        "Wii Édition 25e Anniversaire Mario" to "file:///android_asset/console_photos/wii_25e_anniversaire_mario.webp",
        "Game Gear Rouge" to "file:///android_asset/console_photos/game_gear_rouge.webp",
        "Xbox 360 Édition Kinect Star Wars" to "file:///android_asset/console_photos/xbox_360_kinect_star_wars.webp",
        "PlayStation 4 Pro Édition The Last of Us Part II" to "file:///android_asset/console_photos/ps4_pro_the_last_of_us_part_ii.webp",

        // --- Game Boy Light / Game Boy Micro : photos Wikimedia Commons (licence libre) ---
        "Game Boy Light" to "file:///android_asset/console_photos/game_boy_light.webp",
        "Game Boy Micro" to "file:///android_asset/console_photos/game_boy_micro.webp",

        // --- Machines ajoutées : photos Wikimedia Commons (licence libre, vérifiées HTTP 200) ---
        // Compact Vision TV Boy : aucune photo libre trouvée (machine très rare) -> photo perso attachable.
        "C64 Games System" to "file:///android_asset/console_photos/c64_games_system.webp",
        "LaserActive" to "file:///android_asset/console_photos/laseractive.webp",
        "Apple II" to "file:///android_asset/console_photos/apple_ii.webp",
        "TRS-80" to "file:///android_asset/console_photos/trs_80.webp",
        "PET" to "file:///android_asset/console_photos/pet.webp",
        "400 / 800" to "file:///android_asset/console_photos/400_800.webp",
        "VIC-20" to "file:///android_asset/console_photos/vic_20.webp",
        "ZX81" to "file:///android_asset/console_photos/zx81.webp",
        "BBC Micro" to "file:///android_asset/console_photos/bbc_micro.webp",
        "TO7" to "file:///android_asset/console_photos/to7.webp",
        "MO5" to "file:///android_asset/console_photos/mo5.webp",
        "Macintosh" to "file:///android_asset/console_photos/macintosh.webp",
        "X68000" to "file:///android_asset/console_photos/x68000.webp",
        "Archimedes" to "file:///android_asset/console_photos/archimedes.webp",
        "Amiga 1200" to "file:///android_asset/console_photos/amiga_1200.webp",

        // --- Complétées le 2026-07-12 (audit "aucune fiche sans photo") ---
        "Compact Vision TV Boy" to "file:///android_asset/console_photos/compact_vision_tv_boy.webp",

        // Éditions limitées commerciales : aucune photo de CETTE édition précise n'est disponible
        // sous licence libre sur Wikimedia Commons (recherche vérifiée) — on affiche donc la vraie
        // photo de la console de base plutôt que de laisser la fiche sans image. Ce n'est pas la
        // déco exacte, mais c'est le même matériel et une vraie photo, pas une image inventée.
        "Game Gear Micro" to "file:///android_asset/console_photos/game_gear_micro.webp",
        "PC Engine Mini" to "file:///android_asset/console_photos/pc_engine_mini.webp",
        // Photos fournies par l'utilisateur (2026-07-13) : remplacent les anciens rendus 3D
        // Sketchfab (souvent partagés entre plusieurs éditions différentes) par de vraies photos
        // de chaque exemplaire.
        "PSP Édition God of War: Chains of Olympus" to "file:///android_asset/console_photos/psp_god_of_war_chains_of_olympus.webp",
        "PSP Édition God of War: Ghost of Sparta" to "file:///android_asset/console_photos/psp_god_of_war_ghost_of_sparta.webp",
        "PSP Édition Monster Hunter Portable 2nd G" to "file:///android_asset/console_photos/psp_monster_hunter_portable_2nd_g.webp",
        "PlayStation 4 Pro Édition Marvel's Spider-Man" to "file:///android_asset/console_photos/ps4_pro_spiderman.webp",
        "PlayStation 4 Édition Uncharted 4" to "file:///android_asset/console_photos/ps4_uncharted4.webp",
        "PlayStation Vita Édition Hatsune Miku" to "file:///android_asset/console_photos/ps_vita_hatsune_miku.webp",
        "Xbox 360 Édition Gears of War" to "file:///android_asset/console_photos/xbox360_gears_of_war.webp",
        "Xbox 360 Édition Halo 4" to "file:///android_asset/console_photos/xbox360_halo4.webp",
        "Xbox One S Édition Minecraft" to "file:///android_asset/console_photos/xbox_one_s_minecraft.webp",
        "Xbox One X Édition Cyberpunk 2077" to "file:///android_asset/console_photos/xbox_one_x_cyberpunk.webp",
        // _v2 ne s'affichait toujours pas (vide, pas juste noir) malgré un JPEG valide et un nom
        // de fichier différent — cause exacte non identifiée. Remplacée par une photo entièrement
        // neuve (2026-07-19) fournie par l'utilisateur plutôt que de continuer à réutiliser le
        // même fichier source.
        "Xbox One Édition Halo 5 Guardians" to "file:///android_asset/console_photos/xbox_one_halo5_v3.webp",
        "Xbox Series X Édition Halo Infinite" to "file:///android_asset/console_photos/xbox_series_x_halo_infinite.webp",
        // Photo fournie par l'utilisateur (2026-07-13) : aucune photo libre trouvée sur Commons
        // (recherche vérifiée), remplace le repli générique / rendu manquant.
        "Atari 2600+ Édition Pac-Man" to "file:///android_asset/console_photos/atari_2600plus_pacman.webp",

        // --- Bundles console + jeu(x) d'époque (2026-07-13) : photos Wikimedia Commons vérifiées.
        // La plupart des jaquettes/boîtes de bundles sont sous droit d'auteur et absentes de
        // Commons — seuls les bundles ci-dessous ont une vraie photo libre trouvée (matériel nu
        // ou photo de déballage montrant clairement le contenu). Les autres fiches bundle restent
        // sans photo intégrée (photo personnelle attachable).
        "2600 Jr." to "file:///android_asset/console_photos/2600_jr.webp",
        "NES Édition Deluxe Set" to "file:///android_asset/console_photos/nes_edition_deluxe_set.webp",
        "NES Édition Action Set" to "file:///android_asset/console_photos/nes_edition_action_set.webp",
        "Master System II" to "file:///android_asset/console_photos/master_system_ii.webp",
        "Game Boy Édition Tetris" to "file:///android_asset/console_photos/game_boy_edition_tetris.webp",
        "Mega Drive II" to "file:///android_asset/console_photos/mega_drive_ii.webp",
        "PC Engine GT" to "file:///android_asset/console_photos/pc_engine_gt.webp",
        "Neo Geo CDZ" to "file:///android_asset/console_photos/neo_geo_cdz.webp",
        // Vraie édition officielle Pokémon Jaune (pas un repeint fan-made) : Pikachu/Pichu + logo
        // Pokémon sérigraphiés sur une Game Boy Color or/orange.
        "Game Boy Color Édition Pokémon Yellow" to "file:///android_asset/console_photos/game_boy_color_edition_pokemon_yellow.webp",

        // --- Bundles : photos de jaquette/boîte issues de sites de revente rétro (aucune photo
        // libre sur Wikimedia Commons pour ces jaquettes sous droit d'auteur). Vérifiées une par
        // une (visionnage direct) avant intégration — l'utilisateur a validé ce niveau de risque
        // pour son appli personnelle (2026-07-13).
        "NES Édition Power Set" to "file:///android_asset/console_photos/nes_power_set.webp",
        "NES Édition Super Mario Bros. 3" to "file:///android_asset/console_photos/nes_challenge_set_smb3.webp",
        "Dreamcast Édition Sonic" to "file:///android_asset/console_photos/dreamcast_sonic_bundle.webp",
        "Saturn Édition Nights into Dreams" to "file:///android_asset/console_photos/saturn_nights_into_dreams.webp",
        "GameCube Édition Super Smash Bros. Melee" to "file:///android_asset/console_photos/gamecube_melee_bundle.webp",
        "GameCube Édition Mario Kart: Double Dash!!" to "file:///android_asset/console_photos/gamecube_mariokartdd_bundle.webp",
        "Videopac C52" to "file:///android_asset/console_photos/videopac_c52.webp",
        "GameCube Édition Pikmin 2" to "file:///android_asset/console_photos/gamecube_pikmin2_bundle.webp",
        "Super Nintendo Édition Super Mario World" to "file:///android_asset/console_photos/snes_super_mario_world_bundle.webp",
        "Mega Drive Édition Sonic the Hedgehog" to "file:///android_asset/console_photos/genesis_sonic_bundle.webp",
        "NES Édition Sports Set" to "file:///android_asset/console_photos/nes_sports_set.webp",
        "TurboGrafx-16 Édition Keith Courage" to "file:///android_asset/console_photos/tg16_keith_courage.webp",
        "Saturn Édition Three Free Games" to "file:///android_asset/console_photos/saturn_three_free_games.webp",

        // --- Photos fournies par l'utilisateur (2026-07-13), 2e vague : comble les derniers
        // manques de la vague précédente + nouveaux bundles GameCube/Master System/Game Gear/
        // Mega Drive II/SNES/NES/Game Boy. Vérifiées une par une (visionnage direct).
        "Master System Édition Hang-On / Astro Warrior" to "file:///android_asset/console_photos/ms_hangon_astrowarrior.webp",
        "Master System Édition Hang-On / Safari Hunt" to "file:///android_asset/console_photos/ms_hangon_safarihunt.webp",
        "Mega Drive Édition Altered Beast" to "file:///android_asset/console_photos/md_altered_beast.webp",
        "Saturn Édition Virtua Fighter" to "file:///android_asset/console_photos/saturn_virtua_fighter.webp",
        "Super Nintendo Édition Street Fighter II Turbo" to "file:///android_asset/console_photos/snes_street_fighter2.webp",
        "Super Nintendo Édition Super Set (Mario World / Mario Kart)" to "file:///android_asset/console_photos/snes_super_set_mariokart.webp",
        "Super Nintendo Édition Yoshi's Island" to "file:///android_asset/console_photos/snes_yoshis_island.webp",
        "NES Édition Teenage Mutant Hero Turtles" to "file:///android_asset/console_photos/nes_turtles.webp",
        "GameCube Édition Resident Evil 4" to "file:///android_asset/console_photos/gc_resident_evil4.webp",
        "GameCube Édition Tales of Symphonia" to "file:///android_asset/console_photos/gc_tales_of_symphonia.webp",
        "GameCube Édition The Legend of Zelda: The Wind Waker" to "file:///android_asset/console_photos/gc_windwaker.webp",
        "Master System Édition Super System" to "file:///android_asset/console_photos/ms_super_system.webp",
        "Master System II Plus Édition Rambo III" to "file:///android_asset/console_photos/ms2_plus_rambo3.webp",
        "Game Gear Édition Aladdin" to "file:///android_asset/console_photos/gg_aladdin.webp",
        "Game Gear Édition Le Roi Lion" to "file:///android_asset/console_photos/gg_lionking.webp",
        "Game Gear Édition Le Livre de la Jungle" to "file:///android_asset/console_photos/gg_junglebook.webp",
        "Game Gear Édition Sonic" to "file:///android_asset/console_photos/gg_sonic.webp",
        "Mega Drive II Édition Aladdin" to "file:///android_asset/console_photos/md2_aladdin.webp",
        "Mega Drive II Édition Le Roi Lion" to "file:///android_asset/console_photos/md2_lionking.webp",
        "Mega Drive II Édition Sonic 3" to "file:///android_asset/console_photos/md2_sonic3.webp",
        "Super Nintendo Control Set" to "file:///android_asset/console_photos/snes_control_set.webp",
        "Super Nintendo Power Set 3" to "file:///android_asset/console_photos/snes_power_set3.webp",
        "Super Nintendo Édition Donkey Kong Country" to "file:///android_asset/console_photos/snes_donkey_kong_country.webp",
        "Super Nintendo Scope Set" to "file:///android_asset/console_photos/snes_scope_set.webp",
        "Super Nintendo Édition Super Mario All-Stars" to "file:///android_asset/console_photos/snes_super_mario_allstars.webp",
        "Super Nintendo Édition Super Mario All-Stars + Super Mario World" to "file:///android_asset/console_photos/snes_allstars_plus_smw.webp",
        "NES Édition Super Mario Bros. 2" to "file:///android_asset/console_photos/nes_super_mario_bros2.webp",
        "NES Édition Control Deck" to "file:///android_asset/console_photos/nes_control_deck.webp",
        "Game Boy Édition Tetris + Kirby's Dream Land" to "file:///android_asset/console_photos/gb_tetris_kirby.webp",
        "Game Boy Édition The Legend of Zelda: Link's Awakening" to "file:///android_asset/console_photos/gb_zelda_linksawakening.webp",
        "Mega Drive II Édition Sonic Compilation" to "file:///android_asset/console_photos/md2_sonic_compilation.webp",
        "Master System II Édition Sonic" to "file:///android_asset/console_photos/ms2_sonic.webp",
        "Master System II Édition Aladdin" to "file:///android_asset/console_photos/ms2_aladdin.webp",

        // Révisions matérielles et matériel FPGA/rétro moderne (recherche 2026-07-25), toutes en
        // vraies photos Wikimedia Commons vérifiées.
        "PlayStation 2 (Slim)" to "file:///android_asset/console_photos/playstation_2_slim.webp",
        "PlayStation 3 (Slim)" to "file:///android_asset/console_photos/playstation_3_slim.webp",
        "PlayStation 3 (Super Slim)" to "file:///android_asset/console_photos/playstation_3_super_slim.webp",
        "Xbox 360 S (Slim)" to "file:///android_asset/console_photos/xbox_360_s_slim.webp",
        "Xbox 360 E" to "file:///android_asset/console_photos/xbox_360_e.webp",
        "PlayStation 4 (Slim)" to "file:///android_asset/console_photos/playstation_4_slim.webp",
        "Wii Mini" to "file:///android_asset/console_photos/wii_mini.webp",
        "PlayStation Vita (Slim)" to "file:///android_asset/console_photos/playstation_vita_slim.webp",
        "PSP-2000 (Slim & Lite)" to "file:///android_asset/console_photos/psp_2000_slim_lite.webp",
        "PSP-3000" to "file:///android_asset/console_photos/psp_3000.webp",
        "GameCube Jet Black" to "file:///android_asset/console_photos/gamecube_jet_black.webp",
        "GameCube Spice Orange" to "file:///android_asset/console_photos/gamecube_spice_orange.webp",
        "Super Nt" to "file:///android_asset/console_photos/super_nt.webp",
        "Mega Sg" to "file:///android_asset/console_photos/mega_sg.webp",
        "Evercade VS" to "file:///android_asset/console_photos/evercade_vs.webp",
        "Evercade EXP" to "file:///android_asset/console_photos/evercade_exp.webp",
        "Mega Drive Mini 2" to "file:///android_asset/console_photos/mega_drive_mini_2.webp",
        "WonderSwan Crystal" to "file:///android_asset/console_photos/wonderswan_crystal.webp",
        "Xbox One S" to "file:///android_asset/console_photos/xbox_one_s.webp",
        "PSP E-1000 (Street)" to "file:///android_asset/console_photos/psp_e_1000_street.webp",
        "PlayStation 3 Édition Final Fantasy XIII (Lightning)" to "file:///android_asset/console_photos/ff13_ps3.webp",
        "Xbox 360 Elite" to "file:///android_asset/console_photos/xbox360_elite.webp",

        // Consoles réintégrées malgré l'absence de photo librement réutilisable (2026-07-26) :
        // photos officielles/presse (Microsoft, Analogue, sites d'archives collector) dont les droits
        // ne sont PAS libres — stockées à part dans assets/console_photos/non_libre/ pour bien les
        // distinguer des photos Wikimedia Commons ci-dessus.
        "Xbox One S All-Digital Edition" to "file:///android_asset/console_photos/non_libre/xbox_one_s_alldigital.webp",
        "Analogue Duo" to "file:///android_asset/console_photos/non_libre/analogue_duo.webp",
        "Analogue 3D" to "file:///android_asset/console_photos/non_libre/analogue_3d.webp",
        "PlayStation 3 Édition Metal Gear Solid 4 (Hagane)" to "file:///android_asset/console_photos/non_libre/ps3_mgs4_hagane.webp",
        "PlayStation 2 Ceramic White" to "file:///android_asset/console_photos/non_libre/ps2_ceramic_white.webp",
        "PlayStation 2 Sakura" to "file:///android_asset/console_photos/non_libre/ps2_sakura.webp",
        "Xbox 360 Édition Call of Duty: Modern Warfare 3" to "file:///android_asset/console_photos/non_libre/xbox360_mw3.webp",
        "Xbox 360 S Édition Halo: Reach" to "file:///android_asset/console_photos/non_libre/xbox360_halo_reach.webp",

        // --- Éditions collector complémentaires (recherche approfondie 2026-07-26) ---
        "PlayStation 4 Édition Metal Gear Solid V" to "file:///android_asset/console_photos/non_libre/ps4_metal_gear_solid_v.webp",
        "PlayStation 4 Pro Édition Kingdom Hearts III" to "file:///android_asset/console_photos/non_libre/ps4_pro_kingdom_hearts3.webp",
        "Game Boy Color Édition Pokémon Center (Or & Argent)" to "file:///android_asset/console_photos/non_libre/gbc_pokemon_center.webp",
        "Game Boy Color Édition Hello Kitty" to "file:///android_asset/console_photos/non_libre/gbc_hello_kitty.webp",
        "Game Boy Light Édition Astro Boy (Tetsuwan Atom)" to "file:///android_asset/console_photos/non_libre/gblight_astroboy.webp",
        "PlayStation Vita Slim Édition Persona 4: Dancing All Night" to "file:///android_asset/console_photos/non_libre/vita_persona4_dancing_all_night.webp",
        "PlayStation Vita Slim Édition Danganronpa" to "file:///android_asset/console_photos/non_libre/vita_danganronpa.webp",
        "PlayStation Vita (PCH-2000) Édition New Danganronpa V3" to "file:///android_asset/console_photos/non_libre/vita_danganronpa_v3.webp",
        "PSP Édition Monster Hunter Portable 3rd" to "file:///android_asset/console_photos/non_libre/psp_monster_hunter_portable_3rd.webp",
        "PSP-3000 Édition Hello Kitty Puzzle Party" to "file:///android_asset/console_photos/non_libre/psp_hello_kitty_puzzle_party.webp",
        "Lynx II" to "file:///android_asset/console_photos/lynx2.webp",

        // --- Coloris officiels complémentaires GB/GBC/GBA/PSP/PS Vita (recherche 2026-07-26),
        // toutes en vraies photos Wikimedia Commons vérifiées (licence libre). ---
        "Game Boy Play It Loud! Noire" to "file:///android_asset/console_photos/gb_black.webp",
        "Game Boy Play It Loud! Rouge" to "file:///android_asset/console_photos/gb_red_gray.webp",
        "Game Boy Play It Loud! Transparente" to "file:///android_asset/console_photos/gb_clear.webp",
        "Game Boy Pocket Noire" to "file:///android_asset/console_photos/gbp_black.webp",
        "Game Boy Pocket Bleue" to "file:///android_asset/console_photos/gbp_blue.webp",
        "Game Boy Pocket Verte" to "file:///android_asset/console_photos/gbp_green.webp",
        "Game Boy Pocket Rouge" to "file:///android_asset/console_photos/gbp_red.webp",
        "Game Boy Color Berry (Rouge)" to "file:///android_asset/console_photos/gbc_berry.webp",
        "Game Boy Color Dandelion (Jaune)" to "file:///android_asset/console_photos/gbc_dandelion.webp",
        "Game Boy Color Kiwi (Vert)" to "file:///android_asset/console_photos/gbc_kiwi.webp",
        "Game Boy Color Teal (Bleu Sarcelle)" to "file:///android_asset/console_photos/gbc_teal.webp",
        "Game Boy Color Grape (Violet)" to "file:///android_asset/console_photos/gbc_grape.webp",
        "Game Boy Color Atomic Purple (Violet Transparent)" to "file:///android_asset/console_photos/gbc_atomic_purple.webp",
        "Game Boy Advance Indigo" to "file:///android_asset/console_photos/gba_indigo.webp",
        "Game Boy Advance Arctic White" to "file:///android_asset/console_photos/gba_white.webp",
        "Game Boy Advance Rose" to "file:///android_asset/console_photos/gba_pink.webp",
        "Game Boy Advance Noire" to "file:///android_asset/console_photos/gba_black.webp",
        "PSP-3000 Vibrant Blue" to "file:///android_asset/console_photos/psp3000_vblue.webp",
        "PSP Go Blanche Nacrée" to "file:///android_asset/console_photos/pspgo_white.webp",
        "PlayStation Vita Sapphire Blue" to "file:///android_asset/console_photos/vita_sapphire.webp",
        "PlayStation Vita Slim Blanche" to "file:///android_asset/console_photos/vitaslim_white.webp",
        "PlayStation 3 Super Slim Blanche" to "file:///android_asset/console_photos/ps3_superslim_white.webp",
        "PlayStation 3 Super Slim Rouge Grenat" to "file:///android_asset/console_photos/ps3_superslim_red.webp",
        // Pas de photo libre de droits (Wikimedia Commons) trouvée pour ce coloris : photo produit
        // officielle GameStop reprise à la place (même choix qu'ailleurs, cf. commentaire mini-consoles
        // plus haut dans ce fichier).
        "PlayStation 3 Super Slim Bleu Azurite" to "file:///android_asset/console_photos/playstation_3_super_slim_bleu_azurite.webp",

        // --- Coloris officiels complémentaires GameCube / PSP / PS Vita + photos manquantes de 4
        // presets PS3 déjà existants (recherche 2026-08-01). Aucune photo libre de droits
        // (Wikimedia Commons) trouvée pour ces modèles précis malgré recherche : photos produit
        // officielles reprises depuis consolevariations.com / pricecharting.com à la place (accord
        // explicite préalable de l'utilisateur), rangées dans non_libre/. ---
        "GameCube Pearl White" to "file:///android_asset/console_photos/non_libre/gamecube_pearl_white.webp",
        "PlayStation Vita Crystal White" to "file:///android_asset/console_photos/non_libre/vita_crystal_white.webp",
        "PlayStation Vita Cosmic Red" to "file:///android_asset/console_photos/non_libre/vita_cosmic_red.webp",
        "PlayStation Vita Silver Ice" to "file:///android_asset/console_photos/non_libre/vita_silver_ice.webp",
        "PlayStation Vita Slim Lime Green" to "file:///android_asset/console_photos/non_libre/vita_slim_lime_green.webp",
        "PlayStation Vita Slim Light Blue" to "file:///android_asset/console_photos/non_libre/vita_slim_light_blue.webp",
        "PlayStation Vita Slim Pink" to "file:///android_asset/console_photos/non_libre/vita_slim_pink.webp",
        "PlayStation Vita Slim Khaki" to "file:///android_asset/console_photos/non_libre/vita_slim_khaki.webp",
        "PlayStation Vita Slim Aqua Blue" to "file:///android_asset/console_photos/non_libre/vita_slim_aqua_blue.webp",
        "PlayStation Vita Slim Glacier White" to "file:///android_asset/console_photos/non_libre/vita_slim_glacier_white.webp",
        "PlayStation Vita Slim Neon Orange" to "file:///android_asset/console_photos/non_libre/vita_slim_neon_orange.webp",
        "PlayStation Vita Slim Metallic Red" to "file:///android_asset/console_photos/non_libre/vita_slim_metallic_red.webp",
        "PlayStation Vita Slim Silver" to "file:///android_asset/console_photos/non_libre/vita_slim_silver.webp",
        "PSP Ceramic White" to "file:///android_asset/console_photos/non_libre/psp_ceramic_white.webp",
        "PSP-2000 Ceramic White" to "file:///android_asset/console_photos/non_libre/psp_2000_ceramic_white.webp",
        "PSP-2000 Ice Silver" to "file:///android_asset/console_photos/non_libre/psp_2000_ice_silver.webp",
        "PSP-3000 Pearl White" to "file:///android_asset/console_photos/non_libre/psp_3000_pearl_white.webp",
        "PSP-3000 Mystic Silver" to "file:///android_asset/console_photos/non_libre/psp_3000_mystic_silver.webp",
        "PSP-3000 Radiant Red" to "file:///android_asset/console_photos/non_libre/psp_3000_radiant_red.webp",
        "PSP-3000 Blossom Pink" to "file:///android_asset/console_photos/non_libre/psp_3000_blossom_pink.webp",
        "PSP-3000 Spirited Green" to "file:///android_asset/console_photos/non_libre/psp_3000_spirited_green.webp",
        // Photos manquantes pour 4 presets PS3 déjà existants (pas de nouveau preset créé ici).
        "PlayStation 3 Super Slim Édition Gran Turismo 6 + GTA V" to "file:///android_asset/console_photos/non_libre/ps3_gt6_gtav.webp",
        "PlayStation 3 Scarlet Red" to "file:///android_asset/console_photos/non_libre/ps3_scarlet_red.webp",
        "PlayStation 3 Splash Blue" to "file:///android_asset/console_photos/non_libre/ps3_splash_blue.webp",
        "PlayStation 3 Super Slim Édition Uncharted 3: Game of the Year" to "file:///android_asset/console_photos/non_libre/ps3_uncharted3_goty.webp",

        // --- PHASE 1 (recherche 2026-08-01) : portables Nintendo exclusifs Japon. Photos produit
        // collector (consolevariations.com), non libres de droits -> sous-dossier non_libre/. ---
        "Game Boy Color Édition ANA (All Nippon Airways)" to "file:///android_asset/console_photos/non_libre/game_boy_color_ana_limited.webp",
        "Game Boy Color Édition Card Captor Sakura" to "file:///android_asset/console_photos/non_libre/game_boy_color_card_captor_sakura.webp",
        "Game Boy Color Clear Black (Chūkyō)" to "file:///android_asset/console_photos/non_libre/game_boy_color_clear_black_aiwon.webp",
        "Game Boy Color Clear Green (Toys'R'Us Japon)" to "file:///android_asset/console_photos/non_libre/game_boy_color_clear_green_toysrus.webp",
        "Game Boy Color Édition Jusco Mario Clear" to "file:///android_asset/console_photos/non_libre/game_boy_color_jusco_clear.webp",
        "Game Boy Color Édition Jusco Mario Clear Purple" to "file:///android_asset/console_photos/non_libre/game_boy_color_jusco_purple.webp",
        "Game Boy Color Édition Fukuoka Daiei Hawks" to "file:///android_asset/console_photos/non_libre/game_boy_color_daiei_hawks.webp",
        "Game Boy Color Édition Hello Kitty Special Box 2" to "file:///android_asset/console_photos/non_libre/game_boy_color_hello_kitty_special_box_2.webp",
        "Game Boy Color Ice Blue (Toys'R'Us Japon)" to "file:///android_asset/console_photos/non_libre/game_boy_color_ice_blue_toysrus.webp",
        "Game Boy Color Édition Lawson" to "file:///android_asset/console_photos/non_libre/game_boy_color_lawson.webp",
        "Game Boy Color Midnight Blue (Toys'R'Us Japon)" to "file:///android_asset/console_photos/non_libre/game_boy_color_midnight_blue_toysrus.webp",
        "Game Boy Color Édition Pokémon Center 3e Anniversaire" to "file:///android_asset/console_photos/non_libre/game_boy_color_pokemon_center_3rd_anniversary.webp",
        "Game Boy Color Water Blue (TSUTAYA)" to "file:///android_asset/console_photos/non_libre/game_boy_color_tsutaya_water_blue.webp",
        "Game Boy Color Édition Sakura Taisen" to "file:///android_asset/console_photos/non_libre/game_boy_color_sakura_taisen.webp",
        "Game Boy Color Édition Panasonic Alkaline" to "file:///android_asset/console_photos/non_libre/game_boy_color_panasonic.webp",

        "Game Boy Advance SP Famicom Color" to "file:///android_asset/console_photos/non_libre/gba_sp_famicom_color.webp",
        "Game Boy Advance SP Star Light Gold (Toys'R'Us Japon)" to "file:///android_asset/console_photos/non_libre/gba_sp_starlight_gold_toysrus.webp",
        "Game Boy Advance SP Pearl Green (Toys'R'Us Japon)" to "file:///android_asset/console_photos/non_libre/gba_sp_pearl_green_toysrus.webp",
        "Game Boy Advance SP Édition Pokémon Center Achamo (Torchic)" to "file:///android_asset/console_photos/non_libre/gba_sp_pokemon_center_achamo_torchic.webp",
        "Game Boy Advance SP Édition Pokémon Center Dracaufeu (Charizard)" to "file:///android_asset/console_photos/non_libre/gba_sp_pokemon_center_charizard.webp",
        "Game Boy Advance SP Édition Pokémon Center Florizarre (Venusaur)" to "file:///android_asset/console_photos/non_libre/gba_sp_pokemon_center_venusaur.webp",
        "Game Boy Advance SP Édition Pokémon Center Rayquaza" to "file:///android_asset/console_photos/non_libre/gba_sp_pokemon_center_rayquaza.webp",
        "Game Boy Advance SP Édition Pokémon Center Pikachu" to "file:///android_asset/console_photos/non_libre/gba_sp_pokemon_center_pikachu.webp",
        "Game Boy Advance SP Édition Pokémon Center Kaiogre (Kyogre)" to "file:///android_asset/console_photos/non_libre/gba_sp_pokemon_center_kyogre.webp",
        "Game Boy Advance SP Édition Pokémon Center Groudon" to "file:///android_asset/console_photos/non_libre/gba_sp_pokemon_center_groudon.webp",
        "Game Boy Advance SP Édition Boktai: The Sun Is in Your Hand" to "file:///android_asset/console_photos/non_libre/gba_sp_boktai_django_red_black.webp",
        "Game Boy Advance SP Édition Sword of Mana" to "file:///android_asset/console_photos/non_libre/gba_sp_sword_of_mana_blue.webp",
        "Game Boy Advance SP Édition SD Gundam G Generation Advance" to "file:///android_asset/console_photos/non_libre/gba_sp_sd_gundam_char_aznable.webp",
        "Game Boy Advance SP Édition Naruto RPG" to "file:///android_asset/console_photos/non_libre/gba_sp_naruto_orange.webp",
        "Game Boy Advance SP Édition Kingdom Hearts: Chain of Memories" to "file:///android_asset/console_photos/non_libre/gba_sp_kingdom_hearts_deep_silver.webp",

        "Game Boy Micro Famicom 20e Anniversaire" to "file:///android_asset/console_photos/non_libre/gb_micro_famicom_20th_anniversary.webp",
        "Game Boy Micro Édition Pokémon Center" to "file:///android_asset/console_photos/non_libre/gb_micro_pokemon_center.webp",
        "Game Boy Micro Édition Final Fantasy IV Advance" to "file:///android_asset/console_photos/non_libre/gb_micro_final_fantasy_iv_advance.webp",
        "Game Boy Micro Mother 3 Deluxe Box" to "file:///android_asset/console_photos/non_libre/gb_micro_mother_3_deluxe_box.webp",
        "Game Boy Micro Édition CoroCoro Comic 30e Anniversaire" to "file:///android_asset/console_photos/non_libre/gb_micro_corocoro_30th_anniversary.webp",

        "DS Lite Édition A Bathing Ape × Baby Milo (Or)" to "file:///android_asset/console_photos/non_libre/ds_lite_bape_baby_milo_gold.webp",
        "DS Lite Crimson/Black" to "file:///android_asset/console_photos/non_libre/ds_lite_crimson_black.webp",
        "DS Lite Édition Final Fantasy Crystal Chronicles: Ring of Fates" to "file:///android_asset/console_photos/non_libre/ds_lite_ff_crystal_chronicles_ring_of_fates.webp",
        "DS Lite Édition Final Fantasy III Crystal" to "file:///android_asset/console_photos/non_libre/ds_lite_final_fantasy_iii_crystal.webp",
        "DS Lite Édition Final Fantasy XII: Revenant Wings" to "file:///android_asset/console_photos/non_libre/ds_lite_ff12_revenant_wings.webp",
        "DS Lite Édition SD Gundam G Generation: Cross Drive" to "file:///android_asset/console_photos/non_libre/ds_lite_gundam_cross_drive.webp",
        "DS Lite Édition Love and Berry" to "file:///android_asset/console_photos/non_libre/ds_lite_love_and_berry.webp",
        "DS Lite Édition Momotaro Dentetsu" to "file:///android_asset/console_photos/non_libre/ds_lite_momotaro_dentetsu.webp",
        "DS Lite Édition Pokémon Daisuki Club Giratina (forme Origine)" to "file:///android_asset/console_photos/non_libre/ds_lite_pokemon_daisuki_club_giratina_origin.webp",
        "DS Lite Royal Gold" to "file:///android_asset/console_photos/non_libre/ds_lite_royal_gold.webp",
        "DS Lite Édition Winning Eleven DS" to "file:///android_asset/console_photos/non_libre/ds_lite_winning_eleven.webp",
        "DS Lite Édition It's a Wonderful World (Subarashiki Kono Sekai)" to "file:///android_asset/console_photos/non_libre/ds_lite_wonderful_world.webp",

        "Nintendo DSi Édition Gyakuten Kenji" to "file:///android_asset/console_photos/non_libre/dsi_gyakuten_kenji_ace_attorney.webp",
        "Nintendo DSi Édition SaGa 2: Hihō Densetsu (20e anniversaire)" to "file:///android_asset/console_photos/non_libre/dsi_saga_2.webp",
        "Nintendo DSi LL Super Mario Bros. 25e Anniversaire" to "file:///android_asset/console_photos/non_libre/dsi_ll_super_mario_25th_anniversary.webp",
        "Nintendo DSi LL Édition LovePlus+ Manaka Deluxe" to "file:///android_asset/console_photos/non_libre/dsi_ll_loveplus_blue.webp",
        "Nintendo DSi LL Édition LovePlus+ Rinko Deluxe" to "file:///android_asset/console_photos/non_libre/dsi_ll_loveplus_green.webp",
        "Nintendo DSi LL Édition LovePlus+ Nene Deluxe" to "file:///android_asset/console_photos/non_libre/dsi_ll_loveplus_pink.webp",
        "Nintendo DSi LL Édition Pompompurin 15e Anniversaire" to "file:///android_asset/console_photos/non_libre/dsi_ll_pompompurin_15th_anniversary.webp",

        "Nintendo 3DS Édition Fire Emblem: Awakening" to "file:///android_asset/console_photos/non_libre/3ds_fire_emblem_awakening.webp",
        "Nintendo 3DS Édition Kingdom Hearts 3D: Dream Drop Distance" to "file:///android_asset/console_photos/non_libre/3ds_kingdom_hearts_3d.webp",
        "Nintendo 3DS Édition Dragon Quest Monsters: Terry no Wonderland 3D" to "file:///android_asset/console_photos/non_libre/3ds_dragon_quest_monsters_terry.webp",
        "Nintendo 3DS Ice White Édition Super Mario 3D Land" to "file:///android_asset/console_photos/non_libre/3ds_ice_white_super_mario_3d_land.webp",
        "New Nintendo 2DS LL Édition Dragon Quest XI" to "file:///android_asset/console_photos/non_libre/new_2ds_ll_dragon_quest.webp",
        "New Nintendo 3DS LL Édition Monster Hunter X (Rouge)" to "file:///android_asset/console_photos/non_libre/new_3ds_ll_monster_hunter_x_red.webp",
        "Nintendo 3DS XL Édition Monster Hunter 4 Rajang (Or)" to "file:///android_asset/console_photos/non_libre/3ds_ll_monster_hunter_4_rajang_gold.webp",
        "Nintendo 3DS XL Édition One Piece (Rouge)" to "file:///android_asset/console_photos/non_libre/3ds_ll_one_piece_red.webp",
        "Nintendo 3DS XL Édition Shin Megami Tensei IV" to "file:///android_asset/console_photos/non_libre/3ds_ll_shin_megami_tensei_iv.webp",
        "Nintendo 3DS XL Édition Theatrhythm Final Fantasy: Curtain Call" to "file:///android_asset/console_photos/non_libre/3ds_ll_ff_theatrhythm_curtain_call.webp",
        "Nintendo 3DS XL Édition Dragon Quest Monsters 2" to "file:///android_asset/console_photos/non_libre/3ds_ll_dragon_quest_monsters_2.webp",
        "New Nintendo 3DS Édition Pokémon Alpha Saphir" to "file:///android_asset/console_photos/non_libre/new_3ds_pokemon_alpha_sapphire.webp",

        // --- PHASE 2 (recherche 2026-08-01/02) : consoles de salon Nintendo exclusives Japon.
        // Famicom Téléviseur C1 : vraie photo Wikimedia Commons (licence libre). Les autres
        // (Famicom/Super Famicom/N64/GameCube) sont des photos produit/collection non libres de
        // droits (consolevariations.com, archives presse) -> sous-dossier non_libre/.
        "Famicom Téléviseur C1" to "file:///android_asset/console_photos/famicom_tv_c1.webp",
        "AV Famicom" to "file:///android_asset/console_photos/av_famicom.webp",
        "Twin Famicom Rouge" to "file:///android_asset/console_photos/twin_famicom_rouge.webp",
        "Super Famicom Jr." to "file:///android_asset/console_photos/super_famicom_jr.webp",
        "Nintendo 64 Clear Blue" to "file:///android_asset/console_photos/nintendo_64_clear_blue.webp",
        "Nintendo 64 Clear Red" to "file:///android_asset/console_photos/nintendo_64_clear_red_white.webp",
        "Nintendo 64 Édition ANA (All Nippon Airways)" to "file:///android_asset/console_photos/non_libre/n64_ana_edition.webp",
        "Nintendo 64 Édition Fukuoka Daiei Hawks" to "file:///android_asset/console_photos/non_libre/n64_daiei_hawks.webp",
        "Nintendo 64 Jusco 30e Anniversaire" to "file:///android_asset/console_photos/non_libre/n64_jusco_30th_anniversary.webp",
        "Nintendo 64 Édition Lawson" to "file:///android_asset/console_photos/non_libre/n64_lawson_station.webp",
        "Nintendo 64 Édition Pikachu Orange" to "file:///android_asset/console_photos/non_libre/n64_pikachu_orange.webp",
        "GameCube Starlight Gold" to "file:///android_asset/console_photos/non_libre/gamecube_starlight_gold.webp",
        "GameCube Édition Final Fantasy Crystal Chronicles (Crystal White)" to "file:///android_asset/console_photos/non_libre/gamecube_crystal_white_ffcc.webp"
    )

    // Photo générique de la gamme Game & Watch (vraie photo Wikimedia vérifiée) : repli pour toute
    // fiche que l'utilisateur nommerait manuellement « Game & Watch ... » sans qu'elle corresponde
    // à un modèle précis de la liste (tous les modèles connus ont désormais leur propre photo).
    private const val GAME_AND_WATCH_FALLBACK =
        "file:///android_asset/console_photos/game_watch_fallback_boxing.webp"

    // --- Photo par modèle Game & Watch : une vraie photo Wikimedia Commons de CET exemplaire
    // précis (vérifiée fichier par fichier via l'API Commons), plutôt que la photo générique de
    // la gamme qui ne correspondait à aucun modèle affiché. Quand plusieurs séries partagent le
    // même jeu (ex. Popeye en Wide Screen/Table Top/Panorama) et qu'une photo par série existe
    // vraiment, elle est utilisée ; sinon la meilleure photo disponible du même jeu est réutilisée.
    private val gameAndWatchModelPhotos: Map<String, String> = mapOf(
        "Game & Watch Ball (Silver)" to "file:///android_asset/console_photos/game_watch_ball_silver.webp",
        "Game & Watch Flagman (Silver)" to "file:///android_asset/console_photos/game_watch_flagman_silver.webp",
        "Game & Watch Vermin (Silver)" to "file:///android_asset/console_photos/game_watch_vermin_silver.webp",
        "Game & Watch Fire (Silver)" to "file:///android_asset/console_photos/game_watch_fire_silver.webp",
        "Game & Watch Judge (Silver)" to "file:///android_asset/console_photos/game_watch_judge_silver.webp",
        "Game & Watch Manhole (Gold)" to "file:///android_asset/console_photos/game_watch_manhole_gold.webp",
        "Game & Watch Helmet (Gold)" to "file:///android_asset/console_photos/game_watch_helmet_gold.webp",
        "Game & Watch Lion (Gold)" to "file:///android_asset/console_photos/game_watch_lion_gold.webp",
        "Game & Watch Parachute (Wide Screen)" to "file:///android_asset/console_photos/game_watch_parachute_wide_screen.webp",
        "Game & Watch Octopus (Wide Screen)" to "file:///android_asset/console_photos/game_watch_octopus_wide_screen.webp",
        "Game & Watch Popeye (Wide Screen)" to "file:///android_asset/console_photos/game_watch_popeye_wide_screen.webp",
        "Game & Watch Chef (Wide Screen)" to "file:///android_asset/console_photos/game_watch_chef_wide_screen.webp",
        // Corrigé le 2026-07-12 : l'ancienne photo (réutilisée aussi pour Panorama ci-dessous)
        // porte en réalité la mention "PANORAMA SCREEN" sur le boîtier — remplacée par une vraie
        // photo labellisée "WIDE SCREEN".
        "Game & Watch Mickey Mouse (Wide Screen)" to "file:///android_asset/console_photos/game_watch_mickey_mouse_wide_screen.webp",
        "Game & Watch Egg (Wide Screen)" to "file:///android_asset/console_photos/game_watch_egg_wide_screen.webp",
        "Game & Watch Fire (Wide Screen)" to "file:///android_asset/console_photos/game_watch_fire_wide_screen.webp",
        "Game & Watch Turtle Bridge (Wide Screen)" to "file:///android_asset/console_photos/game_watch_turtle_bridge_wide_screen.webp",
        "Game & Watch Fire Attack (Wide Screen)" to "file:///android_asset/console_photos/game_watch_fire_attack_wide_screen.webp",
        "Game & Watch Snoopy Tennis (Wide Screen)" to "file:///android_asset/console_photos/game_watch_snoopy_tennis_wide_screen.webp",
        "Game & Watch Oil Panic (Multi Screen)" to "file:///android_asset/console_photos/game_watch_oil_panic_multi_screen.webp",
        "Game & Watch Donkey Kong (Multi Screen)" to "file:///android_asset/console_photos/game_watch_donkey_kong_multi_screen.webp",
        "Game & Watch Mickey and Donald (Multi Screen)" to "file:///android_asset/console_photos/game_watch_mickey_and_donald_multi_screen.webp",
        // Photo fournie par l'utilisateur (2026-07-13) : remplace l'ancienne photo (qui montrait
        // Green House ET Donkey Kong côte à côte) par une photo nette de ce seul modèle.
        "Game & Watch Green House (Multi Screen)" to "file:///android_asset/console_photos/gw_green_house.webp",
        "Game & Watch Donkey Kong II (Multi Screen)" to "file:///android_asset/console_photos/game_watch_donkey_kong_ii_multi_screen.webp",
        "Game & Watch Mario Bros. (Multi Screen)" to "file:///android_asset/console_photos/game_watch_mario_bros_multi_screen.webp",
        "Game & Watch Rain Shower (Multi Screen)" to "file:///android_asset/console_photos/game_watch_rain_shower_multi_screen.webp",
        "Game & Watch Pinball (Multi Screen)" to "file:///android_asset/console_photos/game_watch_pinball_multi_screen.webp",
        "Game & Watch Black Jack (Multi Screen)" to "file:///android_asset/console_photos/game_watch_black_jack_multi_screen.webp",
        "Game & Watch Squish (Multi Screen)" to "file:///android_asset/console_photos/game_watch_squish_multi_screen.webp",
        "Game & Watch Bomb Sweeper (Multi Screen)" to "file:///android_asset/console_photos/game_watch_bomb_sweeper_multi_screen.webp",
        "Game & Watch Safebuster (Multi Screen)" to "file:///android_asset/console_photos/game_watch_safebuster_multi_screen.webp",
        "Game & Watch Gold Cliff (Multi Screen)" to "file:///android_asset/console_photos/game_watch_gold_cliff_multi_screen.webp",
        "Game & Watch Zelda (Multi Screen)" to "file:///android_asset/console_photos/game_watch_zelda_multi_screen.webp",
        "Game & Watch Donkey Kong Jr. (Table Top)" to "file:///android_asset/console_photos/game_watch_donkey_kong_jr_table_top.webp",
        // Corrigé le 2026-07-12 : l'ancienne photo (générique) est en fait la version New Wide
        // Screen (voir plus bas) — remplacée par une photo explicitement labellisée "Tabletop".
        "Game & Watch Mario's Cement Factory (Table Top)" to "file:///android_asset/console_photos/game_watch_mario_s_cement_factory_table_top.webp",
        "Game & Watch Snoopy (Table Top)" to "file:///android_asset/console_photos/game_watch_snoopy_table_top.webp",
        "Game & Watch Popeye (Table Top)" to "file:///android_asset/console_photos/game_watch_popeye_table_top.webp",
        // Corrigé le 2026-07-12 : reprenait par erreur la photo Table Top (boîtier cabinet, très
        // différent) — remplacée par une vraie photo labellisée "Panorama".
        "Game & Watch Snoopy (Panorama)" to "file:///android_asset/console_photos/game_watch_snoopy_panorama.webp",
        "Game & Watch Popeye (Panorama)" to "file:///android_asset/console_photos/game_watch_popeye_panorama.webp",
        "Game & Watch Donkey Kong Jr. (Panorama)" to "file:///android_asset/console_photos/game_watch_donkey_kong_jr_panorama.webp",
        "Game & Watch Mickey Mouse (Panorama)" to "file:///android_asset/console_photos/game_watch_mickey_mouse_panorama.webp",
        "Game & Watch Donkey Kong Circus (Panorama)" to "file:///android_asset/console_photos/game_watch_donkey_kong_circus_panorama.webp",
        "Game & Watch Donkey Kong Jr. (New Wide Screen)" to "file:///android_asset/console_photos/game_watch_donkey_kong_jr_new_wide_screen.webp",
        "Game & Watch Mario's Cement Factory (New Wide Screen)" to "file:///android_asset/console_photos/game_watch_mario_s_cement_factory_new_wide_screen.webp",
        "Game & Watch Manhole (New Wide Screen)" to "file:///android_asset/console_photos/game_watch_manhole_new_wide_screen.webp",
        "Game & Watch Tropical Fish (New Wide Screen)" to "file:///android_asset/console_photos/game_watch_tropical_fish_new_wide_screen.webp",
        "Game & Watch Super Mario Bros. (New Wide Screen)" to "file:///android_asset/console_photos/game_watch_super_mario_bros_non_commercialise.webp",
        // Corrigé le 2026-07-12 : l'ancienne photo (réutilisée aussi pour Crystal Screen) montre
        // en fait le boîtier TRANSPARENT typique du Crystal Screen — remplacée par une vraie photo
        // du boîtier opaque New Wide Screen.
        "Game & Watch Climber (New Wide Screen)" to "file:///android_asset/console_photos/game_watch_climber_new_wide_screen.webp",
        "Game & Watch Balloon Fight (New Wide Screen)" to "file:///android_asset/console_photos/game_watch_balloon_fight_new_wide_screen.webp",
        "Game & Watch Mario the Juggler (New Wide Screen)" to "file:///android_asset/console_photos/game_watch_mario_the_juggler_new_wide_screen.webp",
        "Game & Watch Spitball Sparky (Super Color)" to "file:///android_asset/console_photos/game_watch_spitball_sparky_super_color.webp",
        "Game & Watch Crab Grab (Super Color)" to "file:///android_asset/console_photos/game_watch_crab_grab_super_color.webp",
        "Game & Watch Boxing / Punch-Out!! (Micro Vs. System)" to "file:///android_asset/console_photos/game_watch_boxing_punch_out_micro_vs_system.webp",
        "Game & Watch Donkey Kong 3 (Micro Vs. System)" to "file:///android_asset/console_photos/game_watch_donkey_kong_3_micro_vs_system.webp",
        "Game & Watch Donkey Kong Hockey (Micro Vs. System)" to "file:///android_asset/console_photos/game_watch_donkey_kong_hockey_micro_vs_system.webp",
        "Game & Watch Super Mario Bros. (Crystal Screen)" to "file:///android_asset/console_photos/game_watch_super_mario_bros_crystal_screen.webp",
        "Game & Watch Climber (Crystal Screen)" to "file:///android_asset/console_photos/game_watch_climber_crystal_screen.webp",
        "Game & Watch Balloon Fight (Crystal Screen)" to "file:///android_asset/console_photos/game_watch_balloon_fight_crystal_screen.webp",
        // Super Mario Bros. (non commercialisé, prototype YM-901 1987) : aucune photo propre sur
        // Commons (jamais vendu en boutique) — on reprend la photo du modèle New Wide Screen
        // (même jeu, même look monochrome), plus proche que le repli générique de la gamme.
        "Game & Watch Super Mario Bros. (non commercialisé)" to "file:///android_asset/console_photos/game_watch_super_mario_bros_non_commercialise.webp",
        // Photo (3) de cette même série montrait le DOS (vis + étiquette) : remplacée par la (4),
        // qui montre bien l'avant (écran + boutons), comme pour toutes les autres fiches.
        "Game & Watch Ball (réédition 2010)" to "file:///android_asset/console_photos/game_watch_ball_reedition_2010.webp",
        "Game & Watch: Super Mario Bros. (2020)" to "file:///android_asset/console_photos/game_watch_super_mario_bros_2020.webp",
        "Game & Watch: The Legend of Zelda (2021)" to "file:///android_asset/console_photos/game_watch_the_legend_of_zelda_2021.webp",
        // Lifeboat (Multi Screen) et Mario's Bombs Away (Panorama) : aucune photo libre trouvée
        // sur Commons malgré recherche exhaustive -> photos fournies par l'utilisateur (2026-07-13).
        "Game & Watch Lifeboat (Multi Screen)" to "file:///android_asset/console_photos/gw_lifeboat.webp",
        "Game & Watch Mario's Bombs Away (Panorama)" to "file:///android_asset/console_photos/gw_mario_bombs_away.webp",

        // --- PHASE 3 (recherche 2026-08-01/02) : Sony PlayStation/PS2/PS3/PS4/PS5/PSP/Vita
        // exclusifs Japon. Sources croisées : sonyinteractive.com (archives Wayback Machine pour
        // les visuels de presse SCEI d'origine, domaine scei.co.jp), Wikimedia Commons (Net Yaroze),
        // mediaworld.co.jp, catwithmonocle.com. Photos non libres de droits -> non_libre/. ---
        "PlayStation Net Yaroze" to "file:///android_asset/console_photos/net_yaroze.webp",
        "PlayStation 2 Ocean Blue" to "file:///android_asset/console_photos/non_libre/ps2_ocean_blue.webp",
        "PlayStation 2 Slim Ceramic White" to "file:///android_asset/console_photos/non_libre/ps2_slim_ceramic_white.webp",
        "PlayStation 2 Slim Satin Silver" to "file:///android_asset/console_photos/non_libre/ps2_slim_satin_silver.webp",
        "PlayStation 2 Slim Cinnabar Red" to "file:///android_asset/console_photos/non_libre/ps2_slim_cinnabar_red.webp",
        "PlayStation 3 Édition Final Fantasy XIII-2 (Lightning Ver.2)" to "file:///android_asset/console_photos/non_libre/ps3_ff13_2_lightning_v2.webp",
        "PlayStation 3 Édition One Piece Pirate Warriors (Gold Edition)" to "file:///android_asset/console_photos/non_libre/ps3_one_piece_gold_edition.webp",
        "PlayStation 3 Édition Ni no Kuni (Magical Edition)" to "file:///android_asset/console_photos/non_libre/ps3_ninokuni_magical_edition.webp",
        "PlayStation 3 Édition Tales of Xillia (X Edition)" to "file:///android_asset/console_photos/non_libre/ps3_tales_of_xillia_x_edition.webp",
        "PlayStation 3 Édition Gran Turismo 5 (Titanium Blue)" to "file:///android_asset/console_photos/non_libre/ps3_gt5_titanium_blue.webp",
        "PlayStation 4 Édition Dragon Quest (Metal Slime)" to "file:///android_asset/console_photos/non_libre/ps4_dq_metal_slime.webp",
        "PlayStation 4 Édition Dragon Quest XI (Loto)" to "file:///android_asset/console_photos/non_libre/ps4_dq11_loto_edition.webp",
        "PSP-2000 Rose Pink" to "file:///android_asset/console_photos/non_libre/psp2000_rose_pink.webp",
        "PSP-2000 Lavender Purple" to "file:///android_asset/console_photos/non_libre/psp2000_lavender_purple.webp",
        "PSP-2000 Felicia Blue" to "file:///android_asset/console_photos/non_libre/psp2000_felicia_blue.webp",
        "PSP-3000 Value Pack Black/Red" to "file:///android_asset/console_photos/non_libre/psp3000_value_pack_black_red.webp",
        "PSP-3000 Value Pack White/Blue" to "file:///android_asset/console_photos/non_libre/psp3000_value_pack_white_blue.webp",
        "PSP-3000 Bright Yellow" to "file:///android_asset/console_photos/non_libre/psp3000_bright_yellow.webp",
        "PSP-3000 Metallic Blue" to "file:///android_asset/console_photos/non_libre/psp3000_metallic_blue.webp",
        "PlayStation Vita Slim Light Pink" to "file:///android_asset/console_photos/non_libre/vita_slim_light_pink.webp",
        "PlayStation Vita Slim Blue/Black" to "file:///android_asset/console_photos/non_libre/vita_slim_blue_black.webp",
        "PlayStation Vita Slim Red/Black" to "file:///android_asset/console_photos/non_libre/vita_slim_red_black.webp",
        "PlayStation Vita Édition Soul Sacrifice" to "file:///android_asset/console_photos/non_libre/vita_soul_sacrifice.webp",

        // --- PHASE 4 (recherche 2026-08-01/02) : Sega et NEC PC Engine, déclinaisons exclusives
        // au Japon. Photos Wikimedia Commons (licence libre) utilisées en priorité ; à défaut,
        // photos produit consolevariations.com (non libres de droits) -> non_libre/.
        "Mark III" to "file:///android_asset/console_photos/sega_mark_iii.webp",
        "Mega Jet" to "file:///android_asset/console_photos/mega_jet.webp",
        "TeraDrive" to "file:///android_asset/console_photos/teradrive.webp",
        "Wondermega" to "file:///android_asset/console_photos/non_libre/wondermega.webp",
        "Wondermega 2" to "file:///android_asset/console_photos/non_libre/wondermega_2.webp",
        "Hi-Saturn" to "file:///android_asset/console_photos/non_libre/hi_saturn.webp",
        "V-Saturn" to "file:///android_asset/console_photos/non_libre/v_saturn.webp",
        "Saturn Skeleton (This is Cool)" to "file:///android_asset/console_photos/non_libre/saturn_skeleton_this_is_cool.webp",
        "Saturn Blanche (2e modèle)" to "file:///android_asset/console_photos/non_libre/saturn_blanche_2e_modele.webp",
        "Saturn Sonic (Toys'R'Us Japon)" to "file:///android_asset/console_photos/non_libre/saturn_sonic_toysrus.webp",
        "Saturn Édition Derby Stallion" to "file:///android_asset/console_photos/non_libre/saturn_derby_stallion.webp",
        "Dreamcast Édition Hello Kitty" to "file:///android_asset/console_photos/dreamcast_edition_hello_kitty.webp",
        "Dreamcast Édition Sakura Wars (Taisen)" to "file:///android_asset/console_photos/non_libre/dreamcast_edition_sakura_wars.webp",
        "Divers 2000 CX-1" to "file:///android_asset/console_photos/divers_2000_cx1.webp",
        "Game Gear Micro Noire" to "file:///android_asset/console_photos/non_libre/game_gear_micro_noire.webp",
        "Game Gear Micro Bleue" to "file:///android_asset/console_photos/non_libre/game_gear_micro_bleue.webp",
        "Game Gear Micro Jaune" to "file:///android_asset/console_photos/non_libre/game_gear_micro_jaune.webp",
        "Game Gear Micro Rouge" to "file:///android_asset/console_photos/non_libre/game_gear_micro_rouge.webp",
        "Game Gear Blanche" to "file:///android_asset/console_photos/non_libre/game_gear_blanche.webp",
        "PC Engine Shuttle" to "file:///android_asset/console_photos/pc_engine_shuttle.webp",
        "PC Engine Duo-R" to "file:///android_asset/console_photos/pc_engine_duo_r.webp"
    )

    fun urlFor(name: String): String? = gameAndWatchModelPhotos[name] ?: byName[name]
        // Fiche générique « Game & Watch » (sans modèle précis), ou modèle Game & Watch sans photo
        // d'unité disponible (Lifeboat, Mario's Bombs Away) ou pas encore ajouté à la map ci-dessus :
        // repli sur une vraie photo Game & Watch plutôt qu'une image sans rapport (ex. vignette 3D
        // GameCube posée par l'auto-remplissage) ou un lien mort.
        ?: GAME_AND_WATCH_FALLBACK.takeIf { name.startsWith("Game & Watch", ignoreCase = true) }
}
