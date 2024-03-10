package com.example.memoriajatek;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class CountryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HashMap<String, LatLng> countryLatLngMap;
    private Marker selectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        initializeCountryLatLngs();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng budapest = new LatLng(47.4979, 19.0402);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(budapest, 5));

        addMarkersToMap();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                selectedMarker = marker;
                return false;
            }
        });
    }

    private void initializeCountryLatLngs() {
        countryLatLngMap = new HashMap<>();
        countryLatLngMap.put("Hungary", new LatLng(47.4996, 19.0321));
        countryLatLngMap.put("Belgium", new LatLng(50.8441, 4.3548));

        countryLatLngMap.put("Greece", new LatLng(37.9853, 23.7290));
        countryLatLngMap.put("Italy", new LatLng(41.8942, 12.5057));

        countryLatLngMap.put("Spain", new LatLng(40.4148, -3.6857));
        countryLatLngMap.put("France", new LatLng(48.8578, 2.3360));
    }

    private void addMarkersToMap() {
        for (String country : countryLatLngMap.keySet()) {
            LatLng latLng = countryLatLngMap.get(country);
            BitmapDescriptor markerColor;

            switch(country) {
                case "Hungary":
                case "Belgium":
                    markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                    break;
                case "Greece":
                case "France":
                    markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                    break;
                case "Italy":
                case "Spain":
                    markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                    break;

                default:
                    markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
                    break;
            }

            mMap.addMarker(new MarkerOptions().position(latLng).title(country).icon(markerColor));
        }
    }


    private String getCountryNameByLatLng(LatLng latLng) {
        for (String country : countryLatLngMap.keySet()) {
            LatLng countryLatLng = countryLatLngMap.get(country);
            if (latLng.equals(countryLatLng)) {
                return country;
            }
        }
        return "Unknown";
    }

    public void backToMenu(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private String getSelectedCountry() {
        if (selectedMarker != null) {
            LatLng markerPosition = selectedMarker.getPosition();
            for (String country : countryLatLngMap.keySet()) {
                LatLng countryLatLng = countryLatLngMap.get(country);
                if (countryLatLng.equals(markerPosition)) {
                    return country;
                }
            }
        }
        return "Unknown";
    }

    public boolean onMarkerClick(Marker marker) {
        selectedMarker = marker; // Kattintott Marker tárolása
        return false; // Visszatérés a default viselkedéshez
    }

    public void backToMenuWithCountry(View view) {
        if (selectedMarker != null) {
            String selectedCountry = getSelectedCountry();
            if (!selectedCountry.equals("Unknown")) {
                // Kiválasztott ország mentése SharedPreferences-ben
                SharedPreferences sharedPreferences = getSharedPreferences("selected_country", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("country", selectedCountry);
                editor.apply();

                // A főmenü Activity indítása a kiválasztott országgal
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("selectedCountry", selectedCountry);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Ismeretlen ország. Kérem válasszon egy érvényes országot!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Kérem válasszon egy országot!", Toast.LENGTH_SHORT).show();
        }
    }

}
