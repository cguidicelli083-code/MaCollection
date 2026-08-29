@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.macollection.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.macollection.R
import com.example.macollection.data.AccessoryImages
import com.example.macollection.data.AccessoryPreset
import com.example.macollection.data.BrandLogos
import com.example.macollection.data.CachedGame
import com.example.macollection.data.ConsoleColorSupport
import com.example.macollection.data.ConsoleRecognition
import com.example.macollection.data.ConsoleImages
import com.example.macollection.data.ConsolePlatforms
import com.example.macollection.data.ConsolePreset
import com.example.macollection.data.CuratedGames
import com.example.macollection.data.CustomPreset
import com.example.macollection.data.EbayPrices
import com.example.macollection.data.GameCatalog
import com.example.macollection.data.GameCatalogSyncState
import com.example.macollection.data.GameInfo
import com.example.macollection.data.ItemType
import com.example.macollection.data.LeBonCoinLink
import com.example.macollection.data.Offer
import com.example.macollection.data.VintedLink
import com.example.macollection.data.accessoryPresets
import com.example.macollection.data.consolePresets
import com.example.macollection.data.toAccessoryPreset
import com.example.macollection.data.toConsolePreset
import kotlinx.coroutines.launch
import com.example.macollection.ui.theme.CardGradient
import com.example.macollection.ui.theme.NeonBorder
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPurple
import com.example.macollection.ui.theme.themedGradient

// ---------------------------------------------------------------------------
// Encyclopédie : liste de toutes les consoles
// ---------------------------------------------------------------------------

enum class EncycloMode { CONSOLES, ACCESSORIES }

/** Critère de tri de la liste des jeux d'une console (onglet Jeux de l'Encyclopédie). */
enum class GameSortOption { NAME, RELEASE_DATE, RELEASE_DATE_DESC, PRICE, DEVELOPER }

@Composable
private fun gameSortLabel(o: GameSortOption): String = when (o) {
    GameSortOption.NAME -> stringResource(R.string.sort_name)
    GameSortOption.RELEASE_DATE -> stringResource(R.string.sort_release)
    GameSortOption.RELEASE_DATE_DESC -> stringResource(R.string.sort_release_desc)
    GameSortOption.PRICE -> stringResource(R.string.sort_price_desc)
    GameSortOption.DEVELOPER -> stringResource(R.string.sort_developer)
}

/** Critère de tri de la liste des consoles dans l'encyclopédie. */
enum class EncycloSortOption { NAME, BRAND, RELEASE_DATE, RELEASE_DATE_DESC }

/** Libellé lisible d'un critère de tri de l'encyclopédie. */
@Composable
private fun encycloSortLabel(o: EncycloSortOption): String = when (o) {
    EncycloSortOption.NAME -> stringResource(R.string.sort_name)
    EncycloSortOption.BRAND -> stringResource(R.string.sort_brand)
    EncycloSortOption.RELEASE_DATE -> stringResource(R.string.sort_release)
    EncycloSortOption.RELEASE_DATE_DESC -> stringResource(R.string.sort_release_desc)
}

/**
 * Filtre par type de console dans l'encyclopédie (salon / portable / ordinateur), basé sur
 * [ConsolePreset.kind] ("Salon", "Portable" ou "Ordinateur"). ALL n'applique aucun filtre.
 */
enum class EncycloKindFilter(val kind: String?) {
    ALL(null), SALON("Salon"), PORTABLE("Portable"), ORDINATEUR("Ordinateur")
}

/** Libellé lisible d'un filtre de type de console. */
@Composable
private fun encycloKindFilterLabel(f: EncycloKindFilter): String = when (f) {
    EncycloKindFilter.ALL -> stringResource(R.string.filter_kind_all)
    EncycloKindFilter.SALON -> stringResource(R.string.filter_kind_salon)
    EncycloKindFilter.PORTABLE -> stringResource(R.string.filter_kind_portable)
    EncycloKindFilter.ORDINATEUR -> stringResource(R.string.filter_kind_computer)
}

/**
 * Associe une fiche à sa fiche perso d'origine ([CustomPreset], non null seulement si la ligne
 * vient d'une fiche ajoutée par l'utilisateur). Sert à savoir EXACTEMENT quelle ligne a été
 * tapée sans avoir à la retrouver par nom (voir explication sur [EncyclopediaScreen]).
 */
private data class ConsoleEntry(val preset: ConsolePreset, val custom: CustomPreset?)
private data class AccessoryEntry(val preset: AccessoryPreset, val custom: CustomPreset?)

/**
 * Clé stable d'une fiche de catalogue (console/accessoire) : "cat_marque_nom" pour une fiche
 * native, "custom_id" pour une fiche perso (id propre, insensible à un renommage). Utilisée pour
 * la sélection multiple (voir [EncyclopediaScreen]) et pour le cache de cote IA
 * ([com.example.macollection.ui.AppViewModel.cachedPresetPrice]) — [ConsolePreset]/[AccessoryPreset]
 * n'ayant pas d'identifiant propre.
 */
fun presetCacheKey(brand: String, name: String, customId: Long?): String =
    customId?.let { "custom_$it" } ?: "cat_${brand}_${name}"
private fun ConsoleEntry.key(): String = presetCacheKey(preset.brand, preset.name, custom?.id)
private fun AccessoryEntry.key(): String = presetCacheKey(preset.brand, preset.name, custom?.id)

/**
 * Annonces structurées pour la section "Offres" d'une fiche. eBay est la seule marketplace avec
 * une vraie API accessible depuis l'app (client ID/secret, voir [EbayPrices]) — LeBonCoin et
 * Vinted n'ont pas d'API publique officielle : leurs API internes ont été essayées mais se sont
 * révélées bloquées (LeBonCoin, mur CAPTCHA Datadome dès la 1re requête) ou non fiables (Vinted,
 * jeton d'authentification requis que seul le site web sait produire via JavaScript) — voir
 * [LeBonCoinLink] et [VintedLink] pour le repli "lien de recherche" retenu à la place pour ces
 * deux sites.
 */
private suspend fun fetchAllOffers(type: ItemType, brand: String, name: String, platform: String?): List<Offer> =
    runCatching { EbayPrices.offersFor(type, brand, name, platform) }.getOrDefault(emptyList())

@Composable
fun EncyclopediaScreen(
    vm: AppViewModel,
    mode: EncycloMode,
    onModeChange: (EncycloMode) -> Unit,
    onOpenConsole: (ConsolePreset, CustomPreset?) -> Unit,
    onAddAccessory: (AccessoryPreset, Boolean) -> Unit,
    onEditPreset: (CustomPreset) -> Unit = {},
    modifier: Modifier = Modifier,
    // Hissé par l'appelant : ouvrir la fiche plein écran d'une console retire cet écran de la
    // composition, donc sans cet état externe la liste reviendrait en haut à chaque retour.
    consoleListState: LazyListState = rememberLazyListState(),
    // Sélection multiple (bouton dédié dans la barre du haut, voir MainActivity) : quand actif,
    // taper une fiche la coche au lieu de l'ouvrir ; un bandeau en bas permet de tout ajouter
    // d'un coup à la collection ou aux souhaits, consoles et accessoires confondus.
    selectionMode: Boolean = false,
    onAddSelectedToCollection: (List<ConsolePreset>, List<AccessoryPreset>, Boolean) -> Unit = { _, _, _ -> },
    // Ex-icône « sélection multiple » de la barre du haut (voir MainActivity) : déplacée ici, à
    // côté des boutons Consoles/Accessoires, sur demande utilisateur (2026-07-26). L'icône Actus
    // reste dans la barre du haut de MainActivity (visible sur tous les onglets, pas seulement ici).
    onToggleSelectionMode: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(EncycloSortOption.NAME) }
    var kindFilter by remember { mutableStateOf(EncycloKindFilter.ALL) }
    var selectedAccessory by remember { mutableStateOf<AccessoryEntry?>(null) }
    val customPresets by vm.customPresets.collectAsState()
    val photoOverrides by vm.photoOverrides.collectAsState()
    var selectedConsoleKeys by remember { mutableStateOf(setOf<String>()) }
    var selectedAccessoryKeys by remember { mutableStateOf(setOf<String>()) }
    LaunchedEffect(selectionMode) {
        if (!selectionMode) { selectedConsoleKeys = emptySet(); selectedAccessoryKeys = emptySet() }
    }

    // Les fiches ajoutées par l'utilisateur sont converties en fiches de catalogue et fondues
    // directement dans les listes natives : aucune séparation visuelle ni logique (plus d'onglet
    // « Mes ajouts »). Elles se trient, s'ouvrent et acceptent des photos exactement comme les
    // fiches intégrées. On garde toutefois la référence exacte à la fiche perso d'origine
    // (ConsoleEntry.custom / AccessoryEntry.custom) : si une fiche perso partage le même nom
    // qu'une fiche native, on doit savoir laquelle des deux lignes a été tapée (pas la retrouver
    // par nom ensuite, ce qui associerait les boutons Modifier/Supprimer aux deux lignes).
    val consoleList = remember(query, sortOption, customPresets, kindFilter) {
        val q = query.trim().lowercase()
        val native = consolePresets.map { ConsoleEntry(it, null) }
        val custom = customPresets.filter { it.type == ItemType.CONSOLE }.map { ConsoleEntry(it.toConsolePreset(), it) }
        val all = native + custom
        val byKind = kindFilter.kind?.let { k -> all.filter { it.preset.kind == k } } ?: all
        val filtered = if (q.isEmpty()) byKind
        else byKind.filter {
            ConsoleRecognition.matchesQuery(listOf(it.preset.name, it.preset.brand) + ConsoleRecognition.aliasSearchTerms(it.preset.name), q)
        }
        when (sortOption) {
            EncycloSortOption.NAME -> filtered.sortedBy { it.preset.name.lowercase() }
            EncycloSortOption.RELEASE_DATE -> filtered.sortedBy { it.preset.year }
            EncycloSortOption.RELEASE_DATE_DESC -> filtered.sortedByDescending { it.preset.year }
            EncycloSortOption.BRAND -> filtered.sortedWith(compareBy({ it.preset.brand.lowercase() }, { it.preset.name.lowercase() }))
        }
    }
    val consoleGroupsByBrand = remember(consoleList, sortOption) {
        if (sortOption == EncycloSortOption.BRAND) {
            consoleList.groupBy { it.preset.brand }.toList().sortedBy { it.first.lowercase() }
        } else emptyList()
    }
    val accessoriesByConsole = remember(query, customPresets) {
        val q = query.trim().lowercase()
        val native = accessoryPresets.map { AccessoryEntry(it, null) }
        val custom = customPresets.filter { it.type == ItemType.ACCESSOIRE }.map { AccessoryEntry(it.toAccessoryPreset(), it) }
        val all = native + custom
        val filtered = if (q.isEmpty()) all
        else all.filter { ConsoleRecognition.matchesQuery(listOf(it.preset.name, it.preset.brand, it.preset.console), q) }
        // Même tri (par nom) que la liste principale À L'INTÉRIEUR de chaque groupe console : sinon
        // les accessoires ajoutés (fondus après les natifs) restaient en fin de groupe.
        filtered.groupBy { it.preset.console }
            .mapValues { (_, list) -> list.sortedBy { it.preset.name.lowercase() } }
            .toSortedMap()
    }

    // Sélection invalidée si le filtre/tri change la liste affichée (évite de garder cochée une
    // fiche qui n'est plus visible et qu'on ne pourrait plus décocher).
    LaunchedEffect(consoleList) {
        selectedConsoleKeys = selectedConsoleKeys.intersect(consoleList.map { it.key() }.toSet())
    }
    LaunchedEffect(accessoriesByConsole) {
        val visible = accessoriesByConsole.values.flatten().map { it.key() }.toSet()
        selectedAccessoryKeys = selectedAccessoryKeys.intersect(visible)
    }
    val selectedConsolePresets = remember(consoleList, selectedConsoleKeys, photoOverrides) {
        consoleList.filter { it.key() in selectedConsoleKeys }.map { it.preset }
    }
    val selectedAccessoryPresets = remember(accessoriesByConsole, selectedAccessoryKeys, photoOverrides) {
        accessoriesByConsole.values.flatten().filter { it.key() in selectedAccessoryKeys }.map { it.preset }
    }
    val totalSelected = selectedConsolePresets.size + selectedAccessoryPresets.size

    Column(modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            // Largeur égale (1/2 chacune) plutôt qu'une largeur "au contenu" : un Row classique
            // peut dépasser la largeur de l'écran (petits téléphones, langues aux libellés plus
            // longs) et casser le rendu. Ainsi les onglets tiennent TOUJOURS à l'écran sans avoir
            // besoin de défiler.
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                EncycloModeChip(
                    label = stringResource(R.string.encyclo_mode_consoles),
                    selected = mode == EncycloMode.CONSOLES,
                    modifier = Modifier.weight(1f)
                ) { onModeChange(EncycloMode.CONSOLES) }
                EncycloModeChip(
                    label = stringResource(R.string.encyclo_mode_accessories),
                    selected = mode == EncycloMode.ACCESSORIES,
                    modifier = Modifier.weight(1f)
                ) { onModeChange(EncycloMode.ACCESSORIES) }
            }
            IconButton(onClick = onToggleSelectionMode) {
                Icon(
                    Icons.Filled.Checklist,
                    contentDescription = stringResource(R.string.encyclo_selection_mode_toggle),
                    tint = if (selectionMode) NeonCyan else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        ThemedSearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = if (mode == EncycloMode.CONSOLES) stringResource(R.string.search_console_count, consolePresets.size)
            else stringResource(R.string.search_accessory_count, accessoryPresets.size)
        )
        if (mode == EncycloMode.CONSOLES) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ThemedChoiceDropdown(
                    leading = stringResource(R.string.sort_prefix),
                    selectedLabel = encycloSortLabel(sortOption),
                    options = EncycloSortOption.values().toList(),
                    optionLabel = { encycloSortLabel(it) },
                    onSelect = { sortOption = it },
                    modifier = Modifier.weight(1f)
                )
                ThemedChoiceDropdown(
                    leading = stringResource(R.string.filter_kind_prefix),
                    selectedLabel = encycloKindFilterLabel(kindFilter),
                    options = EncycloKindFilter.values().toList(),
                    optionLabel = { encycloKindFilterLabel(it) },
                    onSelect = { kindFilter = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        val bottomInset = 90.dp
        if (mode == EncycloMode.CONSOLES) {
            Box(Modifier.weight(1f)) {
                LazyColumn(state = consoleListState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (sortOption == EncycloSortOption.BRAND) {
                        consoleGroupsByBrand.forEach { (brand, presets) ->
                            item(key = "brand_header_$brand") {
                                BrandGroupHeader(brand)
                            }
                            itemsIndexed(presets, key = { idx, it -> "$idx-${it.preset.brand}-${it.preset.name}" }) { _, entry ->
                                ConsoleRow(
                                    entry.preset, photoOverrides[entry.preset.name], isCustom = entry.custom != null,
                                    selectionMode = selectionMode, selected = entry.key() in selectedConsoleKeys,
                                    onToggleSelect = {
                                        val k = entry.key()
                                        selectedConsoleKeys = if (k in selectedConsoleKeys) selectedConsoleKeys - k else selectedConsoleKeys + k
                                    }
                                ) { onOpenConsole(entry.preset, entry.custom) }
                            }
                        }
                    } else {
                        itemsIndexed(consoleList, key = { idx, it -> "$idx-${it.preset.brand}-${it.preset.name}" }) { _, entry ->
                            ConsoleRow(
                                entry.preset, photoOverrides[entry.preset.name], isCustom = entry.custom != null,
                                selectionMode = selectionMode, selected = entry.key() in selectedConsoleKeys,
                                onToggleSelect = {
                                    val k = entry.key()
                                    selectedConsoleKeys = if (k in selectedConsoleKeys) selectedConsoleKeys - k else selectedConsoleKeys + k
                                }
                            ) { onOpenConsole(entry.preset, entry.custom) }
                        }
                    }
                    item { Spacer(Modifier.height(bottomInset)) }
                }
                ScrollPositionIndicator(
                    listState = consoleListState,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(top = 4.dp, bottom = 84.dp)
                )
            }
        } else {
            val accessoryListState = rememberLazyListState()
            Box(Modifier.weight(1f)) {
                LazyColumn(state = accessoryListState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    accessoriesByConsole.forEach { (console, presets) ->
                        item(key = "header_$console") {
                            Text(
                                console,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        itemsIndexed(presets, key = { idx, it -> "$idx-${it.preset.console}-${it.preset.brand}-${it.preset.name}" }) { _, entry ->
                            AccessoryRow(
                                entry.preset, photoOverrides[entry.preset.name], isCustom = entry.custom != null,
                                selectionMode = selectionMode, selected = entry.key() in selectedAccessoryKeys,
                                onToggleSelect = {
                                    val k = entry.key()
                                    selectedAccessoryKeys = if (k in selectedAccessoryKeys) selectedAccessoryKeys - k else selectedAccessoryKeys + k
                                },
                                onClick = { selectedAccessory = entry }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(bottomInset)) }
                }
                ScrollPositionIndicator(
                    listState = accessoryListState,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(top = 4.dp, bottom = 84.dp)
                )
            }
        }
        if (selectionMode && totalSelected > 0) {
            EncycloSelectionBar(
                count = totalSelected,
                onAddToCollection = {
                    onAddSelectedToCollection(selectedConsolePresets, selectedAccessoryPresets, false)
                    selectedConsoleKeys = emptySet(); selectedAccessoryKeys = emptySet()
                },
                onAddToWishlist = {
                    onAddSelectedToCollection(selectedConsolePresets, selectedAccessoryPresets, true)
                    selectedConsoleKeys = emptySet(); selectedAccessoryKeys = emptySet()
                }
            )
        }
    }

    selectedAccessory?.let { entry ->
        // Fiche perso (ajoutée par l'utilisateur) : on autorise sa suppression, sinon elle se
        // comporte exactement comme une fiche native (photo comprise). On utilise la référence
        // exacte de la ligne tapée (entry.custom), pas une recherche par nom : si une fiche perso
        // et une fiche native partagent le même nom, une recherche par nom associerait les
        // boutons aux deux lignes au lieu de la seule ligne perso.
        val custom = entry.custom
        AccessoryDetailDialog(
            preset = entry.preset,
            vm = vm,
            overrideUrl = photoOverrides[entry.preset.name],
            onAddToCollection = { onAddAccessory(entry.preset, false); selectedAccessory = null },
            onAddToWishlist = { onAddAccessory(entry.preset, true); selectedAccessory = null },
            onEdit = custom?.let { { onEditPreset(it); selectedAccessory = null } },
            onDelete = custom?.let { { vm.deleteCustomPreset(it); selectedAccessory = null } },
            customId = custom?.id,
            onDismiss = { selectedAccessory = null }
        )
    }
}

/** Une ligne du catalogue de jeux mis en cache (fiche Console de l'Encyclopédie), avec cote si déjà connue. */
@Composable
private fun CachedGameRow(game: CachedGame, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)).clickable(onClick = onClick).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (game.coverUrl != null) {
            AsyncImage(
                model = game.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(game.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(game.releaseYear?.toString(), game.developer?.takeIf { it.isNotBlank() })
                    .joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        game.priceCents?.let {
            Spacer(Modifier.width(8.dp))
            Text(formatPrice(it), color = NeonCyan, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/** Bandeau d'actions groupées affiché sous les listes de l'Encyclopédie en mode sélection multiple. */
@Composable
private fun EncycloSelectionBar(count: Int, onAddToCollection: () -> Unit, onAddToWishlist: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            stringResource(R.string.encyclo_selection_count, count),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onAddToCollection, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.encyclo_add_to_collection_button))
            }
            OutlinedButton(onClick = onAddToWishlist, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.encyclo_add_to_wishlist_button))
            }
        }
    }
}

/** Case à cocher ronde (sélection multiple Encyclo), dans le style néon de l'app. */
@Composable
private fun RoundCheckbox(checked: Boolean, onCheckedChange: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (checked) NeonPurple else Color.Transparent)
            .border(2.dp, if (checked) NeonPurple else Color(0xFF7A7A96), CircleShape)
            .clickable(onClick = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        if (checked) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
    }
}

/** En-tête de groupe « marque » : logo centré (si connu) puis le nom de la marque. */
@Composable
private fun BrandGroupHeader(brand: String) {
    val logoUrl = BrandLogos.urlFor(brand)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (logoUrl != null) {
            AsyncImage(
                model = logoUrl,
                contentDescription = brand,
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(36.dp).width(140.dp)
            )
            Spacer(Modifier.height(4.dp))
        }
        Text(
            brand,
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
private fun EncycloModeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .clip(shape)
            .background(if (selected) NeonPurple else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            .border(1.dp, NeonBorder, shape)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/** Petite pastille indiquant qu'une fiche est personnalisée (ajoutée par l'utilisateur). */
@Composable
private fun CustomBadge() {
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .clip(shape)
            .background(NeonPurple)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text("Perso", color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AccessoryRow(
    preset: AccessoryPreset,
    overrideUrl: String? = null,
    isCustom: Boolean = false,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardGradient)
            .border(1.dp, NeonBorder, shape)
            .clickable { if (selectionMode) onToggleSelect() else onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val url = overrideUrl ?: AccessoryImages.urlFor(preset.name)
        if (url != null) {
            var zoomed by remember(url) { mutableStateOf(false) }
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 64.dp, height = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { zoomed = true }
            )
            if (zoomed) {
                EnlargedPhotoDialog(url) { zoomed = false }
            }
        } else {
            Box(
                Modifier
                    .size(width = 64.dp, height = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(NeonPurple, NeonCyan)))
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(preset.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                "${preset.brand} • ${preset.year}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isCustom) {
            Spacer(Modifier.width(8.dp))
            CustomBadge()
        }
        if (selectionMode) {
            Spacer(Modifier.width(8.dp))
            RoundCheckbox(checked = selected, onCheckedChange = onToggleSelect)
        }
    }
}

@Composable
private fun AccessoryDetailDialog(
    preset: AccessoryPreset,
    vm: AppViewModel,
    overrideUrl: String? = null,
    onAddToCollection: () -> Unit,
    onAddToWishlist: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    // Id de la fiche perso d'origine si cet accessoire vient d'un ajout utilisateur (voir
    // [presetCacheKey]) : une fiche native du catalogue intégré n'a pas d'id propre.
    customId: Long? = null,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var offers by remember(preset.name) { mutableStateOf<List<Offer>?>(null) }
    LaunchedEffect(preset.name) {
        offers = fetchAllOffers(ItemType.ACCESSOIRE, preset.brand, preset.name, preset.console)
    }
    var estimatedPrice by remember(preset.name) { mutableStateOf<AppViewModel.QuickPriceEstimate?>(null) }
    val presetKey = remember(preset.brand, preset.name, customId) { presetCacheKey(preset.brand, preset.name, customId) }
    LaunchedEffect(presetKey) {
        estimatedPrice = null
        estimatedPrice = runCatching { vm.cachedPresetPrice(presetKey, ItemType.ACCESSOIRE, preset.brand, preset.name, preset.console) }
            .getOrDefault(AppViewModel.QuickPriceEstimate(null, false, null))
    }
    val cropLauncher = androidx.activity.compose.rememberLauncherForActivityResult(com.canhub.cropper.CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { cropped ->
                scope.launch {
                    val saved = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.example.macollection.data.MediaUtils.copyToInternal(context, cropped)
                    }
                    if (saved != null) vm.setPresetPhoto(preset.name, saved)
                }
            }
        }
    }
    fun launchCropFor(uri: android.net.Uri) {
        cropLauncher.launch(
            com.canhub.cropper.CropImageContractOptions(
                uri,
                com.canhub.cropper.CropImageOptions(activityMenuIconColor = android.graphics.Color.WHITE, cropMenuCropButtonTitle = "OK", initialCropWindowPaddingRatio = 0.2f)
            )
        )
    }
    val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) launchCropFor(uri) }
    var pendingCameraFile by remember { mutableStateOf<Pair<android.net.Uri, String>?>(null) }
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        val pending = pendingCameraFile
        if (success && pending != null) launchCropFor(pending.first)
    }
    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val pair = com.example.macollection.data.MediaUtils.newCameraFile(context)
            pendingCameraFile = pair
            cameraLauncher.launch(pair.first)
        }
    }
    fun launchCamera() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val pair = com.example.macollection.data.MediaUtils.newCameraFile(context)
            pendingCameraFile = pair
            cameraLauncher.launch(pair.first)
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp)
        ) {
            Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                val url = overrideUrl ?: AccessoryImages.urlFor(preset.name)
                if (url != null) {
                    var zoomed by remember(url) { mutableStateOf(false) }
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(14.dp))
                            .clickable { zoomed = true }
                    )
                    if (zoomed) {
                        EnlargedPhotoDialog(url) { zoomed = false }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                Text(preset.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${preset.brand} • ${preset.year} • ${preset.console}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Text(preset.description, color = MaterialTheme.colorScheme.onSurface)
                androidx.compose.material3.TextButton(
                    onClick = {
                        val url = com.example.macollection.data.AccessoryWikipediaLinks.urlFor(preset.name)
                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                    }
                ) { Text(stringResource(R.string.more_info_button)) }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    androidx.compose.material3.TextButton(onClick = {
                        photoPicker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) { Text(stringResource(R.string.choose_photo_option)) }
                    androidx.compose.material3.TextButton(onClick = { launchCamera() }) {
                        Text(stringResource(R.string.take_photo_option))
                    }
                    if (overrideUrl != null) {
                        androidx.compose.material3.TextButton(onClick = { vm.resetPresetPhoto(preset.name) }) {
                            Text(stringResource(R.string.reset_photo_button))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onAddToCollection, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.encyclo_add_to_collection_button), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.OutlinedButton(onClick = onAddToWishlist, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.encyclo_add_to_wishlist_button))
                }
                Spacer(Modifier.height(8.dp))
                EstimatedPriceRow(estimatedPrice)
                Spacer(Modifier.height(8.dp))
                OffersSection(
                    offers,
                    LeBonCoinLink.searchUrl(preset.brand, preset.name, preset.console),
                    VintedLink.searchUrl(preset.brand, preset.name, preset.console)
                )
                Spacer(Modifier.height(8.dp))
                if (onEdit != null) {
                    androidx.compose.material3.OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.edit))
                    }
                    Spacer(Modifier.height(8.dp))
                }
                if (onDelete != null) {
                    androidx.compose.material3.OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.delete))
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

@Composable
private fun ConsoleRow(
    preset: ConsolePreset,
    overrideUrl: String? = null,
    isCustom: Boolean = false,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardGradient)
            .border(1.dp, NeonBorder, shape)
            .clickable { if (selectionMode) onToggleSelect() else onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ConsoleThumb(preset.name, Modifier.size(width = 78.dp, height = 56.dp), overrideUrl)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(preset.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "${preset.brand} • ${preset.year} • ${preset.kind}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isCustom) {
            CustomBadge()
            Spacer(Modifier.width(8.dp))
        }
        if (selectionMode) {
            RoundCheckbox(checked = selected, onCheckedChange = onToggleSelect)
        }
    }
}

/**
 * Zoom plein écran d'une photo de l'Encyclopédie (consoles et accessoires) : appui sur la
 * miniature → dialogue avec l'image en grand. Le clic est consommé par cette miniature (ne
 * remonte pas au clic de la ligne), donc taper la photo zoome au lieu d'ouvrir la fiche.
 * Une fois ouverte, l'image accepte le pincement à deux doigts (zoom/dézoom) et le glissement
 * pour se déplacer une fois zoomée — fermeture via la croix ou en tapant en dehors de l'image
 * (comportement par défaut de [Dialog] : tap hors du contenu = [onDismiss]).
 */
@Composable
private fun EnlargedPhotoDialog(url: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
        val transformState = androidx.compose.foundation.gestures.rememberTransformableState { zoomChange, panChange, _ ->
            val newScale = (scale * zoomChange).coerceIn(1f, 6f)
            scale = newScale
            offset = if (newScale <= 1f) androidx.compose.ui.geometry.Offset.Zero else offset + panChange
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
        ) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(transformState)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = Color(0xFFFFFFFF))
            }
        }
    }
}

@Composable
private fun ConsoleThumb(name: String, modifier: Modifier, overrideUrl: String? = null) {
    val url = overrideUrl ?: ConsoleImages.urlFor(name)
    val shape = RoundedCornerShape(12.dp)
    if (url != null) {
        var zoomed by remember(url) { mutableStateOf(false) }
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape).clickable { zoomed = true }
        )
        if (zoomed) {
            EnlargedPhotoDialog(url) { zoomed = false }
        }
    } else {
        Box(
            modifier
                .clip(shape)
                .background(Brush.linearGradient(listOf(NeonPurple, NeonCyan))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Fiche encyclopédique d'une console (image, specs, 3D, jeux)
// ---------------------------------------------------------------------------

@Composable
fun ConsoleEncyclopediaScreen(
    vm: AppViewModel,
    preset: ConsolePreset,
    onAddToCollection: () -> Unit,
    onAddToWishlist: () -> Unit,
    onAddGameToCollection: (GameInfo) -> Unit,
    onAddGameToWishlist: (GameInfo) -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    // Id de la fiche perso d'origine si cette console vient d'un ajout utilisateur (voir
    // [presetCacheKey]) : une fiche native du catalogue intégré n'a pas d'id propre.
    customId: Long? = null,
    onBack: () -> Unit
) {
    // Liste des jeux de cette console : source mise en cache/synchronisée en base (même mécanisme
    // que l'ex-onglet "Jeux" autonome de l'Encyclopédie, fusionné ici — voir vm.gamesForPlatform),
    // avec repli sur la liste statique [CuratedGames] si la console n'est pas cartographiée à une
    // plateforme RAWG/IGDB ([ConsolePlatforms.platformId] == null).
    val platformKey = remember(preset.name) { ConsolePlatforms.platformId(preset.name) }
    var selectedGame by remember { mutableStateOf<CachedGame?>(null) }
    val gamesFlow = remember(platformKey) {
        platformKey?.let { vm.gamesForPlatform(it) } ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }
    val cachedGames by gamesFlow.collectAsState(initial = emptyList())
    var gameSyncState by remember(platformKey) { mutableStateOf<GameCatalogSyncState?>(null) }
    LaunchedEffect(platformKey) {
        if (platformKey != null) {
            gameSyncState = GameCatalogSyncState(platformKey, "SYNCING", 0, null, null)
            vm.syncGamesIfNeeded(platformKey)
            gameSyncState = vm.gameSyncState(platformKey)
        }
    }
    LaunchedEffect(platformKey, cachedGames.size) {
        if (platformKey != null && cachedGames.isNotEmpty()) gameSyncState = vm.gameSyncState(platformKey)
    }
    var gameSortOption by remember(platformKey) { mutableStateOf(GameSortOption.NAME) }
    val sortedGames = remember(cachedGames, gameSortOption) {
        when (gameSortOption) {
            GameSortOption.NAME -> cachedGames.sortedBy { it.name.lowercase() }
            GameSortOption.RELEASE_DATE -> cachedGames.sortedBy { it.releaseYear ?: Int.MAX_VALUE }
            GameSortOption.RELEASE_DATE_DESC -> cachedGames.sortedByDescending { it.releaseYear ?: Int.MIN_VALUE }
            GameSortOption.PRICE -> cachedGames.sortedByDescending { it.priceCents ?: -1 }
            GameSortOption.DEVELOPER -> cachedGames.sortedBy { (it.developer ?: "").lowercase() }
        }
    }
    var offers by remember { mutableStateOf<List<Offer>?>(null) }
    LaunchedEffect(preset.name) {
        offers = fetchAllOffers(ItemType.CONSOLE, preset.brand, preset.name, null)
    }
    var estimatedPrice by remember { mutableStateOf<AppViewModel.QuickPriceEstimate?>(null) }
    val presetKey = remember(preset.brand, preset.name, customId) { presetCacheKey(preset.brand, preset.name, customId) }
    LaunchedEffect(presetKey) {
        estimatedPrice = null
        estimatedPrice = runCatching { vm.cachedPresetPrice(presetKey, ItemType.CONSOLE, preset.brand, preset.name, null) }
            .getOrDefault(AppViewModel.QuickPriceEstimate(null, false, null))
    }
    val photoOverrides by vm.photoOverrides.collectAsState()
    val overrideUrl = photoOverrides[preset.name]
    val imageUrl = overrideUrl ?: ConsoleImages.urlFor(preset.name)
    val curated = remember(preset.name) { CuratedGames.gamesFor(preset.name) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val cropLauncher = androidx.activity.compose.rememberLauncherForActivityResult(com.canhub.cropper.CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { cropped ->
                scope.launch {
                    val saved = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.example.macollection.data.MediaUtils.copyToInternal(context, cropped)
                    }
                    if (saved != null) vm.setPresetPhoto(preset.name, saved)
                }
            }
        }
    }
    fun launchCropFor(uri: android.net.Uri) {
        cropLauncher.launch(
            com.canhub.cropper.CropImageContractOptions(
                uri,
                com.canhub.cropper.CropImageOptions(activityMenuIconColor = android.graphics.Color.WHITE, cropMenuCropButtonTitle = "OK", initialCropWindowPaddingRatio = 0.2f)
            )
        )
    }
    val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) launchCropFor(uri) }
    var pendingCameraFile by remember { mutableStateOf<Pair<android.net.Uri, String>?>(null) }
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        val pending = pendingCameraFile
        if (success && pending != null) launchCropFor(pending.first)
    }
    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val pair = com.example.macollection.data.MediaUtils.newCameraFile(context)
            pendingCameraFile = pair
            cameraLauncher.launch(pair.first)
        }
    }
    fun launchCamera() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val pair = com.example.macollection.data.MediaUtils.newCameraFile(context)
            pendingCameraFile = pair
            cameraLauncher.launch(pair.first)
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    Box(Modifier.fillMaxSize().background(themedGradient())) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(preset.name, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { p ->
            LazyColumn(
                Modifier.padding(p).fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    val shape = RoundedCornerShape(20.dp)
                    if (imageUrl != null) {
                        var zoomed by remember(imageUrl) { mutableStateOf(false) }
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(200.dp).clip(shape)
                                .border(1.dp, NeonBorder, shape)
                                .clickable { zoomed = true }
                        )
                        if (zoomed) {
                            EnlargedPhotoDialog(imageUrl) { zoomed = false }
                        }
                    } else {
                        Box(
                            Modifier.fillMaxWidth().height(160.dp).clip(shape)
                                .background(Brush.linearGradient(listOf(NeonPurple, NeonCyan))),
                            contentAlignment = Alignment.Center
                        ) { Text(preset.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        androidx.compose.material3.TextButton(onClick = {
                            photoPicker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) { Text(stringResource(R.string.choose_photo_option)) }
                        androidx.compose.material3.TextButton(onClick = { launchCamera() }) {
                            Text(stringResource(R.string.take_photo_option))
                        }
                        if (overrideUrl != null) {
                            androidx.compose.material3.TextButton(onClick = { vm.resetPresetPhoto(preset.name) }) {
                                Text(stringResource(R.string.reset_photo_button))
                            }
                        }
                    }
                }
                item {
                    Text(
                        "${preset.brand} • ${preset.year} • ${preset.kind}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(stringResource(R.string.cpu_label, preset.cpu), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.memory_label, preset.memory), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(6.dp))
                    Text(preset.description, color = MaterialTheme.colorScheme.onSurface)
                }
                item {
                    Row {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                val url = com.example.macollection.data.ConsoleWikipediaLinks.urlFor(preset.name)
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                            }
                        ) { Text(stringResource(R.string.more_info_button)) }
                        androidx.compose.material3.TextButton(
                            onClick = {
                                val url = com.example.macollection.data.ConsoleRepairLinks.urlFor(preset.name)
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                            }
                        ) { Text(stringResource(R.string.repair_help_button)) }
                    }
                }
                item {
                    Spacer(Modifier.height(4.dp))
                    Button(onClick = onAddToCollection, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.encyclo_add_to_collection_button), fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedButton(onClick = onAddToWishlist, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.encyclo_add_to_wishlist_button))
                    }
                }
                item {
                    Spacer(Modifier.height(4.dp))
                    EstimatedPriceRow(estimatedPrice)
                }
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.popular_games_title),
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (platformKey != null && cachedGames.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(2.dp))
                        ThemedChoiceDropdown(
                            leading = stringResource(R.string.sort_prefix),
                            selectedLabel = gameSortLabel(gameSortOption),
                            options = GameSortOption.values().toList(),
                            optionLabel = { gameSortLabel(it) },
                            onSelect = { gameSortOption = it }
                        )
                    }
                }
                when {
                    platformKey != null && cachedGames.isEmpty() && gameSyncState?.status == "SYNCING" -> item {
                        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    platformKey != null && cachedGames.isNotEmpty() -> items(sortedGames, key = { it.id }) { g ->
                        CachedGameRow(g, onClick = { selectedGame = g })
                    }
                    curated != null -> items(curated, key = { it }) { name ->
                        Text(
                            "🎮 $name",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        )
                    }
                    else -> item {
                        Text(
                            stringResource(R.string.game_list_unavailable),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    OffersSection(
                        offers,
                        LeBonCoinLink.searchUrl(preset.brand, preset.name, null),
                        VintedLink.searchUrl(preset.brand, preset.name, null)
                    )
                }
                if (onEdit != null) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.edit)) }
                    }
                }
                if (onDelete != null) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.delete)) }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    selectedGame?.let { cached ->
        val info = remember(cached.id, preset.name) {
            GameInfo(
                sourceId = if (cached.source == "rawg") cached.sourceId.toInt() else null,
                name = cached.name,
                platforms = preset.name,
                genres = cached.genres,
                releaseYear = cached.releaseYear,
                description = cached.description,
                coverUrl = cached.coverUrl,
                publisher = cached.publisher,
                developer = cached.developer,
                source = cached.source
            )
        }
        GameDetailDialog(
            vm = vm,
            game = info,
            consoleName = preset.name,
            cachedGame = cached,
            onAddToCollection = { onAddGameToCollection(info); selectedGame = null },
            onAddToWishlist = { onAddGameToWishlist(info); selectedGame = null },
            onDismiss = { selectedGame = null }
        )
    }
}

@Composable
private fun GameDetailDialog(
    vm: AppViewModel,
    game: GameInfo,
    consoleName: String,
    // Renseigné pour un jeu du catalogue mis en cache (section "Jeux populaires" d'une fiche
    // console) : la cote est alors mise à jour DIRECTEMENT sur sa ligne [CachedGame] plutôt que
    // dans le cache générique [PresetPriceCache] (pas d'id stable sinon pour un jeu éphémère).
    cachedGame: CachedGame? = null,
    onAddToCollection: () -> Unit,
    onAddToWishlist: () -> Unit,
    onDismiss: () -> Unit
) {
    var detail by remember(game.sourceId) { mutableStateOf<GameInfo?>(null) }
    var movieUrl by remember(game.sourceId) { mutableStateOf<String?>(null) }
    var loaded by remember(game.sourceId) { mutableStateOf(false) }
    LaunchedEffect(game.sourceId) {
        val id = game.sourceId
        if (id != null) {
            detail = GameCatalog.detail(id)
            movieUrl = GameCatalog.movieUrl(id)
        }
        loaded = true
    }
    var offers by remember(game.name) { mutableStateOf<List<Offer>?>(null) }
    LaunchedEffect(game.name, consoleName) {
        offers = fetchAllOffers(ItemType.JEU, "", game.name, consoleName)
    }
    var estimatedPrice by remember(game.name, cachedGame?.id) { mutableStateOf<AppViewModel.QuickPriceEstimate?>(null) }
    LaunchedEffect(game.name, consoleName, cachedGame?.id) {
        estimatedPrice = null
        estimatedPrice = runCatching {
            val cached = cachedGame
            if (cached != null) vm.cachedGamePrice(cached, consoleName)
            else vm.cachedPresetPrice(
                "game_${game.source}_${game.sourceId ?: game.name}",
                ItemType.JEU, "", game.name, consoleName
            )
        }.getOrDefault(AppViewModel.QuickPriceEstimate(null, false, null))
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                Text(game.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(
                    listOfNotNull(game.releaseYear?.toString(), game.platforms.takeIf { it.isNotBlank() }, game.genres.takeIf { it.isNotBlank() })
                        .joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                if (!loaded) {
                    Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (movieUrl != null) {
                        Text(stringResource(R.string.game_trailer_title), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        GameTrailerPlayer(movieUrl!!, Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(14.dp)))
                        Spacer(Modifier.height(10.dp))
                    } else if (game.coverUrl != null) {
                        AsyncImage(
                            model = game.coverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    val desc = detail?.description?.takeIf { it.isNotBlank() }
                    Text(
                        desc ?: stringResource(R.string.no_description_available),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    val ctx = androidx.compose.ui.platform.LocalContext.current
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            ctx.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(youtubeSearchUrl(game.name, consoleName))
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.youtube_search_button)) }
                }
                Spacer(Modifier.height(8.dp))
                EstimatedPriceRow(estimatedPrice)
                Spacer(Modifier.height(8.dp))
                OffersSection(
                    offers,
                    LeBonCoinLink.searchUrl("", game.name, consoleName),
                    VintedLink.searchUrl("", game.name, consoleName)
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onAddToCollection, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.encyclo_add_to_collection_button), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.OutlinedButton(onClick = onAddToWishlist, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.encyclo_add_to_wishlist_button))
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

/** Une annonce (eBay, Vinted...) dans la section "Offres" d'une fiche Console/Accessoire/Jeu. */
@Composable
private fun OfferRow(offer: Offer) {
    val shape = RoundedCornerShape(10.dp)
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        Modifier.fillMaxWidth().clip(shape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .clickable(enabled = offer.url != null) {
                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(offer.url)))
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (offer.imageUrl != null) {
            AsyncImage(
                model = offer.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(offer.title, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
            // Source + état sur la même ligne (ex. "Vinted • Très bon état") : l'utilisateur doit
            // savoir où le clic l'emmène avant de taper, plusieurs marketplaces étant mélangées.
            Text(
                listOfNotNull(offer.source, offer.condition?.takeIf { it.isNotBlank() }).joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        offer.priceCents?.let {
            Spacer(Modifier.width(8.dp))
            Text(formatPrice(it), color = NeonCyan, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/**
 * Section "Offres" (titre + liste/état de chargement), partagée entre fiche Console, Accessoire
 * et Jeu. [offers] vient d'eBay, seule marketplace avec des annonces structurées cliquables une
 * par une (voir [fetchAllOffers]) — [leboncoinSearchUrl]/[vintedSearchUrl] ajoutent chacun un
 * bouton de recherche externe en bas (voir [LeBonCoinLink]/[VintedLink] pour pourquoi ces deux
 * sites n'ont pas d'annonces structurées comme eBay).
 */
@Composable
private fun OffersSection(offers: List<Offer>?, leboncoinSearchUrl: String? = null, vintedSearchUrl: String? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Spacer(Modifier.height(4.dp))
    Text(
        stringResource(R.string.offers_title),
        fontWeight = FontWeight.Bold,
        color = NeonCyan,
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.height(6.dp))
    when {
        offers == null -> Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        offers.isEmpty() -> Text(
            stringResource(R.string.offers_unavailable),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            offers.forEach { offer -> OfferRow(offer) }
        }
    }
    if (leboncoinSearchUrl != null || vintedSearchUrl != null) {
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            if (leboncoinSearchUrl != null) {
                androidx.compose.material3.OutlinedButton(
                    onClick = { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(leboncoinSearchUrl))) },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.leboncoin_search_button)) }
            }
            if (vintedSearchUrl != null) {
                androidx.compose.material3.OutlinedButton(
                    onClick = { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(vintedSearchUrl))) },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.vinted_search_button)) }
            }
        }
    }
}

/**
 * Ligne "Cote : X (IA)" d'une fiche Console/Accessoire de l'Encyclopédie ([estimate] vient de
 * [AppViewModel.cachedPresetPrice]), ou un indicateur de chargement pendant la 1re estimation
 * (null = en cours). N'affiche rien si l'IA n'a trouvé aucune estimation (plutôt qu'un message
 * "indisponible" sur une bonne partie des 655+84 fiches du catalogue).
 */
@Composable
private fun EstimatedPriceRow(estimate: AppViewModel.QuickPriceEstimate?) {
    when {
        estimate == null -> Box(Modifier.fillMaxWidth().height(32.dp), contentAlignment = Alignment.CenterStart) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        }
        estimate.priceCents != null -> Text(
            stringResource(R.string.wishlist_price_label, formatPrice(estimate.priceCents) + if (estimate.isAiEstimate) " (IA)" else ""),
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
