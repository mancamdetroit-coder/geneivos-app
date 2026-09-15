package org.geneivos.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView webHome, webVideos, webChat;
    private LinearLayout downloadsScreen, updateBanner, downloadProgressBar;
    private TextView downloadProgressText;
    private ProgressBar downloadProgressIndicator;
    private FrameLayout loadingOverlay;
    private BottomNavigationView bottomNav;
    private RecyclerView downloadsList;
    private LinearLayout emptyDownloads;

    private DownloadManager downloadManager;
    private DownloadsAdapter downloadsAdapter;
    private String pendingApkUrl = null;

    private static final String APP_VERSION = "1.0.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadManager = new DownloadManager(this);

        initViews();
        setupWebViews();
        setupBottomNav();
        setupDownloadsList();
        checkForUpdates();
    }

    private void initViews() {
        webHome              = findViewById(R.id.webHome);
        webVideos            = findViewById(R.id.webVideos);
        webChat              = findViewById(R.id.webChat);
        downloadsScreen      = findViewById(R.id.downloadsScreen);
        updateBanner         = findViewById(R.id.updateBanner);
        downloadProgressBar  = findViewById(R.id.downloadProgressBar);
        downloadProgressText = findViewById(R.id.downloadProgressText);
        downloadProgressIndicator = findViewById(R.id.downloadProgressIndicator);
        bottomNav            = findViewById(R.id.bottomNav);
        downloadsList        = findViewById(R.id.downloadsList);
        emptyDownloads       = findViewById(R.id.emptyDownloads);

        findViewById(R.id.btnUpdate).setOnClickListener(v -> {
            if (pendingApkUrl != null) downloadAndInstallApk(pendingApkUrl);
        });
    }

    private void setupWebView(WebView webView, String url) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.addJavascriptInterface(new AndroidBridge(
            (videoId, title) -> runOnUiThread(() -> startVideoDownload(videoId, title))
        ), "AndroidApp");

        webView.setWebViewClient(new GeneivosWebViewClient(new GeneivosWebViewClient.PageListener() {
            @Override
            public void onPageStarted() {}

            @Override
            public void onPageFinished(String pageUrl) {}

            @Override
            public void onDownloadRequested(String videoId, String title) {
                startVideoDownload(videoId, title);
            }
        }));

        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(url);
    }

    private void setupWebViews() {
        setupWebView(webHome,   Constants.HOME_URL);
        setupWebView(webVideos, Constants.VIDEOS_URL);
        setupWebView(webChat,   Constants.CHAT_URL);
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showTab(webHome);
                return true;
            } else if (id == R.id.nav_videos) {
                showTab(webVideos);
                return true;
            } else if (id == R.id.nav_downloads) {
                showDownloads();
                return true;
            } else if (id == R.id.nav_chat) {
                showTab(webChat);
                return true;
            }
            return false;
        });
    }

    private void showTab(WebView webView) {
        webHome.setVisibility(View.GONE);
        webVideos.setVisibility(View.GONE);
        webChat.setVisibility(View.GONE);
        downloadsScreen.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
    }

    private void showDownloads() {
        webHome.setVisibility(View.GONE);
        webVideos.setVisibility(View.GONE);
        webChat.setVisibility(View.GONE);
        downloadsScreen.setVisibility(View.VISIBLE);
        refreshDownloadsList();
    }

    private void setupDownloadsList() {
        downloadsList.setLayoutManager(new LinearLayoutManager(this));
        refreshDownloadsList();
    }

    private void refreshDownloadsList() {
        List<DownloadedVideo> videos = downloadManager.getDownloads();
        if (videos.isEmpty()) {
            downloadsList.setVisibility(View.GONE);
            emptyDownloads.setVisibility(View.VISIBLE);
        } else {
            downloadsList.setVisibility(View.VISIBLE);
            emptyDownloads.setVisibility(View.GONE);
            downloadsAdapter = new DownloadsAdapter(videos, new DownloadsAdapter.OnItemClickListener() {
                @Override
                public void onPlay(DownloadedVideo video) {
                    playOfflineVideo(video);
                }

                @Override
                public void onDelete(DownloadedVideo video, int position) {
                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Video")
                        .setMessage("Remove \"" + video.title + "\" from offline storage?")
                        .setPositiveButton("Delete", (d, w) -> {
                            downloadManager.deleteDownload(video.id);
                            refreshDownloadsList();
                            Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                }
            });
            downloadsList.setAdapter(downloadsAdapter);
        }
    }

    private void startVideoDownload(String videoId, String title) {
        if (downloadManager.isDownloaded(videoId)) {
            Toast.makeText(this, "Already saved offline", Toast.LENGTH_SHORT).show();
            return;
        }

        String streamUrl = Constants.BASE_URL + "/api/stream.php?id=" + videoId;
        String cookie    = CookieManager.getInstance().getCookie(Constants.BASE_URL);

        downloadProgressBar.setVisibility(View.VISIBLE);
        downloadProgressText.setText("Saving \"" + title + "\"...");
        downloadProgressIndicator.setProgress(0);

        new AsyncTask<Void, Integer, Boolean>() {
            File outputFile;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    outputFile = downloadManager.getVideoFile(videoId, "mp4");
                    URL url = new URL(streamUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    if (cookie != null) conn.setRequestProperty("Cookie", cookie);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);

                    int total = conn.getContentLength();
                    InputStream in = conn.getInputStream();
                    FileOutputStream out = new FileOutputStream(outputFile);

                    byte[] buffer = new byte[8192];
                    int read, downloaded = 0;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        downloaded += read;
                        if (total > 0) {
                            publishProgress((int) ((downloaded * 100L) / total));
                        }
                    }
                    out.close();
                    in.close();
                    return true;
                } catch (Exception e) {
                    if (outputFile != null && outputFile.exists()) outputFile.delete();
                    return false;
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                downloadProgressIndicator.setProgress(values[0]);
                downloadProgressText.setText("Saving \"" + title + "\"... " + values[0] + "%");
            }

            @Override
            protected void onPostExecute(Boolean success) {
                downloadProgressBar.setVisibility(View.GONE);
                if (success && outputFile != null) {
                    DownloadedVideo video = new DownloadedVideo(
                        videoId, title, outputFile.getAbsolutePath(),
                        outputFile.length(), System.currentTimeMillis()
                    );
                    downloadManager.saveDownload(video);
                    Toast.makeText(MainActivity.this,
                        "✅ \"" + title + "\" saved for offline viewing",
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this,
                        "❌ Download failed. Please try again.",
                        Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void playOfflineVideo(DownloadedVideo video) {
        File file = new File(video.filePath);
        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            downloadManager.deleteDownload(video.id);
            refreshDownloadsList();
            return;
        }
        Uri uri = FileProvider.getUriForFile(this, "org.geneivos.app.fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/mp4");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Play with..."));
    }

    private void checkForUpdates() {
        new UpdateChecker(APP_VERSION, new UpdateChecker.UpdateCallback() {
            @Override
            public void onUpdateAvailable(String version, String notes, String apkUrl) {
                runOnUiThread(() -> {
                    pendingApkUrl = apkUrl;
                    updateBanner.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onNoUpdate() {}

            @Override
            public void onError() {}
        }).check();
    }

    private void downloadAndInstallApk(String apkUrl) {
        Toast.makeText(this, "Downloading update...", Toast.LENGTH_SHORT).show();
        updateBanner.setVisibility(View.GONE);

        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... voids) {
                try {
                    File cacheDir = new File(getCacheDir(), "apk");
                    if (!cacheDir.exists()) cacheDir.mkdirs();
                    File apkFile = new File(cacheDir, "geneivos_update.apk");

                    URL url = new URL(apkUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    InputStream in = conn.getInputStream();
                    FileOutputStream out = new FileOutputStream(apkFile);
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
                    out.close();
                    in.close();
                    return apkFile;
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(File apkFile) {
                if (apkFile == null) {
                    Toast.makeText(MainActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri uri = FileProvider.getUriForFile(
                    MainActivity.this, "org.geneivos.app.fileprovider", apkFile);
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setDataAndType(uri, "application/vnd.android.package-archive");
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(install);
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        int selectedId = bottomNav.getSelectedItemId();
        if (selectedId == R.id.nav_home && webHome.canGoBack()) {
            webHome.goBack();
        } else if (selectedId == R.id.nav_videos && webVideos.canGoBack()) {
            webVideos.goBack();
        } else if (selectedId == R.id.nav_chat && webChat.canGoBack()) {
            webChat.goBack();
        } else if (selectedId != R.id.nav_home) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }
}
