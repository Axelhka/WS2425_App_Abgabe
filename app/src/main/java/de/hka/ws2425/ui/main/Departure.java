package de.hka.ws2425.ui.main;

public class Departure {
    private String route;
    private String departureTime;

    public Departure(String route, String departureTime) {
        this.route = route;
        this.departureTime = departureTime;
    }

    public String getRoute() {
        return route;
    }

    public String getDepartureTime() {
        return departureTime;
    }
}
