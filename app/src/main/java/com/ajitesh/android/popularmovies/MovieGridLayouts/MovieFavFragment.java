package com.ajitesh.android.popularmovies.MovieGridLayouts;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.ajitesh.android.popularmovies.ContentProvider.MoviesContract;
import com.ajitesh.android.popularmovies.MainActivity;
import com.ajitesh.android.popularmovies.Movie;
import com.ajitesh.android.popularmovies.R;

import java.util.ArrayList;

import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * Created by ajitesh on 30/1/16.
 */
public class MovieFavFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,MainActivity.UpdateableFragment {

    private static final int CURSOR_LOADER_ID = 0;
    private MovieFavAdapter mFlavorAdapter;
    private GridViewWithHeaderAndFooter mGridView;
    private MovieGridFragment.OnItemSelectedListener listener;
    ArrayList<Movie> movieList;
    TextView status;

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MovieGridFragment.OnItemSelectedListener) {
            listener = (MovieGridFragment.OnItemSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                MoviesContract.MoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        movieList=new ArrayList<Movie>();
        while(data.moveToNext())
            movieList.add(getMovie(data));
        if(movieList.isEmpty()){
            status.setVisibility(View.VISIBLE);
            status.setText("No movies found. . .");}
        else
            status.setVisibility(View.GONE);
        mFlavorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieList=null;
        mFlavorAdapter.swapCursor(null);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate fragment_main layout
        final View rootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);


        mFlavorAdapter = new MovieFavAdapter(getActivity(), null, 0, CURSOR_LOADER_ID);

        mGridView = (GridViewWithHeaderAndFooter) rootView.findViewById(R.id.gridview);

        mGridView.setAdapter(mFlavorAdapter);


        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // increment the position to match Database Ids indexed starting at 1
                listener.onMovieSelected(movieList.get(position));
            }
        });
        status=(TextView)rootView.findViewById(R.id.status);
        return rootView;
    }
    @Override
    public void update() {
        Log.d("Load", "Loading");
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }
    public Movie getMovie(Cursor cursor){
        if((cursor!=null)&&(cursor.getCount()>0)){
            Log.d("Movie",cursor.getInt(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID))+"-"+cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_TITLE)));
            return new Movie(cursor.getInt(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID)),
                    cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER)),
                    cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_OVERVIEW)),
                    Float.parseFloat(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_RATING))),
                    cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_RELEASE)),
                    Float.parseFloat(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POPULARITY))),
                    cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_BACKDROP)));
        }
        return null;
    }
}
