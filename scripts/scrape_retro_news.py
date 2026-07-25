#!/usr/bin/env python3
"""
Agregateur d'actus retrogaming (consoles retro, packs collectors, consoles a venir,
bornes d'arcade type 1up, sorties de jeux, rééditions de consoles) a partir de flux RSS
de sites specialises.

Usage:
    python scrape_retro_news.py [--db retro_news.sqlite3] [--out retro_news.json]
                                 [--recent-days 21]

Contrairement au scraper WCF (E:\\MaCollectionWCF\\scripts\\scrape_wcf_news.py) qui visite
un site unique avec des fiches produit structurees (date de sortie precise dans un champ
dedie), les actus retrogaming proviennent de plusieurs sites heterogenes. On lit donc des
flux RSS (deja structures en titre/lien/date/resume, bien plus robustes a parser que du
HTML site par site) plutot que de scraper le HTML de chaque source.

Aucune donnee n'est inventee : titre, resume, lien source et date de publication
proviennent directement du flux RSS d'origine.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sqlite3
import sys
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from pathlib import Path

import feedparser
import requests

try:
    from deep_translator import GoogleTranslator
except ImportError:
    GoogleTranslator = None  # traduction desactivee si le paquet n'est pas installe

if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
        "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    ),
}

# Flux RSS retenus (verifies actifs manuellement) : rééditions/mini-consoles/Evercade (Time
# Extension), bornes d'arcade dont les modeles home-arcade type 1up (Arcade Heroes, Arcade
# Blogger), actus retrogaming generalistes (RetroDodo, Racketboy, The Retro Gamer, Retrogaming.fr),
# et sites généralistes anglophones (Nintendo Life, Push Square, Pure Xbox, My Nintendo News,
# Gematsu, Kotaku, GamingBolt) ou francophones (JeuxActu, Gamekult, ActuGaming, JeuxVideo.com)
# filtres ensuite par mots-cles retro puisqu'ils couvrent aussi l'actualite non-retro. Chaque
# entree = (nom, url, langue d'origine du flux) ; la langue sert a savoir depuis quelle langue
# traduire (voir translate_entry).
FEEDS = [
    ("Time Extension", "https://timeextension.com/feed", "en"),
    ("Arcade Heroes", "https://arcadeheroes.com/feed", "en"),
    ("Arcade Blogger", "https://arcadeblogger.com/feed/", "en"),
    ("RetroDodo", "https://retrododo.com/feed/", "en"),
    ("Racketboy", "https://www.racketboy.com/feed", "en"),
    ("Nintendo Life", "https://www.nintendolife.com/feeds/latest", "en"),
    ("Push Square", "https://www.pushsquare.com/feeds/latest", "en"),
    ("Pure Xbox", "https://www.purexbox.com/feeds/latest", "en"),
    ("My Nintendo News", "https://www.mynintendonews.com/feed/", "en"),
    ("Gematsu", "https://www.gematsu.com/feed", "en"),
    ("Kotaku", "https://kotaku.com/tag/retro/rss", "en"),
    ("GamingBolt", "https://gamingbolt.com/feed", "en"),
    ("The Retro Gamer", "https://the-retrogamer.com/feed", "fr"),
    ("Retrogaming.fr", "https://retrogaming.fr/feed", "fr"),
    ("JeuxActu", "https://www.jeuxactu.com/rss/news.rss", "fr"),
    ("Gamekult", "https://www.gamekult.com/feed.xml", "fr"),
    ("ActuGaming", "https://www.actugaming.net/feed", "fr"),
    ("JeuxVideo.com", "https://www.jeuxvideo.com/rss/rss.xml", "fr"),
]

# Categorisation par mots-cles (titre + resume), meme esprit que classify_licence() dans le
# scraper WCF -- une simple classification d'affichage (filtre dans l'ecran Actus), pas une
# donnee inventee. Premiere categorie dont un mot-cle correspond l'emporte.
CATEGORY_KEYWORDS = [
    ("ARCADE_CABINET", [
        "arcade1up", "arcade 1up", "1up cabinet", "arcade cabinet", "arcade machine",
        "borne d'arcade", "borne arcade", "home arcade", "cabinet arcade", "pinball machine",
        "pinball cabinet", "bartop",
    ]),
    ("COLLECTOR_PACK", [
        "collector's edition", "collectors edition", "edition collector", "collector edition",
        "limited edition", "edition limitee", "coffret collector", "pack collector",
        "special edition", "steelbook", "collector's set",
    ]),
    ("UPCOMING_CONSOLE", [
        "new console", "nouvelle console", "console announced", "console annoncee",
        "unveiled console", "next console", "console revealed", "console unveiled",
        "kickstarter console",
    ]),
    ("REISSUE", [
        "mini console", "reissue", "reedition", "réédition", "relaunch", "remaster console",
        "anniversary edition", "throwback", "classic edition", "revival", "clone console",
        "plug and play", "flashback",
    ]),
    ("GAME_RELEASE", [
        "release date", "date de sortie", "out now", "sortie le", "coming soon", "launches on",
        "sortira le", "release window", "launch date", "confirmed for", "announced for",
        "coming to switch", "coming to ps5", "coming to pc", "physical release", "pre-order",
        "preorder",
    ]),
    ("RETRO_CONSOLE", [
        "retro console", "console retro", "console rétro", "handheld retro", "fpga",
        "evercade", "analogue", "retron", "mister fpga", "retroarch", "raspberry pi console",
    ]),
]

# Detection best-effort d'une periode future explicitement mentionnee dans le titre/resume
# (mois + annee, ou trimestre + annee, ou simple annee). Si aucune mention trouvee, on retombe
# sur une fenetre de recence (voir --recent-days) : un article qui vient de tomber parle
# presque toujours d'un evenement a venir ou tout juste sorti.
_MONTHS = {
    "january": 1, "janvier": 1, "february": 2, "fevrier": 2, "février": 2, "march": 3, "mars": 3,
    "april": 4, "avril": 4, "may": 5, "mai": 5, "june": 6, "juin": 6, "july": 7, "juillet": 7,
    "august": 8, "aout": 8, "août": 8, "september": 9, "septembre": 9, "october": 10,
    "octobre": 10, "november": 11, "novembre": 11, "december": 12, "decembre": 12,
    "décembre": 12,
}
_MONTH_YEAR_RE = re.compile(
    r"(" + "|".join(_MONTHS.keys()) + r")\s+(20\d{2})", re.IGNORECASE
)
_QUARTER_YEAR_RE = re.compile(r"\bQ([1-4])\s*(20\d{2})\b", re.IGNORECASE)
_YEAR_RE = re.compile(r"\b(20\d{2})\b")


def detect_period(text: str) -> tuple[int, int] | None:
    """(annee, mois) le plus specifique trouve dans `text`, ou None. Le mois vaut 1 si seule
    une annee (ou un trimestre) a ete detecte -- utilise ensuite en borne basse permissive
    (voir is_upcoming)."""
    m = _MONTH_YEAR_RE.search(text)
    if m:
        month = _MONTHS[m.group(1).lower()]
        return int(m.group(2)), month
    q = _QUARTER_YEAR_RE.search(text)
    if q:
        quarter, year = int(q.group(1)), int(q.group(2))
        return year, (quarter - 1) * 3 + 1
    y = _YEAR_RE.search(text)
    if y:
        return int(y.group(1)), 1
    return None


def is_upcoming(text: str, published_at: datetime, now: datetime, recent_days: int) -> bool:
    period = detect_period(text)
    if period is not None:
        year, month = period
        current_ym = now.year * 12 + now.month
        # Annee seule (mois force a 1) : on considere "a venir" des que l'annee n'est pas deja
        # revolue, plutot que d'exiger janvier precisement (ambigu, on prefere sur-inclure).
        if month == 1 and f"{year}" in text and not _MONTH_YEAR_RE.search(text) and not _QUARTER_YEAR_RE.search(text):
            return year >= now.year
        return year * 12 + month >= current_ym
    return (now - published_at) <= timedelta(days=recent_days)


def classify(text: str) -> str:
    lower = text.lower()
    for category, keywords in CATEGORY_KEYWORDS:
        if any(kw in lower for kw in keywords):
            return category
    return "AUTRE"


# Memes 11 langues que MaCollection (retrogaming) et le scraper WCF. "zh" utilise le code
# deep-translator "zh-CN" mais est expose sous la cle "zh" (correspond a
# Locale.getDefault().language sur Android).
ALL_LANGS = [
    ("en", "en"), ("fr", "fr"), ("es", "es"), ("it", "it"), ("de", "de"), ("pt", "pt"),
    ("ru", "ru"), ("el", "el"), ("tr", "tr"), ("ja", "ja"), ("zh", "zh-CN"),
]


def translate_entry(title: str, summary: str, source_lang: str) -> dict:
    """Traduit un titre+resume depuis `source_lang` (langue d'origine du flux, "en" ou "fr") vers
    les 10 autres langues de l'app, best-effort (jamais bloquant : une langue en echec retombe sur
    le texte d'origine plutot que de faire planter le scraper). Un seul appel reseau par langue
    (traduction en lot titre+resume), plutot qu'un appel par champ. Retourne
    {"en": {...}, "fr": {...}, ...} avec la langue source incluse telle quelle (jamais traduite
    vers elle-meme)."""
    original = {"title": title, "summary": summary}
    result = {source_lang: original}
    target_langs = [(k, dt) for k, dt in ALL_LANGS if k != source_lang]
    if GoogleTranslator is None:
        for json_key, _ in target_langs:
            result[json_key] = original
        return result

    texts = [title, summary]
    non_empty_idx = [i for i, t in enumerate(texts) if t]
    non_empty_texts = [texts[i] for i in non_empty_idx]

    for json_key, dt_code in target_langs:
        try:
            translated = (
                GoogleTranslator(source=source_lang, target=dt_code).translate_batch(non_empty_texts)
                if non_empty_texts else []
            )
            full = list(texts)
            for idx, val in zip(non_empty_idx, translated):
                full[idx] = val or texts[idx]
            result[json_key] = {"title": full[0], "summary": full[1]}
        except Exception as exc:
            print(f"  [erreur traduction {json_key}] {title[:40]}... : {exc}", file=sys.stderr)
            result[json_key] = original
    return result


def entry_id(link: str) -> str:
    return hashlib.sha1(link.encode("utf-8")).hexdigest()


def entry_image(entry) -> str:
    media = entry.get("media_content") or entry.get("media_thumbnail")
    if media:
        url = media[0].get("url")
        if url:
            return url
    for link in entry.get("links", []):
        if str(link.get("type", "")).startswith("image/"):
            return link.get("href", "")
    return ""


@dataclass
class NewsItem:
    id: str
    title: str
    summary: str
    category: str
    source_name: str
    source_url: str
    image_url: str
    published_at: str
    scraped_at: str


def fetch_feed(source_name: str, url: str) -> list[NewsItem]:
    try:
        resp = requests.get(url, headers=HEADERS, timeout=20)
        resp.raise_for_status()
    except requests.RequestException as exc:
        print(f"  [erreur reseau] {source_name} ({url}) : {exc}", file=sys.stderr)
        return []

    parsed = feedparser.parse(resp.content)
    items = []
    for entry in parsed.entries:
        link = entry.get("link")
        title = entry.get("title", "").strip()
        if not link or not title:
            continue
        summary = re.sub(r"<[^>]+>", " ", entry.get("summary", "")).strip()
        summary = re.sub(r"\s+", " ", summary)
        published_struct = entry.get("published_parsed") or entry.get("updated_parsed")
        published_at = (
            datetime(*published_struct[:6], tzinfo=timezone.utc)
            if published_struct else datetime.now(timezone.utc)
        )
        items.append(NewsItem(
            id=entry_id(link),
            title=title,
            summary=summary[:500],
            category=classify(f"{title} {summary}"),
            source_name=source_name,
            source_url=link,
            image_url=entry_image(entry),
            published_at=published_at.isoformat(),
            scraped_at=datetime.now(timezone.utc).isoformat(),
        ))
    return items


def init_db(db_path: Path) -> sqlite3.Connection:
    conn = sqlite3.connect(db_path)
    conn.execute(
        """
        CREATE TABLE IF NOT EXISTS retro_news (
            id TEXT PRIMARY KEY,
            title TEXT NOT NULL,
            summary TEXT,
            category TEXT NOT NULL,
            source_name TEXT NOT NULL,
            source_url TEXT NOT NULL,
            image_url TEXT,
            published_at TEXT NOT NULL,
            scraped_at TEXT NOT NULL
        )
        """
    )
    # Migration douce : traductions ajoutees apres coup (voir translate_entry()).
    existing_cols = {row[1] for row in conn.execute("PRAGMA table_info(retro_news)")}
    if "translations_json" not in existing_cols:
        conn.execute("ALTER TABLE retro_news ADD COLUMN translations_json TEXT")
    conn.commit()
    return conn


def item_exists(conn: sqlite3.Connection, item_id: str) -> bool:
    cur = conn.execute("SELECT 1 FROM retro_news WHERE id = ?", (item_id,))
    return cur.fetchone() is not None


def upsert_item(conn: sqlite3.Connection, item: NewsItem, translations_json: str) -> None:
    # INSERT OR REPLACE : contrairement au scraper WCF (fiche produit figee une fois publiee),
    # un article RSS peut voir son resume affine par la source apres publication -- on garde
    # donc la derniere version vue plutot que de figer sur la premiere.
    conn.execute(
        """
        INSERT OR REPLACE INTO retro_news
            (id, title, summary, category, source_name, source_url, image_url,
             published_at, scraped_at, translations_json)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            item.id, item.title, item.summary, item.category, item.source_name,
            item.source_url, item.image_url, item.published_at, item.scraped_at,
            translations_json,
        ),
    )


def export_json(conn: sqlite3.Connection, out_path: Path, recent_days: int) -> int:
    cur = conn.execute(
        """
        SELECT id, title, summary, category, source_name, source_url, image_url,
               published_at, scraped_at, translations_json
        FROM retro_news
        ORDER BY published_at DESC
        """
    )
    rows = cur.fetchall()
    now = datetime.now(timezone.utc)
    data = []
    for r in rows:
        category = r[3]
        if category == "AUTRE":
            # Aucun mot-cle de categorie (console retro/pack collector/console a venir/borne
            # d'arcade/sortie de jeu/reedition) trouve : on exclut plutot que d'afficher du
            # contenu retro generaliste (retrospectives, tests, actus non liees a une sortie).
            continue
        published_at = datetime.fromisoformat(r[7])
        text = f"{r[1]} {r[2]}"
        if not is_upcoming(text, published_at, now, recent_days):
            continue
        data.append({
            "id": r[0],
            "title": r[1],
            "summary": r[2],
            "category": r[3],
            "sourceName": r[4],
            "sourceUrl": r[5],
            "imageUrl": r[6],
            "publishedAt": r[7],
            "scrapedAt": r[8],
            "translations": json.loads(r[9]) if r[9] else {},
        })
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    return len(data)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--db", default="retro_news.sqlite3", type=Path)
    parser.add_argument("--out", default="retro_news.json", type=Path)
    parser.add_argument(
        "--recent-days", default=21, type=int,
        help="fenetre de recence (jours) pour les articles sans date future detectee",
    )
    args = parser.parse_args()

    conn = init_db(args.db)
    total_seen = 0
    translated_count = 0
    for source_name, url, source_lang in FEEDS:
        print(f"[{source_name}] {url}")
        items = fetch_feed(source_name, url)
        for item in items:
            existing = conn.execute(
                "SELECT translations_json FROM retro_news WHERE id = ?", (item.id,)
            ).fetchone()
            if item.category == "AUTRE":
                # Exclue de l'export (voir export_json) : pas la peine de la traduire.
                translations_json = existing[0] if existing else None
            elif existing and existing[0]:
                translations_json = existing[0]
            else:
                translations_json = json.dumps(
                    translate_entry(item.title, item.summary, source_lang), ensure_ascii=False
                )
                translated_count += 1
            upsert_item(conn, item, translations_json)
        conn.commit()
        total_seen += len(items)
        print(f"  {len(items)} entree(s)")
    print(f"{translated_count} entree(s) traduite(s) (nouvelles ou jamais traduites).")

    total_exported = export_json(conn, args.out, args.recent_days)
    conn.close()

    print(f"\nTermine : {total_seen} entree(s) vue(s) au total.")
    print(f"Export JSON : {args.out} ({total_exported} actu(s) a venir)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
