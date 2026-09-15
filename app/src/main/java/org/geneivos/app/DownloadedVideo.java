package org.geneivos.app;

public class DownloadedVideo {
    public String id;
    public String title;
    public String filePath;
    public long fileSize;
    public long downloadDate;

    public DownloadedVideo(String id, String title, String filePath, long fileSize, long downloadDate) {
        this.id           = id;
        this.title        = title;
        this.filePath     = filePath;
        this.fileSize     = fileSize;
        this.downloadDate = downloadDate;
    }

    public String getFormattedSize() {
        if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        }
    }
}
