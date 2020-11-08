package com.asgstudios.flumen_mobile.ui.play;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.asgstudios.flumen_mobile.MainActivity;
import com.asgstudios.flumen_mobile.R;
import com.asgstudios.flumen_mobile.Song;
import com.asgstudios.flumen_mobile.ui.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlayAdapter extends RecyclerView.Adapter<PlayAdapter.PlayViewHolder> {

    private Player player;
    private Context context;
    private List<Song> songs;

    public PlayAdapter(Player player, Context context, List<Song> songs) {
        this.player = player;
        this.context = context;
        this.songs = songs;

        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }

    @NonNull
    @Override
    public PlayAdapter.PlayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.play_row, parent, false);

        return new PlayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlayAdapter.PlayViewHolder holder, final int position) {
        final Song song = songs.get(position);
        holder.songTextView.setText(song.getName());
        holder.artistTextView.setText(song.getArtist());
        holder.songLengthTextView.setText(secondsToFormatted(song.getLength()));

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.vibrate(50);
                player.playPauseSong(song, holder);
            }
        });
    }

    public static String secondsToFormatted(int seconds) {
        int mins = seconds / 60;
        seconds = seconds % 60;

        return String.format("%d:%02d", mins, seconds);
    }

    @Override
    public int getItemCount() {
        return songs.size();
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

            this.playButton = itemView.findViewById(R.id.playRowButton);
            this.queueButton = itemView.findViewById(R.id.queueRowButton);
        }

        public ImageButton getPlayButton() {
            return this.playButton;
        }
    }
}
