package com.wickeddevs.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.wickeddevs.popularmovies.data.Movie;

import java.util.ArrayList;

public class MovieListRVA extends RecyclerView.Adapter<MovieListRVA.MovieVH> {

    ArrayList<Movie> movies;
    OnMovieSelectedListener listener;

    public MovieListRVA(ArrayList<Movie> movies, OnMovieSelectedListener listener) {
        this.movies = movies;
        this.listener = listener;
    }

    @Override
    public MovieVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_movie_poster, parent, false);
        return new MovieVH(view);
    }

    @Override
    public void onBindViewHolder(MovieVH holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    class MovieVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivMoviePoster;

        public MovieVH(View itemView) {
            super(itemView);
            ivMoviePoster = (ImageView) itemView.findViewById(R.id.iv_movie_poster);
            itemView.setOnClickListener(this);
        }

        public void bind(int listIndex) {
            String url = movies.get(listIndex).thumbnailUrl;
            Picasso.with(ivMoviePoster.getContext()).load(url).into(ivMoviePoster);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            listener.onMovieSelected(movies.get(position));
        }
    }

    public interface OnMovieSelectedListener {
        void onMovieSelected(Movie movie);
    }
}
