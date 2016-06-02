package com.mobile.cls.letsmeetapp;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by User on 06/05/2016.
 */
public class AppPlace implements Parcelable {
    private Uri webSite;
    private  String phoneNumber;
    private  String name;
    private  String placeId;
    private  String address;
    private String type;
    private  LatLng coordinates;
    private float distance;

    public AppPlace(Place place) {
        this.name= place.getName().toString();
        this.placeId = place.getId();
        this.address = place.getAddress().toString();
        this.type = getTypeFromPlace(place.getPlaceTypes());
        this.coordinates = place.getLatLng();
        this.phoneNumber = place.getPhoneNumber().toString();
        this.webSite= place.getWebsiteUri();
    }

    private String getTypeFromPlace(List<Integer> placeTypes) {
        String type = "other";
        int placeType = placeTypes.get(0);
       switch (placeType){
           case Place.TYPE_BAR: {
               type = "bar";
               break;
           }
           case Place.TYPE_RESTAURANT: {
               type = "restaurant";
               break;
           }
           case Place.TYPE_CAFE: {
               type = "cafe";
               break;
           }
       }
        return type;
    }

    public String getName() {
        return name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getAddress() {
        return address;
    }
    public String getType() {return type;}

    public LatLng getCoordinates() {
        return coordinates;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public AppPlace(JSONObject jsonPlace) {
        try {
            JSONObject geometry = jsonPlace.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            this.coordinates = new LatLng(location.getDouble("lat"),location.getDouble("lng"));
            this.name = jsonPlace.getString("name");
            this.placeId = jsonPlace.getString("place_id");
            this.address = jsonPlace.getString("vicinity");
            JSONArray types  = jsonPlace.getJSONArray("types");
            this.type=(String)types.get(0);
            this.distance=0;
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }


    //parcel part
    protected AppPlace(Parcel in) {
        name = in.readString();
        placeId = in.readString();
        address = in.readString();
        type = in.readString();
        coordinates = (LatLng) in.readValue(LatLng.class.getClassLoader());
        distance = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(placeId);
        dest.writeString(address);
        dest.writeString(type);
        dest.writeValue(coordinates);
        dest.writeFloat(distance);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AppPlace> CREATOR = new Parcelable.Creator<AppPlace>() {
        @Override
        public AppPlace createFromParcel(Parcel in) {
            return new AppPlace(in);
        }

        @Override
        public AppPlace[] newArray(int size) {
            return new AppPlace[size];
        }
    };


}
