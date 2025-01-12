package de.hka.ws2425.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.gtfs.reader.GtfsSimpleDao;
import org.gtfs.reader.model.Route;
import org.gtfs.reader.model.Stop;
import org.gtfs.reader.model.StopTime;
import org.gtfs.reader.model.Trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hka.ws2425.MainActivity;
import de.hka.ws2425.R;

public class DeparturesActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departures);

        ListView departuresListView = findViewById(R.id.departures_list_view);
        TextView headerTextView = findViewById(R.id.departures_header);

        String stopId = getIntent().getStringExtra("STOP_ID");
        Log.d("DeparturesActivity", "Empfangene stopId: " + stopId);

        if (stopId != null) {
            // GTFS-Daten laden
            GtfsSimpleDao dao = GtfsLoader.getDao();

            // Haltestellen-Objekt aus der Liste suchen
            Stop stop = null;
            for (Stop s : dao.getStops()) {
                if (s.getId().equals(stopId)) {
                    stop = s;
                    break;
                }
            }

            // Haltestellennamen ermitteln
            String stopName = (stop != null) ? stop.getName() : "Unbekannte Haltestelle";

            // Überschrift setzen
            headerTextView.setText("Nächste Abfahrten ab " + stopName);

            // Departure-Daten verarbeiten (Rest deines Codes bleibt gleich)
            DepartureDataParser departureDataParser = new DepartureDataParser(dao);
            List<StopTime> departures = departureDataParser.getDeparturesForStop(stopId);
            List<String> departureInfo = new ArrayList<>();

            // HashMaps erstellen, um Trip- und Route-Daten effizient zu suchen
            Map<String, Trip> tripsMap = new HashMap<>();
            for (Trip trip : dao.getTrips()) {
                tripsMap.put(trip.getTripId(), trip);
            }

            Map<String, Route> routesMap = new HashMap<>();
            for (Route route : dao.getRoutes()) {
                routesMap.put(route.getId(), route);
            }

            String time = null;
            String tripInfo = null;
            for (StopTime stopTime : departures) {
                String tripId = stopTime.getTripId();
                Trip trip = tripsMap.get(tripId);
                if (trip != null) {
                    time = stopTime.getDepartureTime();
                    String routeId = trip.getRouteId();
                    Route route = routesMap.get(routeId);

                    String routeName = (route != null && route.getShortName() != null) ? route.getShortName() : "Unbekannte Linie";
                    tripInfo = trip.getHeadsign();

                    departureInfo.add(time + " - " + routeName + " - " + tripInfo);
                }
            }

            //ListView mit Abfahrten füllen
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    departureInfo
            );
            departuresListView.setAdapter(adapter);

            departuresListView.setOnItemClickListener((parent, view, position, id) -> {
                // Trip-ID aus der entsprechenden Abfahrt extrahieren
                StopTime selectedStopTime = departures.get(position); // Das entsprechende StopTime-Objekt
                String selectedTripId = selectedStopTime.getTripId();

                if (selectedTripId != null) {
                    // Neue Activity starten und Trip-ID übergeben
                    Intent intent = new Intent(DeparturesActivity.this, TripStopsActivity.class);
                    intent.putExtra("TRIP_ID", selectedTripId);
                    startActivity(intent);
                } else {
                    Log.e("DeparturesActivity", "Trip-ID ist null für Position: " + position);
                }
            });
        }

        Button backToMapButton = findViewById(R.id.back_to_map_button);
        backToMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeparturesActivity.this, MainActivity.class); // Name der Karten-Activity
                startActivity(intent);
//                finish(); // Optional, um die aktuelle Activity zu schließen
            }
        });
    }

}
