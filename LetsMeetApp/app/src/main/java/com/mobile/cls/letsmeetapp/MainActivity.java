package com.mobile.cls.letsmeetapp;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener , LocationListener, AppActivity{

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private boolean locationUpdatesRequested = false;
    private int primary = R.color.colorPrimary;
    private int primary_light = R.color.primary_light;
    private STATUS status = STATUS.FIND_CURRENT_LOCATION;
    private LatLng lastLatLng;
    private String[] placesTypes = {"bar","cafe", "restaurant"};
    private double RADIUS = 1000;
    private ArrayList<AppPlace> placesToMark;
    public  int PLACE_REQUEST;
    public int CREATE_EVENT;
    private Location lastKnowLocation;
    private HashMap<Marker,String> markerMap;
    private String accountName;


    public void goToMenu (View view){
        Intent goToMenu = new Intent(getApplicationContext(), MenuActivity.class);
        Location location = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        Bundle data = new Bundle();
        LatLng locationLatLng = new LatLng(location.getLatitude(),location.getLongitude());
        data.putParcelable("Location",locationLatLng);
        data.putParcelableArrayList("Places",placesToMark);
        data.putString("Account Name",accountName);
        goToMenu.putExtras(data);
        startActivityForResult(goToMenu,PLACE_REQUEST);
    }


    public void goNearMe(View view){
        Intent goToNearMe = new Intent( getApplicationContext(), NearMeActivity.class);
        Bundle data = new Bundle();
        Location location = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        LatLng locationLatLng = new LatLng(location.getLatitude(),location.getLongitude());
        data.putParcelable("Location",locationLatLng);
        data.putParcelableArrayList("Places", placesToMark);
        goToNearMe.putExtras(data);
        startActivityForResult(goToNearMe, PLACE_REQUEST);
    }

    public void goFavoritePlaces(View view){
        Intent goToFavoritePlaces = new Intent( getApplicationContext(), FavoriteActivity.class);
        startActivity(goToFavoritePlaces);
    }
    public void goMyEvents(View view){
        Intent goToMyEvents = new Intent( getApplicationContext(), MyEventsActivity.class);
        goToMyEvents.putExtra("Account Name",accountName);
        startActivity(goToMyEvents);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accountName = getIntent().getStringExtra("Account Name");
        PLACE_REQUEST=this.getResources().getInteger(R.integer.PLACE_REQUEST);
        CREATE_EVENT=this.getResources().getInteger(R.integer.CREATE_EVENT);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }

        createAutoComplete();
        markerMap = new HashMap<>();
        lastLatLng = new LatLng(0,0);
        placesToMark = new ArrayList<>();
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
                AppPlace appPlace = new AppPlace(place);
                markPlace(appPlace);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("INFO", "An error occurred: " + status);
            }
        });
    }

    private void markPlace(AppPlace place) {
        mMap.clear();

        createUserMark(lastKnowLocation);


        MarkerOptions marker = new MarkerOptions().title(place.getName().toString())
                                    .position(place.getCoordinates());

        Marker m = mMap.addMarker(marker);
        markerMap.put(m,place.getPlaceId());

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getCoordinates(),15));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("DEBUG", "Google Map Ready");
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        mMap.setOnMarkerClickListener(new MarkerClick());

        if (lastLocation != null) {
            updateGUI(lastLocation);}
            

    }

    private void updateGUI(Location lastLocation) {
        if (status == STATUS.FIND_CURRENT_LOCATION) {
            mMap.clear();
            LatLng location = createUserMark(lastLocation);
            createPlacesMarks(lastLocation);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15));

        }
    }

    @NonNull
    private LatLng createUserMark(Location lastLocation) {
        CircleOptions circle = new CircleOptions().fillColor(primary)
                                .strokeColor(primary_light)
                                .radius(5)
                                .strokeWidth(lastLocation.getAccuracy());
        LatLng location = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        circle.center(location);

        mMap.addCircle(circle);
        return location;
    }

    protected void createPlacesMarks(Location lastLocation) {
        LatLng currentLatLng = truncateLatLng(lastLocation);

        if (lastLatLng.equals(currentLatLng)){
            Log.i("INFO", "Location with minor change");
        }else {
            LatLng location = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
            QueryData query = new QueryData(this,location,RADIUS, placesTypes);
            new QueryPlaces().execute(query);
            lastLatLng= currentLatLng;

            Log.i("INFO", "Location change");
        }

        for (AppPlace place :placesToMark){
            MarkerOptions marker = new MarkerOptions().position(place.getCoordinates())
                    .title(place.getName());

            Marker m = mMap.addMarker(marker);
            markerMap.put(m,place.getPlaceId());
        }

    }

    @NonNull
    private LatLng truncateLatLng(Location lastLocation) {
        DecimalFormat format =new DecimalFormat("#.###");
        String latString = format.format(lastLocation.getLatitude());
        double latDouble =Double.parseDouble(latString);

        String longString = format.format(lastLocation.getLongitude());
        double longDouble = Double.parseDouble(longString);

        return new LatLng(latDouble, longDouble);
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
        if (googleApiClient.isConnected()) {
            stopLocationUpdates();
            locationUpdatesRequested = false;
        }
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

        Log.i("INFO", "Location Changed: New Location Lat "+location.getLatitude()+" Long "+location.getLongitude());
        lastKnowLocation = location;
        updateGUI(location);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK && requestCode == PLACE_REQUEST){
            status = STATUS.FIND_PLACE;
            Bundle res = data.getExtras();
            AppPlace appPlace = res.getParcelable("Place");
            Log.d("DEBUG","Place obtain is "+appPlace.getName());
            markPlace(appPlace);

        }
    }


    private void sortPlacesByDistance(){
        Log.i("INFO", "Sorting places by distance from :"+lastLatLng.latitude+" Lat , "+lastLatLng.longitude+" Long");
        for (AppPlace place :placesToMark) {
            float [] results = new float[1];
            Location.distanceBetween(lastLatLng.latitude,lastLatLng.longitude,place.getCoordinates().latitude,place.getCoordinates().longitude,results);
            place.setDistance(results[0]);
            Log.d("DEBUG","Place "+place.getName()+" is a "+place.getDistance()+" m");
        }
        Collections.sort(placesToMark, new Comparator<AppPlace>() {
            @Override
            public int compare(AppPlace appPlace, AppPlace t1) {
                return (int)(appPlace.getDistance()- t1.getDistance());
            }
        });
    }


    @Override
    public void update(Bundle data) {
        this.placesToMark = data.getParcelableArrayList("Places");
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        sortPlacesByDistance();

        if(location != null)createPlacesMarks(location);
    }



    private class MarkerClick implements GoogleMap.OnMarkerClickListener{
        Bundle data;
        @Override
        public boolean onMarkerClick(Marker marker) {
            boolean customListenerUsed = false;
            String placeID = markerMap.get(marker);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient,placeID);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        if (!places.getStatus().isSuccess()) {
                            Log.e("ERROR","Place not found");
                            // Request did not complete successfully
                            places.release();
                            return;
                        }
                        Place place;
                        try {
                            place = places.get(0);
                            Log.i("INFO","Place get successfully - Place "+place.getName());

                        } catch (IllegalStateException e) {
                            places.release();
                            return;
                        }
                        createData(place);
                        places.release();
                    }
                });

            if( data !=null) {

                Intent placeShow = new Intent(getApplicationContext(), PlaceInfoActivity.class);
                placeShow.putExtras(data);
                startActivity(placeShow);
                customListenerUsed = true;
            }

            return customListenerUsed;
        }

        public void createData(Place place) {
            data = new Bundle();
            data.putString("Account Name", accountName);
            data.putString("Place Name", place.getName().toString());
            data.putString("Place Address", place.getAddress().toString());
            if(place.getPhoneNumber()!= null) {
                data.putString("Place Phone Number", place.getPhoneNumber().toString());
            }
            if(place.getWebsiteUri() != null) {
                data.putString("Place WebSite", place.getWebsiteUri().toString());
            }
        }
    }

}
