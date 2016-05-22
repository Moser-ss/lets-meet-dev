package com.mobile.cls.letsmeetapp;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    public int PLACE_REQUEST;
    private Bundle data;

    public void goNearMe(View view){
        Intent goToNearMe = new Intent( getApplicationContext(), NearMeActivity.class);
        Bundle dataToNearMe = new Bundle();
        dataToNearMe.putParcelable("Location", data.getParcelable("Location"));
        dataToNearMe.putParcelableArrayList("Places", data.getParcelableArrayList("Places"));
        goToNearMe.putExtras(dataToNearMe);
        startActivityForResult(goToNearMe, PLACE_REQUEST);
    }
    public void goFindAPlace(View view){
        Intent goToFindAPlace = new Intent( getApplicationContext(), FindPlaceActivity.class);
        Bundle dataToFindAPlace = new Bundle();
        dataToFindAPlace.putParcelable("Location",data.getParcelable("Location"));
        goToFindAPlace.putExtras(dataToFindAPlace);
        startActivityForResult(goToFindAPlace,PLACE_REQUEST);
    }
    public void goMyEvents(View view){
        Intent goToMyEvents = new Intent( getApplicationContext(), MyEventsActivity.class);
        startActivity(goToMyEvents);
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
        PLACE_REQUEST=this.getResources().getInteger(R.integer.PLACE_REQUEST);
        data = getIntent().getExtras();
        setContentView(R.layout.activity_menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == PLACE_REQUEST){
            setResult(RESULT_OK,data);
            finish();
        }
    }
}
