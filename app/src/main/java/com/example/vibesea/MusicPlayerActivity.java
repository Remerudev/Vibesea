package com.example.vibesea;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

public class MusicPlayerActivity extends AppCompatActivity {
    private float duration;
    private float currentTime;
    private boolean isPlaying;
    private Song currentSong;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player);

    }
    protected void togglePlayPause(){
        if(isPlaying){

        }
        isPlaying = !isPlaying;
    }

    protected void play(){

    }
}
