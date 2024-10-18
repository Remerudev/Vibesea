package com.example.vibesea;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.database.Cursor;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;

import com.chibde.visualizer.BarVisualizer;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerview;
    SongAdapter songAdapter;
    List<Song> allSongs = new ArrayList<>();
    ActivityResultLauncher<String> storagePermissionLauncher;
    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;


    ExoPlayer player;
    ActivityResultLauncher<String> recordAudioPermissionLauncher;
    final String recordAudioPermission = Manifest.permission.RECORD_AUDIO;
    ConstraintLayout playerView;
    TextView playerCloseBtn;
    //controls
    TextView songNameView, skipPreviousBtn, skipNextBtn, playPauseBtn, repeatModeBtn, playlistBtn;
    TextView homeSongNameView, homeSkipPreviousBtn, homePlayPauseBtn, homeSkipNextBtn;
    //wrappers
    ConstraintLayout homeControlWrapper, headWrapper, artworkWrapper, seekbarWrapper, controlWrapper, audioVisualizerWrapper;
    //artwork
    CircleImageView artworkView;
    //seek bar
    SeekBar seekbar;
    TextView progressView, durationView;
    //audio visualizer
    BarVisualizer audioVisualizer;
    //status bar and navigation color
    int defaultStatusColor;
    //repeat mode
    int repeatMode = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //save status color
        defaultStatusColor = getWindow().getStatusBarColor();
        //set navigation color
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor, 199));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

        recyclerview = findViewById(R.id.recyclerview);
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                fetchSongs();
            } else {
                userResponses();
            }
        });
        storagePermissionLauncher.launch(permission);

        //record audio permission
        recordAudioPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted && player.isPlaying()) {
                activateAudioVisualizer();
            } else {
                userResponsesOnRecordAudioPerm();
            }
        });

        //views
        player = new ExoPlayer.Builder(this).build();
        playerView = findViewById(R.id.playerView);
        playerCloseBtn = findViewById(R.id.playerCloseBtn);
        songNameView = findViewById(R.id.songNameView);
        skipPreviousBtn = findViewById(R.id.skipPreviousBtn);
        skipNextBtn = findViewById(R.id.skipNextBtn);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        repeatModeBtn = findViewById(R.id.repeatModeBtn);
        playlistBtn = findViewById(R.id.playlistBtn);

        homeSongNameView = findViewById(R.id.homeSongNameView);
        homeSkipPreviousBtn = findViewById(R.id.homeSkipPreviousBtn);
        homeSkipNextBtn = findViewById(R.id.homeSkipNextBtn);
        homePlayPauseBtn = findViewById(R.id.homePlayPauseBtn);

        //wrappers
        homeControlWrapper = findViewById(R.id.homeControlWrapper);
        headWrapper = findViewById(R.id.headWrapper);
        artworkWrapper = findViewById(R.id.artworkWrapper);
        seekbarWrapper = findViewById(R.id.seekbarWrapper);
        controlWrapper = findViewById(R.id.controlWrapper);
        audioVisualizerWrapper = findViewById(R.id.audioVisualizerWrapper);

        //artwork
        artworkView = findViewById(R.id.artworkView);
        //seek bar
        seekbar = findViewById(R.id.seekbar);
        progressView = findViewById(R.id.progressView);
        durationView = findViewById(R.id.durationView);
        //audio visualizer
        audioVisualizer = findViewById(R.id.visualiazer);


        //controls
        playerControl();
    }

    private void playerControl() {
        //song name marquee
        songNameView.setSelected(true);
        homeSongNameView.setSelected(true);

        //exit music player
        playerCloseBtn.setOnClickListener(view -> exitPlayerView());
        playlistBtn.setOnClickListener(view -> exitPlayerView());
        //open music player on home
        homeControlWrapper.setOnClickListener(view -> showPlayerView());

        //player listener
        player.addListener(new Player.Listener(){
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                //show playing song title
                songNameView.setText(mediaItem.mediaMetadata.title);
                homeSongNameView.setText(mediaItem.mediaMetadata.title);

                progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                seekbar.setProgress((int) player.getCurrentPosition());
                seekbar.setMax((int) player.getDuration());
                durationView.setText(getReadableTime((int) player.getDuration()));
                playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_outline, 0, 0, 0);
                homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);

                //show the current art work
                showCurrentArtwork();

                //update the progress position of a current song
                updatePlayerPositionProgress();

                //load the artwork animation
                artworkView.setAnimation(loadRotation());

                //set audio visualizer
                activateAudioVisualizer();

                //update player view colors
                updatePlayerColors();

                if (!player.isPlaying()){
                    player.play();
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == ExoPlayer.STATE_READY){
                    //set values to player views
                    songNameView.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    homeSongNameView.setText(player.getCurrentMediaItem().mediaMetadata.title);
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    durationView.setText(getReadableTime((int) player.getDuration()));
                    seekbar.setMax((int) player.getDuration());
                    seekbar.setProgress((int) player.getCurrentPosition());
                    playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_outline, 0, 0, 0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);

                    //show the current art work
                    showCurrentArtwork();

                    //update the progress position of a current song
                    updatePlayerPositionProgress();

                    //load the artwork animation
                    artworkView.setAnimation(loadRotation());

                    //set audio visualizer
                    activateAudioVisualizer();

                    //update player view colors
                    updatePlayerColors();
                }
                else {
                    playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_outline,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
                }
            }
        });

        //skip to next track
        skipNextBtn.setOnClickListener(view -> skipToNextSong());
        homeSkipNextBtn.setOnClickListener(view -> skipToNextSong());

        //skip to previous track
        skipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());
        homeSkipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());

        //play or pause the player
        playPauseBtn.setOnClickListener(view -> playOrPausePlayer());
        homePlayPauseBtn.setOnClickListener(view -> playOrPausePlayer());

        //seek bar listener
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressValue = seekBar.getProgress();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player.getPlaybackState() == ExoPlayer.STATE_READY) {
                    seekBar.setProgress(progressValue);
                    progressView.setText(getReadableTime(progressValue));
                    player.seekTo(progressValue);
                }

            }
        });

        //repeat mode
        repeatModeBtn.setOnClickListener(view -> {
            if (repeatMode == 1){
                //repeat 1
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                repeatMode =2;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_one,0,0,0);
            }
            else if (repeatMode == 2){
                //shuffle all
                player.setShuffleModeEnabled(true);
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                repeatMode = 3;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shuffle,0,0,0);
            }
            else if (repeatMode == 3){
                //repeat all
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                player.setShuffleModeEnabled(false);
                repeatMode = 1;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_all,0,0,0);
            }
            //update colors
            updatePlayerColors();
        });
    }

    private void playOrPausePlayer() {
        if (player.isPlaying()){
            player.pause();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_outline,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
            artworkView.clearAnimation();
        }
        else{
            player.play();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_outline,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);
            artworkView.startAnimation(loadRotation());
        }
        //update player colors
        updatePlayerColors();
    }

    private void skipToPreviousSong() {
        if (player.hasPreviousMediaItem()){
            player.seekToPrevious();
        }
    }

    private void skipToNextSong() {
        if (player.hasNextMediaItem()){
            player.seekToNext();
        }
    }

    private Animation loadRotation() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        return rotateAnimation;
    }

    private void updatePlayerPositionProgress() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player.isPlaying()) {
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    seekbar.setProgress((int) player.getCurrentPosition());
                }
                //repeat calling method
                updatePlayerPositionProgress();
            }
        }, 1000);
    }



    private void showCurrentArtwork() {
        artworkView.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);

        if (artworkView.getDrawable() == null) {
            artworkView.setImageResource(R.drawable.default_artwork);
        }
    }

    String getReadableTime(int duration) {
        String time;
        int hrs = duration/(1000*60*60);
        int min = (duration%(1000*60*60))/(1000*60);
        int secs = (((duration%(1000*60*60))%(1000*60*60))%(1000*60))/1000;

        if (hrs<1) { time = min +":"+secs; }
        else{
            time = hrs +":"+ min +":"+secs;
        }
        return time;
    }

    private void updatePlayerColors() {

    }

    private void showPlayerView() {
        playerView.setVisibility(View.VISIBLE);
        updatePlayerColors();
    }

    private void exitPlayerView() {
        playerView.setVisibility(View.GONE);
        getWindow().setStatusBarColor(defaultStatusColor);
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));
    }

    private void userResponsesOnRecordAudioPerm() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(shouldShowRequestPermissionRationale(recordAudioPermission)){
                new AlertDialog.Builder(this)
                        .setTitle("Requesting to show Audio Visualizer")
                        .setMessage("Allow this app to display audio visualizer")
                        .setPositiveButton("Allow", (dialogInterface, i) -> recordAudioPermissionLauncher.launch(recordAudioPermission))
                        .setNegativeButton("Don't Allow", (dialogInterface, i) -> {
                            Toast.makeText(getApplicationContext(),"You did not want to show the audio visualizer", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        })
                        .show();
            }
            else {
                Toast.makeText(getApplicationContext(),"You did not want to show the audio visualizer", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //audio visualizer
    private void activateAudioVisualizer() {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player.isPlaying()){
            player.stop();
        }
        player.release();
    }
    private void userResponses() {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            fetchSongs();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(permission)) {
                new AlertDialog.Builder(this)
                        .setTitle("Requesting Permission")
                        .setMessage("Allow us to access your device's songs")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Request permission to user device
                                storagePermissionLauncher.launch(permission);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                                finish();
                            }
                        })
                        .show();
            }
        } else {
            Toast.makeText(this, "You canceled the permission request, no songs will be shown.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSongs(){
        List<Song> songs = new ArrayList<>();
        Uri mediaStorageUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaStorageUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            mediaStorageUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID
        };

        // Sort the songs by date added
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        // Query the songs
        try (Cursor cursor = getContentResolver().query(mediaStorageUri, projection, null, null, sortOrder)) {

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while (cursor.moveToNext()){
                // Retrieve song information
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                long albumId = cursor.getLong(albumIdColumn);

                // Generate URIs for song and album artwork
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                Uri albumArtworkUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);

                // Remove the file extension from the song name
                name = name.substring(0, name.lastIndexOf("."));

                Song song = new Song(name, uri, albumArtworkUri, size, duration);

                // Add the song to the list
                songs.add(song);
            }

            // Display the songs in RecyclerView
            showSongs(songs);
        }
    }


    private void showSongs(List<Song> songs) {
        if (songs.size() == 0){
            Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
            return;
        }

        //save song
        allSongs.clear();
        allSongs.addAll(songs);

        // update the title
        String title = getResources().getString(R.string.app_name) + " - " + songs.size();
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        // layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(layoutManager);

        //set the adapter to recycler view
        songAdapter = new SongAdapter(getApplicationContext(), songs, player, playerView);
        recyclerview.setAdapter(songAdapter);


    }

    // para paganahin ung search btn
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_btn, menu);

        MenuItem menuItem = menu.findItem(R.id.searchBtn);
        SearchView searchView = (SearchView) menuItem.getActionView();

        SearchSong(searchView);
        return super.onCreateOptionsMenu(menu);
    }

    private void SearchSong(SearchView searchView) {
        // search view listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText.toLowerCase());
                return true;
            }
        });
    }

    private void filterSongs(String query) {
        List<Song> filterList = new ArrayList<>();
        if (allSongs.size() > 0) {
            for (Song song : allSongs) {
                if (song.getTitle().toLowerCase().contains(query)) {
                    filterList.add(song);

                }
            }
            if (songAdapter != null) {
                songAdapter.filterSongs(filterList);
            }
        }
    }
}