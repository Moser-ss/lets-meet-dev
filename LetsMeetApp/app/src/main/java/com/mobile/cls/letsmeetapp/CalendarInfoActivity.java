package com.mobile.cls.letsmeetapp;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CalendarInfoActivity extends FragmentActivity {


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int REQUEST_AUTHORIZATION = 1001;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
     static final int REQUEST_PEOPLE = 1004 ;
    ProgressDialog mProgress;
    GoogleAccountCredential mCredential;
    private ListView mOutputText;
    CheckBoxArrayAdapter<String> calendarAdapter ;
    private ArrayList<String> emailList;
    private DialogFragment startTimeFragment;
    private DialogFragment endTimeFragment;
    private DialogFragment startDateFragment;
    private DialogFragment endDateFragment;
    private String eventAddress;
    private String eventPlaceName;
    private HashMap<String, String> calendarMap;
    private String accountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_info);
        mOutputText = (ListView) findViewById(R.id.calendarListTextView);
        eventAddress = getIntent().getStringExtra("Event Address");
        eventPlaceName = getIntent().getStringExtra("Event Place Name");
        accountName = (getIntent().getStringExtra("Account Name"));
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(accountName);
        getResultsFromApi();
    }
    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.",Toast.LENGTH_LONG).show();

                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
            case REQUEST_PEOPLE:
                if (resultCode == RESULT_OK) {
                    emailList = data.getStringArrayListExtra("Email list");
                    Log.d("DEBUG","Email's attendees : "+emailList.toString());
                }
            break;
        }
    }
    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        Log.d("DEBUG","Get Results from API");
        if (!isGooglePlayServicesAvailable()) {
            Log.d("DEBUG","Acquiring Google Play Services");
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            Log.d("DEBUG","Choosing Account");
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(getApplicationContext(),"No network connection available.",Toast.LENGTH_LONG).show();

        } else {
            Log.d("DEBUG","Requesting Events");
            new MakeRequestTask(mCredential).execute();
        }
    }
    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
        /**
         * Check that Google Play services APK is installed and up to date.
         * @return true if Google Play Services is available and up to
         *     date on this device; false otherwise.
         */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                CalendarInfoActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    public String getDataStartEvent() {
        DatePickerFragment pickerDate= (DatePickerFragment)startDateFragment;
        int eventStartYear = pickerDate.getYear();
        int eventStartMonth = pickerDate.getMonth()+1;
        int eventStartDay = pickerDate.getDay();


        TimePickerFragment pickerTime= (TimePickerFragment)startTimeFragment;
        int eventStartHour = pickerTime.getHour();
        int eventStartMinute = pickerTime.getMinute();

        String dataStartEvent =""+eventStartYear+"-"+String.format("%02d",eventStartMonth)+"-"+String.format("%02d",eventStartDay)+"T"
                +String.format("%02d",eventStartHour)+":"+String.format("%02d",eventStartMinute)+":00";
        return dataStartEvent;
    }

    public String getDateEndEvent() {

        DatePickerFragment pickerDate= (DatePickerFragment)endDateFragment;
        int eventEndYear = pickerDate.getYear();
        int eventEndMonth = pickerDate.getMonth()+1;
        int eventEndDay = pickerDate.getDay();

        TimePickerFragment pickerTime= (TimePickerFragment)endTimeFragment;
        int eventEndHour = pickerTime.getHour();
        int eventEndMinute = pickerTime.getMinute();

        String dateEndEvent = ""+eventEndYear+"-"+String.format("%02d",eventEndMonth)+"-"+String.format("%02d",eventEndDay)+"T"
                +String.format("%02d",eventEndHour)+":"+String.format("%02d",eventEndMinute)+":00";
        return dateEndEvent;
    }


    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, HashMap<String,String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected HashMap<String,String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private HashMap<String,String> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            HashMap<String,String> calendarStrings = new HashMap<>();
            /*Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();*/

            String pageToken = null;
            do {
                CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> items = calendarList.getItems();

                for (CalendarListEntry calendarListEntry : items) {
                    Log.d("DEBUG", "Get Calendar "+calendarListEntry.getSummary()+" with ACL "+calendarListEntry.getAccessRole());
                     if( calendarListEntry.getAccessRole().equals("writer") || calendarListEntry.getAccessRole().equals("owner")) {
                         calendarStrings.put(calendarListEntry.getSummary(),calendarListEntry.getId());
                         Log.d("DEBUG", "Calendar "+calendarListEntry.getSummary()+" added");
                     }
                }
                pageToken = calendarList.getNextPageToken();
            } while (pageToken != null);

            return calendarStrings;
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(HashMap<String,String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                    Toast.makeText(getApplicationContext(),"No results returned.",Toast.LENGTH_LONG).show();

            } else {

                update(output);

            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CalendarInfoActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(getApplicationContext(),"The following error occurred:"
                            + mLastError.getMessage(),Toast.LENGTH_LONG).show();
                    Log.e("ERROR", "The following error occurred:"+ mLastError.getMessage());
                }
            } else {
                Toast.makeText(getApplicationContext(),"Request cancelled.",Toast.LENGTH_LONG).show();

            }
        }
    }

    private void update(HashMap<String,String> output) {
        Log.d("DEBUG", "List of calendars "+ output.keySet().toString());
        ArrayList<String> arrayOutput = new ArrayList<>();
        calendarMap = output;
        arrayOutput.addAll(output.keySet());
        calendarAdapter = new CheckBoxArrayAdapter<String>(this, R.layout.checkbox_row, arrayOutput);

        mOutputText.setAdapter(calendarAdapter);
        mOutputText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String calendar = calendarAdapter.getItem(position);
                Log.i("INFO", "Calendar selected "+calendar);

            }
        });

    }

    public void addPeople(View view){
        Intent intent = new Intent(getApplicationContext(),PeopleInfoActivity.class);
        startActivityForResult(intent,REQUEST_PEOPLE);
    }

    public void createEvent(View view){
        ArrayList<String> calendarsChecked = calendarAdapter.getCheckedItems();
        Bundle dataEvent = new Bundle();
        dataEvent.putStringArrayList("Calendars",calendarsChecked);
        dataEvent.putStringArrayList("People Invited",emailList);
        final Calendar c = Calendar.getInstance();
        String eventSummary = getResources().getString(R.string.event_summary);
        TimeZone timeZone =c.getTimeZone();
        int mGMTOffset = timeZone.getRawOffset();

        String[] availableIDs = TimeZone.getAvailableIDs(mGMTOffset);
        Log.d("DEBUG", "Available IDs : "+availableIDs.toString());

        timeZone = TimeZone.getTimeZone(availableIDs[0]);

        String timeZoneName =timeZone.getDisplayName();
        String dateStartEvent = getDataStartEvent();
        //dateStartEvent = dateStartEvent+TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS);
        String dateEndEvent = getDateEndEvent();
        //dateEndEvent = dateEndEvent+TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS);

        Log.i("INFO", "Event Summary :"+eventSummary);
        Log.i("INFO", "Event Place Name :"+eventPlaceName);
        Log.i("INFO", "Event Location :"+eventAddress);
        Log.i("INFO", "Event Start Date :"+dateStartEvent);
        Log.i("INFO", "Event End Date :"+dateEndEvent);
        Log.i("INFO", "Event TimeZone :"+timeZoneName);
        if (emailList != null) {
            Log.i("INFO", "Event Attendees  :"+emailList.toString());
        } else {Log.i("INFO", "Event Attendees  : email list empty");
        }

        Event event = createEventObject(eventSummary, timeZoneName, dateStartEvent, dateEndEvent);
        ArrayList<String> calendarsCheckedId = new ArrayList<>();

        for (String calendar:calendarsChecked ) {

            calendarsCheckedId.add(calendarMap.get(calendar));
        }
        CreateRequestTask task = new CreateRequestTask(mCredential,event,calendarsCheckedId);
        task.execute();
    }

    private void finishActivityTasks() {
        Intent intent = new Intent(getApplicationContext(),MyEventsActivity.class);
        intent.putExtra("Account Name",accountName);
        startActivity(intent);
        finish();
    }
    @NonNull
    private Event createEventObject(String eventSummary, String timeZoneName, String dateStartEvent, String dateEndEvent) {
        Event event = new Event()
                .setSummary(eventSummary)
                .setLocation(eventAddress)
                .setDescription("A nice event in "+eventPlaceName);

        DateTime startDateTime = new DateTime(dateStartEvent);
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(timeZoneName);
        event.setStart(start);

        DateTime endDateTime = new DateTime(dateEndEvent);
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(timeZoneName);
        event.setEnd(end);

        if (emailList !=null) {
            ArrayList<EventAttendee> attendees = new ArrayList<>();
            for (String email:emailList ) {
                attendees.add(new EventAttendee().setEmail(email));

            }
            event.setAttendees(attendees);
        }

        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("email").setMinutes(24 * 60),
                new EventReminder().setMethod("popup").setMinutes(10),
        };
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);
        return event;
    }

    public void showTimePickerDialog(View v) {

        if(v.getId() == R.id.timeStartPicker){
            startTimeFragment = new TimePickerFragment();
            startTimeFragment.show(getFragmentManager(), "timePicker");
        }
        if(v.getId() == R.id.timeEndPicker){
            endTimeFragment = new TimePickerFragment();
            endTimeFragment.show(getFragmentManager(), "timePicker");
        }
    }

    public void showDatePickerDialog(View v) {

        if(v.getId() == R.id.dateStartPicker){
            startDateFragment = new DatePickerFragment();
            startDateFragment.show(getFragmentManager(), "datePicker");

        }
        if(v.getId() == R.id.dateEndPicker){
            endDateFragment = new DatePickerFragment();
            endDateFragment.show(getFragmentManager(), "datePicker");
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {


        private int hour;
        private int minute;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour = hourOfDay;
            this.minute = minute;
        }

        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private int year;
        private int month;
        private int day;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            this.year=year;
            this.month = month;
            this.day = day;
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public int getDay() {
            return day;
        }
    }

    private class CreateRequestTask extends AsyncTask<Void, Void, List<Event>> {
        private  ArrayList<String> calendarsCheckedId;
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private Event event;
        public CreateRequestTask(GoogleAccountCredential credential, Event event, ArrayList<String> calendarsCheckedId) {
            this.event =event;
            this.calendarsCheckedId =calendarsCheckedId;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<Event> doInBackground(Void... params) {
            try {
                return setDataInApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<Event> setDataInApi() throws IOException {
            ArrayList<Event> events = new ArrayList<>();
            for (String calendarId:
                    calendarsCheckedId) {
                Event eventCreated= mService.events().insert(calendarId, event).execute();
                events.add(eventCreated);
                Log.i("INFO", "Event created : Summary "+eventCreated.getSummary()+", ID "+eventCreated.getId());
                Log.d("DEBUG", "Event created : "+eventCreated.getHtmlLink());
            }

            return events;
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<Event> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                Toast.makeText(getApplicationContext(),"No results returned.",Toast.LENGTH_LONG).show();

            } else {

                    finishActivityTasks();

            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CalendarInfoActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(getApplicationContext(),"The following error occurred:"
                            + mLastError.getMessage(),Toast.LENGTH_LONG).show();

                }
            } else {
                Toast.makeText(getApplicationContext(),"Request cancelled.",Toast.LENGTH_LONG).show();

            }
        }
    }


}
