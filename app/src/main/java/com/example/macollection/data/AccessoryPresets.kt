package com.example.macollection.data

/** Un accessoire du catalogue intégré (preset). */
data class AccessoryPreset(
    val brand: String,
    val name: String,
    val year: Int,
    /** Nom de la console associée (référence à [consolePresets], peut ne pas y figurer). */
    val console: String,
    val description: String
) {
    /** Texte de fiche prêt à stocker dans l'objet de la collection. */
    fun specsText(): String = buildString {
        append("$brand $name ($year)\n")
        append("Console associée : $console")
        if (description.isNotBlank()) append("\n$description")
    }
}

/**
 * Catalogue d'accessoires officiels (manettes et périphériques notables).
 * Les noms reprennent exactement ceux renvoyés par [AccessoryRecognition] quand ils
 * existent, pour que le scan photo et le choix manuel donnent le même résultat.
 */
val accessoryPresets: List<AccessoryPreset> = listOf(
    AccessoryPreset("Nintendo", "Zapper (NES)", 1985, "NES", "Pistolet optique livré avec Duck Hunt."),
    AccessoryPreset("Nintendo", "R.O.B.", 1985, "NES", "Robot-jouet piloté par la lumière de l'écran, vendu pour relancer la NES aux USA."),
    AccessoryPreset("Sega", "Light Phaser", 1986, "Master System", "Pistolet optique de la Master System."),
    AccessoryPreset("Sega", "3-D Glasses", 1987, "Master System", "Lunettes stéréoscopiques actives pour les jeux compatibles « 3-D »."),
    AccessoryPreset("Sega", "Control Pad (Master System)", 1986, "Master System", "Manette standard de la Master System : croix directionnelle et deux boutons."),
    AccessoryPreset("Sega", "Control Stick (Master System)", 1987, "Master System", "Joystick officiel de la Master System (modèle 3050), au manche amovible, pensé pour un confort de type arcade."),
    AccessoryPreset("Nintendo", "Super Scope", 1992, "Super Nintendo (SNES)", "Pistolet optique sans fil de la SNES, en forme de bazooka d'épaule."),
    AccessoryPreset("Nintendo", "Super Scope (Super Famicom)", 1992, "Super Famicom", "Version japonaise du pistolet optique sans fil de la Super Famicom, vendue avec Super Scope 6."),
    AccessoryPreset("Nintendo", "Manette NES", 1985, "NES", "Manette rectangulaire d'origine, croix directionnelle + A/B/Select/Start."),
    AccessoryPreset("Nintendo", "Manette Famicom", 1983, "Famicom", "Manette filaire fixée à la console (non détachable), modèle japonais d'origine."),
    AccessoryPreset("Nintendo", "Manette SNES", 1991, "Super Nintendo (SNES)", "Première manette à 4 boutons façon losange (A/B/X/Y) et gâchettes L/R."),
    AccessoryPreset("Atari", "Joystick CX40", 1977, "2600 (VCS)", "Joystick à un bouton emblématique de l'Atari 2600, copié par de nombreux concurrents."),
    AccessoryPreset("Sega", "Manette Mega Drive", 1988, "Mega Drive", "Manette à 3 puis 6 boutons (modèle « 6 Button »)."),
    AccessoryPreset("Sega", "Manette Saturn", 1994, "Saturn", "Manette à 6 boutons façon arcade, très appréciée pour les jeux de combat."),
    AccessoryPreset("Sega", "Manette Dreamcast", 1998, "Dreamcast", "Premier pad Sega avec stick analogique et emplacement pour carte VMU."),
    AccessoryPreset("Nintendo", "Manette Nintendo 64", 1996, "Nintendo 64", "Forme à trois branches inédite, premier stick analogique grand public."),
    AccessoryPreset("Coleco", "Manette ColecoVision", 1982, "ColecoVision", "Pavé numérique intégré et joystick auto-centré amovible."),
    AccessoryPreset("Mattel", "Manette Intellivision", 1979, "Intellivision", "Disque directionnel circulaire et pavé numérique à overlays interchangeables."),
    AccessoryPreset("Sega", "Menacer", 1992, "Mega Drive", "Pistolet optique sans fil (infrarouge) de la Mega Drive."),
    AccessoryPreset("Nintendo", "Manette GameCube", 2001, "GameCube", "Manette officielle, prisée encore aujourd'hui pour les jeux de combat."),
    AccessoryPreset("Sony", "Manette DualShock (PS1)", 1997, "PlayStation", "Première manette PlayStation avec retour de force (vibrations)."),
    AccessoryPreset("Sony", "Manette DualShock 2 (PS2)", 2000, "PlayStation 2", "Boutons et sticks analogiques sensibles à la pression."),
    AccessoryPreset("Nintendo", "Wiimote / Nunchuk (Wii)", 2006, "Wii", "Manette à détection de mouvement, associée au Nunchuk."),
    AccessoryPreset("Sony", "Manette DualShock 3 (PS3)", 2006, "PlayStation 3", "Retour de la vibration après l'épisode Sixaxis sans vibration."),
    AccessoryPreset("Microsoft", "Manette Xbox 360", 2005, "Xbox 360", "Manette très appréciée, devenue un standard de fait sur PC."),
    AccessoryPreset("Sony", "Manette DualShock 4 (PS4)", 2013, "PlayStation 4", "Pavé tactile et barre lumineuse intégrés."),
    AccessoryPreset("Microsoft", "Manette Xbox One", 2013, "Xbox One", "Sticks affinés et retour de force dans les gâchettes."),
    AccessoryPreset("Nintendo", "Paire de Joy-Con (Switch)", 2017, "Switch", "Paire de manettes détachables, utilisables seules ou ensemble."),
    AccessoryPreset("Nintendo", "Manette Switch Pro", 2017, "Switch", "Manette classique pour la Switch, façon manette de salon."),
    AccessoryPreset("Microsoft", "Manette Xbox Series", 2020, "Xbox Series X", "Croix directionnelle revue et nouveau bouton de partage."),
    AccessoryPreset("Sony", "Manette DualSense (PS5)", 2020, "PlayStation 5", "Retour haptique fin et gâchettes adaptatives."),

    // --- Éditions limitées notables (pas de photo libre de droits disponible : produits
    // commerciaux récents, non hébergeables sous licence libre sur Wikimedia Commons) ---
    AccessoryPreset("Sony", "Manette DualSense Édition The Last of Us Part II", 2020, "PlayStation 5", "Édition limitée aux couleurs et motifs du jeu The Last of Us Part II."),
    AccessoryPreset("Sony", "Manette DualSense Édition 30e Anniversaire", 2024, "PlayStation 5", "Édition translucide grise hommage à la PlayStation originale (1994-2024)."),
    AccessoryPreset("Sony", "Manette DualSense Édition Marvel's Spider-Man 2", 2023, "PlayStation 5", "Édition limitée aux couleurs rouge/noir du jeu Marvel's Spider-Man 2."),
    AccessoryPreset("Sony", "Manette DualSense Édition God of War Ragnarök", 2022, "PlayStation 5", "Édition limitée aux couleurs et gravures du jeu God of War Ragnarök."),
    AccessoryPreset("Microsoft", "Manette Xbox Édition Halo Infinite", 2021, "Xbox Series X", "Édition limitée aux couleurs et motifs du jeu Halo Infinite."),
    AccessoryPreset("Nintendo", "Manette Switch Pro Édition Splatoon 3", 2022, "Switch", "Édition limitée aux couleurs et motifs du jeu Splatoon 3."),
    AccessoryPreset("Nintendo", "Manette Switch Pro Édition The Legend of Zelda: Tears of the Kingdom", 2023, "Switch", "Édition limitée gris foncé à motif spirale doré gravé, sortie avec The Legend of Zelda: Tears of the Kingdom."),

    // --- Ajouts 2026-07-13 (photos fournies par l'utilisateur) ---
    AccessoryPreset("Capcom / ASCII", "Resident Evil Pad", 1996, "PlayStation", "Manette tierce officiellement sous licence Capcom, vendue avec la Directional Pad étendue, inspirée de Resident Evil."),
    AccessoryPreset("SNK", "Manette Neo Geo AES", 1990, "Neo Geo AES", "Manette en forme de « cacahuète », avec joystick amovible façon arcade et 4 boutons."),
    AccessoryPreset("RAM Electronics", "Magnum Light Phaser", 1983, "ZX Spectrum", "Pistolet optique pour le ZX Spectrum, sans lien avec le Light Phaser de la Sega Master System malgré le nom similaire."),
    AccessoryPreset("Nintendo", "Manette Pikachu (N64)", 2000, "Nintendo 64", "Manette orange à l'effigie de Pikachu, sortie pour promouvoir Pokémon Stadium."),
    AccessoryPreset("SNK / Tommo", "Arc Stick Pro (NeoGeo X)", 2013, "NeoGeo X Gold", "Joystick d'arcade officiel de la NeoGeo X Gold, avec stick amovible façon borne d'arcade."),
    AccessoryPreset("SNK / PLAION", "Arcade Stick Noir (NeoGeo AES+)", 2026, "Neo Geo AES+", "Stick d'arcade sans fil vendu avec la Neo Geo AES+, coloris noir d'origine, rétrocompatible avec les Neo Geo AES classiques."),
    AccessoryPreset("SNK / PLAION", "Arcade Stick Blanc 35e Anniversaire (NeoGeo AES+)", 2026, "Neo Geo AES+", "Stick d'arcade sans fil édition 35e anniversaire, coloris blanc, vendu avec la Neo Geo AES+."),
    AccessoryPreset("Nintendo", "Mallette de transport (N64)", 1997, "Nintendo 64", "Mallette de transport rigide officielle aux couleurs de Super Mario 64, avec rangement pour la console, les manettes et les jeux."),
    AccessoryPreset("Sega", "Stunner / Virtua Gun (Saturn)", 1995, "Saturn", "Pistolet optique de la Saturn, vendu à l'origine avec Virtua Cop."),
    AccessoryPreset("Sega", "SF-7000", 1983, "SC-3000", "« Super Control Station » : extension professionnelle de la SC-3000 avec lecteur de disquette, port série et RAM étendue."),
    AccessoryPreset("Sony", "Écran LCD PSone (Combo)", 2001, "PSone", "Écran LCD 5 pouces officiel se clipsant au dos de la PSone, vendu avec son adaptateur secteur/allume-cigare."),

    // --- Accessoires complémentaires (recherche approfondie 2026-07-26) ---
    AccessoryPreset("Sony", "Carte Mémoire (PlayStation)", 1994, "PlayStation", "Carte mémoire 1 Mo (SCPH-1020) pour sauvegarder les parties sur PS1."),
    AccessoryPreset("Sony", "Carte Mémoire 8 Mo (PS2)", 2000, "PlayStation 2", "Carte mémoire officielle 8 Mo (SCPH-10020) avec chiffrement MagicGate."),
    AccessoryPreset("Sega", "VMU (Dreamcast)", 1998, "Dreamcast", "Carte mémoire à écran LCD intégré, servant aussi de mini-console autonome (Chao Adventure)."),
    AccessoryPreset("Nintendo", "Carte Mémoire 251 (GameCube)", 2001, "GameCube", "Carte mémoire officielle 2 Mo (251 blocs)."),
    AccessoryPreset("Microsoft", "Unité de Mémoire (Xbox 360)", 2005, "Xbox 360", "Carte mémoire officielle (64 Mo puis 512 Mo) pour les modèles Xbox 360 sans disque dur."),
    AccessoryPreset("Sony", "Multitap (PlayStation)", 1997, "PlayStation", "Adaptateur officiel permettant de brancher jusqu'à 4 manettes/cartes mémoire sur un seul port."),
    AccessoryPreset("Nintendo", "Volant Wii (Wii Wheel)", 2008, "Wii", "Coque plastique dans laquelle s'insère la Wiimote, fournie avec chaque exemplaire de Mario Kart Wii."),
    AccessoryPreset("Microsoft", "Volant sans fil Xbox 360 (Wireless Racing Wheel)", 2006, "Xbox 360", "Volant à retour de force développé avec Forza Motorsport 2."),
    AccessoryPreset("Sega", "Canne à pêche Dreamcast (Sega Bass Fishing)", 1999, "Dreamcast", "Contrôleur en forme de canne à pêche avec moulinet rotatif et vibration, vendu avec Sega Bass Fishing."),
    AccessoryPreset("Nintendo", "Wii Balance Board", 2007, "Wii", "Plateforme d'équilibre à capteurs de pression, popularisée par Wii Fit."),
    AccessoryPreset("Nintendo", "Wii MotionPlus", 2009, "Wii", "Extension se greffant sous la Wiimote pour une détection de mouvement 1:1."),
    AccessoryPreset("Microsoft", "Kinect (Xbox 360)", 2010, "Xbox 360", "Capteur de mouvement et de voix sans manette, record du monde de ventes."),
    AccessoryPreset("Microsoft", "Kinect (Xbox One)", 2013, "Xbox One", "Version HD du capteur, avec reconnaissance vocale et infrarouge avancée."),
    AccessoryPreset("Sony", "Manette PlayStation Move", 2010, "PlayStation 3", "Manette de mouvement suivie par la caméra PlayStation Eye."),
    AccessoryPreset("Sony", "PlayStation Move Sharp Shooter", 2011, "PlayStation 3", "Crosse d'arme accueillant une manette Move et une manette de navigation."),
    AccessoryPreset("Sony", "PS VR Aim Controller", 2017, "PlayStation 4", "Fusil dédié au jeu Farpoint, suivi par la caméra PS4 et le casque PS VR."),
    AccessoryPreset("Nintendo", "Power Pad (NES)", 1988, "NES", "Tapis de sol à capteurs de pression, vendu avec World Class Track Meet."),
    AccessoryPreset("Nintendo / Asciiware", "NES Advantage", 1987, "NES", "Joystick d'arcade à turbo réglable."),
    AccessoryPreset("Nintendo", "NES Max", 1988, "NES", "Variante à disque directionnel façon « cyclone » plutôt qu'un stick d'arcade."),
    AccessoryPreset("Nintendo", "Imprimante Game Boy", 1998, "Game Boy", "Imprimante thermique compacte, utilisée avec la Game Boy Camera."),
    AccessoryPreset("Nintendo", "Game Boy Camera", 1998, "Game Boy", "Appareil photo numérique le plus petit du monde selon le Guinness 1999."),
    AccessoryPreset("Nintendo", "Rumble Pak (N64)", 1997, "Nintendo 64", "Extension à vibrations, élue « Best Peripheral » en 1997."),
    AccessoryPreset("Nintendo", "e-Reader (GBA)", 2001, "Game Boy Advance", "Lecteur de cartes à code-barres débloquant du contenu, dont des jeux NES complets."),
    AccessoryPreset("Nintendo", "Voice Recognition Unit (N64)", 2000, "Nintendo 64", "Micro reconnaissant environ 200 mots, indispensable pour Hey You, Pikachu!."),
    AccessoryPreset("Nintendo", "Wii Zapper", 2007, "Wii", "Crosse en plastique accueillant Wiimote et Nunchuk, fournie avec Link's Crossbow Training."),
    AccessoryPreset("Nintendo", "Classic Controller Pro (Wii)", 2009, "Wii", "Manette façon salon pour la Console Virtuelle, forme proche de la manette GameCube."),
    AccessoryPreset("Nintendo", "Manette WaveBird (GameCube)", 2002, "GameCube", "Première manette sans fil (RF) de Nintendo, restée sans vibration pour éviter les interférences radio."),
    AccessoryPreset("Nintendo / Namco", "DK Bongos (GameCube)", 2003, "GameCube", "Paire de bongos avec micro intégré, conçue pour Donkey Konga."),
    AccessoryPreset("Capcom", "Manette Steel Battalion (Xbox)", 2002, "Xbox", "Contrôleur massif à plus de 40 points d'entrée (2 joysticks, 3 pédales), vendu avec le jeu."),
    AccessoryPreset("Sega", "3D Control Pad (Saturn)", 1996, "Saturn", "Première manette Saturn à stick analogique, débutant avec NiGHTS into Dreams."),
    AccessoryPreset("Sony", "EyeToy (PS2)", 2003, "PlayStation 2", "Webcam couleur exploitant la reconnaissance de mouvement et gestuelle."),

    AccessoryPreset("Nintendo", "Controller Pak (N64)", 1997, "Nintendo 64", "Carte mémoire se logeant dans la manette N64, pour les jeux sans sauvegarde sur cartouche."),
    AccessoryPreset("Sega", "Mission Stick (Saturn)", 1995, "Saturn", "Joystick de vol/tir 3 axes, pensé pour Panzer Dragoon."),
    AccessoryPreset("Sega", "Sega Activator", 1993, "Mega Drive", "Anneau infrarouge au sol détectant les mouvements du corps entier, précurseur de la Wiimote/Kinect.")
)
    .sortedBy { it.name.lowercase() }
