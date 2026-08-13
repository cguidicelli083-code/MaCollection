package com.example.macollection.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.macollection.R
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Scanner de code-barres caméra en direct, construit avec CameraX + le modèle ML Kit "barcode"
 * déjà utilisé (et confirmé fonctionnel) par le scan à partir d'une photo
 * ([com.example.macollection.data.ScanTools.scanImage]). Remplace l'ancien scanner hébergé par
 * Google Play Services (`GmsBarcodeScanning`, retiré) : son module séparé ("barcode_ui") pouvait
 * échouer à se télécharger sur certains appareils indépendamment de l'app (bug Google connu,
 * issuetracker.google.com/issues/236022573), sans qu'aucune correction côté app n'y change rien.
 * Validé d'abord sur l'édition TEST avant généralisation aux 3 éditions.
 */
@Composable
fun NativeBarcodeScannerScreen(onResult: (String?) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // Un seul résultat pris en compte : l'analyse tourne en continu sur chaque frame tant qu'aucun
    // code n'a été détecté, plusieurs frames pouvant matcher avant que la caméra ne soit libérée.
    val handled = remember { AtomicBoolean(false) }
    // Détecteur ML Kit créé une seule fois pour toute la durée de vie de l'écran (jamais recréé à
    // chaque frame) : fermé explicitement dans [DisposableEffect] ci-dessous, comme le fait déjà
    // ScanTools.scanImage — sans ce close(), chaque session de scan fuyait un détecteur natif.
    val scanner = remember { BarcodeScanning.getClient() }
    // Délai avant d'accepter un résultat : sans lui, la toute première frame (souvent floue ou mal
    // cadrée pendant que l'utilisateur lève encore le téléphone) pouvait suffire à déclencher une
    // détection, avant d'avoir eu le temps de viser correctement le bon code-barres — la caméra
    // s'affiche normalement pendant ce délai, seule l'acceptation d'un résultat est repoussée.
    // 4,5 s (retour utilisateur : 1,2 s ne laissait toujours pas le temps de cadrer correctement).
    val scanDelayMs = 4500L
    val readyAtMs = remember { System.currentTimeMillis() + scanDelayMs }
    // Piloté séparément par un vrai minuteur Compose (pas juste la comparaison de readyAtMs ci-dessus,
    // lue uniquement par l'analyseur caméra) pour pouvoir masquer l'indication "Cadre le code-barres"
    // au bon moment sans dépendre d'une recomposition déclenchée par autre chose.
    var showFramingHint by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(scanDelayMs)
        showFramingHint = false
    }

    // Permission caméra : sans elle, bindToLifecycle() ci-dessous échoue avec une SecurityException
    // (repliée sur le simple catch générique plus bas, donc silencieuse) et l'écran reste noir sans
    // qu'aucune demande ne soit jamais affichée à l'utilisateur — contrairement à la prise de photo
    // classique (cameraLauncher dans MainActivity), qui la demande déjà correctement au premier
    // lancement. Même mécanique ici, pour le scanner caméra en direct.
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) onResult(null)
    }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(Unit) {
        onDispose {
            // Libère explicitement la caméra à la fermeture de cet écran (retour, ou code trouvé) :
            // sans ça, la caméra restait accaparée par ce use case après le premier scan et les
            // tentatives suivantes échouaient silencieusement à se lier (plus aucun jeu détecté après
            // le tout premier scan, symptôme observé en test).
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            scanner.close()
        }
    }

    BackHandler { onResult(null) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && !handled.get() && System.currentTimeMillis() >= readyAtMs) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        val value = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                                        if (value != null && handled.compareAndSet(false, true)) {
                                            onResult(value)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("ScanBarcode", "Native scanner analysis failed: ${e.javaClass.simpleName}: ${e.message}")
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                            )
                        } catch (e: Exception) {
                            Log.e("ScanBarcode", "Native scanner bind failed: ${e.javaClass.simpleName}: ${e.message}")
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                }
            )
            // Cadre de visée simple, purement indicatif (l'analyse porte sur l'image entière).
            Box(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1.6f)
                    .border(2.dp, Color.White, RoundedCornerShape(12.dp))
            )
            if (showFramingHint) {
                Text(
                    stringResource(R.string.native_scanner_framing_hint),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 96.dp, start = 24.dp, end = 24.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
        IconButton(
            onClick = { onResult(null) },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = Color.White)
        }
    }
}
