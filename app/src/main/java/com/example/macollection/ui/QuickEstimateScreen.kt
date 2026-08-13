@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.macollection.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.macollection.R
import com.example.macollection.data.GameInfo
import com.example.macollection.data.ItemType

/**
 * Résultat d'une estimation rapide ("$" dans la TopAppBar) : identification (photo ou code-barres,
 * cf. [MainActivity]) déjà faite, prix déjà résolu (eBay puis IA, cf.
 * [AppViewModel.quickEstimatePrice]) — ne reste qu'à choisir collection/souhaits ou abandonner.
 * Les champs presetName/accessoryName/gameMatch/gameConsoleHint/coverUri/originalCoverUri/barcode
 * sont ceux déjà utilisés par [CollectionEditor] pour pré-remplir [AddCollectionForm] : ce sont
 * exactement les mêmes qu'un scan/photo normal, seuls priceCents/priceIsAiEstimate s'y ajoutent.
 */
data class QuickEstimateData(
    val name: String,
    val brand: String,
    val type: ItemType,
    val platform: String?,
    val coverUri: String?,
    val originalCoverUri: String?,
    val barcode: String?,
    val presetName: String?,
    val accessoryName: String?,
    val gameMatch: GameInfo?,
    val gameConsoleHint: String?,
    val priceCents: Int?,
    val priceIsAiEstimate: Boolean,
    val priceInfo: String?
)

@Composable
fun QuickEstimateResultScreen(
    data: QuickEstimateData,
    onAddToCollection: () -> Unit,
    onAddToWishlist: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }
    GamerScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.quick_estimate_chooser_title), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (data.coverUri != null) {
                        AsyncImage(
                            model = data.coverUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Icon(Icons.Filled.Image, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    data.name.ifBlank { stringResource(R.string.unnamed_item) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                data.platform?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    if (data.priceCents != null) {
                        formatPrice(data.priceCents) + if (data.priceIsAiEstimate) " (IA)" else ""
                    } else {
                        stringResource(R.string.quick_estimate_price_unknown)
                    },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = com.example.macollection.ui.theme.NeonCyan
                )
                data.priceInfo?.takeIf { it.isNotBlank() && data.priceCents != null }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = onAddToCollection, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.quick_estimate_add_to_collection))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onAddToWishlist, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.quick_estimate_add_to_wishlist))
                }
            }
        }
    }
}
