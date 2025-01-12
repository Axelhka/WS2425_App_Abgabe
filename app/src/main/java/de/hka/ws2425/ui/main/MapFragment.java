package de.hka.ws2425.ui.main;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.gtfs.reader.GtfsSimpleDao;
import org.gtfs.reader.model.Stop;
import org.gtfs.reader.model.StopTime;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.hka.ws2425.R;
import android.content.Intent;

public class MapFragment extends Fragment {

    private MapViewModel mViewModel;

    private MapView mapView;

    private List<Stop> stopList = new ArrayList<>();

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        // TODO: Use the ViewModel


    }
    private void displayStopsOnMap(List<Stop> stopList) {

        if (stopList == null || stopList.isEmpty()) {
            System.out.println("StopList ist leer oder null.");
            if (stopList == null) {
                System.out.println("stopList ist null.");
            } else {
                System.out.println("stopList hat Größe: " + stopList.size());
            }
        }

        System.out.println("DisplayStopsonMap wurde gestartet, StopList:" + stopList);

        for (Stop stop : stopList) {
            double latitude = Double.parseDouble(stop.getLatitude());
            double longitude = Double.parseDouble(stop.getLongitude());

            GeoPoint stopLocation = new GeoPoint(latitude, longitude);

            // Marker erstellen
            Marker marker = new Marker(mapView);
            marker.setPosition(stopLocation);
            marker.setTitle(stop.getName());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//            System.out.println("Marker hat Daten?" + marker.getPosition());

            // Marker-Click-Event
            marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
                Intent intent = new Intent(getActivity(), DeparturesActivity.class);
                intent.putExtra("STOP_ID", stop.getId()); // Stop-ID übergeben
                startActivity(intent);
                return true; // Event wird konsumiert
            });


            // Marker zur Karte hinzufügen
            mapView.getOverlays().add(marker);
        }

        mapView.invalidate(); // Karte aktualisieren
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        this.mapView = root.findViewById(R.id.mapView);

        XYTileSource mapServer = new XYTileSource("MapServer",
                8,
                20,
                256,
                ".png",
                new String[]{"https://tileserver.svprod01.app/styles/default/"}
        );

        String authorizationString = this.getMapServerAuthorizationString(
                "ws2223@hka",
                "LeevwBfDi#2027"
        );

        Configuration
                .getInstance()
                .getAdditionalHttpRequestProperties()
                .put("Authorization", authorizationString);

        this.mapView.setTileSource(mapServer);

        GeoPoint startPoint = new GeoPoint(48.998695, 8.803826);

        IMapController mapController = this.mapView.getController();
        mapController.setZoom(14.0);
        mapController.setCenter(startPoint);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
        };

        if (!(ContextCompat.checkSelfPermission( this.getContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED)){
            new AlertDialog.Builder(this.getContext()).setTitle("Permissions Request")
                    .setMessage("Diese App benötigt die präzisen Standortdaten um die Haltestelle ihres Busses anzuzeigen.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        Permissions.check(this.getContext(), permissions, null, null, new PermissionHandler() {
                            @Override
                            public void onGranted() {
                                setupLocationListener();
                            }
                            @Override
                            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                                super.onDenied(context, deniedPermissions);
                            }
                        });

                    })
                    .setCancelable(false)
                    .create().show();
        }


        displayStopsOnMap(stopList);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("MapFragment", "onViewCreated wurde aufgerufen.");

        // GTFS-Datenparser initialisieren
        GtfsSimpleDao dao = StopDataParser.getInstance(this);
        if (dao != null) {
            Log.d("MapFragment", "GtfsSimpleDao erfolgreich initialisiert.");
            Log.d("MapFragment", "Anzahl der Stops: " + dao.getStops().size());


            // stopList mit den geladenen Stops befüllen
            stopList = dao.getStops(); // Stops aus GtfsSimpleDao holen

            // GtfsLoader initialisieren
            GtfsLoader.initialize(dao);

            Log.d("MapFragment", "StopList wurde mit " + stopList.size() + " Einträgen befüllt.");
        } else {
            Log.e("MapFragment", "GtfsSimpleDao konnte nicht initialisiert werden.");
        }

        if (dao.getStopTimes() != null) {
            Log.d("MapFragment", "StopTimes geladen: " + dao.getStopTimes().size());
//            for (StopTime stopTime : dao.getStopTimes()) {
//                Log.d("MapFragment", "StopTime: StopId=" + stopTime.getStopId() +
//                        ", DepartureTime=" + stopTime.getDepartureTime());
//            }
        }

//        for (Stop stop : stopList) {
//            Log.d("MapFragment", "Stop: " + stop.getId() + ", Lat: " + stop.getLatitude() + ", Lon: " + stop.getLongitude());
//        }



        // Stops auf der Karte anzeigen
        displayStopsOnMap(stopList);
    }



    @SuppressLint("MissingPermission")
    private void setupLocationListener()
    {
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                GeoPoint startPoint = new GeoPoint(latitude, longitude);

                IMapController mapController = mapView.getController();
                mapController.setCenter(startPoint);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        LocationManager locationManager = (LocationManager) this.getContext().getSystemService(
                Context.LOCATION_SERVICE
        );

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000,
                10,
                locationListener
        );
    }

    private String getMapServerAuthorizationString(String username, String password)
    {
        String authorizationString = String.format("%s:%s", username, password);
        return "Basic " + Base64.encodeToString(authorizationString.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }
}