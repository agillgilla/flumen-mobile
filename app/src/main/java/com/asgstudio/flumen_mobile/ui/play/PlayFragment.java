package com.asgstudio.flumen_mobile.ui.play;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asgstudio.flumen_mobile.R;

public class PlayFragment extends Fragment {

    private PlayViewModel playViewModel;

    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        playViewModel =
                ViewModelProviders.of(this).get(PlayViewModel.class);
        View root = inflater.inflate(R.layout.fragment_play, container, false);


        //final TextView textView = root.findViewById(R.id.text_dashboard);
        playViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        String[] songs = new String[] {"Good Vibrations", "Bohemian Rhapsody"};
        String[] artists = new String[] {"Marky Mark and the Funky Bunch", "Queen"};
        int[] songLengths = new int[] {180, 240};

        PlayAdapter playAdapter = new PlayAdapter(this.getContext(), songs, artists, songLengths);

        recyclerView = getView().findViewById(R.id.playView);
        if (recyclerView == null) {
            System.out.println("RECYCLER VIEW IS NULL!");
        }
        recyclerView.setAdapter(playAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }
}