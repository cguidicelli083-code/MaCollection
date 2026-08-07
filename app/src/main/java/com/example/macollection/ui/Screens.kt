@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.example.macollection.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.DocumentsContract
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.macollection.R
import com.example.macollection.data.AccessoryImages
import com.example.macollection.data.AccessoryPreset
import com.example.macollection.data.AccessoryWikipediaLinks
import com.example.macollection.data.accessoryPresets
import com.example.macollection.data.AppPrefs
import com.example.macollection.data.CollectionItem
import com.example.macollection.data.Condition
import com.example.macollection.data.ConsoleColorSupport
import com.example.macollection.data.ConsoleImages
import com.example.macollection.data.ConsolePlatforms
import com.example.macollection.data.ConsolePreset
import com.example.macollection.data.ConsoleRecognition
import com.example.macollection.data.ConsoleRepairLinks
import com.example.macollection.data.ConsoleWikipediaLinks
import com.example.macollection.data.CurrencyOptions
import com.example.macollection.data.CustomPreset
import com.example.macollection.data.EbayPrices
import com.example.macollection.data.GameCatalog
import com.example.macollection.data.GeminiVision
import com.example.macollection.data.GroqVision
import com.example.macollection.data.HybridGameSearch
import com.example.macollection.data.IgdbCatalog
import com.example.macollection.data.GameShopCatalog
import com.example.macollection.data.GameInfo
import com.example.macollection.data.WikiPresetResult
import com.example.macollection.data.WikipediaPresetSearch
import com.example.macollection.data.ImageSearch
import com.example.macollection.data.ItemPhoto
import com.example.macollection.data.ItemType
import com.example.macollection.data.KnownBrands
import com.example.macollection.data.PriceHistory
import com.example.macollection.data.Region
import com.example.macollection.data.ScanTools
import com.example.macollection.data.SearchCascade
import com.example.macollection.data.SpreadsheetImport
import com.example.macollection.data.UrlImport
import com.example.macollection.data.consolePresets
import com.example.macollection.BuildConfig
import com.example.macollection.ui.ads.showTotalScreenAd
import com.example.macollection.ui.ads.watchRewardedAdForPoints
import kotlinx.coroutines.launch

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.macollection.data.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import com.example.macollection.ui.theme.CardGradient
import com.example.macollection.ui.theme.NeonBorder
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPink
import com.example.macollection.ui.theme.NeonPurple
import com.example.macollection.ui.theme.themedGradient
import java.util.Locale
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------
// Onglet COLLECTION
// ---------------------------------------------------------------------------

@Composable
fun CollectionScreen(
    vm: AppViewModel,
    onOpen: (CollectionItem) -> Unit,
    onEdit: (CollectionItem) -> Unit,
    modifier: Modifier = Modifier,
    wishlist: Boolean = false,
    // Hissé par l'appelant (MainActivity) : sans ça, la LazyColumn perd sa position de
    // défilement chaque fois que cet écran est temporairement retiré de la composition (passage
    // par la fiche de détail/modification, ou changement d'onglet), puisqu'un état par défaut
    // (rememberLazyListState() interne) est alors recréé de zéro au retour.
    listState: LazyListState = rememberLazyListState(),
    // Transfert par lot (Souhaits -> Collection uniquement, voir §3g) : mêmes vérifications de
    // quota TEST/gratuit que le transfert unitaire (canMoveToCollection sur ItemDetailScreen).
    remainingCollectionSlots: () -> Int = { Int.MAX_VALUE },
    onBlockedByCollectionLimit: () -> Unit = {}
) {
    val allItems by (if (wishlist) vm.wishlist else vm.items).collectAsState()
    val sort by vm.sortOption.collectAsState()
    val filter by vm.typeFilter.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    // Mode sélection multiple (case à cocher / appui long) : état local, pas besoin de survivre à
    // un changement d'onglet (contrairement à listState ci-dessus).
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var confirmingBulkDelete by remember { mutableStateOf(false) }
    fun exitSelectionMode() { selectionMode = false; selectedIds = emptySet() }
    BackHandler(enabled = selectionMode) { exitSelectionMode() }
    val items = remember(allItems, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) allItems
        else allItems.filter {
            // Le nom de la console associée (jeux/accessoires) entre aussi dans la recherche, avec
            // ses abréviations (« ps3 », « gb »...) : sans ça, taper le nom d'une console ne
            // remontait que les objets dont LE NOM À EUX contenait ce mot (quasi jamais un jeu).
            val haystack = listOf(it.name, it.brand) + ConsoleRecognition.aliasSearchTerms(it.name) +
                listOfNotNull(it.platform) + (it.platform?.let(ConsoleRecognition::aliasSearchTerms) ?: emptyList())
            ConsoleRecognition.matchesQuery(haystack, q)
        }
    }
    // En Jeux/Accessoires triés par console associée, les fiches sont regroupées sous un en-tête
    // par console qui reste figé en haut pendant le défilement (voir stickyHeader ci-dessous).
    val groupByConsole = (filter == ItemType.JEU || filter == ItemType.ACCESSOIRE) && sort == SortOption.BRAND
    val consoleGroups = remember(items, groupByConsole) {
        if (!groupByConsole) emptyList()
        else items.groupConsecutiveBy { it.platform?.trim()?.takeIf { p -> p.isNotBlank() } }
    }
    // Sélection invalidée si la liste affichée change (filtre/tri/recherche/suppression ailleurs) :
    // évite de garder cochée une fiche qui n'est plus visible et qu'on ne pourrait plus décocher.
    LaunchedEffect(items) { selectedIds = selectedIds.intersect(items.map { it.id }.toSet()) }
    val selectedItems = remember(items, selectedIds) { items.filter { it.id in selectedIds } }
    val selectedTotalCents = remember(selectedItems) { selectedItems.sumOf { it.priceCents ?: 0 } }

    Column(modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                ThemedSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = stringResource(if (wishlist) R.string.wishlist_search_label else R.string.collection_search_label)
                )
            }
            IconButton(onClick = {
                if (selectionMode) exitSelectionMode() else selectionMode = true
            }) {
                Icon(
                    Icons.Filled.Checklist,
                    contentDescription = stringResource(R.string.selection_mode_toggle),
                    tint = if (selectionMode) NeonCyan else Color.White
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        if (!wishlist) {
            val isPremium by vm.isPremium.collectAsState()
            if (!isPremium && !BuildConfig.IS_TEST) {
                val ownedCount by vm.allOwnedItems.collectAsState()
                val extraSlots by AppPrefs.extraCollectionSlots
                val quota = FREE_COLLECTION_LIMIT + extraSlots
                Text(
                    "${ownedCount.size}/$quota objets (compte gratuit)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (ownedCount.size >= quota) NeonPink else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        TypeFilterDropdown(filter) { vm.setTypeFilter(it) }
        Spacer(Modifier.height(8.dp))
        SortDropdown(sort, filter) { vm.setSort(it) }
        Spacer(Modifier.height(10.dp))

        if (items.isEmpty()) {
            val message = when {
                searchQuery.isNotBlank() -> stringResource(R.string.collection_search_no_results)
                wishlist -> stringResource(R.string.empty_wishlist)
                else -> stringResource(R.string.empty_collection)
            }
            EmptyState(message)
        } else {
            Box(Modifier.weight(1f)) {
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (groupByConsole) {
                        consoleGroups.forEach { (console, groupItems) ->
                            stickyHeader(key = "header_${console ?: "?"}") {
                                ConsoleGroupHeader(
                                    console ?: stringResource(R.string.console_group_unknown),
                                    itemCount = groupItems.size,
                                    totalCents = groupItems.sumOf { it.priceCents ?: 0 }
                                )
                            }
                            items(groupItems, key = { it.id }) { item ->
                                CollectionCard(
                                    item = item,
                                    onOpen = { onOpen(item) },
                                    onEdit = { onEdit(item) },
                                    onDelete = { vm.deleteCollectionItem(item) },
                                    selectionMode = selectionMode,
                                    selected = item.id in selectedIds,
                                    onToggleSelect = {
                                        selectedIds = if (item.id in selectedIds) selectedIds - item.id else selectedIds + item.id
                                    },
                                    onLongPress = { selectionMode = true; selectedIds = selectedIds + item.id }
                                )
                            }
                        }
                    } else {
                        items(items, key = { it.id }) { item ->
                            CollectionCard(
                                item = item,
                                onOpen = { onOpen(item) },
                                onEdit = { onEdit(item) },
                                onDelete = { vm.deleteCollectionItem(item) },
                                selectionMode = selectionMode,
                                selected = item.id in selectedIds,
                                onToggleSelect = {
                                    selectedIds = if (item.id in selectedIds) selectedIds - item.id else selectedIds + item.id
                                },
                                onLongPress = { selectionMode = true; selectedIds = selectedIds + item.id }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(90.dp)) } // espace sous le bouton flottant
                }
                ScrollPositionIndicator(
                    listState = listState,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(top = 4.dp, bottom = 84.dp)
                )
            }
        }
        if (selectionMode && selectedItems.isNotEmpty()) {
            SelectionActionBar(
                count = selectedItems.size,
                totalCents = selectedTotalCents,
                showTransfer = wishlist,
                onSelectAll = { selectedIds = items.map { it.id }.toSet() },
                onDelete = { confirmingBulkDelete = true },
                onTransfer = {
                    if (selectedItems.size > remainingCollectionSlots()) {
                        onBlockedByCollectionLimit()
                    } else {
                        vm.bulkTransferToCollection(selectedItems)
                        exitSelectionMode()
                    }
                },
                onCancel = { exitSelectionMode() }
            )
        }
    }

    if (confirmingBulkDelete) {
        AlertDialog(
            onDismissRequest = { confirmingBulkDelete = false },
            title = { Text(stringResource(R.string.bulk_delete_confirm_title, selectedItems.size)) },
            text = { Text(stringResource(R.string.bulk_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.bulkDeleteItems(selectedItems)
                    confirmingBulkDelete = false
                    exitSelectionMode()
                }) { Text(stringResource(R.string.bulk_delete_button)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingBulkDelete = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

/** Bandeau d'actions groupées affiché sous la liste Collection/Souhaits en mode sélection multiple. */
@Composable
private fun SelectionActionBar(
    count: Int,
    totalCents: Int,
    showTransfer: Boolean,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    onTransfer: () -> Unit,
    onCancel: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            stringResource(R.string.selection_count_value, count, formatPrice(totalCents)),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onSelectAll, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.selection_mode_select_all))
            }
            Button(onClick = onDelete, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = NeonPink)) {
                Text(stringResource(R.string.bulk_delete_button))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            if (showTransfer) {
                Button(onClick = onTransfer, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.bulk_transfer_button))
                }
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.selection_mode_cancel))
            }
        }
    }
}

@Composable
private fun CollectionCard(
    item: CollectionItem,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    GamerCard(
        onClick = { if (selectionMode) onToggleSelect() else onOpen() },
        onLongClick = { if (!selectionMode) onLongPress() }
    ) {
        Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectionMode) {
                SelectionCheckbox(checked = selected, onCheckedChange = onToggleSelect)
                Spacer(Modifier.width(10.dp))
            }
            if (item.imageUri != null) {
                AsyncImage(
                    model = item.imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            } else {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(NeonPurple, NeonCyan))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(typeEmoji(item.type), fontSize = 24.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.name.ifBlank { stringResource(R.string.unnamed_item) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    listOfNotNull(
                        item.brand.ifBlank { null },
                        item.region.label,
                        item.releaseYear?.toString()
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.type == ItemType.JEU) {
                    val gameInfo = listOfNotNull(
                        item.platform?.takeIf { it.isNotBlank() },
                        item.genre?.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")
                    if (gameInfo.isNotBlank()) {
                        Text(
                            gameInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ConditionBadge(item.condition)
                    Spacer(Modifier.width(6.dp)); TinyTag(stringResource(R.string.tag_box), filled = item.hasBox)
                    Spacer(Modifier.width(6.dp)); TinyTag(stringResource(R.string.tag_manual), filled = item.hasManual)
                    if (!item.hasBox && !item.hasManual) { Spacer(Modifier.width(6.dp)); LooseTag() }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    formatPrice(item.priceCents) + if (item.priceIsAiEstimate) " (IA)" else "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                item.description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (!selectionMode) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, stringResource(R.string.edit), tint = NeonPurple)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, stringResource(R.string.delete), tint = NeonPink)
                }
            }
        }
        }
    }
}

/** Case à cocher ronde (sélection multiple Collection/Souhaits), même style que celle de l'Encyclopédie. */
@Composable
private fun SelectionCheckbox(checked: Boolean, onCheckedChange: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (checked) NeonPurple else Color.Transparent)
            .border(2.dp, if (checked) NeonPurple else Color(0xFF7A7A96), CircleShape)
            .clickable(onClick = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        if (checked) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

/**
 * Regroupe une liste déjà triée en tronçons consécutifs de même clé (contrairement à
 * [kotlin.collections.groupBy], qui rassemblerait toutes les occurrences d'une clé même non
 * adjacentes) : utilisé pour les en-têtes de console, où l'ordre du tri par console associée doit
 * être préservé tel quel.
 */
private fun <T, K> List<T>.groupConsecutiveBy(keySelector: (T) -> K): List<Pair<K, List<T>>> {
    val result = mutableListOf<Pair<K, MutableList<T>>>()
    for (element in this) {
        val key = keySelector(element)
        val last = result.lastOrNull()
        if (last != null && last.first == key) last.second.add(element)
        else result.add(key to mutableListOf(element))
    }
    return result
}

/** En-tête figé (sticky) au-dessus d'un groupe de jeux/accessoires partageant la même console. */
@Composable
private fun ConsoleGroupHeader(consoleName: String, itemCount: Int, totalCents: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 4.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardGradient)
                .border(1.5.dp, NeonBorder, RoundedCornerShape(12.dp))
                .padding(vertical = 8.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(typeEmoji(ItemType.CONSOLE), fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    consoleName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    stringResource(R.string.console_group_summary, itemCount, formatPrice(totalCents)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Onglet TOTAL
// ---------------------------------------------------------------------------

@Composable
fun TotalScreen(
    vm: AppViewModel,
    gameVm: GameViewModel,
    modifier: Modifier = Modifier,
    // Permet de taper une puce (Consoles/Accessoires/Jeux) pour basculer directement sur l'onglet
    // Collection filtré sur cette catégorie, au lieu de devoir y aller puis choisir le filtre soi-même.
    onNavigateToCollection: (ItemType) -> Unit = {}
) {
    val total by vm.totalCents.collectAsState()
    val items by vm.allOwnedItems.collectAsState()

    val context = LocalContext.current
    val adsEnabled = !gameVm.isAdsReduced.collectAsState().value
    LaunchedEffect(Unit) {
        showTotalScreenAd(context, adsEnabled)
    }

    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(CardGradient)
                .border(1.5.dp, NeonBorder, RoundedCornerShape(28.dp))
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.total_value_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    formatPrice(total),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.total_items_count, items.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        val consoleCount = items.count { it.type == ItemType.CONSOLE }
        val accessoryCount = items.count { it.type == ItemType.ACCESSOIRE }
        val gameCount = items.count { it.type == ItemType.JEU }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TotalCountChip(
                stringResource(R.string.total_consoles_count, consoleCount),
                Modifier.weight(1f)
            ) { onNavigateToCollection(ItemType.CONSOLE) }
            TotalCountChip(
                stringResource(R.string.total_accessories_count, accessoryCount),
                Modifier.weight(1f)
            ) { onNavigateToCollection(ItemType.ACCESSOIRE) }
            TotalCountChip(
                stringResource(R.string.total_games_count, gameCount),
                Modifier.weight(1f)
            ) { onNavigateToCollection(ItemType.JEU) }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.total_auto_update_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TotalCountChip(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------------------
// Fiche détail (image qui s'incline en 3D au gyroscope)
// ---------------------------------------------------------------------------

/**
 * Recherche YouTube ciblée pour un gameplay du jeu — bien plus fiable que les extraits RAWG
 * pour les jeux rétro (RAWG ne couvre quasiment que les bandes-annonces Steam de jeux récents).
 */
/**
 * Vraie langue d'affichage actuelle de l'app (les 11 codes possibles), pour la traduction des
 * descriptions via [GroqVision.translate] — contrairement à [AppPrefs.language], qui ne vaut
 * jamais que "fr" ou "en" (limité à cela exprès pour RAWG/Wikipédia, qui n'ont que ces 2 langues).
 * Utiliser [AppPrefs.language] ici collapse à tort toute langue non-fr vers l'anglais, empêchant
 * toute traduction réelle vers l'allemand, le grec, le turc, etc. (texte resté en anglais).
 */
private fun currentUiLanguage(): String =
    AppCompatDelegate.getApplicationLocales().get(0)?.language ?: AppPrefs.language

fun youtubeSearchUrl(gameName: String, platform: String?): String {
    // Le nom du jeu entre guillemets force une recherche en phrase exacte sur YouTube,
    // ce qui évite les résultats hors-sujet (autres jeux, compilations, vidéos sans rapport).
    val cleanName = gameName.trim().replace("\"", "")
    val shortPlatform = platform
        ?.let { Regex("\\(([^)]+)\\)").find(it)?.groupValues?.get(1) ?: it }
        ?.trim()
    val query = listOfNotNull(
        cleanName.takeIf { it.isNotBlank() }?.let { "\"$it\"" },
        shortPlatform?.takeIf { it.isNotBlank() },
        "gameplay"
    ).joinToString(" ")
    return "https://www.youtube.com/results?search_query=" + java.net.URLEncoder.encode(query, "UTF-8")
}

/**
 * Recherche PriceCharting pour affiner/trouver la cote d'un objet (jeu, console ou accessoire).
 * PriceCharting indexe ses fiches sous la forme « Console Nom du jeu » : ajouter la plateforme
 * (nom court, ex. « SNES » plutôt que « Super Nintendo (SNES) ») quand elle est connue ouvre
 * directement sur la bonne fiche au lieu d'une recherche générique sur le seul titre.
 */
fun priceChartingSearchUrl(name: String, platform: String? = null): String {
    val cleanName = name.trim()
    val shortPlatform = platform
        ?.let { Regex("\\(([^)]+)\\)").find(it)?.groupValues?.get(1) ?: it }
        ?.trim()
    val query = listOfNotNull(shortPlatform?.takeIf { it.isNotBlank() }, cleanName.takeIf { it.isNotBlank() })
        .joinToString(" ")
    return "https://www.pricecharting.com/search-products?q=" + java.net.URLEncoder.encode(query, "UTF-8") + "&type=videogames"
}

/**
 * Domaine eBay à utiliser, choisi selon la LANGUE de l'appli (pas la position géographique
 * réelle de l'utilisateur, invérifiable depuis ici) : un Français en déplacement à l'étranger
 * avec l'appli restée en français doit quand même arriver sur le site FR, pas sur celui du
 * pays où il se trouve. eBay n'a pas de site dédié pour toutes les langues supportées (grec,
 * japonais, russe, turc, chinois...) : on retombe alors sur ebay.com (site international,
 * toujours en anglais) plutôt que d'imposer le français à qui ne le lit pas.
 */
private fun ebayHostForLanguage(lang: String): String = when (lang) {
    "fr" -> "www.ebay.fr"
    "de" -> "www.ebay.de"
    "es" -> "www.ebay.es"
    "it" -> "www.ebay.it"
    else -> "www.ebay.com"
}

/**
 * Lien "voir les offres" pour un objet des Souhaits : eBay en priorité (mieux indexé que
 * Rakuten/Google Shopping sur le rétro-gaming de niche, constaté sur les recherches par
 * code-barres de cette appli), avec repli en cascade si une étape échoue à produire une URL —
 * jamais un simple bouton désactivé tant qu'un nom est disponible.
 * - Priorité 1 : le lien source de la fiche s'il pointe déjà vers eBay (import URL précis, TOUJOURS
 *   respecté tel quel), sinon une recherche eBay avec [verifiedEbayQuery] SI ELLE EST FOURNIE —
 *   c'est à l'appelant de vérifier au préalable (cote confirmée sur la fiche, ou
 *   [EbayPrices.findWorkingEbayQuery]) que cette requête précise trouve bien quelque chose. BUG
 *   constaté en conditions réelles avant ce paramètre : reconstruire la requête ici à partir du
 *   nom COMPLET (ex. "Nintendo 64 Jusco 30e Anniversaire") rouvrait une recherche VIDE alors même
 *   que l'appelant avait confirmé qu'une version raccourcie ("Nintendo 64 Jusco") trouvait bien
 *   une annonce — la requête utilisée ici doit être EXACTEMENT celle qui a été vérifiée, jamais
 *   reconstruite indépendamment.
 * - Priorité 2 : recherche Google Shopping, si eBay n'a pas de résultat connu (ou si la
 *   construction de son URL a échoué). Constaté en conditions réelles (bloqué par Cloudflare/
 *   DataDome pour une vérification automatisée directe, cf. session) que Rakuten est nettement
 *   moins fiable que Google Shopping sur le rétro-gaming de niche — Google reste en pratique la
 *   seule des deux qui retrouve presque toujours quelque chose.
 * - Priorité 3 : recherche Rakuten, dernier recours si même Google Shopping a échoué.
 * Renvoie null seulement si [name] est vide (cf. bouton masqué dans [ItemDetailScreen]).
 */
/**
 * Texte de recherche "propre" pour un objet des Souhaits : plateforme courte (contenu entre
 * parenthèses si présent) + nom, DÉBARRASSÉ de ses propres parenthèses (ex. "Sonic the Hedgehog
 * (Édition Collector)", "Zelda (import japonais)") — un simple complément d'info sur la fiche,
 * jamais une partie du titre officiel du jeu/console, qui peut faire tomber le nombre de
 * résultats à zéro (constaté en conditions réelles). Jamais appliqué au nom affiché ailleurs
 * dans l'appli (fiche, liste...), seulement à cette recherche.
 *
 * Marque ajoutée en préfixe UNIQUEMENT quand le nom commence par un chiffre (ex. "1040 STF" ->
 * "Atari 1040 STF", "2600" -> "Atari 2600", "3DO" -> "Panasonic 3DO") : un nom purement numérique
 * ou débutant par un chiffre est trop ambigu seul pour une recherche (rien n'indique qu'il s'agit
 * d'une console), contrairement à un nom alphabétique déjà distinctif ("Dreamcast", "Game Boy
 * Advance") où ajouter la marque n'apporte rien et risque plutôt de réduire les résultats (déjà
 * constaté cette session avec des requêtes trop longues) — on ne l'ajoute donc QUE dans ce cas
 * précis, jamais partout.
 */
private fun offersSearchQuery(name: String, brand: String, platform: String?): String {
    val cleanName = name.trim()
    val shortPlatform = platform
        ?.let { Regex("\\(([^)]+)\\)").find(it)?.groupValues?.get(1) ?: it }
        ?.trim()
    val nameWithoutParens = cleanName.replace(Regex("\\([^)]*\\)"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .ifBlank { cleanName }
    val brandTrim = brand.trim()
    val nameWithBrand = if (nameWithoutParens.firstOrNull()?.isDigit() == true &&
        brandTrim.isNotBlank() && !nameWithoutParens.contains(brandTrim, ignoreCase = true)
    ) {
        "$brandTrim $nameWithoutParens"
    } else {
        nameWithoutParens
    }
    return listOfNotNull(shortPlatform?.takeIf { it.isNotBlank() }, nameWithBrand).joinToString(" ")
}

fun wishlistOffersUrl(
    name: String,
    brand: String,
    platform: String?,
    sourceUrl: String?,
    verifiedEbayQuery: String?,
    uiLanguage: String
): String? {
    val cleanName = name.trim()
    if (cleanName.isBlank()) return null
    val query = offersSearchQuery(cleanName, brand, platform)

    val directEbayUrl = sourceUrl?.trim()?.takeIf { it.isNotBlank() }
        ?.takeIf { runCatching { Uri.parse(it).host?.contains("ebay", ignoreCase = true) == true }.getOrDefault(false) }

    val ebayUrl = directEbayUrl ?: verifiedEbayQuery?.takeIf { it.isNotBlank() }?.let { vq ->
        runCatching {
            "https://${ebayHostForLanguage(uiLanguage)}/sch/i.html?_nkw=" + java.net.URLEncoder.encode(vq, "UTF-8")
        }.getOrNull()
    }
    if (ebayUrl != null) return ebayUrl

    val googleUrl = runCatching {
        "https://www.google.com/search?tbm=shop&q=" + java.net.URLEncoder.encode(query, "UTF-8")
    }.getOrNull()
    if (googleUrl != null) return googleUrl

    return runCatching {
        "https://fr.shopping.rakuten.com/search/mo/" + java.net.URLEncoder.encode(query, "UTF-8")
    }.getOrNull()
}

/** Petit lecteur vidéo (extrait RAWG) façon Batocera/Recalbox, lecture automatique en boucle muette. */
@Composable
fun GameTrailerPlayer(url: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            android.widget.VideoView(ctx).apply {
                setVideoURI(android.net.Uri.parse(url))
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    mp.setVolume(0f, 0f)
                    start()
                }
            }
        }
    )
}

@Composable
fun ItemDetailScreen(
    vm: AppViewModel,
    item: CollectionItem,
    loadHistory: suspend (Long) -> List<PriceHistory>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    /** Renvoie false pour bloquer le passage souhait -> collection (ex. limite de la version de
     * test) : appelé avant le déplacement, qui n'a lieu que si la fonction renvoie true. */
    canMoveToCollection: () -> Boolean = { true }
) {
    val (rotX, rotY) = rememberTilt()
    val context = LocalContext.current
    var history by remember { mutableStateOf<List<PriceHistory>>(emptyList()) }
    LaunchedEffect(item.id, item.priceCents) { history = loadHistory(item.id) }
    val galleryPhotos by vm.photosFor(item.id).collectAsState(initial = emptyList())
    var fullscreenUri by remember { mutableStateOf<String?>(null) }
    var editingPrice by remember { mutableStateOf(false) }

    // Description dans la langue de l'utilisateur : si elle n'a pas encore été traduite/vérifiée
    // dans la langue courante (descriptionLang != langue de l'appli), on tente une traduction Groq
    // à l'affichage, puis on mémorise le résultat en base (via [updateDescriptionTranslation]).
    // Tant que la traduction échoue (réseau/quota/Groq non configuré) on garde l'original et on
    // retentera à la prochaine ouverture — ce qui rattrape les fiches restées en anglais faute de
    // traduction réussie au moment de l'ajout, tous types confondus (jeu, console, accessoire).
    val currentLang = currentUiLanguage()
    var shownDescription by remember(item.id) { mutableStateOf(item.description) }
    LaunchedEffect(item.id, currentLang, item.description, item.descriptionLang) {
        val original = item.description
        shownDescription = original
        if (original.isNullOrBlank() || item.descriptionLang == currentLang) return@LaunchedEffect
        val tr = GroqVision.translate(original, currentLang)
        if (tr != null) {
            shownDescription = tr
            vm.updateDescriptionTranslation(item, tr, currentLang)
        }
    }

    GamerScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(item.name.ifBlank { stringResource(R.string.unnamed_item) }, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { p ->
            Column(
                Modifier
                    .padding(p)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.fillMaxWidth().height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Halo lumineux
                    Box(
                        Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(48.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(NeonPurple.copy(alpha = 0.55f), Color.Transparent)
                                )
                            )
                    )
                    // Visuel incliné en 3D
                    Box(
                        Modifier
                            .graphicsLayer {
                                rotationX = rotX
                                rotationY = rotY
                                cameraDistance = 14f * density
                            }
                            .let { m ->
                                if (item.imageUri != null) m.clickable { fullscreenUri = item.imageUri } else m
                            }
                    ) {
                        HeroVisual(item)
                    }
                }
                Spacer(Modifier.height(16.dp))

                Text(
                    listOfNotNull(
                        item.brand.ifBlank { null },
                        item.region.label,
                        item.releaseYear?.toString()
                    ).joinToString(" • "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ConditionBadge(item.condition)
                    Spacer(Modifier.width(6.dp)); TinyTag(stringResource(R.string.tag_box), filled = item.hasBox)
                    Spacer(Modifier.width(6.dp)); TinyTag(stringResource(R.string.tag_manual), filled = item.hasManual)
                    if (!item.hasBox && !item.hasManual) { Spacer(Modifier.width(6.dp)); LooseTag() }
                    if (item.type == ItemType.CONSOLE && item.condition == Condition.HS) {
                        Spacer(Modifier.width(6.dp))
                        TextButton(
                            onClick = {
                                val url = ConsoleRepairLinks.urlFor(item.name)
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        ) { Text(stringResource(R.string.repair_help_button)) }
                    }
                }
                if (item.type == ItemType.ACCESSOIRE && !item.platform.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.associated_console_label, item.platform),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                if (item.type == ItemType.CONSOLE || item.type == ItemType.ACCESSOIRE) {
                    TextButton(
                        onClick = {
                            val url = if (item.type == ItemType.CONSOLE) ConsoleWikipediaLinks.urlFor(item.name)
                                else AccessoryWikipediaLinks.urlFor(item.name)
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    ) { Text(stringResource(R.string.more_info_button)) }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    formatPrice(item.priceCents) + if (item.priceIsAiEstimate) " (IA)" else "",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan
                )
                if (item.priceCents == null || item.priceIsAiEstimate) {
                    item.info?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(priceChartingSearchUrl(item.name, item.platform))))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.pricecharting_button)) }
                TextButton(onClick = { editingPrice = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.edit_price_button))
                }
                if (item.isWishlist) {
                    // La cote déjà stockée sur la fiche (item.priceCents confirmé, pas une
                    // estimation IA) suffit dans la plupart des cas — mais elle vient d'[EbayPrices.lookup],
                    // filtré sur achat immédiat + état pour une cote FIABLE, qui écarte à tort les
                    // objets rares n'ayant que des annonces aux enchères (constaté en conditions
                    // réelles sur "Sega Game Gear Magic Knight Rayearth"). Sans cote confirmée, on
                    // revérifie donc en direct avec [EbayPrices.hasAnyListing] (aucun filtre
                    // achat immédiat/état, juste "eBay a-t-il AU MOINS une annonce ?") avant de
                    // conclure qu'il n'y a rien et de passer à Google.
                    var offersUrl by remember(item.id) { mutableStateOf<String?>(null) }
                    val hasStoredEbayPrice = item.priceCents != null && !item.priceIsAiEstimate
                    LaunchedEffect(item.id, item.name, item.platform, item.sourceUrl, hasStoredEbayPrice, currentLang) {
                        android.util.Log.d("WishlistOffers", "fiche: type=${item.type} brand=\"${item.brand}\" name=\"${item.name}\" platform=\"${item.platform}\" priceCents=${item.priceCents} priceIsAiEstimate=${item.priceIsAiEstimate}")
                        // La requête utilisée pour ouvrir eBay doit être EXACTEMENT celle qui a été
                        // vérifiée : reconstruire une requête différente (ex. à partir du nom complet)
                        // ici rouvrirait potentiellement une recherche vide, cf. doc de [wishlistOffersUrl].
                        val verifiedEbayQuery = if (hasStoredEbayPrice) {
                            offersSearchQuery(item.name, item.brand, item.platform)
                        } else {
                            runCatching {
                                EbayPrices.findWorkingEbayQuery(item.type, item.brand, item.name, item.platform)
                            }.getOrNull()
                        }
                        android.util.Log.d("WishlistOffers", "hasStoredEbayPrice=$hasStoredEbayPrice verifiedEbayQuery=$verifiedEbayQuery")
                        offersUrl = wishlistOffersUrl(
                            item.name, item.brand, item.platform, item.sourceUrl,
                            verifiedEbayQuery = verifiedEbayQuery,
                            uiLanguage = currentLang
                        )
                        android.util.Log.d("WishlistOffers", "URL finale = $offersUrl")
                    }
                    if (offersUrl != null) {
                        // Couleur "secondary" du thème actif (pas une couleur figée) : reste
                        // cohérent si l'utilisateur a débloqué un autre skin (Retour vers le
                        // Futur, Star Wars...), tout en se distinguant nettement du bouton
                        // "Marquer comme acquis" juste en dessous (couleur "primary").
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(offersUrl))) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.wishlist_offers_button)) }
                        Spacer(Modifier.height(8.dp))
                    }
                    Button(
                        onClick = {
                            if (canMoveToCollection()) {
                                vm.saveCollectionItem(item.copy(isWishlist = false))
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.move_to_collection_button)) }
                }

                if (history.size >= 2) {
                    Spacer(Modifier.height(14.dp))
                    PriceHistoryChart(history)
                }

                if (item.type == ItemType.JEU) {
                    val platformsLabel = stringResource(R.string.platforms_label, item.platform.orEmpty())
                    val genreLabel = stringResource(R.string.genre_label, item.genre.orEmpty())
                    val info = listOfNotNull(
                        item.platform?.takeIf { it.isNotBlank() }?.let { platformsLabel },
                        item.genre?.takeIf { it.isNotBlank() }?.let { genreLabel }
                    )
                    if (info.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        info.forEach {
                            Text(it, color = Color.White, textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        stringResource(R.string.game_trailer_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(youtubeSearchUrl(item.name, item.platform)))
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.youtube_search_button)) }
                    val id = item.rawgId
                    if (id == null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.no_rawg_link),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        var movieUrl by remember(id) { mutableStateOf<String?>(null) }
                        var movieChecked by remember(id) { mutableStateOf(false) }
                        LaunchedEffect(id) {
                            movieUrl = GameCatalog.movieUrl(id)
                            movieChecked = true
                        }
                        when {
                            movieUrl != null -> {
                                Spacer(Modifier.height(8.dp))
                                GameTrailerPlayer(movieUrl!!, Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)))
                            }
                            movieChecked -> Text(
                                stringResource(R.string.no_trailer_available),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            else -> Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                shownDescription?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardGradient)
                            .border(1.dp, NeonBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(it, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                if (galleryPhotos.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Text(
                        stringResource(R.string.gallery_title, galleryPhotos.size),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(galleryPhotos, key = { it.id }) { photo ->
                            AsyncImage(
                                model = photo.uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, NeonBorder, RoundedCornerShape(14.dp))
                                    .clickable { fullscreenUri = photo.uri }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Edit, null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.edit))
                    }
                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Delete, null, tint = NeonPink)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.delete))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    fullscreenUri?.let { uri ->
        Dialog(onDismissRequest = { fullscreenUri = null }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                ZoomableImage(
                    uri = uri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                IconButton(
                    onClick = { fullscreenUri = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Filled.Close, stringResource(R.string.close), tint = Color.White)
                }
            }
        }
    }

    if (editingPrice) {
        var priceText by remember { mutableStateOf(item.priceCents?.let { centsToText(it) } ?: "") }
        AlertDialog(
            onDismissRequest = { editingPrice = false },
            title = { Text(stringResource(R.string.edit_price_button)) },
            text = {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(stringResource(R.string.field_price, CurrencyOptions.symbolFor(AppPrefs.currency.value))) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val cents = parsePriceToCents(priceText)
                    vm.saveCollectionItem(item.copy(priceCents = cents, priceIsManual = cents != null, priceIsAiEstimate = false))
                    editingPrice = false
                }) { Text(stringResource(R.string.save_button)) }
            },
            dismissButton = {
                TextButton(onClick = { editingPrice = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun HeroVisual(item: CollectionItem) {
    val shape = RoundedCornerShape(24.dp)
    if (item.imageUri != null) {
        AsyncImage(
            model = item.imageUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 230.dp, height = 200.dp)
                .clip(shape)
                .border(1.5.dp, NeonBorder, shape)
        )
    } else {
        Box(
            Modifier
                .size(width = 230.dp, height = 200.dp)
                .clip(shape)
                .background(CardGradient)
                .border(1.5.dp, NeonBorder, shape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(typeEmoji(item.type), fontSize = 72.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    item.name.ifBlank { item.type.label },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun rememberTilt(): Pair<Float, Float> {
    val context = LocalContext.current
    var targetX by remember { mutableStateOf(0f) }
    var targetY by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                targetY = (e.values[0] / 9.81f * 14f).coerceIn(-14f, 14f)
                targetX = (-e.values[1] / 9.81f * 14f + 6f).coerceIn(-14f, 14f)
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        if (sensor != null) {
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sm?.unregisterListener(listener) }
    }

    val animX by animateFloatAsState(targetX, label = "tiltX")
    val animY by animateFloatAsState(targetY, label = "tiltY")
    return animX to animY
}

@Composable
private fun PriceHistoryChart(history: List<PriceHistory>) {
    val prices = history.map { it.priceCents }
    val minP = prices.minOrNull() ?: 0
    val maxP = prices.maxOrNull() ?: 0
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardGradient)
            .border(1.dp, NeonBorder, shape)
            .padding(14.dp)
    ) {
        Text(
            stringResource(R.string.price_evolution_title, prices.size),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(Modifier.height(10.dp))
        Canvas(Modifier.fillMaxWidth().height(80.dp)) {
            if (prices.size >= 2) {
                val range = (maxP - minP).coerceAtLeast(1)
                val stepX = size.width / (prices.size - 1)
                val pts = prices.mapIndexed { i, p ->
                    Offset(i * stepX, size.height - ((p - minP).toFloat() / range) * size.height)
                }
                for (i in 0 until pts.size - 1) {
                    drawLine(NeonCyan, pts[i], pts[i + 1], strokeWidth = 5f)
                }
                pts.forEach { drawCircle(NeonPink, radius = 4f, center = it) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.price_min, formatPrice(minP)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.price_current, formatPrice(prices.last())),
                style = MaterialTheme.typography.bodySmall,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.price_max, formatPrice(maxP)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Formulaire : ajouter OU modifier un objet de la collection
// ---------------------------------------------------------------------------

/** Vignette de galerie avec un bouton ✕ pour retirer la photo. */
@Composable
private fun GalleryThumb(uri: String, onRemove: () -> Unit) {
    Box(Modifier.size(84.dp)) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(14.dp))
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(22.dp)
                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(50))
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.remove_photo),
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * Vrai si [candidate] (résultat RAWG) correspond vraiment au titre [query] tapé/corrigé par
 * l'utilisateur, pour ne déclencher l'auto-remplissage de la description que sur un match
 * fiable (pas le premier résultat RAWG venu, qui peut être un titre sans rapport).
 */
private fun gameNameLooksConfident(query: String, candidate: String): Boolean {
    fun words(s: String) = s.lowercase().replace(Regex("[^a-z0-9 ]"), " ").split(" ").filter { it.length >= 3 }.toSet()
    val qWords = words(query)
    if (qWords.isEmpty()) return false
    val overlap = qWords.intersect(words(candidate))
    return overlap.size.toDouble() / qWords.size >= 0.6
}

/**
 * Éditions/rééditions courantes, proposées en liste déroulante pour l'ajout unitaire d'un jeu.
 * Reste volontairement en anglais (comme [GameCatalog.editionKeywords], non traduit) : ce sont des
 * noms commerciaux figés, jamais traduits sur les jaquettes elles-mêmes quelle que soit la langue.
 * "" = aucune édition particulière.
 */
private val editionOptions = listOf(
    "", "Collector", "Platinum", "Greatest Hits", "Player's Choice", "Director's Cut",
    "Deluxe", "Game of the Year", "Complete Edition", "Definitive Edition",
    "Special Edition", "Limited Edition", "Day One Edition", "Ultimate Edition",
    "Essentials", "Classics", "Premium Edition"
)

@Composable
fun AddCollectionForm(
    vm: AppViewModel,
    onSave: (CollectionItem, List<String>) -> Unit,
    onCancel: () -> Unit,
    existing: CollectionItem? = null,
    initialName: String = "",
    initialBarcode: String? = null,
    initialPresetName: String? = null,
    initialAccessoryName: String? = null,
    initialGameMatch: GameInfo? = null,
    initialGameConsoleHint: String? = null,
    initialCoverUri: String? = null,
    /** Photo telle que choisie/prise AVANT recadrage (galerie/caméra) : c'est elle qu'il faut
     * recadrer à nouveau (bouton « Recadrer »), pas le résultat déjà rogné une première fois. */
    initialOriginalCoverUri: String? = null,
    isWishlist: Boolean = false
) {
    val isEdit = existing != null
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var fullscreenCoverUri by remember { mutableStateOf<String?>(null) }
    val customPresets by vm.customPresets.collectAsState()
    // Console reconnue par photo -> pré-remplissage complet depuis le catalogue.
    val recognized = remember(initialPresetName) {
        initialPresetName?.let { n -> consolePresets.firstOrNull { it.name == n } }
    }
    // Accessoire choisi depuis l'encyclopédie -> même pré-remplissage complet (marque, année,
    // console associée, description, photo), pas seulement le nom.
    val recognizedAccessory = remember(initialAccessoryName) {
        initialAccessoryName?.let { n -> accessoryPresets.firstOrNull { it.name == n } }
    }
    var type by remember {
        mutableStateOf(
            existing?.type ?: when {
                recognized != null -> ItemType.CONSOLE
                initialAccessoryName != null -> ItemType.ACCESSOIRE
                initialGameMatch != null -> ItemType.JEU
                else -> AppPrefs.defaultType()
            }
        )
    }
    var name by remember {
        mutableStateOf(existing?.name ?: recognized?.name ?: initialAccessoryName ?: initialGameMatch?.name ?: initialName)
    }
    var brand by remember { mutableStateOf(existing?.brand ?: recognized?.brand ?: recognizedAccessory?.brand ?: initialGameMatch?.publisher ?: "") }
    var region by remember { mutableStateOf(existing?.region ?: Region.PAL) }
    var condition by remember { mutableStateOf(existing?.condition ?: Condition.BON) }
    var hasBox by remember { mutableStateOf(existing?.hasBox ?: true) }
    var hasManual by remember { mutableStateOf(existing?.hasManual ?: true) }
    var year by remember {
        mutableStateOf(existing?.releaseYear?.toString() ?: recognized?.year?.toString() ?: recognizedAccessory?.year?.toString() ?: initialGameMatch?.releaseYear?.toString() ?: "")
    }
    var genre by remember { mutableStateOf(existing?.genre ?: initialGameMatch?.genres?.takeIf { it.isNotBlank() } ?: "") }
    var platform by remember { mutableStateOf(existing?.platform ?: initialGameConsoleHint ?: recognizedAccessory?.console ?: "") }
    // Édition (Collector, Platinum...) : "" = aucune. Prise en compte dans le nom final à
    // l'enregistrement (donc dans la recherche de cote eBay/IA), et affichée à côté du titre
    // comme pour le scan par lot (voir [GameCatalog.preserveEditionSuffix]).
    var edition by remember { mutableStateOf("") }
    var rawgId by remember { mutableStateOf(existing?.rawgId ?: initialGameMatch?.sourceId) }
    var price by remember { mutableStateOf(existing?.priceCents?.let { centsToText(it) } ?: "") }
    var barcode by remember { mutableStateOf(existing?.barcode ?: initialBarcode ?: "") }
    var sourceUrl by remember { mutableStateOf(existing?.sourceUrl ?: "") }
    var importing by remember { mutableStateOf(false) }
    var importFeedback by remember { mutableStateOf<String?>(null) }
    var description by remember {
        mutableStateOf(existing?.description ?: recognized?.specsText() ?: recognizedAccessory?.specsText() ?: initialGameMatch?.description?.takeIf { it.isNotBlank() } ?: "")
    }
    var coverUrl by remember {
        // La photo prise/choisie par l'utilisateur prime toujours sur l'image générique
        // du catalogue ou de RAWG.
        mutableStateOf(
            existing?.imageUri ?: initialCoverUri ?: recognized?.let { ConsoleImages.urlFor(it.name) }
                ?: recognizedAccessory?.let { AccessoryImages.urlFor(it.name) } ?: initialGameMatch?.coverUrl
        )
    }
    var showCatalog by remember { mutableStateOf(false) }
    var showGameSearch by remember { mutableStateOf(false) }
    var showAccessoryCatalog by remember { mutableStateOf(false) }
    var showOnlinePresetSearch by remember { mutableStateOf(false) }
    var pendingEncyclopediaAdd by remember { mutableStateOf<WikiPresetResult?>(null) }
    // Non nul quand une recherche en ligne (GameSearchDialog) renvoie un jeu disponible sur
    // PLUSIEURS consoles : au lieu de tout empiler automatiquement dans "Console associée", on
    // demande à l'utilisateur de cocher celle(s) qu'il possède réellement (même principe que le
    // scan de lot, voir BatchScanDialog/PlatformPickerDialog).
    var pendingPlatformChoice by remember { mutableStateOf<List<String>?>(null) }
    var retryingScan by remember { mutableStateOf(false) }
    // Enregistrement d'une photo (galerie/caméra) juste après recadrage : sans indicateur, l'appli
    // pouvait sembler figée le temps de copier le fichier recadré en stockage interne.
    var cropSaving by remember { mutableStateOf(false) }
    // Photo telle que prise/choisie AVANT recadrage : c'est elle qu'on recadre à nouveau (jamais
    // un recadrage d'un recadrage, qui réduit la marge disponible à chaque tentative).
    var originalCoverUri by remember { mutableStateOf(initialOriginalCoverUri ?: initialCoverUri) }

    // Auto-remplissage quand le nom tapé/sélectionné à la main correspond exactement à une
    // console ou un accessoire du catalogue (même règle que le bouton "Choisir dans le
    // catalogue", sans avoir à l'ouvrir) — utile quand le scan photo n'a pas pu identifier
    // l'objet avec confiance et que la saisie reste manuelle. On ignore le tout premier
    // déclenchement (valeur initiale du champ) pour ne pas écraser les champs déjà
    // personnalisés d'un objet existant en cours de modification.
    // true dès que l'utilisateur a lui-même choisi/pris/recadré une photo : l'auto-remplissage
    // ne doit alors plus jamais y toucher, même si le nom tapé correspond ensuite à une fiche
    // connue (sinon une photo perso se faisait silencieusement remplacer par celle du catalogue).
    var photoManuallySet by remember { mutableStateOf(false) }
    var nameAutoFillArmed by remember { mutableStateOf(false) }
    LaunchedEffect(name, type) {
        if (!nameAutoFillArmed) {
            nameAutoFillArmed = true
            return@LaunchedEffect
        }
        val trimmed = name.trim()
        when (type) {
            ItemType.CONSOLE -> {
                val customMatch = customPresets.firstOrNull { it.type == ItemType.CONSOLE && it.name.equals(trimmed, ignoreCase = true) }
                if (customMatch != null) {
                    brand = customMatch.brand
                    year = (customMatch.year ?: 0).takeIf { it != 0 }?.toString() ?: ""
                    description = customMatch.description
                    if (!photoManuallySet) customMatch.photoUri?.let { coverUrl = it }
                } else {
                    var preset = consolePresets.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
                        ?: ConsoleRecognition.exactAliasMatch(trimmed)?.let { resolved ->
                            consolePresets.firstOrNull { it.name == resolved }
                        }
                    if (preset == null && trimmed.length >= 3) {
                        // Pas de correspondance exacte/alias strict : on attend une courte pause
                        // sans nouvelle saisie (annulée/relancée si le nom change encore) avant la
                        // recherche floue multi-mots-clés, pour ne pas faire clignoter les champs
                        // sur un résultat encore très partiel pendant la frappe (ex. "play...").
                        delay(400)
                        preset = consolePresets
                            .filter {
                                ConsoleRecognition.matchesQuery(listOf(it.name, it.brand) + ConsoleRecognition.aliasSearchTerms(it.name), trimmed)
                            }
                            .minByOrNull { ConsoleRecognition.specificity(it.name, it.brand) }
                    }
                    preset?.let {
                        brand = it.brand
                        year = it.year.toString()
                        description = it.specsText()
                        if (!photoManuallySet) ConsoleImages.urlFor(it.name)?.let { url -> coverUrl = url }
                    }
                }
            }
            ItemType.ACCESSOIRE -> {
                val customMatch = customPresets.firstOrNull { it.type == ItemType.ACCESSOIRE && it.name.equals(trimmed, ignoreCase = true) }
                if (customMatch != null) {
                    brand = customMatch.brand
                    year = (customMatch.year ?: 0).takeIf { it != 0 }?.toString() ?: ""
                    description = customMatch.description
                    if (platform.isBlank()) platform = customMatch.consoleOrKind
                    if (!photoManuallySet) customMatch.photoUri?.let { coverUrl = it }
                } else {
                    var preset = accessoryPresets.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
                    if (preset == null && trimmed.length >= 3) {
                        delay(400)
                        preset = accessoryPresets
                            .filter { ConsoleRecognition.matchesQuery(listOf(it.name, it.brand, it.console), trimmed) }
                            .minByOrNull { ConsoleRecognition.specificity(it.name, it.brand, it.console) }
                    }
                    preset?.let {
                        brand = it.brand
                        year = it.year.toString()
                        description = it.specsText()
                        if (platform.isBlank()) platform = it.console
                        if (!photoManuallySet) AccessoryImages.urlFor(it.name)?.let { coverUrl = it }
                    }
                }
            }
            ItemType.JEU -> if (trimmed.length >= 3) {
                // Pas d'appel réseau à chaque frappe : on attend une courte pause sans nouvelle
                // saisie avant de chercher en ligne (annulé/relancé automatiquement par
                // LaunchedEffect si le nom change encore pendant ce délai).
                delay(500)
                val platformId = platform.ifBlank { null }?.let { ConsolePlatforms.platformId(it) }
                val results = runCatching { GameCatalog.search(trimmed, platformId) }.getOrNull().orEmpty()
                val match = results.firstOrNull { gameNameLooksConfident(trimmed, it.name) }
                if (match != null) {
                    val detailed = match.sourceId?.let { id -> runCatching { GameCatalog.detail(id) }.getOrNull() } ?: match
                    if (detailed.genres.isNotBlank()) genre = detailed.genres
                    detailed.releaseYear?.let { year = it.toString() }
                    if (detailed.description.isNotBlank()) description = detailed.description
                    detailed.publisher?.let { brand = it }
                    if (detailed.platforms.isNotBlank()) platform = ConsoleRecognition.canonicalizePlatformList(detailed.platforms)
                    if (!photoManuallySet) {
                        // La photo principale d'un jeu doit être sa JAQUETTE. RAWG ne renvoie
                        // qu'une capture de gameplay (background_image) : on tente d'abord la vraie
                        // jaquette IGDB (cover art), avec repli sur l'image RAWG si introuvable.
                        val jaquette = IgdbCatalog.coverUrlFor(detailed.name, detailed.releaseYear) ?: detailed.coverUrl
                        if (!photoManuallySet) jaquette?.let { coverUrl = it }
                    }
                    rawgId = detailed.sourceId
                }
            }
        }
    }

    // Galerie de photos supplémentaires : déjà en base si l'objet existe,
    // sinon gardées en mémoire jusqu'à l'enregistrement (pas d'id tant que l'objet n'existe pas).
    val savedGalleryPhotos by vm.photosFor(existing?.id ?: -1L).collectAsState(initial = emptyList())
    var pendingGalleryPhotos by remember { mutableStateOf<List<String>>(emptyList()) }

    // Recadrage : toute photo destinée à la couverture ou prise à la caméra passe par là
    // pour un meilleur cadrage avant d'être conservée (boîte/jaquette/console bien centrée).
    var cropForGallery by remember { mutableStateOf(false) }
    var pendingOriginalUri by remember { mutableStateOf<Uri?>(null) }
    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val croppedUri = result.uriContent
            if (croppedUri != null) {
                scope.launch {
                    cropSaving = true
                    val saved = withContext(Dispatchers.IO) { MediaUtils.copyToInternal(context, croppedUri) }
                    if (saved != null) {
                        if (cropForGallery) {
                            if (existing != null) {
                                vm.addPhotos(existing.id, listOf(saved))
                            } else {
                                pendingGalleryPhotos = pendingGalleryPhotos + saved
                            }
                            // La première photo ajoutée (s'il n'y en a pas déjà une) devient
                            // la photo principale par défaut, affichée dans les listes/la fiche.
                            if (coverUrl.isNullOrBlank()) { coverUrl = saved; photoManuallySet = true }
                        } else {
                            coverUrl = saved
                            photoManuallySet = true
                            // Garde une copie stable de la photo AVANT ce recadrage, pour que
                            // « Recadrer » reparte de la vraie source la prochaine fois plutôt
                            // que de recadrer un recadrage (marge de cadrage qui se réduit).
                            val origUri = pendingOriginalUri
                            originalCoverUri = if (origUri != null) {
                                withContext(Dispatchers.IO) { MediaUtils.copyToInternal(context, origUri) } ?: saved
                            } else saved
                        }
                    }
                    cropSaving = false
                }
            }
        }
    }
    fun launchCrop(uri: Uri, forGallery: Boolean) {
        cropForGallery = forGallery
        pendingOriginalUri = uri
        cropLauncher.launch(CropImageContractOptions(uri, CropImageOptions(activityMenuIconColor = android.graphics.Color.WHITE, cropMenuCropButtonTitle = "OK", initialCropWindowPaddingRatio = 0.2f)))
    }

    // Applique une piste de reconnaissance (résultat initial ou alternative) aux champs du
    // formulaire — factorisé pour être utilisé à la fois après un nouveau recadrage et par le
    // bouton « Réessayer » (qui applique juste l'alternative suivante, sans recadrer).
    fun applyScanResult(r: ScanTools.ScanResult?) {
        when {
            r?.gameMatch != null -> {
                type = ItemType.JEU
                name = r.gameMatch.name
                if (r.gameMatch.genres.isNotBlank()) genre = r.gameMatch.genres
                r.gameMatch.releaseYear?.let { year = it.toString() }
                if (r.gameMatch.description.isNotBlank()) description = r.gameMatch.description
                r.gameMatch.publisher?.let { brand = it }
                // Liste complète des consoles compatibles (IGDB/RAWG) si connue ; sinon on garde
                // au moins la console repérée sur la boîte/cartouche (OCR/IA), qui peut être la
                // seule info disponible pour un jeu absent de ces catalogues.
                platform = r.gameMatch.platforms.takeIf { it.isNotBlank() }?.let { ConsoleRecognition.canonicalizePlatformList(it) }
                    ?: r.gameConsoleHint ?: platform
                rawgId = r.gameMatch.sourceId
            }
            r?.consolePresetName != null -> {
                type = ItemType.CONSOLE
                name = r.consolePresetName
            }
            r?.accessoryName != null -> {
                type = ItemType.ACCESSOIRE
                name = r.accessoryName
            }
            else -> {
                name = r?.suggestedName ?: name
            }
        }
        r?.barcode?.let { barcode = it }
    }

    // "Recadrer" : un nouveau recadrage de LA PHOTO D'ORIGINE (jamais d'un recadrage déjà fait,
    // qui réduit la marge disponible à chaque tentative) peut suffire à faire trouver la bonne
    // correspondance à l'OCR (texte mieux cadré) — on efface les champs déduits de la tentative
    // précédente avant de relancer l'analyse complète.
    val recropAndRescanLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val croppedUri = result.uriContent
            if (croppedUri != null) {
                scope.launch {
                    retryingScan = true
                    val saved = withContext(Dispatchers.IO) { MediaUtils.copyToInternal(context, croppedUri) }
                    if (saved != null) {
                        coverUrl = saved
                        photoManuallySet = true
                        val r = runCatching { ScanTools.scanImage(context, Uri.parse(saved)) }.getOrNull()
                        applyScanResult(r)
                    }
                    retryingScan = false
                }
            }
        }
    }
    fun recropAndRescan() {
        val source = originalCoverUri ?: coverUrl ?: return
        name = ""; brand = ""; year = ""; genre = ""; platform = ""; barcode = ""; description = ""
        rawgId = null
        type = AppPrefs.defaultType()
        recropAndRescanLauncher.launch(
            CropImageContractOptions(Uri.parse(source), CropImageOptions(activityMenuIconColor = android.graphics.Color.WHITE, cropMenuCropButtonTitle = "OK", initialCropWindowPaddingRatio = 0.2f))
        )
    }

    // Sélecteur de photo personnelle (galerie, sans permission).
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) launchCrop(uri, forGallery = false)
    }

    // Sélecteur de photo pour la galerie (boîte, dos, défauts, accessoires...). Une photo à la
    // fois (pas de multi-sélection) pour pouvoir recadrer chacune avant de l'ajouter.
    val galleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) launchCrop(uri, forGallery = true)
    }

    // Prise de photo directe à la caméra, pour la photo principale OU pour la galerie.
    // rememberSaveable (pas remember) : tourner le téléphone pendant que l'appli caméra système
    // est ouverte peut faire tuer notre activité par le système (mémoire), ce qui recréait cet
    // état à null et faisait échouer silencieusement le retour de la photo prise ("la capture se
    // ferme") — rememberSaveable survit à cette recréation, contrairement à remember.
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraForGallery by rememberSaveable { mutableStateOf(false) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val pending = pendingCameraUri
        pendingCameraUri = null
        if (success && pending != null) {
            launchCrop(Uri.parse(pending), forGallery = cameraForGallery)
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val pair = MediaUtils.newCameraFile(context)
            pendingCameraUri = pair.first.toString()
            cameraLauncher.launch(pair.first)
        }
    }
    fun launchCamera(forGallery: Boolean) {
        cameraForGallery = forGallery
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val pair = MediaUtils.newCameraFile(context)
            pendingCameraUri = pair.first.toString()
            cameraLauncher.launch(pair.first)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showCatalog) {
        ConsolePickerDialog(
            customPresets = customPresets,
            initialQuery = name,
            onPick = { preset ->
                brand = preset.brand
                name = preset.name
                year = preset.year.toString()
                description = preset.specsText()
                showCatalog = false
                // Image de la console : vignette Sketchfab si dispo, sinon Wikipédia best-effort.
                // Ne jamais écraser une photo déjà choisie à la main par l'utilisateur.
                if (!photoManuallySet) {
                    val img = ConsoleImages.urlFor(preset.name)
                    if (img != null) {
                        coverUrl = img
                    } else {
                        scope.launch {
                            ImageSearch.consoleImage(preset.brand, preset.name)?.let { coverUrl = it }
                        }
                    }
                }
            },
            onPickCustom = { preset ->
                brand = preset.brand
                name = preset.name
                year = (preset.year ?: 0).takeIf { it != 0 }?.toString() ?: ""
                description = preset.description
                showCatalog = false
                if (!photoManuallySet) preset.photoUri?.let { coverUrl = it }
            },
            onDismiss = { showCatalog = false }
        )
    }
    if (showAccessoryCatalog) {
        AccessoryPickerDialog(
            customPresets = customPresets,
            initialQuery = name,
            consoleHint = platform.ifBlank { null },
            onPick = { preset ->
                brand = preset.brand
                name = preset.name
                year = preset.year.toString()
                platform = preset.console
                description = preset.specsText()
                showAccessoryCatalog = false
                if (!photoManuallySet) AccessoryImages.urlFor(preset.name)?.let { coverUrl = it }
            },
            onPickCustom = { preset ->
                brand = preset.brand
                name = preset.name
                year = (preset.year ?: 0).takeIf { it != 0 }?.toString() ?: ""
                platform = preset.consoleOrKind
                description = preset.description
                showAccessoryCatalog = false
                if (!photoManuallySet) preset.photoUri?.let { coverUrl = it }
            },
            onDismiss = { showAccessoryCatalog = false }
        )
    }
    if (showOnlinePresetSearch) {
        OnlinePresetSearchDialog(
            title = if (type == ItemType.CONSOLE) stringResource(R.string.search_console_online) else stringResource(R.string.search_accessory_online),
            initialQuery = name,
            consoleHint = if (type == ItemType.ACCESSOIRE) platform.ifBlank { null } else null,
            onPick = { result ->
                name = result.title
                description = result.description
                showOnlinePresetSearch = false
                if (!photoManuallySet) result.photoUrl?.let { coverUrl = it }
                pendingEncyclopediaAdd = result
            },
            onDismiss = { showOnlinePresetSearch = false }
        )
    }
    pendingEncyclopediaAdd?.let { result ->
        AlertDialog(
            onDismissRequest = { pendingEncyclopediaAdd = null },
            title = { Text(stringResource(R.string.add_to_encyclopedia_title)) },
            text = { Text(stringResource(R.string.add_to_encyclopedia_message, result.title)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.addCustomPreset(
                        CustomPreset(
                            type = type,
                            brand = brand.ifBlank { "?" },
                            name = result.title,
                            year = null,
                            consoleOrKind = if (type == ItemType.CONSOLE) "Salon" else platform.ifBlank { "?" },
                            description = result.description,
                            photoUri = result.photoUrl
                        )
                    )
                    pendingEncyclopediaAdd = null
                }) { Text(stringResource(R.string.add_to_encyclopedia_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingEncyclopediaAdd = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
    if (showGameSearch) {
        GameSearchDialog(
            consoleHint = platform.ifBlank { null },
            initialQuery = name,
            onPick = { game ->
                // Le titre déjà tapé n'est remplacé par celui du résultat choisi que s'ils
                // partagent au moins un mot significatif : sinon, le résultat vient forcément
                // d'une correspondance multilingue (alias connu de RAWG, ou traduction automatique
                // de la cascade de recherche, cf. [SearchCascade]) — remplacer donnerait un titre
                // dans une langue différente de celle voulue (ex. garder "Max et les monstres"
                // plutôt que le "Where the Wild Things Are" qui a servi à le retrouver). Le reste
                // de la fiche (genre/année/description/jaquette...) est complété dans tous les cas.
                if (sharesSignificantWord(name, game.name)) {
                    name = GameCatalog.preserveEditionSuffix(name, game.name)
                }
                if (game.genres.isNotBlank()) genre = game.genres
                game.releaseYear?.let { year = it.toString() }
                if (game.description.isNotBlank()) description = game.description
                game.publisher?.let { brand = it }
                val platformOptions = game.platforms.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map { ConsoleRecognition.canonicalize(it) }
                    .distinct()
                when {
                    platformOptions.size > 1 -> pendingPlatformChoice = platformOptions
                    platformOptions.size == 1 -> platform = platformOptions.first()
                }
                if (!photoManuallySet) {
                    // Jaquette IGDB déjà = vraie couverture ; sinon (résultat RAWG = capture de
                    // gameplay, ou Wikipédia) on va chercher la jaquette IGDB correspondante.
                    coverUrl = game.coverUrl
                    if (game.source != "igdb") {
                        scope.launch {
                            val jaquette = IgdbCatalog.coverUrlFor(game.name, game.releaseYear)
                            if (jaquette != null && !photoManuallySet) coverUrl = jaquette
                        }
                    }
                }
                rawgId = game.sourceId
                showGameSearch = false
            },
            onDismiss = { showGameSearch = false }
        )
    }
    pendingPlatformChoice?.let { options ->
        PlatformPickerDialog(
            platforms = options,
            onConfirm = { selected ->
                platform = selected.joinToString(", ")
                pendingPlatformChoice = null
            },
            onDismiss = { pendingPlatformChoice = null }
        )
    }

    FormScaffold(
        title = when {
            isEdit -> stringResource(R.string.edit_item_title)
            isWishlist -> stringResource(R.string.new_wish_title)
            else -> stringResource(R.string.add_item_title)
        },
        onCancel = onCancel
    ) {
        if (!isEdit && (initialBarcode != null || initialName.isNotBlank())) {
            Text(
                stringResource(R.string.scan_detected_hint),
                style = MaterialTheme.typography.bodySmall,
                color = NeonCyan
            )
        }
        LabeledDropdown(stringResource(R.string.field_type), ItemType.values().toList(), type, { "${typeEmoji(it)} ${it.label}" }) { type = it }
        if (type == ItemType.CONSOLE) {
            OutlinedButton(
                onClick = { showCatalog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.choose_from_catalog, consolePresets.size + customPresets.count { it.type == ItemType.CONSOLE }))
            }
            OutlinedButton(
                onClick = { showOnlinePresetSearch = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.search_console_online))
            }
        }
        if (type == ItemType.JEU) {
            OutlinedButton(
                onClick = { showGameSearch = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.search_game_online))
            }
        }
        if (type == ItemType.ACCESSOIRE) {
            OutlinedButton(
                onClick = { showAccessoryCatalog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.choose_from_catalog, accessoryPresets.size + customPresets.count { it.type == ItemType.ACCESSOIRE }))
            }
            OutlinedButton(
                onClick = { showOnlinePresetSearch = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.search_accessory_online))
            }
        }
        // Import automatique via une URL source (fiche produit, Wikipédia, annonce…) : ne remplit
        // que les champs encore vides (enrichissement, jamais écrasement d'une saisie existante).
        val importOkMsg = stringResource(R.string.import_url_success)
        val importFailMsg = stringResource(R.string.import_url_failed)
        Field(sourceUrl, stringResource(R.string.field_source_url), KeyboardType.Uri) {
            sourceUrl = it; importFeedback = null
        }
        OutlinedButton(
            onClick = {
                scope.launch {
                    importing = true
                    val info = UrlImport.fetch(sourceUrl)
                    importing = false
                    if (info == null) {
                        importFeedback = importFailMsg
                    } else {
                        if (name.isBlank()) info.name?.let { name = it }
                        if (description.isBlank()) info.description?.let { description = it }
                        if (year.isBlank()) info.year?.let { year = it.toString() }
                        if (!photoManuallySet && coverUrl.isNullOrBlank()) info.imageUrl?.let { coverUrl = it }
                        importFeedback = importOkMsg
                    }
                }
            },
            enabled = !importing && UrlImport.looksLikeUrl(sourceUrl),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (importing) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(stringResource(R.string.import_from_url_button))
        }
        importFeedback?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = NeonCyan)
        }

        OutlinedButton(
            onClick = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.item_photo_gallery))
        }
        OutlinedButton(
            onClick = { launchCamera(forGallery = false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.take_photo_option))
        }
        if (cropSaving) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = NeonCyan)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.photo_saving), color = Color.White)
            }
        }
        if (coverUrl != null) {
            AsyncImage(
                model = coverUrl,
                contentDescription = stringResource(R.string.photo_content_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { fullscreenCoverUri = coverUrl }
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                TextButton(onClick = { coverUrl = null }) {
                    Text(stringResource(R.string.remove_photo_button))
                }
                if (coverUrl?.startsWith("file://") == true) {
                    TextButton(
                        onClick = { recropAndRescan() },
                        enabled = !retryingScan
                    ) {
                        if (retryingScan) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = NeonCyan)
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(stringResource(R.string.recrop_button))
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.gallery_section_title),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(savedGalleryPhotos, key = { "saved_${it.id}" }) { photo ->
                GalleryThumb(
                    uri = photo.uri,
                    onRemove = { vm.deletePhoto(photo) }
                )
            }
            items(pendingGalleryPhotos, key = { "pending_$it" }) { uri ->
                GalleryThumb(
                    uri = uri,
                    onRemove = {
                        pendingGalleryPhotos = pendingGalleryPhotos - uri
                        MediaUtils.deleteFile(uri)
                    }
                )
            }
            item {
                Box(
                    Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardGradient)
                        .border(1.dp, NeonBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            galleryPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, stringResource(R.string.add_photos), tint = NeonCyan)
                }
            }
            item {
                Box(
                    Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardGradient)
                        .border(1.dp, NeonBorder, RoundedCornerShape(14.dp))
                        .clickable { launchCamera(forGallery = true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CameraAlt, stringResource(R.string.take_photo_option), tint = NeonCyan)
                }
            }
        }

        if (type == ItemType.CONSOLE) {
            // Saisie assistée du nom de console : liste déroulante des fiches de l'encyclopédie
            // (catalogue intégré + ajouts perso). Taper « Game & Watch » déroule tous les modèles
            // (Ball, Fire, Zelda, séries Panorama/Table Top, rééditions…), chacun pointant vers
            // une fiche console pré-existante qui pré-remplit alors marque/année/description.
            val consoleNameOptions = remember(customPresets) {
                (consolePresets.map { it.name } +
                    customPresets.filter { it.type == ItemType.CONSOLE }.map { it.name })
                    .distinct().sorted()
            }
            AutocompleteField(
                value = name,
                label = stringResource(R.string.field_name),
                suggestions = consoleNameOptions,
                onValueChange = { name = it }
            )
        } else {
            Field(name, stringResource(R.string.field_name)) { name = it }
        }
        AutocompleteField(
            value = brand,
            label = stringResource(R.string.field_brand),
            suggestions = remember(AppPrefs.customBrands.value) { (KnownBrands.list + AppPrefs.customBrands.value).distinct().sorted() },
            onValueChange = { brand = it }
        )
        LabeledDropdown(stringResource(R.string.field_region), Region.values().toList(), region, { it.label }) { region = it }
        LabeledDropdown(stringResource(R.string.field_condition), Condition.values().toList(), condition, { it.label }) { condition = it }

        SwitchRow(stringResource(R.string.switch_has_box), hasBox) { hasBox = it }
        SwitchRow(stringResource(R.string.switch_has_manual), hasManual) { hasManual = it }

        Field(year, stringResource(R.string.field_release_year), KeyboardType.Number) {
            year = it.filter { c -> c.isDigit() }.take(4)
        }
        if (type == ItemType.JEU || type == ItemType.ACCESSOIRE) {
            val consoleOptions = remember { consolePresets.map { it.name } }
            AutocompleteField(
                value = platform,
                label = if (type == ItemType.JEU) stringResource(R.string.field_platforms)
                    else stringResource(R.string.field_associated_console),
                suggestions = consoleOptions,
                // Résout une abréviation ou variante d'orthographe tapée (ex. "ps2", "playstation2")
                // vers le nom canonique du preset ("PlayStation 2") : sans ça, la plateforme restait
                // telle quelle, ce qui la faisait échapper au catalogue, à la recherche de cote
                // (eBay/IA) et au regroupement par console (deux libellés pour la même console).
                onValueChange = { platform = ConsoleRecognition.canonicalizeOrNull(it.trim()) ?: it }
            )
            if (type == ItemType.JEU) {
                Field(genre, stringResource(R.string.field_genre)) { genre = it }
                val noneLabel = stringResource(R.string.edition_none)
                LabeledDropdown(
                    stringResource(R.string.field_edition),
                    editionOptions,
                    edition,
                    { it.ifBlank { noneLabel } }
                ) { edition = it }
            }
        }
        Field(price, stringResource(R.string.field_price, CurrencyOptions.symbolFor(AppPrefs.currency.value)), KeyboardType.Decimal) { price = it }
        Field(barcode, stringResource(R.string.field_barcode)) { barcode = it }
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.field_description)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(6.dp))
        var isSaving by remember { mutableStateOf(false) }
        Button(
            onClick = {
                AppPrefs.setLastType(context, type)
                AppPrefs.addCustomBrand(context, brand)
                val typedPriceCents = parsePriceToCents(price)
                isSaving = true
                scope.launch {
                    // Une couverture encore distante (jaquette IGDB/RAWG, photo Wikipédia trouvée
                    // par la recherche...) est rapatriée en stockage interne avant d'enregistrer —
                    // sinon la fiche ne garde qu'un lien internet qui peut expirer/disparaître plus
                    // tard, faisant "perdre" la photo alors que la base n'a jamais été touchée.
                    val cover = coverUrl
                    val localCover = if (cover != null && (cover.startsWith("http://", true) || cover.startsWith("https://", true))) {
                        MediaUtils.downloadToInternal(context, cover) ?: cover
                    } else cover
                    // Édition choisie (Collector, Platinum...) ajoutée au nom final : entre dans la
                    // recherche de cote (eBay/IA n'utilisent que ce champ) et s'affiche à côté du
                    // titre partout où il est montré, comme pour un jeu détecté par scan par lot.
                    val trimmedName = name.trim()
                    val finalName = if (edition.isNotBlank() && !trimmedName.lowercase().contains(edition.lowercase())) {
                        "$trimmedName ($edition)"
                    } else trimmedName
                    onSave(
                        CollectionItem(
                            id = existing?.id ?: 0,
                            type = type,
                            name = finalName,
                            brand = brand.trim(),
                            region = region,
                            condition = condition,
                            hasBox = hasBox,
                            hasManual = hasManual,
                            releaseYear = year.toIntOrNull(),
                            genre = genre.ifBlank { null },
                            platform = platform.ifBlank { null },
                            priceCents = typedPriceCents,
                            // Un prix saisi ici prime sur la cote en ligne et ne sera plus jamais
                            // écrasé automatiquement. Laisser le champ vide pour une cote auto eBay.
                            priceIsManual = typedPriceCents != null,
                            barcode = barcode.ifBlank { null },
                            description = description.ifBlank { null },
                            imageUri = localCover,
                            sourceUrl = sourceUrl.trim().ifBlank { null },
                            rawgId = rawgId,
                            isWishlist = existing?.isWishlist ?: isWishlist,
                            createdAt = existing?.createdAt ?: 0
                        ),
                        pendingGalleryPhotos
                    )
                    isSaving = false
                }
            },
            enabled = name.isNotBlank() && !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    when {
                        isEdit -> stringResource(R.string.update_button)
                        isWishlist -> stringResource(R.string.add_to_wishlist_button)
                        else -> stringResource(R.string.save_button)
                    }
                )
            }
        }
    }

    fullscreenCoverUri?.let { uri ->
        Dialog(onDismissRequest = { fullscreenCoverUri = null }) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                ZoomableImage(
                    uri = uri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                IconButton(
                    onClick = { fullscreenCoverUri = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Filled.Close, stringResource(R.string.close), tint = Color.White)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tutoriel de premier lancement
// ---------------------------------------------------------------------------

private data class OnboardingPage(val title: String, val text: String)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(stringResource(R.string.onboarding_page1_title), stringResource(R.string.onboarding_page1_text)),
        OnboardingPage(stringResource(R.string.onboarding_page2_title), stringResource(R.string.onboarding_page2_text)),
        OnboardingPage(stringResource(R.string.onboarding_page3_title), stringResource(R.string.onboarding_page3_text)),
        OnboardingPage(stringResource(R.string.onboarding_page11_title), stringResource(R.string.onboarding_page11_text)),
        OnboardingPage(stringResource(R.string.onboarding_page10_title), stringResource(R.string.onboarding_page10_text)),
        OnboardingPage(stringResource(R.string.onboarding_page13_title), stringResource(R.string.onboarding_page13_text)),
        OnboardingPage(stringResource(R.string.onboarding_page6_title), stringResource(R.string.onboarding_page6_text)),
        OnboardingPage(stringResource(R.string.onboarding_page5_title), stringResource(R.string.onboarding_page5_text)),
        OnboardingPage(stringResource(R.string.onboarding_page4_title), stringResource(R.string.onboarding_page4_text)),
        OnboardingPage(stringResource(R.string.onboarding_page14_title), stringResource(R.string.onboarding_page14_text)),
        OnboardingPage(stringResource(R.string.onboarding_page12_title), stringResource(R.string.onboarding_page12_text)),
        OnboardingPage(stringResource(R.string.onboarding_page8_title), stringResource(R.string.onboarding_page8_text)),
        OnboardingPage(stringResource(R.string.onboarding_page9_title), stringResource(R.string.onboarding_page9_text)),
        OnboardingPage(stringResource(R.string.onboarding_page7_title), stringResource(R.string.onboarding_page7_text))
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    GamerScreenBackground {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Le défilement tactile gauche/droite permet de naviguer entre les pages, en plus
            // du bouton « Suivant » — plus naturel pour un tutoriel de ce type.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { page ->
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        pages[page].title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        pages[page].text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                pages.indices.forEach { i ->
                    Box(
                        Modifier
                            .size(if (i == pagerState.currentPage) 10.dp else 7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (i == pagerState.currentPage) NeonCyan else Color.White.copy(alpha = 0.25f))
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onFinish) { Text(stringResource(R.string.onboarding_skip)) }
                Button(onClick = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onFinish()
                    }
                }) {
                    Text(if (pagerState.currentPage < pages.lastIndex) stringResource(R.string.onboarding_next) else stringResource(R.string.onboarding_finish))
                }
            }
        }
    }
}

/**
 * Tutoriel de premier lancement, version courte/visuelle (7 étapes, 1 phrase par étape) — remplace
 * [OnboardingScreen] (14 pages de texte dense, retour utilisateur : trop de lecture, personne ne
 * prend le temps de tout lire). Idée : un gros pictogramme animé (pulsation) plutôt qu'un mur de
 * texte, une seule phrase courte par étape ; les fonctionnalités avancées (import Excel, import URL,
 * astuce Game &amp; Watch, sélection multiple...) ne sont plus détaillées ici, seulement mentionnées
 * comme "à découvrir dans les Réglages" — elles restent couvertes par l'onglet Astuces existant.
 *
 * ISOLÉ à l'édition `restricted` (V2test) tant qu'il n'est pas validé, cf. appel conditionné par
 * `BuildConfig.IS_TEST` dans [MainActivity] : `full` et `noads` (V2SP) continuent d'utiliser
 * l'ancien [OnboardingScreen], inchangé.
 */
private data class OnboardingLightStep(val emoji: String, val titleRes: Int, val textRes: Int)

@Composable
fun OnboardingScreenLight(onFinish: () -> Unit) {
    val steps = listOf(
        OnboardingLightStep("🎮", R.string.onboarding_light_step1_title, R.string.onboarding_light_step1_text),
        OnboardingLightStep("📷", R.string.onboarding_light_step2_title, R.string.onboarding_light_step2_text),
        OnboardingLightStep("📚", R.string.onboarding_light_step3_title, R.string.onboarding_light_step3_text),
        OnboardingLightStep("💎", R.string.onboarding_light_step4_title, R.string.onboarding_light_step4_text),
        OnboardingLightStep("🕹️", R.string.onboarding_light_step5_title, R.string.onboarding_light_step5_text),
        OnboardingLightStep("⚙️", R.string.onboarding_light_step6_title, R.string.onboarding_light_step6_text),
        OnboardingLightStep("🚀", R.string.onboarding_light_step7_title, R.string.onboarding_light_step7_text)
    )
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val scope = rememberCoroutineScope()

    // Légère pulsation continue du pictogramme (halo + emoji) : donne un côté "vivant"/ludique
    // sans avoir besoin d'assets image ou vidéo (hors de portée ici, cf. discussion).
    val pulse = rememberInfiniteTransition(label = "onboarding_pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onboarding_pulse_scale"
    )

    GamerScreenBackground {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { page ->
                val step = steps[page]
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        Modifier.size(180.dp).graphicsLayer { scaleX = pulseScale; scaleY = pulseScale },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(90.dp))
                                .background(
                                    Brush.radialGradient(
                                        listOf(NeonPurple.copy(alpha = 0.5f), Color.Transparent)
                                    )
                                )
                        )
                        Text(step.emoji, fontSize = 76.sp)
                    }
                    Spacer(Modifier.height(28.dp))
                    Text(
                        stringResource(step.titleRes),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(step.textRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                steps.indices.forEach { i ->
                    Box(
                        Modifier
                            .size(if (i == pagerState.currentPage) 10.dp else 7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (i == pagerState.currentPage) NeonCyan else Color.White.copy(alpha = 0.25f))
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onFinish) { Text(stringResource(R.string.onboarding_skip)) }
                Button(onClick = {
                    if (pagerState.currentPage < steps.lastIndex) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onFinish()
                    }
                }) {
                    Text(if (pagerState.currentPage < steps.lastIndex) stringResource(R.string.onboarding_next) else stringResource(R.string.onboarding_finish))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sauvegarde / restauration de la collection (fichier .zip choisi par l'utilisateur)
// ---------------------------------------------------------------------------

/**
 * Sélecteur de document d'import identique à [ActivityResultContracts.OpenDocument] (donc filtré
 * sur les types passés au lancement, ici « application/zip »), mais qui s'ouvre par défaut dans le
 * dossier de la dernière sauvegarde exportée ([initialUri]) — l'utilisateur retrouve directement
 * ses .zip compatibles au lieu de repartir de « Récents ».
 */
private class OpenBackupDocument(
    private val initialUri: () -> Uri?
) : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: Context, input: Array<String>): Intent {
        val intent = super.createIntent(context, input)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initialUri()?.let { intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, it) }
        }
        return intent
    }
}

@Composable
fun BackupScreen(vm: AppViewModel, gameVm: GameViewModel, onOpenShop: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var working by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var confirmRestoreUri by remember { mutableStateOf<Uri?>(null) }
    val backupSuccessMessage = stringResource(R.string.backup_success_message)
    val backupErrorMessage = stringResource(R.string.backup_error_message)
    val restoreSuccessMessage = stringResource(R.string.restore_success_message)
    val restoreErrorMessage = stringResource(R.string.restore_error_message)
    val excelSuccessMessage = stringResource(R.string.excel_export_success_message)
    val excelErrorMessage = stringResource(R.string.excel_export_error_message)
    val importParseError = stringResource(R.string.import_parse_error)

    // Import d'un fichier Excel/CSV externe (voir SpreadsheetImport) : lecture -> correspondance
    // des colonnes -> aperçu/validation -> insertion. Trois écrans successifs, chacun repose sur
    // l'état renseigné par le précédent (null = pas encore à cette étape).
    var pendingSheet by remember { mutableStateOf<SpreadsheetImport.ParsedSheet?>(null) }
    var pendingPreviewItems by remember { mutableStateOf<List<CollectionItem>?>(null) }
    val importSaving by vm.batchSaving.collectAsState()

    // Gamification (MaCollectionV2) : quota d'exports gratuits + déblocage de l'export Excel.
    val isPremium by gameVm.isPremiumAllAccess.collectAsState()
    val backupExportCount by AppPrefs.backupExportCount
    val extraBackupPacks by AppPrefs.extraBackupPacks
    val excelUnlocked by gameVm.isExcelExportUnlocked.collectAsState()
    val remainingBackups = if (isPremium) Int.MAX_VALUE else {
        (GameShopCatalog.FREE_BACKUP_EXPORTS + extraBackupPacks * GameShopCatalog.BACKUP_PACK_SIZE - backupExportCount)
            .coerceAtLeast(0)
    }
    val canExportBackupFree = isPremium || remainingBackups > 0

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            working = true
            scope.launch {
                val ok = vm.exportBackup(uri)
                working = false
                if (ok) {
                    gameVm.recordBackupExport()
                    AppPrefs.setLastBackupUri(context, uri.toString()) // dossier initial de l'import
                }
                resultMessage = if (ok) backupSuccessMessage else backupErrorMessage
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        remember { OpenBackupDocument { AppPrefs.lastBackupUri.value?.let(Uri::parse) } }
    ) { uri ->
        if (uri != null) confirmRestoreUri = uri
    }
    val excelExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.ms-excel")
    ) { uri ->
        if (uri != null) {
            working = true
            scope.launch {
                val ok = vm.exportExcel(uri)
                working = false
                resultMessage = if (ok) excelSuccessMessage else excelErrorMessage
            }
        }
    }
    val importSheetLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            working = true
            scope.launch {
                val sheet = withContext(Dispatchers.IO) { SpreadsheetImport.parseFile(context, uri) }
                working = false
                resultMessage = if (sheet != null) null else importParseError
                pendingSheet = sheet
            }
        }
    }

    GamerScreenBackground {
        Column(
            modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.backup_explainer),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            if (canExportBackupFree) {
                Text(
                    if (isPremium) "Sauvegardes illimitées (Premium)" else "$remainingBackups export(s) gratuit(s) restant(s)",
                    color = NeonCyan,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = {
                    if (canExportBackupFree) {
                        val name = "macollection-${System.currentTimeMillis()}.zip"
                        exportLauncher.launch(name)
                    } else {
                        onOpenShop()
                    }
                },
                enabled = !working,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (canExportBackupFree) stringResource(R.string.backup_save_button) else "Quota atteint — ouvrir la Boutique") }
            if (!canExportBackupFree && BuildConfig.ADS_ENABLED) {
                val canWatchAd = AppPrefs.canWatchAdForPoints(context)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        watchRewardedAdForPoints(context, onEarned = {
                            gameVm.earnPoints(AD_REWARD_POINTS)
                            AppPrefs.recordAdRewardWatched(context)
                        })
                    },
                    enabled = canWatchAd,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (canWatchAd) "🎬 Regarder une pub (+$AD_REWARD_POINTS points)"
                        else "🎬 Limite quotidienne atteinte (revenez demain)"
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/zip")) },
                enabled = !working,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.backup_restore_button)) }
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.excel_export_explainer),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    if (excelUnlocked) {
                        val name = "macollection-${System.currentTimeMillis()}.xls"
                        excelExportLauncher.launch(name)
                    } else {
                        onOpenShop()
                    }
                },
                enabled = !working,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (excelUnlocked) stringResource(R.string.excel_export_button) else "🔒 Débloquer l'export Excel") }
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.import_sheet_explainer),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { importSheetLauncher.launch(arrayOf("*/*")) },
                enabled = !working && importSaving == null,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.import_sheet_button)) }

            if (working || importSaving != null) {
                Spacer(Modifier.height(20.dp))
                CircularProgressIndicator()
                importSaving?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.batch_saving, it), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            resultMessage?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = NeonCyan, textAlign = TextAlign.Center)
            }
        }
    }

    pendingSheet?.let { sheet ->
        ImportMappingDialog(
            headers = sheet.headers,
            initialMapping = remember(sheet) { SpreadsheetImport.guessMapping(sheet.headers) },
            onConfirm = { mapping ->
                val rate = AppPrefs.currencyRates.value[AppPrefs.currency.value] ?: 1.0
                pendingPreviewItems = sheet.rows.mapNotNull {
                    SpreadsheetImport.buildItem(it, mapping, isWishlist = false, currencyRate = rate)
                }
                pendingSheet = null
            },
            onDismiss = { pendingSheet = null }
        )
    }

    pendingPreviewItems?.let { items ->
        ImportPreviewDialog(
            items = items,
            onConfirm = { selected ->
                scope.launch {
                    vm.importSpreadsheet(selected).join()
                    resultMessage = context.getString(R.string.import_success, selected.size)
                }
                pendingPreviewItems = null
            },
            onDismiss = { pendingPreviewItems = null }
        )
    }

    confirmRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { confirmRestoreUri = null },
            title = { Text(stringResource(R.string.backup_restore_button)) },
            text = { Text(stringResource(R.string.backup_restore_warning)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestoreUri = null
                    working = true
                    scope.launch {
                        val ok = vm.importBackup(uri)
                        working = false
                        resultMessage = if (ok) restoreSuccessMessage else restoreErrorMessage
                    }
                }) { Text(stringResource(R.string.backup_restore_button)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestoreUri = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Formulaire : ajouter manuellement une console/accessoire à la bibliothèque
// ---------------------------------------------------------------------------

@Composable
fun AddCustomPresetForm(
    vm: AppViewModel,
    initialType: ItemType = ItemType.CONSOLE,
    // Non nul = modification d'une fiche perso existante au lieu d'une création.
    existing: CustomPreset? = null,
    onSaved: (String) -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val startType = existing?.type ?: initialType
    var type by remember { mutableStateOf(startType) }
    // Le champ « type de console / console associée » suit le type choisi : « Salon » par défaut
    // pour une console, vide pour un accessoire (à renseigner par l'utilisateur).
    var consoleOrKind by remember { mutableStateOf(existing?.consoleOrKind ?: if (startType == ItemType.CONSOLE) "Salon" else "") }
    var brand by remember { mutableStateOf(existing?.brand ?: "") }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var year by remember { mutableStateOf(existing?.year?.toString() ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    // Photo affichée = l'override courant (l'utilisateur a pu la changer depuis la fiche) sinon la
    // photo d'origine de la fiche perso.
    var photoUri by remember { mutableStateOf(existing?.let { vm.photoOverrides.value[it.name] ?: it.photoUri }) }
    var showOnlineSearch by remember { mutableStateOf(false) }

    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { cropped ->
                scope.launch {
                    val saved = withContext(Dispatchers.IO) { MediaUtils.copyToInternal(context, cropped) }
                    if (saved != null) photoUri = saved
                }
            }
        }
    }
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            cropLauncher.launch(
                CropImageContractOptions(uri, CropImageOptions(activityMenuIconColor = android.graphics.Color.WHITE, cropMenuCropButtonTitle = "OK", initialCropWindowPaddingRatio = 0.2f))
            )
        }
    }

    // rememberSaveable (pas remember) : tourner le téléphone pendant que l'appli caméra système
    // est ouverte peut faire tuer notre activité par le système (mémoire), ce qui recréait cet
    // état à null et faisait échouer silencieusement le retour de la photo prise ("la capture se
    // ferme") — rememberSaveable survit à cette recréation, contrairement à remember.
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val pending = pendingCameraUri
        pendingCameraUri = null
        if (success && pending != null) {
            cropLauncher.launch(
                CropImageContractOptions(Uri.parse(pending), CropImageOptions(activityMenuIconColor = android.graphics.Color.WHITE, cropMenuCropButtonTitle = "OK", initialCropWindowPaddingRatio = 0.2f))
            )
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val pair = MediaUtils.newCameraFile(context)
            pendingCameraUri = pair.first.toString()
            cameraLauncher.launch(pair.first)
        }
    }
    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val pair = MediaUtils.newCameraFile(context)
            pendingCameraUri = pair.first.toString()
            cameraLauncher.launch(pair.first)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    FormScaffold(title = stringResource(if (existing != null) R.string.edit_custom_preset_title else R.string.add_custom_preset_title), onCancel = onCancel) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonChip(ItemType.CONSOLE.label, type == ItemType.CONSOLE) {
                type = ItemType.CONSOLE
                // Le champ « genre » d'une console doit rester une valeur valide du menu déroulant.
                if (consoleOrKind != "Salon" && consoleOrKind != "Portable") consoleOrKind = "Salon"
            }
            NeonChip(ItemType.ACCESSOIRE.label, type == ItemType.ACCESSOIRE) {
                type = ItemType.ACCESSOIRE
                if (consoleOrKind == "Salon" || consoleOrKind == "Portable") consoleOrKind = ""
            }
        }
        // Recherche en ligne (Wikipédia) : pré-remplit nom, description et photo pour ce qui
        // manque au catalogue intégré. L'utilisateur complète marque/année puis enregistre.
        OutlinedButton(
            onClick = { showOnlineSearch = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (type == ItemType.CONSOLE) stringResource(R.string.search_console_online)
                else stringResource(R.string.search_accessory_online)
            )
        }
        AutocompleteField(
            value = brand,
            label = stringResource(R.string.field_brand),
            suggestions = remember(AppPrefs.customBrands.value) { (KnownBrands.list + AppPrefs.customBrands.value).distinct().sorted() },
            onValueChange = { brand = it }
        )
        Field(name, stringResource(R.string.field_name)) { name = it }
        Field(year, stringResource(R.string.field_release_year), KeyboardType.Number) {
            year = it.filter { c -> c.isDigit() }.take(4)
        }
        if (type == ItemType.CONSOLE) {
            LabeledDropdown(stringResource(R.string.field_kind), listOf("Salon", "Portable"), consoleOrKind, { it }) {
                consoleOrKind = it
            }
        } else {
            AutocompleteField(
                value = consoleOrKind,
                label = stringResource(R.string.field_associated_console),
                suggestions = remember { consolePresets.map { it.name } },
                onValueChange = { consoleOrKind = ConsoleRecognition.canonicalizeOrNull(it.trim()) ?: it }
            )
        }
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.field_description)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
        )
        OutlinedButton(
            onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.choose_photo_option))
        }
        OutlinedButton(
            onClick = { launchCamera() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.take_photo_option))
        }
        photoUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = stringResource(R.string.photo_content_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp))
            )
            TextButton(onClick = {
                cropLauncher.launch(
                    CropImageContractOptions(Uri.parse(uri), CropImageOptions(activityMenuIconColor = android.graphics.Color.WHITE, cropMenuCropButtonTitle = "OK", initialCropWindowPaddingRatio = 0.2f))
                )
            }) {
                Text(stringResource(R.string.recrop_button))
            }
        }
        Button(
            onClick = {
                AppPrefs.addCustomBrand(context, brand)
                val preset = CustomPreset(
                    type = type,
                    brand = brand,
                    name = name,
                    year = year.toIntOrNull(),
                    consoleOrKind = consoleOrKind,
                    description = description,
                    photoUri = photoUri
                )
                if (existing != null) vm.updateCustomPreset(existing, preset)
                else vm.addCustomPreset(preset)
                onSaved(name.trim())
            },
            enabled = name.isNotBlank() && brand.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_button))
        }
    }

    if (showOnlineSearch) {
        OnlinePresetSearchDialog(
            title = if (type == ItemType.CONSOLE) stringResource(R.string.search_console_online) else stringResource(R.string.search_accessory_online),
            initialQuery = name,
            consoleHint = if (type == ItemType.ACCESSOIRE) consoleOrKind.ifBlank { null } else null,
            onPick = { result ->
                // Même remplissage que « Collection »/« Souhaits » : le résultat retenu écrase
                // systématiquement nom, description et photo (structure de traitement identique).
                name = result.title
                description = result.description
                result.photoUrl?.let { photoUri = it }
                showOnlineSearch = false
            },
            onDismiss = { showOnlineSearch = false }
        )
    }
}

// ---------------------------------------------------------------------------
// Composants réutilisables
// ---------------------------------------------------------------------------

/** Photo plein écran avec zoom (pincement) et déplacement une fois zoomée. */
@Composable
private fun ZoomableImage(uri: String, modifier: Modifier = Modifier) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 6f)
        scale = newScale
        offset = if (newScale <= 1f) Offset.Zero else offset + panChange
    }
    AsyncImage(
        model = uri,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state)
    )
}

@Composable
private fun GamerCard(
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    var m = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(CardGradient)
        .border(1.dp, NeonBorder, shape)
    m = if (onLongClick != null) {
        m.combinedClickable(onClick = { onClick?.invoke() }, onLongClick = onLongClick)
    } else if (onClick != null) {
        m.clickable { onClick() }
    } else m
    Box(m.padding(14.dp)) { content() }
}

@Composable
fun GamerScreenBackground(content: @Composable () -> Unit) {
    val customBg by AppPrefs.backgroundImageUri
    Box(Modifier.fillMaxSize()) {
        if (customBg != null) {
            AsyncImage(
                model = customBg,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))
        } else {
            Box(Modifier.fillMaxSize().background(themedGradient()))
        }
        content()
    }
}

@Composable
private fun FormScaffold(
    title: String,
    onCancel: () -> Unit,
    content: @Composable () -> Unit
) {
    GamerScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(title, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel), tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { p ->
            Column(
                Modifier
                    .padding(p)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                content()
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Field(
    value: String,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = checked, onCheckedChange = onChange)
        Spacer(Modifier.width(10.dp))
        Text(label, color = Color.White)
    }
}

@Composable
private fun NeonChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = NeonPurple,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun ConditionBadge(condition: Condition) {
    val color = conditionColor(condition)
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(condition.label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TinyTag(label: String, filled: Boolean = true) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier
            .clip(shape)
            .background(if (filled) NeonCyan.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f))
            .let { if (filled) it.border(1.dp, NeonCyan.copy(alpha = 0.5f), shape) else it }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            label,
            color = if (filled) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = if (filled) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/** Badge distinct (orange) pour signaler un objet « loose » (sans boîte ni notice) — terme courant chez les collectionneurs. */
@Composable
private fun LooseTag() {
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier
            .clip(shape)
            .background(Color(0xFFFF9800).copy(alpha = 0.22f))
            .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f), shape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(stringResource(R.string.tag_loose), color = Color(0xFFFF9800), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun sortOptionLabel(option: SortOption, filter: ItemType? = null): String = when (option) {
    SortOption.NAME -> stringResource(R.string.sort_name)
    SortOption.BRAND -> if (filter == ItemType.JEU || filter == ItemType.ACCESSOIRE) stringResource(R.string.sort_console) else stringResource(R.string.sort_brand)
    SortOption.PRICE_DESC -> stringResource(R.string.sort_price_desc)
    SortOption.PRICE_ASC -> stringResource(R.string.sort_price_asc)
    SortOption.RELEASE -> stringResource(R.string.sort_release)
    SortOption.RELEASE_DESC -> stringResource(R.string.sort_release_desc)
}

@Composable
private fun SortDropdown(current: SortOption, filter: ItemType? = null, onSelect: (SortOption) -> Unit) {
    ThemedChoiceDropdown(
        leading = stringResource(R.string.sort_prefix),
        selectedLabel = sortOptionLabel(current, filter),
        options = SortOption.values().toList(),
        optionLabel = { sortOptionLabel(it, filter) },
        onSelect = onSelect
    )
}

/** Libellé du filtre d'affichage de la collection (Tous / Consoles / Accessoires / Jeux). */
@Composable
private fun typeFilterLabel(t: ItemType?): String =
    if (t == null) stringResource(R.string.filter_all) else "${typeEmoji(t)} ${t.labelPlural}"

/** Filtre d'affichage sous forme de liste de choix arrondie et thémée (comme le tri). */
@Composable
private fun TypeFilterDropdown(current: ItemType?, onSelect: (ItemType?) -> Unit) {
    ThemedChoiceDropdown(
        leading = stringResource(R.string.filter_show),
        selectedLabel = typeFilterLabel(current),
        options = listOf(null, ItemType.CONSOLE, ItemType.ACCESSOIRE, ItemType.JEU),
        optionLabel = { typeFilterLabel(it) },
        onSelect = onSelect
    )
}

/**
 * Liste de choix « gamer » : encadré arrondi au dégradé de carte + contour néon du thème, qui
 * s'ouvre sur les options. Remplace les anciennes puces/champs carrés pour un rendu homogène avec
 * le reste de l'app. Réutilisable sur toutes les pages (tri, filtre, encyclopédie...).
 */
@Composable
internal fun <T> ThemedChoiceDropdown(
    leading: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        Row(
            Modifier
                .menuAnchor()
                .fillMaxWidth()
                .clip(shape)
                .background(CardGradient)
                .border(1.5.dp, NeonBorder, shape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading.isNotBlank()) {
                Text(
                    "$leading : ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                selectedLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(optionLabel(opt)) },
                    onClick = { onSelect(opt); expanded = false }
                )
            }
        }
    }
}

/**
 * Champ de recherche « gamer » : même encadré arrondi (dégradé de carte + contour néon du thème)
 * que les listes de choix, pour un rendu homogène. Réutilisable sur toutes les pages.
 */
@Composable
internal fun ThemedSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardGradient)
            .border(1.5.dp, NeonBorder, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (value.isNotEmpty()) {
            IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Champ de texte libre avec suggestions filtrées au fur et à mesure de la frappe. À la
 * différence de [LabeledDropdown], la valeur tapée est toujours prise en compte telle quelle
 * (pas besoin de cliquer une suggestion) — utile pour un champ ouvert comme la console
 * associée d'un jeu, qui peut ne pas figurer dans le catalogue.
 */
@Composable
private fun AutocompleteField(
    value: String,
    label: String,
    suggestions: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var lastLength by remember { mutableStateOf(value.length) }
    // Pendant un effacement (Retour/Backspace maintenu ou répété), on suspend le recalcul des
    // suggestions : les régénérer à chaque caractère supprimé ralentissait nettement l'effacement
    // et bloquait la saisie. Elles reprennent dès que l'utilisateur retape (ajout de caractère) ou,
    // à défaut, après une courte pause sans saisie (debounce).
    var suggestionsSuspended by remember { mutableStateOf(false) }
    val filtered = remember(value, suggestions, suggestionsSuspended) {
        when {
            suggestionsSuspended -> emptyList()
            // Plafonné même quand le champ est vide : afficher toute la liste (des centaines de
            // consoles) sans limite rendait le menu lent à composer/afficher à chaque frappe.
            value.isBlank() -> suggestions.take(20)
            else -> suggestions.filter { it.lowercase().contains(value.lowercase()) }.take(20)
        }
    }
    LaunchedEffect(value, suggestionsSuspended) {
        if (suggestionsSuspended) {
            delay(300)
            suggestionsSuspended = false
        }
    }
    ExposedDropdownMenuBox(expanded = expanded && filtered.isNotEmpty(), onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                val deleting = it.length < lastLength
                lastLength = it.length
                if (deleting) {
                    suggestionsSuspended = true
                    expanded = false
                } else {
                    suggestionsSuspended = false
                    expanded = true
                }
                onValueChange(it)
            },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filtered.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Liste déroulante avec filtrage au fur et à mesure de la frappe : on peut toujours taper
 * directement plutôt que de défiler toute la liste. La sélection reste l'une des [options]
 * proposées (utilisé pour des énumérations fermées : type, région, état...).
 */
@Composable
private fun <T> LabeledDropdown(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember(selected) { mutableStateOf(optionLabel(selected)) }
    // Tant que l'utilisateur n'a rien tapé de nouveau (le texte affiché est toujours celui de
    // la sélection actuelle), on montre TOUTES les options à l'ouverture — sinon ouvrir le menu
    // le filtrait déjà sur la valeur en cours et cachait les autres choix (ex. État/Région).
    val filtered = remember(query, options, selected) {
        if (query.isBlank() || query == optionLabel(selected)) options
        else options.filter { optionLabel(it).lowercase().contains(query.lowercase()) }
    }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; expanded = true },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false; query = optionLabel(selected) }
        ) {
            filtered.forEach { o ->
                DropdownMenuItem(
                    text = { Text(optionLabel(o)) },
                    onClick = {
                        onSelect(o)
                        query = optionLabel(o)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sélecteur de console (catalogue intégré)
// ---------------------------------------------------------------------------

@Composable
private fun ConsolePickerDialog(
    customPresets: List<CustomPreset> = emptyList(),
    initialQuery: String = "",
    onPick: (ConsolePreset) -> Unit,
    onPickCustom: (CustomPreset) -> Unit = {},
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }
    val customConsolePresets = remember(customPresets) { customPresets.filter { it.type == ItemType.CONSOLE } }
    val filtered = remember(query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) consolePresets
        else consolePresets.filter {
            ConsoleRecognition.matchesQuery(listOf(it.name, it.brand) + ConsoleRecognition.aliasSearchTerms(it.name), q)
        }
    }
    val filteredCustom = remember(query, customConsolePresets) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) customConsolePresets
        else customConsolePresets.filter { ConsoleRecognition.matchesQuery(listOf(it.name, it.brand), q) }
    }
    // Fiches natives et fiches ajoutées fondues dans une seule liste triée (plus de section
    // « Mes ajouts » distincte), même présentation pour toutes.
    val merged = remember(filtered, filteredCustom) {
        (filtered.toList<Any>() + filteredCustom).sortedBy {
            when (it) {
                is ConsolePreset -> "${it.brand} ${it.name}".lowercase()
                is CustomPreset -> "${it.brand} ${it.name}".lowercase()
                else -> ""
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.console_catalog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.search_name_or_brand)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        merged,
                        key = {
                            when (it) {
                                is ConsolePreset -> "n-${it.brand}-${it.name}-${it.year}"
                                is CustomPreset -> "c-${it.id}"
                                else -> ""
                            }
                        }
                    ) { entry ->
                        val brandName: String
                        val subtitle: String
                        val onClickEntry: () -> Unit
                        when (entry) {
                            is CustomPreset -> {
                                brandName = "${entry.brand} ${entry.name}"
                                subtitle = "${entry.year ?: ""} • ${entry.consoleOrKind}"
                                onClickEntry = { onPickCustom(entry) }
                            }
                            is ConsolePreset -> {
                                brandName = "${entry.brand} ${entry.name}"
                                subtitle = "${entry.year} • ${entry.kind} • ${entry.memory}"
                                onClickEntry = { onPick(entry) }
                            }
                            else -> { brandName = ""; subtitle = ""; onClickEntry = {} }
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { onClickEntry() }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(brandName, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (filtered.isEmpty() && filteredCustom.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.no_console_found),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sélecteur d'accessoire (catalogue intégré)
// ---------------------------------------------------------------------------

private fun consoleMatchesHint(console: String, hint: String): Boolean {
    if (console.isBlank()) return false
    val c = console.trim().lowercase()
    val h = hint.trim().lowercase()
    return c == h || c.contains(h) || h.contains(c)
}

@Composable
private fun AccessoryPickerDialog(
    customPresets: List<CustomPreset> = emptyList(),
    initialQuery: String = "",
    consoleHint: String? = null,
    onPick: (AccessoryPreset) -> Unit,
    onPickCustom: (CustomPreset) -> Unit = {},
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }
    val customAccessoryPresets = remember(customPresets) { customPresets.filter { it.type == ItemType.ACCESSOIRE } }
    val filtered = remember(query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) accessoryPresets
        else accessoryPresets.filter { ConsoleRecognition.matchesQuery(listOf(it.name, it.brand, it.console), q) }
    }
    val filteredCustom = remember(query, customAccessoryPresets) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) customAccessoryPresets
        else customAccessoryPresets.filter { ConsoleRecognition.matchesQuery(listOf(it.name, it.brand, it.consoleOrKind), q) }
    }
    // Fiches natives et fiches ajoutées fondues dans une seule liste triée (plus de section
    // « Mes ajouts » distincte), même présentation pour toutes.
    val hint = consoleHint?.trim()?.takeIf { it.isNotBlank() }
    val merged = remember(filtered, filteredCustom, hint) {
        fun consoleOf(e: Any): String = when (e) {
            is AccessoryPreset -> e.console
            is CustomPreset -> e.consoleOrKind
            else -> ""
        }
        fun sortKey(e: Any): String = when (e) {
            is AccessoryPreset -> "${e.brand} ${e.name}".lowercase()
            is CustomPreset -> "${e.brand} ${e.name}".lowercase()
            else -> ""
        }
        // Recherche « ciblée » comme pour les jeux : les accessoires de la console associée
        // (le champ « Console associée » du formulaire) remontent en tête, puis ordre alphabétique.
        (filtered.toList<Any>() + filteredCustom).sortedWith(
            compareByDescending<Any> { e -> hint != null && consoleMatchesHint(consoleOf(e), hint) }
                .thenBy { sortKey(it) }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.accessory_catalog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.search_name_or_brand)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        merged,
                        key = {
                            when (it) {
                                is AccessoryPreset -> "n-${it.brand}-${it.name}-${it.year}"
                                is CustomPreset -> "c-${it.id}"
                                else -> ""
                            }
                        }
                    ) { entry ->
                        val brandName: String
                        val subtitle: String
                        val onClickEntry: () -> Unit
                        when (entry) {
                            is CustomPreset -> {
                                brandName = "${entry.brand} ${entry.name}"
                                subtitle = "${entry.year ?: ""} • ${entry.consoleOrKind}"
                                onClickEntry = { onPickCustom(entry) }
                            }
                            is AccessoryPreset -> {
                                brandName = "${entry.brand} ${entry.name}"
                                subtitle = "${entry.year} • ${entry.console}"
                                onClickEntry = { onPick(entry) }
                            }
                            else -> { brandName = ""; subtitle = ""; onClickEntry = {} }
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { onClickEntry() }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(brandName, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (filtered.isEmpty() && filteredCustom.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.no_console_found),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Recherche de console/accessoire en ligne (Wikipédia), pour ce qui manque au catalogue
// intégré et aux ajouts personnalisés déjà existants.
// ---------------------------------------------------------------------------

@Composable
private fun OnlinePresetSearchDialog(
    title: String,
    onPick: (WikiPresetResult) -> Unit,
    onDismiss: () -> Unit,
    initialQuery: String = "",
    consoleHint: String? = null
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<WikiPresetResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var currentStage by remember { mutableStateOf(SearchCascade.Stage.EXACT) }
    var showRetryHelp by remember { mutableStateOf(false) }

    // Recherche en cascade (cf. [SearchCascade]) : essai exact tel que tapé, puis nettoyé
    // (mentions d'édition seulement — PAS le nettoyeur agressif des jeux, qui retirerait des mots
    // comme "Switch"/"Nintendo", ici le sujet même de la recherche), puis traduit en anglais.
    fun runSearch() {
        if (query.isBlank()) return
        // Annule la recherche précédente encore en vol : sans ça, une vieille recherche lente
        // (souvent celle restée sans résultat) se termine APRÈS la nouvelle et écrase ses
        // résultats avec sa liste vide — d'où « plus aucun résultat » après avoir modifié
        // l'intitulé. La garde isActive ignore de toute façon les réponses périmées.
        searchJob?.cancel()
        searchJob = scope.launch {
            loading = true
            showRetryHelp = false
            val outcome = SearchCascade.run(
                rawQuery = query,
                cleaningCandidates = SearchCascade::presetCleaningCandidates,
                translate = { GroqVision.translate(it, "en") },
                onStageStart = { currentStage = it },
                search = { q -> WikipediaPresetSearch.search(q, consoleHint) }
            )
            if (!isActive) return@launch
            results = outcome.items
            searched = true
            loading = false
        }
    }

    LaunchedEffect(Unit) { if (initialQuery.isNotBlank()) runSearch() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 580.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { runSearch() }) { Text(stringResource(R.string.search_button)) }
                }
                Spacer(Modifier.height(10.dp))
                if (loading) {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            if (currentStage != SearchCascade.Stage.EXACT) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.search_deep_in_progress),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(results, key = { it.title }) { result ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable {
                                        // Traduit la description dans la langue de l'appli (via Groq)
                                        // avant de remplir la fiche ; inchangée si déjà bonne / si échec.
                                        searchJob?.cancel()
                                        searchJob = scope.launch {
                                            loading = true
                                            val tr = GroqVision.translate(result.description, currentUiLanguage())
                                            loading = false
                                            onPick(if (tr != null) result.copy(description = tr) else result)
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (result.photoUrl != null) {
                                    AsyncImage(
                                        model = result.photoUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))
                                    )
                                    Spacer(Modifier.width(10.dp))
                                }
                                Column {
                                    Text(result.title, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        result.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (searched && results.isEmpty()) {
                            item {
                                Column {
                                    Text(stringResource(R.string.collection_search_no_results), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    TextButton(onClick = { showRetryHelp = true }) {
                                        Text(stringResource(R.string.search_retry_button))
                                    }
                                    if (showRetryHelp) {
                                        Text(
                                            stringResource(R.string.search_no_results_retry_help),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Recherche de jeux en ligne (hybride : IGDB en source principale, RAWG en complément)
// ---------------------------------------------------------------------------

@Composable
private fun GameSearchDialog(
    onPick: (GameInfo) -> Unit,
    onDismiss: () -> Unit,
    consoleHint: String? = null,
    initialQuery: String = ""
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<GameInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var searcher by remember { mutableStateOf<HybridGameSearch?>(null) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var currentStage by remember { mutableStateOf(SearchCascade.Stage.EXACT) }
    var showRetryHelp by remember { mutableStateOf(false) }
    val platformId = remember(consoleHint) { consoleHint?.let { ConsolePlatforms.platformId(it) } }
    val anyApiConfigured = IgdbCatalog.isConfigured() || GameCatalog.isConfigured()

    // Recherche en cascade (cf. [SearchCascade]) : essai exact tel que tapé, puis nettoyé (bruit
    // d'annonce/mentions d'édition, mot final retiré progressivement), puis traduit en anglais —
    // ne passe à l'étape suivante que si la précédente n'a rien trouvé. La session HybridGameSearch
    // gagnante est conservée dans [searcher] pour que "Plus de résultats" pagine la BONNE requête.
    fun runSearch(raw: String) {
        if (raw.isBlank()) return
        searchJob?.cancel()
        searchJob = scope.launch {
            loading = true
            showRetryHelp = false
            var lastSession: HybridGameSearch? = null
            val outcome = SearchCascade.run(
                rawQuery = raw,
                cleaningCandidates = SearchCascade::gameCleaningCandidates,
                translate = { GroqVision.translate(it, "en") },
                onStageStart = { currentStage = it },
                search = { q ->
                    val session = HybridGameSearch(q, platformId, consoleHint)
                    lastSession = session
                    session.loadFirst()
                }
            )
            if (!isActive) return@launch
            searcher = lastSession
            results = outcome.items
            searched = true
            loading = false
            loadingMore = false
        }
    }

    // Le nom déjà tapé/reconnu est repris automatiquement : pas besoin de le retaper, la
    // recherche se lance directement à l'ouverture si un nom est déjà disponible.
    LaunchedEffect(Unit) {
        if (initialQuery.isNotBlank() && anyApiConfigured) runSearch(initialQuery)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 580.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.search_game_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(10.dp))

                if (!anyApiConfigured) {
                    Text(
                        stringResource(R.string.api_key_not_configured),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text(stringResource(R.string.field_game_title)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { runSearch(query) }
                        ) { Text(stringResource(R.string.search_button)) }
                    }
                    if (consoleHint != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (platformId != null) {
                                stringResource(R.string.search_filtered_by_console, consoleHint)
                            } else {
                                stringResource(R.string.search_no_platform_filter, consoleHint)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    if (loading) {
                        Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                if (currentStage != SearchCascade.Stage.EXACT) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        stringResource(R.string.search_deep_in_progress),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(results, key = { "${it.source}:${it.sourceId ?: it.name.lowercase()}" }) { game ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .clickable {
                                            scope.launch {
                                                loading = true
                                                // RAWG : fiche détaillée (description + éditeur) ;
                                                // IGDB : tout est déjà là, on tente juste la description FR.
                                                val rawgId = game.sourceId
                                                val detailed = when {
                                                    rawgId != null -> GameCatalog.detail(rawgId) ?: game
                                                    game.source == "igdb" -> IgdbCatalog.frenchify(game)
                                                    else -> game
                                                }
                                                // Description dans la langue de l'utilisateur : on la traduit
                                                // (via Groq) si elle ne l'est pas déjà. Sans réponse, on garde l'originale.
                                                val localized = if (detailed.description.isNotBlank()) {
                                                    val tr = GroqVision.translate(detailed.description, currentUiLanguage())
                                                    if (tr != null) detailed.copy(description = tr) else detailed
                                                } else detailed
                                                loading = false
                                                onPick(localized)
                                            }
                                        }
                                        .padding(8.dp),
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
                                        Text(
                                            game.name,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        val meta = listOfNotNull(
                                            game.releaseYear?.toString(),
                                            game.platforms.takeIf { it.isNotBlank() }
                                        ).joinToString(" • ")
                                        if (meta.isNotBlank()) {
                                            Text(
                                                meta,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        // Aperçu de la description (quand disponible : Wikipédia/Wikidata/IGDB)
                                        // pour voir tout de suite qu'il y a du contenu. Traduite à la sélection.
                                        if (game.description.isNotBlank()) {
                                            Text(
                                                game.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                            // Pagination progressive : page IGDB suivante + complément RAWG
                            // fusionné sans doublons, uniquement à la demande de l'utilisateur.
                            if (searched && results.isNotEmpty() && searcher?.hasMore == true) {
                                item(key = "more_results") {
                                    OutlinedButton(
                                        onClick = {
                                            val session = searcher ?: return@OutlinedButton
                                            searchJob?.cancel()
                                            searchJob = scope.launch {
                                                loadingMore = true
                                                val merged = session.loadMore()
                                                if (!isActive) return@launch
                                                results = merged
                                                loadingMore = false
                                            }
                                        },
                                        enabled = !loadingMore,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (loadingMore) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text(stringResource(R.string.search_more_results))
                                        }
                                    }
                                }
                            }
                            if (searched && results.isEmpty()) {
                                item {
                                    Column {
                                        Text(
                                            stringResource(R.string.no_game_found),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        TextButton(onClick = { showRetryHelp = true }) {
                                            Text(stringResource(R.string.search_retry_button))
                                        }
                                        if (showRetryHelp) {
                                            Text(
                                                stringResource(R.string.search_no_results_retry_help),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Choix de console(s) quand une recherche en ligne renvoie un jeu multi-plateformes
// ---------------------------------------------------------------------------

/**
 * Un jeu trouvé en ligne (RAWG/IGDB) est souvent sorti sur plusieurs consoles à la fois : plutôt
 * que d'empiler automatiquement toutes les plateformes dans "Console associée", on demande ici
 * laquelle/lesquelles l'utilisateur possède réellement — même principe (cases à cocher) que
 * [BatchScanDialog] pour le scan de lot.
 */
@Composable
private fun PlatformPickerDialog(
    platforms: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // Rien de coché par défaut : contrairement au scan de lot (où chaque ligne est un objet
    // distinct, tout présélectionné), ici il s'agit d'UN SEUL jeu sur plusieurs supports
    // possibles — le forcer à choisir évite qu'il valide par réflexe une console qu'il n'a pas.
    val checked = remember(platforms) { mutableStateListOf<Boolean>().apply { repeat(platforms.size) { add(false) } } }
    val selectedCount = checked.count { it }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.platform_picker_title)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.platform_picker_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    itemsIndexed(platforms) { i, name ->
                        Row(
                            Modifier.fillMaxWidth().clickable { checked[i] = !checked[i] }.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = checked[i], onCheckedChange = { checked[i] = it })
                            Text(name, color = Color.White, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(platforms.filterIndexed { i, _ -> checked[i] }) },
                enabled = selectedCount > 0
            ) { Text(stringResource(R.string.platform_picker_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

// ---------------------------------------------------------------------------
// Scan multiple : liste des articles détectés dans un lot, à cocher avant ajout
// ---------------------------------------------------------------------------

@Composable
fun BatchScanDialog(
    items: List<GeminiVision.BatchItem>,
    onConfirm: (List<GeminiVision.BatchItem>) -> Unit,
    onDismiss: () -> Unit
) {
    // Tout coché par défaut ; l'utilisateur décoche les erreurs avant de valider.
    val checked = remember(items) { mutableStateListOf<Boolean>().apply { repeat(items.size) { add(true) } } }
    val selectedCount = checked.count { it }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.batch_detected_title, items.size)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.batch_selected_count, selectedCount, items.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    itemsIndexed(items) { i, item ->
                        Row(
                            Modifier.fillMaxWidth().clickable { checked[i] = !checked[i] }.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = checked[i], onCheckedChange = { checked[i] = it })
                            Column(Modifier.weight(1f)) {
                                Text("${batchTypeEmoji(item.type)} ${item.title}", fontWeight = FontWeight.Bold, color = Color.White)
                                item.console?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(items.filterIndexed { i, _ -> checked[i] }) },
                enabled = checked.any { it }
            ) { Text(stringResource(R.string.batch_validate)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

// ---------------------------------------------------------------------------
// Import d'un fichier Excel/CSV externe (voir SpreadsheetImport)
// ---------------------------------------------------------------------------

@Composable
private fun ImportMappingDialog(
    headers: List<String>,
    initialMapping: Map<SpreadsheetImport.ImportField, Int?>,
    onConfirm: (Map<SpreadsheetImport.ImportField, Int?>) -> Unit,
    onDismiss: () -> Unit
) {
    val mapping = remember { mutableStateMapOf<SpreadsheetImport.ImportField, Int?>().apply { putAll(initialMapping) } }
    val noneLabel = stringResource(R.string.import_column_none)
    val columnOptions = remember(headers) { listOf<Int?>(null) + headers.indices.toList() }
    val currencySymbol = CurrencyOptions.symbolFor(AppPrefs.currency.value)
    val fieldLabels = mapOf(
        SpreadsheetImport.ImportField.NAME to stringResource(R.string.field_name),
        SpreadsheetImport.ImportField.TYPE to stringResource(R.string.field_type),
        SpreadsheetImport.ImportField.BRAND to stringResource(R.string.field_brand),
        SpreadsheetImport.ImportField.PLATFORM to stringResource(R.string.field_platforms),
        SpreadsheetImport.ImportField.CONDITION to stringResource(R.string.field_condition),
        SpreadsheetImport.ImportField.HAS_BOX to stringResource(R.string.switch_has_box),
        SpreadsheetImport.ImportField.HAS_MANUAL to stringResource(R.string.switch_has_manual),
        SpreadsheetImport.ImportField.YEAR to stringResource(R.string.field_release_year),
        SpreadsheetImport.ImportField.GENRE to stringResource(R.string.field_genre),
        SpreadsheetImport.ImportField.PRICE to stringResource(R.string.field_price, currencySymbol),
        SpreadsheetImport.ImportField.BARCODE to stringResource(R.string.field_barcode),
        SpreadsheetImport.ImportField.DESCRIPTION to stringResource(R.string.field_description),
        SpreadsheetImport.ImportField.REGION to stringResource(R.string.field_region)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_mapping_title)) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                Text(
                    stringResource(R.string.import_mapping_explainer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                SpreadsheetImport.ImportField.values().forEach { field ->
                    LabeledDropdown(
                        label = fieldLabels[field].orEmpty(),
                        options = columnOptions,
                        selected = mapping[field],
                        optionLabel = { idx -> if (idx == null) noneLabel else headers.getOrElse(idx) { "?" } },
                        onSelect = { mapping[field] = it }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(mapping.toMap()) },
                enabled = mapping[SpreadsheetImport.ImportField.NAME] != null
            ) { Text(stringResource(R.string.import_next)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun ImportPreviewDialog(
    items: List<CollectionItem>,
    onConfirm: (List<CollectionItem>) -> Unit,
    onDismiss: () -> Unit
) {
    val checked = remember(items) { mutableStateListOf<Boolean>().apply { repeat(items.size) { add(true) } } }
    val selectedCount = checked.count { it }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_preview_title, items.size)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.batch_selected_count, selectedCount, items.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    itemsIndexed(items) { i, item ->
                        Row(
                            Modifier.fillMaxWidth().clickable { checked[i] = !checked[i] }.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = checked[i], onCheckedChange = { checked[i] = it })
                            Column(Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, color = Color.White)
                                item.platform?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(items.filterIndexed { i, _ -> checked[i] }) },
                enabled = checked.any { it }
            ) { Text(stringResource(R.string.import_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

/**
 * Fine barre latérale cyan indiquant la position de défilement dans une [LazyColumn] (Collection,
 * Souhaits, Encyclo...). Invisible tant que le contenu tient déjà entièrement à l'écran (rien à
 * faire défiler) : pas de calcul pixel précis (position/hauteur des items réels), on se base sur
 * le nombre d'items visibles vs. total, largement suffisant pour un simple repère visuel.
 */
@Composable
fun ScrollPositionIndicator(listState: LazyListState, modifier: Modifier = Modifier) {
    val layoutInfo = listState.layoutInfo
    val totalCount = layoutInfo.totalItemsCount
    val visibleCount = layoutInfo.visibleItemsInfo.size
    if (totalCount == 0 || visibleCount == 0 || visibleCount >= totalCount) return

    val thumbFraction = (visibleCount.toFloat() / totalCount.toFloat()).coerceIn(0.08f, 1f)
    val scrollableSteps = (totalCount - visibleCount).coerceAtLeast(1)
    val progress = (listState.firstVisibleItemIndex.toFloat() / scrollableSteps.toFloat()).coerceIn(0f, 1f)
    val remainingFraction = (1f - thumbFraction).coerceAtLeast(0f)
    val beforeFraction = progress * remainingFraction
    val afterFraction = (remainingFraction - beforeFraction).coerceAtLeast(0f)

    Column(modifier.fillMaxHeight().width(5.dp)) {
        if (beforeFraction > 0f) Spacer(Modifier.weight(beforeFraction))
        Box(
            Modifier
                .weight(thumbFraction.coerceAtLeast(0.001f))
                .fillMaxWidth()
                .background(NeonCyan, RoundedCornerShape(3.dp))
        )
        if (afterFraction > 0f) Spacer(Modifier.weight(afterFraction))
    }
}

// ---------------------------------------------------------------------------
// Utilitaires
// ---------------------------------------------------------------------------

/** Icône affichée devant chaque article détecté par lot, selon le type deviné par l'IA. */
private fun batchTypeEmoji(type: String?): String = when (type) {
    "console" -> "🕹️"
    "accessoire" -> "🎮"
    else -> "💿"
}

private fun typeEmoji(t: ItemType): String = when (t) {
    ItemType.CONSOLE -> "🕹️"
    ItemType.JEU -> "💿"
    ItemType.ACCESSOIRE -> "🎮"
}

private fun conditionColor(c: Condition): Color = when (c) {
    Condition.HS -> Color(0xFFFF5252)
    Condition.MAUVAIS -> Color(0xFFFF8A4C)
    Condition.BON -> Color(0xFFFFD54F)
    Condition.TRES_BON -> Color(0xFF9CCC65)
    Condition.MINT -> Color(0xFF4DD0E1)
    Condition.NEUF -> Color(0xFF69F0AE)
}

/** Taux de la devise choisie par rapport à l'euro (base de stockage des cotes) ; 1.0 par défaut. */
private fun currentCurrencyRate(): Double = AppPrefs.currencyRates.value[AppPrefs.currency.value] ?: 1.0

private fun formatPrice(cents: Int?): String =
    if (cents == null || cents == 0) "—"
    else String.format(Locale.FRANCE, "%.2f %s", (cents / 100.0) * currentCurrencyRate(), CurrencyOptions.symbolFor(AppPrefs.currency.value))

private fun centsToText(cents: Int): String =
    String.format(Locale.FRANCE, "%.2f", (cents / 100.0) * currentCurrencyRate())

/**
 * Vrai si [a] et [b] partagent au moins un mot significatif (≥3 lettres) — ou si [a] n'en a aucun,
 * auquel cas il n'y a rien à préserver. Sert à décider si le titre déjà tapé d'un jeu peut être
 * remplacé par celui d'un résultat de recherche choisi (cf. [GameSearchDialog]) sans risquer de
 * passer d'une langue à une autre (ex. "Max et les monstres" -> "Where the Wild Things Are").
 */
private fun sharesSignificantWord(a: String, b: String): Boolean {
    fun words(s: String) = s.lowercase().replace(Regex("[^\\p{L}\\p{Nd} ]"), " ")
        .split(" ").filter { it.length >= 3 }.toSet()
    val wa = words(a)
    return wa.isEmpty() || words(b).any { it in wa }
}

private fun parsePriceToCents(text: String): Int? {
    val cleaned = text.replace(",", ".").trim()
    if (cleaned.isEmpty()) return null
    val value = cleaned.toDoubleOrNull() ?: return null
    val rate = currentCurrencyRate()
    if (rate <= 0.0) return null
    return ((value / rate) * 100).roundToInt()
}
