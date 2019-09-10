package com.basement.panosx2.moviedatabase.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basement.panosx2.moviedatabase.Helpers.DBStuff;
import com.basement.panosx2.moviedatabase.Helpers.RequestQueueSingleton;
import com.basement.panosx2.moviedatabase.Objects.Saved;
import com.basement.panosx2.moviedatabase.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Details extends AppCompatActivity {

    private static Context context;

    private Toolbar toolbar;
    private Button back;

    private ImageView poster, bookmark, trailer;
    private TextView title, genre, type, rate, summary;

    private static int _id;
    private static String _type;

    private String posterURI;
    private String trailerURI;

    private DBStuff dbStuff;
    private static boolean isInWatchlist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        context = Details.this;
        dbStuff = new DBStuff(context);

        toolbar = findViewById(R.id.toolbar);
        back = toolbar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        poster = findViewById(R.id.poster);
        bookmark = findViewById(R.id.bookmark);
        trailer = findViewById(R.id.trailer);
        title = findViewById(R.id.title);
        genre = findViewById(R.id.genre);
        type = findViewById(R.id.type);
        rate = findViewById(R.id.rate);
        summary = findViewById(R.id.summary);

        _id = getIntent().getIntExtra("id", 0);
        _type = getIntent().getStringExtra("type").toLowerCase();
        title.setText(getIntent().getStringExtra("title"));
        type.setText(_type.equals("tv")?_type.toUpperCase():"Movie");

        posterURI = getIntent().getStringExtra("poster");
        Picasso.get().load(posterURI).fit().into(poster);

        poster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog imageDialog = new Dialog(context);
                imageDialog.setCanceledOnTouchOutside(true);
                imageDialog.setCancelable(true);
                imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                imageDialog.setContentView(R.layout.dialog_image);

                ImageView imageView = imageDialog.findViewById(R.id.imageView);

                Picasso
                        .get()
                        .load(posterURI)
                        .into(imageView);

                Button close = imageDialog.findViewById(R.id.close);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageDialog.cancel();
                    }
                });

                imageDialog.show();
            }
        });

        getDetails();

        dbStuff.open();
        if (dbStuff.isInWatchlist(_id)) {
            isInWatchlist = true;
            Picasso.get().load(R.drawable.remove).fit().into(bookmark);
        }
        else {
            isInWatchlist = false;
            Picasso.get().load(R.drawable.add).fit().into(bookmark);
        }
        dbStuff.close();

        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbStuff.open();
                if (!isInWatchlist) {
                    dbStuff.addToWatchlist(context, new Saved(_id, posterURI, title.getText().toString(), _type, summary.getText().toString().substring(0,55) + "..."));
                    Picasso.get().load(R.drawable.remove).fit().into(bookmark);
                    isInWatchlist = true;
                }
                else {
                    dbStuff.removeFromWatchList(context, _id);
                    Picasso.get().load(R.drawable.add).fit().into(bookmark);
                    isInWatchlist = false;
                }
                dbStuff.close();
            }
        });
    }

    private void getDetails() {
        String url = "https://api.themoviedb.org/3/" + _type + "/" + _id+"?api_key=6b2e856adafcc7be98bdf0d8b076851c";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        JSONArray jsonArrayGenres = jsonObject.getJSONArray("genres");
                        genre.setText(jsonArrayGenres.getJSONObject(0).getString("name"));

                        rate.setText(jsonObject.getInt("vote_average") +
                                Html.fromHtml("&#9733;").toString() +
                                " /" +
                                jsonObject.getInt("vote_count") +
                                " users");
                        summary.setText(jsonObject.getString("overview"));

                        getTrailer();
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
                return map;
            }
        };
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void getTrailer() {
        String url = "https://api.themoviedb.org/3/" + _type + "/" + _id + "/videos?api_key=6b2e856adafcc7be98bdf0d8b076851c&language=en-US";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        JSONObject tempJsonObject = jsonArray.getJSONObject(0);

                        if (tempJsonObject.getString("site").equals("YouTube")) {
                            trailerURI = tempJsonObject.getString("key");
                        }
                        else trailerURI = "";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (trailerURI.isEmpty()) Picasso.get().load(R.drawable.no_trailer).fit().into(trailer);

                trailer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (trailerURI.isEmpty()) Toast.makeText(context, "Trailer is not available", Toast.LENGTH_SHORT).show();
                        else {
                            final Dialog d = new Dialog(context);
                            d.setCanceledOnTouchOutside(true);
                            d.setCancelable(true);
                            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            d.setContentView(R.layout.dialog_trailer);
                            d.setTitle(title.getText().toString());

                            final YouTubePlayerView videoView = d.findViewById(R.id.videoView);
                            getLifecycle().addObserver(videoView);

                            videoView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                                @Override
                                public void onReady(YouTubePlayer youTubePlayer) {
                                    youTubePlayer.loadVideo(trailerURI, 0);
                                }
                            });

                            d.show();
                        }
                    }
                });
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
                //map.put("language", "en-US");
                return map;
            }
        };
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(context, Search.class));
    }
}
