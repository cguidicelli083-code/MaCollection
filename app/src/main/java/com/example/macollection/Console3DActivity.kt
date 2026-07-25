package com.example.macollection

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

/**
 * Écran 3D dédié : WebView plein écran (hors Compose pour que le WebGL se compose).
 * Charge le modèle via le Viewer API Sketchfab + une palette (masquable, couleur libre)
 * pour teinter la console.
 */
class Console3DActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        val uid = Regex("/models/([^/]+)/embed").find(url)?.groupValues?.getOrNull(1)

        val web = WebView(this).apply {
            setBackgroundColor(0xFF0B0B14.toInt())
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                    android.util.Log.d("MC3D", "${m.message()} @${m.sourceId()}:${m.lineNumber()}")
                    return true
                }
            }
            webViewClient = WebViewClient()

            if (uid != null) {
                loadDataWithBaseURL(
                    "https://sketchfab.com/",
                    viewerHtml(uid),
                    "text/html",
                    "UTF-8",
                    null
                )
            } else {
                val sep = if (url.contains("?")) "&" else "?"
                loadUrl("$url${sep}autostart=1&autospin=0.3&ui_theme=dark")
            }
        }
        setContentView(web)
    }

    companion object {
        const val EXTRA_URL = "url"
        const val EXTRA_TITLE = "title"

        private fun viewerHtml(uid: String): String = HTML.replace("__UID__", uid)

        private val HTML = """
<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <script src="https://static.sketchfab.com/api/sketchfab-viewer-1.12.1.js"></script>
  <style>
    html, body { margin: 0; height: 100%; background: #0B0B14; overflow: hidden; font-family: sans-serif; }
    #api-frame { width: 100%; height: 100%; border: 0; display: block; }
    #label { position: fixed; top: 8px; left: 0; right: 0; text-align: center; color: #22E6FF; font-size: 13px; pointer-events: none; }
    #toggle { position: fixed; top: 12px; right: 12px; width: 46px; height: 46px; border-radius: 50%;
              background: rgba(11,11,20,0.88); border: 2px solid #22E6FF; color: #fff; font-size: 20px;
              display: flex; align-items: center; justify-content: center; z-index: 10; }
    #palette { position: fixed; left: 0; right: 0; bottom: 0; display: flex; flex-wrap: wrap; gap: 10px;
               padding: 14px; background: rgba(11,11,20,0.88); justify-content: center; align-items: center; }
    .sw { width: 36px; height: 36px; border-radius: 50%; border: 2px solid rgba(255,255,255,0.35); }
    .sw:active { transform: scale(0.9); }
    #reset { width: 36px; height: 36px; border-radius: 50%; border: 2px solid #fff; color: #fff;
             display: flex; align-items: center; justify-content: center; font-size: 18px; }
    #custom { width: 40px; height: 40px; padding: 0; border: 2px solid #fff; border-radius: 50%;
              background: conic-gradient(red, orange, yellow, lime, aqua, blue, magenta, red); }
    input[type=color]::-webkit-color-swatch-wrapper { padding: 0; }
    input[type=color]::-webkit-color-swatch { border: none; border-radius: 50%; }
  </style>
</head>
<body>
  <iframe id="api-frame" allow="autoplay; fullscreen; xr-spatial-tracking" allowfullscreen></iframe>
  <div id="label">Chargement du modele 3D...</div>
  <div id="toggle">&#127912;</div>
  <div id="palette"></div>
  <script>
    var UID = "__UID__";
    var iframe = document.getElementById('api-frame');
    var client = new Sketchfab(iframe);
    var apiRef = null;
    var materials = [];
    var presets = ['#d91a1a','#1a4fd9','#1aa83c','#e6c81a','#8b2fe6','#e62f8b','#e6731a','#111111','#ededed'];

    function hexToRgb(h) {
      var n = parseInt(h.slice(1), 16);
      return [((n >> 16) & 255) / 255, ((n >> 8) & 255) / 255, (n & 255) / 255];
    }
    function applyColor(rgb) {
      if (!apiRef || !materials.length) return;
      materials.forEach(function (m) {
        var ch = m.channels || {};
        Object.keys(ch).forEach(function (key) {
          var c = ch[key];
          if (c && typeof c === 'object' && c.color !== undefined) {
            c.enable = true;
            c.color = rgb;
            if ('factor' in c) c.factor = 1;
          }
        });
        apiRef.setMaterial(m);
      });
    }
    function buildPalette() {
      var p = document.getElementById('palette');
      var r = document.createElement('div');
      r.id = 'reset'; r.innerHTML = '&#8635;';
      r.onclick = function () { location.reload(); };
      p.appendChild(r);

      var ci = document.createElement('input');
      ci.type = 'color'; ci.id = 'custom'; ci.value = '#22e6ff';
      ci.oninput = function () { applyColor(hexToRgb(this.value)); };
      p.appendChild(ci);

      presets.forEach(function (hex) {
        var b = document.createElement('div');
        b.className = 'sw';
        b.style.background = hex;
        b.onclick = function () { applyColor(hexToRgb(hex)); };
        p.appendChild(b);
      });
    }
    var visible = true;
    document.getElementById('toggle').onclick = function () {
      visible = !visible;
      document.getElementById('palette').style.display = visible ? 'flex' : 'none';
    };
    buildPalette();

    client.init(UID, {
      autostart: 1, ui_theme: 'dark', ui_infos: 0, ui_controls: 1, ui_watermark: 0, dnt: 1,
      success: function (api) {
        apiRef = api;
        api.start();
        api.addEventListener('viewerready', function () {
          document.getElementById('label').textContent = 'Touche une couleur pour teinter la console';
          api.getMaterialList(function (err, mats) { if (!err) { materials = mats; } });
          setTimeout(function () { var l = document.getElementById('label'); if (l) l.style.display = 'none'; }, 4500);
        });
      },
      error: function () { document.getElementById('label').textContent = 'Erreur de chargement du modele'; }
    });
  </script>
</body>
</html>
""".trimIndent()
    }
}
