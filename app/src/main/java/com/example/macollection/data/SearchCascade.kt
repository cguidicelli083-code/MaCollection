package com.example.macollection.data

/**
 * Orchestrateur générique de recherche en cascade, utilisé par les recherches manuelles
 * jeu/console/accessoire (`GameSearchDialog`, `OnlinePresetSearchDialog`) : essai exact tel que
 * tapé, puis nettoyé (mentions d'édition), puis traduit en anglais — et, en tout dernier recours,
 * mot final retiré progressivement du texte le plus à jour (traduit si disponible, sinon nettoyé).
 * S'arrête au premier niveau qui renvoie un résultat jugé suffisant (cf. [isSufficient]).
 */
object SearchCascade {

    enum class Stage { EXACT, CLEANED, TRANSLATED }

    data class Outcome<T>(val items: List<T>, val stage: Stage, val query: String)

    suspend fun <T> run(
        rawQuery: String,
        cleaningCandidates: (String) -> List<String>,
        translate: suspend (String) -> String?,
        onStageStart: (Stage) -> Unit = {},
        // Par défaut, un résultat non vide suffit à arrêter la cascade (cas des consoles/
        // accessoires, où Wikipédia est déjà la source de référence). Les jeux fournissent un
        // critère plus strict (cf. GameSearchDialog) : un résultat Wikipédia/Wikidata seul ne
        // renseigne jamais plateforme/genre, donc "non vide" n'y suffit pas à juger la cascade
        // terminée — voir le paramètre `isSufficient` transmis par l'appelant.
        isSufficient: (List<T>) -> Boolean = { it.isNotEmpty() },
        // Dernier recours, essayé uniquement si EXACT/CLEANED/TRANSLATED ont tous échoué : reçoit
        // le texte traduit (ou, à défaut, le texte nettoyé) et renvoie des variantes progressivement
        // raccourcies à tenter. Volontairement PAS appliqué au texte français d'origine : un nom de
        // franchise à la graphie identique dans les deux langues (ex. "Harry Potter") ferait sinon
        // remonter n'importe quel épisode de la saga au lieu du bon dès qu'on tronque le texte
        // français avant traduction (cf. le bug "Harry Potter et la Coupe de Feu" -> "Order of the
        // Phoenix"). Par défaut aucune variante (comportement inchangé pour les consoles/accessoires,
        // qui gèrent déjà leur propre raccourcissement dans [presetCleaningCandidates]).
        shortenedCandidates: (String) -> List<String> = { emptyList() },
        search: suspend (String) -> List<T>
    ): Outcome<T> {
        // Meilleur résultat non vide rencontré jusqu'ici mais jugé insuffisant : sert de repli si
        // AUCUNE étape ne satisfait jamais [isSufficient] (mieux vaut un résultat incomplet
        // qu'un écran vide).
        var fallback: Outcome<T>? = null
        fun remember(items: List<T>, stage: Stage, query: String) {
            if (fallback == null && items.isNotEmpty()) fallback = Outcome(items, stage, query)
        }

        val trimmed = rawQuery.trim()
        onStageStart(Stage.EXACT)
        val exact = search(trimmed)
        if (isSufficient(exact)) return Outcome(exact, Stage.EXACT, trimmed)
        remember(exact, Stage.EXACT, trimmed)

        val candidates = cleaningCandidates(trimmed).filter { it.isNotBlank() && it != trimmed }.distinct()
        if (candidates.isNotEmpty()) onStageStart(Stage.CLEANED)
        for (candidate in candidates) {
            val cleaned = search(candidate)
            if (isSufficient(cleaned)) return Outcome(cleaned, Stage.CLEANED, candidate)
            remember(cleaned, Stage.CLEANED, candidate)
        }

        val base = candidates.firstOrNull() ?: trimmed
        onStageStart(Stage.TRANSLATED)
        val translated = translate(base)?.takeIf { it.isNotBlank() && it != base }
        if (translated != null) {
            val result = search(translated)
            if (isSufficient(result)) return Outcome(result, Stage.TRANSLATED, translated)
            remember(result, Stage.TRANSLATED, translated)
        }

        for (shortCandidate in shortenedCandidates(translated ?: base)) {
            val result = search(shortCandidate)
            if (isSufficient(result)) return Outcome(result, Stage.TRANSLATED, shortCandidate)
            remember(result, Stage.TRANSLATED, shortCandidate)
        }

        return fallback ?: Outcome(emptyList(), Stage.TRANSLATED, translated ?: base)
    }

    /**
     * Nettoyage pour les JEUX : réutilise le nettoyeur existant du scan code-barres (mentions
     * d'édition, bruit d'annonce, ET marques/consoles — sans risque ici car la console est déjà un
     * champ séparé du formulaire jeu). Pas de raccourcissement progressif ici (contrairement à
     * avant) : voir [progressiveShortenings], désormais réservé au tout dernier recours de [run]
     * (texte déjà dans la bonne langue), pour éviter de tronquer un titre français avant traduction.
     */
    fun gameCleaningCandidates(raw: String): List<String> {
        val base = ScanTools.cleanListingTitle(raw).ifBlank { raw }
        return listOf(base)
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
    fun progressiveShortenings(base: String): List<String> {
        val words = base.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size <= 2) return emptyList()
        return (words.size - 1 downTo 2).map { n -> words.take(n).joinToString(" ") }
    }
}
