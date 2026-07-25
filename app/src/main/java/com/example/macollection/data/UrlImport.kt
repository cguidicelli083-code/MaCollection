package com.example.macollection.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

/**
 * Informations extraites d'une page web par [UrlImport]. Tous les champs sont facultatifs :
 * l'appelant ne remplit que ceux qui sont vides dans le formulaire (enrichissement, jamais
 * écrasement d'une saisie de l'utilisateur).
 */
data class ImportedInfo(
    val name: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val year: Int? = null
) {
    val isEmpty: Boolean get() = name == null && description == null && imageUrl == null && year == null
}

/**
 * Import automatique via « URL source » : télécharge la page cible et en extrait les
 * métadonnées OpenGraph / balises `<meta>` standard (titre, description, image, année). Couvre
 * Wikipédia, sites de collection, fiches produit, annonces… Best-effort : renvoie null en cas
 * d'échec réseau/parse, sans jamais lever d'exception vers l'UI.
 */
object UrlImport {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    /** Vrai si le texte ressemble à une URL http(s) exploitable. */
    fun looksLikeUrl(s: String): Boolean =
        s.trim().matches(Regex("^https?://\\S+$", RegexOption.IGNORE_CASE))

    suspend fun fetch(url: String): ImportedInfo? = withContext(Dispatchers.IO) {
        val clean = url.trim()
        if (!looksLikeUrl(clean)) return@withContext null
        try {
            val request = Request.Builder()
                .url(clean)
                .header("User-Agent", "Mozilla/5.0 (Android) MaCollection/2.0 (import de fiche)")
                .header("Accept-Language", if (AppPrefs.language == "fr") "fr,en;q=0.8" else "en,fr;q=0.8")
                .build()
            val html = client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                resp.body?.string() ?: return@withContext null
            }
            val doc = Jsoup.parse(html, clean)

            fun meta(vararg selectors: String): String? {
                for (sel in selectors) {
                    val v = doc.selectFirst(sel)?.attr("content")?.trim()
                    if (!v.isNullOrBlank()) return v
                }
                return null
            }

            val rawTitle = meta("meta[property=og:title]", "meta[name=twitter:title]")
                ?: doc.selectFirst("h1")?.text()?.trim()?.takeIf { it.isNotBlank() }
                ?: doc.title().takeIf { it.isNotBlank() }
            val description = meta(
                "meta[property=og:description]",
                "meta[name=twitter:description]",
                "meta[name=description]"
            )?.takeIf { it.isNotBlank() }
            val image = meta(
                "meta[property=og:image:secure_url]",
                "meta[property=og:image]",
                "meta[name=twitter:image]",
                "meta[itemprop=image]",
                "link[rel=image_src]"
            )?.let { it }
            // Année : premier millésime crédible trouvé dans le titre/description.
            val year = Regex("\\b(18[5-9]\\d|19\\d\\d|20[0-4]\\d)\\b")
                .find(listOfNotNull(rawTitle, description).joinToString(" "))
                ?.value?.toIntOrNull()

            ImportedInfo(
                name = cleanTitle(rawTitle),
                description = description,
                imageUrl = image?.takeIf { it.startsWith("http", ignoreCase = true) },
                year = year
            ).takeUnless { it.isEmpty }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Retire le suffixe de site du titre OpenGraph (« Titre - Wikipédia », « Titre | eBay »,
     * « Titre – PriceCharting »…) pour ne garder que le nom du produit.
     */
    private fun cleanTitle(raw: String?): String? {
        val t = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val head = t.split(Regex("\\s[|\\-–—:]\\s")).firstOrNull()?.trim()
        return head?.takeIf { it.length >= 2 } ?: t
    }
}
