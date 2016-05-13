package com.mobile.cls.letsmeetapp;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by User on 08/05/2016.
 */
public class QueryData {
    private  MainActivity main;
    private  String[] types;
    private  double radius;
    private Location location;



    public String[] getTypes() {
        return types;
    }

    public double getRadius() {
        return radius;
    }

    public Location getLocation() {
        return location;
    }

    public QueryData(MainActivity main ,Location location, double radius, String [] types) {
        this.main = main;
        this.location = location;
        this.radius = radius;
        this.types = types;
    }

    public MainActivity getMainActivity() {
        return main;
    }
}
