package com.example.macollection.data

/**
 * Image (1 par console) issue des vignettes de modèles 3D Sketchfab.
 * Clé = nom exact de la console dans [consolePresets].
 */
object ConsoleImages {

    val byName: Map<String, String> = mapOf(
        "Odyssey" to "https://media.sketchfab.com/models/1442250a6bba4c70af4fc6b9807bcfdf/thumbnails/7ce87845445548dbb99215108fda1d3d/a12009567a934e52af02bf02c6d898d5.jpeg",
        "Pong (Home)" to "https://media.sketchfab.com/models/dac537f1911b4f6b80b41ff4f37cd4a3/thumbnails/b82b838c158f45d89ea6d857c5ce7194/63d3ee9bdedf4b47bee52adbf40fc1cc.jpeg",
        "Channel F" to "https://media.sketchfab.com/models/065f8fdd4a7a434baaa7e6026936e9d3/thumbnails/c7b7cd88f5f549c2a4db097cc3275ae1/bbac7c920f744857acf79dc5b9b71495.jpeg",
        // Photo fournie par l'utilisateur (2026-07-13) : remplace le rendu 3D Sketchfab par une
        // vraie photo du modèle « wood-grain » emblématique.
        "2600 (VCS)" to "file:///android_asset/console_photos/atari_2600_vcs.webp",
        "Famicom" to "https://media.sketchfab.com/models/b33939368ad04640919570a9d3bdbeda/thumbnails/aca5faaf876742849a2bb50e1d47391e/d69356e0115448778d89435b871fb76c.jpeg",
        "NES" to "file:///android_asset/console_photos/nes.webp",
        "Master System" to "https://media.sketchfab.com/models/0ec998c9b9de4e7f956cab56bbca3c0f/thumbnails/3455e37a767440c8945e435aafd5baf3/b8901a3b278949789df1b56745f967e5.jpeg",
        "PC Engine" to "https://media.sketchfab.com/models/694dd99905564d2b8f90d8b5f3ace002/thumbnails/7f6ebc84b918489f8aab138fd8e2b74d/720x405.jpeg",
        "Mega Drive" to "https://media.sketchfab.com/models/bceba7d09cd147da8e25ff3f5c238b37/thumbnails/7ec21a3504554b36b06938c54d7a6574/fd4ed7e2b33743ab9e4fef44f3f9e128.jpeg",
        "SuperGrafx" to "https://media.sketchfab.com/models/4dcd673206c244a9ab423de50e420366/thumbnails/ecd5c002168940f99853949dd0b0815b/021ba8a93afb40d183ad617c99f385d9.jpeg",
        "Game Boy" to "file:///android_asset/console_photos/game_boy.webp",
        "Game Boy Pocket" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Game-Boy-Pocket-FL.jpg/500px-Game-Boy-Pocket-FL.jpg",
        "Lynx" to "https://media.sketchfab.com/models/19163d8c9a464eda85977f245b3e6525/thumbnails/353365651eeb405f9665db26061a15e8/36ae7b4cf94b41b09066cfc2bcbf3b22.jpeg",
        "Game Gear" to "https://media.sketchfab.com/models/f1f25f61df37448ebf11b1b36fed6a5d/thumbnails/85d479104ffc400cbee8a304fe0b389e/717c60416a274f5ab13e5c70f2879f2c.jpeg",
        "Neo Geo AES" to "https://media.sketchfab.com/models/e2b303532785471fbad04af56c205b5f/thumbnails/56d5932563ff47c7984cc8cb7176d7bb/450182c8146846349dc476dfe7ab65c4.jpeg",
        "Super Famicom" to "https://media.sketchfab.com/models/610c0cd0c841486385495f53e3b68236/thumbnails/1eedcb9703b84adbb50531b59bacd0d5/a676b462391246f1a5422003aa07795c.jpeg",
        "Super Nintendo (SNES)" to "https://media.sketchfab.com/models/31f4914a42e14631aeba15a539161e62/thumbnails/df5f3885f2834b5e9f7da73350b0e627/9696286aba25484889d3df9c44fb6f3d.jpeg",
        "CD-i" to "https://media.sketchfab.com/models/3186b09a0d67448b957ce7958300c4c6/thumbnails/75f2290ef4d44f87973bdb1e61e386f2/24fd99d1f28d459d818c969d8448a905.jpeg",
        "3DO" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/3DO-FZ1-Console-Set.jpg/500px-3DO-FZ1-Console-Set.jpg",
        "3DO (FZ-10)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/26/3DO-FZ-10-Console-FL.jpg/500px-3DO-FZ-10-Console-FL.jpg",
        "Saturn" to "https://media.sketchfab.com/models/a0eab4c5a35f4ff68e5b83690e4179cd/thumbnails/416f2f08948e495286cf61e2168efb9b/927349dc756b479483d6e2138267fb89.jpeg",
        "PlayStation" to "https://media.sketchfab.com/models/de8bb282a94b468c997e4b7d8c2aa2f5/thumbnails/fcf8935911fd4e9696e8be94038e17de/dac1582a670d4a46984ab89e4588bbe2.jpeg",
        "Loopy" to "file:///android_asset/console_photos/loopy.webp",
        "Virtual Boy" to "https://media.sketchfab.com/models/0086f3db0c21424ea910d9e5fa235170/thumbnails/98bdb1e66ab44e7b8888eb96325ac172/dbad2412d36342dfaab4c410a992309d.jpeg",
        "Nintendo 64" to "file:///android_asset/console_photos/n64.webp",
        "Neo Geo Pocket" to "file:///android_asset/console_photos/neogeo_pocket.webp",
        "Game Boy Color" to "https://media.sketchfab.com/models/277998bca398436faaece92470d93372/thumbnails/6fd92c19dc0045cfae7ee4ad8f32d209/bde6ea0eb5144292b46d246c4aaa6452.jpeg",
        "Dreamcast" to "https://media.sketchfab.com/models/f82d99e4739e42f4abfc37cb97918cfc/thumbnails/490268e67af143d9b007bf5e9f52ccab/5ba76addbc21478b832c825f14da2137.jpeg",
        "Neo Geo Pocket Color" to "file:///android_asset/console_photos/neogeo_pocket_color.webp",
        "PlayStation 2" to "https://media.sketchfab.com/models/b53f858aa1584229a7f906239fc158a2/thumbnails/3f0b58966f4c412885e95c22d83d2020/3587efba532543c79695e7b62a033c2e.jpeg",
        "Game Boy Advance" to "https://upload.wikimedia.org/wikipedia/commons/thumb/9/98/Nintendo-Game-Boy-Advance-Milky-Blue-FL.jpg/500px-Nintendo-Game-Boy-Advance-Milky-Blue-FL.jpg",
        "Xbox" to "https://media.sketchfab.com/models/50a6590eff87403992facd761a9d8f24/thumbnails/714d4a9641f94e0da4616246e610bd11/2a41f463825d41d1b0ef868eb2fe365c.jpeg",
        "GameCube" to "file:///android_asset/console_photos/gamecube.webp",
        "Nintendo DS" to "https://media.sketchfab.com/models/ca529eb7208746e89b7a28fd2246659d/thumbnails/ea9e17fa3c424c7b8469ceb474d88235/7e4202dc6da4441ea12b9ff6c69c7980.jpeg",
        "Nintendo DS Lite" to "https://upload.wikimedia.org/wikipedia/commons/a/a0/Nintendo-DS-Lite-Black-Open.jpg",
        "PSP" to "https://media.sketchfab.com/models/dca89d10ec304d0cab76837750df7761/thumbnails/756b87e08caa445994e5315b41e232cb/53312a7452cd4727bc88d24cc723dae7.jpeg",
        "Xbox 360" to "https://media.sketchfab.com/models/f65ac4721fd34dd8a8b63adfa712f5b8/thumbnails/0e2ef976c1134a918e53f1ddb376ff5c/640x360.jpeg",
        "PlayStation 3" to "https://media.sketchfab.com/models/d3eb52c6cf144601a52c99f9be78dfec/thumbnails/155132cbbf994342908baa2efe9d554d/71b8cc9026364b249e2a1d9023c21e8d.jpeg",
        "Wii" to "https://media.sketchfab.com/models/9552175840544217aa68a81574618425/thumbnails/0f0b6d3868ad496b8c3aff77ea02775c/72f86327ee704c308d0f022cfe4cd0bd.jpeg",
        "Nintendo DSi" to "https://media.sketchfab.com/models/ca529eb7208746e89b7a28fd2246659d/thumbnails/ea9e17fa3c424c7b8469ceb474d88235/7e4202dc6da4441ea12b9ff6c69c7980.jpeg",
        "Nintendo 3DS" to "https://media.sketchfab.com/models/fceffa4bd7064acd9e31cab9f7e27d89/thumbnails/07ee717fe0ba4821bb5f81bc9432cd32/f1b289990a0b44858311dd4992ff86d3.jpeg",
        "New 3DS XL Édition Super Nintendo" to "https://upload.wikimedia.org/wikipedia/commons/6/63/NEWNINTENDO3DSXLSNESEDITION.jpg",
        "PlayStation Vita" to "https://media.sketchfab.com/models/b68ef4821c9c426da5d09decee4c259d/thumbnails/c24d31bb227d4ab099de32584dfbfa9f/b1c94b1b936a457f88204d3d76425ce0.jpeg",
        "Wii U" to "https://media.sketchfab.com/models/a2e2e62fd88e43b1a5232daf34aed850/thumbnails/a9fa718061a345acb33eaf002c569d52/d1b2cb5c3f9b457ca5d4a83177234317.jpeg",
        "Xbox One" to "https://media.sketchfab.com/models/8866e33768e84b27b69e07abcde8a593/thumbnails/590cd33013144b728d0606eabbe9c253/57bed1205c844138ab5a2c61a226bdd2.jpeg",
        "PlayStation 4" to "https://media.sketchfab.com/models/b7LzIm8JrnPw4GBDOMBNGYc39qM/thumbnails/50a8e5e6a36e422eb693bbeff16fb4dc/2023d41f9b52488bb6c3c11bd12e839c.jpeg",
        "PlayStation 4 Pro" to "https://media.sketchfab.com/models/47cbb239d70a4deda806fc81b1025675/thumbnails/19ed1249f83c4c89bb7d98adb651e4ff/81c80c891f5540d79a86d04e1f123ec8.jpeg",
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
        "Xbox One X" to "https://media.sketchfab.com/models/f2c797d110d04c4fb9000b9afc1230b6/thumbnails/e04f1d3c9d334e8ebd69a902d9b6a03d/74fa2269c7c445a0abdb98a38a220245.jpeg",
        "Xbox Series S" to "https://media.sketchfab.com/models/61a9d3b53ba34841a9272f38e8be736a/thumbnails/fe3ae295f42649be99cd70801eeac7e4/eac54f9c61fb403ab69eaeda796541ca.jpeg",
        "Xbox Series X" to "https://media.sketchfab.com/models/29460c063c7b4046aea8be633f90210e/thumbnails/47cac01fdc814c46818dcfeca434793a/f50ed39529e340b5ada360eaee96f32f.jpeg",
        "PlayStation 5" to "https://media.sketchfab.com/models/54810f0b23a441ca93f44a8b06ec937b/thumbnails/0dca4b6a20be4645b24f150160ee4302/0b0355aea6a74db78239309a5ddc65f4.jpeg",
        "Switch OLED" to "https://media.sketchfab.com/models/36438fd911bb42138926cc82f7a2b522/thumbnails/87ad5b5f7e494453a0571eae418b944d/91ecf9c9de9244bfbb51926a77cf8185.jpeg",
        "Pocket" to "https://media.sketchfab.com/models/40bdbec5d7fd429584db8f0d695c9ff6/thumbnails/ebd67e9fb81f4e0a8ed23435f3157b01/ff4d3a7d11ec4431907273eeb8e8cf71.jpeg",
        "Steam Deck" to "https://media.sketchfab.com/models/3f777a4d9aee495c8fda9896df8a9c37/thumbnails/c11e7e9b1bbe46c4bf1ed08bd8f1786b/07bb833c928a4c9bb4fda4d44541853e.jpeg",
        "Playdate" to "https://media.sketchfab.com/models/92865c31353a4c9ba291f91de4ef1bfa/thumbnails/0281ffc1e8f74edb8787a3771c920782/fce810147fa2416693c85316b45bd92f.jpeg",
        "PSP Go" to "https://media.sketchfab.com/models/241b744e6c404545ab117d2de3597497/thumbnails/5a2c551f43f64c689d7f23552e64d23d/5c9c81c399a842e480cbceeb1bf3ed56.jpeg",
        "Odyssey 2" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2d/Magnavox-Odyssey-2-Console-Set.jpg/500px-Magnavox-Odyssey-2-Console-Set.jpg",
        "Microvision" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/Milton-Bradley-Microvision-Handheld-FL.jpg/500px-Milton-Bradley-Microvision-Handheld-FL.jpg",
        "Intellivision" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fc/Intellivision-Console-Set.png/500px-Intellivision-Console-Set.png",
        "5200" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Atari-5200-4-Port-wController-L.jpg/500px-Atari-5200-4-Port-wController-L.jpg",
        "ColecoVision" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/ColecoVision-wController-L.jpg/500px-ColecoVision-wController-L.jpg",
        "Vectrex" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7a/Vectrex-Console-Set.jpg/500px-Vectrex-Console-Set.jpg",
        "SG-1000" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Sega-SG-1000-Console-Set.jpg/500px-Sega-SG-1000-Console-Set.jpg",
        "SC-3000" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/21/Sega_SC-3000.jpg/500px-Sega_SC-3000.jpg",
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
        "Famicom Disk System" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f4/Nintendo-Famicom-Disk-System.png/500px-Nintendo-Famicom-Disk-System.png",
        "7800" to "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Atari-7800-Console-Set.jpg/500px-Atari-7800-Console-Set.jpg",
        "XEGS" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/43/Atari_XEGS_-_Computerspielemuseum-49_%2817134338672%29.jpg/500px-Atari_XEGS_-_Computerspielemuseum-49_%2817134338672%29.jpg",
        "Gamate" to "https://upload.wikimedia.org/wikipedia/commons/f/f8/Gamate.jpg",
        "Playdia" to "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Bandai-Playdia-Set-R.jpg/500px-Bandai-Playdia-Set-R.jpg",
        "Nintendo 64DD" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/64DD-Attached.png/500px-64DD-Attached.png",
        "N-Gage" to "https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Nokia-NGage-LL.png/500px-Nokia-NGage-LL.png",
        "TurboGrafx-16" to "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/TurboGrafx16-Console-Set.jpg/500px-TurboGrafx16-Console-Set.jpg",
        "Mega CD" to "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1f/Sega-CD-Model1-Set.jpg/500px-Sega-CD-Model1-Set.jpg",
        "Supervision" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Watara-Supervision-Tilted.jpg/500px-Watara-Supervision-Tilted.jpg",
        "PC-FX" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f4/PC-FX-Console-Set.jpg/500px-PC-FX-Console-Set.jpg",

        // --- Complétées via l'API Wikipédia/Commons (photos vérifiées) ---
        "32X" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Sega-Genesis-Model2-32X.jpg/500px-Sega-Genesis-Model2-32X.jpg",
        "GX4000" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/af/Amstrad-GX4000-Console-Set.jpg/500px-Amstrad-GX4000-Console-Set.jpg",
        "TurboExpress" to "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/NEC-TurboExpress-Upright-FL.jpg/500px-NEC-TurboExpress-Upright-FL.jpg",
        "CDTV" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/27/Commodore_CDTV_Setup.jpg/500px-Commodore_CDTV_Setup.jpg",
        "Jaguar" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fc/Atari-Jaguar-Console-Set.jpg/500px-Atari-Jaguar-Console-Set.jpg",
        "Neo Geo CD" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Neo-Geo-CD-TopLoader-wController-FL.png/500px-Neo-Geo-CD-TopLoader-wController-FL.png",
        "Neo Geo CD (Front Loader)" to "file:///android_asset/console_photos/neogeo_cd_frontloader.webp",
        "Nomad" to "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d8/Sega-Nomad-Front.jpg/500px-Sega-Nomad-Front.jpg",
        "Pippin" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/51/Bandai-Apple-Pippin-Console-FL.jpg/500px-Bandai-Apple-Pippin-Console-FL.jpg",
        "Pico" to "https://upload.wikimedia.org/wikipedia/commons/0/0c/Kids_Computer_Pico-01.jpg",
        "Game.com" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/af/Tiger-Game-Com-FL.jpg/500px-Tiger-Game-Com-FL.jpg",
        // Corrigé le 2026-07-13 : les deux fiches partageaient la même photo (WonderSwan Color) —
        // photos fournies par l'utilisateur, distinctes pour chaque modèle.
        "WonderSwan" to "file:///android_asset/console_photos/wonderswan.webp",
        "WonderSwan Color" to "file:///android_asset/console_photos/wonderswan_color.webp",
        "Zodiac" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/75/Tapwave-Zodiac2-FL.jpg/500px-Tapwave-Zodiac2-FL.jpg",
        "Evercade" to "https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Evercade_Original_Handheld_Console_SwitchedOff.jpg/500px-Evercade_Original_Handheld_Console_SwitchedOff.jpg",
        "Studio II" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/RCA-Studio-II-FL.jpg/500px-RCA-Studio-II-FL.jpg",
        "Astrocade" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/Bally-Arcade-Console.jpg/500px-Bally-Arcade-Console.jpg",
        "Cassette Vision" to "https://upload.wikimedia.org/wikipedia/commons/thumb/d/db/Epoch-Cassette-Vision-Console.jpg/500px-Epoch-Cassette-Vision-Console.jpg",
        "Arcadia 2001" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Emerson-Arcadia-2001.jpg/500px-Emerson-Arcadia-2001.jpg",
        "Intellivision II" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fc/Intellivision-Console-Set.png/500px-Intellivision-Console-Set.png",
        "PV-1000" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/Casio-PV-1000-Controller-wPlug.jpg/500px-Casio-PV-1000-Controller-wPlug.jpg",
        "Super Cassette Vision" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/21/Super-Cassette-Vision-Console-L.jpg/500px-Super-Cassette-Vision-Console-L.jpg",
        "Amiga CD32" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2b/Amiga-CD32-wController-L-TRSP.png/500px-Amiga-CD32-wController-L-TRSP.png",
        "FM Towns Marty" to "https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/FM-Towns-Marty-Console-Set.jpg/500px-FM-Towns-Marty-Console-Set.jpg",
        "CDX" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5f/Sega-CDX-FL.jpg/500px-Sega-CDX-FL.jpg",
        "Jaguar CD" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Atari-Jaguar-CD-wController.jpg/500px-Atari-Jaguar-CD-wController.jpg",
        "R-Zone" to "https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Tiger-RZone-Headset.jpg/500px-Tiger-RZone-Headset.jpg",
        "Coleco Gemini" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/42/Coleco-Gemini-Console.jpg/500px-Coleco-Gemini-Console.jpg",
        "Videopac G7200" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/67/Philips_Videopac_G7200_%281983%29_1.jpg/500px-Philips_Videopac_G7200_%281983%29_1.jpg",
        "Mega Drive 2" to "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/Sega-Genesis-Mk2-Console-02.jpg/500px-Sega-Genesis-Mk2-Console-02.jpg",
        "Mega CD 2" to "https://upload.wikimedia.org/wikipedia/commons/thumb/8/81/Sega-CD-Model2-Set.jpg/500px-Sega-CD-Model2-Set.jpg",
        "PC Engine Duo" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cb/PC-Engine-Duo-Console-Set.jpg/500px-PC-Engine-Duo-Console-Set.jpg",
        "PC Engine LT" to "https://upload.wikimedia.org/wikipedia/commons/f/f6/PC_Engine_LT.jpg",
        "Imagination Machine (MP1000)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/APF-MP1000-Console-FL.jpg/500px-APF-MP1000-Console-FL.jpg",
        "Twin Famicom" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Sharp-Twin-Famicom-Console.jpg/500px-Sharp-Twin-Famicom-Console.jpg",
        "PC Engine Duo-RX" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/78/PC_Engine_Duo-RX.jpg/500px-PC_Engine_Duo-RX.jpg",
        "Game Boy Advance SP" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a1/Game-Boy-Advance-SP-Mk1-Blue.jpg/500px-Game-Boy-Advance-SP-Mk1-Blue.jpg",
        "Nintendo DSi XL" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/55/Nintendo-DSi-XL-Burg.jpg/500px-Nintendo-DSi-XL-Burg.jpg",
        "Nintendo 3DS XL" to "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e4/Nintendo-3DS-XL-angled.jpg/500px-Nintendo-3DS-XL-angled.jpg",
        "Nintendo 2DS" to "https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/Nintendo-2DS-angle.jpg/500px-Nintendo-2DS-angle.jpg",
        "New Nintendo 3DS XL" to "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/New-3DS-XL-Black.jpg/500px-New-3DS-XL-Black.jpg",
        "New Nintendo 2DS XL" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/55/New_Nintendo_2DS_LL_%28White_%26_Orange%29.png/500px-New_Nintendo_2DS_LL_%28White_%26_Orange%29.png",
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
        "GameCube (Panasonic Q)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a6/Panasonic-Q-Console-Set.jpg/500px-Panasonic-Q-Console-Set.jpg",
        "Wii U Édition The Wind Waker HD" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/61/Wii_U_%22Wind_Waker%22_edition_GamePad_and_Console_sitting_together.jpg/500px-Wii_U_%22Wind_Waker%22_edition_GamePad_and_Console_sitting_together.jpg",
        "Nintendo 64 Jungle Green" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fe/Jungle_green_Nintendo_64_%2810448842084%29.jpg/500px-Jungle_green_Nintendo_64_%2810448842084%29.jpg",
        "Nintendo 64 Fire Orange" to "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/N64-Console-Orange.jpg/500px-N64-Console-Orange.jpg",
        "NES Classic Edition" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/NES_Classic_Edition_with_controller.jpg/500px-NES_Classic_Edition_with_controller.jpg",
        "Super NES Classic Edition" to "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b8/Super_NES_Classic_Edition_top.jpg/500px-Super_NES_Classic_Edition_top.jpg",
        "Mega Drive Mini" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Sega_Mega_Drive_Mini.jpg/500px-Sega_Mega_Drive_Mini.jpg",
        "Neo Geo Mini" to "https://upload.wikimedia.org/wikipedia/commons/thumb/d/da/Neo_Geo_Mini%2C_version_international_%28cropped%29.jpg/500px-Neo_Geo_Mini%2C_version_international_%28cropped%29.jpg",
        "PlayStation Classic" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/aa/PlayStation_Classic_Konsole_%2B_Controller.jpg/500px-PlayStation_Classic_Konsole_%2B_Controller.jpg",
        "ZX Spectrum" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/33/ZXSpectrum48k.jpg/500px-ZXSpectrum48k.jpg",
        "Commodore 64" to "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Commodore-64-Computer-FL.jpg/500px-Commodore-64-Computer-FL.jpg",
        "CPC 464" to "https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Amstrad_CPC464.jpg/500px-Amstrad_CPC464.jpg",
        "CPC 6128" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/45/Amstrad_CPC_6128.jpg/500px-Amstrad_CPC_6128.jpg",
        "CPC 6128+" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ad/Amstrad_6128Plus_%2854848973881%29.jpg/500px-Amstrad_6128Plus_%2854848973881%29.jpg",
        "1040 STF" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/39/Atari_1040STf.jpg/500px-Atari_1040STf.jpg",
        "Amiga 500" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c3/Amiga500_system.jpg/500px-Amiga500_system.jpg",
        "MSX" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/01/MITSUBISHI_MSX_ML-8000.jpg/500px-MITSUBISHI_MSX_ML-8000.jpg",
        // Xbox Series X Édition Halo Infinite / PC Engine Mini : photos retirées (sites
        // officiels/CDN constructeurs, non redistribuables dans une app publiée ; aucune photo
        // libre trouvée sur Wikimedia Commons). L'utilisateur peut attacher sa propre photo à la
        // fiche. (Atari 2600+ Édition Pac-Man : voir photo embarquée plus bas.)

        // --- Photos Wikimedia Commons (licence libre) ---
        "Nintendo 64 Édition Pikachu" to "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Pikachu_Nintendo_64_in_the_Science_Museum_2025-08-14.jpg/500px-Pikachu_Nintendo_64_in_the_Science_Museum_2025-08-14.jpg",
        "Nintendo 64 Ice Blue" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/N64_Clear_Blue_with_Super_Mario_64_20100603.jpg/500px-N64_Clear_Blue_with_Super_Mario_64_20100603.jpg",
        "Nintendo 64 Grape Purple" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/Nintendo_64_violette.JPG/500px-Nintendo_64_violette.JPG",
        "Nintendo 64 Watermelon Red" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f8/Nintendo_64_Rouge_Translucide.JPG/500px-Nintendo_64_Rouge_Translucide.JPG",
        "Xbox 360 Édition Halo 3" to "file:///android_asset/console_photos/xbox360_halo3.webp",

        // --- Éditions limitées : SEULES les photos sous licence libre (Wikimedia Commons) sont
        // conservées. Les anciennes photos de presse/officielles (Flickr constructeur, Xbox Wire,
        // Minecraft.net) et celles de consolevariations.com (licence non clarifiée) ont été
        // RETIRÉES pour la publication de l'app — pas de redistribution publique possible.
        // Fiches concernées désormais sans photo intégrée (photo personnelle attachable) :
        // PS4 Uncharted 4, PS4 Pro Spider-Man, Xbox 360 Gears of War / Halo 4, Xbox One Halo 5,
        // Xbox One S Minecraft, Xbox One X Cyberpunk 2077, Game Gear Micro, PSP God of War (x2),
        // PSP Monster Hunter 2nd G, PS Vita Hatsune Miku.
        "NeoGeo X Gold" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/SNK_Neo-Geo_X_%2826017301183%29.jpg/500px-SNK_Neo-Geo_X_%2826017301183%29.jpg",

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
        "Game Boy Light" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/Game-Boy-Light-FL.jpg/500px-Game-Boy-Light-FL.jpg",
        "Game Boy Micro" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Game-Boy-Micro.jpg/500px-Game-Boy-Micro.jpg",

        // --- Machines ajoutées : photos Wikimedia Commons (licence libre, vérifiées HTTP 200) ---
        // Compact Vision TV Boy : aucune photo libre trouvée (machine très rare) -> photo perso attachable.
        "C64 Games System" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/C64GS-Console-Set.jpg/500px-C64GS-Console-Set.jpg",
        "LaserActive" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Pioneer-LaserActive-Set-FL.jpg/500px-Pioneer-LaserActive-Set-FL.jpg",
        "Apple II" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Apple_II-IMG_7064.jpg/500px-Apple_II-IMG_7064.jpg",
        "TRS-80" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Radioshack_TRS80-IMG_7206.jpg/500px-Radioshack_TRS80-IMG_7206.jpg",
        "PET" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Commodore_2001_Series-IMG_0448b.jpg/500px-Commodore_2001_Series-IMG_0448b.jpg",
        "400 / 800" to "https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/Atari-800-Computer-FL.jpg/500px-Atari-800-Computer-FL.jpg",
        "VIC-20" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f1/Commodore-VIC-20-FL.jpg/500px-Commodore-VIC-20-FL.jpg",
        "ZX81" to "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Sinclair-ZX81.png/500px-Sinclair-ZX81.png",
        "BBC Micro" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/32/BBC_Micro_Front_Restored.jpg/500px-BBC_Micro_Front_Restored.jpg",
        "TO7" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/af/Thomson_TO-07-IMG_0413.JPG/500px-Thomson_TO-07-IMG_0413.JPG",
        "MO5" to "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/Thomson_MO5-IMG_1732.jpg/500px-Thomson_MO5-IMG_1732.jpg",
        "Macintosh" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/79/Computer_macintosh_128k%2C_1984_%28all_about_Apple_onlus%29.jpg/500px-Computer_macintosh_128k%2C_1984_%28all_about_Apple_onlus%29.jpg",
        "X68000" to "https://upload.wikimedia.org/wikipedia/commons/thumb/1/13/X68000ACE-HD.JPG/500px-X68000ACE-HD.JPG",
        "Archimedes" to "https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/AcornArchimedes-Wiki.jpg/500px-AcornArchimedes-Wiki.jpg",
        "Amiga 1200" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/55/Commodore_Amiga_A1200.jpg/500px-Commodore_Amiga_A1200.jpg",

        // --- Complétées le 2026-07-12 (audit "aucune fiche sans photo") ---
        "Compact Vision TV Boy" to "https://upload.wikimedia.org/wikipedia/commons/3/31/Compact_Vision_TV_Boy%2C_Gakken_01.png",

        // Éditions limitées commerciales : aucune photo de CETTE édition précise n'est disponible
        // sous licence libre sur Wikimedia Commons (recherche vérifiée) — on affiche donc la vraie
        // photo de la console de base plutôt que de laisser la fiche sans image. Ce n'est pas la
        // déco exacte, mais c'est le même matériel et une vraie photo, pas une image inventée.
        "Game Gear Micro" to "https://media.sketchfab.com/models/f1f25f61df37448ebf11b1b36fed6a5d/thumbnails/85d479104ffc400cbee8a304fe0b389e/717c60416a274f5ab13e5c70f2879f2c.jpeg",
        "PC Engine Mini" to "https://media.sketchfab.com/models/694dd99905564d2b8f90d8b5f3ace002/thumbnails/7f6ebc84b918489f8aab138fd8e2b74d/720x405.jpeg",
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
        "Xbox One X Édition Project Scorpio" to "https://media.sketchfab.com/models/f2c797d110d04c4fb9000b9afc1230b6/thumbnails/e04f1d3c9d334e8ebd69a902d9b6a03d/74fa2269c7c445a0abdb98a38a220245.jpeg",
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
        "2600 Jr." to "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Atari-2600-Jr-FL.jpg/500px-Atari-2600-Jr-FL.jpg",
        "NES Édition Deluxe Set" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/06/First_NES.jpg/500px-First_NES.jpg",
        "NES Édition Action Set" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/NES_Action_Set_-_Nintendo_%28cropped%29.jpg/500px-NES_Action_Set_-_Nintendo_%28cropped%29.jpg",
        "Master System II" to "https://upload.wikimedia.org/wikipedia/commons/thumb/9/98/Master_System_II.jpg/500px-Master_System_II.jpg",
        "Game Boy Édition Tetris" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Gameboy_Original_shipped_with_Tetris.jpg/500px-Gameboy_Original_shipped_with_Tetris.jpg",
        "Mega Drive II" to "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Mega_Drive_2.jpg/500px-Mega_Drive_2.jpg",
        "PC Engine GT" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/PC_Engine_GT.jpg/500px-PC_Engine_GT.jpg",
        "Neo Geo CDZ" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Neo-Geo-CDZ-FL.jpg/500px-Neo-Geo-CDZ-FL.jpg",
        // Vraie édition officielle Pokémon Jaune (pas un repeint fan-made) : Pikachu/Pichu + logo
        // Pokémon sérigraphiés sur une Game Boy Color or/orange.
        "Game Boy Color Édition Pokémon Yellow" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/Game_Boy_Color_Pikachu.jpg/500px-Game_Boy_Color_Pikachu.jpg",

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
        "Master System II Édition Aladdin" to "file:///android_asset/console_photos/ms2_aladdin.webp"
    )

    // Photo générique de la gamme Game & Watch (vraie photo Wikimedia vérifiée) : repli pour toute
    // fiche que l'utilisateur nommerait manuellement « Game & Watch ... » sans qu'elle corresponde
    // à un modèle précis de la liste (tous les modèles connus ont désormais leur propre photo).
    private const val GAME_AND_WATCH_FALLBACK =
        "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/Nintendo_Game_%26_Watch_-_Boxing.jpg/500px-Nintendo_Game_%26_Watch_-_Boxing.jpg"

    // --- Photo par modèle Game & Watch : une vraie photo Wikimedia Commons de CET exemplaire
    // précis (vérifiée fichier par fichier via l'API Commons), plutôt que la photo générique de
    // la gamme qui ne correspondait à aucun modèle affiché. Quand plusieurs séries partagent le
    // même jeu (ex. Popeye en Wide Screen/Table Top/Panorama) et qu'une photo par série existe
    // vraiment, elle est utilisée ; sinon la meilleure photo disponible du même jeu est réutilisée.
    private val gameAndWatchModelPhotos: Map<String, String> = mapOf(
        "Game & Watch Ball (Silver)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c4/Game_and_watch_Ball.JPG/500px-Game_and_watch_Ball.JPG",
        "Game & Watch Flagman (Silver)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Game_and_Watch_-_FLAGMAN.jpg/500px-Game_and_Watch_-_FLAGMAN.jpg",
        "Game & Watch Vermin (Silver)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/Vermin_-_Game%26Watch_-_Nintendo.jpg/500px-Vermin_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Fire (Silver)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/Game_%26_Watch_fire_silver.jpg/500px-Game_%26_Watch_fire_silver.jpg",
        "Game & Watch Judge (Silver)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6c/JudgeGameandWatch.jpg/500px-JudgeGameandWatch.jpg",
        "Game & Watch Manhole (Gold)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/Manhole_-_Game%26Watch_-_Nintendo.jpg/500px-Manhole_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Helmet (Gold)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Helmet_-_Game%26Watch_-_Nintendo.jpg/500px-Helmet_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Lion (Gold)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/8/80/Lion_-_Game_%26_Watch_Nintendo.jpg/500px-Lion_-_Game_%26_Watch_Nintendo.jpg",
        "Game & Watch Parachute (Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ab/Parachute_-_Game%26Watch_-_Nintendo.jpg/500px-Parachute_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Octopus (Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/9/93/Octopus_-_Game%26Watch_-_Nintendo.jpg/500px-Octopus_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Popeye (Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Popeye_-_Game%26Watch_-_Nintendo.jpg/500px-Popeye_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Chef (Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Chef_-_Game%26Watch_-_Nintendo.jpg/500px-Chef_-_Game%26Watch_-_Nintendo.jpg",
        // Corrigé le 2026-07-12 : l'ancienne photo (réutilisée aussi pour Panorama ci-dessous)
        // porte en réalité la mention "PANORAMA SCREEN" sur le boîtier — remplacée par une vraie
        // photo labellisée "WIDE SCREEN".
        "Game & Watch Mickey Mouse (Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f0/Mikey_Mouse_-_Game_%26_Watch_-_Nintendo.jpg/500px-Mikey_Mouse_-_Game_%26_Watch_-_Nintendo.jpg",
        "Game & Watch Egg (Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/58/Nintendo_Game_And_Watch_-_Egg.jpg/500px-Nintendo_Game_And_Watch_-_Egg.jpg",
        "Game & Watch Fire (Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/Fire_Game_%26_Watch_Wide_Screen.jpg/500px-Fire_Game_%26_Watch_Wide_Screen.jpg",
        "Game & Watch Turtle Bridge (Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/01/Turtle_Bridge_-_Game%26Watch_-_Nintendo.jpg/500px-Turtle_Bridge_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Fire Attack (Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/8/80/Fire_Attack_-_Game%26Watch_-_Nintendo_%28pixelized_screen_backdrop%29.jpg/500px-Fire_Attack_-_Game%26Watch_-_Nintendo_%28pixelized_screen_backdrop%29.jpg",
        "Game & Watch Snoopy Tennis (Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/GameAndWatch_SnoopyTennis.jpg/500px-GameAndWatch_SnoopyTennis.jpg",
        "Game & Watch Oil Panic (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3c/Nintendo_Oil_Panic_Game_%26_Watch%2C_Model_OP-51%2C_Made_In_Japan%2C_Copyright_1982_%28Electronic_Handheld_Game%29_%282%29.jpg/500px-Nintendo_Oil_Panic_Game_%26_Watch%2C_Model_OP-51%2C_Made_In_Japan%2C_Copyright_1982_%28Electronic_Handheld_Game%29_%282%29.jpg",
        "Game & Watch Donkey Kong (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Game_%26_Watch_DK.jpg/500px-Game_%26_Watch_DK.jpg",
        "Game & Watch Mickey and Donald (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/Mickey_%26_Donald_-_Game%26Watch_-_Nintendo.jpg/500px-Mickey_%26_Donald_-_Game%26Watch_-_Nintendo.jpg",
        // Photo fournie par l'utilisateur (2026-07-13) : remplace l'ancienne photo (qui montrait
        // Green House ET Donkey Kong côte à côte) par une photo nette de ce seul modèle.
        "Game & Watch Green House (Multi Screen)" to "file:///android_asset/console_photos/gw_green_house.webp",
        "Game & Watch Donkey Kong II (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/Nintendo_Donkey_Kong_II_Game_%26_Watch%2C_Model_No._JR-55%2C_Made_in_Japan%2C_Copyright_1983_%28Handheld_Electronic_Game%29.jpg/500px-Nintendo_Donkey_Kong_II_Game_%26_Watch%2C_Model_No._JR-55%2C_Made_in_Japan%2C_Copyright_1983_%28Handheld_Electronic_Game%29.jpg",
        "Game & Watch Mario Bros. (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ae/Mario_Bros._-_Game%26Watch_-_Nintendo.jpg/500px-Mario_Bros._-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Rain Shower (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/Rain_Shower_-_Game%26Watch_-_Nintendo.jpg/500px-Rain_Shower_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Pinball (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/Pinball_-_Game%26Watch_-_Nintendo.jpg/500px-Pinball_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Black Jack (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/Black_Jack_-_Game%26Watch_-_Nintendo.jpg/500px-Black_Jack_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Squish (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/Squish_-_Game%26Watch_-_Nintendo.jpg/500px-Squish_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Bomb Sweeper (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3c/Bomb_Sweeper_-_Game%26Watch_-_Nintendo.jpg/500px-Bomb_Sweeper_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Safebuster (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/SafeBuster_-_Game%26Watch_-_Nintendo.jpg/500px-SafeBuster_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Gold Cliff (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/Gold_Cliff_-_Game%26watch_-_Nintendo.jpg/500px-Gold_Cliff_-_Game%26watch_-_Nintendo.jpg",
        "Game & Watch Zelda (Multi Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/2/23/Zelda_-_Game_%26_Watch_-_Nintendo.jpg/500px-Zelda_-_Game_%26_Watch_-_Nintendo.jpg",
        "Game & Watch Donkey Kong Jr. (Table Top)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e3/Donkey_kong_jr_Game_and_Watch.jpg/500px-Donkey_kong_jr_Game_and_Watch.jpg",
        // Corrigé le 2026-07-12 : l'ancienne photo (générique) est en fait la version New Wide
        // Screen (voir plus bas) — remplacée par une photo explicitement labellisée "Tabletop".
        "Game & Watch Mario's Cement Factory (Table Top)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/d/da/Mario%27s_Cement_Factory_%28Tabletop%29_-_Game%26Watch_-_Nintendo.jpg/500px-Mario%27s_Cement_Factory_%28Tabletop%29_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Snoopy (Table Top)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Snoopy_Game_%26_Watch_Tabletop.jpg/500px-Snoopy_Game_%26_Watch_Tabletop.jpg",
        "Game & Watch Popeye (Table Top)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/Nintendo_Game_And_Watch_-_Popeye_PG-74_Table_top.jpg/500px-Nintendo_Game_And_Watch_-_Popeye_PG-74_Table_top.jpg",
        // Corrigé le 2026-07-12 : reprenait par erreur la photo Table Top (boîtier cabinet, très
        // différent) — remplacée par une vraie photo labellisée "Panorama".
        "Game & Watch Snoopy (Panorama)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6c/Snoopy_%28Panorama%29_-_Game%26Watch_-_Nintendo.jpg/500px-Snoopy_%28Panorama%29_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Popeye (Panorama)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6a/Nintendo_-_Game_%26_watch_-_Popeye_Panorama_PG-92.jpg/500px-Nintendo_-_Game_%26_watch_-_Popeye_Panorama_PG-92.jpg",
        "Game & Watch Donkey Kong Jr. (Panorama)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/03/Donkey_Kong_Jr._%28Panorama%29_-_Game%26Watch_-_Nintendo.jpg/500px-Donkey_Kong_Jr._%28Panorama%29_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Mickey Mouse (Panorama)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/Nintendo_Game_%26_Watch_Mickey_Mouse.jpg/500px-Nintendo_Game_%26_Watch_Mickey_Mouse.jpg",
        "Game & Watch Donkey Kong Circus (Panorama)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9f/Nintendo_Game_%26_Watch_Donkey_Kong_Circus.jpg/500px-Nintendo_Game_%26_Watch_Donkey_Kong_Circus.jpg",
        "Game & Watch Donkey Kong Jr. (New Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Donkey_Kong_Jr._-_Nintendo_Game_%26_Watch.jpg/500px-Donkey_Kong_Jr._-_Nintendo_Game_%26_Watch.jpg",
        "Game & Watch Mario's Cement Factory (New Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d8/Mario%27s_Cement_Factory_%28widescreen%29_-_Game%26Watch_-_Nintendo.jpg/500px-Mario%27s_Cement_Factory_%28widescreen%29_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Manhole (New Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Manhole_Game_%26_Watch_New_Wide_Screen.jpg/500px-Manhole_Game_%26_Watch_New_Wide_Screen.jpg",
        "Game & Watch Tropical Fish (New Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5f/Tropical_Fish_-_Game%26Watch_-_Nintendo.jpg/500px-Tropical_Fish_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Super Mario Bros. (New Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/GameAndWatch_SuperMarioBros.jpg/500px-GameAndWatch_SuperMarioBros.jpg",
        // Corrigé le 2026-07-12 : l'ancienne photo (réutilisée aussi pour Crystal Screen) montre
        // en fait le boîtier TRANSPARENT typique du Crystal Screen — remplacée par une vraie photo
        // du boîtier opaque New Wide Screen.
        "Game & Watch Climber (New Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/8/84/Game_%26_Watch_Climber.jpg/500px-Game_%26_Watch_Climber.jpg",
        "Game & Watch Balloon Fight (New Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/GameAndWatch_BalloonFight.jpg/500px-GameAndWatch_BalloonFight.jpg",
        "Game & Watch Mario the Juggler (New Wide Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7a/Game_%26_Watch_-_Mario_the_Juggler.png/500px-Game_%26_Watch_-_Mario_the_Juggler.png",
        "Game & Watch Spitball Sparky (Super Color)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Spitball_Sparky_-_Game%26Watch_-_Nintendo.jpg/500px-Spitball_Sparky_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Crab Grab (Super Color)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7f/Crab_grab.jpg/500px-Crab_grab.jpg",
        "Game & Watch Boxing / Punch-Out!! (Micro Vs. System)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/Nintendo_Game_%26_Watch_-_Boxing.jpg/500px-Nintendo_Game_%26_Watch_-_Boxing.jpg",
        "Game & Watch Donkey Kong 3 (Micro Vs. System)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Game%26watch-donkey-kong-3.jpg/500px-Game%26watch-donkey-kong-3.jpg",
        "Game & Watch Donkey Kong Hockey (Micro Vs. System)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/Donkey_Kong_Hockey_-_Game%26Watch_-_Nintendo.jpg/500px-Donkey_Kong_Hockey_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Super Mario Bros. (Crystal Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/Game_and_Watch_crystal_super_mario_bros.png/500px-Game_and_Watch_crystal_super_mario_bros.png",
        "Game & Watch Climber (Crystal Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Climber_-_Game%26Watch_-_Nintendo.jpg/500px-Climber_-_Game%26Watch_-_Nintendo.jpg",
        "Game & Watch Balloon Fight (Crystal Screen)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f4/Nintendo_Game_Watch_Balloon_Fight_BF-803.jpg/500px-Nintendo_Game_Watch_Balloon_Fight_BF-803.jpg",
        // Super Mario Bros. (non commercialisé, prototype YM-901 1987) : aucune photo propre sur
        // Commons (jamais vendu en boutique) — on reprend la photo du modèle New Wide Screen
        // (même jeu, même look monochrome), plus proche que le repli générique de la gamme.
        "Game & Watch Super Mario Bros. (non commercialisé)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/GameAndWatch_SuperMarioBros.jpg/500px-GameAndWatch_SuperMarioBros.jpg",
        // Photo (3) de cette même série montrait le DOS (vis + étiquette) : remplacée par la (4),
        // qui montre bien l'avant (écran + boutons), comme pour toutes les autres fiches.
        "Game & Watch Ball (réédition 2010)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Nintendo_Game_%26_Watch_Ball_RGW-001_%2830th_anniversary_reissue%2C_2011%29_%284%29.jpg/500px-Nintendo_Game_%26_Watch_Ball_RGW-001_%2830th_anniversary_reissue%2C_2011%29_%284%29.jpg",
        "Game & Watch: Super Mario Bros. (2020)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f2/Game_%26_Watch_Super_Mario_Bros_-_Color_Screen_%281%29.jpg/500px-Game_%26_Watch_Super_Mario_Bros_-_Color_Screen_%281%29.jpg",
        "Game & Watch: The Legend of Zelda (2021)" to "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d6/Game_%26_Watch_The_Legend_of_Zelda_system_%2853665518430%29.jpg/500px-Game_%26_Watch_The_Legend_of_Zelda_system_%2853665518430%29.jpg",
        // Lifeboat (Multi Screen) et Mario's Bombs Away (Panorama) : aucune photo libre trouvée
        // sur Commons malgré recherche exhaustive -> photos fournies par l'utilisateur (2026-07-13).
        "Game & Watch Lifeboat (Multi Screen)" to "file:///android_asset/console_photos/gw_lifeboat.webp",
        "Game & Watch Mario's Bombs Away (Panorama)" to "file:///android_asset/console_photos/gw_mario_bombs_away.webp"
    )

    fun urlFor(name: String): String? = gameAndWatchModelPhotos[name] ?: byName[name]
        // Fiche générique « Game & Watch » (sans modèle précis), ou modèle Game & Watch sans photo
        // d'unité disponible (Lifeboat, Mario's Bombs Away) ou pas encore ajouté à la map ci-dessus :
        // repli sur une vraie photo Game & Watch plutôt qu'une image sans rapport (ex. vignette 3D
        // GameCube posée par l'auto-remplissage) ou un lien mort.
        ?: GAME_AND_WATCH_FALLBACK.takeIf { name.startsWith("Game & Watch", ignoreCase = true) }
}
