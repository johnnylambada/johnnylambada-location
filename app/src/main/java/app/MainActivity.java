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

package app;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import com.johnnylambada.location.LocationProvider;

import app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        locationProvider = new LocationProvider.Builder(this)
//                .lifecycleOwner(this)   // not necessary for activity when activity==lifecycleowner
//                .locationPermission(Manifest.permission.ACCESS_FINE_LOCATION) // default FINE
//                .accuracy(LocationRequest.PRIORITY_HIGH_ACCURACY) // default HIGH_ACCURACY
//                .intervalMs(1000)   // 1 second
                .locationObserver(location -> {
                    final String prev = binding.answer.getText().toString();
                    final String updated = prev+"\n"+"(" + location.getLatitude() + ", " + location.getLongitude() + ")";
                    binding.answer.setText(updated);
                })
                .onPermissionDeniedFirstTime(() -> {
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            "Denied: This app needs location permission.",
                            Snackbar.LENGTH_LONG)
                            .setAction("OK", __ -> {})
                            .show();
                })
                .onPermissionDeniedAgain(()->{
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            "Rationale: This app can't show location without permission.",
                            Snackbar.LENGTH_LONG)
                            .setAction("OK", __ -> {})
                            .show();

                })
                .onPermissionDeniedForever(()->{
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            "Settings: This app can't show location without permission. Please update settings.",
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction("Settings", __ -> locationProvider.startAppSettings(BuildConfig.APPLICATION_ID))
                            .show();

                })
                .build();

        binding.button.setOnClickListener(__->{
            if (locationProvider.isTrackingLocation()){
                locationProvider.stopTrackingLocation();
            } else {
                locationProvider.startTrackingLocation();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationProvider.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
}
