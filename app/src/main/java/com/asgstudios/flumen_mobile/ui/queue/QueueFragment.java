package com.asgstudios.flumen_mobile.ui.queue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asgstudios.flumen_mobile.MainActivity;
import com.asgstudios.flumen_mobile.R;
import com.asgstudios.flumen_mobile.SongAndIndex;

import java.util.List;
import java.util.Queue;

public class QueueFragment extends Fragment {

    private MainActivity mainActivity;
    private View rootView;

    private QueueViewModel queueViewModel;

    private QueueAdapter queueAdapter;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        this.mainActivity = (MainActivity) getParentFragment().getActivity();

        setRetainInstance(true);

        queueViewModel = new ViewModelProvider(mainActivity, ViewModelProvider.AndroidViewModelFactory.getInstance(mainActivity.getApplication())).get(QueueViewModel.class);


        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_queue, container, false);
        }

        recyclerView = rootView.findViewById(R.id.queueView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        queueViewModel.getQueue().observe(getViewLifecycleOwner(), new Observer<List<SongAndIndex>>() {
            @Override
            public void onChanged(List<SongAndIndex> songs) {
                queueAdapter = new QueueAdapter(queueViewModel, rootView.getContext(), songs);
                queueViewModel.setQueueAdapter(queueAdapter);
                recyclerView.setAdapter(queueAdapter);

                queueAdapter.notifyDataSetChanged();
            }
        });


        return rootView;
    }
}