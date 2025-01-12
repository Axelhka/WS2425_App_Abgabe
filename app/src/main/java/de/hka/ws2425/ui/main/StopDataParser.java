package de.hka.ws2425.ui.main;

import org.gtfs.reader.GtfsReader;
import org.gtfs.reader.GtfsSimpleDao;

import java.io.File;
import java.io.IOException;

public class StopDataParser {
    private static GtfsSimpleDao instance; // Singleton-Instanz

    // Methode zum Abrufen der Singleton-Instanz
    public static GtfsSimpleDao getInstance(MapFragment context) {
        if (context == null || context.getActivity() == null) {
            throw new IllegalArgumentException("Ungültiger Kontext: context oder activity ist null.");
        }

        if (instance == null) {
            synchronized (StopDataParser.class) {
                if (instance == null) { // Doppelte Prüfung für Thread-Sicherheit
                    instance = new GtfsSimpleDao();
                    initializeGtfsReader(context, instance);
                }
            }
        }
        return instance;
    }

    // Initialisierung des GTFS-Readers
    private static void initializeGtfsReader(MapFragment context, GtfsSimpleDao dao) {
        File gtfsInputFile = new File(context.getActivity().getFilesDir(), "/gtfs-hka-s24.zip");

        if (!gtfsInputFile.exists()) {
            System.out.println("GTFS-Datei nicht gefunden: " + gtfsInputFile.getAbsolutePath());
            return;
        }

        GtfsReader gtfsReader = new GtfsReader();
        gtfsReader.setDataAccessObject(dao);

        try {
            gtfsReader.read(gtfsInputFile.getAbsolutePath());
            if (dao.getStops().isEmpty()) {
                System.out.println("Keine Stop-Daten in der GTFS-Datei gefunden.");
            } else {
                System.out.println("Anzahl der Stop-Daten: " + dao.getStops().size());
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Lesen der GTFS-Datei: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



