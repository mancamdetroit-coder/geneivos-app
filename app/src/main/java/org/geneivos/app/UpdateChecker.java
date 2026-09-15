package org.geneivos.app;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    public interface UpdateCallback {
        void onUpdateAvailable(String version, String notes, String apkUrl);
        void onNoUpdate();
        void onError();
    }

    private final String currentVersion;
    private final UpdateCallback callback;

    public UpdateChecker(String currentVersion, UpdateCallback callback) {
        this.currentVersion = currentVersion;
        this.callback       = callback;
    }

    public void check() {
        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    URL url = new URL(Constants.VERSION_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    return new JSONObject(sb.toString());
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result == null) {
                    callback.onError();
                    return;
                }
                try {
                    String serverVersion = result.getString("version");
                    String notes         = result.optString("notes", "");
                    String apkUrl        = result.optString("apk_url", Constants.APK_DOWNLOAD);

                    if (!serverVersion.equals(currentVersion)) {
                        callback.onUpdateAvailable(serverVersion, notes, apkUrl);
                    } else {
                        callback.onNoUpdate();
                    }
                } catch (Exception e) {
                    callback.onError();
                }
            }
        }.execute();
    }
}
