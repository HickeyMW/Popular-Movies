package com.wickeddevs.popularmovies;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.wickeddevs.popularmovies.data.Movie;
import com.wickeddevs.popularmovies.data.MovieContract;
import com.wickeddevs.popularmovies.data.MovieDbHelper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieListActivity extends AppCompatActivity implements MovieListRVA.OnMovieSelectedListener,
        LoaderManager.LoaderCallbacks<String> {

    private static final String MOVIE_LIST_URL_EXTRA = "movieList";
    private static final String DISPLAYED_LIST_KEY = "displayedList";
    private static final int MOVIE_LIST_LOADER = 0;
    private static final int MOST_POPULAR = 0;
    private static final int HIGHEST_RATED = 1;
    private static final int MY_FAVORITES = 2;

    @BindView(R.id.rv_movie_list) RecyclerView rvMovieList;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    private SQLiteDatabase db;
    private int displayedList = 0;
    private MenuItem miMostPopular;
    private MenuItem miHighestRated;
    private MenuItem miMyFavorites;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DISPLAYED_LIST_KEY, displayedList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        ButterKnife.bind(this);
        rvMovieList.setLayoutManager(new GridLayoutManager(this, 2));
        db = new MovieDbHelper(this).getReadableDatabase();
        if (savedInstanceState != null) {
            if(savedInstanceState.containsKey(DISPLAYED_LIST_KEY)) {
                displayedList = savedInstanceState.getInt(DISPLAYED_LIST_KEY);
            }
        }
        loadMovieList();

    }

    private void loadMovieList() {
        if (NetworkUtils.isOnline(this)) {
            progressBar.setVisibility(View.VISIBLE);
            rvMovieList.setVisibility(View.INVISIBLE);
            Bundle bundle = new Bundle();
            if (displayedList == MOST_POPULAR) {
                setTitle(R.string.most_popular_movies);
                bundle.putString(MOVIE_LIST_URL_EXTRA, NetworkUtils.buildPopularMoviesUrl().toString());
                getSupportLoaderManager().restartLoader(MOVIE_LIST_LOADER, bundle, this);
            } else if (displayedList == HIGHEST_RATED) {
                setTitle(R.string.highest_rated_movies);
                bundle.putString(MOVIE_LIST_URL_EXTRA, NetworkUtils.buildTopRatedMoviesUrl().toString());
                getSupportLoaderManager().restartLoader(MOVIE_LIST_LOADER, bundle, this);
            } else {
                setTitle(getString(R.string.my_favorites));
                ArrayList<Movie> movies = MovieContract.getFavoriteMovies(db);
                progressBar.setVisibility(View.INVISIBLE);
                if (movies.size() == 0) {
                    Toast.makeText(this, R.string.no_favorites_message, Toast.LENGTH_LONG).show();
                } else {
                    rvMovieList.setVisibility(View.VISIBLE);
                    rvMovieList.setAdapter(new MovieListRVA(movies, this));
                }

            }
        } else {
            Toast.makeText(this, R.string.no_internet_message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        miMostPopular = menu.findItem(R.id.mi_most_popular);
        miHighestRated = menu.findItem(R.id.mi_highest_rated);
        miMyFavorites = menu.findItem(R.id.mi_my_favorites);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mi_most_popular) {
            displayedList = MOST_POPULAR;
            miMostPopular.setVisible(false);
            miHighestRated.setVisible(true);
            miMyFavorites.setVisible(true);
        } else if (item.getItemId() == R.id.mi_highest_rated) {
            displayedList = HIGHEST_RATED;
            miMostPopular.setVisible(true);
            miHighestRated.setVisible(false);
            miMyFavorites.setVisible(true);
        } else {
            displayedList = MY_FAVORITES;
            miMostPopular.setVisible(true);
            miHighestRated.setVisible(true);
            miMyFavorites.setVisible(false);
        }
        loadMovieList();
        return true;
    }

    @Override
    public void onMovieSelected(Movie movie) {
        Intent i = Henson.with(this).gotoMovieDetailActivity().movie(movie).build();
        startActivity(i);
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {

        return new AsyncTaskLoader<String>(this) {

            String movieListJson;

            @Override
            protected void onStartLoading() {
                if (args == null) {
                    return;
                }

                if (movieListJson != null) {
                    deliverResult(movieListJson);
                } else {
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {
                String movieListUrl = args.getString(MOVIE_LIST_URL_EXTRA);
                try {
                    return NetworkUtils.getResponseFromHttpUrl(new URL(movieListUrl));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void deliverResult(String data) {
                movieListJson = data;
                super.deliverResult(data);
            }
        };
    }


    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        progressBar.setVisibility(View.INVISIBLE);

        if (data == null) {
            Toast.makeText(this, R.string.error_loading_data, Toast.LENGTH_LONG).show();
        } else {
            ArrayList<Movie> movies = NetworkUtils.parseMovieListJson(data);
            rvMovieList.setAdapter(new MovieListRVA(movies, this));
            rvMovieList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
