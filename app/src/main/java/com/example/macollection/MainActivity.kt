@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.macollection

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.macollection.data.AppPrefs
import com.example.macollection.data.CollectionItem
import com.example.macollection.data.AccessoryPreset
import com.example.macollection.data.ConsolePreset
import com.example.macollection.data.GameInfo
import com.example.macollection.data.GeminiVision
import com.example.macollection.data.GroqVision
import com.example.macollection.data.CustomPreset
import com.example.macollection.data.ItemType
import com.example.macollection.data.MediaUtils
import com.example.macollection.data.ScanTools
import com.example.macollection.ui.AddCollectionForm
import com.example.macollection.ui.AddCustomPresetForm
import com.example.macollection.ui.AppViewModel
import com.example.macollection.ui.CollectionScreen
import com.example.macollection.ui.ConsoleEncyclopediaScreen
import com.example.macollection.ui.EncyclopediaScreen
import com.example.macollection.ui.EncycloMode
import com.example.macollection.ui.ItemDetailScreen
import com.example.macollection.ui.NewsScreen
import com.example.macollection.ui.BackupScreen
import com.example.macollection.ui.BatchScanDialog
import com.example.macollection.ui.ads.watchRewardedAd
import com.example.macollection.data.GameGuide
import com.example.macollection.data.GameShopCatalog
import com.example.macollection.ui.GameViewModel
import com.example.macollection.ui.GamerScreenBackground
import com.example.macollection.ui.OnboardingScreen
import com.example.macollection.ui.games.engine.GameIntroScreen
import com.example.macollection.ui.TotalScreen
import com.example.macollection.ui.TEST_ITEM_LIMIT
import com.example.macollection.ui.games.GamesHubScreen
import com.example.macollection.ui.games.ShopScreen
import com.example.macollection.ui.games.arkanoid.ArkanoidScreen
import com.example.macollection.ui.games.centipede.CentipedeScreen
import com.example.macollection.ui.games.invaders.SpaceInvadersScreen
import com.example.macollection.ui.games.frogger.FroggerScreen
import com.example.macollection.ui.games.pacman.PacmanScreen
import com.example.macollection.ui.games.simon.SimonScreen
import com.example.macollection.ui.games.snake.SnakeScreen
import com.example.macollection.ui.games.tetris.TetrisScreen
import com.example.macollection.ui.games.pong.PongScreen
import com.example.macollection.ui.games.quiz.QuizScreen
import com.example.macollection.ui.theme.MaCollectionTheme
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPurple
import com.example.macollection.ui.theme.AppTheme
import com.example.macollection.ui.theme.themedGradient
import com.example.macollection.ui.theme.SurfaceBg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPrefs.load(this)
        setContent {
            MaCollectionTheme {
                AppRoot()
            }
        }
    }
}

enum class Tab { COLLECTION, WISHLIST, ENCYCLO, TOTAL, GAMES, BACKUP }

/** Adresse de contact affichée dans les Paramètres (bouton "Nous contacter"). */
private const val CONTACT_EMAIL = "nawash083@gmail.com"

/** Sous-écran plein écran ouvert depuis l'onglet Jeux (même pattern que `viewing`/`encyclo`). */
private enum class GamesSubScreen { QUIZ, PONG, ARKANOID, INVADERS, CENTIPEDE, PACMAN, TETRIS, SNAKE, SIMON, FROGGER, SHOP }

/** État du formulaire Collection : nouvel objet (scan ou manuel) OU modification. */
data class CollectionEditor(
    val existing: CollectionItem? = null,
    val name: String = "",
    val barcode: String? = null,
    val presetName: String? = null,
    val accessoryName: String? = null,
    val gameMatch: GameInfo? = null,
    val gameConsoleHint: String? = null,
    val coverUri: String? = null,
    val originalCoverUri: String? = null,
    val isWishlist: Boolean = false
)

@Composable
fun AppRoot(vm: AppViewModel = viewModel(), gameVm: GameViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        vm.refreshAllPrices()
        vm.refreshCurrencyRates()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Statut Premium (achat réel, points, ou édition V2SP UNLOCK_ALL) — conditionne l'accès
    // gratuit au scan par lot et à l'analyse IA approfondie garantie du scan unitaire.
    val isPremium by gameVm.isPremiumAllAccess.collectAsState()

    var showOnboarding by remember { mutableStateOf(!AppPrefs.onboardingSeen) }
    var onboardingLangChosen by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(Tab.COLLECTION) }
    var gamesSubScreen by remember { mutableStateOf<GamesSubScreen?>(null) }
    var showChooser by remember { mutableStateOf(false) }
    // true quand le chooser/formulaire en cours sert à ajouter un SOUHAIT (onglet Acquisitions
    // futures) plutôt qu'un objet de la collection : même flux exact (scan/photo/manuel), la
    // seule différence est ce drapeau reporté sur l'objet créé (exclu de la valeur totale).
    var chooserForWishlist by remember { mutableStateOf(false) }
    // Variante TEST (BuildConfig.IS_TEST) : plafond de 10 objets de collection pour les testeurs.
    // Toujours compté sur la collection complète (vm.allOwnedItems), pas vm.items qui suit le
    // filtre par type affiché sur l'onglet Collection — sinon ce plafond semblait non atteint
    // dès qu'un filtre (Consoles/Accessoires/Jeux) masquait une partie des objets déjà possédés.
    val testCollectionItems by vm.allOwnedItems.collectAsState()
    val testLimitReached = BuildConfig.IS_TEST && testCollectionItems.size >= TEST_ITEM_LIMIT
    var showTestLimitDialog by remember { mutableStateOf(false) }
    var editor by remember { mutableStateOf<CollectionEditor?>(null) }
    var viewing by remember { mutableStateOf<CollectionItem?>(null) }
    // Hissés ici (avant les branches editor/viewing ET le when(tab) plus bas) pour que la
    // position de défilement de chaque liste survive à la fois à un aller-retour vers la fiche
    // de détail/modification et à un changement d'onglet — sinon un état par défaut serait
    // recréé de zéro chaque fois que CollectionScreen est retiré puis remis en composition.
    val collectionListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val wishlistListState = androidx.compose.foundation.lazy.rememberLazyListState()
    // Même besoin pour la liste des consoles de l'Encyclopédie : ouvrir la fiche plein écran
    // d'une console (état `encyclo` ci-dessous) retire aussi EncyclopediaScreen de la
    // composition, donc sans cet état hissé ici, le retour à la liste remontrait en haut.
    val encycloConsoleListState = androidx.compose.foundation.lazy.rememberLazyListState()
    var encyclo by remember { mutableStateOf<ConsolePreset?>(null) }
    // Fiche perso exacte à l'origine de la ligne tapée (voir EncyclopediaScreen) : évite de la
    // retrouver par nom (customPresetFor), ce qui associait par erreur les boutons Modifier/
    // Supprimer aux DEUX lignes quand une fiche perso et une fiche native partageaient le même nom.
    var encycloCustom by remember { mutableStateOf<CustomPreset?>(null) }
    // Mode courant de l'encyclopédie (consoles/accessoires), remonté ici pour que le bouton « + »
    // pré-sélectionne le bon type dans le formulaire d'ajout (sinon un accessoire ajouté depuis le
    // mode accessoires était enregistré comme console).
    var encycloMode by remember { mutableStateOf(EncycloMode.CONSOLES) }
    // Sélection multiple dans l'Encyclopédie (bouton dédié dans la TopAppBar, visible seulement
    // sur cet onglet) : permet d'ajouter plusieurs consoles/accessoires d'un coup à la collection
    // ou aux souhaits, sans repasser fiche par fiche.
    var encycloSelectionMode by remember { mutableStateOf(false) }
    // Écran Actus (nouveautés retrogaming à venir), ouvert depuis l'icône dédiée dans la TopAppBar
    // de l'Encyclopédie — overlay plein écran indépendant du Tab, sur le modèle de gamesSubScreen.
    var showNewsScreen by remember { mutableStateOf(false) }
    var addingCustomPreset by remember { mutableStateOf(false) }
    var editingCustomPreset by remember { mutableStateOf<CustomPreset?>(null) }
    var showLangDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var language by remember {
        mutableStateOf(
            AppCompatDelegate.getApplicationLocales().get(0)?.language ?: AppPrefs.language
        )
    }

    // On recadre d'abord (l'utilisateur cadre bien sur l'étiquette/la jaquette), puis on
    // analyse la photo RECADRÉE : un cadrage serré sur le texte aide l'OCR/Vision à mieux lire.
    // Affiche une roue de chargement pendant l'analyse (OCR + Vision en dernier recours) :
    // ça peut prendre plusieurs secondes, sans ça l'app paraît plantée.
    var scanning by remember { mutableStateOf(false) }
    var deepScanning by remember { mutableStateOf(false) }
    var batchScanning by remember { mutableStateOf(false) }
    var batchResults by remember { mutableStateOf<List<GeminiVision.BatchItem>?>(null) }
    // Vrai quand l'analyse IA a échoué (quota atteint, réseau…) — à distinguer d'un lot vide.
    var batchError by remember { mutableStateOf(false) }
    var pendingOriginalUri by remember { mutableStateOf<Uri?>(null) }

    // Scan multiple : photo de lot envoyée telle quelle à Gemini (pas de recadrage restrictif).
    val batchPhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) scope.launch {
            scanning = true; batchScanning = true; batchError = false
            // Gemini d'abord : meilleure qualité de reconnaissance que Groq (Qwen3.6, vérifié en
            // conditions réelles), et le scan par lot ne coûte qu'UN SEUL appel Gemini par photo
            // (quel que soit le nombre d'articles détectés dessus) — l'inverser pour économiser le
            // quota Gemini (20 requêtes/jour) n'apportait donc quasiment rien, tout en dégradant la
            // détection. Repli sur Groq si Gemini échoue (quota atteint, réseau…).
            // null = échec des deux ; liste vide = analyse OK mais rien détecté.
            val res = runCatching { GeminiVision.identifyBatch(context, uri) }.getOrNull()
                ?: runCatching { GroqVision.identifyBatch(context, uri) }.getOrNull()
            scanning = false; batchScanning = false
            if (res == null) batchError = true else batchResults = res
        }
    }
    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val croppedUri = result.uriContent
            if (croppedUri != null) {
                scope.launch {
                    scanning = true; deepScanning = false
                    val r = runCatching { ScanTools.scanImage(context, croppedUri) { deepScanning = true } }.getOrNull()
                    val saved = withContext(Dispatchers.IO) { MediaUtils.copyToInternal(context, croppedUri) }
                    // Garde une copie stable de la photo AVANT ce recadrage : le bouton
                    // « Recadrer » du formulaire doit repartir de la vraie source, pas recadrer
                    // un recadrage (marge de cadrage qui se réduit à chaque tentative).
                    val origUri = pendingOriginalUri
                    val originalSaved = if (origUri != null) {
                        withContext(Dispatchers.IO) { MediaUtils.copyToInternal(context, origUri) } ?: saved
                    } else saved
                    scanning = false
                    editor = CollectionEditor(
                        name = r?.suggestedName ?: "",
                        barcode = r?.barcode,
                        presetName = r?.consolePresetName,
                        accessoryName = r?.accessoryName,
                        gameMatch = r?.gameMatch,
                        gameConsoleHint = r?.gameConsoleHint,
                        coverUri = saved,
                        originalCoverUri = originalSaved,
                        isWishlist = chooserForWishlist
                    )
                }
            }
        }
    }

    // Sélecteur de photos (aucune permission stockage requise).
    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingOriginalUri = uri
            cropLauncher.launch(CropImageContractOptions(uri, CropImageOptions(activityMenuIconColor = android.graphics.Color.WHITE, cropMenuCropButtonTitle = "OK")))
        }
    }

    // Choix d'une image de fond personnalisée (Réglages) : copiée en stockage interne pour
    // persister, puis enregistrée comme préférence (état Compose -> mise à jour immédiate).
    val backgroundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val saved = withContext(Dispatchers.IO) { MediaUtils.copyToInternal(context, uri) }
                if (saved != null) AppPrefs.setBackgroundImageUri(context, saved)
            }
        }
    }

    // Prise de photo directe à la caméra : utilisée à la fois pour le scan (texte/code-barres)
    // et comme photo principale par défaut du nouvel objet.
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
            val uri = Uri.parse(pending)
            pendingOriginalUri = uri
            cropLauncher.launch(CropImageContractOptions(uri, CropImageOptions(activityMenuIconColor = android.graphics.Color.WHITE, cropMenuCropButtonTitle = "OK")))
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

    if (showOnboarding) {
        if (!onboardingLangChosen) {
            OnboardingLanguageScreen(
                currentTag = language,
                onChosen = { tag ->
                    setAppLanguage(context, tag)
                    language = tag
                    onboardingLangChosen = true
                }
            )
        } else {
            OnboardingScreen(onFinish = {
                AppPrefs.setOnboardingSeen(context)
                showOnboarding = false
            })
        }
        return
    }

    if (scanning) {
        Box(Modifier.fillMaxSize().background(themedGradient()), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = NeonCyan)
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(when {
                        batchScanning -> R.string.batch_analyzing
                        deepScanning -> R.string.scan_deep_analysis
                        else -> R.string.scanning_in_progress
                    }),
                    color = Color.White
                )
            }
        }
        return
    }

    // --- Formulaires plein écran ---
    if (addingCustomPreset) {
        BackHandler { addingCustomPreset = false }
        AddCustomPresetForm(
            vm = vm,
            initialType = if (encycloMode == EncycloMode.ACCESSORIES) ItemType.ACCESSOIRE else ItemType.CONSOLE,
            onSaved = { addingCustomPreset = false },
            onCancel = { addingCustomPreset = false }
        )
        return
    }
    editingCustomPreset?.let { edited ->
        BackHandler { editingCustomPreset = null }
        AddCustomPresetForm(
            vm = vm,
            existing = edited,
            onSaved = { editingCustomPreset = null },
            onCancel = { editingCustomPreset = null }
        )
        return
    }
    editor?.let { ed ->
        BackHandler { editor = null }
        AddCollectionForm(
            vm = vm,
            existing = ed.existing,
            initialName = ed.name,
            initialBarcode = ed.barcode,
            initialPresetName = ed.presetName,
            initialAccessoryName = ed.accessoryName,
            initialGameMatch = ed.gameMatch,
            initialGameConsoleHint = ed.gameConsoleHint,
            initialCoverUri = ed.coverUri,
            initialOriginalCoverUri = ed.originalCoverUri,
            isWishlist = ed.isWishlist,
            onSave = { item, photos -> vm.saveCollectionItem(item, photos); editor = null },
            onCancel = { editor = null }
        )
        return
    }
    viewing?.let { snapshot ->
        // On relit l'objet en direct depuis la base plutôt que d'utiliser le snapshot capturé à
        // l'ouverture : si l'utilisateur corrige le nom ou la plateforme (via Modifier) puis
        // revient sur cette fiche, le lien PriceCharting (et tout le reste) doit refléter la
        // valeur corrigée, pas l'ancienne reconnaissance automatique. L'objet peut venir de la
        // collection ou des souhaits (même table) : on cherche dans les deux listes.
        val liveItems by vm.items.collectAsState()
        val liveWishlist by vm.wishlist.collectAsState()
        val item = (liveItems + liveWishlist).firstOrNull { it.id == snapshot.id } ?: snapshot
        BackHandler { viewing = null }
        ItemDetailScreen(
            vm = vm,
            item = item,
            loadHistory = { vm.priceHistory(it) },
            onEdit = { editor = CollectionEditor(existing = item); viewing = null },
            onDelete = { vm.deleteCollectionItem(item); viewing = null },
            onBack = { viewing = null },
            canMoveToCollection = {
                // Variante TEST : déplacer un souhait vers la collection est aussi plafonné.
                if (testLimitReached) { showTestLimitDialog = true; false } else true
            }
        )
        return
    }
    encyclo?.let { preset ->
        BackHandler { encyclo = null; encycloCustom = null }
        // Fiche perso (ajoutée par l'utilisateur) : identique à une fiche native, avec en plus la
        // possibilité de la supprimer. On utilise la référence exacte remontée par la ligne tapée
        // (encycloCustom), pas une recherche par nom : si une fiche perso et une fiche native
        // partagent le même nom, une recherche par nom associerait les boutons aux deux lignes.
        val customConsole = encycloCustom
        ConsoleEncyclopediaScreen(
            vm = vm,
            preset = preset,
            onAddToCollection = {
                // Variante TEST : l'ajout depuis l'encyclopédie est aussi plafonné.
                if (testLimitReached) {
                    showTestLimitDialog = true
                } else {
                    editor = CollectionEditor(presetName = preset.name, isWishlist = false)
                    encyclo = null; encycloCustom = null
                }
            },
            onAddToWishlist = {
                editor = CollectionEditor(presetName = preset.name, isWishlist = true)
                encyclo = null; encycloCustom = null
            },
            onAddGameToCollection = { game ->
                // Variante TEST : l'ajout depuis l'encyclopédie est aussi plafonné.
                if (testLimitReached) {
                    showTestLimitDialog = true
                } else {
                    editor = CollectionEditor(gameMatch = game, gameConsoleHint = preset.name, isWishlist = false)
                    encyclo = null; encycloCustom = null
                }
            },
            onAddGameToWishlist = { game ->
                editor = CollectionEditor(gameMatch = game, gameConsoleHint = preset.name, isWishlist = true)
                encyclo = null; encycloCustom = null
            },
            onEdit = customConsole?.let { { editingCustomPreset = it; encyclo = null; encycloCustom = null } },
            onDelete = customConsole?.let { { vm.deleteCustomPreset(it); encyclo = null; encycloCustom = null } },
            onBack = { encyclo = null; encycloCustom = null }
        )
        return
    }
    gamesSubScreen?.let { screen ->
        BackHandler { gamesSubScreen = null }
        // Écran d'explication (règles) au tout premier lancement de chaque jeu : on ne compose la
        // partie qu'après avoir appuyé sur « Jouer », pour que rien ne bouge pendant la lecture.
        val guideId = when (screen) {
            GamesSubScreen.QUIZ -> GameShopCatalog.QUIZ
            GamesSubScreen.PONG -> GameShopCatalog.PONG
            GamesSubScreen.ARKANOID -> GameShopCatalog.ARKANOID
            GamesSubScreen.INVADERS -> GameShopCatalog.SPACE_INVADERS
            GamesSubScreen.CENTIPEDE -> GameShopCatalog.CENTIPEDE
            GamesSubScreen.PACMAN -> GameShopCatalog.PACMAN
            GamesSubScreen.TETRIS -> GameShopCatalog.TETRIS
            GamesSubScreen.SNAKE -> GameShopCatalog.SNAKE
            GamesSubScreen.SIMON -> GameShopCatalog.SIMON
            GamesSubScreen.FROGGER -> GameShopCatalog.FROGGER
            GamesSubScreen.SHOP -> null
        }
        val guide = guideId?.let { GameGuide.forId(it) }
        var showIntro by remember(screen) {
            mutableStateOf(guide != null && guideId != null && !AppPrefs.gameIntrosSeen.value.contains(guideId))
        }
        if (guide != null && guideId != null && showIntro) {
            GameIntroScreen(
                guide = guide,
                onStart = { AppPrefs.markGameIntroSeen(context, guideId); showIntro = false },
                onExit = { gamesSubScreen = null }
            )
            return
        }
        when (screen) {
            GamesSubScreen.QUIZ -> QuizScreen(vm = gameVm, onExit = { gamesSubScreen = null })
            GamesSubScreen.PONG -> PongScreen(vm = gameVm, onExit = { gamesSubScreen = null })
            GamesSubScreen.ARKANOID -> ArkanoidScreen(vm = gameVm, onExit = { gamesSubScreen = null })
            GamesSubScreen.INVADERS -> SpaceInvadersScreen(vm = gameVm, onExit = { gamesSubScreen = null })
            GamesSubScreen.CENTIPEDE -> CentipedeScreen(vm = gameVm, onExit = { gamesSubScreen = null })
            GamesSubScreen.PACMAN -> PacmanScreen(vm = gameVm, onExit = { gamesSubScreen = null })
            GamesSubScreen.TETRIS -> TetrisScreen(vm = gameVm, onExit = { gamesSubScreen = null })
            GamesSubScreen.SNAKE -> SnakeScreen(vm = gameVm, onExit = { gamesSubScreen = null })
            GamesSubScreen.SIMON -> SimonScreen(vm = gameVm, onExit = { gamesSubScreen = null })
            GamesSubScreen.FROGGER -> FroggerScreen(vm = gameVm, onExit = { gamesSubScreen = null })
            GamesSubScreen.SHOP -> ShopScreen(vm = gameVm, onBack = { gamesSubScreen = null })
        }
        return
    }
    if (showNewsScreen) {
        BackHandler { showNewsScreen = false }
        NewsScreen(vm = vm, onBack = { showNewsScreen = false })
        return
    }

    // --- Menu d'ajout (onglet Collection) ---
    if (showChooser) {
        AlertDialog(
            onDismissRequest = { showChooser = false },
            title = { Text(if (chooserForWishlist) stringResource(R.string.new_wish_title) else stringResource(R.string.add_item_dialog_title)) },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showChooser = false }) { Text(stringResource(R.string.cancel)) }
            },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showChooser = false
                            fun applyResult(code: String, r: ScanTools.ScanResult?) {
                                editor = CollectionEditor(
                                    name = r?.suggestedName ?: "",
                                    barcode = code,
                                    presetName = r?.consolePresetName,
                                    accessoryName = r?.accessoryName,
                                    gameMatch = r?.gameMatch,
                                    gameConsoleHint = r?.gameConsoleHint,
                                    isWishlist = chooserForWishlist
                                )
                            }
                            // Suite "complète" (bases à quota partagé) : seulement si le quota
                            // gratuit du jour n'est pas atteint (Premium/V2SP toujours illimités),
                            // sinon pub récompensée pour débloquer un scan de plus (cf.
                            // GameShopCatalog.FREE_DAILY_BARCODE_SCANS).
                            fun runFullScan(code: String) {
                                scope.launch {
                                    AppPrefs.recordBarcodeScanUsed(context)
                                    scanning = true
                                    val r = runCatching { ScanTools.identifyFromBarcode(code, allowQuotaLimitedSources = true) }.getOrNull()
                                    scanning = false
                                    applyResult(code, r)
                                }
                            }
                            fun offerFullScan(code: String) {
                                if (isPremium || AppPrefs.canScanBarcodeFree(context)) {
                                    runFullScan(code)
                                } else {
                                    watchRewardedAd(
                                        context,
                                        onRewarded = { AppPrefs.recordBarcodeBonusScanGranted(context) },
                                        onClosed = { if (isPremium || AppPrefs.canScanBarcodeFree(context)) runFullScan(code) }
                                    )
                                }
                            }
                            scope.launch {
                                val code = runCatching { ScanTools.scanCamera(context) }.getOrNull() ?: return@launch
                                // Premier essai TOUJOURS gratuit (ScanDex + eBay, pas de quota
                                // partagé) : la plupart des jeux se résolvent déjà ici, sans jamais
                                // avoir besoin du quota limité ni d'une pub.
                                scanning = true
                                val free = runCatching { ScanTools.identifyFromBarcode(code, allowQuotaLimitedSources = false) }.getOrNull()
                                scanning = false
                                if (free?.gameMatch != null || free?.consolePresetName != null || free?.accessoryName != null) {
                                    applyResult(code, free)
                                } else {
                                    offerFullScan(code)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.scan_barcode_option)) }

                    TextButton(
                        onClick = {
                            showChooser = false
                            photoLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.choose_photo_option)) }

                    TextButton(
                        onClick = {
                            showChooser = false
                            launchCamera()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.take_photo_option)) }

                    TextButton(
                        onClick = {
                            showChooser = false
                            // Scan par lot : toujours Premium ou pub (utilise systématiquement
                            // l'IA visuelle, contrairement au scan unitaire qui passe le plus
                            // souvent par l'OCR gratuit) — voir la note mémoire "pricing APIs"
                            // du 2026-07-16 pour le raisonnement économique complet.
                            fun launchBatchScan() = batchPhotoLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                            if (isPremium) launchBatchScan()
                            else watchRewardedAd(context, onRewarded = { launchBatchScan() })
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.batch_scan_option)) }

                    TextButton(
                        onClick = {
                            showChooser = false
                            editor = CollectionEditor(isWishlist = chooserForWishlist)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.manual_entry_option)) }
                }
            }
        )
    }

    // Scan multiple : échec de l'analyse IA (quota/réseau) -> message clair, pas « aucun article ».
    if (batchError) {
        AlertDialog(
            onDismissRequest = { batchError = false },
            title = { Text(stringResource(R.string.batch_unavailable)) },
            confirmButton = { TextButton(onClick = { batchError = false }) { Text(stringResource(R.string.close)) } }
        )
    }

    // Scan multiple : liste des articles détectés (cases à cocher) avant ajout au lot.
    batchResults?.let { res ->
        if (res.isEmpty()) {
            AlertDialog(
                onDismissRequest = { batchResults = null },
                title = { Text(stringResource(R.string.batch_none)) },
                confirmButton = { TextButton(onClick = { batchResults = null }) { Text(stringResource(R.string.close)) } }
            )
        } else {
            BatchScanDialog(
                items = res,
                onConfirm = { selected -> vm.saveBatch(selected, chooserForWishlist); batchResults = null },
                onDismiss = { batchResults = null }
            )
        }
    }

    // Intégration du lot validé : peut prendre plus d'une minute pour plusieurs jeux (recherche
    // IGDB/RAWG/Wikipédia + cote eBay/IA par jeu, l'un après l'autre) — sans cet indicateur,
    // l'écran semblait figé/planté pendant tout ce temps après la fermeture du dialogue de scan.
    val batchSavingCount by vm.batchSaving.collectAsState()
    batchSavingCount?.let { count ->
        AlertDialog(
            onDismissRequest = {},
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.batch_saving, count))
                }
            },
            confirmButton = {}
        )
    }

    if (showOptionsMenu) {
        val contactEmailSubject = stringResource(R.string.contact_email_subject)
        AlertDialog(
            onDismissRequest = { showOptionsMenu = false },
            title = { Text(stringResource(R.string.options_menu_title)) },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showOptionsMenu = false }) { Text(stringResource(R.string.close)) }
            },
            text = {
                Column {
                    TextButton(
                        onClick = { showOptionsMenu = false; showLangDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.options_language)) }
                    TextButton(
                        onClick = { showOptionsMenu = false; showCurrencyDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.options_currency)) }
                    TextButton(
                        onClick = { showOptionsMenu = false; tab = Tab.BACKUP },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.options_backup)) }
                    TextButton(
                        onClick = { showOptionsMenu = false; showOnboarding = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.options_replay_tutorial)) }
                    TextButton(
                        onClick = { showOptionsMenu = false; showTipsDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.options_tips)) }
                    TextButton(
                        onClick = {
                            showOptionsMenu = false
                            backgroundPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.options_background)) }
                    if (AppPrefs.backgroundImageUri.value != null) {
                        TextButton(
                            onClick = { showOptionsMenu = false; AppPrefs.setBackgroundImageUri(context, null) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.options_background_reset)) }
                    }
                    TextButton(
                        onClick = { showOptionsMenu = false; showThemeDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Thème") }
                    TextButton(
                        onClick = {
                            showOptionsMenu = false
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$CONTACT_EMAIL")
                                putExtra(Intent.EXTRA_SUBJECT, contactEmailSubject)
                            }
                            runCatching { context.startActivity(intent) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.options_contact)) }
                }
            }
        )
    }

    if (showThemeDialog) {
        val unlockedItems by gameVm.unlockedItemIds.collectAsState()
        val isPremium by gameVm.isPremiumAllAccess.collectAsState()
        val currentThemeId = AppPrefs.selectedTheme.value
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Thème de l'application") },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text(stringResource(R.string.close)) }
            },
            text = {
                Column {
                    Text(
                        "Choisis ton ambiance années 80. Les thèmes se débloquent dans la Boutique des jeux (100 points chacun).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    AppTheme.entries.forEach { theme ->
                        val available = theme == AppTheme.DEFAULT || isPremium ||
                            unlockedItems.contains(theme.id) ||
                            (!BuildConfig.IS_TEST && AppPrefs.devUnlockAll.value)
                        val selected = currentThemeId == theme.id
                        TextButton(
                            onClick = {
                                if (available) {
                                    AppPrefs.setSelectedTheme(context, theme.id)
                                    showThemeDialog = false
                                } else {
                                    // Verrouillé : on emmène directement à la Boutique.
                                    showThemeDialog = false
                                    tab = Tab.GAMES
                                    gamesSubScreen = GamesSubScreen.SHOP
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(theme.emoji, fontSize = 20.sp)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    when {
                                        !available -> "${theme.label} — 🔒 boutique"
                                        selected -> "${theme.label}  ✓"
                                        else -> theme.label
                                    },
                                    color = if (available) Color.Unspecified else Color.Gray,
                                    modifier = Modifier.weight(1f)
                                )
                                // Aperçu des deux couleurs d'accent du thème.
                                Box(
                                    Modifier.size(16.dp)
                                        .background(theme.accent, CircleShape)
                                )
                                Spacer(Modifier.width(4.dp))
                                Box(
                                    Modifier.size(16.dp)
                                        .background(theme.accentAlt, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    if (showTestLimitDialog) {
        AlertDialog(
            onDismissRequest = { showTestLimitDialog = false },
            title = { Text("Version test") },
            text = { Text("Cette version de test est limitée à $TEST_ITEM_LIMIT objets dans la collection.") },
            confirmButton = {
                TextButton(onClick = { showTestLimitDialog = false }) { Text(stringResource(R.string.close)) }
            }
        )
    }

    if (showTipsDialog) {
        AlertDialog(
            onDismissRequest = { showTipsDialog = false },
            title = { Text(stringResource(R.string.tips_dialog_title)) },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTipsDialog = false }) { Text(stringResource(R.string.close)) }
            },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())
                ) {
                    Text(
                        stringResource(R.string.tips_recognition_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.tips_recognition_text), style = MaterialTheme.typography.bodyMedium)
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.tips_crop_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.tips_crop_text), style = MaterialTheme.typography.bodyMedium)
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.tips_naming_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.tips_naming_text), style = MaterialTheme.typography.bodyMedium)
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.tips_price_ai_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.tips_price_ai_text), style = MaterialTheme.typography.bodyMedium)
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.tips_news_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.tips_news_text), style = MaterialTheme.typography.bodyMedium)
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.tips_encyclo_filter_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.tips_encyclo_filter_text), style = MaterialTheme.typography.bodyMedium)
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.tips_photo_zoom_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.tips_photo_zoom_text), style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }

    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text(stringResource(R.string.lang_dialog_title)) },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLangDialog = false }) { Text(stringResource(R.string.close)) }
            },
            text = {
                Column {
                    Text(
                        stringResource(R.string.lang_dialog_rawg_note),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())
                    ) {
                        supportedLanguages.forEach { (tag, flag, nameRes) ->
                            val name = "$flag  ${stringResource(nameRes)}"
                            TextButton(
                                onClick = {
                                    setAppLanguage(context, tag)
                                    language = tag
                                    showLangDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (language == tag) stringResource(R.string.lang_option_checked, name) else name
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    if (showCurrencyDialog) {
        val currentCurrency = AppPrefs.currency.value
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text(stringResource(R.string.currency_dialog_title)) },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text(stringResource(R.string.close)) }
            },
            text = {
                Column {
                    Text(
                        stringResource(R.string.currency_dialog_note),
                        style = MaterialTheme.typography.bodySmall
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())
                    ) {
                        com.example.macollection.data.CurrencyOptions.list.forEach { option ->
                            val name = "${option.symbol}  ${option.label} (${option.code})"
                            TextButton(
                                onClick = {
                                    AppPrefs.setCurrency(context, option.code)
                                    showCurrencyDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (currentCurrency == option.code) stringResource(R.string.lang_option_checked, name) else name
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    GamerScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(titleFor(tab), fontWeight = FontWeight.Bold) },
                    actions = {
                        // Sélection multiple : déplacée dans EncyclopediaScreen, à côté des boutons
                        // Consoles/Accessoires (voir EncyclopediaScreens.kt). Actus reste ici, visible
                        // sur tous les onglets (pas seulement l'Encyclopédie).
                        IconButton(onClick = { showNewsScreen = true }) {
                            Icon(
                                Icons.Filled.Campaign,
                                contentDescription = stringResource(R.string.news_content_description),
                                tint = Color.White
                            )
                        }
                        // Accès permanent à la Boutique depuis n'importe quel onglet, en plus du
                        // bouton existant dans l'onglet Jeux (les deux coexistent, rien n'est retiré).
                        IconButton(onClick = { tab = Tab.GAMES; gamesSubScreen = GamesSubScreen.SHOP }) {
                            Icon(
                                Icons.Filled.Storefront,
                                contentDescription = stringResource(R.string.shop_content_description),
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.settings_content_description),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                val navTheme = AppTheme.byId(AppPrefs.selectedTheme.value)
                NavigationBar(containerColor = SurfaceBg) {
                    NavigationBarItem(
                        selected = tab == Tab.COLLECTION,
                        onClick = { tab = Tab.COLLECTION },
                        icon = { ThemedNavIcon(navTheme, 0, Icons.Filled.VideogameAsset) },
                        label = { NavLabel(stringResource(R.string.nav_collection)) },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = tab == Tab.WISHLIST,
                        onClick = { tab = Tab.WISHLIST },
                        icon = { ThemedNavIcon(navTheme, 1, Icons.Filled.Favorite) },
                        label = { NavLabel(stringResource(R.string.nav_wishlist)) },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = tab == Tab.ENCYCLO,
                        onClick = { tab = Tab.ENCYCLO },
                        icon = { ThemedNavIcon(navTheme, 2, Icons.Filled.MenuBook) },
                        label = { NavLabel(stringResource(R.string.nav_encyclo)) },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = tab == Tab.TOTAL,
                        onClick = { tab = Tab.TOTAL },
                        icon = { ThemedNavIcon(navTheme, 3, Icons.Filled.Euro) },
                        label = { NavLabel(stringResource(R.string.nav_total)) },
                        colors = navColors()
                    )
                    NavigationBarItem(
                        selected = tab == Tab.GAMES,
                        onClick = { tab = Tab.GAMES; gamesSubScreen = null },
                        icon = { ThemedNavIcon(navTheme, 4, Icons.Filled.SportsEsports) },
                        label = { NavLabel(stringResource(R.string.nav_games)) },
                        colors = navColors()
                    )
                }
            },
            floatingActionButton = {
                if (tab == Tab.COLLECTION || tab == Tab.WISHLIST || tab == Tab.ENCYCLO) {
                    FloatingActionButton(
                        onClick = {
                            when (tab) {
                                Tab.WISHLIST -> { chooserForWishlist = true; showChooser = true }
                                Tab.ENCYCLO -> addingCustomPreset = true
                                // Onglet Collection : bloqué au plafond dans la variante TEST.
                                else -> {
                                    if (testLimitReached) showTestLimitDialog = true
                                    else { chooserForWishlist = false; showChooser = true }
                                }
                            }
                        },
                        containerColor = NeonPurple,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_content_description))
                    }
                }
            }
        ) { padding ->
            // Navigation par glissement horizontal entre les pages principales (comme un carrousel).
            // Le seuil évite les changements d'onglet accidentels ; le défilement vertical des listes
            // n'est pas gêné (seuls les glissés nettement horizontaux déclenchent le changement).
            val swipeOrder = listOf(Tab.COLLECTION, Tab.WISHLIST, Tab.ENCYCLO, Tab.TOTAL, Tab.GAMES)
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(tab) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onDragEnd = {
                                val threshold = 72.dp.toPx()
                                val idx = swipeOrder.indexOf(tab)
                                if (idx >= 0) {
                                    if (totalDrag <= -threshold && idx < swipeOrder.lastIndex) {
                                        tab = swipeOrder[idx + 1]; gamesSubScreen = null
                                    } else if (totalDrag >= threshold && idx > 0) {
                                        tab = swipeOrder[idx - 1]; gamesSubScreen = null
                                    }
                                }
                            }
                        ) { change, dragAmount -> totalDrag += dragAmount; change.consume() }
                    }
            ) {
            when (tab) {
                Tab.COLLECTION -> CollectionScreen(
                    vm = vm,
                    onOpen = { viewing = it },
                    onEdit = { editor = CollectionEditor(existing = it) },
                    modifier = Modifier.padding(padding),
                    listState = collectionListState
                )
                Tab.WISHLIST -> CollectionScreen(
                    vm = vm,
                    onOpen = { viewing = it },
                    onEdit = { editor = CollectionEditor(existing = it) },
                    modifier = Modifier.padding(padding),
                    wishlist = true,
                    listState = wishlistListState
                )
                Tab.ENCYCLO -> EncyclopediaScreen(
                    vm = vm,
                    mode = encycloMode,
                    onModeChange = { encycloMode = it },
                    onOpenConsole = { preset, custom -> encyclo = preset; encycloCustom = custom },
                    onAddAccessory = { preset, isWishlist ->
                        // Variante TEST : l'ajout depuis l'encyclopédie est aussi plafonné.
                        if (!isWishlist && testLimitReached) {
                            showTestLimitDialog = true
                        } else {
                            editor = CollectionEditor(accessoryName = preset.name, isWishlist = isWishlist)
                        }
                    },
                    onEditPreset = { editingCustomPreset = it },
                    modifier = Modifier.padding(padding),
                    consoleListState = encycloConsoleListState,
                    selectionMode = encycloSelectionMode,
                    onToggleSelectionMode = { encycloSelectionMode = !encycloSelectionMode },
                    onAddSelectedToCollection = { consoles, accessories, isWishlist ->
                        // Variante TEST : l'ajout groupé depuis l'encyclopédie est aussi plafonné.
                        if (!isWishlist && testLimitReached) {
                            showTestLimitDialog = true
                        } else {
                            vm.addPresetsToCollection(consoles, accessories, isWishlist)
                            encycloSelectionMode = false
                        }
                    }
                )
                Tab.TOTAL -> TotalScreen(vm, gameVm, Modifier.padding(padding))
                Tab.GAMES -> GamesHubScreen(
                    vm = gameVm,
                    onOpenQuiz = { gamesSubScreen = GamesSubScreen.QUIZ },
                    onOpenPong = { gamesSubScreen = GamesSubScreen.PONG },
                    onOpenArcade = { gameId ->
                        when (gameId) {
                            com.example.macollection.data.GameShopCatalog.ARKANOID ->
                                gamesSubScreen = GamesSubScreen.ARKANOID
                            com.example.macollection.data.GameShopCatalog.SPACE_INVADERS ->
                                gamesSubScreen = GamesSubScreen.INVADERS
                            com.example.macollection.data.GameShopCatalog.CENTIPEDE ->
                                gamesSubScreen = GamesSubScreen.CENTIPEDE
                            com.example.macollection.data.GameShopCatalog.PACMAN ->
                                gamesSubScreen = GamesSubScreen.PACMAN
                            com.example.macollection.data.GameShopCatalog.TETRIS ->
                                gamesSubScreen = GamesSubScreen.TETRIS
                            com.example.macollection.data.GameShopCatalog.SNAKE ->
                                gamesSubScreen = GamesSubScreen.SNAKE
                            com.example.macollection.data.GameShopCatalog.SIMON ->
                                gamesSubScreen = GamesSubScreen.SIMON
                            com.example.macollection.data.GameShopCatalog.FROGGER ->
                                gamesSubScreen = GamesSubScreen.FROGGER
                        }
                    },
                    onOpenShop = { gamesSubScreen = GamesSubScreen.SHOP },
                    modifier = Modifier.padding(padding)
                )
                Tab.BACKUP -> BackupScreen(
                    vm = vm,
                    gameVm = gameVm,
                    onOpenShop = { tab = Tab.GAMES; gamesSubScreen = GamesSubScreen.SHOP },
                    modifier = Modifier.padding(padding)
                )
            }
            }
        }
    }
}

@Composable
private fun NavLabel(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        softWrap = false
    )
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.secondary,
    selectedTextColor = MaterialTheme.colorScheme.secondary,
    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
    unselectedIconColor = Color(0xFF9A9AB5),
    unselectedTextColor = Color(0xFF9A9AB5)
)

/**
 * Icône d'un onglet : l'emoji du thème actif si défini (thèmes films), sinon l'icône Material
 * d'origine (thème par défaut). Emoji standards — aucune image de film (droits d'auteur).
 */
@Composable
private fun ThemedNavIcon(theme: AppTheme, index: Int, fallback: androidx.compose.ui.graphics.vector.ImageVector) {
    val emoji = theme.navIcons.getOrNull(index)
    when {
        emoji == "@delorean" -> GreySportsCarIcon()
        emoji != null -> Text(emoji, fontSize = 20.sp)
        else -> Icon(fallback, contentDescription = null)
    }
}

/**
 * Icône dessinée d'une voiture de sport grise/argentée (silhouette basse et anguleuse évoquant
 * la DeLorean de Retour vers le futur), avec la lueur bleue du voyage temporel dessous. Dessin
 * vectoriel 100 % original (pas d'emoji, pas de reproduction d'une marque déposée).
 */
@Composable
private fun GreySportsCarIcon() {
    Canvas(modifier = Modifier.size(26.dp)) {
        val w = size.width
        val h = size.height
        // Carrosserie argentée.
        val body = Path().apply {
            moveTo(0.05f * w, 0.62f * h)
            lineTo(0.05f * w, 0.58f * h)
            lineTo(0.30f * w, 0.50f * h)
            lineTo(0.42f * w, 0.36f * h)
            lineTo(0.62f * w, 0.35f * h)
            lineTo(0.75f * w, 0.50f * h)
            lineTo(0.95f * w, 0.55f * h)
            lineTo(0.95f * w, 0.64f * h)
            lineTo(0.05f * w, 0.64f * h)
            close()
        }
        drawPath(body, Color(0xFFC2C6CE))
        drawPath(body, Color(0xFF5A5F6B), style = Stroke(width = w * 0.03f))
        // Vitres (teinte bleutée).
        val glass = Path().apply {
            moveTo(0.34f * w, 0.50f * h)
            lineTo(0.43f * w, 0.39f * h)
            lineTo(0.60f * w, 0.385f * h)
            lineTo(0.71f * w, 0.50f * h)
            close()
        }
        drawPath(glass, Color(0xFF8FB8DB))
        // Roues (pneu noir + jante argentée).
        val wheelR = 0.11f * w
        val hubR = 0.05f * w
        listOf(0.30f, 0.73f).forEach { fx ->
            drawCircle(Color(0xFF1B1B22), wheelR, Offset(fx * w, 0.66f * h))
            drawCircle(Color(0xFFB8BCC4), hubR, Offset(fx * w, 0.66f * h))
        }
        // Lueur bleue du voyage temporel sous la voiture.
        drawLine(Color(0xFF2E7DFF), Offset(0.14f * w, 0.82f * h), Offset(0.86f * w, 0.82f * h), strokeWidth = w * 0.05f)
    }
}

@Composable
private fun titleFor(tab: Tab): String = when (tab) {
    Tab.COLLECTION -> stringResource(R.string.title_collection)
    Tab.WISHLIST -> stringResource(R.string.title_wishlist)
    Tab.ENCYCLO -> stringResource(R.string.title_encyclo)
    Tab.TOTAL -> stringResource(R.string.title_total)
    Tab.GAMES -> stringResource(R.string.title_games)
    Tab.BACKUP -> stringResource(R.string.title_backup)
}

/** Premier écran du tutoriel : choix de la langue, pour que la suite du tutoriel s'affiche déjà traduite. */
@Composable
private fun OnboardingLanguageScreen(currentTag: String, onChosen: (String) -> Unit) {
    GamerScreenBackground {
        Column(
            Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Text(
                stringResource(R.string.onboarding_choose_language),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            supportedLanguages.forEach { (tag, flag, nameRes) ->
                val name = "$flag  ${stringResource(nameRes)}"
                TextButton(
                    onClick = { onChosen(tag) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (currentTag == tag) stringResource(R.string.lang_option_checked, name) else name)
                }
            }
        }
    }
}

/** Liste des langues prises en charge : (tag BCP-47, drapeau emoji, ressource du nom natif). */
private val supportedLanguages: List<Triple<String, String, Int>> = listOf(
    Triple("fr", "🇫🇷", R.string.lang_name_fr),
    Triple("en", "🇬🇧", R.string.lang_name_en),
    Triple("es", "🇪🇸", R.string.lang_name_es),
    Triple("it", "🇮🇹", R.string.lang_name_it),
    Triple("de", "🇩🇪", R.string.lang_name_de),
    Triple("pt", "🇵🇹", R.string.lang_name_pt),
    Triple("ru", "🇷🇺", R.string.lang_name_ru),
    Triple("el", "🇬🇷", R.string.lang_name_el),
    Triple("tr", "🇹🇷", R.string.lang_name_tr),
    Triple("ja", "🇯🇵", R.string.lang_name_ja),
    Triple("zh", "🇨🇳", R.string.lang_name_zh)
)

/**
 * Change la langue d'affichage de toute l'app via AppCompat (persistance automatique),
 * et garde AppPrefs synchronisé pour la logique RAWG (descriptions fr/en de jeux).
 */
private fun setAppLanguage(context: android.content.Context, tag: String) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    // Les fiches RAWG ne sont disponibles qu'en fr/en : français si la langue choisie est le
    // français, anglais pour toutes les autres langues.
    val rawgLang = if (tag == "fr") "fr" else "en"
    AppPrefs.setLanguage(context, rawgLang)
    AppPrefs.applyLanguageCurrencyDefault(context, tag)
}
