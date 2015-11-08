package com.example.ehc.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.location.LocationManager;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    private static final String NAV_ITEM_ID = "navItemId";
    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private final HomeFragment homeFragment = new HomeFragment();
    private final AboutFragment aboutFragment = new AboutFragment();
    private final Help_Relax_Fragment relaxFragment = new Help_Relax_Fragment();
    private final Help_Contact_Fragment contactFragment = new Help_Contact_Fragment();
    private final Data_Fragment dataFragment = new Data_Fragment();
    private final HelpFragment helpFragment = new HelpFragment();
    private final PreferencesFragment preferenceFragment = new PreferencesFragment();
    private BandClient client = null;
    private TextView txtStatus, firstItem, secondItem;
    private long tStart, tStop, tDelta;
    double elapsedTime;
    private boolean firstPoll = true;
    private ArrayList<Integer> BPMList;
    private Toolbar toolbar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private LocationRequest mLocationRequest;

    private int mNavItemId;
    private ActionBarDrawerToggle mDrawerToggle;
    private final Handler mDrawerActionHandler = new Handler();

    private String loc = "New Haven, CT 06520";
    private DBHandler dbHandler = new DBHandler(this,null,null,1);
    Data panic = new Data();
    protected MediaPlayer mp;
    String TAG = "Alert!";
    private boolean diagOpen = false;
    private boolean sent = false;


    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {
                if (firstPoll) {
                    tStart = System.currentTimeMillis();
                    BPMList = new ArrayList<>();
                    firstPoll = !firstPoll;
                }
                tStop = System.currentTimeMillis();
                tDelta = tStop - tStart;
                elapsedTime = tDelta / 1000.0;
                BPMList.add(event.getHeartRate());
                //if the elapsed time exceeds 30s, calculate the average bpm
                if (elapsedTime >= 30.00) calcAvgBPM(BPMList);


                appendToUI(String.format("Heart Rate = %d bpm\n"
                        + "Quality = %s\n", event.getHeartRate(), event.getQuality()));
                if (event.getHeartRate() > 120) {
                    //appendToUI(String.format("Heart Rate rising, in danger zone. Do you require assistance?"));
                    while (!diagOpen) {
                        diagOpen = !diagOpen;

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                        builder1.setMessage("Heart Rate rising, in danger zone. Do you require assistance?");
                        builder1.setCancelable(true);
                        AlertDialog.Builder yes = builder1.setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                        //Give user 4 options
                                        AlertDialog levelDialog = null;
// Strings to Show In Dialog with Radio Buttons
                                        final CharSequence[] items = {"Soothing Music ", "Breathing Exercises", "Text Emergency Contact", "Call Emergency Contact"};
                                        // Creating and Building the Dialog
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle("Select from the options below");
                                        final AlertDialog finalLevelDialog = builder.create();
                                        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int item) {
                                                switch (item) {
                                                    case 0:
                                                        // Run the music playing method
                                                        mp = MediaPlayer.create(MainActivity.this, R.raw.calm);
                                                        mp.start();
                                                        diagOpen = !diagOpen;
                                                        break;
                                                    case 1:
                                                        // Play the breathing exercises GIF
                                                        diagOpen = !diagOpen;
                                                        break;
                                                    case 2:
                                                        // Send the text to a pre-loaded emergency contact
                                                        diagOpen = !diagOpen;
                                                        SMSAction();
                                                        break;
                                                    case 3:
                                                        // Call the pre-set emergency contact
                                                        diagOpen = !diagOpen;
                                                        AlertAction();
                                                        break;
                                                }

                                            }
                                        });
                                        levelDialog = builder.create();
                                        levelDialog.show();
                                    }
                                });
                        builder1.setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                }

                    if (calcAvgBPM(BPMList) > 120) {
                        // send SMS if AVG heart rate is > 120
                        if (!sent) {
                            SMSAction();
                            sent = true;
                        }

                    }
                 if (event.getHeartRate() > 135) {
                    AlertAction();                                      // contact emergency contact as soon as heart rate >= 135
                }
            }
        }
    };

    //calculates the average of all heart rate stored from the past 30s
    private double calcAvgBPM(List<Integer> list) {
        if (list.isEmpty() || list == null) {
            return 0;
        }
        int sum = 0;
        int n = list.size();
        for (int i = 0; i < n; i++) {
            sum += list.get(i);
        }

        double avgBPM = (double) sum / n;

        if (avgBPM > 135) AlertAction();
        else firstPoll = !firstPoll;

        return avgBPM;
    }

    private void SMSAction() {
        Date now = new Date();
        panic.set_date(now.toString());
        panic.set_work(loc);

        dbHandler.addData(panic);

        if (mLastLocation!=null) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
            appendToUI(addresses.get(0).toString());
        } catch (IOException ioException) {
            String errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            String errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + mLastLocation.getLatitude() +
                    ", Longitude = " +
                    mLastLocation.getLongitude(), illegalArgumentException);
        }
    }
        String message = "ALERT: I need help, having high heart rate. I'm here: " + loc;
        SmsManager.getDefault().sendTextMessage(getContactNumber(), null, message, null, null);
        Toast.makeText(getApplicationContext(), "Message Sent.", Toast.LENGTH_SHORT).show();
    }

    private void AlertAction(){
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:" + getContactNumber()));
        startActivity(phoneIntent);
    }


    private String getContactNumber(){
        return "11111111111";
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        buildGoogleApiClient();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
        mGoogleApiClient.connect();
        if (null == savedInstanceState) {
            mNavItemId = R.id.nav_home;
        } else {
            mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
        }

        txtStatus = (TextView) findViewById(R.id.txtStatus);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        //listen for navigation events
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView.getMenu().findItem(mNavItemId).setChecked(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open,
                R.string.close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        navigate(mNavItemId);

//        BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
//        BandClient bandClient = BandClientManager.getInstance().create(getActivity(), pairedBands[0]);
        final WeakReference<Activity> reference = new WeakReference<Activity>(this);
        new HeartRateConsentTask().execute(reference);
    }

    public void play(View view){
        mp=MediaPlayer.create(this,R.raw.calm);
        mp.start();
    }

    private void navigate(final int itemId) {
        switch (itemId) {
            case R.id.nav_home:
                getFragmentManager().beginTransaction().replace(R.id.frame, homeFragment).commit(); break;
            case R.id.about:
                getFragmentManager().beginTransaction().replace(R.id.frame, aboutFragment).commit(); break;
            case R.id.nav_help_relax:
                getFragmentManager().beginTransaction().replace(R.id.frame, relaxFragment).commit(); break;
            case R.id.nav_help_contact:
                getFragmentManager().beginTransaction().replace(R.id.frame, contactFragment).commit(); break;
            case R.id.nav_data:
                getFragmentManager().beginTransaction().replace(R.id.frame, dataFragment).commit(); break;
            case R.id.help:
                getFragmentManager().beginTransaction().replace(R.id.frame, helpFragment).commit(); break;
            case R.id.preferences:
                getFragmentManager().beginTransaction().replace(R.id.frame, preferenceFragment).commit(); break;
            default:
                //skip
                break;
        }


    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.support.v7.appcompat.R.id.home) {
            return mDrawerToggle.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        // update highlighted item in the navigation menu
        menuItem.setChecked(true);
        mNavItemId = menuItem.getItemId();

        // allow some time after closing the drawer before performing real navigation
        // so the user can see what is happening
        mDrawerLayout.closeDrawer(GravityCompat.START);
        mDrawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(menuItem.getItemId());
            }
        }, DRAWER_CLOSE_DELAY_MS);
        return true;
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, mNavItemId);
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if(mLastLocation == null)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }



    private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                    } else {
                        appendToUI("You have not given this application consent to access heart rate data yet."
                                + " Please press the Heart Rate Consent button.\n");
                    }
                } else {

                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");

                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occurred: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }

    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(WeakReference<Activity>... params) {
            try {
                if (getConnectedBandClient()) {

                    if (params[0].get() != null) {
                        client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean consentGiven) {
                                new HeartRateSubscriptionTask().execute();
                            }
                        });
                    }
                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");

                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occurred: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }

//    public void getBandVersionInfo() {
//        String fwVersion = null;
//        String hwVersion = null;
//        try {
//            fwVersion = bandClient.getFirmwareVersion().await();
//            hwVersion = bandClient.getHardwareVersion().await();
//        } catch (InterruptedException ex) {
//        // handle InterruptedException
//        } catch (BandIOException ex) {
//    // handle BandIOException
//        } catch (BandException ex) {
//    // handle BandException
//        }
//    }

    //display to UI
    private void appendToUI(final String string) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtStatus.setText(string);
            }
        });
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }
}
