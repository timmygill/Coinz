package ilp.coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener, DownloadFileResponse {

    private final String tag = "MainActivity";

    private MapView mapView;
    private MapboxMap map;

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;

    private String downloadDate = ""; //YYYY/MM/DD
    private String coindata = "";
    private final String preferencesFile = "MyPrefsFile";

    private ArrayList<Coin> coinsArray = new ArrayList<Coin>();
    private ExchangeRates exchangeRates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapboxMapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap){
        if(mapboxMap == null){
            Log.d(tag, "[onMapReady] mapboxMap is null");
        } else {
            map = mapboxMap;
            //Set user interface options
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);

            //Make location information available
            enableLocation();

        }
    }

    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d(tag, "Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Log.d(tag, "Permissions are not granted");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine(){
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000);
        locationEngine.setFastestInterval(1000);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null){
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer(){
        if(mapView == null){
            Log.d(tag, "mapView is null") ;
        } else {
            if (map == null){
                Log.d(tag, "map is null");
            }else{
                locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }

    private void setCameraPosition(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onLocationChanged(Location location){
        if(location == null){
            Log.d(tag, "[onLocationChanged] location is null");
        } else {
            Log.d(tag, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);
        }
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected(){
        Log.d(tag, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
        Log.d(tag, "[onConnected] location updates granted");
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain){
        Log.d(tag, "Permissions: " + permissionsToExplain.toString());
    }

    @Override
    public void onPermissionResult(boolean granted){
        Log.d(tag, "[onPermissionResult] granted == " + granted);
        if(granted){
            enableLocation();
        } else {
            //TODO: dialogue with user
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();

        //Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        downloadDate = settings.getString("lastDownloadDate", "");
        Log.d(tag, "[onStart] Recalled lastDownloadDate is '" + downloadDate + "'");

        downloadTodaysMap(settings);

        //TODO: login dialogue, change def val to false
        boolean userLoggedIn = settings.getBoolean("isUserLoggedIn", true);

        if(!userLoggedIn){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }



    }

    //Download todays coin map and update preferences
    private void downloadTodaysMap(SharedPreferences settings){

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String todaysDate = df.format(Calendar.getInstance().getTime());

        //TODO: change after testing
        //if (!(todaysDate.equals(downloadDate))) {
        if (true){
            downloadDate = todaysDate;

            String downloadString = "https://homepages.inf.ed.ac.uk/stg/coinz/" + downloadDate + "/coinzmap.geojson";
            DownloadFileTask downloadMapTask = new DownloadFileTask();
            downloadMapTask.delegate = this;
            downloadMapTask.execute(downloadString);


            SharedPreferences.Editor editor = settings.edit();
            editor.putString("lastDownloadDate", downloadDate);
            editor.commit();
        }
    }


    @Override
    public void downloadFinish(String result){
        coindata = result;
        //Log.d(tag, "result: " + coindata);
        try {
            JSONObject coindatajson = new JSONObject(coindata);
            exchangeRates = new ExchangeRates(coindatajson.getJSONObject("rates"));

            JSONArray coinsjson = coindatajson.getJSONArray("features");
            for (int i = 0; i < coinsjson.length(); i++) {
                   coinsArray.add(new Coin(coinsjson.getJSONObject(i)));
            }
        } catch (JSONException e){
            Log.d(tag, e.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        Log.d(tag, "Stopping and saving coin data");
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput("coinzdata.geojson", Context.MODE_PRIVATE));
            outputStreamWriter.write(coindata);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
