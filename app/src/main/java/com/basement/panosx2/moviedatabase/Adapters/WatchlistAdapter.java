package com.basement.panosx2.moviedatabase.Adapters;

/*
 * Created by panos on 6/9/2019
 */

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.basement.panosx2.moviedatabase.Activities.Details;
import com.basement.panosx2.moviedatabase.Objects.Saved;
import com.basement.panosx2.moviedatabase.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.ViewHolder> {
    private Context context;
    private List<Saved> watchlist;
    private static boolean isForDialog;

    public WatchlistAdapter(Context context, List<Saved> watchlist, boolean isForDialog) {
        this.context = context;
        this.watchlist = watchlist;
        this.isForDialog = isForDialog;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, description;

        public ViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.poster);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
        }
    }

    @Override
    public WatchlistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (!isForDialog) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_watchlist, parent, false);
        }
        else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_result, parent, false);
        }
        return new WatchlistAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final WatchlistAdapter.ViewHolder holder, final int position) {
        final Saved saved = watchlist.get(position);

        if (isForDialog) Log.d("DB", "creating dialog item with title: "+saved.getTitle());

        Picasso.get().load(saved.getPoster()).fit().into(holder.poster);
        holder.title.setText(saved.getTitle());
        holder.description.setText(saved.getDescription());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Details.class);
                intent.putExtra("id", saved.getId());
                intent.putExtra("poster", saved.getPoster());
                intent.putExtra("title", saved.getTitle());
                intent.putExtra("type", saved.getType());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return watchlist.size();
    }

    public long getItemId(int position) {
        return position;
    }
}
