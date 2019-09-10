package com.basement.panosx2.moviedatabase.Adapters;

/*
 * Created by panos on 4/9/2019
 */

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.basement.panosx2.moviedatabase.Activities.Details;
import com.basement.panosx2.moviedatabase.Objects.Result;
import com.basement.panosx2.moviedatabase.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private Context context;
    private List<Result> results;

    public ResultAdapter(Context context, List<Result> results) {
        this.context = context;
        this.results = results;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, type, rate;

        public ViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.poster);
            title = view.findViewById(R.id.title);
            type = view.findViewById(R.id.type);
            rate = view.findViewById(R.id.rate);
        }
    }

    @Override
    public ResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        return new ResultAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ResultAdapter.ViewHolder holder, final int position) {
        final Result result = results.get(position);

        Picasso.get().load(result.getPoster()).fit().into(holder.poster);
        holder.title.setText(result.getTitle());
        holder.type.setText(result.getType());
        holder.rate.setText(result.getRate() + Html.fromHtml("&#9733;").toString()); //na vrw white star

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Details.class);
                intent.putExtra("id", result.getId());
                intent.putExtra("poster", result.getPoster());
                intent.putExtra("title", result.getTitle());
                intent.putExtra("type", result.getType());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public long getItemId(int position) {
        return position;
    }
}