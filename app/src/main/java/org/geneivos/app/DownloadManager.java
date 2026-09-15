package org.geneivos.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {

    private static final String PREF_DOWNLOADS = "downloads_list";
    private final Context context;
    private final SharedPreferences prefs;

    public DownloadManager(Context context) {
        this.context = context;
        this.prefs   = context.getSharedPreferences("GeneivosDownloads", Context.MODE_PRIVATE);
    }

    public File getDownloadsDir() {
        File dir = new File(context.getFilesDir(), Constants.DOWNLOADS_DIR);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public File getVideoFile(String videoId, String ext) {
        return new File(getDownloadsDir(), videoId + "." + ext);
    }

    public void saveDownload(DownloadedVideo video) {
        List<DownloadedVideo> list = getDownloads();
        // Remove existing with same id
        list.removeIf(v -> v.id.equals(video.id));
        list.add(video);
        saveList(list);
    }

    public void deleteDownload(String videoId) {
        List<DownloadedVideo> list = getDownloads();
        DownloadedVideo toDelete = null;
        for (DownloadedVideo v : list) {
            if (v.id.equals(videoId)) { toDelete = v; break; }
        }
        if (toDelete != null) {
            File file = new File(toDelete.filePath);
            if (file.exists()) file.delete();
            list.remove(toDelete);
            saveList(list);
        }
    }

    public boolean isDownloaded(String videoId) {
        for (DownloadedVideo v : getDownloads()) {
            if (v.id.equals(videoId)) {
                return new File(v.filePath).exists();
            }
        }
        return false;
    }

    public List<DownloadedVideo> getDownloads() {
        List<DownloadedVideo> list = new ArrayList<>();
        String json = prefs.getString(PREF_DOWNLOADS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(new DownloadedVideo(
                        obj.getString("id"),
                        obj.getString("title"),
                        obj.getString("filePath"),
                        obj.getLong("fileSize"),
                        obj.getLong("downloadDate")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void saveList(List<DownloadedVideo> list) {
        JSONArray arr = new JSONArray();
        for (DownloadedVideo v : list) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id",           v.id);
                obj.put("title",        v.title);
                obj.put("filePath",     v.filePath);
                obj.put("fileSize",     v.fileSize);
                obj.put("downloadDate", v.downloadDate);
                arr.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(PREF_DOWNLOADS, arr.toString()).apply();
    }
}
