package adriantodorov.estimotetounityapi;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;

public class MyApplication extends Application {

    //private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // beaconManager = new BeaconManager(getApplicationContext());

        /*
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                showNotification(
                        "Beacon #1",
                        "Beacon approached.");
            }
            @Override
            public void onExitedRegion(Region region) {
                // could add an "exit" notification too
            }
        });
        */


        // add this below:
        /*
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            // beacon #1 (F8:63:81:24:61:34)
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"),
                        24884, 33060));
            }
        });
        */

    } // end onCreate

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
} // end Application