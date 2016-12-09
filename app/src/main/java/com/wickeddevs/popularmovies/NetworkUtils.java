package com.wickeddevs.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.wickeddevs.popularmovies.data.Movie;
import com.wickeddevs.popularmovies.data.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class NetworkUtils {

    private final static String API_KEY = "";
    private final static String MOVIE_API_BASE_URL = "https://api.themoviedb.org/3/movie";
    private final static String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    private final static String PATH_POPULAR = "popular";
    private final static String PATH_TOP_RATED = "top_rated";
    private final static String PARAM_API_KEY = "api_key";


    public static URL buildTopRatedMoviesUrl() {
        Uri builtUri = Uri.parse(MOVIE_API_BASE_URL).buildUpon()
                .appendPath(PATH_TOP_RATED)
                .appendQueryParameter(PARAM_API_KEY, API_KEY)
                .build();

        return convertUriToUrl(builtUri);
    }

    public static URL buildPopularMoviesUrl() {
        Uri builtUri = Uri.parse(MOVIE_API_BASE_URL).buildUpon()
                .appendPath(PATH_POPULAR)
                .appendQueryParameter(PARAM_API_KEY, API_KEY)
                .build();

        return convertUriToUrl(builtUri);
    }

    public static URL buildMoviePosterUrl(String posterPath) {
        Uri builtUri = Uri.parse(MOVIE_POSTER_BASE_URL).buildUpon()
                .appendEncodedPath(posterPath)
                .build();

        return convertUriToUrl(builtUri);
    }

    public static URL buildMovieTrailerUrl(String movieId) {
        Uri builtUri = Uri.parse(MOVIE_API_BASE_URL).buildUpon()
                .appendEncodedPath(movieId)
                .appendEncodedPath("videos")
                .appendQueryParameter(PARAM_API_KEY, API_KEY)
                .build();

        return convertUriToUrl(builtUri);
    }

    public static URL buildMovieReviewUrl(String movieId) {
        Uri builtUri = Uri.parse(MOVIE_API_BASE_URL).buildUpon()
                .appendEncodedPath(movieId)
                .appendEncodedPath("reviews")
                .appendQueryParameter(PARAM_API_KEY, API_KEY)
                .build();

        return convertUriToUrl(builtUri);
    }

    private static URL convertUriToUrl(Uri uri) {
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static ArrayList<Movie> parseMovieListJson(String movieListJson) {
        try {
            ArrayList<Movie> movies = new ArrayList<>();
            JSONArray jsonMovies = new JSONObject(movieListJson).getJSONArray("results");
            for (int i = 0; i < jsonMovies.length(); i++) {
                JSONObject jsonMovie = jsonMovies.getJSONObject(i);
                Movie movie = new Movie();
                movie.movieId = String.valueOf(jsonMovie.getInt("id"));
                movie.title = jsonMovie.getString("original_title");
                movie.thumbnailUrl = NetworkUtils.buildMoviePosterUrl(jsonMovie.getString("poster_path")).toString();
                movie.plotSynopsis = jsonMovie.getString("overview");
                movie.userRating = jsonMovie.getDouble("vote_average");
                movie.releaseDate = jsonMovie.getString("release_date");
                movies.add(movie);
            }
            return movies;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Trailer> parseTrailersJson(String trailerJson) {
        try {
            ArrayList<Trailer> trailers = new ArrayList<>();
            JSONArray jsonTrailers = new JSONObject(trailerJson).getJSONArray("results");
            for (int i = 0; i < jsonTrailers.length(); i++) {
                JSONObject jsonTrailer = jsonTrailers.getJSONObject(i);
                if (jsonTrailer.getString("site").equals("YouTube")) {
                    Trailer trailer = new Trailer();
                    trailer.name = jsonTrailer.getString("name");
                    trailer.url = "https://www.youtube.com/watch?v=" + jsonTrailer.getString("key");
                    trailers.add(trailer);
                }
            }
            return trailers;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String parseReviewsJson(String trailerJson) {
        try {
            String reviews = "";
            JSONArray jsonReviews = new JSONObject(trailerJson).getJSONArray("results");
            for (int i = 0; i < jsonReviews.length(); i++) {
                JSONObject jsonReview = jsonReviews.getJSONObject(i);
                if (jsonReview.has("content")) {
                    reviews += jsonReview.get("content");
                }
            }
            return reviews;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
