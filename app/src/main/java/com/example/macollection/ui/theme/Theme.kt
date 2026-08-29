package com.example.macollection.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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

// Palette claire (thème France) : fond quasi-blanc, cartes blanches, texte bleu marine — jamais
// de rouge sur du texte (voir [AppTheme.FRANCE]).
val LightBg = Color(0xFFF7F9FC)
val LightSurface = Color(0xFFFFFFFF)
val LightCardTop = Color(0xFFFFFFFF)
val LightCardBottom = Color(0xFFEFF3F8)
val LightOnBg = Color(0xFF11151C)
val LightOnSurfaceVariant = Color(0xFF5B6472)
val LightOutline = Color(0xFFD5DBE3)

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

private fun lightColors(primary: Color, onPrimary: Color, secondary: Color) = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    secondary = secondary,
    onSecondary = Color.White,
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightOnBg,
    surface = LightSurface,
    onSurface = LightOnBg,
    surfaceVariant = LightCardTop,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

/** Fond dégradé sombre du thème par défaut (conservé pour compat, préférer [themedGradient]). */
val ScreenGradient = Brush.verticalGradient(
    listOf(Color(0xFF15102B), Color(0xFF0B0B14), Color(0xFF120A1F))
)

/**
 * Contour lumineux des cartes : suit le thème actif (accent → bordure secondaire). Utilise
 * [AppTheme.borderAlt] plutôt que [AppTheme.accentAlt] : ce dernier alimente aussi
 * `colorScheme.secondary` (texte/icône de l'onglet sélectionné dans la barre de nav, voir
 * `navColors()` dans MainActivity), alors qu'une bordure de carte peut se permettre une couleur
 * (ex. le rouge du thème France) qui serait exclue du texte.
 * Getter @Composable pour que toutes les bordures de l'app changent avec le thème.
 */
val NeonBorder: Brush
    @Composable get() {
        val t = AppTheme.byId(AppPrefs.selectedTheme.value)
        return Brush.linearGradient(listOf(t.accent, t.borderAlt))
    }

/** Fond des cartes : clair ou sombre selon le thème actif (voir [AppTheme.isLight]). */
val CardGradient: Brush
    @Composable get() {
        val light = AppTheme.byId(AppPrefs.selectedTheme.value).isLight
        return if (light) Brush.verticalGradient(listOf(LightCardTop, LightCardBottom))
        else Brush.verticalGradient(listOf(CardTop, CardBottom))
    }

@Composable
fun MaCollectionTheme(content: @Composable () -> Unit) {
    val theme = AppTheme.byId(AppPrefs.selectedTheme.value)
    val colors = if (theme.isLight) lightColors(theme.accent, theme.onAccent, theme.accentAlt)
    else gamerColors(theme.accent, theme.onAccent, theme.accentAlt)
    MaterialTheme(
        colorScheme = colors,
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
    val navIcons: List<String> = emptyList(),
    /** Vrai pour un thème à fond clair (voir [lightColors]) — faux (fond sombre) par défaut. */
    val isLight: Boolean = false,
    /**
     * Couleur secondaire du dégradé de bordure des cartes ([NeonBorder]) — distincte
     * d'[accentAlt] (qui alimente aussi `colorScheme.secondary`, donc le texte/icône de l'onglet
     * de nav sélectionné) pour permettre une couleur de bordure exclue du texte, ex. le rouge du
     * thème [FRANCE]. Vaut [accentAlt] par défaut (comportement inchangé pour les autres thèmes).
     */
    val borderAlt: Color = accentAlt
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
    ),

    /**
     * Thème clair aux couleurs du drapeau français — gratuit dès l'installation (voir
     * MainActivity.showThemeDialog), pas un skin à débloquer. Le rouge (Pantone 032 C, #EF4135)
     * n'alimente QUE [borderAlt] (bordure décorative des cartes) : jamais [accentAlt] (qui
     * alimenterait `colorScheme.secondary`, donc du texte), conformément à la demande "pas
     * d'écriture en rouge".
     */
    FRANCE(
        "light_france", "France", "🇫🇷",
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFEAF1FB), Color(0xFFFFFFFF))),
        Color(0xFF0055A4), Color.White, Color(0xFF002654),
        isLight = true,
        borderAlt = Color(0xFFEF4135)
    ),

    /**
     * Même famille que [FRANCE] : un thème clair par langue disponible dans l'appli (voir
     * `values-de/strings.xml`), aux couleurs du drapeau du pays associé. Même règle stricte :
     * quand le drapeau contient du rouge, il n'alimente QUE [borderAlt] (bordure décorative),
     * jamais [accent]/[accentAlt] (qui alimentent respectivement les boutons/FAB et le texte/icône
     * de l'onglet de nav sélectionné) — donc jamais de texte rouge. Quand le drapeau n'a pas
     * d'autre couleur que rouge/blanc (JAPAN, TURKEY), [accent]/[accentAlt] retombent sur un
     * neutre sombre plutôt que de forcer le rouge en couleur "safe".
     */
    GERMANY(
        "light_germany", "Allemagne", "🇩🇪",
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFBF3D9), Color(0xFFFFFFFF))),
        Color(0xFFFFCC00), Color.Black, Color(0xFF1A1A1A),
        isLight = true,
        borderAlt = Color(0xFFDD0000)
    ),
    GREECE(
        "light_greece", "Grèce", "🇬🇷",
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE9F3FB), Color(0xFFFFFFFF))),
        Color(0xFF0D5EAF), Color.White, Color(0xFF063970),
        isLight = true,
        borderAlt = Color(0xFF66A9E0)
    ),
    UK(
        "light_uk", "Royaume-Uni", "🇬🇧",
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE8ECFB), Color(0xFFFFFFFF))),
        Color(0xFF012169), Color.White, Color(0xFF1D3461),
        isLight = true,
        borderAlt = Color(0xFFC8102E)
    ),
    SPAIN(
        "light_spain", "Espagne", "🇪🇸",
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFFF6E0), Color(0xFFFFFFFF))),
        Color(0xFFF1BF00), Color.Black, Color(0xFF7A5900),
        isLight = true,
        borderAlt = Color(0xFFAA151B)
    ),
    ITALY(
        "light_italy", "Italie", "🇮🇹",
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE8F5EC), Color(0xFFFFFFFF))),
        Color(0xFF008C45), Color.White, Color(0xFF00572D),
        isLight = true,
        borderAlt = Color(0xFFCD212A)
    ),
    JAPAN(
        "light_japan", "Japon", "🇯🇵",
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5), Color(0xFFFFFFFF))),
        Color(0xFF1A1A1A), Color.White, Color(0xFF333333),
        isLight = true,
        borderAlt = Color(0xFFBC002D)
    ),
    PORTUGAL(
        "light_portugal", "Portugal", "🇵🇹",
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE8F5EC), Color(0xFFFFFFFF))),
        Color(0xFF046A38), Color.White, Color(0xFF00401F),
        isLight = true,
        borderAlt = Color(0xFFDA020E)
    ),
    RUSSIA(
        "light_russia", "Russie", "🇷🇺",
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE8ECFB), Color(0xFFFFFFFF))),
        Color(0xFF0039A6), Color.White, Color(0xFF002157),
        isLight = true,
        borderAlt = Color(0xFFD52B1E)
    ),
    TURKEY(
        "light_turkey", "Turquie", "🇹🇷",
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5), Color(0xFFFFFFFF))),
        Color(0xFF262626), Color.White, Color(0xFF3D3D3D),
        isLight = true,
        borderAlt = Color(0xFFE30A17)
    ),
    CHINA(
        "light_china", "Chine", "🇨🇳",
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFFF6E0), Color(0xFFFFFFFF))),
        Color(0xFFFFDE00), Color.Black, Color(0xFF6B5200),
        isLight = true,
        borderAlt = Color(0xFFDE2910)
    );

    companion object {
        fun byId(id: String?): AppTheme = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

/** Dégradé de fond du thème actif — à utiliser à la place de [ScreenGradient] dans les composables. */
@Composable
fun themedGradient(): Brush = AppTheme.byId(AppPrefs.selectedTheme.value).gradient
