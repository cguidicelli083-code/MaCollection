package com.example.macollection.data

/**
 * Question de culture générale « créateur/inventeur » pour le quiz difficile.
 * [consoleLabel] est le libellé affiché dans la question, [creator] la bonne réponse.
 */
data class CreatorFact(
    val consoleLabel: String,
    val creator: String
)

/**
 * Faits « qui a créé quoi » VÉRIFIÉS (Wikipedia / Smithsonian / Computer History Museum,
 * revérifiés le 2026-07-02) — n'ajouter ici que des faits contrôlés à la source, jamais de
 * mémoire : ce quiz s'appuie sur la réputation d'exactitude de l'encyclopédie.
 */
object ConsoleTrivia {

    val creatorFacts: List<CreatorFact> = listOf(
        CreatorFact("la Magnavox Odyssey (première console de salon)", "Ralph Baer"),
        CreatorFact("le Game Boy et les Game & Watch (Nintendo)", "Gunpei Yokoi"),
        CreatorFact("la PlayStation (Sony)", "Ken Kutaragi"),
        CreatorFact("la Fairchild Channel F (première console à cartouches)", "Jerry Lawson"),
        CreatorFact("la NES/Famicom et la Super NES (architecte en chef)", "Masayuki Uemura"),
        CreatorFact("Atari et le jeu Pong (fondateur)", "Nolan Bushnell")
    )

    /**
     * Personnalités réelles de l'industrie utilisées comme mauvaises réponses (leurres) :
     * tous des noms authentiques, jamais de personnes inventées.
     */
    val distractorPeople: List<String> = listOf(
        "Ralph Baer", "Gunpei Yokoi", "Ken Kutaragi", "Jerry Lawson",
        "Masayuki Uemura", "Nolan Bushnell", "Shigeru Miyamoto", "Hiroshi Yamauchi"
    )
}
