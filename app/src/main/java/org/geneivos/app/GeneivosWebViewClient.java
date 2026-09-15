package org.geneivos.app;

import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class GeneivosWebViewClient extends WebViewClient {

    public interface PageListener {
        void onPageStarted();
        void onPageFinished(String url);
        void onDownloadRequested(String videoId, String title);
    }

    private final PageListener listener;

    public GeneivosWebViewClient(PageListener listener) {
        this.listener = listener;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (listener != null) listener.onPageStarted();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (listener != null) listener.onPageFinished(url);

        // Inject download buttons into video player pages
        if (url != null && url.contains("/player/")) {
            String videoId = url.substring(url.lastIndexOf("/") + 1);
            injectDownloadButton(view, videoId);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        // Keep all geneivos.org URLs within the app
        if (url.contains("geneivos.org")) {
            return false;
        }
        return true;
    }

    private void injectDownloadButton(WebView view, String videoId) {
        String js = "javascript:(function() {" +
            "var existing = document.getElementById('app-download-btn');" +
            "if (existing) return;" +
            "var btn = document.createElement('button');" +
            "btn.id = 'app-download-btn';" +
            "btn.innerHTML = '⬇️ Save Offline';" +
            "btn.style.cssText = 'position:fixed;bottom:80px;right:16px;" +
            "background:#1A6EBD;color:white;border:none;border-radius:50px;" +
            "padding:10px 20px;font-size:14px;font-weight:bold;z-index:9999;" +
            "box-shadow:0 4px 16px rgba(0,0,0,0.4);cursor:pointer;';" +
            "btn.onclick = function() {" +
            "  window.AndroidApp.downloadVideo('" + videoId + "', document.title);" +
            "};" +
            "document.body.appendChild(btn);" +
            "})();";
        view.loadUrl(js);
    }
}
