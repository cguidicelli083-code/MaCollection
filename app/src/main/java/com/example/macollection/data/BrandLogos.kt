package com.example.macollection.data

/**
 * Logos de marques (Wikimedia Commons) affichés en en-tête de groupe quand l'encyclopédie est
 * triée par marque. Seules les marques dont l'URL a été vérifiée (HTTP 200) sont incluses ; les
 * autres marques s'affichent simplement avec leur nom en texte (pas de logo fictif).
 */
object BrandLogos {
    private val urls: Map<String, String> = mapOf(
        "Nintendo" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Nintendo.svg/330px-Nintendo.svg.png",
        "Sega" to "https://upload.wikimedia.org/wikipedia/commons/thumb/1/13/SEGA_logo.svg/330px-SEGA_logo.svg.png",
        "Sony" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Sony_logo.svg/330px-Sony_logo.svg.png",
        "Microsoft" to "https://upload.wikimedia.org/wikipedia/commons/thumb/9/96/Microsoft_logo_%282012%29.svg/330px-Microsoft_logo_%282012%29.svg.png",
        "Atari" to "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3b/Atari_logo.svg/330px-Atari_logo.svg.png",
        "SNK" to "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/SNK_logo.svg/330px-SNK_logo.svg.png",
        "NEC" to "https://upload.wikimedia.org/wikipedia/commons/thumb/9/96/NEC_logo.svg/330px-NEC_logo.svg.png",
        "Sharp" to "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/Logo_of_the_Sharp_Corporation.svg/330px-Logo_of_the_Sharp_Corporation.svg.png",
        "Casio" to "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Casio_logo.svg/330px-Casio_logo.svg.png",
        "Amstrad" to "https://upload.wikimedia.org/wikipedia/commons/thumb/0/06/Amstrad_logo.svg/330px-Amstrad_logo.svg.png",
        "Panasonic" to "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/Panasonic_logo_%28Blue%29.svg/330px-Panasonic_logo_%28Blue%29.svg.png"
    )

    fun urlFor(brand: String): String? = urls[brand]
}
