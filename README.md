# johnnylambada-location

This library makes it simple to track your current location using Google's 
FusedLocationProviderClient. It's based on the 
[Google Training WalkMyAndroid-Solution](https://github.com/google-developer-training/android-advanced/tree/master/WalkMyAndroid-Solution) 
repository. 

## Using this library

The following steps enable you to use this library.

### Set up jitpack.io for your project

Refer to [jitpack's documentation](https://jitpack.io/) for instructions.

### Add the library to your project

Add the following lines to your `app/build.gradle` dependencies:

```groovy
// https://github.com/johnnylambada/johnnylambada-location
implementation "com.github.johnnylambada:johnnylambada-location:0.0.2"
```

### Set up the service using the `LocationProvider`

```java
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
```

### Add a call to `LocationProvider.onRequestPermissionsResult` in your `Activity`'s `onRequestPermissionsResult`

```java
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationProvider.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
```

### Review the sample app to see how to integrate the library.

Review the [MainActivity](https://github.com/johnnylambada/johnnylambada-location/blob/master/app/src/main/java/app/MainActivity.java), 
it shows you how to use the `LocationProvider` from your activity.
