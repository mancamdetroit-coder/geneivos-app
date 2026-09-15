package org.geneivos.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onPlay(DownloadedVideo video);
        void onDelete(DownloadedVideo video, int position);
    }

    private final List<DownloadedVideo> videos;
    private final OnItemClickListener listener;

    public DownloadsAdapter(List<DownloadedVideo> videos, OnItemClickListener listener) {
        this.videos   = videos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloadedVideo video = videos.get(position);
        holder.title.setText(video.title);
        holder.meta.setText(video.getFormattedSize() + " · Offline");

        holder.btnPlay.setOnClickListener(v -> listener.onPlay(video));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(video, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() { return videos.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, meta;
        Button btnPlay, btnDelete;

        ViewHolder(View view) {
            super(view);
            title     = view.findViewById(R.id.videoTitle);
            meta      = view.findViewById(R.id.videoMeta);
            btnPlay   = view.findViewById(R.id.btnPlay);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
}
