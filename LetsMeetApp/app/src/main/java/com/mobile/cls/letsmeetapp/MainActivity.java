package com.mobile.cls.letsmeetapp;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener , LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private boolean locationUpdatesRequested = false;
    private int primary = R.color.colorPrimary;
    private int primary_light = R.color.primary_light;
    private STATUS status = STATUS.FIND_CURRENT_LOCATION;


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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createAutoComplete();
    }

    public void goToCurrentLocation(View view){
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        status=STATUS.FIND_CURRENT_LOCATION;
        updateGUI(currentLocation);

    }


    private void createAutoComplete() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("INFO", "Place: " + place.getName());
                status=STATUS.FIND_PLACE;
                markPlace(place);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("INFO", "An error occurred: " + status);
            }
        });
    }

    private void markPlace(Place place) {
        MarkerOptions marker = new MarkerOptions().title(place.getName().toString())
                                    .position(place.getLatLng());
        mMap.addMarker(marker);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (lastLocation != null) {
            updateGUI(lastLocation);}
            

    }

    private void updateGUI(Location lastLocation) {
        if (status == STATUS.FIND_CURRENT_LOCATION) {
            mMap.clear();
            CircleOptions circle = new CircleOptions().fillColor(primary)
                                    .strokeColor(primary_light)
                                    .radius(5)
                                    .strokeWidth(lastLocation.getAccuracy());
            LatLng location = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
            circle.center(location);

            mMap.addCircle(circle);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15));
        }
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if(googleApiClient.isConnected()) {
            if(locationRequest == null)createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient.isConnected() && !locationUpdatesRequested) {
            startLocationUpdates();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        locationUpdatesRequested=false;

    }

    private void stopLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (!locationUpdatesRequested) {
            startLocationUpdates();
            locationUpdatesRequested=true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("INFO", "Location Changed");
        updateGUI(location);

    }
}
