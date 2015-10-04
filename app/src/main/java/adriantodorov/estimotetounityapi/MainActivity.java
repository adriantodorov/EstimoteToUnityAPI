package adriantodorov.estimotetounityapi;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.MacAddress;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Y positions are relative to height of bg_distance image.
    private static final double RELATIVE_START_POS = 320.0 / 1110.0;
    private static final double RELATIVE_STOP_POS = 885.0 / 1110.0;

    TextView appId;
    TextView appToken;

    TextView fireTextView;
    TextView extinguishTextView;

    TextView fireMinor;
    TextView extMinor;

    String tag = "AndroidLifecycle";

    EditText sendDataEditText;
    EditText ipAddressEditText;

    boolean isOnFire = false;
    boolean isExtinguished = false;

    boolean isActivated = false;

    ProgressDialog pDialog;

    Socket client = null;

    String ipAddressString;
    Byte dataByte;

    Button activateButton;
    Button disableButton;

    int port = 15000;

    boolean dataSent = false;

    private BeaconManager beaconManager;

    private Beacon fireBeacon;
    private Beacon extinguishBeacon;

    private List<Beacon> beaconList;

    private Region region;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // both estimotes have the same UUID
        region = new Region(
                "monitored region",
                UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), null, null);


        // set TextViews
        appId = (TextView) findViewById(R.id.appIdString);
        appToken = (TextView) findViewById(R.id.appTokenString);

        activateButton = (Button) findViewById(R.id.activateButton);
        disableButton = (Button) findViewById(R.id.disactivateButton);

        disableButton.setVisibility(View.INVISIBLE);

        sendDataEditText = (EditText) findViewById(R.id.sendDataEditText);
        ipAddressEditText = (EditText) findViewById(R.id.ipEditText);

        fireTextView = (TextView) findViewById(R.id.distanceFire);
        extinguishTextView = (TextView) findViewById(R.id.distanceExtinguish);

        fireMinor = (TextView) findViewById(R.id.minorFire);
        extMinor = (TextView) findViewById(R.id.minorExting);

        //  App ID & App Token can be taken from App section of Estimote Cloud.
        EstimoteSDK.initialize(getApplicationContext(), "wearhacks-2015-montreal-3mt", "e9cd76b118e99e34a719dc0ca2a1ea50");
        // Optional, debug logging.
        EstimoteSDK.enableDebugLogging(false);

        UUID fireBeaconID = UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d");
        MacAddress fireBeaconMacAddress = MacAddress.fromString("E6:1E:96:6B:E2:0C");
        fireBeacon = new Beacon (fireBeaconID, "fireBeacon", fireBeaconMacAddress, 57868, 38507, 0, 0);

        UUID extinguishID = UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d");
        MacAddress extinguishMacAddress = MacAddress.fromString("F8:63:81:24:61:34");
        extinguishBeacon = new Beacon (extinguishID, "extinguishBeacon", extinguishMacAddress, 24884, 33060, 0, 0);

        beaconList = new ArrayList<Beacon>();
        beaconList.add(fireBeacon);
        beaconList.add(extinguishBeacon);

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> rangedBeacons) {
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Beacon rangedBeacon : rangedBeacons) {
                            updateDistanceView(rangedBeacon);
                        }

                    }
                });
            }
        });



        appId.setText(EstimoteSDK.getAppId());
        appToken.setText(EstimoteSDK.getAppToken());


        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ipAddressString = ipAddressEditText.getText().toString();

                if (ipAddressEditText.getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "No entered information for IP address.",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    isActivated = true;
                    activateButton.setVisibility(View.INVISIBLE);
                    disableButton.setVisibility(View.VISIBLE);
                }

            }
        });

        disableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                isActivated = false;

                activateButton.setVisibility(View.VISIBLE);
                disableButton.setVisibility(View.INVISIBLE);

                }
        });



    }

    private void updateDistanceView(Beacon foundBeacon) {

        // fireBeacon
        if (foundBeacon.getMajor() == fireBeacon.getMajor()
                && foundBeacon.getMinor() == fireBeacon.getMinor())
        {
            double fireDistance =  computeDotPosY(foundBeacon);
            fireTextView.setText(String.valueOf(fireDistance) + " m.");
            fireMinor.setText(String.valueOf(foundBeacon.getMinor()));

            // do the logic stuff
            if (fireDistance < 0.50)
            {
                isOnFire = true;
                if (isActivated)
                {
                    new sendDataUnity().execute();
                }
            }
            else
            {
                isOnFire = false;
            }

        }

        // extunguishBeacon
        if (foundBeacon.getMajor() == extinguishBeacon.getMajor()
                && foundBeacon.getMinor() == extinguishBeacon.getMinor())
        {
            double extinguishDistance = computeDotPosY(foundBeacon);
            extinguishTextView.setText(String.valueOf(extinguishDistance) + " m.");
            extMinor.setText(String.valueOf(foundBeacon.getMinor()));

            // do the logic stuff
            if (extinguishDistance < 0.50)
            {
                isExtinguished = true;
                if (isActivated)
                {
                    new sendDataUnity().execute();
                }
            }
            else
            {
                isExtinguished = false;
            }

        }

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     */
    class sendDataUnity extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Sending data to Unity...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All trees from url
         */
        protected String doInBackground(String... args) {

            try {
                Log.d("activateButton", "Connecting to " + ipAddressString +
                        " on port " + port);
                if (client == null) {
                    client = new Socket(ipAddressString, port);
                }
                Log.d("activateButton", "Just connected to "
                        + client.getRemoteSocketAddress());
                OutputStream outToServer = client.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                if (isOnFire)
                {
                    dataByte = 102;
                }
                else if (isExtinguished)
                {
                    dataByte = 101;
                }
                out.write(dataByte);
                client.close();
            } catch (IOException e) {
                Log.d("activateButton", e.getStackTrace().toString());
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all trees
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {

                    if (dataSent) {
                        Toast.makeText(getApplicationContext(), "Data sent.", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private double computeDotPosY(Beacon beacon) {
        // Let's put dot at the end of the scale when it's further than 6m.
        double distance = Math.min(Utils.computeAccuracy(beacon), 6.0);
        return distance;
    }


    public void onStart() {
        super.onStart();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
        Log.d(tag, "In the onStart() event");
    }

    public void onRestart() {
        super.onRestart();
        Log.d(tag, "In the onRestart() event");
    }

    public void onResume() {
        super.onResume();
        Log.d(tag, "In the onResume() event");
    }

    public void onPause() {
        super.onPause();
        Log.d(tag, "In the onPause() event");
    }

    public void onStop() {
        beaconManager.disconnect();
        Log.d(tag, "In the onStop() event");
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(tag, "In the onDestroy() event");
    }
}