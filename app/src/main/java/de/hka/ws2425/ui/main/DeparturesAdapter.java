package de.hka.ws2425.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hka.ws2425.R;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import de.hka.ws2425.ui.main.Departure;


public class DeparturesAdapter extends RecyclerView.Adapter<DeparturesAdapter.DepartureViewHolder> {
    private List<Departure> departures;

    public DeparturesAdapter(List<Departure> departures) {
        this.departures = departures;
    }

    @NonNull
    @Override
    public DepartureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_departure, parent, false);
        return new DepartureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DepartureViewHolder holder, int position) {
        Departure departure = departures.get(position);
        holder.routeTextView.setText(departure.getRoute());
        holder.timeTextView.setText(departure.getDepartureTime());
    }

    @Override
    public int getItemCount() {
        return departures.size();
    }

    public static class DepartureViewHolder extends RecyclerView.ViewHolder {
        TextView routeTextView;
        TextView timeTextView;

        public DepartureViewHolder(@NonNull View itemView) {
            super(itemView);
            routeTextView = itemView.findViewById(R.id.tvRoute);
            timeTextView = itemView.findViewById(R.id.tvDepartureTime);
        }
    }
}
