package com.udacity.aruna.mypopularmovies;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;


import com.udacity.aruna.mypopularmovies.utilities.MovieUtilities;
import com.udacity.aruna.mypopularmovies.utilities.NetworkUtils;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * Created by Aruna
 */

public class MainActivityFragement extends Fragment {

    private ImageFlavourAdaptor imageAdaptor;
    String[] movieApiJsonData;
    private MovieApiTask task;
    private GridView gridView;
    String sortyBy= null;
    private List<MoviePosters> movieInfoList;
    String movieDataResponse;
    private ArrayList<MoviePosters> movieList;


    public static final String KEY_TITLE = "movie_title";
    public static final String KEY_POSTER = "movie_poster";
    public static final String KEY_OVERVIEW = "overview";
    public static final String KEY_VOTE = "average_vote";
    public static final String KEY_RELEASEDATE = "release_date";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        try {

            if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
                loadMovieData(getSortMethod());
                // movieList = new ArrayList<>(movieInfoList);

            } else {
                movieList = savedInstanceState.getParcelableArrayList("movies");
                loadMovieData(getSortMethod());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", movieList);
        super.onSaveInstanceState(outState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            // loadMovieData(getSortMethod());
            View rootView = inflater.inflate(R.layout.grid_layout, container, false);
            gridView = rootView.findViewById(R.id.flavors_grid);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View view, int position, long id) {

                    Context context = getActivity();
                    Class childActivity = MovieDetailsActivity.class;
                    Intent startChildActivityIntent = new Intent(context, childActivity);


                    MoviePosters m = (MoviePosters) parent.getItemAtPosition(position);
                    String title = m.getmTitle();

                    startChildActivityIntent.putExtra(KEY_TITLE, title);
                    startChildActivityIntent.putExtra(KEY_OVERVIEW, m.getOverView());
                    startChildActivityIntent.putExtra(KEY_VOTE, m.getVoteAverage());
                    startChildActivityIntent.putExtra(KEY_RELEASEDATE, m.getReleaseDate());
                    startChildActivityIntent.putExtra(KEY_POSTER, m.getPosterPath());
                    startActivity(startChildActivityIntent);

                }

            });


            return rootView;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_popular:
                sortyBy = "popularity.desc";
                updateSharedPreferences("popularity.desc");
                loadMovieData(getSortMethod());
                return true;

            case R.id.action_topRated:
                sortyBy = "average_vote.desc";
                updateSharedPreferences("vote_average.desc");
                loadMovieData(getSortMethod());
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    protected void loadMovieData(String sortBy) {
        task = new MovieApiTask(this);
        Context context = getActivity();
        if(NetworkUtils.isNetworkAvailable(context)) {
            task.execute(sortBy);
        }else{
            Toast.makeText(getActivity(), "oops! Network Connection is not available!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSharedPreferences(String sortMethod) {

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.action_sort), sortMethod);
        editor.commit();
    }

    private String getSortMethod() {
        String s = getString(R.string.action_sort);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(getString(R.string.action_sort), "popularity.desc");
    }



    public class MovieApiTask extends AsyncTask<String, Void, String[]> {

        MainActivityFragement container;

        public MovieApiTask(MainActivityFragement f) {
            this.container = f;
        }



        @Override
        protected String[] doInBackground(String... params) {
            try {
                String sort = params[0];



                URL movieSearchResults = NetworkUtils.buildUrl(sort);
                movieDataResponse = NetworkUtils
                        .getResponseFromHttpUrl(movieSearchResults);

                movieApiJsonData = MovieUtilities.getMoviesFromJson(movieDataResponse);
                movieInfoList = MovieUtilities.getMovieInfo(movieDataResponse);
                if (getActivity().getFragmentManager() != null) {
                    imageAdaptor = new ImageFlavourAdaptor(getActivity(), movieInfoList);
                }

                movieList = new ArrayList<>(movieInfoList);


                return movieApiJsonData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected void onPostExecute(String[] result) {
            try {

                gridView.setAdapter(imageAdaptor);
                imageAdaptor.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

}