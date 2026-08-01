# Règles ProGuard/R8 du projet.
#
# Objectif : obfusquer (renommer) un maximum de classes/méthodes/variables pour rendre la
# décompilation (jadx, apktool...) beaucoup plus pénible à lire et à patcher — notamment la
# logique de l'appli (MainActivity, ScanTools, reconnaissance, ViewModel...).
#
# MAIS certaines classes sont sérialisées/désérialisées par Gson (sauvegarde .zip, réponses
# RAWG/eBay/Wikipédia/taux de change) : Gson les construit par réflexion (sans passer par leur
# constructeur Kotlin), donc R8 ne voit jamais de site de construction "réel" pour elles et peut,
# avec les optimisations agressives (proguard-android-optimize.txt), purement et simplement
# SUPPRIMER leurs champs (propagation de valeurs) malgré une règle -keepclassmembers — c'est
# exactement ce qui a cassé la restauration de sauvegarde. Il faut donc un -keep complet (pas
# seulement -keepclassmembers) sur ces classes précises : leur nom de classe reste visible en
# clair dans l'APK, mais c'est le seul moyen fiable de garder Gson fonctionnel.

-keepattributes Signature
-keepattributes *Annotation*

# Gson TypeToken (utilisé par RetroNewsEntry.localized() pour désérialiser les traductions JSON en
# Map<String, ...>) : R8 peut sinon fusionner/renommer la sous-classe anonyme et perdre sa
# signature générique, faisant planter Gson à l'exécution avec "TypeToken must be created with a
# type argument" (règle officielle recommandée par Gson pour les builds minifiés).
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-keep class com.example.macollection.data.CollectionItem { *; }
-keep class com.example.macollection.data.PriceHistory { *; }
-keep class com.example.macollection.data.ItemPhoto { *; }
-keep class com.example.macollection.data.CustomPreset { *; }
-keep class com.example.macollection.data.PresetPhotoOverride { *; }
-keep class com.example.macollection.data.BackupPayload { *; }

-keep class com.example.macollection.data.RawgSearchResponse { *; }
-keep class com.example.macollection.data.RawgGame { *; }
-keep class com.example.macollection.data.RawgNamed { *; }
-keep class com.example.macollection.data.RawgPlatformWrap { *; }
-keep class com.example.macollection.data.RawgGameDetail { *; }
-keep class com.example.macollection.data.RawgMoviesResponse { *; }
-keep class com.example.macollection.data.RawgMovie { *; }
-keep class com.example.macollection.data.RawgMovieData { *; }

# Gemini (reconnaissance visuelle du scan photo) : requête et réponse sérialisées par Gson.
-keep class com.example.macollection.data.GemPart { *; }
-keep class com.example.macollection.data.GemInlineData { *; }
-keep class com.example.macollection.data.GemContent { *; }
-keep class com.example.macollection.data.GemRequest { *; }
-keep class com.example.macollection.data.GemCandidate { *; }
-keep class com.example.macollection.data.GemResponse { *; }
-keep class com.example.macollection.data.GemProduct { *; }
-keep class com.example.macollection.data.GemBatchEntry { *; }
-keep class com.example.macollection.data.GemTool { *; }
-keep class com.example.macollection.data.GemGoogleSearch { *; }
-keep class com.example.macollection.data.GemPriceEstimate { *; }
# Tavily (recherche web, 3e recours de cote) : requête et réponse sérialisées par Gson.
-keep class com.example.macollection.data.TavilySearchRequest { *; }
-keep class com.example.macollection.data.TavilyResult { *; }
-keep class com.example.macollection.data.TavilySearchResponse { *; }
# Groq (reconnaissance visuelle de secours) : requête et réponse sérialisées par Gson.
-keep class com.example.macollection.data.GqImageUrl { *; }
-keep class com.example.macollection.data.GqContent { *; }
-keep class com.example.macollection.data.GqMessage { *; }
-keep class com.example.macollection.data.GqRequest { *; }
-keep class com.example.macollection.data.GqRespMsg { *; }
-keep class com.example.macollection.data.GqChoice { *; }
-keep class com.example.macollection.data.GqResponse { *; }

# IGDB (recherche de jeux principale) + jeton Twitch associé.
-keep class com.example.macollection.data.TwitchToken { *; }
-keep class com.example.macollection.data.IgdbGame { *; }
-keep class com.example.macollection.data.IgdbNamed { *; }
-keep class com.example.macollection.data.IgdbCover { *; }
-keep class com.example.macollection.data.IgdbInvolved { *; }

-keep class com.example.macollection.data.EbayToken { *; }
-keep class com.example.macollection.data.EbaySearch { *; }
-keep class com.example.macollection.data.EbayItem { *; }
-keep class com.example.macollection.data.EbayPrice { *; }

# UPCitemdb (identification par code-barres, source principale).
-keep class com.example.macollection.data.UpcItem { *; }
-keep class com.example.macollection.data.UpcLookupResponse { *; }

# Barcode Lookup (identification par code-barres, 2e source).
-keep class com.example.macollection.data.BarcodeLookupProduct { *; }
-keep class com.example.macollection.data.BarcodeLookupResponse { *; }

# Barcode Spider (identification par code-barres, 3e source).
-keep class com.example.macollection.data.BarcodeSpiderAttributes { *; }
-keep class com.example.macollection.data.BarcodeSpiderResponse { *; }

# ScanDex (identification par code-barres, source specialisee jeux video).
-keep class com.example.macollection.data.ScanDexPlatform { *; }
-keep class com.example.macollection.data.ScanDexIgdbMetadata { *; }
-keep class com.example.macollection.data.ScanDexResponse { *; }

-keep class com.example.macollection.data.WikiSummary { *; }
-keep class com.example.macollection.data.WikiImage { *; }
-keep class com.example.macollection.data.WikiSearchResponse { *; }
-keep class com.example.macollection.data.WikiSearchQuery { *; }
-keep class com.example.macollection.data.WikiSearchItem { *; }

-keep class com.example.macollection.data.WikiPresetSummary { *; }
-keep class com.example.macollection.data.WikiPresetImage { *; }
-keep class com.example.macollection.data.WikiPresetSearchResponse { *; }
-keep class com.example.macollection.data.WikiPresetSearchQuery { *; }
-keep class com.example.macollection.data.WikiPresetSearchItem { *; }

-keep class com.example.macollection.data.WdSearchEntity { *; }
-keep class com.example.macollection.data.WdSearchResponse { *; }
-keep class com.example.macollection.data.WdSitelink { *; }
-keep class com.example.macollection.data.WdEntity { *; }
-keep class com.example.macollection.data.WdGetResponse { *; }
-keep class com.example.macollection.data.WdSummaryImage { *; }
-keep class com.example.macollection.data.WdSummary { *; }

-keep class com.example.macollection.data.RatesResponse { *; }

# Actus retrogaming (écran Actus de l'Encyclopédie) : JSON désérialisé par Gson.
-keep class com.example.macollection.data.RetroNewsDto { *; }
-keep class com.example.macollection.data.RetroNewsTranslationDto { *; }
-keep class com.example.macollection.ui.NewsTranslation { *; }

# Les enums (ItemType, Region, Condition) sont aussi lus par Gson via leur nom (CollectionItem en
# contient) : on protège leurs constantes de la même façon, par précaution.
-keepclassmembers enum com.example.macollection.data.** { *; }
