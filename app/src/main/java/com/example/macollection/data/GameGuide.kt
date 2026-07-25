package com.example.macollection.data

/**
 * Explications « comment jouer » de chaque mini-jeu, affichées une seule fois au premier lancement
 * (voir [AppPrefs.gameIntrosSeen]). Textes en français, cohérents avec le reste de l'UI des jeux.
 */
object GameGuide {

    data class Guide(val emoji: String, val title: String, val rules: List<String>)

    val byId: Map<String, Guide> = mapOf(
        GameShopCatalog.QUIZ to Guide(
            "❓", "Quiz rétro",
            listOf(
                "Réponds aux questions sur les consoles, jeux et accessoires rétro (10 questions par session).",
                "Choisis la bonne réponse parmi les propositions ; chaque bonne réponse rapporte 2 points.",
                "Obtiens 8 bonnes réponses ou plus pour débloquer le niveau suivant : 10 niveaux, du Débutant à l'Expert ultime (plus dur = questions plus pointues)."
            )
        ),
        GameShopCatalog.PONG to Guide(
            "🏓", "Tennis Rétro",
            listOf(
                "Glisse ton doigt pour déplacer ta raquette (en bas) : de gauche à droite, et aussi en avançant/reculant.",
                "Renvoie la balle vers l'adversaire. Un déplacement latéral au moment de l'impact donne de l'effet (lift).",
                "Premier arrivé à 10 points gagne ; si tu rates la balle, l'adversaire marque.",
                "Après chaque point, une pause d'une seconde puis « GO ! » avant que la balle ne soit remise en jeu."
            )
        ),
        GameShopCatalog.ARKANOID to Guide(
            "🧱", "Casse-Briques",
            listOf(
                "Glisse pour déplacer la raquette et renvoyer la balle.",
                "Casse toutes les briques pour passer au niveau suivant.",
                "Attrape les capsules bonus (raquette large, laser, multi-balles, vie…). Tu as 3 vies."
            )
        ),
        GameShopCatalog.SPACE_INVADERS to Guide(
            "👾", "Invasion",
            listOf(
                "Glisse pour déplacer le canon ; il tire tout seul.",
                "Détruis tous les envahisseurs avant qu'ils n'atteignent le bas.",
                "Tous les 5 niveaux, un boss à désintégrer bloc par bloc. Tu as 3 vies."
            )
        ),
        GameShopCatalog.CENTIPEDE to Guide(
            "🐛", "Mille-Pattes",
            listOf(
                "Glisse pour déplacer ton vaisseau ; tape l'écran pour tirer.",
                "Détruis le mille-pattes qui descend ; tu peux monter d'un ou deux crans pour l'éviter.",
                "S'il atteint le bas, il remonte. Tu as 3 vies."
            )
        ),
        GameShopCatalog.PACMAN to Guide(
            "🟡", "Glouton",
            listOf(
                "Glisse dans une direction pour orienter le glouton aux intersections.",
                "Mange toutes les pastilles du labyrinthe pour finir le niveau.",
                "Les grosses pastilles rendent les fantômes vulnérables quelques secondes : mange-les pour des points, sinon ils te coûtent une vie. Le labyrinthe change de forme à chaque niveau."
            )
        ),
        GameShopCatalog.TETRIS to Guide(
            "🟦", "Blocs",
            listOf(
                "Glisse à gauche/droite pour déplacer la pièce, vers le bas pour accélérer sa chute.",
                "Tape l'écran pour faire pivoter la pièce.",
                "Complète des lignes pleines pour les effacer et marquer. La partie finit quand les pièces atteignent le haut."
            )
        ),
        GameShopCatalog.SNAKE to Guide(
            "🐍", "Serpent",
            listOf(
                "Glisse dans une direction pour orienter le serpent (impossible de faire demi-tour).",
                "Mange les pommes pour grandir et accélérer.",
                "Évite les murs (bordure néon) et ton propre corps."
            )
        ),
        GameShopCatalog.SIMON to Guide(
            "🎨", "Mémo Couleurs",
            listOf(
                "Observe et écoute la séquence de couleurs qui s'illumine.",
                "Reproduis-la dans le bon ordre en tapant les couleurs.",
                "La séquence s'allonge à chaque tour ; chaque couleur a son propre son (mémoire auditive)."
            )
        ),
        GameShopCatalog.FROGGER to Guide(
            "🐸", "Grenouille",
            listOf(
                "Tape l'écran pour avancer d'une case ; double-tape pour reculer.",
                "Traverse de bas en haut en évitant les voitures des voies mobiles.",
                "Atteins le haut pour réussir une traversée : ça accélère, et le parcours change dès la 4ᵉ traversée. Tu as 3 vies."
            )
        )
    )

    fun forId(id: String): Guide? = byId[id]
}
