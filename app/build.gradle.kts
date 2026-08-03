import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// Lit la clé API RAWG depuis local.properties (non versionné).
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val rawgApiKey: String = localProps.getProperty("rawgApiKey") ?: ""
val ebayClientId: String = localProps.getProperty("ebayClientId") ?: ""
val ebayClientSecret: String = localProps.getProperty("ebayClientSecret") ?: ""
// Clé de secours : bascule automatique si la clé principale atteint son quota eBay (voir EbayPrices.kt).
val ebayClientIdBackup: String = localProps.getProperty("ebayClientIdBackup") ?: ""
val ebayClientSecretBackup: String = localProps.getProperty("ebayClientSecretBackup") ?: ""
// IGDB s'utilise via un compte développeur Twitch (Client ID + Secret, flux "client credentials").
val twitchClientId: String = localProps.getProperty("twitchClientId") ?: ""
val twitchClientSecret: String = localProps.getProperty("twitchClientSecret") ?: ""
// Reconnaissance visuelle par IA (Gemini). À défaut de clé dédiée, on réutilise la clé Google
// Cloud "vision" existante (même format AIza..., même API generativelanguage si activée).
val geminiApiKey: String = (localProps.getProperty("geminiApiKey")?.takeIf { it.isNotBlank() }
    ?: localProps.getProperty("visionApiKey")) ?: ""
// Groq (Llama multimodal) : reconnaissance visuelle de secours si le quota Gemini est atteint.
val groqApiKey: String = localProps.getProperty("groqApiKey") ?: ""
// Tavily (recherche web) : 3e recours pour l'estimation de cote quand eBay ET Gemini échouent
// tous les deux (quota Gemini très bas, 20 requêtes/jour) — quota Tavily séparé et plus généreux.
val tavilyApiKey: String = localProps.getProperty("tavilyApiKey") ?: ""
// Barcode Lookup : 2e source d'identification par code-barres (après UPCitemdb, avant eBay).
val barcodeLookupApiKey: String = localProps.getProperty("barcodeLookupApiKey") ?: ""
// Barcode Spider : 3e source d'identification par code-barres, quota gratuit distinct des deux
// précédentes (100 requêtes/jour propres à ce compte).
val barcodeSpiderApiKey: String = localProps.getProperty("barcodeSpiderApiKey") ?: ""
// ScanDex : base spécialisée jeux vidéo (code-barres -> fiche IGDB déjà appariée), essayée en
// tout premier pour un jeu, avant les bases produits généralistes ci-dessus.
val scanDexApiKey: String = localProps.getProperty("scanDexApiKey") ?: ""

android {
    namespace = "com.example.macollection"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nawash.macollection"
        minSdk = 24
        targetSdk = 36
        versionCode = 11
        versionName = "2.7"

        buildConfigField("String", "RAWG_API_KEY", "\"$rawgApiKey\"")
        buildConfigField("String", "EBAY_CLIENT_ID", "\"$ebayClientId\"")
        buildConfigField("String", "EBAY_CLIENT_SECRET", "\"$ebayClientSecret\"")
        buildConfigField("String", "EBAY_CLIENT_ID_BACKUP", "\"$ebayClientIdBackup\"")
        buildConfigField("String", "EBAY_CLIENT_SECRET_BACKUP", "\"$ebayClientSecretBackup\"")
        buildConfigField("String", "TWITCH_CLIENT_ID", "\"$twitchClientId\"")
        buildConfigField("String", "TWITCH_CLIENT_SECRET", "\"$twitchClientSecret\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
        buildConfigField("String", "TAVILY_API_KEY", "\"$tavilyApiKey\"")
        buildConfigField("String", "BARCODE_LOOKUP_API_KEY", "\"$barcodeLookupApiKey\"")
        buildConfigField("String", "BARCODE_SPIDER_API_KEY", "\"$barcodeSpiderApiKey\"")
        buildConfigField("String", "SCANDEX_API_KEY", "\"$scanDexApiKey\"")

        // N'embarque que l'architecture ARM 64 (tous les téléphones modernes) -> APK plus léger.
        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    // Deux éditions depuis le MÊME code :
    //  - "full" : l'appli normale (com.nawash.macollection), économie de points complète.
    //  - "test" : l'appli de test partagée aux testeurs (com.nawash.macollection.test),
    //    installable côté à côté. BuildConfig.IS_TEST = true active les restrictions (plafond
    //    de 10 objets, achats de jeux/thèmes/premium bloqués). Comme IS_TEST est une constante
    //    de compilation, R8 supprime carrément le code de l'autre édition dans chaque APK :
    //    impossible de contourner les blocages à l'exécution.
    flavorDimensions += "edition"
    productFlavors {
        create("full") {
            dimension = "edition"
            buildConfigField("boolean", "IS_TEST", "false")
            buildConfigField("boolean", "ADS_ENABLED", "true")
            buildConfigField("boolean", "UNLOCK_ALL", "false")
            manifestPlaceholders["appLabel"] = "@string/app_name"
        }
        // Nom "restricted" : Android interdit tout nom de flavor commençant par "test"
        // (réservé aux sources de test), d'où ce nom sans rapport avec le préfixe.
        create("restricted") {
            dimension = "edition"
            applicationIdSuffix = ".test"
            versionNameSuffix = "-test"
            buildConfigField("boolean", "IS_TEST", "true")
            buildConfigField("boolean", "ADS_ENABLED", "true")
            buildConfigField("boolean", "UNLOCK_ALL", "false")
            manifestPlaceholders["appLabel"] = "Ma Collection V2 (TEST)"
        }
        // "noads" : édition SANS PUBLICITÉ (com.nawash.macollection.sp, « V2SP »), installable
        // à côté des deux autres. ADS_ENABLED = false est une constante de compilation : R8 supprime
        // tout le code d'affichage des pubs (bannière + interstitiel) dans cet APK.
        create("noads") {
            dimension = "edition"
            applicationIdSuffix = ".sp"
            versionNameSuffix = "-sp"
            buildConfigField("boolean", "IS_TEST", "false")
            buildConfigField("boolean", "ADS_ENABLED", "false")
            // V2SP : tous les verrous levés (jeux, thèmes, options débloqués) pour les tests.
            buildConfigField("boolean", "UNLOCK_ALL", "true")
            manifestPlaceholders["appLabel"] = "Ma Collection V2SP"
        }
    }

    signingConfigs {
        // Signature de production (Play Store) : clé dans un keystore hors du dépôt, chemin et
        // mots de passe lus depuis local.properties (jamais versionné). Volontairement absent
        // tant que ces 3 propriétés ne sont pas renseignées : les builds locaux/debug restent
        // possibles sans keystore, seul `bundleFullRelease`/`assembleFullRelease` en a besoin.
        val releaseStorePath = localProps.getProperty("releaseKeystorePath")
        if (!releaseStorePath.isNullOrBlank()) {
            create("release") {
                storeFile = file(releaseStorePath)
                storePassword = localProps.getProperty("releaseKeystorePassword")
                keyAlias = localProps.getProperty("releaseKeyAlias")
                keyPassword = localProps.getProperty("releaseKeystorePassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
        // L'appli est distribuée aux testeurs en build debug (gradlew assembleDebug) : il faut
        // donc obfusquer aussi ce variant, sinon la release obfusquée ne sert à rien en pratique.
        // R8 désactive le renommage sur un build "debuggable" (avertissement Gradle) : on doit
        // explicitement repasser isDebuggable à false pour que l'obfuscation s'applique vraiment.
        debug {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Scan par photo : code-barres (caméra + image) et OCR (lecture du titre).
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    // Modèle dédié pour le japonais (kanji/hiragana/katakana) : le modèle par défaut
    // ci-dessus ne lit que les caractères latins, donc rien de lisible sur les boîtes
    // de jeux japonaises sans ce modèle spécifique.
    implementation("com.google.mlkit:text-recognition-japanese:16.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Catalogue de jeux en ligne (RAWG) + images de jaquettes.
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Import automatique via URL source : téléchargement (OkHttp) + extraction des métadonnées
    // OpenGraph/HTML (jsoup) de la page cible pour pré-remplir une fiche.
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jsoup:jsoup:1.17.2")

    // Recadrage de photo après sélection/prise (boîte, jaquette, console...).
    implementation("com.github.CanHub:Android-Image-Cropper:4.5.0")

    // Gamification (V2) : pubs (bannière/interstitiel/récompensée) en mode TEST uniquement,
    // et achat Premium via Google Play Billing (scaffold non fonctionnel tant que l'appli
    // n'est pas publiée sur le Play Console avec un produit in-app configuré).
    implementation("com.google.android.gms:play-services-ads:23.3.0")
    // Artefact de base (pas -ktx) : évite un conflit de version de métadonnées Kotlin, les
    // extensions coroutines de billing-ktx ne sont pas utilisées ici (callbacks classiques).
    implementation("com.android.billingclient:billing:8.3.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
