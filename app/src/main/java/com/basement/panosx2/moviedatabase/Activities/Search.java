package com.basement.panosx2.moviedatabase.Activities;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basement.panosx2.moviedatabase.Adapters.ResultAdapter;
import com.basement.panosx2.moviedatabase.Adapters.SuggestionAdapter;
import com.basement.panosx2.moviedatabase.Adapters.WatchlistAdapter;
import com.basement.panosx2.moviedatabase.Helpers.DBStuff;
import com.basement.panosx2.moviedatabase.Helpers.RequestQueueSingleton;
import com.basement.panosx2.moviedatabase.Objects.Result;
import com.basement.panosx2.moviedatabase.Objects.Saved;
import com.basement.panosx2.moviedatabase.Objects.Suggestion;
import com.basement.panosx2.moviedatabase.R;
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Search extends AppCompatActivity {

    private static final String TAG = "Search";
    private static Context context;
    private SearchView searchView;

    private RecyclerView suggestionsRecyclerView;
    private List<Suggestion> suggestions;
    private SuggestionAdapter suggestionAdapter;

    private RelativeLayout resultsLayout;
    private TextView resultsText;
    private RecyclerView resultsRecyclerView;
    private List<Result> results;
    private ResultAdapter resultAdapter;
    private Button clear, next, previous;
    private TextView pageText;
    private static int _pages;
    private static int currentPage = 1;

    private NestedScrollView watchlistLayout;
    private BottomSheetBehavior bottomSheetBehavior;
    private MultiSnapRecyclerView watchlistRecyclerView;
    private List<Saved> watchlist;
    private WatchlistAdapter watchlistAdapter;
    private WatchlistAdapter watchlistDialogAdapter;
    private TextView watchlistText;
    private Button showAll;

    private DBStuff dbStuff;

    private int INTERNET_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        context = Search.this;
        dbStuff = new DBStuff(context);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            if (!isInternetAvailable()) Toast.makeText(context, "Not connected to internet", Toast.LENGTH_LONG).show();
        }
        else requestInternetPermission();

        searchView = findViewById(R.id.searchView);
        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView);
        resultsLayout = findViewById(R.id.resultsLayout);
        resultsText = findViewById(R.id.resultsText);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        clear = findViewById(R.id.clear);
        watchlistLayout = findViewById(R.id.watchlistLayout);
        watchlistText = findViewById(R.id.watchlistText);
        showAll = findViewById(R.id.showAll);
        pageText = findViewById(R.id.pageText);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);

        suggestions = new ArrayList<>();
        suggestionAdapter = new SuggestionAdapter(context, suggestions);
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        suggestionsRecyclerView.setAdapter(suggestionAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    suggestionsRecyclerView.setVisibility(View.INVISIBLE);
                    resultsLayout.setVisibility(View.VISIBLE);
                    resultsText.setText("Results for '"+ query +"' :");

                    getResults(query, 1);

                    searchView.clearFocus();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    suggestionsRecyclerView.setVisibility(View.VISIBLE);

                    getSuggestions(newText);
                }
                else {
                    suggestionsRecyclerView.setVisibility(View.INVISIBLE);
                    suggestions.clear();
                    suggestionAdapter.notifyDataSetChanged();
                }

                return true;
            }
        });

        results = new ArrayList<>();
        resultAdapter = new ResultAdapter(context, results);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        resultsRecyclerView.setAdapter(resultAdapter);

        /*
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchView.getQuery().toString().isEmpty()) {
                    resultsLayout.setVisibility(View.VISIBLE);
                    resultsText.setText("Results for '"+ searchView.getQuery().toString() +"' :");

                    getResults(searchView.getQuery().toString(), 1);
                }
            }
        });
        */

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery("", false);
                resultsLayout.setVisibility(View.INVISIBLE);
                results.clear();
                resultAdapter.notifyDataSetChanged();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((currentPage + 1) <= _pages) {
                    getResults(searchView.getQuery().toString(), currentPage + 1);
                    previous.setVisibility(View.VISIBLE);
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage != 1) { //useless condition
                    getResults(searchView.getQuery().toString(), currentPage - 1);
                }
            }
        });

        getWatchlist();
    }

    private void getSuggestions(final String newText) {
        suggestions.clear();
        suggestionAdapter.notifyDataSetChanged();

        String url = "https://api.themoviedb.org/3/search/multi?api_key=6b2e856adafcc7be98bdf0d8b076851c&query=" + newText;

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("results");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject tempJsonObject = jsonArray.getJSONObject(i);
                            int id = tempJsonObject.getInt("id");
                            String title, year;
                            String type = tempJsonObject.getString("media_type");
                            if (type.equals("tv")) {
                                type = type.toUpperCase();
                                title = tempJsonObject.getString("name");
                                year = tempJsonObject.getString("first_air_date");
                            }
                            else {
                                type = "Movie";
                                title = tempJsonObject.getString("title");
                                year = tempJsonObject.getString("release_date");
                            }
                            if (!year.isEmpty()) {
                                year = " (" + year.substring(0, 4) + ")";
                                title += year;
                            }
                            String poster = "https://image.tmdb.org/t/p/w300_and_h450_bestv2" + tempJsonObject.getString("poster_path");

                            suggestions.add(new Suggestion(id, poster, title, type));
                            suggestionAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                //map.put("api_key", "6b2e856adafcc7be98bdf0d8b076851c");
                //map.put("query", newText);
                return map;
            }
        };
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void getResults(final String query, final int toGetpage) {
        currentPage = toGetpage;

        results.clear();
        resultAdapter.notifyDataSetChanged();

        String url = "https://api.themoviedb.org/3/search/multi?api_key=6b2e856adafcc7be98bdf0d8b076851c&query=" + query + "&page=" + toGetpage;

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        if (toGetpage == 1) {
                            _pages = jsonObject.getInt("total_pages");
                            previous.setVisibility(View.INVISIBLE);

                            if (_pages > 1) next.setVisibility(View.VISIBLE);
                            else next.setVisibility(View.INVISIBLE);
                        }
                        else if (toGetpage == _pages) {
                            next.setVisibility(View.INVISIBLE);
                            previous.setVisibility(View.VISIBLE);
                        }

                        pageText.setText(toGetpage + " /" + _pages);

                        JSONArray jsonArray = jsonObject.getJSONArray("results");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject tempJsonObject = jsonArray.getJSONObject(i);

                            int id = tempJsonObject.getInt("id");
                            String title, year;
                            String type = tempJsonObject.getString("media_type");
                            if (type.equals("tv")) {
                                type = type.toUpperCase();
                                title = tempJsonObject.getString("name");
                                year = tempJsonObject.getString("first_air_date");
                            }
                            else {
                                type = "Movie";
                                title = tempJsonObject.getString("title");
                                year = tempJsonObject.getString("release_date");
                            }
                            if (!year.isEmpty()) {
                                year = " (" + year.substring(0, 4) + ")";
                                title += year;
                            }
                            int rate = tempJsonObject.getInt("vote_average");
                            String poster = "https://image.tmdb.org/t/p/w300_and_h450_bestv2" + tempJsonObject.getString("poster_path");

                            results.add(new Result(id, poster, title, type, rate));
                            resultAdapter.notifyDataSetChanged();
                        }

                        if (results.isEmpty()) pageText.setVisibility(View.GONE);
                        else pageText.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                //map.put("api_key", "6b2e856adafcc7be98bdf0d8b076851c");
                //map.put("query", query);
                //map.put("page", "" + toGetpage);
                return map;
            }
        };
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void getWatchlist() {
        watchlist = new ArrayList<>();
        dbStuff.open();
        watchlist = dbStuff.getWatchlist();
        dbStuff.close();

        if (watchlist.isEmpty()) {
            watchlistLayout.setVisibility(View.GONE);
            showAll.setVisibility(View.GONE);
        }
        else {
            watchlistLayout.setVisibility(View.VISIBLE);

            bottomSheetBehavior = BottomSheetBehavior.from(watchlistLayout);
            bottomSheetBehavior.setHideable(false);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            watchlistText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    else
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });

            watchlistLayout.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    Log.d("bottomsheet", "drag listener");
                    if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                        Log.d("bottomsheet", "drag ended");
                        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        else
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }

                    return true;
                }
            });

            watchlistRecyclerView = findViewById(R.id.watchlistRecyclerView);
            watchlistAdapter = new WatchlistAdapter(context, watchlist, false);
            watchlistRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            watchlistRecyclerView.setAdapter(watchlistAdapter);

            watchlistText.setText("Watchlist ("+ watchlist.size() +")");

            showAll.setVisibility(View.VISIBLE);
            showAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog d = new Dialog(context);
                    d.setCanceledOnTouchOutside(true);
                    d.setCancelable(true);
                    d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    d.setContentView(R.layout.dialog_watchlist);

                    final RecyclerView watchlistDialogRecyclerView = d.findViewById(R.id.watchlistRecyclerView);
                    watchlistDialogAdapter = new WatchlistAdapter(d.getContext(), watchlist, true);
                    watchlistDialogRecyclerView.setLayoutManager(new LinearLayoutManager(d.getContext()));
                    watchlistDialogRecyclerView.setAdapter(watchlistDialogAdapter);

                    final DBStuff dbStuff2 = new DBStuff(d.getContext());

                    final SearchView search = d.findViewById(R.id.search);
                    search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            if (!query.isEmpty()) {
                                watchlist.clear();
                                watchlistDialogAdapter.notifyDataSetChanged();

                                Log.d("DB", "search for: "+query);

                                dbStuff2.open();
                                watchlist = dbStuff2.getFilteredWatchList(query);
                                dbStuff2.close();

                                Log.d("DB", "watchlist items: "+watchlist.size());

                                watchlistDialogAdapter = new WatchlistAdapter(d.getContext(), watchlist, true);
                                watchlistDialogRecyclerView.setLayoutManager(new LinearLayoutManager(d.getContext()));
                                watchlistDialogRecyclerView.setAdapter(watchlistDialogAdapter);

                                search.clearFocus();
                            }
                            else {
                                watchlist.clear();
                                watchlistDialogAdapter.notifyDataSetChanged();

                                dbStuff2.open();
                                watchlist = dbStuff2.getWatchlist();
                                dbStuff2.close();

                                watchlistDialogAdapter = new WatchlistAdapter(d.getContext(), watchlist, true);
                                watchlistDialogRecyclerView.setLayoutManager(new LinearLayoutManager(d.getContext()));
                                watchlistDialogRecyclerView.setAdapter(watchlistDialogAdapter);

                                search.clearFocus();
                            }
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            if (newText.isEmpty()) { //show all
                                watchlist.clear();
                                watchlistDialogAdapter.notifyDataSetChanged();

                                dbStuff2.open();
                                watchlist = dbStuff2.getWatchlist();
                                dbStuff2.close();

                                watchlistDialogAdapter = new WatchlistAdapter(d.getContext(), watchlist, true);
                                watchlistDialogRecyclerView.setLayoutManager(new LinearLayoutManager(d.getContext()));
                                watchlistDialogRecyclerView.setAdapter(watchlistDialogAdapter);
                            }
                            return true;
                        }
                    });

                    d.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Toast.makeText(d.getContext(), "press Back to exit Watchlist", Toast.LENGTH_SHORT).show();
                        }
                    });

                    d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            getWatchlist();
                        }
                    });

                    d.show();
                }
            });
        }
    }

    private void requestInternetPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Internet permission is needed for using the app")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Search.this,
                                    new String[] {Manifest.permission.INTERNET}, INTERNET_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
        else ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET}, INTERNET_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == INTERNET_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!isInternetAvailable()) Toast.makeText(context, "Not connected to internet", Toast.LENGTH_LONG).show();
            }
            else Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        //close app
    }
}
