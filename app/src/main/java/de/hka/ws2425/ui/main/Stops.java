package de.hka.ws2425.ui.main;

public class Stops {
    private String id;
    private static String name;
    private static double latitude;
    private static double longitude;

    public Stops(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public static String getName() { return name; }
    public static double getLatitude() { return latitude; }
    public static double getLongitude() { return longitude;}
}