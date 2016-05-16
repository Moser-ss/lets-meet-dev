package com.mobile.cls.letsmeetapp;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NearMeActivity extends AppCompatActivity {

    private ArrayList<AppPlace> placesNearMe;
    private String longitude;
    private String latitude;
    protected String[] types = {"Bar","Restaurant","Cafe"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_me);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            placesNearMe = extras.getParcelableArrayList("Places");
            LatLng location = extras.getParcelable("Location");
            String[] bundleTypes = extras.getStringArray("Types");
            if(bundleTypes != null){types=bundleTypes;}
            longitude = Double.toString(location.longitude);
            latitude = Double.toString(location.latitude);
        }

        new QueryLocation().execute(new String[] {latitude,longitude});
        final ListView listview = (ListView) findViewById(R.id.resultQueryListView);

        final AppPlaceArrayAdapter adapter = new AppPlaceArrayAdapter(this,
                R.layout.list_near_me, placesNearMe);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                AppPlace selectedPlace = adapter.getItem(position);
                Log.i("INFO", "Place selected "+selectedPlace.getName());
                Intent intent = new Intent();
                Bundle data = new Bundle();
                data.putParcelable("Place", selectedPlace);
                intent.putExtras(data);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }



    private class AppPlaceArrayAdapter extends ArrayAdapter<AppPlace>{

        private HashMap<AppPlace, Integer> mIdMap = new HashMap<AppPlace, Integer>();
        private Context context;

        public AppPlaceArrayAdapter(Context context, int resource, List<AppPlace> objects) {
            super(context, resource, objects);
            this.context = context;
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }
        @Override
        public long getItemId(int position) {
            AppPlace item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            AppPlace place = getItem(position);
            View rowView = inflater.inflate(R.layout.list_near_me, parent, false);
            TextView placeAddressView = (TextView) rowView.findViewById(R.id.placeAddress);
            TextView placeNameView = (TextView) rowView.findViewById(R.id.placeName);
            ImageView placeIconView = (ImageView) rowView.findViewById(R.id.placeIcon);
            placeAddressView.setText(place.getAddress());
            placeNameView.setText(place.getName());
            // change the icon for Windows and iPhone
            String type = place.getType();

            switch (type){
                case "bar":
                    placeIconView.setImageResource(R.drawable.bar);
                    break;
                case "cafe":
                    placeIconView.setImageResource(R.drawable.cafe);
                    break;
                case "restaurant":
                    placeIconView.setImageResource(R.drawable.restaurant);
                    break;
                default:
                    break;
            }

            TextView distanceView = (TextView) rowView.findViewById(R.id.placeDistance);
            DecimalFormat format = new DecimalFormat("#");
            distanceView.setText("Distance :"+format.format(place.getDistance())+" meters");
            return rowView;
        }
    }

    private class QueryLocation extends AsyncTask< String,Integer,String> {


        @Override
        protected String doInBackground(String... strings) {
            String urlString = makeUrl(strings[0],strings[1]);
            Log.i("INFO", "URL created : "+urlString);
            String jsonContents = getUrlContents(urlString);
            String location = getLocation(jsonContents);
            return location;
        }
        @Override
        protected void onPostExecute(String location) {
            TextView queryView = (TextView) findViewById(R.id.queryView);
            StringBuilder textBuilder = new StringBuilder();
            for( int i =0; i<types.length;i++ ){
                String type = types[i].substring(0,1).toUpperCase() + types[i].substring(1).toLowerCase();
                textBuilder.append(type);
                if(!(i==(types.length-1))){
                    if(i==(types.length-2)){
                        textBuilder.append(" & ");
                    }else textBuilder.append(" , ");
                }
            }
            textBuilder.append(" near "+location);
            queryView.setText(textBuilder.toString());
        }

        private String getLocation(String jsonContents) {
            String location ="None";
            try {
                JSONObject object = new JSONObject(jsonContents);
                JSONArray array = object.getJSONArray("results");
                object = array.getJSONObject(0);
                JSONArray address_components = object.getJSONArray("address_components");
                 JSONObject locality = address_components.getJSONObject(0);
                location = locality.getString("long_name");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return  location;
        }

        private String makeUrl(String latitude, String longitude) {
            StringBuilder urlString = new StringBuilder(
                    "https://maps.googleapis.com/maps/api/geocode/json?");

            urlString.append("&latlng=");
            urlString.append(latitude);
            urlString.append(",");
            urlString.append(longitude);
            urlString.append("&result_type=locality");
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
