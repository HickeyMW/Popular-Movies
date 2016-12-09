package com.wickeddevs.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wickeddevs.popularmovies.data.Trailer;

import java.util.ArrayList;

public class TrailerRVA extends RecyclerView.Adapter<TrailerRVA.TrailerVH> {

    ArrayList<Trailer> trailers;
    OnTrailerSelectedListener listener;

    public TrailerRVA(ArrayList<Trailer> trailers, OnTrailerSelectedListener listener) {
        this.trailers = trailers;
        this.listener = listener;
    }

    @Override
    public TrailerVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_trailer, parent, false);
        return new TrailerVH(view);
    }

    @Override
    public void onBindViewHolder(TrailerVH holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    class TrailerVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvName;

        public TrailerVH(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            itemView.setOnClickListener(this);
        }

        public void bind(int listIndex) {
            tvName.setText(trailers.get(listIndex).name);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            listener.onTrailerSelected(trailers.get(position).url);
        }
    }

    public interface OnTrailerSelectedListener {
        void onTrailerSelected(String url);
    }
}
