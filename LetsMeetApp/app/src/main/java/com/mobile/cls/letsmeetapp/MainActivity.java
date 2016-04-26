package com.mobile.cls.letsmeetapp;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    String provider;

    public void goToMenu (View view){
        Intent goToMenu = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(goToMenu);
    }


    public void goNearMe(View view){
        Intent goToNearMe = new Intent( getApplicationContext(), NearMeActivity.class);
        startActivity(goToNearMe);
    }

    public void goFavoritePlaces(View view){
        Intent goToFavoritePlaces = new Intent( getApplicationContext(), FavoriteActivity.class);
        startActivity(goToFavoritePlaces);
    }
    public void goMyEvents(View view){
        Intent goToMyEnvents = new Intent( getApplicationContext(), MyEventsActivity.class);
        startActivity(goToMyEnvents);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera


        Location currentlocation = locationManager.getLastKnownLocation(provider);
        LatLng mylocation = new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude());

        mMap.addMarker(new MarkerOptions().position(mylocation).title("Current Location"));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mylocation,15));

    }

    @Override
    protected void onResume() {
        super.onResume();


        locationManager.requestLocationUpdates(provider, 500, 25, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onLocationChanged(Location location) {

        LatLng mylocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(mylocation).title("Current Location"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));

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
}
