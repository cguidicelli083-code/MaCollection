package com.example.macollection.data

/**
 * Orchestrateur générique de recherche en cascade à 3 niveaux, utilisé par les recherches
 * manuelles jeu/console/accessoire (`GameSearchDialog`, `OnlinePresetSearchDialog`) : essai exact
 * tel que tapé, puis nettoyé (mentions d'édition, mot final retiré progressivement), puis traduit
 * en anglais. S'arrête au premier niveau qui renvoie un résultat non vide.
 */
object SearchCascade {

    enum class Stage { EXACT, CLEANED, TRANSLATED }

    data class Outcome<T>(val items: List<T>, val stage: Stage, val query: String)

    suspend fun <T> run(
        rawQuery: String,
        cleaningCandidates: (String) -> List<String>,
        translate: suspend (String) -> String?,
        onStageStart: (Stage) -> Unit = {},
        search: suspend (String) -> List<T>
    ): Outcome<T> {
        val trimmed = rawQuery.trim()
        onStageStart(Stage.EXACT)
        search(trimmed).takeIf { it.isNotEmpty() }?.let { return Outcome(it, Stage.EXACT, trimmed) }

        val candidates = cleaningCandidates(trimmed).filter { it.isNotBlank() && it != trimmed }.distinct()
        if (candidates.isNotEmpty()) onStageStart(Stage.CLEANED)
        for (candidate in candidates) {
            search(candidate).takeIf { it.isNotEmpty() }?.let { return Outcome(it, Stage.CLEANED, candidate) }
        }

        val base = candidates.firstOrNull() ?: trimmed
        onStageStart(Stage.TRANSLATED)
        val translated = translate(base)?.takeIf { it.isNotBlank() && it != base }
        if (translated != null) {
            search(translated).takeIf { it.isNotEmpty() }?.let { return Outcome(it, Stage.TRANSLATED, translated) }
        }
        return Outcome(emptyList(), Stage.TRANSLATED, translated ?: base)
    }

    /**
     * Nettoyage AGRESSIF pour les JEUX : réutilise le nettoyeur existant du scan code-barres
     * (mentions d'édition, bruit d'annonce, ET marques/consoles) — sans risque ici car la console
     * est déjà un champ séparé du formulaire jeu.
     */
    fun gameCleaningCandidates(raw: String): List<String> {
        val base = ScanTools.cleanListingTitle(raw).ifBlank { raw }
        return listOf(base) + progressiveShortenings(base)
    }

    /**
     * Nettoyage LÉGER pour CONSOLE/ACCESSOIRE : mentions d'édition seulement — surtout PAS le
     * nettoyeur de [gameCleaningCandidates], qui supprimerait des mots comme "Switch"/"Nintendo"
     * (redondants pour un jeu car la console est un champ à part, mais ce sont ici le sujet même
     * de la recherche).
     */
    fun presetCleaningCandidates(raw: String): List<String> {
        val base = GameCatalog.stripEditionKeywords(raw).ifBlank { raw }
        return listOf(base) + progressiveShortenings(base)
    }

    // Retire progressivement le dernier mot (jusqu'à 2 mots minimum) : "Sonic Colours Ultimate
    // Steelbook" -> "Sonic Colours Ultimate" -> "Sonic Colours", même principe que la réduction
    // progressive déjà utilisée par ScanTools.firstGameMatchScored pour le scan code-barres.
    private fun progressiveShortenings(base: String): List<String> {
        val words = base.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size <= 2) return emptyList()
        return (words.size - 1 downTo 2).map { n -> words.take(n).joinToString(" ") }
    }
}
