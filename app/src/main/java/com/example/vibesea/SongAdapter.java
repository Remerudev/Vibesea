package com.example.vibesea;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        viewHolder.durationHolder.setText(String.valueOf(song.getDuration()));
        viewHolder.sizeHolder.setText(String.valueOf(song.getSize()));

        // Set songs BG
        Uri artworkUri = song.getArtworkUri();

        if(artworkUri!= null){
            viewHolder.artworkHolder.setImageURI(artworkUri);

            if (viewHolder.artworkHolder.getDrawable() == null) {
                viewHolder.artworkHolder.setImageResource(R.drawable.default_artwork);
            }
        }

        viewHolder.itemView.setOnClickListener(view -> Toast.makeText(context, song.getTitle(), Toast.LENGTH_SHORT).show());
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder{

        ImageView artworkHolder;
        TextView titleHolder, durationHolder, sizeHolder;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            artworkHolder = itemView.findViewById(R.id.artworkView);
            titleHolder = itemView.findViewById(R.id.titleView);
            durationHolder = itemView.findViewById(R.id.durationView);
            sizeHolder = itemView.findViewById(R.id.sizeView);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterSongs(List<Song> filteredList){
        songs = filteredList;
        notifyDataSetChanged();
    }
}
