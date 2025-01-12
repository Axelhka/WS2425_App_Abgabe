package de.hka.ws2425.ui.main;

import android.util.Log;

import org.gtfs.reader.GtfsSimpleDao;
import org.gtfs.reader.model.Stop;
import org.gtfs.reader.model.StopTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepartureDataParser {
    private final GtfsSimpleDao dao;

    public DepartureDataParser(GtfsSimpleDao dao) {
        this.dao = dao;
    }

    /**
     * Lädt die Abfahrtsdaten für eine bestimmte Haltestelle.
     * @param stopId Die ID der Haltestelle
     * @return Liste der Abfahrtszeiten (nach Uhrzeit sortiert)
     */
    public List<StopTime> getDeparturesForStop(String stopId) {
        Map<String, List<String>> parentStationMap = new HashMap<>();
        for (Stop stop : dao.getStops()) {
            String parentStation = stop.getParentStation();
            if (parentStation != null) {
                parentStationMap
                        .computeIfAbsent(parentStation, k -> new ArrayList<>())
                        .add(stop.getId());
            }
        }

        List<StopTime> departures = new ArrayList<>();



        // Alle stop_id's der Parent-Station holen
        List<String> stopIdsForParent = parentStationMap.getOrDefault(stopId, new ArrayList<>());

        // Filtere die StopTimes
        for (StopTime stopTime : dao.getStopTimes()) {
            if (stopIdsForParent.contains(stopTime.getStopId())) {
                departures.add(stopTime);
            }
        }

        Log.d("DepartureDataParser", "Anzahl gefundener StopTimes für Parent-StopId " + stopId + ": " + departures.size());

        return departures;
    }
}
