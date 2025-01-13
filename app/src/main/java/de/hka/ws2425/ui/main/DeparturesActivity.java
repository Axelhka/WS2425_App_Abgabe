package de.hka.ws2425.ui.main;

import android.content.Intent;
import android.os.Build;
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
import org.gtfs.reader.model.Calendar;
import org.gtfs.reader.model.CalendarDate;
import org.gtfs.reader.model.Route;
import org.gtfs.reader.model.Stop;
import org.gtfs.reader.model.StopTime;
import org.gtfs.reader.model.Trip;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

            // Departure-Daten verarbeiten
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

            // Filtere Abfahrten basierend auf dem aktuellen Tag
            LocalDate today = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                today = LocalDate.now();
            }


            // Mapping von tripId zu serviceId
            Map<String, String> tripToServiceMap = new HashMap<>();
            for (Trip trip : dao.getTrips()) {
                tripToServiceMap.put(trip.getTripId(), trip.getServiceId());
            }


            List<StopTime> filteredDepartures = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                today = LocalDate.now();
            }

            for (StopTime stopTime : departures) {
                String tripId = stopTime.getTripId();
                String serviceId = tripToServiceMap.get(tripId);

                if (serviceId != null && isServiceActiveOnDate(serviceId, today, dao.getCalendars())) {
                    filteredDepartures.add(stopTime);
                }
            }


            // Füge die gültigen Abfahrten zur Anzeige hinzu
            String time = null;
            String tripInfo = null;
            for (StopTime stopTime : filteredDepartures) {
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

            // ListView mit Abfahrten füllen
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    departureInfo
            );
            departuresListView.setAdapter(adapter);

            departuresListView.setOnItemClickListener((parent, view, position, id) -> {
                StopTime selectedStopTime = filteredDepartures.get(position);
                String selectedTripId = selectedStopTime.getTripId();

                if (selectedTripId != null) {
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

    public boolean isServiceActiveOnDate(String serviceId, LocalDate date, List<Calendar> calendars) {
        // Umwandlungsformat für Datum (angenommen: YYYYMMDD)
        DateTimeFormatter dateFormatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        }

        for (Calendar calendar : calendars) {
            // Service-ID prüfen (falls notwendig)
            // if (!calendar.getServiceId().equals(serviceId)) continue; // Falls `serviceId` existiert
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // Datumskonvertierung
                LocalDate startDate = null;
                startDate = LocalDate.parse(String.valueOf(calendar.getStartDate()), dateFormatter);
                LocalDate endDate = LocalDate.parse(String.valueOf(calendar.getEndDate()), dateFormatter);

                // Überprüfung, ob das Datum innerhalb des gültigen Bereichs liegt
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    DayOfWeek dayOfWeek = date.getDayOfWeek();

                    // Überprüfung des Wochentags
                    switch (dayOfWeek) {
                        case MONDAY:
                            if (calendar.getMonday()) return true;
                            break;
                        case TUESDAY:
                            if (calendar.getTuesday()) return true;
                            break;
                        case WEDNESDAY:
                            if (calendar.getWednesday()) return true;
                            break;
                        case THURSDAY:
                            if (calendar.getThursday()) return true;
                            break;
                        case FRIDAY:
                            if (calendar.getFriday()) return true;
                            break;
                        case SATURDAY:
                            if (calendar.getSaturday()) return true;
                            break;
                        case SUNDAY:
                            if (calendar.getSunday()) return true;
                            break;
                    }
                }
            }
        }
        return false; // Kein passender Eintrag gefunden
    }
}
