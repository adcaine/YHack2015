package com.example.ehc.myapplication;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;
import android.app.PendingIntent;

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
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.app.Activity;
import android.app.AlertDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    private BandClient client = null;
    private TextView txtStatus;
    private long tStart, tStop, tDelta;
    double elapsedTime;
    private boolean firstPoll = true;
    private ArrayList<Integer> BPMList;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;

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

                if (event.getHeartRate() > 100 && event.getHeartRate() < 125) {
                    appendToUI(String.format("Heart Rate rising, in danger zone. Do you require assistance?"));

                    if (calcAvgBPM(BPMList) > 100) {
                        SMSContact();
                    }

                } else if (event.getHeartRate() >= 125) {
                    panicAction();
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

        if (avgBPM > 125) AlertContact();
        else firstPoll = !firstPoll;

        return avgBPM;
    }

    private void SMSContact() {
        SmsManager.getDefault().sendTextMessage("416-453-9845", null, "ALERT: Heart Rate rising, in danger zone.", null, null);

    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleApiClient();
        txtStatus = (TextView) findViewById(R.id.txtStatus);
//        BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
//        BandClient bandClient = BandClientManager.getInstance().create(getActivity(), pairedBands[0]);
        final WeakReference<Activity> reference = new WeakReference<Activity>(this);
        new HeartRateConsentTask().execute(reference);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void panicAction(){

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
