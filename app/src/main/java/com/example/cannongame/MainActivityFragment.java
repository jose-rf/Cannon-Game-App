package com.example.cannongame;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

public class MainActivityFragment extends Fragment {
    public CannonView cannonView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        cannonView = (CannonView) view.findViewById(R.id.cannonView);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC); // volume pro jogo
    }

    @Override
    public void onPause() {
        super.onPause();
        cannonView.stopGame(); // para
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cannonView.releaseResources(); // libera
    }
}