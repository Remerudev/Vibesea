package com.example.vibesea;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    Context context;
    List<Song> songs;

    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row_item, parent, false);

        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {



        Song song = songs.get(position);
        SongViewHolder viewHolder = (SongViewHolder) holder;

        viewHolder.titleHolder.setText(song.getTitle());
        viewHolder.durationHolder.setText(getDuration(song.getDuration()));
        viewHolder.sizeHolder.setText(getSize(song.getSize()));

        // Set songs BG
        Uri artworkUri = song.getArtworkUri();

        if(artworkUri!= null){
            viewHolder.artworkHolder.setImageURI(artworkUri);

            if (viewHolder.artworkHolder.getDrawable() == null) {
                viewHolder.artworkHolder.setImageResource(R.drawable.default_artwork);
            }
        }

//        viewHolder.itemView.setOnClickListener(
//                view -> {
//                    Toast.makeText(context, song.getTitle(), Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent((Activity)viewHolder.itemView.getContext(), MusicPlayerActivity.class);
//                    ((Activity)viewHolder.itemView.getContext()).startActivity(intent);
//
//                }
//        );

    }

    public class SongViewHolder extends RecyclerView.ViewHolder{

        ImageView artworkHolder;
        TextView titleHolder, durationHolder, sizeHolder;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            artworkHolder = itemView.findViewById(R.id.artworkView);
            titleHolder = itemView.findViewById(R.id.titleView);
            durationHolder = itemView.findViewById(R.id.durationView);
            sizeHolder = itemView.findViewById(R.id.sizeView);


            itemView.setOnClickListener(view -> {
                Toast.makeText(context, "test", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(view.getContext(), MusicPlayerActivity.class);
                view.getContext().startActivity(intent);
            });

        }

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
    //filter the song and search results
    @SuppressLint("NotifyDataSetChanged")
    public void filterSongs(List<Song> filteredList){
        songs = filteredList;
        notifyDataSetChanged();
    }

    private String getDuration(int totalDuration){
        String totalDurationText;

        int hrs = totalDuration/(1000*60*60);
        int min = (totalDuration%(1000*60*60))/(1000*60);
        int secs = (((totalDuration%(1000*60*60))%(1000*60*60))%(1000*60))/1000;

        if (hrs <1){
            totalDurationText = String.format("%02d:%02d", min, secs);
        }
        else {
            totalDurationText = String.format("%1d:%02d:%02d", hrs, min, secs);
        }

        return totalDurationText;
    }

    // get the size of the songs in MB
    private String getSize(long bytes){
        String hrSize;

        double k = bytes/1024.0;
        double m = ((bytes/1024.0)/1024.0);
        double g = (((bytes/1024.0)/1024.0)/1024.0);
        double t = ((((bytes/1024.0)/1024.0)/1024.0)/1024.0);
        // format of the song size
        DecimalFormat dec = new DecimalFormat("0.00");

        if (t>1){
            hrSize = dec.format(t).concat(" TB");
        }else if (g>1){
            hrSize = dec.format(g).concat(" GB");

        }
        else if (m>1) {
            hrSize = dec.format(m).concat(" MB");
        }
        else if (k>1){
            hrSize = dec.format(k).concat(" KB");
        }
        else {
            hrSize = dec.format(bytes).concat(" Bytes");

        }

        return hrSize;
    }
}
