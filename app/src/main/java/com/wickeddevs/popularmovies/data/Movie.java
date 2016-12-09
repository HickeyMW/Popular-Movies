package com.wickeddevs.popularmovies.data;

import org.parceler.Parcel;

@Parcel
public class Movie {
    public String dbId;
    public String movieId;
    public String title;
    public String thumbnailUrl;
    public String plotSynopsis;
    public double userRating;
    public String releaseDate;
}
