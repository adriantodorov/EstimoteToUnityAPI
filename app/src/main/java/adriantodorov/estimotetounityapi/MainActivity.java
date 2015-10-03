package adriantodorov.estimotetounityapi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    TextView appId;
    TextView appToken;

    EditText sendDataString;

    Button sendData;

    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appId = (TextView) findViewById(R.id.appIdString);
        appToken = (TextView) findViewById(R.id.appTokenString);
        sendData = (Button) findViewById(R.id.sendDataButton);
        sendDataString = (EditText) findViewById(R.id.sendDataEditText);

        //  App ID & App Token can be taken from App section of Estimote Cloud.
        EstimoteSDK.initialize(getApplicationContext(), "wearhacks-2015-montreal-3mt", "e9cd76b118e99e34a719dc0ca2a1ea50");
        // Optional, debug logging.
        EstimoteSDK.enableDebugLogging(true);

        appId.setText(EstimoteSDK.getAppId());
        appToken.setText(EstimoteSDK.getAppToken());

        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Socket socket = null;
                DataOutputStream dataOutputStream = null;
                DataInputStream dataInputStream = null;

                try {
                    socket = new Socket("192.168.1.23", 15000);
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream.writeUTF(sendDataString.getText().toString());
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                finally{
                    if (socket != null){
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    if (dataOutputStream != null){
                        try {
                            dataOutputStream.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    if (dataInputStream != null){
                        try {
                            dataInputStream.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        });




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
