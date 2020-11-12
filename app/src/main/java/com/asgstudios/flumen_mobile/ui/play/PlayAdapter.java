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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlayAdapter extends RecyclerView.Adapter<PlayAdapter.PlayViewHolder> {

    private PlayViewModel playViewModel;
    private Context context;
    private List<Song> songs;
    public int playingIndex;
    public int pausedIndex;

    public PlayAdapter(PlayViewModel playViewModel, Context context, List<Song> songs, int playingIndex) {
        this.playViewModel = playViewModel;
        this.context = context;
        this.songs = songs;
        this.playingIndex = playingIndex;

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
        holder.rowSongTextView.setText(song.getName());
        holder.rowArtistTextView.setText(song.getArtist());
        holder.songLengthTextView.setText(secondsToFormatted(song.getLength()));

        if (position == playingIndex) {
            holder.playButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            holder.playButton.setImageResource(android.R.drawable.ic_media_play);
        }

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.vibrate(50);

                if (playViewModel.playPauseSong(song, holder)) {
                    playingIndex = position;
                    playViewModel.setPlayingIndex(playingIndex);
                }
                /*
                int prevPosition = playingIndex;
                if (player.playPauseSong(song, holder)) {
                    playingIndex = position;
                } else {
                    pausedIndex = playingIndex;
                    playingIndex = -1;
                }
                */
                //PlayAdapter.this.notifyItemChanged(position);

                PlayAdapter.this.notifyDataSetChanged();
                /*
                if (prevPosition != -1) {
                    PlayAdapter.this.notifyItemChanged(prevPosition);
                }
                */
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

        TextView rowSongTextView;
        TextView rowArtistTextView;
        TextView songLengthTextView;

        ImageButton playButton;
        ImageButton queueButton;

        public PlayViewHolder(@NonNull View itemView) {
            super(itemView);

            this.rowSongTextView = itemView.findViewById(R.id.rowSongTextView);
            this.rowArtistTextView = itemView.findViewById(R.id.rowArtistTextView);
            this.songLengthTextView = itemView.findViewById(R.id.lengthTextView);

            this.playButton = itemView.findViewById(R.id.playRowButton);
            this.queueButton = itemView.findViewById(R.id.queueRowButton);
        }

        public ImageButton getPlayButton() {
            return this.playButton;
        }
    }
}
