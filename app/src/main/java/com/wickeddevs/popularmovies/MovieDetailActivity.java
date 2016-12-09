package com.wickeddevs.popularmovies;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.squareup.picasso.Picasso;
import com.wickeddevs.popularmovies.data.Movie;
import com.wickeddevs.popularmovies.data.MovieContract;
import com.wickeddevs.popularmovies.data.MovieDbHelper;
import com.wickeddevs.popularmovies.data.Trailer;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<String>>,
        TrailerRVA.OnTrailerSelectedListener {

    private static final String MOVIE_ID_EXTRA = "movieId";
    private static final int MOVIE_LIST_LOADER = 1;

    @InjectExtra Movie movie;

    @BindView(R.id.iv_thumbnail) ImageView ivThumbnail;
    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.tv_release_date) TextView tvReleaseDate;
    @BindView(R.id.tv_user_rating) TextView tvUserRating;
    @BindView(R.id.tv_plot_synopsis) TextView tvPlotSynopsis;
    @BindView(R.id.btn_favorites) Button btnFavorites;
    @BindView(R.id.tv_reviews) TextView tvReviews;
    @BindView(R.id.rv_trailers) RecyclerView rvTrailers;

    private boolean isFavorite;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);
        Dart.inject(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        rvTrailers.setLayoutManager(new LinearLayoutManager(this));
        rvTrailers.addItemDecoration(new com.wickeddevs.popularmovies.DividerItemDecoration(this, R.drawable.divider));

        tvTitle.setText(movie.title);
        tvReleaseDate.setText(movie.releaseDate);
        String rating = String.valueOf(movie.userRating) + "/10";
        tvUserRating.setText(rating);
        tvPlotSynopsis.setText(movie.plotSynopsis);
        Picasso.with(this).load(movie.thumbnailUrl).into(ivThumbnail);
        db = new MovieDbHelper(this).getWritableDatabase();
        isFavorite = MovieContract.isMovieFavorite(db, movie);
        if (isFavorite) {
            btnFavorites.setText(R.string.remove_from_favorites);
        }
        Bundle bundle = new Bundle();
        bundle.putString(MOVIE_ID_EXTRA, movie.movieId);
        getSupportLoaderManager().restartLoader(MOVIE_LIST_LOADER, bundle, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.btn_favorites)
    public void toggleFavorite() {
        if (isFavorite) {
            MovieContract.removeFavoriteMovie(db, movie);
            btnFavorites.setText(R.string.add_to_favorites);
            isFavorite = false;
        } else {
            MovieContract.addFavoriteMovie(db, movie);
            btnFavorites.setText(R.string.remove_from_favorites);
            isFavorite = true;
        }
    }

    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<String>>(this) {

            ArrayList<String> resultJson;

            @Override
            protected void onStartLoading() {
                if (args == null) {
                    return;
                }

                if (resultJson != null) {
                    deliverResult(resultJson);
                } else {
                    forceLoad();
                }
            }

            @Override
            public ArrayList<String> loadInBackground() {
                String movieId = args.getString(MOVIE_ID_EXTRA);
                try {
                    ArrayList<String> jsonResults = new ArrayList<>();
                    String trailerJson = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildMovieTrailerUrl(movieId));
                    String reviewsJson = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildMovieReviewUrl(movieId));
                    jsonResults.add(trailerJson);
                    jsonResults.add(reviewsJson);
                    return jsonResults;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void deliverResult(ArrayList<String> data) {
                resultJson = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<String>> loader, ArrayList<String> data) {
        ArrayList<Trailer> trailers = NetworkUtils.parseTrailersJson(data.get(0));
        String formattedReviews = NetworkUtils.parseReviewsJson(data.get(1));
        tvReviews.setText(formattedReviews);
        rvTrailers.setAdapter(new TrailerRVA(trailers, this));
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {

    }

    @Override
    public void onTrailerSelected(String url) {
        Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
