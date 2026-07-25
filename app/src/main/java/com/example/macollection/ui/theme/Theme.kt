package com.example.macollection.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.macollection.data.AppPrefs

// Palette « gamer » néon sur fond sombre (thème par défaut).
val NeonPurple = Color(0xFF8B5CFF)
val NeonCyan = Color(0xFF22E6FF)
val NeonPink = Color(0xFFFF3D9A)
val DeepBg = Color(0xFF0B0B14)
val SurfaceBg = Color(0xFF15151F)
val CardTop = Color(0xFF20203A)
val CardBottom = Color(0xFF16161F)

private fun gamerColors(primary: Color, onPrimary: Color, secondary: Color) = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    secondary = secondary,
    onSecondary = Color(0xFF00131A),
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = DeepBg,
    onBackground = Color(0xFFECECF7),
    surface = SurfaceBg,
    onSurface = Color(0xFFECECF7),
    surfaceVariant = CardTop,
    onSurfaceVariant = Color(0xFFB6B6CC),
    outline = Color(0xFF3A3A55)
)

/** Fond dégradé sombre du thème par défaut (conservé pour compat, préférer [themedGradient]). */
val ScreenGradient = Brush.verticalGradient(
    listOf(Color(0xFF15102B), Color(0xFF0B0B14), Color(0xFF120A1F))
)

/**
 * Contour lumineux des cartes : suit le thème actif (accent → accent secondaire).
 * Getter @Composable pour que toutes les bordures de l'app changent avec le thème.
 */
val NeonBorder: Brush
    @Composable get() {
        val t = AppTheme.byId(AppPrefs.selectedTheme.value)
        return Brush.linearGradient(listOf(t.accent, t.accentAlt))
    }

val CardGradient = Brush.verticalGradient(listOf(CardTop, CardBottom))

@Composable
fun MaCollectionTheme(content: @Composable () -> Unit) {
    val theme = AppTheme.byId(AppPrefs.selectedTheme.value)
    MaterialTheme(
        colorScheme = gamerColors(theme.accent, theme.onAccent, theme.accentAlt),
        typography = Typography(),
        content = content
    )
}

/**
 * Thèmes visuels « films des années 80 » déblocables en boutique (skins). Chaque thème
 * redéfinit le dégradé de fond, la couleur d'accent (boutons/sélections), l'accent secondaire
 * (bordures lumineuses, scores) et un emoji signature affiché dans le sélecteur.
 * DEFAULT reprend la palette néon d'origine ; l'id des autres correspond à l'id boutique du
 * skin (voir GameShopCatalog). Couleurs/emoji évocateurs uniquement — aucun asset de film.
 */
enum class AppTheme(
    val id: String,
    val label: String,
    val emoji: String,
    val gradient: Brush,
    val accent: Color,
    val onAccent: Color,
    val accentAlt: Color,
    /**
     * Icônes emoji des onglets de navigation, dans l'ordre :
     * [Collection, Souhaits, Encyclopédie, Total, Jeux]. Vide pour DEFAULT (icônes Material
     * d'origine). Emoji standards uniquement — pas d'images de films (droits d'auteur/marques).
     */
    val navIcons: List<String> = emptyList()
) {
    DEFAULT(
        "default", "Néon (défaut)", "🕹️",
        Brush.verticalGradient(listOf(Color(0xFF15102B), Color(0xFF0B0B14), Color(0xFF120A1F))),
        NeonPurple, Color.White, NeonCyan
    ),

    /** DeLorean lancée dans la nuit : trainées de feu ROUGES et lueur BLEUE du voyage temporel. */
    BACK_TO_FUTURE(
        "skin_bttf", "Voyage temporel", "⚡🛹",
        Brush.verticalGradient(listOf(Color(0xFF0A1E4D), Color(0xFF080611), Color(0xFF360A0A))),
        Color(0xFFE5202A), Color.White, Color(0xFF2E7DFF),
        listOf("@delorean", "⚡", "📔", "⏱️", "🛹")
    ),

    /** Espace noir infini, jaune légendaire du générique et éclats de sabre bleu. */
    STAR_WARS(
        "skin_starwars", "Galaxie", "🌠⚔️",
        Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF04060E), Color(0xFF0A1024))),
        Color(0xFFFFE81F), Color.Black, Color(0xFF4FC3FF),
        listOf("⚔️", "🪐", "🌌", "⭐", "🚀")
    ),

    /** Grotte au trésor : or des doublons et vert lagon de la crique. */
    GOONIES(
        "skin_goonies", "Chasse au trésor", "🗺️💰",
        Brush.verticalGradient(listOf(Color(0xFF0B2E2B), Color(0xFF0E0A04), Color(0xFF2E1F0A))),
        Color(0xFFE8B84B), Color.Black, Color(0xFF2FD4A8),
        listOf("💰", "🗺️", "📖", "💎", "🏴‍☠️")
    ),

    /** Nuit new-yorkaise, vert ectoplasme et rouge d'alerte. */
    GHOSTBUSTERS(
        "skin_ghostbusters", "Ectoplasme", "👻⚡",
        Brush.verticalGradient(listOf(Color(0xFF12161C), Color(0xFF0A0F0A), Color(0xFF1A2410))),
        Color(0xFF95E020), Color.Black, Color(0xFFFF3B3B),
        listOf("👻", "🔮", "📖", "⚡", "🕹️")
    );

    companion object {
        fun byId(id: String?): AppTheme = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

/** Dégradé de fond du thème actif — à utiliser à la place de [ScreenGradient] dans les composables. */
@Composable
fun themedGradient(): Brush = AppTheme.byId(AppPrefs.selectedTheme.value).gradient
