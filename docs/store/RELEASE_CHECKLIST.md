# Checklist mise en ligne Play Store — Ma Collection V2

## ✅ Fait automatiquement (mise à jour 2026-08-29 — version 2.12 (16))
- [x] Encyclopédie : le mode "Jeux" autonome est remplacé par une liste de jeux enrichie
      (cache local synchronisé, tri) directement dans la fiche de chaque console.
- [x] Offres eBay cliquables (ouverture de l'annonce dans le navigateur), boutons de recherche
      directs vers LeBonCoin et Vinted pour jeux/consoles/accessoires.
- [x] Correctif recherche jeu : la requête n'utilise plus l'éditeur comme "marque" parasite.
- [x] Correctif offres eBay : filtrage par plateforme (une fiche 32X n'affiche plus d'annonces
      d'une autre console).
- [x] 11 thèmes clairs aux couleurs des drapeaux des langues de l'app (France + Allemagne, Chine,
      Espagne, Grèce, Italie, Japon, Portugal, Royaume-Uni, Russie, Turquie), gratuits dès
      l'installation, avec adaptation du chrome de l'app au thème clair/sombre.
- [x] Estimation d'un lot de plusieurs objets à partir d'une seule photo : prix par objet estimé,
      total dynamique selon les objets cochés, tout cocher/décocher, partage du récapitulatif,
      ajout direct à la collection ou aux souhaits — accessible depuis "Ajouter un objet" et
      depuis "Estimation rapide".
- [x] Nouvelles photos locales pour plusieurs consoles/accessoires (remplacent des URLs distantes,
      plus fiable hors-ligne).
- [x] Consentement RGPD (Google UMP) pour les pubs personnalisées en UE — `ui/ads/AdsManager.requestConsentAndInitAds`,
      appelé depuis `MainActivity.onCreate` (nécessite une Activity, l'init des pubs a donc été
      déplacée hors de `MaCollectionApp`). Même modèle que MaCollection WCF. Build de release
      reconstruit avec ce changement.

## ✅ Fait automatiquement (mise à jour 2026-08-07 — version 2.10 (14))
- [x] Traduction complète de l'app en 11 langues (fr, en, es, it, de, pt, ru, el, tr, ja, zh) — sélecteur de langue dans Réglages.
- [x] Keystore de release déjà généré et configuré : `C:\Users\Nawash\AndroidKeystores\macollectionv2-release.jks`, référencé dans `local.properties` (gitignore, jamais commit).
- [x] AdMob déjà en IDs RÉELS (pas des IDs de test) : `AndroidManifest.xml` (APPLICATION_ID) et `AdsManager.kt` (bannière/interstitiel/récompensée) — contrairement à MaCollection WCF, rien à remplacer ici.
- [x] R8/ProGuard actif sur le build release.
- [x] Build de release testé : `./gradlew bundleFullRelease` réussi, `app/build/outputs/bundle/fullRelease/app-full-release.aab` généré et signé avec succès (version 2.10, code 14, ~39 Mo).
- [x] Politique de confidentialité rédigée (fr + en) : `docs/privacy-policy.html`.
- [x] Fiche Store (titre, descriptions courte/longue fr+en) rédigée : `docs/store/fiche-play-store.md`.
- [x] Premium (2 abonnements + achat à vie, argent réel) déjà codé côté app : Google Play Billing (`BillingManager.kt`), scaffold fonctionnel mais INACTIF tant que les produits ne sont pas créés côté Play Console (affiche "Bientôt disponible").
- [x] Nouveau tutoriel de premier lancement, plus visuel et concis (voir section dédiée plus bas) — déployé sur les 3 éditions (`full`, `restricted`, `noads`), l'ancien tutoriel 14 pages n'est plus utilisé nulle part.
- [x] Recherche en ligne résiliente (ajout/édition manuelle) : cascade automatique essai exact → titre nettoyé (retrait des mentions "Édition Collector", "Import"...) → traduction anglaise, avec bouton "Réessayer" et aide contextuelle si tout échoue. Voir `data/SearchCascade.kt`.
- [x] Souhaits : sélection multiple avec suppression groupée et transfert groupé vers la Collection (respecte le plafond gratuit/TEST) — remplace l'ancien bouton "Rechercher les jaquettes manquantes" (un par un).
- [x] Annulation d'un scan en cours (code-barres, photo, lot) via un bouton dédié pendant le chargement.

## ⚠️ À FAIRE PAR TOI avant de publier (nécessite ton compte / des captures d'écran)

0. **Play Console — 3 produits Premium** (bloquant pour que le Paywall fonctionne) :
   - Play Console → ton app → Monétiser → Produits.
   - **Abonnements** → créer `sub_monthly` et `sub_yearly` (IDs EXACTS, sensibles à la casse).
   - **Produits gérés (achats uniques)** → créer `inapp_lifetime`.
   - Fixe les prix de ton choix. Tant qu'ils ne sont pas créés et actifs, le Paywall affiche "Bientôt disponible" (comportement normal et voulu, pas un bug).
   - **Rappel offre de lancement** : si tu veux une réduction limitée dans le temps sur `sub_yearly` (ex. 9,99 € le premier mois puis retour à 14,99 €), configure la fenêtre d'offre Play Console AVANT la publication du build de production officiel — pas sur un simple push en test fermé.

1. **Compte développeur Google Play** (25 $ one-shot si pas déjà fait) : https://play.google.com/console/signup

2. **Captures d'écran** : déjà prises, voir `docs/store/screenshots/` (6 images). Rajoutes-en si tu veux couvrir d'autres écrans (jusqu'à 8 recommandé).

3. **Icône 512×512 et bannière 1024×500** : pas encore générées côté `docs/store/` pour ce projet (contrairement à WCF) — à préparer avant publication.

4. **Activer GitHub Pages** pour héberger la politique de confidentialité (si pas déjà fait pour ce dépôt — `index.html` semble déjà servi, donc peut-être déjà actif) :
   `github.com/cguidicelli083-code/MaCollection → Settings → Pages → Source: branch "main", dossier "/docs"`.
   L'URL sera alors `https://cguidicelli083-code.github.io/MaCollection/privacy-policy.html`.

5. **Dans Play Console** (nouvelle app) :
   - Nom, description courte/longue, icône, bannière → copier depuis `docs/store/fiche-play-store.md` et `docs/store/*.png` (une fois générés).
   - Coller l'URL de la politique de confidentialité (étape 4).
   - Questionnaire de classification du contenu (aucun contenu sensible dans l'app elle-même).
   - Section "Sécurité des données" (Data safety) : déclarer les données collectées — voir `docs/privacy-policy.html` pour la liste exacte (photos envoyées à Gemini/Groq pour reconnaissance, codes-barres envoyés à UPCitemdb/Barcode Lookup/Barcode Spider/ScanDex, requêtes de recherche envoyées à IGDB/RAWG/eBay/Tavily/Wikipédia, IDs publicitaires via AdMob). Cocher aussi "Contient des achats intégrés" (voir section dédiée dans `fiche-play-store.md`).
   - Uploader le bundle de release (`app/build/outputs/bundle/fullRelease/app-full-release.aab`) dans une release (commencer par un test interne/fermé est recommandé avant production).

6. **Après publication** : pense à sauvegarder le keystore (`macollectionv2-release.jks`) et son mot de passe (dans `local.properties`) ailleurs que sur ce PC (gestionnaire de mots de passe, cloud chiffré...) — leur perte rendrait impossible toute future mise à jour de l'app.

## 🎓 Tutoriel de premier lancement — terminé

L'ancien tutoriel (14 pages de texte, trop dense d'après ton retour) est remplacé par `OnboardingScreenLight` : une version courte (7 étapes) et visuelle, guidée pas à pas, avec un premier écran de choix de langue pour que la suite s'affiche déjà traduite. Déployé sur les 3 éditions.

## 📝 Notes de version 2.12 (16) — à coller dans Play Console → Notes de version

Français :
```
📸 Estimation d'un lot de plusieurs objets en une seule photo : prix par objet, total qui se met à jour selon ce que tu coches, ajout direct à ta collection ou tes souhaits.
🎮 L'Encyclopédie affiche maintenant les jeux directement dans la fiche de chaque console (tri, mise à jour automatique).
🔗 Les offres eBay s'ouvrent d'un tap, avec des liens de recherche directs vers LeBonCoin et Vinted.
🎨 11 thèmes clairs aux couleurs des drapeaux (France, Allemagne, Chine, Espagne, Grèce, Italie, Japon, Portugal, Royaume-Uni, Russie, Turquie), gratuits.
Correctifs et améliorations de stabilité.
```

English :
```
📸 Estimate a lot of several items from a single photo: price per item, running total based on what you check, add straight to your collection or wishlist.
🎮 The Encyclopedia now shows games directly on each console's page (sorting, automatic sync).
🔗 eBay listings open with a single tap, plus direct search links to LeBonCoin and Vinted.
🎨 11 free light themes in the colors of the app's language flags (France, Germany, China, Spain, Greece, Italy, Japan, Portugal, UK, Russia, Turkey).
Fixes and stability improvements.
```

## 📝 Notes de version 2.10 (14) — archivées

Français :
```
🔎 Recherche en ligne plus fiable (jeux, consoles, accessoires) : nettoyage automatique du titre et traduction si besoin, avec un bouton Réessayer en cas d'échec.
❤️ Souhaits : sélection multiple pour supprimer ou transférer plusieurs objets vers ta collection en un geste.
⏹️ Possibilité d'annuler un scan en cours.
Correctifs et améliorations de stabilité.
```

English :
```
🔎 More reliable online search (games, consoles, accessories): automatic title cleanup and translation fallback, with a Retry button if nothing is found.
❤️ Wishlist: multi-select to delete or transfer several items to your collection at once.
⏹️ You can now cancel an in-progress scan.
Fixes and stability improvements.
```
