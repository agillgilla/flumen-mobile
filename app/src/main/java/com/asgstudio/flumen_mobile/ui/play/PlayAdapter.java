package com.asgstudio.flumen_mobile.ui.play;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.asgstudio.flumen_mobile.R;

public class PlayAdapter extends RecyclerView.Adapter<PlayAdapter.PlayViewHolder> {

    private Context context;
    private String[] songs;
    private String[] artists;
    private int[] songLengths;

    public PlayAdapter(Context context, String[] songs, String[] artists, int[] songLengths) {
        this.context = context;
        this.songs = songs;
        this.artists = artists;
        this.songLengths = songLengths;
    }

    @NonNull
    @Override
    public PlayAdapter.PlayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.play_row, parent, false);

        return new PlayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayAdapter.PlayViewHolder holder, int position) {
        holder.songTextView.setText(songs[position]);
        holder.artistTextView.setText(artists[position]);
        holder.songLengthTextView.setText(secondsToFormatted(songLengths[position]));
    }

    public static String secondsToFormatted(int seconds) {
        int mins = seconds / 60;
        seconds = seconds % 60;

        return String.format("%d:%02d", mins, seconds);
    }

    @Override
    public int getItemCount() {
        return Math.min(songs.length, artists.length);
    }

    public class PlayViewHolder extends RecyclerView.ViewHolder {

        TextView songTextView;
        TextView artistTextView;
        TextView songLengthTextView;

        ImageButton playButton;
        ImageButton queueButton;

        public PlayViewHolder(@NonNull View itemView) {
            super(itemView);

            this.songTextView = itemView.findViewById(R.id.songTextView);
            this.artistTextView = itemView.findViewById(R.id.artistTextView);
            this.songLengthTextView = itemView.findViewById(R.id.lengthTextView);

            this.playButton = itemView.findViewById(R.id.playButton);
            this.queueButton = itemView.findViewById(R.id.queueButton);
        }
    }
}
