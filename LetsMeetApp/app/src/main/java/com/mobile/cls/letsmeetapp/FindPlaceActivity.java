package com.mobile.cls.letsmeetapp;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FindPlaceActivity extends AppCompatActivity implements AppActivity {

    private int RADIUS = 1000;
    public  int PLACE_REQUEST;
    private String [] placesTypes;
    private boolean restaurantButton = false;
    private boolean barButton = false;
    private boolean cafeButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getIntent().getExtras();
        setContentView(R.layout.activity_find_place);
        PLACE_REQUEST=this.getResources().getInteger(R.integer.PLACE_REQUEST);
        EditText placeToQueryText = (EditText) findViewById(R.id.placeToQueryText);
        placeToQueryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText placeToQueryText = (EditText) view;
                placeToQueryText.setText("");
            }
        });
    }



    public void uncheckedRestaurantButton(View view){
        RadioButton radioButton = (RadioButton)view;
        if(restaurantButton){
            Log.d("DEBUG","RadioButton is checked");
            radioButton.setChecked(false);
            Log.d("DEBUG","RadioButton unchecked ");
            restaurantButton=false;
        }else {
            Log.d("DEBUG","RadioButton is unchecked");
            radioButton.setChecked(true);
            Log.d("DEBUG","RadioButton checked ");
            restaurantButton=true;
        }
    }

    public void uncheckedBarButton(View view){
        RadioButton radioButton = (RadioButton)view;
        if(barButton){
            Log.d("DEBUG","RadioButton is checked");
            radioButton.setChecked(false);
            Log.d("DEBUG","RadioButton unchecked ");
            barButton=false;
        }else {
            Log.d("DEBUG","RadioButton is unchecked");
            radioButton.setChecked(true);
            Log.d("DEBUG","RadioButton checked ");
            barButton=true;
        }
    }

    public void uncheckedCafeButton(View view){
        RadioButton radioButton = (RadioButton)view;
        if(cafeButton){
            Log.d("DEBUG","RadioButton is checked");
            radioButton.setChecked(false);
            Log.d("DEBUG","RadioButton unchecked ");
            cafeButton=false;
        }else {
            Log.d("DEBUG","RadioButton is unchecked");
            radioButton.setChecked(true);
            Log.d("DEBUG","RadioButton checked ");
            cafeButton=true;
        }
    }

    public void queryPlace(View view){
        ArrayList<String> types = new ArrayList<>();
        RadioButton restaurantButton = (RadioButton) findViewById(R.id.restaurantButton);
        RadioButton barButton = (RadioButton) findViewById(R.id.barButton);
        RadioButton cafeButton = (RadioButton) findViewById(R.id.cafeButton);
        if(restaurantButton.isChecked()) types.add("restaurant");
        if(barButton.isChecked())types.add("bar");
        if(cafeButton.isChecked())types.add("cafe");
        placesTypes = new String[types.size()];
        placesTypes = types.toArray(placesTypes);

        EditText placeToQuery = (EditText) findViewById(R.id.placeToQueryText);

        String location = placeToQuery.getText().toString().trim();
        if(location.contains(" ")){
            StringBuilder builder = new StringBuilder(location);
            builder.insert(0,'"');
            builder.append('"');
            location = builder.toString();
        }
        
        Log.i("INFO","Query places with type "+types.toString()+" in location "+location);
        new QueryAddress().execute(location);


    }
    private void queryPlaces(LatLng location) {
        QueryData query = new QueryData(this,location,RADIUS, placesTypes);
        new QueryPlaces().execute(query);
    }
    private void sortPlacesByDistance(LatLng location, ArrayList<AppPlace> placesToMark){
        Log.i("INFO", "Sorting places by distance from :"+location.latitude+" Lat , "+location.longitude+" Long");
        for (AppPlace place :placesToMark) {
            float [] results = new float[1];
            Location.distanceBetween(location.latitude,location.longitude,place.getCoordinates().latitude,place.getCoordinates().longitude,results);
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
        Intent showPlacesResult = new Intent(getApplicationContext(),NearMeActivity.class);
        ArrayList<AppPlace> placesToMark = data.getParcelableArrayList("Places");
        LatLng location =data.getParcelable("Location");
        sortPlacesByDistance(location, placesToMark);
        data.putParcelableArrayList("Places",placesToMark);
        showPlacesResult.putExtras(data);

        startActivityForResult(showPlacesResult,PLACE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK && requestCode == PLACE_REQUEST){
            setResult(RESULT_OK,data);
            finish();
        }
    }

    private class QueryAddress extends AsyncTask< String,Integer,LatLng> {


        @Override
        protected LatLng doInBackground(String... strings) {
            String urlString = makeUrl(strings[0]);
            Log.i("INFO", "URL created : "+urlString);
            String jsonContents = getUrlContents(urlString);
            LatLng location = getLocation(jsonContents);
            return location;
        }
        @Override
        protected void onPostExecute( LatLng location) {
            if(location.latitude==0 && location.longitude ==0){
                Toast.makeText(getBaseContext(),"Place not found.Please insert new place",Toast.LENGTH_LONG).show();
            }
            else queryPlaces(location);
        }

        private LatLng getLocation(String jsonContents) {
            LatLng location =new LatLng(0,0);
            try {
                JSONObject object = new JSONObject(jsonContents);
                JSONArray array = object.getJSONArray("results");
                object = array.getJSONObject(0);
                JSONObject geometry = object.getJSONObject("geometry");
                JSONObject locationObject = geometry.getJSONObject("location");
                Double latitude =locationObject.getDouble("lat");
                Double longitude = locationObject.getDouble("lng");
                location = new LatLng(latitude,longitude);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return  location;
        }

        private String makeUrl(String address) {
            StringBuilder urlString = new StringBuilder(
                    "https://maps.googleapis.com/maps/api/geocode/json?");

            urlString.append("&address=");
            urlString.append(address);
            urlString.append("&key=" +getBaseContext().getResources().getString(R.string.server_places_key));

            return urlString.toString();
        }
        private String getUrlContents(String theUrl) {
            StringBuilder content = new StringBuilder();
            try {
                URL url = new URL(theUrl);
                Log.i("INFO", "Opening URL connection");
                URLConnection urlConnection = url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()), 8);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line + "\n");
                }
                bufferedReader.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("INFO", "Closing URL connection");
            return content.toString();
        }
    }



}
