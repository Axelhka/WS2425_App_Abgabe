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

import androidx.annotation.RequiresApi;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.hka.ws2425.MainActivity;
import de.hka.ws2425.R;

public class DeparturesActivity extends AppCompatActivity {

    private GtfsSimpleDao dao;

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departures);

        dao = GtfsLoader.getDao();
        if (dao == null) {
            Log.e("DeparturesActivity", "GtfsSimpleDao ist null. Daten konnten nicht geladen werden.");
        }

        ListView departuresListView = findViewById(R.id.departures_list_view);
        TextView headerTextView = findViewById(R.id.departures_header);

        String stopId = getIntent().getStringExtra("STOP_ID");
        Log.d("DeparturesActivity", "Empfangene stopId: " + stopId);

        if (stopId != null) {
            Stop stop = null;
            for (Stop s : dao.getStops()) {
                if (s.getId().equals(stopId)) {
                    stop = s;
                    break;
                }
            }

            String stopName = (stop != null) ? stop.getName() : "Unbekannte Haltestelle";
            headerTextView.setText("Nächste Abfahrten ab " + stopName);

            DepartureDataParser departureDataParser = new DepartureDataParser(dao);
            List<StopTime> departures = departureDataParser.getDeparturesForStop(stopId);
            List<String> departureInfo = new ArrayList<>();

            Map<String, Trip> tripsMap = new HashMap<>();
            for (Trip trip : dao.getTrips()) {
                tripsMap.put(trip.getTripId(), trip);
            }

            Map<String, Route> routesMap = new HashMap<>();
            for (Route route : dao.getRoutes()) {
                routesMap.put(route.getId(), route);
            }

            LocalDate today = LocalDate.now();
            Map<String, String> tripToServiceMap = new HashMap<>();
            for (Trip trip : dao.getTrips()) {
                tripToServiceMap.put(trip.getTripId(), trip.getServiceId());
            }

            List<CalendarDate> calendarDates = loadCalendarDatesFromZip();
            List<StopTime> filteredDepartures = new ArrayList<>();

            for (StopTime stopTime : departures) {
                String tripId = stopTime.getTripId();
                String serviceId = tripToServiceMap.get(tripId);
                if (serviceId != null && isServiceActiveOnDate(serviceId, today, calendarDates)) {
                    filteredDepartures.add(stopTime);
                }
            }

            for (StopTime stopTime : filteredDepartures) {
                String tripId = stopTime.getTripId();
                Trip trip = tripsMap.get(tripId);
                if (trip != null) {
                    String time = stopTime.getDepartureTime();
                    String routeId = trip.getRouteId();
                    Route route = routesMap.get(routeId);

                    String routeName = (route != null && route.getShortName() != null) ? route.getShortName() : "Unbekannte Linie";
                    String tripInfo = trip.getHeadsign();

                    departureInfo.add(time + " - " + routeName + " - " + tripInfo);
                }
            }

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
        backToMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(DeparturesActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean isServiceActiveOnDate(String serviceId, LocalDate date, List<CalendarDate> calendarDates) {
        Log.d("isServiceActiveOnDate", "Prüfung, ob Service aktiv ist: serviceId=" + serviceId + ", date=" + date);
        String dateString = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Log.d("isServiceActiveOnDate", "Umgewandeltes Datum: " + dateString);

        for (CalendarDate calendarDate : calendarDates) {
            Log.d("isServiceActiveOnDate", "Überprüfe CalendarDate: serviceId=" + calendarDate.getServiceId() + ", date=" + calendarDate.getDate() + ", exceptionType=" + calendarDate.getExceptionType());

            if (calendarDate.getServiceId().equals(serviceId) && calendarDate.getDate().equals(dateString)) {
                boolean isActive = calendarDate.getExceptionType() == 1;
                Log.d("isServiceActiveOnDate", "Gefunden: serviceId=" + serviceId + " ist aktiv: " + isActive);
                return isActive;
            }
        }

        Log.d("isServiceActiveOnDate", "Kein aktiver Service gefunden für serviceId=" + serviceId + ", date=" + date);
        return false;
    }

    private List<CalendarDate> loadCalendarDatesFromZip() {
        List<CalendarDate> calendarDates = new ArrayList<>();
        String zipFileName = "gtfs-hka-s24.zip";
        String targetFileName = "calendar_dates.txt";

        try (InputStream zipStream = getAssets().open(zipFileName);
             ZipInputStream zis = new ZipInputStream(zipStream)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(targetFileName)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
                    String line;
                    reader.readLine(); // Kopfzeile überspringen
                    while ((line = reader.readLine()) != null) {
                        String[] fields = line.split(",");
                        if (fields.length == 3) {
                            String serviceId = fields[0];
                            String date = fields[1];
                            int exceptionType = Integer.parseInt(fields[2]);
                            calendarDates.add(new CalendarDate(serviceId, date, exceptionType));
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            Log.e("DeparturesActivity", "Fehler beim Laden der calendar_dates.txt aus dem ZIP: " + e.getMessage());
        }

        return calendarDates;
    }

    public static class CalendarDate {
        private final String serviceId;
        private final String date;
        private final int exceptionType;

        public CalendarDate(String serviceId, String date, int exceptionType) {
            this.serviceId = serviceId;
            this.date = date;
            this.exceptionType = exceptionType;
        }

        public String getServiceId() {
            return serviceId;
        }

        public String getDate() {
            return date;
        }

        public int getExceptionType() {
            return exceptionType;
        }
    }
}





