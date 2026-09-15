package org.geneivos.app;

import android.webkit.JavascriptInterface;

public class AndroidBridge {

    public interface BridgeListener {
        void onDownloadVideo(String videoId, String title);
    }

    private final BridgeListener listener;

    public AndroidBridge(BridgeListener listener) {
        this.listener = listener;
    }

    @JavascriptInterface
    public void downloadVideo(String videoId, String title) {
        if (listener != null) {
            listener.onDownloadVideo(videoId, title);
        }
    }
}
