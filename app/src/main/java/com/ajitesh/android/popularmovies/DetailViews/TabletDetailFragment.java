package com.ajitesh.android.popularmovies.DetailViews;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ajitesh.android.popularmovies.ContentProvider.MoviesContract;
import com.ajitesh.android.popularmovies.HelperClasses.DbBitmapUtility;
import com.ajitesh.android.popularmovies.Movie;
import com.ajitesh.android.popularmovies.R;
import com.ajitesh.android.popularmovies.Volley_Networking.AppController;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.inthecheesefactory.thecheeselibrary.widget.AdjustableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by ajitesh on 29/1/16.
 */
public class TabletDetailFragment extends Fragment {

    private String TAG="MovieDetailsTablet";
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    CollapsingToolbarLayout collapsingToolbar;
    Toolbar toolbar;
    String BASE_URL="https://api.themoviedb.org/3/movie/";
    String API_KEY;
    Movie movie;
    LinearLayout trailersLayout,reviewsLayout;
    Context context;
    View view;
    NetworkImageView headerMovie;
    AdjustableImageView poster;
    FloatingActionButton fab,fabShare;
    TextView stars,title,popularity,release,tvDetails;
    ImageLoader.ImageContainer imageContainer=null;
    String youtubeURL=null;

    RefreshGrid refreshGrid;
    public interface RefreshGrid{
        public void refreshFavGrid();
    }
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RefreshGrid) {
            refreshGrid = (RefreshGrid) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.movie=getArguments().getParcelable("movie");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_movie_detail, container, false);
        API_KEY=getActivity().getResources().getString(R.string.API_KEY);
        trailersLayout= (LinearLayout)view.findViewById(R.id.layoutTrailers);
        reviewsLayout=(LinearLayout)view.findViewById(R.id.layoutReviews);
        toolbar = (Toolbar) view.findViewById(R.id.anim_toolbar);
        context=getActivity();
        collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        headerMovie= (NetworkImageView) view.findViewById(R.id.headerMovie);
        poster=(AdjustableImageView)view.findViewById(R.id.moviePoster);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fabShare=(FloatingActionButton) view.findViewById(R.id.sharefab);
        stars=(TextView)view.findViewById(R.id.stars);
        title=(TextView)view.findViewById(R.id.titletext);
        popularity=(TextView)view.findViewById(R.id.popularity);
        release=(TextView)view.findViewById(R.id.release);
        tvDetails=(TextView)view.findViewById(R.id.tvDetails);
        showData();
        return  view;
    }
    public void showData(){
        collapsingToolbar.setTitle(movie.getTitle());

        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = movie.getTitle()+"-\n"+movie.getOverview();
                if(youtubeURL!=null)
                    shareBody=shareBody+"\nWatch Trailer- https://www.youtube.com/watch?v="+youtubeURL;
                else
                    Toast.makeText(context,"Trailer Unavailable",Toast.LENGTH_SHORT).show();
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, movie.getTitle());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share Movie/Trailer"));
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFavourite())
                    addToFav();
                else
                    remFromFav();

                if (refreshGrid != null)
                    refreshGrid.refreshFavGrid();
            }
        });
        stars.setText(movie.getRating() + "/10");
        title.setText(movie.getTitle());
        DecimalFormat df = new DecimalFormat("###.##");
        popularity.setText("Popularity - " + df.format(movie.getPopularity()) + " %");
        release.setText("Release Date - " + movie.getRelease());
        tvDetails.setText(movie.getOverview());
        if((isFavourite())&&(getImageBlob()!=null)) {
            poster.setImageBitmap(DbBitmapUtility.getImage(getImageBlob()));
        } else {
            imageLoader.get(movie.getImage(), ImageLoader.getImageListener(poster, R.drawable.loading, R.drawable.error));
            imageLoader.get(movie.getImage(), new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if(response.getBitmap()!=null)
                        imageContainer = response;
                }
                @Override
                public void onErrorResponse(VolleyError error) {
                    imageContainer = null;
                }
            });
        }
        headerMovie.setImageUrl(movie.getBackdrop(), imageLoader);
        if(isFavourite())
            fab.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.star_big_on));
        loadData();
    }
    public LinearLayout createTrailerLayout(String name, final String source)
    {
        LinearLayout linearLayout=new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setWeightSum(100f);
        linearLayout.setPadding(10, 10, 10, 10);
        AdjustableImageView adjustableImageView=new AdjustableImageView(context);
        adjustableImageView.setAdjustViewBounds(true);
        adjustableImageView.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 50f));
        adjustableImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + source)));
            }
        });
        linearLayout.addView(adjustableImageView);
        TextView textView=new TextView(context);
        textView.setText(name);
        textView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        textView.setPadding(20, 0, 0, 0);
        TableLayout.LayoutParams layoutParams=new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 50f);
        layoutParams.gravity=Gravity.CENTER;
        textView.setLayoutParams(layoutParams);
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        linearLayout.addView(textView);
        imageLoader.get("http://img.youtube.com/vi/" + source + "/0.jpg", ImageLoader.getImageListener(adjustableImageView, R.drawable.loading_trailer, R.drawable.error_trailer));
        return linearLayout;
    }
    public LinearLayout createReviewLayout(String author, final String content)
    {
        TableLayout.LayoutParams layoutParams=new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity=Gravity.CENTER;
        LinearLayout linearLayout=new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setPadding(10, 10, 10, 10);
        TextView textViewContent=new TextView(context);
        textViewContent.setText("\" " + content + " \"");
        textViewContent.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        textViewContent.setPadding(0, 10, 0, 10);
        textViewContent.setLayoutParams(layoutParams);
        textViewContent.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        linearLayout.addView(textViewContent);
        TextView textViewAuthor=new TextView(context);
        textViewAuthor.setText("-" + author);
        textViewAuthor.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        textViewAuthor.setPadding(0, 0, 20, 30);
        textViewAuthor.setLayoutParams(layoutParams);
        textViewAuthor.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        linearLayout.addView(textViewAuthor);
        return linearLayout;
    }
    public View getLineView()
    {
        View view=new View(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setBackgroundColor(Color.GRAY);
        view.setMinimumHeight(2);
        return  view;
    }

    public void loadData()
    {
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(BASE_URL + movie.getId() +"/trailers?api_key=" + API_KEY,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        view.findViewById(R.id.loadingIndicatorViewTrailers).setVisibility(View.GONE);
                        try {
                            JSONArray jsonArray=response.getJSONArray("youtube");
                            if(jsonArray.length()>0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if(i==0)
                                        youtubeURL=jsonObject.getString("source");
                                    trailersLayout.addView(createTrailerLayout(jsonObject.getString("name"), jsonObject.getString("source")));
                                    if((i+1)!=jsonArray.length())
                                        trailersLayout.addView(getLineView());
                                }
                            }
                            else
                            {
                                TextView textView=new TextView(context);
                                textView.setText("No trailers found");
                                textView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
                                textView.setPadding(10, 10, 10, 10);
                                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                textView.setTextSize(15f);
                                textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                                trailersLayout.addView(textView);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isConnected();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest);

        JsonObjectRequest jsonObjectRequestReviews=new JsonObjectRequest(BASE_URL + movie.getId() +"/reviews?api_key=" + API_KEY,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        view.findViewById(R.id.loadingIndicatorViewReviews).setVisibility(View.GONE);
                        try {
                            JSONArray jsonArray=response.getJSONArray("results");
                            if(jsonArray.length()>0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    reviewsLayout.addView(createReviewLayout(jsonObject.getString("author"), jsonObject.getString("content")));
                                    if((i+1)!=jsonArray.length())
                                        reviewsLayout.addView(getLineView());
                                }
                            }
                            else
                            {
                                TextView textView=new TextView(context);
                                textView.setText("No Reviews found");
                                textView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
                                textView.setPadding(10, 10, 10, 10);
                                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                textView.setTextSize(15f);
                                textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                                reviewsLayout.addView(textView);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isConnected();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjectRequestReviews);
    }

    public void isConnected()
    {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected)
        {
            Snackbar.make(view.findViewById(R.id.coordinatorMovieDetails), "Something Went Wrong", Snackbar.LENGTH_LONG)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadData();
                        }
                    })
                    .setDuration(Snackbar.LENGTH_INDEFINITE)
                    .show();
        }
        else
        {
            Snackbar.make(view.findViewById(R.id.coordinatorMovieDetails), "Please Connect to the Internet", Snackbar.LENGTH_LONG)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadData();
                        }
                    })
                    .setDuration(Snackbar.LENGTH_INDEFINITE)
                    .show();
        }
    }
    public void addToFav()
    {
        ContentValues contentValues=new ContentValues();
        contentValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE,movie.getTitle());
        contentValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW,movie.getOverview());
        contentValues.put(MoviesContract.MoviesEntry.COLUMN_BACKDROP,movie.getBackdrop());
        contentValues.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY,String.valueOf(movie.getPopularity()));
        contentValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER,movie.getImage());
        if(imageContainer!=null)
            contentValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_BLOB, DbBitmapUtility.getBytes(imageContainer.getBitmap()));
        contentValues.put(MoviesContract.MoviesEntry.COLUMN_RATING,String.valueOf(movie.getRating()));
        contentValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE,movie.getRelease());
        contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movie.getId());
        getActivity().getContentResolver().insert(MoviesContract.MoviesEntry.CONTENT_URI, contentValues);
        fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.star_big_on));
        Toast.makeText(getActivity(), "Added to favourites. . .", Toast.LENGTH_SHORT).show();
    }
    public void remFromFav(){
        getActivity().getContentResolver().delete(MoviesContract.MoviesEntry.CONTENT_URI, MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + "=?", new String[]{String.valueOf(movie.getId())});
        fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.star_big_off));
        Toast.makeText(getActivity(),"Removed from favourites. . .",Toast.LENGTH_SHORT).show();
    }
    public boolean isFavourite(){
        Cursor cursor=getActivity().getContentResolver().query(MoviesContract.MoviesEntry.CONTENT_URI, null, MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + "=?", new String[]{String.valueOf(movie.getId())}, null);
        if(cursor.getCount()>0)
            return true;
        return false;
    }
    public byte[] getImageBlob(){
        Cursor cursor=getActivity().getContentResolver().query(MoviesContract.MoviesEntry.CONTENT_URI, new String[]{MoviesContract.MoviesEntry.COLUMN_POSTER_BLOB}, MoviesContract.MoviesEntry.COLUMN_MOVIE_ID+"=?", new String[]{String.valueOf(movie.getId())}, null);
        try{
            cursor.moveToNext();
            return cursor.getBlob(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_BLOB));
        }catch (Exception e)
        {
            Log.d(TAG,e.toString());
            return null;
        }
    }
}
