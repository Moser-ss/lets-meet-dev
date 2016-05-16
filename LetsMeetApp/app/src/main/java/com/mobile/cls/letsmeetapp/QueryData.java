package com.mobile.cls.letsmeetapp;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by User on 08/05/2016.
 */
public class QueryData {
    private  AppActivity appActivity;
    private  String[] types;
    private  double radius;
    private LatLng location;



    public String[] getTypes() {
        return types;
    }

    public double getRadius() {
        return radius;
    }

    public LatLng getLocation() {
        return location;
    }

    public QueryData(AppActivity appActivity , LatLng location, double radius, String [] types) {
        this.appActivity = appActivity;
        this.location = location;
        this.radius = radius;
        this.types = types;
    }


    public AppActivity getAppActivity() {
        return appActivity;
    }
}
