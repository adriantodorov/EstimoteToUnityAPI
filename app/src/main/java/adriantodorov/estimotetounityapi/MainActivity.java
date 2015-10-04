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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
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

    String tag = "AndroidLifecycle";

    private int startY = -1;
    private int segmentLength = -1;

    EditText sendDataEditText;
    EditText ipAddressEditText;

    ProgressDialog pDialog;

    Socket client = null;

    String ipAddressString;
    Byte dataByte;

    Button sendData;

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


        // set TextViews

        appId = (TextView) findViewById(R.id.appIdString);
        appToken = (TextView) findViewById(R.id.appTokenString);
        sendData = (Button) findViewById(R.id.sendDataButton);
        sendDataEditText = (EditText) findViewById(R.id.sendDataEditText);
        ipAddressEditText = (EditText) findViewById(R.id.ipEditText);
        fireTextView = (TextView) findViewById(R.id.distanceFire);
        extinguishTextView = (TextView) findViewById(R.id.distanceExtinguish);

        //  App ID & App Token can be taken from App section of Estimote Cloud.
        EstimoteSDK.initialize(getApplicationContext(), "wearhacks-2015-montreal-3mt", "e9cd76b118e99e34a719dc0ca2a1ea50");
        // Optional, debug logging.
        EstimoteSDK.enableDebugLogging(true);

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



                    }
                });
            }
        });



        appId.setText(EstimoteSDK.getAppId());
        appToken.setText(EstimoteSDK.getAppToken());

        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ipAddressString = ipAddressEditText.getText().toString();
                if (sendDataEditText.getText().toString().getBytes().length >= 1) {
                    dataByte = sendDataEditText.getText().toString().getBytes()[0];
                    new sendDataUnity().execute();
                } else {
                    Toast.makeText(getApplicationContext(), "No data entered.", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void updateDistanceView(Beacon foundBeacon) {



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
                Log.d("sendData", "Connecting to " + ipAddressString +
                        " on port " + port);
                if (client == null) {
                    client = new Socket(ipAddressString, port);
                }
                Log.d("sendData", "Just connected to "
                        + client.getRemoteSocketAddress());
                OutputStream outToServer = client.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                out.write(dataByte);
                client.close();
            } catch (IOException e) {
                Log.d("sendData", e.getStackTrace().toString());
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

    private int computeDotPosY(Beacon beacon) {
        // Let's put dot at the end of the scale when it's further than 6m.
        double distance = Math.min(Utils.computeAccuracy(beacon), 6.0);
        return startY + (int) (segmentLength * (distance / 6.0));
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