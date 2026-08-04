package com.example.macollection.data

/**
 * Reconnaît une console à partir du texte lu sur une photo (OCR), en cherchant
 * des mots-clés connus. Renvoie le nom exact du preset (utilisable pour le prix eBay),
 * ou null si rien de fiable.
 *
 * Règles ordonnées du plus spécifique au plus général (ex. "super nintendo" avant
 * "nintendo entertainment", "xbox 360" avant "xbox").
 */
object ConsoleRecognition {

    private val rules: List<Pair<List<String>, String>> = listOf(
        // --- Éditions limitées de consoles : à vérifier avant les règles génériques
        // (ex. « playstation 4 ») sous peine de toujours retomber sur le modèle de base.
        listOf("uncharted 4 limited edition", "uncharted 4 ps4 limited edition") to "PlayStation 4 Édition Uncharted 4",
        listOf("spider-man ps4 pro", "spider-man limited edition ps4", "marvel's spider-man ps4 pro") to "PlayStation 4 Pro Édition Marvel's Spider-Man",
        listOf("last of us part ii ps4 pro", "last of us part 2 ps4 pro", "the last of us part ii limited edition") to "PlayStation 4 Pro Édition The Last of Us Part II",
        listOf("halo 3 limited edition", "halo 3 special edition") to "Xbox 360 Édition Halo 3",
        listOf("gears of war limited edition") to "Xbox 360 Édition Gears of War",
        listOf("halo 4 limited edition") to "Xbox 360 Édition Halo 4",
        listOf("star wars limited edition", "star wars xbox 360", "kinect star wars limited edition", "r2-d2 xbox 360") to "Xbox 360 Édition Kinect Star Wars",
        listOf("halo 5 limited edition", "halo 5: guardians limited edition", "halo 5 guardians limited edition") to "Xbox One Édition Halo 5 Guardians",
        listOf("project scorpio edition", "project scorpio") to "Xbox One X Édition Project Scorpio",
        listOf("minecraft limited edition", "minecraft xbox one s") to "Xbox One S Édition Minecraft",
        listOf("cyberpunk 2077 limited edition", "cyberpunk 2077 xbox one x") to "Xbox One X Édition Cyberpunk 2077",
        listOf("halo infinite limited edition console", "halo infinite xbox series x console") to "Xbox Series X Édition Halo Infinite",
        listOf("pikachu edition", "pikachu version", "n64 pikachu") to "Nintendo 64 Édition Pikachu",
        listOf("neogeo x gold", "neo geo x gold", "neogeo x station", "neo geo x station") to "NeoGeo X Gold",
        listOf("chains of olympus limited", "chains of olympus psp", "god of war chains of olympus") to "PSP Édition God of War: Chains of Olympus",
        listOf("ghost of sparta limited", "ghost of sparta psp", "god of war ghost of sparta") to "PSP Édition God of War: Ghost of Sparta",
        listOf("monster hunter portable 2nd g", "monster hunter freedom 2 hunter pack") to "PSP Édition Monster Hunter Portable 2nd G",
        listOf("hatsune miku limited edition", "hatsune miku vita", "project diva vita limited") to "PlayStation Vita Édition Hatsune Miku",
        listOf("wii 25th anniversary", "wii 25e anniversaire", "mario 25th anniversary wii") to "Wii Édition 25e Anniversaire Mario",
        listOf("wind waker hd deluxe", "wind waker hd premium", "zelda wind waker wii u") to "Wii U Édition The Wind Waker HD",
        listOf("super smash bros wii u bundle", "smash bros for wii u bundle") to "Wii U Édition Super Smash Bros.",
        listOf("2600+ pac-man", "2600 plus pac-man", "atari 2600+ pac-man") to "Atari 2600+ Édition Pac-Man",
        listOf("magic knight rayearth", "rayearth") to "Game Gear Magic Knight Rayearth",
        listOf("jungle green") to "Nintendo 64 Jungle Green",
        listOf("fire orange", "sun orange") to "Nintendo 64 Fire Orange",
        listOf("ice blue") to "Nintendo 64 Ice Blue",
        listOf("watermelon red") to "Nintendo 64 Watermelon Red",
        listOf("grape purple", "midnight blue n64") to "Nintendo 64 Grape Purple",
        listOf("nes classic edition", "nes classic mini") to "NES Classic Edition",
        listOf("super nes classic edition", "snes classic edition", "super nintendo classic mini") to "Super NES Classic Edition",
        listOf("mega drive mini", "genesis mini") to "Mega Drive Mini",
        listOf("pc engine mini", "pc engine coregrafx mini", "turbografx mini") to "PC Engine Mini",
        listOf("neo geo mini", "neogeo mini") to "Neo Geo Mini",
        listOf("playstation classic") to "PlayStation Classic",
        listOf("game gear micro") to "Game Gear Micro",
        listOf("zx spectrum") to "ZX Spectrum",
        listOf("commodore 64", "c64", "c=64") to "Commodore 64",
        listOf("amstrad cpc 464", "cpc464", "cpc 464") to "CPC 464",
        listOf("amstrad cpc 6128+", "cpc6128+", "cpc 6128+", "cpc 6128 plus") to "CPC 6128+",
        listOf("amstrad cpc 6128", "cpc6128", "cpc 6128") to "CPC 6128",
        listOf("atari st", "1040stf", "1040 stf", "520st") to "1040 STF",
        listOf("amiga 1200", "a1200") to "Amiga 1200",
        listOf("amiga 500", "a500") to "Amiga 500",
        listOf("msx") to "MSX",
        listOf("apple ii", "apple 2", "appleii") to "Apple II",
        listOf("apple macintosh", "macintosh") to "Macintosh",
        listOf("trs-80", "trs 80", "trs80") to "TRS-80",
        listOf("commodore pet", "cbm pet") to "PET",
        listOf("atari 400/800", "atari 800", "atari 400", "atari 8-bit") to "400 / 800",
        listOf("vic-20", "vic 20", "vic20") to "VIC-20",
        listOf("zx81", "zx-81", "zx 81", "zx80", "zx-80") to "ZX81",
        listOf("bbc micro", "bbc model b") to "BBC Micro",
        listOf("thomson to7", "to7", "to-7") to "TO7",
        listOf("thomson mo5", "mo5", "mo-5") to "MO5",
        listOf("sharp x68000", "x68000", "x68k") to "X68000",
        listOf("acorn archimedes", "archimedes") to "Archimedes",
        listOf("laseractive", "laser active") to "LaserActive",
        listOf("c64 games system", "c64gs", "c64 gs") to "C64 Games System",
        listOf("compact vision tv boy", "tv boy") to "Compact Vision TV Boy",

        listOf("super famicom", "sfc") to "Super Famicom",
        listOf("super nintendo entertainment system", "super nintendo", "super nes", "snes") to "Super Nintendo (SNES)",
        listOf("nintendo 64dd", "64dd", "n64dd") to "Nintendo 64DD",
        listOf("nintendo 64", "n64") to "Nintendo 64",
        listOf("game boy advance sp", "gameboy advance sp", "gba sp", "gbasp") to "Game Boy Advance SP",
        listOf("game boy advance", "gameboy advance", "gba") to "Game Boy Advance",
        // OCR confond souvent le « B » du logo stylisé Game Boy avec un « 8 » (« Game 8oy »).
        listOf("game boy color", "gameboy color", "game 8oy color", "game8oy color", "gbc") to "Game Boy Color",
        listOf("game boy pocket", "gameboy pocket", "game 8oy pocket", "gbp") to "Game Boy Pocket",
        listOf("game boy light", "gameboy light", "game 8oy light", "gbl") to "Game Boy Light",
        listOf("game boy micro", "gameboy micro", "game 8oy micro", "gbm") to "Game Boy Micro",
        listOf("game boy", "gameboy", "game 8oy", "game8oy", "gb") to "Game Boy",
        listOf("entertainment system", "nintendo entertainment", "nes") to "NES",
        listOf("panasonic q") to "GameCube (Panasonic Q)",
        listOf("gamecube", "game cube", "ngc", "gcn") to "GameCube",
        listOf("wii u", "wiiu") to "Wii U",
        listOf("nintendo wii", "wii") to "Wii",
        listOf("nintendo switch oled", "switch oled") to "Switch OLED",
        listOf("nintendo switch 2", "switch 2", "switch2", "ns2") to "Switch 2",
        listOf("nintendo switch") to "Switch",
        listOf("new nintendo 3ds xl", "new 3ds xl", "new 3dsll") to "New Nintendo 3DS XL",
        listOf("new nintendo 2ds xl", "new 2ds xl", "new 2dsll") to "New Nintendo 2DS XL",
        listOf("nintendo 3ds xl", "3ds xl") to "Nintendo 3DS XL",
        listOf("nintendo 2ds", "2ds") to "Nintendo 2DS",
        listOf("nintendo 3ds", "new 3ds", "3ds") to "Nintendo 3DS",
        listOf("nintendo dsi xl", "dsi xl", "dsi ll") to "Nintendo DSi XL",
        listOf("nintendo dsi", "dsi") to "Nintendo DSi",
        listOf("nintendo ds", "ds lite", "nds") to "Nintendo DS",
        listOf("virtual boy", "vb") to "Virtual Boy",
        listOf("playstation 5", "ps5") to "PlayStation 5",
        listOf("playstation 4 pro", "ps4 pro", "ps4pro") to "PlayStation 4 Pro",
        listOf("playstation 4", "ps4") to "PlayStation 4",
        listOf("playstation 3", "ps3") to "PlayStation 3",
        listOf("playstation 2", "ps2") to "PlayStation 2",
        listOf("psp go") to "PSP Go",
        listOf("playstation portable", "psp") to "PSP",
        listOf("playstation vita", "ps vita", "psvita", "vita") to "PlayStation Vita",
        listOf("playstation classic", "ps classic") to "PlayStation Classic",
        listOf("playstation", "psx", "ps one", "psone", "ps1") to "PlayStation",
        listOf("xbox series x", "xsx") to "Xbox Series X",
        listOf("xbox series s", "xss") to "Xbox Series S",
        listOf("xbox series") to "Xbox Series X",
        listOf("xbox one", "xb1", "xbone") to "Xbox One",
        listOf("xbox 360", "x360") to "Xbox 360",
        listOf("xbox") to "Xbox",
        listOf("dreamcast", "dc") to "Dreamcast",
        listOf("sega saturn", "saturn") to "Saturn",
        listOf("mega drive 2", "megadrive 2", "genesis 2", "md2") to "Mega Drive 2",
        listOf("mega drive", "megadrive", "genesis", "md") to "Mega Drive",
        listOf("master system", "sms") to "Master System",
        listOf("game gear", "gg") to "Game Gear",
        listOf("mega cd 2", "sega cd 2", "sega cd v2", "mcd2") to "Mega CD 2",
        listOf("mega cd", "sega cd", "mcd") to "Mega CD",
        listOf("game & watch", "game and watch", "g&w", "gw") to "Game & Watch",
        listOf("neo geo cd", "ngcd") to "Neo Geo CD",
        listOf("neo geo pocket color", "ngpc") to "Neo Geo Pocket Color",
        listOf("neo geo pocket", "ngp") to "Neo Geo Pocket",
        listOf("neo geo", "neogeo", "ng") to "Neo Geo AES",
        listOf("pc engine duo-rx", "pc engine duo rx") to "PC Engine Duo-RX",
        listOf("pc engine duo") to "PC Engine Duo",
        listOf("twin famicom") to "Twin Famicom",
        listOf("pc engine lt") to "PC Engine LT",
        listOf("supergrafx", "super grafx", "sgfx") to "SuperGrafx",
        listOf("turbografx", "turbo grafx", "tg16", "tg-16") to "TurboGrafx-16",
        listOf("turboexpress", "turbo express", "tg-gt") to "TurboExpress",
        listOf("core grafx ii", "coregrafx ii", "core grafx 2", "coregrafx 2") to "PC Engine CoreGrafx II",
        listOf("core grafx", "coregrafx") to "PC Engine CoreGrafx",
        listOf("pc engine", "pce") to "PC Engine",
        listOf("atari 2600", "2600") to "2600 (VCS)",
        listOf("atari 5200", "5200") to "5200",
        listOf("atari 7800", "7800") to "7800",
        listOf("atari lynx", "lynx") to "Lynx",
        listOf("atari jaguar cd", "jaguar cd") to "Jaguar CD",
        listOf("atari jaguar", "jaguar") to "Jaguar",
        listOf("intellivision ii", "intellivision 2") to "Intellivision II",
        listOf("intellivision") to "Intellivision",
        listOf("colecovision") to "ColecoVision",
        listOf("vectrex") to "Vectrex",
        listOf("wonderswan color", "wsc") to "WonderSwan Color",
        listOf("wonderswan", "ws") to "WonderSwan",
        listOf("3do fz-10", "3do fz10") to "3DO (FZ-10)",
        listOf("3do") to "3DO",
        listOf("steam deck") to "Steam Deck"
    )

    /**
     * Une abréviation courte (« gb », « ps1», « n64»...) ne doit matcher que comme mot entier,
     * sinon elle déclencherait des faux positifs en apparaissant à l'intérieur d'un autre mot
     * (ex. « nes » à l'intérieur de « snes »). Les phrases à plusieurs mots restent comparées en
     * sous-chaîne classique (déjà sans risque, et nécessaire pour les variantes sans espace fixe).
     */
    private fun matchesKey(text: String, key: String): Boolean {
        // Un tiret joue souvent le même rôle qu'une espace dans ces libellés ("PC-Engine" vs
        // "PC Engine", "Core-Grafx" vs "Core Grafx") : on l'aplatit aussi des deux côtés, sinon un
        // titre d'annonce stylisé avec un tiret ne matchait plus du tout (ex. "Pc-engine Core Grafx
        // II" scanné par code-barres, jamais reconnu comme console faute de "pc engine" littéral).
        val normalizedKey = key.replace(Regex("[\\s-]+"), " ")
        return if (!normalizedKey.contains(' ') && normalizedKey.length <= 5) {
            Regex("\\b${Regex.escape(normalizedKey)}\\b").containsMatchIn(text)
        } else {
            text.contains(normalizedKey)
        }
    }

    /** Renvoie le nom de preset reconnu, ou null. */
    fun recognize(ocrText: String): String? {
        // Les retours à la ligne de l'OCR coupent les mots-clés à plusieurs mots
        // (ex. "Super\nNintendo" ne contient pas "super nintendo") : on les aplatit en espaces,
        // de même pour les tirets (cf. [matchesKey]).
        val t = ocrText.lowercase().replace(Regex("[\\s-]+"), " ")
        for ((keys, name) in rules) {
            if (keys.any { matchesKey(t, it) }) return name
        }
        return null
    }

    /** Abréviations connues par nom de preset (ex. "PlayStation 2" -> ["ps2"]), pour la recherche manuelle. */
    private val abbreviationsByName: Map<String, List<String>> by lazy {
        rules.groupBy({ it.second }, { it.first })
            .mapValues { (_, lists) -> lists.flatten().filter { !it.contains(' ') && it.length <= 5 } }
    }

    // Compilées une seule fois (au lieu d'un `Regex(...)` recréé — donc recompilé — à chaque appel
    // de [normalize]) : cette fonction est appelée des centaines de fois par frappe dans
    // [exactAliasMatch] (une fois par alias connu), donc le coût de compilation d'un Pattern à
    // chaque fois se sentait nettement en tapant vite, en particulier en effaçant au Backspace
    // (champ "Console associée" du formulaire d'ajout/modification, ralenti à l'effacement).
    private val DIACRITICS_REGEX = Regex("\\p{Mn}+")
    private val APOSTROPHE_DASH_REGEX = Regex("['’-]")
    private val NON_ALNUM_REGEX = Regex("[^a-z0-9]+")

    /**
     * Normalise un texte pour la recherche floue : minuscules, accents retirés, apostrophes/tirets
     * fusionnés dans les mots adjacents (« spider-man » -> « spiderman », « marvel's » -> « marvels »),
     * et toute autre ponctuation (« : », etc.) traitée comme séparateur de mots.
     */
    private fun normalize(text: String): String {
        val lower = text.lowercase()
        val noDiacritics = java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
            .replace(DIACRITICS_REGEX, "")
        return noDiacritics.replace(APOSTROPHE_DASH_REGEX, "").replace(NON_ALNUM_REGEX, " ").trim()
    }

    private fun tokensOf(text: String): List<String> = normalize(text).split(' ').filter { it.isNotEmpty() }

    /** Table alias normalisé -> nom de preset normalisé, dérivée de [rules] (ex. "ps4" -> "playstation 4"). */
    private val aliasToCanonical: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        for ((keys, name) in rules) {
            val canonical = normalize(name)
            for (key in keys) map[normalize(key)] = canonical
        }
        map
    }

    /**
     * Mots-clés alias enregistrés par nom de preset (ex. "Wii U Édition The Wind Waker HD" ->
     * mots de "zelda wind waker wii u"). Une édition est parfois nommée d'après le sous-titre du
     * jeu sans la franchise ("The Wind Waker HD" ne contient pas "Zelda") : ces mots-clés servent
     * de texte de recherche supplémentaire pour qu'on retrouve quand même la fiche en tapant le
     * nom de la franchise/du personnage, même absent du nom officiel du produit.
     */
    private val aliasWordsByName: Map<String, List<String>> by lazy {
        rules.groupBy({ it.second }, { it.first }).mapValues { (_, lists) -> lists.flatten() }
    }

    /** Texte de recherche supplémentaire (mots-clés alias) pour [presetName], vide si aucun. */
    fun aliasSearchTerms(presetName: String): List<String> = aliasWordsByName[presetName] ?: emptyList()

    /**
     * Recherche floue multi-mots-clés : [query] est découpée en mots, et CHAQUE mot doit se
     * retrouver (ET logique) dans [haystack] — soit directement (préfixe d'un mot du texte), soit
     * via un alias connu (ex. "ps4" retrouve "PlayStation 4" même écrit en toutes lettres).
     * Insensible aux accents, tirets, apostrophes et à l'ordre des mots : couvre "ps4 pro",
     * "ps4 spiderman", ":édition", etc. Une [query] vide matche tout.
     */
    fun matchesQuery(haystack: List<String>, query: String): Boolean {
        val queryTokens = tokensOf(query)
        return matchScore(haystack, query) == queryTokens.size
    }

    private fun tokenMatches(haystackTokens: List<String>, qt: String): Boolean =
        haystackTokens.any { it == qt || it.startsWith(qt) } ||
            aliasToCanonical[qt]?.split(' ')?.all { ct -> haystackTokens.any { it == ct || it.startsWith(ct) } } == true

    /**
     * Nombre de mots-clés de [query] (sur le total de [query]) effectivement retrouvés dans
     * [haystack] — directement ou via alias, comme [matchesQuery]. Sert à classer des résultats
     * par pertinence quand aucun ne matche la requête en entier (ex. recherche web, où une
     * édition limitée précise n'a souvent pas de fiche dédiée) : on garde alors le résultat qui
     * couvre le plus de mots-clés, plutôt qu'un résultat sans aucun rapport.
     */
    fun matchScore(haystack: List<String>, query: String): Int {
        val haystackTokens = haystack.flatMap { tokensOf(it) }
        val queryTokens = tokensOf(query)
        if (queryTokens.isEmpty()) return 0
        return queryTokens.count { tokenMatches(haystackTokens, it) }
    }

    /**
     * Nombre de mots-clés normalisés de [haystack] — sert à départager plusieurs fiches qui
     * matchent toutes [matchesQuery] pour une même requête (ex. "ps4 pro" matche aussi bien
     * "PlayStation 4 Pro" que ses éditions limitées) : on garde la fiche la plus PROCHE de ce qui
     * a été tapé, donc celle avec le moins de mots-clés "en trop" (la moins spécifique des
     * fiches restantes, et non une édition limitée non demandée).
     */
    fun specificity(vararg haystack: String): Int = haystack.toList().flatMap { tokensOf(it) }.size

    /**
     * Renvoie le nom de preset dont [query] est EXACTEMENT une abréviation connue (ex. "ps5" ->
     * "PlayStation 5"), ou null sinon. Contrairement à [aliasMatches] (préfixe, pour une liste de
     * résultats de recherche), cette correspondance est stricte : utile pour l'auto-remplissage
     * en saisie manuelle, où l'utilisateur a fini de taper une abréviation précise.
     */
    /**
     * Réécrit chaque abréviation connue de [query] en son nom complet (« carry case n64 » →
     * « carry case nintendo 64 »). Nos alias sont résolus en interne par [matchesQuery]/[matchScore],
     * mais les moteurs de recherche EXTERNES (Wikipédia...) ne les connaissent pas : leur envoyer
     * aussi la requête développée fait remonter les pages qui n'emploient que le nom complet.
     * Renvoie la requête inchangée si aucun mot n'est une abréviation connue.
     */
    fun expandQueryAliases(query: String): String {
        val tokens = tokensOf(query)
        if (tokens.isEmpty()) return query
        return tokens.joinToString(" ") { aliasToCanonical[it] ?: it }
    }

    /**
     * Alias déjà normalisés une seule fois par nom de preset (dérivé de [abbreviationsByName]) :
     * évite de rappeler [normalize] — Normalizer + plusieurs Regex — sur chaque alias connu à
     * CHAQUE frappe dans [exactAliasMatch], ce qui ralentissait nettement la saisie/l'effacement.
     */
    private val normalizedAliasesByName: Map<String, List<String>> by lazy {
        abbreviationsByName.mapValues { (_, aliases) -> aliases.map { normalize(it) } }
    }

    fun exactAliasMatch(query: String): String? {
        // Normalisé des deux côtés : insensible casse/accents/tirets/espaces (« néo-géo », « NEO GEO »
        // ... trouvent le même alias), en s'appuyant sur le même dictionnaire d'abréviations.
        val q = normalize(query)
        if (q.isEmpty()) return null
        return normalizedAliasesByName.entries
            .firstOrNull { (_, aliases) -> aliases.any { it == q } }?.key
    }

    /**
     * Nom de preset canonique par forme normalisée, avec ET sans espaces (ex. « playstation 3 »
     * ET « playstation3 » -> « PlayStation 3 »), pour reconnaître aussi bien une abréviation
     * (« ps3 »), le nom complet correctement écrit (« PlayStation 3 ») que des variantes collées/
     * mal espacées (« PlayStation3 », « ps 3 »).
     */
    private val canonicalNameByNormalized: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        fun register(text: String, name: String) {
            val n = normalize(text)
            if (n.isEmpty()) return
            map[n] = name
            map[n.replace(" ", "")] = name
        }
        for ((keys, name) in rules) {
            keys.forEach { register(it, name) }
            register(name, name)
        }
        map
    }

    /**
     * Comme [canonicalize], mais renvoie null (au lieu de [raw] recadré) quand aucune variante
     * connue ne correspond — pour un appelant qui veut garder LUI-MÊME le texte brut d'origine
     * dans ce cas (ex. saisie live où on ne doit pas altérer un texte en cours de frappe qui ne
     * matche encore rien).
     */
    fun canonicalizeOrNull(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val norm = normalize(trimmed)
        return canonicalNameByNormalized[norm] ?: canonicalNameByNormalized[norm.replace(" ", "")]
    }

    /**
     * Ramène [raw] à son nom de preset canonique quand c'est une variante connue (abréviation ou
     * nom complet, quels que soient casse/accents/espaces — « ps3 », « PlayStation3 »,
     * « playstation 3 » donnent tous « PlayStation 3 »), pour que deux jeux de la même console ne
     * se retrouvent jamais sous deux intitulés différents (tri/regroupement par console, voir
     * [AppViewModel] et [Screens.groupConsecutiveBy]). Renvoie [raw] simplement recadré (trim) si
     * aucune variante connue ne correspond — jamais d'échec, juste pas de fusion possible.
     */
    fun canonicalize(raw: String): String = canonicalizeOrNull(raw) ?: raw.trim()

    /**
     * Applique [canonicalize] à chaque plateforme d'une valeur `platform` pouvant contenir
     * plusieurs consoles séparées par des virgules (ex. brut RAWG/IGDB "PS3, Xbox 360", ou une
     * sélection multi-plateformes de [PlatformPickerDialog]), en dédoublonnant les entrées qui se
     * retrouvent sur le même nom canonique (ex. "PS3, PlayStation 3" -> "PlayStation 3").
     */
    fun canonicalizePlatformList(raw: String): String =
        raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { canonicalize(it) }
            .distinct()
            .joinToString(", ")
}
