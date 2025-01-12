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

import org.gtfs.reader.GtfsSimpleDao;
import org.gtfs.reader.model.Stop;
import org.gtfs.reader.model.StopTime;
import org.gtfs.reader.model.Trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hka.ws2425.MainActivity;
import de.hka.ws2425.R;

public class TripStopsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_stops);

        // UI-Elemente
        TextView headerTextView = findViewById(R.id.trip_stops_header);
        ListView stopsListView = findViewById(R.id.trip_stops_list_view);

        // Empfange die Trip-ID aus dem Intent
        String tripId = getIntent().getStringExtra("TRIP_ID");
        Log.d("TripStopsActivity", "Empfangene Trip-ID: " + tripId);

        if (tripId != null) {
            // GTFS-Daten laden
            GtfsSimpleDao dao = GtfsLoader.getDao();

            // Trip-Objekt suchen
            Trip trip = null;
            for (Trip t : dao.getTrips()) {
                if (t.getTripId().equals(tripId)) {
                    trip = t;
                    break;
                }
            }

            if (trip != null) {
                // Überschrift mit Fahrtziel setzen
                String headerText = "Haltestellen der Fahrt nach: " + trip.getHeadsign();
                headerTextView.setText(headerText);

                // StopTimes für die Trip-ID abrufen
                List<StopTime> stopTimes = new ArrayList<>();
                for (StopTime stopTime : ((GtfsSimpleDao) dao).getStopTimes()) {
                    if (stopTime.getTripId().equals(tripId)) {
                        stopTimes.add(stopTime);
                    }
                }


                // HashMap für Stop-Daten erstellen
                Map<String, Stop> stopsMap = new HashMap<>();
                for (Stop stop : dao.getStops()) { // Iteriere durch alle Stops
                    stopsMap.put(stop.getId(), stop); // Verknüpfe die Stop-ID mit dem Stop-Objekt
                }

                // Haltestellen-Daten vorbereiten
                List<String> stopInfo = new ArrayList<>();
                for (StopTime stopTime : stopTimes) {
                    String stopId = stopTime.getStopId(); // Hole die Stop-ID aus StopTime
                    Stop stop = stopsMap.get(stopId); // Abrufen des Stop-Objekts über die HashMap
                    if (stop != null) {
                        String stopName = stop.getName();
                        String departureTime = stopTime.getDepartureTime();
                        stopInfo.add(departureTime + " - " + stopName);
                    }
                }

                // ListView mit den Haltestellen-Daten füllen
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        stopInfo
                );
                stopsListView.setAdapter(adapter);
            } else {
                Log.e("TripStopsActivity", "Kein Trip gefunden für tripId: " + tripId);
            }
        } else {
            Log.e("TripStopsActivity", "Trip-ID ist null.");
        }

        Button backToMapButton = findViewById(R.id.back_to_map_button);
        backToMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripStopsActivity.this, MainActivity.class); // Name der Karten-Activity
                startActivity(intent);
//                finish(); // Optional, um die aktuelle Activity zu schließen
            }
        });
    }
}
