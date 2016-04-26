package com.mobile.cls.letsmeetapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MenuActivity extends AppCompatActivity {


    public void goNearMe(View view){
        Intent goToNearMe = new Intent( getApplicationContext(), NearMeActivity.class);
        startActivity(goToNearMe);
    }
    public void goFindAPlace(View view){
        Intent goToFindAPlace = new Intent( getApplicationContext(), FindPlaceActivity.class);
        startActivity(goToFindAPlace);
    }
    public void goMyEvents(View view){
        Intent goToMyEnvents = new Intent( getApplicationContext(), MyEventsActivity.class);
        startActivity(goToMyEnvents);
    }
    public void goFavoritePlaces(View view){
        Intent goToFavoritePlaces = new Intent( getApplicationContext(), FavoriteActivity.class);
        startActivity(goToFavoritePlaces);
    }

    public void goOptions(View view){
        Intent goToOptions = new Intent( getApplicationContext(), OptionsActivity.class);
        startActivity(goToOptions);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }
}
