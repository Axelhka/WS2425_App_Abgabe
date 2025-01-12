package de.hka.ws2425.ui.main;

import org.gtfs.reader.GtfsSimpleDao;

public class GtfsLoader {
    private static GtfsSimpleDao dao;

    public static void initialize(GtfsSimpleDao newDao) {
        dao = newDao;
    }

    public static GtfsSimpleDao getDao() {
        if (dao == null) {
            throw new IllegalStateException("GtfsLoader wurde nicht initialisiert!");
        }
        return dao;
    }
}

