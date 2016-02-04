package com.ajitesh.android.popularmovies.MovieGridLayouts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.ajitesh.android.popularmovies.ContentProvider.MoviesContract;
import com.ajitesh.android.popularmovies.HelperClasses.DbBitmapUtility;
import com.ajitesh.android.popularmovies.R;
import com.ajitesh.android.popularmovies.Volley_Networking.AppController;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.inthecheesefactory.thecheeselibrary.widget.AdjustableImageView;

import java.sql.Blob;

/**
 * Created by ajitesh on 30/1/16.
 */
public class MovieFavAdapter extends CursorAdapter {
    private static final String LOG_TAG = MovieFavFragment.class.getSimpleName();
    private Context mContext;
    private static int sLoaderID;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();


    public MovieFavAdapter(Context context, Cursor c, int flags, int loaderID){
        super(context, c, flags);
        Log.d(LOG_TAG, "FlavAdapter");
        mContext = context;
        sLoaderID = loaderID;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        AdjustableImageView imageView=new AdjustableImageView(mContext);
        imageView.setAdjustViewBounds(true);
        return imageView;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        AdjustableImageView adjustableImageView = (AdjustableImageView) view;
        try {
            adjustableImageView.setImageBitmap(DbBitmapUtility.getImage(cursor.getBlob(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_BLOB))));
        } catch (Exception e)
        {
            imageLoader.get(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER)), ImageLoader.getImageListener(adjustableImageView, R.drawable.loading, R.drawable.error));
            saveImagetoDB(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER)),cursor.getInt(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID)));
        }
    }
    public void saveImagetoDB(String poster, final int id){
        imageLoader.get(poster, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if(response.getBitmap()!=null)
                {
                    ContentValues contentValues=new ContentValues();
                    contentValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_BLOB, DbBitmapUtility.getBytes(response.getBitmap()));
                    mContext.getContentResolver().update(MoviesContract.MoviesEntry.CONTENT_URI, contentValues, MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + "=?", new String[]{String.valueOf(id)});

                }
            }
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }
}
