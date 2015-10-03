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

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    TextView appId;
    TextView appToken;

    EditText sendDataEditText;
    EditText ipAddressEditText;

    ProgressDialog pDialog;

    String ipAddressString;
    String dataString;

    Button sendData;

    int port = 15000;

    boolean dataSent = false;

    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appId = (TextView) findViewById(R.id.appIdString);
        appToken = (TextView) findViewById(R.id.appTokenString);
        sendData = (Button) findViewById(R.id.sendDataButton);
        sendDataEditText = (EditText) findViewById(R.id.sendDataEditText);
        ipAddressEditText = (EditText) findViewById(R.id.ipEditText);

        //  App ID & App Token can be taken from App section of Estimote Cloud.
        EstimoteSDK.initialize(getApplicationContext(), "wearhacks-2015-montreal-3mt", "e9cd76b118e99e34a719dc0ca2a1ea50");
        // Optional, debug logging.
        EstimoteSDK.enableDebugLogging(true);

        appId.setText(EstimoteSDK.getAppId());
        appToken.setText(EstimoteSDK.getAppToken());

        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ipAddressString = ipAddressEditText.getText().toString();
                dataString = sendDataEditText.getText().toString();

                new sendDataUnity().execute();
            }
        });




    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class sendDataUnity extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
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
         * */
        protected String doInBackground(String... args) {

            try
            {
                Log.d("sendData", "Connecting to " + ipAddressString +
                        " on port " + port);
                Socket client = new Socket(ipAddressString, port);
                Log.d("sendData", "Just connected to "
                        + client.getRemoteSocketAddress());
                OutputStream outToServer = client.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                out.write(Byte.parseByte(dataString));
                client.close();
            }catch(IOException e)
            {
                Log.d("sendData", e.getStackTrace().toString());
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all trees
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {

                    if (dataSent)
                    {
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
}
