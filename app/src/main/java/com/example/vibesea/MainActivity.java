package com.example.vibesea;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.database.Cursor;
import androidx.appcompat.widget.SearchView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerview;
    SongAdapter songAdapter;
    List<Song> allSongs = new ArrayList<>();
    ActivityResultLauncher<String> storagePermissionLauncher;
    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

    //


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);
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
            if (granted){
                fetchSongs();
            }
            else {
                userResponses();
            }
        });
        storagePermissionLauncher.launch(permission);



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
        songAdapter = new SongAdapter(getApplicationContext(), songs);
        recyclerview.setAdapter(songAdapter);
        

    }

    // para paganahin ung search btn
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_btn, menu);

        MenuItem menuItem = menu.findItem(R.id.searchBtn);
        SearchView searchView = (SearchView) menuItem.getActionView();  // This should now work correctly

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