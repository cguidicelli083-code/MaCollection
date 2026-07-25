package com.example.macollection.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.macollection.R
import com.example.macollection.data.RetroNewsEntry
import com.example.macollection.ui.theme.CardGradient
import com.example.macollection.ui.theme.NeonBorder
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPurple

/** Libellé FR affiché pour chaque catégorie du scraper (voir `scripts/scrape_retro_news.py`). */
private fun categoryLabel(category: String): Int = when (category) {
    "RETRO_CONSOLE" -> R.string.news_category_retro_console
    "COLLECTOR_PACK" -> R.string.news_category_collector_pack
    "UPCOMING_CONSOLE" -> R.string.news_category_upcoming_console
    "ARCADE_CABINET" -> R.string.news_category_arcade_cabinet
    "GAME_RELEASE" -> R.string.news_category_game_release
    "REISSUE" -> R.string.news_category_reissue
    else -> R.string.news_category_other
}

/**
 * Écran Actus de l'Encyclopédie : nouveautés retrogaming à venir (consoles rétro, packs
 * collectors, consoles annoncées, bornes d'arcade, sorties de jeux, rééditions), agrégées par le
 * scraper (`scripts/scrape_retro_news.py`, exécuté chaque nuit par GitHub Actions) — voir
 * [RetroNewsRepository][com.example.macollection.data.RetroNewsRepository]. Lecture seule, aucun
 * lien avec la collection. Le filtrage "à venir uniquement" est déjà fait côté scraper (le JSON
 * publié ne contient que des actus jugées à venir), donc tout ce qui arrive ici s'affiche.
 */
@Composable
fun NewsScreen(vm: AppViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val allNews by vm.retroNews.collectAsState()
    var categoryFilter by remember { mutableStateOf<String?>(null) }
    var opened by remember { mutableStateOf<RetroNewsEntry?>(null) }

    val availableCategories = remember(allNews) { allNews.map { it.category }.distinct() }
    val filtered = remember(allNews, categoryFilter) {
        if (categoryFilter == null) allNews else allNews.filter { it.category == categoryFilter }
    }

    GamerScreenBackground {
        Column(modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                }
                Text(stringResource(R.string.title_news), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            if (availableCategories.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = categoryFilter == null,
                            onClick = { categoryFilter = null },
                            label = { Text(stringResource(R.string.news_category_all)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonPurple, selectedLabelColor = Color.White)
                        )
                    }
                    items(availableCategories) { category ->
                        FilterChip(
                            selected = categoryFilter == category,
                            onClick = { categoryFilter = category },
                            label = { Text(stringResource(categoryLabel(category))) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonPurple, selectedLabelColor = Color.White)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.news_empty),
                        fontSize = 13.sp,
                        color = Color(0xFFB5B5CC),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { entry -> NewsCard(entry) { opened = entry } }
                }
            }
        }
    }

    opened?.let { entry -> NewsDetailDialog(entry) { opened = null } }
}

@Composable
private fun NewsCard(entry: RetroNewsEntry, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardGradient)
            .border(1.dp, NeonBorder, shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            if (entry.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = entry.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1E2C))
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.fillMaxWidth()) {
                Text(stringResource(categoryLabel(entry.category)), fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(entry.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White, maxLines = 3)
                Spacer(Modifier.height(4.dp))
                Text(entry.sourceName, fontSize = 11.sp, color = Color(0xFF7A7A96))
            }
        }
    }
}

@Composable
private fun NewsDetailDialog(entry: RetroNewsEntry, onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(entry.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (entry.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = entry.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1E2C))
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Text(stringResource(categoryLabel(entry.category)), fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(entry.summary, fontSize = 13.sp, color = Color(0xFFB5B5CC))
                Spacer(Modifier.height(8.dp))
                Text(entry.sourceName, fontSize = 12.sp, color = NeonPurple)
            }
        },
        confirmButton = {
            Column(Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(entry.sourceUrl)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.news_view_source)) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
                }
            }
        }
    )
}
