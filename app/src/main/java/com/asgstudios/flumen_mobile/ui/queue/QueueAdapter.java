package com.asgstudios.flumen_mobile.ui.queue;

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
import com.asgstudios.flumen_mobile.SongAndIndex;
import com.asgstudios.flumen_mobile.ui.play.PlayViewModel;

import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    private QueueViewModel queueViewModel;
    private Context context;
    private List<SongAndIndex> songs;

    public QueueAdapter(QueueViewModel queueViewModel, Context context, List<SongAndIndex> songs) {
        this.queueViewModel = queueViewModel;
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public QueueAdapter.QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.queue_row, parent, false);

        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final QueueAdapter.QueueViewHolder holder, final int position) {
        final Song song = songs.get(position).getSong();
        holder.rowSongTextView.setText(song.getName());
        holder.rowArtistTextView.setText(song.getArtist());
        holder.songLengthTextView.setText(secondsToFormatted(song.getLength()));

        holder.upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.vibrate(50);

                queueViewModel.getPlayQueue().moveUp(position);

                QueueAdapter.this.notifyDataSetChanged();
            }
        });

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.vibrateDouble(25);

                queueViewModel.getPlayQueue().remove(position);

                QueueAdapter.this.notifyDataSetChanged();
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

    public class QueueViewHolder extends RecyclerView.ViewHolder {

        TextView rowSongTextView;
        TextView rowArtistTextView;
        TextView songLengthTextView;

        ImageButton upButton;
        ImageButton removeButton;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);

            this.rowSongTextView = itemView.findViewById(R.id.rowQueueSongTextView);
            this.rowArtistTextView = itemView.findViewById(R.id.rowQueueArtistTextView);
            this.songLengthTextView = itemView.findViewById(R.id.queueLengthTextView);

            this.upButton = itemView.findViewById(R.id.upRowButton);
            this.removeButton = itemView.findViewById(R.id.removeRowButton);
        }

        public ImageButton getUpButton() {
            return this.upButton;
        }
    }
}
