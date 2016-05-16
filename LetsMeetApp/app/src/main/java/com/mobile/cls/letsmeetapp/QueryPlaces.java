package com.mobile.cls.letsmeetapp;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by User on 05/05/2016.
 */
public class QueryPlaces extends AsyncTask<QueryData,Integer, ArrayList<AppPlace>> {


    private static final String SERVER_PLACES_KEY ="AIzaSyAQardAatFiJGIWRWRU7RF-YIv-sBvYBSI" ;
    private AppActivity appActivity;
    private QueryData queryData;

    private ArrayList<AppPlace> getPlaces(LatLng location, double radius, String[] types){
        ArrayList<AppPlace> places = new ArrayList<>();

        for(String type : types){
            ArrayList<AppPlace> tmpPlaces = getPlaces(location,radius,type);
            places.addAll(tmpPlaces);
        }

        Log.i("INFO", "Total places found "+places.size());
        return places;
    }

    private  ArrayList<AppPlace> getPlaces(LatLng location, double radius , String type)  {
        ArrayList<AppPlace> places = null;

        String urlString = makeUrl(location.latitude,location.longitude,radius,type);
        Log.i("INFO", "URL created : "+urlString);
        String jsonContents = getUrlContents(urlString);


            try {
                JSONObject object = new JSONObject(jsonContents);
                JSONArray array = object.getJSONArray("results");
                Log.i("INFO", "Query with "+array.length()+" results");
                places = getPlaces(array);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        Log.i("INFO", places.size()+" places found with type "+type);
        return places;
    }




    private String makeUrl(double latitude, double longitude, double radius, String type) {
        StringBuilder urlString = new StringBuilder(
                "https://maps.googleapis.com/maps/api/place/search/json?");

            urlString.append("&location=");
            urlString.append(Double.toString(latitude));
            urlString.append(",");
            urlString.append(Double.toString(longitude));
            urlString.append("&radius="+Double.toString(radius));
            urlString.append("&type=" + type);
            urlString.append("&key=" +SERVER_PLACES_KEY);

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

    private ArrayList<AppPlace> getPlaces(JSONArray array){
        ArrayList<AppPlace> places = new ArrayList<>();

        for (int i = 0; i< array.length();i++){
            try {
                JSONObject jsonPlace = (JSONObject)array.get(i);

                AppPlace place = new AppPlace(jsonPlace);
                places.add(place);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return  places;
    }



    @Override
    protected ArrayList<AppPlace> doInBackground(QueryData... data) {
        queryData =data[0];

        this.appActivity= queryData.getAppActivity();


        return getPlaces(queryData.getLocation(),queryData.getRadius(),queryData.getTypes());
    }

    @Override
    protected void onPostExecute(ArrayList<AppPlace> places) {
        Bundle data = new Bundle();
        data.putParcelableArrayList("Places", places);
        data.putParcelable("Location", queryData.getLocation());
        data.putStringArray("Types",queryData.getTypes());
        this.appActivity.update(data);
    }




}
