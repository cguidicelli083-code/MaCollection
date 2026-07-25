package com.example.macollection.data

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Protection légère contre la modification directe de la base SQLite locale (root + éditeur
 * SQLite générique) : chaque ligne sensible (points, déblocages) embarque une empreinte HMAC
 * calculée à partir de son contenu + une clé interne à l'appli. Si la ligne est modifiée hors de
 * l'appli, l'empreinte ne correspond plus et la ligne est ignorée (traitée comme absente) plutôt
 * que d'être créditée. Dissuasif, pas infaillible : quelqu'un qui décompile l'appli peut retrouver
 * la clé — mais ça bloque la triche "simple" (éditeur SQLite générique, sans reverse engineering).
 */
object IntegrityGuard {
    // Clé interne à l'appli (pas un secret serveur, juste suffisante pour empêcher l'édition
    // directe de la base sans toucher au code compilé).
    private const val SECRET = "MaCollectionV2-7f3a9c2e-1b4d-integrity-key"

    private fun hmac(input: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(SECRET.toByteArray(), "HmacSHA256"))
        return mac.doFinal(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    /** Empreinte pour une ligne [PlayerProgress] (points + points cumulés à vie). */
    fun signProgress(points: Int, lifetimePoints: Int): String = hmac("progress:$points:$lifetimePoints")

    /** Empreinte pour une ligne [UnlockedItem] (id de l'item + date de déblocage). */
    fun signUnlock(itemId: String, unlockedAt: Long): String = hmac("unlock:$itemId:$unlockedAt")
}
