package com.mobile.cls.letsmeetapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PeopleInfoActivity extends AppCompatActivity {

    private CheckBoxArrayAdapter<String> contactAdapter;
    private HashMap<String,String> nameEmailMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_info);
        ListView contactsList = (ListView) findViewById(R.id.peopleListTextView);
        nameEmailMap =getContactsInfo();
        ArrayList<String> contactsNameList = new ArrayList<>(nameEmailMap.keySet());
        java.util.Collections.sort(contactsNameList);
        contactAdapter = new CheckBoxArrayAdapter<String>(this, R.layout.checkbox_row, contactsNameList);

        contactsList.setAdapter(contactAdapter);


    }

    public void invitePeople(View view){
        ArrayList<String> nameChecked = contactAdapter.getCheckedItems();

        ArrayList<String> emailPeopleInvited = new ArrayList<>();
        for (String name :nameChecked ) {
            String email = nameEmailMap.get(name);
            Log.d("DEBUG","Email from people invited: "+email);
            emailPeopleInvited.add(email);
        }

        Intent intent = new Intent();

        intent.putStringArrayListExtra("Email list",emailPeopleInvited);

        setResult(RESULT_OK, intent);
        finish();
    }

    private HashMap<String,String> getContactsInfo() {
        HashMap<String,String> nameEmailMap = new HashMap<>();
        HashSet<String> emlRecsHS = new HashSet<String>();
        ContentResolver cr = getApplicationContext().getContentResolver();
        String[] PROJECTION = new String[] { ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID };
        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";
        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
        if (cur.moveToFirst()) {
            do {
                // names comes in hand sometimes
                String name = cur.getString(1);
                String emlAddr = cur.getString(3);

                // keep unique only
                if (emlRecsHS.add(emlAddr.toLowerCase())) {
                    if (!name.contains("@")) {
                        nameEmailMap.put(name, emlAddr);
                        Log.d("DEBUG", "Contact Name " + name + " Email " + emlAddr);
                    }
                }
            } while (cur.moveToNext());
        }

        cur.close();
        return nameEmailMap;
    }
}
