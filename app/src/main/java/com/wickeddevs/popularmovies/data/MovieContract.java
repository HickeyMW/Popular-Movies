package com.wickeddevs.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;

public class MovieContract {

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_THUMBNAIL_URL = "thumbnailUrl";
        public static final String COLUMN_PLOT_SYNOPSIS = "plotSynopsis";
        public static final String COLUMN_USER_RATING = "userRating";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
    }

    public static ArrayList<Movie> getFavoriteMovies(SQLiteDatabase db) {
        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME, null, null, null, null, null, null);

        ArrayList<Movie> movies = new ArrayList<Movie>();
        while (cursor.moveToNext()) {
            Movie movie = new Movie();
            movie.dbId = cursor.getString(cursor.getColumnIndex(MovieEntry._ID));
            movie.movieId = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID));
            movie.title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
            movie.thumbnailUrl = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_THUMBNAIL_URL));
            movie.plotSynopsis = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_PLOT_SYNOPSIS));
            movie.userRating = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_USER_RATING));
            movie.releaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
            movies.add(movie);
        }
        cursor.close();
        return movies;
    }

    public static long addFavoriteMovie(SQLiteDatabase db, Movie movie) {
        ContentValues cv = new ContentValues();
        cv.put(MovieEntry.COLUMN_MOVIE_ID, movie.movieId);
        cv.put(MovieEntry.COLUMN_TITLE, movie.title);
        cv.put(MovieEntry.COLUMN_THUMBNAIL_URL, movie.thumbnailUrl);
        cv.put(MovieEntry.COLUMN_PLOT_SYNOPSIS, movie.plotSynopsis);
        cv.put(MovieEntry.COLUMN_USER_RATING, movie.userRating);
        cv.put(MovieEntry.COLUMN_RELEASE_DATE, movie.releaseDate);

        return db.insert(MovieEntry.TABLE_NAME, null, cv);
    }

    public static boolean isMovieFavorite(SQLiteDatabase db, Movie movie) {
        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,
                null,
                MovieEntry.COLUMN_MOVIE_ID + "=" + movie.movieId,
                null,
                null,
                null,
                null);
        boolean exists = cursor.moveToNext();
        cursor.close();
        return exists;
    }

    public static boolean removeFavoriteMovie(SQLiteDatabase db, Movie movie) {
        return db.delete(MovieEntry.TABLE_NAME, MovieEntry._ID + "=" + movie.dbId, null) > 0;
    }
}
