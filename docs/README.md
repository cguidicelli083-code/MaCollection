# 🕹️ Ma Collection

**Application Android pour cataloguer, estimer et faire vivre votre collection de consoles, jeux et accessoires rétro.**

Ma Collection transforme votre téléphone en inventaire complet de votre collection rétro-gaming, du salon à la poche.

## ✨ Fonctionnalités

- **📸 Ajout par photo ou code-barres** — une IA visuelle identifie une jaquette, une boîte ou une console à partir d'une simple photo et pré-remplit la fiche (nom, éditeur, année, jaquette).
- **🗂️ Collection & liste de souhaits** — consoles, jeux et accessoires classés par type, marque, état et région, avec photos, galerie et notes.
- **💶 Cote des prix** — estimation de la valeur des objets à partir d'annonces réelles, pour suivre la cote de la collection dans le temps.
- **📚 Encyclopédie rétro** — fiches détaillées de centaines de consoles (specs, année, fabricant) et catalogue de jeux en ligne.
- **🏆 Gamification** — points, thèmes visuels à débloquer, une dizaine de mini-jeux rétro et un quiz progressif à 10 niveaux.
- **☁️ Sauvegarde & multi-langues** — export de la collection, sauvegarde des points liée au compte Google, interface en 11 langues.

## 🔌 Technologies & sources de données

| Service | Rôle |
|---|---|
| **IGDB** (via **Twitch**) | Base de données de jeux, source principale de la recherche |
| **RAWG** | Catalogue de jeux complémentaire (jaquettes, genres, descriptions) |
| **eBay** | Cote des prix à partir d'annonces |
| **Google Gemini** | Reconnaissance visuelle des objets sur les photos |
| **Wikipédia** | Descriptions et visuels d'appoint |

Construite en **Kotlin / Jetpack Compose**, base locale **Room**, sauvegarde via **Android Auto Backup**.

## 🔒 Confidentialité

L'application n'a aucun serveur propre et ne crée aucun compte : les données restent sur l'appareil.
Voir la [politique de confidentialité](https://VOTRE-UTILISATEUR.github.io/VOTRE-DEPOT/).

## ✉️ Contact

c.guidicelli083@gmail.com

---

*Projet indépendant. Les noms de consoles et de jeux cités le sont à titre encyclopédique ; les marques appartiennent à leurs propriétaires respectifs.*
