package com.example.macollection.data

/**
 * Jeux les plus connus pour les consoles/ordinateurs absents du catalogue RAWG (vérifié
 * directement auprès de l'API RAWG : ces plateformes n'y existent simplement pas, donc aucune
 * requête en ligne ne peut jamais renvoyer de résultat pour elles). Liste manuelle, juste les
 * noms — affichée en repli dans l'Encyclopédie à la place de « Liste de jeux indisponible ».
 */
object CuratedGames {

    val byConsole: Map<String, List<String>> = mapOf(
        "ColecoVision" to listOf(
            "Donkey Kong", "Zaxxon", "Lady Bug", "BurgerTime", "Pitfall!", "Venture", "Cosmic Avenger"
        ),
        "Vectrex" to listOf(
            "Star Trek: The Motion Picture", "Minestorm", "Spike", "Berzerk", "Spinball", "Scramble"
        ),
        "Intellivision" to listOf(
            "Astrosmash", "Utopia", "Night Stalker", "Tron: Deadly Discs", "NFL Football", "Burger Time"
        ),
        "Videopac G7200" to listOf(
            "K.C. Munchkin!", "Pick Axe Pete!", "Killer Bees!", "UFO!", "Power Lords", "Q-bert"
        ),
        "Channel F" to listOf(
            "Video Whizball", "Maze", "Tic-Tac-Toe", "Spitfire", "Pinball Challenge", "Dodge It"
        ),
        "SG-1000" to listOf(
            "Congo Bongo", "Flicky", "Pitfall II", "Zaxxon", "Doki Doki Penguin Land", "Girl's Garden"
        ),
        // Même architecture et mêmes cartouches que la SG-1000 (la SC-3000 ajoute juste un clavier).
        "SC-3000" to listOf(
            "Congo Bongo", "Flicky", "Pitfall II", "Zaxxon", "Doki Doki Penguin Land", "Girl's Garden"
        ),
        "Neo Geo Pocket" to listOf(
            "SNK vs. Capcom: Card Fighters' Clash", "King of Fighters R-1", "Samurai Shodown! 2", "Baseball Stars"
        ),
        "Neo Geo Pocket Color" to listOf(
            "Metal Slug: 1st Mission", "Metal Slug 2nd Mission", "Fatal Fury: First Contact",
            "SNK vs. Capcom: Match of the Millennium", "Sonic the Hedgehog Pocket Adventure", "The Last Blade: Beyond the Destiny"
        ),
        "WonderSwan" to listOf(
            "Mr. Driller", "Rockman & Forte (Mega Man & Bass)", "Digimon Tamers: Battle Spirit", "Final Fantasy"
        ),
        "WonderSwan Color" to listOf(
            "Final Fantasy I & II", "Klonoa: Moonlight Museum", "Guilty Gear Petit 2", "Digimon Adventure", "Rockman & Forte: Mirai Kara no Chōsen"
        ),
        "ZX Spectrum" to listOf(
            "Manic Miner", "Jet Set Willy", "Chuckie Egg", "Skool Daze", "Elite", "The Lords of Midnight"
        ),
        "Commodore 64" to listOf(
            "The Last Ninja", "International Karate +", "Pirates!", "Maniac Mansion", "Wizball", "Uridium"
        ),
        "CPC 464" to listOf(
            "Sorcery", "Roland in the Caves", "Prehistorik 2", "Chase H.Q.", "Head Over Heels", "Batman: The Movie"
        ),
        "CPC 6128" to listOf(
            "Sorcery", "Roland in the Caves", "Prehistorik 2", "Chase H.Q.", "Head Over Heels", "Batman: The Movie"
        ),
        "CPC 6128+" to listOf(
            "Sorcery", "Roland in the Caves", "Prehistorik 2", "Chase H.Q.", "Head Over Heels", "Batman: The Movie"
        ),
        "MSX" to listOf(
            "Metal Gear", "Knightmare", "Salamander", "Aleste", "Penguin Adventure", "Vampire Killer"
        )
    )

    fun gamesFor(consoleName: String): List<String>? = byConsole[consoleName]
}
