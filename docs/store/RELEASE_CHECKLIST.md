# Checklist mise en ligne Play Store — Ma Collection V2

## ✅ Fait automatiquement (2026-08-04)
- [x] Traduction complète de l'app en 11 langues (fr, en, es, it, de, pt, ru, el, tr, ja, zh) — sélecteur de langue dans Réglages.
- [x] Keystore de release déjà généré et configuré : `C:\Users\Nawash\AndroidKeystores\macollectionv2-release.jks`, référencé dans `local.properties` (gitignore, jamais commit).
- [x] AdMob déjà en IDs RÉELS (pas des IDs de test) : `AndroidManifest.xml` (APPLICATION_ID) et `AdsManager.kt` (bannière/interstitiel/récompensée) — contrairement à MaCollection WCF, rien à remplacer ici.
- [x] R8/ProGuard actif sur le build release.
- [x] Build de release testé : `./gradlew bundleFullRelease` réussi, `app/build/outputs/bundle/fullRelease/app-full-release.aab` généré et signé avec succès (39 Mo).
- [x] Politique de confidentialité rédigée (fr + en) : `docs/privacy-policy.html`.
- [x] Fiche Store (titre, descriptions courte/longue fr+en) rédigée : `docs/store/fiche-play-store.md`.
- [x] Premium (2 abonnements + achat à vie, argent réel) déjà codé côté app : Google Play Billing (`BillingManager.kt`), scaffold fonctionnel mais INACTIF tant que les produits ne sont pas créés côté Play Console (affiche "Bientôt disponible").
- [x] Nouveau tutoriel de premier lancement, plus visuel et concis (voir section dédiée plus bas) — testé uniquement sur l'édition `restricted` (V2test) pour l'instant, PAS encore répercuté sur `full` (production) ni `noads` (V2SP).

## ⚠️ À FAIRE PAR TOI avant de publier (nécessite ton compte / des captures d'écran)

0. **Play Console — 3 produits Premium** (bloquant pour que le Paywall fonctionne) :
   - Play Console → ton app → Monétiser → Produits.
   - **Abonnements** → créer `sub_monthly` et `sub_yearly` (IDs EXACTS, sensibles à la casse).
   - **Produits gérés (achats uniques)** → créer `inapp_lifetime`.
   - Fixe les prix de ton choix. Tant qu'ils ne sont pas créés et actifs, le Paywall affiche "Bientôt disponible" (comportement normal et voulu, pas un bug).
   - **Rappel offre de lancement** : si tu veux une réduction limitée dans le temps sur `sub_yearly` (ex. 9,99 € le premier mois puis retour à 14,99 €), configure la fenêtre d'offre Play Console AVANT la publication du build de production officiel — pas sur un simple push en test fermé.

1. **Consentement RGPD (UMP) — recommandé avant tout, PAS encore implémenté** : contrairement à MaCollection WCF, ce projet n'a aucun flux de consentement Google User Messaging Platform pour les utilisateurs de l'Union européenne. AdMob impose ce recueil de consentement pour les pubs personnalisées en UE — à ajouter avant une publication grand public en Europe (dis-moi si tu veux que je l'implémente, ça se fait au même endroit que le tutoriel de premier lancement).

2. **Compte développeur Google Play** (25 $ one-shot si pas déjà fait) : https://play.google.com/console/signup

3. **Captures d'écran** (2 minimum, jusqu'à 8 recommandé, format téléphone) : Collection, Encyclopédie, fiche détail d'un jeu, onglet Jeux, sélecteur de langue... Ton téléphone est déjà connecté en USB à cette session — dis-le-moi si tu veux qu'on les prenne ensemble maintenant.

4. **Icône 512×512 et bannière 1024×500** : pas encore générées côté `docs/store/` pour ce projet (contrairement à WCF) — à préparer avant publication.

5. **Activer GitHub Pages** pour héberger la politique de confidentialité (si pas déjà fait pour ce dépôt — `index.html` semble déjà servi, donc peut-être déjà actif) :
   `github.com/cguidicelli083-code/MaCollection → Settings → Pages → Source: branch "main", dossier "/docs"`.
   L'URL sera alors `https://cguidicelli083-code.github.io/MaCollection/privacy-policy.html`.

6. **Dans Play Console** (nouvelle app) :
   - Nom, description courte/longue, icône, bannière → copier depuis `docs/store/fiche-play-store.md` et `docs/store/*.png` (une fois générés).
   - Coller l'URL de la politique de confidentialité (étape 5).
   - Questionnaire de classification du contenu (aucun contenu sensible dans l'app elle-même).
   - Section "Sécurité des données" (Data safety) : déclarer les données collectées — voir `docs/privacy-policy.html` pour la liste exacte (photos envoyées à Gemini/Groq pour reconnaissance, codes-barres envoyés à UPCitemdb/Barcode Lookup/Barcode Spider/ScanDex, requêtes de recherche envoyées à IGDB/RAWG/eBay/Tavily/Wikipédia, IDs publicitaires via AdMob). Cocher aussi "Contient des achats intégrés" (voir section dédiée dans `fiche-play-store.md`).
   - Uploader le bundle de release (`app/build/outputs/bundle/fullRelease/app-full-release.aab`) dans une release (commencer par un test interne/fermé est recommandé avant production).

7. **Après publication** : pense à sauvegarder le keystore (`macollectionv2-release.jks`) et son mot de passe (dans `local.properties`) ailleurs que sur ce PC (gestionnaire de mots de passe, cloud chiffré...) — leur perte rendrait impossible toute future mise à jour de l'app.

## 🎓 Tutoriel de premier lancement — refonte en cours

L'ancien tutoriel (14 pages de texte, trop dense d'après ton retour) est remplacé par une version plus courte et visuelle, guidée pas à pas. Détails et statut : voir le message de la session, ou demande-moi un résumé.
