package com.example.macollection.data

import android.util.Base64
import com.example.macollection.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import kotlin.math.roundToInt

// --- DTO eBay ---
private data class EbayToken(val access_token: String?, val expires_in: Long?)
private data class EbaySearch(val total: Int?, val itemSummaries: List<EbayItem>?)
private data class EbayItem(val title: String?, val price: EbayPrice?, val condition: String?)
private data class EbayPrice(val value: String?, val currency: String?)

private interface EbayApi {
    @FormUrlEncoded
    @POST("identity/v1/oauth2/token")
    suspend fun token(
        @Header("Authorization") basic: String,
        @Field("grant_type") grantType: String,
        @Field("scope") scope: String
    ): EbayToken

    @GET("buy/browse/v1/item_summary/search")
    suspend fun search(
        @Header("Authorization") bearer: String,
        @Header("X-EBAY-C-MARKETPLACE-ID") market: String,
        @Query("q") q: String? = null,
        @Query("gtin") gtin: String? = null,
        @Query("category_ids") categoryIds: String? = null,
        @Query("filter") filter: String? = null,
        @Query("limit") limit: Int = 50
    ): EbaySearch
}

/**
 * Cote « best-effort » d'un objet, basée sur les annonces eBay (marché FR, EUR).
 *
 * ⚠️ LIMITE IMPORTANTE (technique, pas un choix d'implémentation) :
 * eBay expose deux APIs différentes :
 *  - Browse API (`item_summary/search`) : annonces ACTIVES (en cours de vente). C'est la seule
 *    à laquelle ce projet a accès aujourd'hui (clientId/secret "grand public").
 *  - Marketplace Insights API (`item_sales/search`) : VENTES RÉUSSIES (vendu), mais (1) réservée
 *    aux comptes partenaires approuvés par eBay — d'où le ticket en attente mentionné dans le
 *    LISEZMOI, qui peut être refusé pour un usage personnel — et (2) limitée par eBay elle-même
 *    aux 90 derniers jours d'historique, jamais 6 mois, même pour les comptes qui y ont accès.
 *
 * Tant que l'accès Marketplace Insights n'est pas accordé, la cote ci-dessous est donc calculée
 * sur les annonces actives comparables (même catégorie, même état, même profil boîte/notice),
 * ce qui reste la meilleure estimation possible avec les clés actuelles. Le jour où l'accès est
 * accordé, il suffira de remplacer l'appel à `item_summary/search` par `item_sales/search` dans
 * [search] ci-dessus : la structure (filtre catégorie + conditionIds + mots-clés de complétude)
 * reste valable à l'identique.
 */
object EbayPrices {

    data class PriceResult(val priceCents: Int?, val count: Int, val info: String?)

    private const val MARKET = "EBAY_FR"
    private const val SCOPE = "https://api.ebay.com/oauth/api_scope"
    private const val FIXED_PRICE = "buyingOptions:{FIXED_PRICE}"
    private const val CAT_CONSOLES = "139971" // Video Game Consoles
    private const val CAT_GAMES = "139973" // Video Games

    private val primaryClientId: String get() = BuildConfig.EBAY_CLIENT_ID
    private val primaryClientSecret: String get() = BuildConfig.EBAY_CLIENT_SECRET
    private val backupClientId: String get() = BuildConfig.EBAY_CLIENT_ID_BACKUP
    private val backupClientSecret: String get() = BuildConfig.EBAY_CLIENT_SECRET_BACKUP
    private fun hasBackupKey(): Boolean = backupClientId.isNotBlank() && backupClientSecret.isNotBlank()

    private val api: EbayApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.ebay.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EbayApi::class.java)
    }

    private val mutex = Mutex()
    private var cachedToken: String? = null
    private var tokenExpiry: Long = 0

    // Bascule sur la clé de secours quand la clé principale atteint son quota eBay (429, 5000
    // requêtes/jour — cf. developer.ebay.com/develop/get-started/api-call-limits) : mieux vaut
    // continuer avec un second compte que de laisser tomber la cote pour le reste de la journée.
    // Retour automatique sur la clé principale le jour suivant (même principe que le court-circuit
    // quotidien de [GeminiVision]).
    @Volatile private var usingBackupKey = false
    @Volatile private var primaryResumesAt: Long = 0L

    private fun clientId(): String = if (usingBackupKey) backupClientId else primaryClientId
    private fun clientSecret(): String = if (usingBackupKey) backupClientSecret else primaryClientSecret

    private fun maybeResumePrimaryKey() {
        if (usingBackupKey && System.currentTimeMillis() >= primaryResumesAt) {
            usingBackupKey = false
            cachedToken = null
            tokenExpiry = 0
        }
    }

    private fun switchToBackupKey() {
        if (!hasBackupKey() || usingBackupKey) return
        usingBackupKey = true
        cachedToken = null
        tokenExpiry = 0
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        primaryResumesAt = cal.timeInMillis
    }

    fun isConfigured(): Boolean = primaryClientId.isNotBlank() && primaryClientSecret.isNotBlank()

    private suspend fun token(): String? = mutex.withLock {
        maybeResumePrimaryKey()
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiry) return@withLock cachedToken
        val basic = "Basic " + Base64.encodeToString(
            "${clientId()}:${clientSecret()}".toByteArray(), Base64.NO_WRAP
        )
        val t = try {
            api.token(basic, "client_credentials", SCOPE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        cachedToken = t?.access_token
        tokenExpiry = System.currentTimeMillis() + ((t?.expires_in ?: 0L) * 1000) - 60_000
        cachedToken
    }

    /**
     * Exécute [call] (un appel `api.search(...)`) avec le token courant ; si eBay renvoie 429
     * (quota atteint) et qu'une clé de secours est configurée, bascule dessus et réessaie UNE
     * fois avec un nouveau token. Renvoie une liste vide en cas d'échec (jamais d'exception).
     */
    private suspend fun searchOrFallback(call: suspend (bearer: String) -> EbaySearch): EbaySearch? {
        val tok = token() ?: return null
        return try {
            call("Bearer $tok")
        } catch (e: CancellationException) {
            throw e
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 429 && hasBackupKey() && !usingBackupKey) {
                switchToBackupKey()
                val retryTok = token() ?: return null
                try {
                    call("Bearer $retryTok")
                } catch (e2: CancellationException) {
                    throw e2
                } catch (e2: Exception) {
                    null
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cote estimée d'un objet : moyenne (tronquée) des annonces eBay FR comparables.
     *
     * Le profil de comparaison tient compte de :
     *  - [condition] : état déclaré par l'utilisateur (Neuf, Mint, Très bon, Bon, Mauvais, HS) ;
     *  - [hasBox] / [hasManual] : boîte et notice présentes ou non.
     *
     * On ne compare jamais une console loose à une console complète, ni un objet HS à un objet
     * en bon état : ça fausserait complètement la cote.
     */
    suspend fun lookup(
        barcode: String?,
        type: ItemType,
        brand: String,
        name: String,
        region: Region,
        condition: Condition = Condition.BON,
        hasBox: Boolean = true,
        hasManual: Boolean = true,
        platform: String? = null
    ): PriceResult {
        if (!isConfigured()) return PriceResult(null, 0, null)
        if (token() == null) return PriceResult(null, 0, "Connexion eBay impossible")

        // Filtre catégorie pour exclure le matériel hors-sujet (pièces détachées, cartes
        // électroniques d'arcade, accessoires...) qui partage parfois des mots avec la requête
        // texte. Sans ce filtre, une seule annonce mal catégorisée mais textuellement proche
        // (ex. une carte mère d'arcade à 1000 €+ pour une recherche de cartouche) peut fausser
        // complètement la cote si c'est le seul résultat trouvé — IMPORTANT : on ne retire
        // JAMAIS ce filtre dans les tentatives ci-dessous, même en élargissant la recherche.
        val category = when (type) {
            ItemType.CONSOLE -> CAT_CONSOLES
            ItemType.JEU -> CAT_GAMES
            ItemType.ACCESSOIRE -> null
        }
        val filter = "$FIXED_PRICE,conditionIds:{${conditionIdsFor(condition)}}"

        // On NE met plus le profil boîte/notice dans la requête texte eBay : en "ET" avec le
        // reste, ça renvoyait souvent zéro résultat pour un objet peu courant. On récupère un
        // lot d'annonces comparables (marque + nom + catégorie + état), puis on filtre/priorise
        // nous-mêmes celles dont le titre correspond vraiment au profil boîte/notice déclaré.
        var items = emptyList<EbayItem>()
        if (!barcode.isNullOrBlank()) {
            items = searchOrFallback { bearer ->
                api.search(bearer, MARKET, gtin = barcode, categoryIds = category, filter = filter)
            }?.itemSummaries.orEmpty()
        }

        if (items.isEmpty()) {
            // La plateforme (ex. "PlayStation 2") est incluse en priorité : un même titre de jeu
            // existe souvent sur plusieurs consoles avec une cote très différente, et sans elle
            // la requête ne changeait jamais quand on modifiait la plateforme associée dans la
            // fiche. Repli SANS plateforme en dernier recours si la requête qualifiée ne trouve
            // rien (ex. eBay utilise un nom de console différent du nôtre dans le titre).
            val attempts = listOf(
                buildQuery(type, brand, name, platform, condition) to filter,
                buildQuery(type, brand, name, platform, condition = null) to FIXED_PRICE,
                buildQuery(type, brand, name, null, condition = null) to FIXED_PRICE
            )
            for ((q, f) in attempts) {
                if (q.isBlank()) continue
                items = searchOrFallback { bearer ->
                    api.search(bearer, MARKET, q = q, categoryIds = category, filter = f)
                }?.itemSummaries.orEmpty()
                if (items.isNotEmpty()) break
            }
        }

        // Certains vendeurs classent du matériel hors-sujet (cartes électroniques d'arcade,
        // pièces détachées de réparation...) dans la catégorie « Jeux vidéo » malgré tout — le
        // filtre de catégorie seul ne suffit pas, on exclut aussi ces titres explicitement.
        items = items.filterNot { isLikelyHardwarePart(it.title.orEmpty()) }

        // Priorité aux annonces dont le titre confirme vraiment le profil boîte/notice déclaré ;
        // si aucune ne correspond explicitement, on retombe sur tout le lot (mieux qu'aucun prix,
        // mais on le précise dans le message).
        val matching = items.filter { matchesCompleteness(it.title.orEmpty(), hasBox, hasManual) }
        val approximate = matching.isEmpty() && items.isNotEmpty()
        val kept0 = matching.ifEmpty { items }

        val prices = kept0
            .mapNotNull { it.price }
            .filter { it.currency == "EUR" }
            .mapNotNull { it.value?.toDoubleOrNull() }
            .filter { it > 0 }
            .sorted()

        val profile = profileLabel(condition, hasBox, hasManual)
        if (prices.isEmpty()) {
            return PriceResult(null, 0, "Aucune annonce eBay trouvée pour ce profil ($profile)")
        }

        // eBay ne donne que des annonces EN COURS (prix demandés) : certains vendeurs affichent
        // un prix « aspirationnel » bien au-dessus du marché et ne vendent jamais à ce prix-là.
        // Une moyenne classique se laisse complètement tirer vers le haut par ces annonces.
        // On élimine d'abord les valeurs aberrantes (méthode IQR, plus stricte vers le haut que
        // vers le bas car c'est là que se concentrent les annonces surcotées), puis on prend la
        // MÉDIANE du reste plutôt que la moyenne : bien plus robuste face aux valeurs extrêmes.
        val filtered = if (prices.size >= 4) {
            val q1 = percentile(prices, 0.25)
            val q3 = percentile(prices, 0.75)
            val iqr = q3 - q1
            prices.filter { it in (q1 - 1.5 * iqr)..(q3 + 1.0 * iqr) }.ifEmpty { prices }
        } else prices
        // La médiane seule reste un prix DEMANDÉ, pas un prix de VENTE — et le marché du rétro
        // a une tendance bien connue à afficher des prix demandés sensiblement au-dessus de ce
        // qui se vend vraiment. Sans accès aux ventes réelles (Marketplace Insights), on vise
        // le 35e centile plutôt que le 50e (médiane) pour corriger ce biais systématique vers
        // le haut, surtout sensible avec peu d'annonces disponibles (objets rares).
        val estimate = percentile(filtered, 0.35)
        val approxNote = if (approximate) " (profil boîte/notice approximatif : aucune annonce ne le confirmait explicitement)" else ""
        val rangeNote = if (filtered.size > 1) " (de ${formatEuro(filtered.first())} à ${formatEuro(filtered.last())})" else ""

        return PriceResult(
            priceCents = (estimate * 100).roundToInt(),
            count = prices.size,
            info = "Médiane de ${prices.size} annonce(s) eBay FR en cours$rangeNote — $profile$approxNote " +
                "(ventes réussies indisponibles : accès Marketplace Insights non accordé par eBay)"
        )
    }

    private fun formatEuro(value: Double): String = "${value.roundToInt()} €"

    /** Valeur au rang [p] (0.0–1.0) d'une liste déjà triée, avec interpolation linéaire. */
    private fun percentile(sorted: List<Double>, p: Double): Double {
        if (sorted.size == 1) return sorted[0]
        val idx = p * (sorted.size - 1)
        val lower = idx.toInt()
        val upper = minOf(lower + 1, sorted.size - 1)
        val frac = idx - lower
        return sorted[lower] + (sorted[upper] - sorted[lower]) * frac
    }

    /**
     * Titre de la première annonce eBay correspondant à ce code-barres (EAN/UPC), ou null.
     * Utilisé pour identifier un objet scanné par code-barres (eBay n'expose pas de base
     * "produit" séparée, mais une annonce qui matche le gtin donne quasiment toujours le bon
     * nom de jeu/console dans son titre).
     */
    suspend fun titleForBarcode(barcode: String): String? {
        if (!isConfigured() || barcode.isBlank()) return null
        return searchOrFallback { bearer ->
            api.search(bearer = bearer, market = MARKET, gtin = barcode)
        }?.itemSummaries?.firstOrNull()?.title
    }

    /**
     * Vrai si le titre ressemble à une carte électronique, une pièce détachée ou du matériel
     * de réparation plutôt qu'à un jeu/console complet — certains vendeurs classent ça dans
     * « Jeux vidéo » malgré tout (ex. une carte mère d'arcade CPS-1 au prix d'une pièce rare,
     * qui n'a rien à voir avec la cartouche grand public du même jeu).
     */
    private fun isLikelyHardwarePart(title: String): Boolean {
        val t = title.lowercase()
        return listOf(
            "carte mere", "carte mère", "carte de sub", "motherboard", "mainboard", "pcb",
            "circuit board", "carte electronique", "carte électronique", "jamma", "cps-1", "cps-2", "cps-3",
            "arcade board", "carte arcade"
        ).any { t.contains(it) }
    }

    /**
     * Vrai si le titre de l'annonce confirme (par mots-clés du vocabulaire retrogaming)
     * le même profil boîte/notice que celui déclaré par l'utilisateur.
     */
    private fun matchesCompleteness(title: String, hasBox: Boolean, hasManual: Boolean): Boolean {
        val t = title.lowercase()
        val looseHit = listOf("loose", "sans boite", "sans boîte", "no box", "cartridge only", "console only").any { t.contains(it) }
        val completeHit = listOf("cib", "complet", "complete", "boxed", "boite", "boîte", "notice", "manual").any { t.contains(it) }
        return when {
            hasBox && hasManual -> completeHit && !looseHit
            hasBox || hasManual -> !looseHit
            else -> looseHit || !completeHit
        }
    }

    /** Identifiant(s) d'état eBay (champ structuré conditionIds) les plus proches de notre état. */
    private fun conditionIdsFor(condition: Condition): String = when (condition) {
        Condition.NEUF -> "1000"   // New
        Condition.HS -> "7000"     // For parts or not working
        // eBay n'a pas de granularité plus fine pour le reculé/occasion : Mint, Très bon, Bon
        // et Mauvais sont tous de l'« Used » côté eBay ; on affine via les mots-clés de la requête.
        else -> "3000"             // Used
    }

    private fun profileLabel(condition: Condition, hasBox: Boolean, hasManual: Boolean): String {
        val completeness = when {
            hasBox && hasManual -> "complet (boîte + notice)"
            hasBox -> "boîte sans notice"
            else -> "loose, sans boîte"
        }
        return "état ${condition.label}, $completeness"
    }

    /**
     * Construit la requête mots-clés. [condition] et [completeness] sont optionnels :
     * les omettre (null) permet d'élargir la recherche quand la version stricte ne
     * trouve rien (voir [lookup]).
     */
    private fun buildQuery(
        type: ItemType,
        brand: String,
        name: String,
        platform: String?,
        condition: Condition?
    ): String {
        // Évite de dupliquer la marque (ex. "Nintendo Nintendo 3DS").
        val base = if (brand.isBlank() || name.lowercase().contains(brand.lowercase())) {
            name
        } else {
            "$brand $name"
        }
        // Idem pour la plateforme : pas la répéter si le nom la mentionne déjà (ex. "Zelda Switch").
        val platformSuffix = if (!platform.isNullOrBlank() && !base.lowercase().contains(platform.lowercase())) " $platform" else ""
        val typeSuffix = if (type == ItemType.CONSOLE) " console" else ""
        // "HS" est un mot-clé très utilisé par les vendeurs pour les objets en panne / pour pièces.
        val hsSuffix = if (condition == Condition.HS) " HS pour pieces" else ""
        return "$base$platformSuffix$typeSuffix$hsSuffix".trim()
    }
}
