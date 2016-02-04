package com.ajitesh.android.popularmovies.MovieGridLayouts;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.ajitesh.android.popularmovies.Movie;
import com.ajitesh.android.popularmovies.R;
import com.android.volley.toolbox.ImageLoader;
import com.ajitesh.android.popularmovies.Volley_Networking.AppController;
import com.inthecheesefactory.thecheeselibrary.widget.AdjustableImageView;

import java.util.List;

/**
 * Created by ajitesh on 30/11/15.
 */
public class MovieGridAdapter extends ArrayAdapter<Movie> {

    Context context;
    List<Movie> movies;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public MovieGridAdapter(Context context, List<Movie> objects) {
        super(context, 0, objects);
        this.context=context;
        this.movies=objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AdjustableImageView imageView;
        if(convertView==null){
            imageView=new AdjustableImageView(context);
        }
        else{
            imageView=(AdjustableImageView)convertView;
        }
        imageView.setAdjustViewBounds(true);
        imageLoader.get(movies.get(position).getImage(),ImageLoader.getImageListener(imageView, R.drawable.loading,R.drawable.error));
        return imageView;
    }
}
