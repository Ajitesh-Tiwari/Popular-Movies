package com.ajitesh.android.popularmovies.MovieGridLayouts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ajitesh.android.popularmovies.Movie;
import com.ajitesh.android.popularmovies.R;
import com.ajitesh.android.popularmovies.Volley_Networking.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.srain.cube.views.GridViewWithHeaderAndFooter;


public class MovieGridFragment extends Fragment{

    private String API_KEY;
    private String TAG="MovieFragment";
    private String TAG_JSON = "json_obj_req";
    private String SORT_BY="popularity.desc";

    private int PAGE=1;
    private String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
    private ArrayList<Movie> movieList = new ArrayList<Movie>();
    private MovieGridAdapter adapter;
    GridViewWithHeaderAndFooter gridView;
    Context context=getContext();
    View view;
    View footerView;
    TextView footerText;
    private OnItemSelectedListener listener;

    public MovieGridFragment() {
        // Required empty public constructor
    }
    public interface OnItemSelectedListener {
        public void onMovieSelected(Movie movie);
    }
    public void updateDetail(Movie movie) {
        listener.onMovieSelected(movie);
    }
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnItemSelectedListener) {
            listener = (OnItemSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public void loadData(int tempPage)
    {
        if(PAGE==tempPage) {
            footerView.setEnabled(false);
            footerText.setText("Loading . . . ");
            footerText.setBackgroundColor(Color.DKGRAY);
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                    BASE_URL + "sort_by=" + SORT_BY + "&page=" + PAGE + "&api_key=" + API_KEY,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            footerView.setEnabled(true);
                            footerText.setText("Show More");
                            footerText.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            try {
                                JSONArray jsonArray = response.getJSONArray("results");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    movieList.add(new Movie(
                                            obj.getInt("id"),
                                            obj.getString("title"),
                                            obj.getString("poster_path"),
                                            obj.getString("overview"),
                                            (float) obj.getDouble("vote_average"),
                                            obj.getString("release_date"),
                                            (float) obj.getDouble("popularity"),
                                            obj.getString("backdrop_path")
                                    ));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            adapter.notifyDataSetChanged();
                            PAGE++;
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    footerView.setEnabled(true);
                    footerText.setText("Show More");
                    footerText.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    isConnected();
                }
            });

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq, TAG_JSON);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view=inflater.inflate(R.layout.fragment_movie_grid, container, false);
        API_KEY=getActivity().getResources().getString(R.string.API_KEY);
        gridView = (GridViewWithHeaderAndFooter) view.findViewById(R.id.gridview);

        adapter=new MovieGridAdapter(getActivity(), movieList);
        setGridViewHeaderAndFooter();
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateDetail(movieList.get(position));
            }
        });

        SORT_BY=getArguments().getString("sort_by");
        loadData(1);
        return view;
    }

    private void setGridViewHeaderAndFooter() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        footerView = layoutInflater.inflate(R.layout.layout_footer, null, false);
        footerText = (TextView)footerView.findViewById(R.id.text);
        footerText.setText("Show More");
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData(PAGE);
            }
        });
        gridView.addFooterView(footerView);
    }

    public void isConnected()
    {
        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected)
        {
            Snackbar.make(view, "Something Went Wrong", Snackbar.LENGTH_LONG)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadData(PAGE);
                        }
                    })
                    .setDuration(Snackbar.LENGTH_INDEFINITE)
                    .show();

        }
        else
        {
            Snackbar.make(view, "Please Connect to the Internet", Snackbar.LENGTH_LONG)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadData(PAGE);
                        }
                    })
                    .setDuration(Snackbar.LENGTH_INDEFINITE)
                    .show();
        }
    }


}