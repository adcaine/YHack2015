package com.example.ehc.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

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
    protected TextView mLatitudeText;
    protected TextView  mLongitudeText;
    private ListView listView;
    private int mNavItemId;
    private ActionBarDrawerToggle mDrawerToggle;
    private final Handler mDrawerActionHandler = new Handler();


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

                appendToUI(String.format("Heart Rate = %d beats per minute\n"
                        + "Quality = %s\n", event.getHeartRate(), event.getQuality()));

                if (event.getHeartRate() > 100 && event.getHeartRate() < 135) {
                    appendToUI(String.format("Heart Rate rising, in danger zone. Do you require assistance?"));

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(null);
                    builder1.setMessage("Heart Rate rising, in danger zone. Do you require assistance?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    builder1.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    if (calcAvgBPM(BPMList) > 120 ) {                   // send SMS if AVG heart rate is > 120
//                        SMSAction();
                    }

                }
            }
    };


//    private void CreateButton() {
//        try {
//// get the current set of tiles
//            List<BandTile> tiles =
//                    bandClient.getBandTileManager().getTiles().await();
//        } catch (BandException e) {
//// handle BandException
//        } catch (InterruptedException e) {
//
//// handle InterruptedException
//// }
//            // Create the small and tile icons from writable bitmaps.
//// Small icons are 24x24 pixels.
//            Bitmap smallIconBitmap = Bitmap.createBitmap(24, 24, null);
//            BandIcon smallIcon = BandIcon.toBandIcon(smallIconBitmap);
//// Tile icons are 46x46 pixels for Microsoft Band 1 and 48x48 pixels
//// for Microsoft Band 2.
//            Bitmap tileIconBitmap = Bitmap.createBitmap(46, 46, null);
//            BandIcon tileIcon = BandIcon.toBandIcon(tileIconBitmap);
//// create a new UUID for the tile
//            UUID tileUuid = UUID.randomUUID();
//// create a new BandTile using the builder
//// add optional small icon
//// enable badging (the count of unread messages)
//            BandTile tile = new BandTile.Builder(tileUuid, "YHack", tileIcon)
//                    .setTileSmallIcon(smallIcon).setBadgingEnabled(true).build();
//            tile.IsBadingEnabled = true;
//            try {
//                if (bandClient.getBandTileManager().addTile(getActivity(),
//                        tile).await()) {
//// do work if the tile was successfully created
//                }
//            } catch (BandException e) {
//// handle BandException
//            } catch (InterruptedException e) {
//// handle InterruptedException
//            }
//        }
//    }

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

//    private void SMSAction() {
//        SmsManager.getDefault().sendTextMessage(getContactNumber(), null, "ALERT: I need help, high heart rate.", null, null);
//    }


    private void AlertAction(){
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse(getContactNumber()));
    }


    private String getContactNumber(){
        //TODO get the specified phone number
        return "16477709721";
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();


        txtStatus = (TextView) findViewById(R.id.txtStatus);
        firstItem = (TextView) findViewById(R.id.firstListItem);
        secondItem = (TextView) findViewById(R.id.secondListItem);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);




        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //setting up selected item listener
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();

                        switch (menuItem.getItemId()) {
                            case R.id.nav_home:
                                Toast.makeText(getApplicationContext(), "Home is clicked", Toast.LENGTH_LONG).show();
                                ContentFragment fragment = new ContentFragment();
                                android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.frame, fragment);
                                fragmentTransaction.commit();
                                return true;
                            default: return true;
                        }
                    }
                });


        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

//        BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
//        BandClient bandClient = BandClientManager.getInstance().create(getActivity(), pairedBands[0]);
        final WeakReference<Activity> reference = new WeakReference<Activity>(this);
        new HeartRateConsentTask().execute(reference);
    }

    private void setupDrawerContent(NavigationView navigationView) {

        addItemsRunTime(navigationView);

        //setting up selected item listener
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void addItemsRunTime(NavigationView navigationView) {

        //adding items run time
        final Menu menu = navigationView.getMenu();
        for (int i = 1; i <= 3; i++) {
            menu.add("Runtime item "+ i);
        }

        // adding a section and items into it
        final SubMenu subMenu = menu.addSubMenu("SubMenu Title");
        for (int i = 1; i <= 2; i++) {
            subMenu.add("SubMenu Item " + i);
        }

        // refreshing navigation drawer adapter
        for (int i = 0, count = mNavigationView.getChildCount(); i < count; i++) {
            final View child = mNavigationView.getChildAt(i);
            if (child != null && child instanceof ListView) {
                final ListView menuView = (ListView) child;
                final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                wrapped.notifyDataSetChanged();
            }
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                Toast.makeText(getApplicationContext(),"home selected",Toast.LENGTH_LONG);
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
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
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
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
