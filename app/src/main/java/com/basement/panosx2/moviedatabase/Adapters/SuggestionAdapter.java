package com.basement.panosx2.moviedatabase.Adapters;

/*
 * Created by panos on 4/9/2019
 */

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.basement.panosx2.moviedatabase.Activities.Details;
import com.basement.panosx2.moviedatabase.Objects.Suggestion;
import com.basement.panosx2.moviedatabase.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    private Context context;
    private List<Suggestion> suggestions;

    public SuggestionAdapter(Context context, List<Suggestion> suggestions) {
        this.context = context;
        this.suggestions = suggestions;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, type;

        public ViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.poster);
            title = view.findViewById(R.id.title);
            type = view.findViewById(R.id.type);
        }
    }

    @Override
    public SuggestionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SuggestionAdapter.ViewHolder holder, final int position) {
        final Suggestion suggestion = suggestions.get(position);

        Picasso.get().load(suggestion.getPoster()).fit().into(holder.poster);
        holder.title.setText(suggestion.getTitle());
        holder.type.setText(suggestion.getType());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Details.class);
                intent.putExtra("id", suggestion.getId());
                intent.putExtra("poster", suggestion.getPoster());
                intent.putExtra("title", suggestion.getTitle());
                intent.putExtra("type", suggestion.getType());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public long getItemId(int position) {
        return position;
    }
}
