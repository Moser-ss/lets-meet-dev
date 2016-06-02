package com.mobile.cls.letsmeetapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PlaceInfoActivity extends AppCompatActivity {

    private int CREATE_EVENT;
    private String accountName;
    private String placeAddress;
    private String placeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_info);
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        accountName = data.getString("Account Name");
        TextView placeNameTextView = (TextView)findViewById(R.id.placeNameTextView);
        TextView placeAddressTextView = (TextView)findViewById(R.id.placeAddressTextView);
        TextView placePhoneNumberTextView = (TextView)findViewById(R.id.placePhoneNumberTextView);
        TextView placeWebSiteTextView = (TextView)findViewById(R.id.placeWebSiteTextView);
        placeName= data.getString("Place Name");
        placeAddress = data.getString("Place Address");
        placeNameTextView.setText(data.getString("Place Name"));
        placeAddressTextView.setText(data.getString("Place Address"));
        placePhoneNumberTextView.setText(data.getString("Place Phone Number"));
        placeWebSiteTextView.setText(data.getString("Place WebSite"));

        CREATE_EVENT = this.getResources().getInteger(R.integer.CREATE_EVENT);

    }

    public void createEvent(View view){
        Intent intent = new Intent(getApplicationContext(),CalendarInfoActivity.class);
        intent.putExtra("Account Name",accountName);
        intent.putExtra("Event Address",placeAddress);
        intent.putExtra("Event Place Name",placeName);
        startActivity(intent);
        finish();
    }
    public void addFavorite(View view){

    }
}
