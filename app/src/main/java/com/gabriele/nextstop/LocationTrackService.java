package com.gabriele.nextstop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LocationTrackService {

    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final LocationUpdateListener listener;
    private final Executor executor;

    public interface LocationUpdateListener {
        void onLocationUpdate(double latitude, double longitude);
    }

    @SuppressLint("MissingPermission")
    public LocationTrackService(Context context, LocationUpdateListener listener) {
        this.listener = listener;
        this.executor = Executors.newSingleThreadExecutor();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                .setIntervalMillis(1000)
                .setMinUpdateIntervalMillis(1000)
                .build();
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        if (locationCallback != null) return;

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (result == null) return;

                for (Location location : result.getLocations()) {
                    listener.onLocationUpdate(location.getLatitude(), location.getLongitude());
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                executor,
                locationCallback
        );
    }

    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }
}
