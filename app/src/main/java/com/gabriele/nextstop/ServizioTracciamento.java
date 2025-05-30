

package com.gabriele.nextstop;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;

import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;

public class ServizioTracciamento extends Service {

    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 12345678;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private String stopName;
    private double lat,lon;


    public ServizioTracciamento(){

    }
    @Override
    public void onCreate() {
        super.onCreate();


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createNotificationChannel();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;


                for (Location location : locationResult.getLocations()) {
                    if (FunzioniHelper.isVicinoAllaDestinazione(location.getLatitude(), location.getLongitude(), lat, lon)) {
                        System.out.println("ARRIVATOOOO");
                        Intent alertIntent = new Intent("com.gabriele.nextstop.ALERTA_FERMATA");
                        alertIntent.setClass(ServizioTracciamento.this, AlertReceiver.class);
                        sendBroadcast(alertIntent);
                        stopLocationUpdates();

                    }

                }
            }
        };
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.stopName = intent.getStringExtra("DEST_NAME");
        this.lat = intent.getDoubleExtra("DEST_LAT",0);
        this.lon = intent.getDoubleExtra("DEST_LON",0);

        Notification notification = getNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        startLocationUpdates();

        return START_STICKY;
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(" 🚀 Timer attivo : Fermata - "+AppState.getInstance().getFermataSelezionata().getValue().getStopName())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setColor(Color.parseColor("#4CAF50"))
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "NextStop";
            String description = "Abilita Notifiche per ottenere il tracciamento in background";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5_000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onDestroy() {
        AppState.getInstance().setTimerAttivo(false);
        AppState.getInstance().setFermataSelezionata(null);
        stopLocationUpdates();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }
}
