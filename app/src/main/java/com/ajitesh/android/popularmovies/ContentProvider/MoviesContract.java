package com.ajitesh.android.popularmovies.ContentProvider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ajitesh on 30/1/16.
 */
public class MoviesContract {
    public static final String CONTENT_AUTHORITY = "com.ajitesh.android.popularmovies.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final class MoviesEntry implements BaseColumns {
        // table name
        public static final String TABLE_MOVIES = "movie";

        public static final String _ID = "_id";
        public static final String COLUMN_POSTER= "poster";
        public static final String COLUMN_POSTER_BLOB= "posterblob";
        public static final String COLUMN_TITLE= "title";
        public static final String COLUMN_RATING= "rating";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE = "release";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_BACKDROP = "backdrop";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        // create content uri
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_MOVIES).build();
        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_MOVIES;
        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +"/" + CONTENT_AUTHORITY + "/" + TABLE_MOVIES;

        // for building URIs on insertion
        public static Uri buildFlavorsUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
