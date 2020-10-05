/**
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.johnnylambada.location;

import android.Manifest;
import android.app.Activity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationProvider {

    // Constants
    private static final int REQUEST_LOCATION_PERMISSION = 0x70CA;

    private final Activity activity;
    private final LifecycleOwner lifecycleOwner;
    private final String locationPermission;
    private final int accuracy;
    private final int intervalMs;
    private final LocationObserver locationObserver;
    private final Runnable onPermissionDeniedFirstTime;
    private final Runnable onPermissionDeniedAgain;
    private final Runnable onPermissionDeniedForever;

    // Location classes
    private boolean showPermissionRationaleFirstTime = true;
    private boolean isTrackingLocation;
    private FusedLocationProviderClient fusedLocationClient;

    private final LifecycleObserver lifecycleObserver = new LifecycleObserver();
    private final LocationCallback locationCallback = new LocationCallback() {
        /**
         * This is the callback that is triggered when the
         * FusedLocationClient updates your location.
         * @param locationResult The result containing the device location.
         */
        @Override
        public void onLocationResult(LocationResult locationResult) {
            // If tracking is turned on, reverse geocode into an address
            if (isTrackingLocation) {
                locationObserver.onLocation(locationResult.getLastLocation());
            }
        }
    };

    private LocationProvider(
            Activity activity,
            LifecycleOwner lifecycleOwner,
            String locationPermission,
            int accuracy,
            int intervalMs,
            LocationObserver locationObserver,
            Runnable onPermissionDeniedFirstTime,
            Runnable onPermissionDeniedAgain,
            Runnable onPermissionDeniedForever
    ) {
        this.activity = activity;
        this.lifecycleOwner = lifecycleOwner;
        this.locationPermission = locationPermission;
        this.accuracy = accuracy;
        this.intervalMs = intervalMs;
        this.locationObserver = locationObserver;
        this.onPermissionDeniedFirstTime = onPermissionDeniedFirstTime;
        this.onPermissionDeniedAgain = onPermissionDeniedAgain;
        this.onPermissionDeniedForever = onPermissionDeniedForever;

        isTrackingLocation = false;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        lifecycleOwner.getLifecycle().addObserver(lifecycleObserver);
    }

    public boolean isTrackingLocation() {
        return isTrackingLocation;
    }

    public void stopTrackingLocation() {
        if (isTrackingLocation) {
            isTrackingLocation = false;
        }
    }

    public void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(activity,locationPermission) == PackageManager.PERMISSION_GRANTED) {
            isTrackingLocation = true;
            fusedLocationClient.requestLocationUpdates(
                    getLocationRequest(),
                    locationCallback,
                    null /* Looper */
            );
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{locationPermission}, REQUEST_LOCATION_PERMISSION);
        }
    }

    /**
     * You must call this in your Activity's onRequestPermissionsResults
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTrackingLocation();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,locationPermission)) {
                        if (showPermissionRationaleFirstTime) {
                            showPermissionRationaleFirstTime = false;
                            onPermissionDeniedFirstTime.run();
                        } else {
                            onPermissionDeniedAgain.run();
                        }
                    } else {
                        onPermissionDeniedForever.run();
                    }
                }
                break;
        }
    }

    /**
     * Sets up the location request.
     *
     * @return The LocationRequest object containing the desired parameters.
     */
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(intervalMs);
        locationRequest.setFastestInterval(intervalMs/2);
        locationRequest.setPriority(accuracy);
        return locationRequest;
    }

    private class LifecycleObserver implements DefaultLifecycleObserver {
        @Override public void onResume(@NonNull LifecycleOwner owner) {
            if (isTrackingLocation) {
                startTrackingLocation();
            }
        }

        @Override public void onPause(@NonNull LifecycleOwner owner) {
            if (isTrackingLocation) {
                stopTrackingLocation();
                isTrackingLocation = true;
            }
        }

        @Override public void onDestroy(@NonNull LifecycleOwner owner) {
            lifecycleOwner.getLifecycle().removeObserver(lifecycleObserver);
        }
    }

    public void startAppSettings(String applicationId){
        // Build intent that displays the App settings screen.
        activity.startActivity(new Intent()
                .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", applicationId, null))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }

    public static class Builder {
        private final Activity activity;
        private LifecycleOwner lifecycleOwner;
        private String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        private int accuracy = LocationRequest.PRIORITY_HIGH_ACCURACY;
        private int intervalMs = 1000;
        private LocationObserver locationObserver;
        private Runnable onPermissionDeniedFirstTime;
        private Runnable onPermissionDeniedAgain;
        private Runnable onPermissionDeniedForever;

        public Builder(Activity activity){
            this.activity = activity;
            if (activity instanceof LifecycleOwner){
                lifecycleOwner = (LifecycleOwner) activity;
            }
        }

        public Builder lifecycleOwner(LifecycleOwner lifecycleOwner){
            this.lifecycleOwner = lifecycleOwner;
            return this;
        }

        public Builder locationPermission(String locationPermission){
            this.locationPermission = locationPermission;
            return this;
        }

        public Builder accuracy(int accuracy){
            this.accuracy = accuracy;
            return this;
        }

        public Builder intervalMs(int intervalMs){
            this.intervalMs = intervalMs;
            return this;
        }

        public Builder locationObserver(LocationObserver locationObserver){
            this.locationObserver = locationObserver;
            return this;
        }

        public Builder onPermissionDeniedFirstTime(Runnable onPermissionDeniedFirstTime){
            this.onPermissionDeniedFirstTime = onPermissionDeniedFirstTime;
            return this;
        }

        public Builder onPermissionDeniedAgain(Runnable onPermissionDeniedAgain){
            this.onPermissionDeniedAgain = onPermissionDeniedAgain;
            return this;
        }

        public Builder onPermissionDeniedForever(Runnable onPermissionDeniedForever){
            this.onPermissionDeniedForever = onPermissionDeniedForever;
            return this;
        }

        public LocationProvider build(){
            if (lifecycleOwner==null) throw new IllegalArgumentException("lifecycleOwner can't be null");
            if (locationObserver==null) throw new IllegalArgumentException("locationObserver can't be null");
            if (onPermissionDeniedFirstTime==null) throw new IllegalArgumentException("onPermissionDeniedFirstTime can't be null");
            if (onPermissionDeniedAgain==null) throw new IllegalArgumentException("onPermissionDeniedAgain can't be null");
            if (onPermissionDeniedForever==null) throw new IllegalArgumentException("onPermissionDeniedAgain can't be null");

            return new LocationProvider(
                    activity,
                    lifecycleOwner,
                    locationPermission,
                    accuracy,
                    intervalMs,
                    locationObserver,
                    onPermissionDeniedFirstTime,
                    onPermissionDeniedAgain,
                    onPermissionDeniedForever
            );
        }
    }
}
