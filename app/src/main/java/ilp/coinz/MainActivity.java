package ilp.coinz;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements DownloadFileResponse {

    final String tag = "MainActivity";

    private String todaysDate = ""; //YYYY/MM/DD

    private Player player;

    private HashMap<String, Coin> coinsCollection = new HashMap<>();
    private HashMap<String, Marker> markers = new HashMap<>();
    private ArrayList<String> collectedIDs = new ArrayList<>();
    private ArrayList<String> bankedIDs = new ArrayList<>();
    private HashMap<String, Double> exchangeRates;

    private String jsonResult;

    private double spareChangeValue;
    private double loginReward;

    private boolean coinsReady = false;
    private boolean navBarReady = false;
    private boolean locationGranted = false;

    BottomNavigationView navigation;

    private MapFragment mapFragment;

    private FragmentManager fragmentManager = getSupportFragmentManager();

    private String currentFragment;

    private final int REQUEST_LOCATION = 1111;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get user
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        player = new Player();

        //if user has not autheticated, take to login activity, else set local player instance from Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            Log.d(tag, "No user currently logged in");
            finish();
        } else {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            player.setEmail(currentUser.getEmail());
            db.collection("/user/" + player.getEmail() + "/Player")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                                player.setGoldBalance(doc.getDouble("goldBalance"));
                                player.setLastLogin(doc.getString("lastLogin"));
                                player.setConsecLogins(doc.getLong("consecLogins").intValue());
                                player.setBankedCount(doc.getLong("bankedCount").intValue());
                                player.setLifetimeCoins(doc.getLong("lifetimeCoins").intValue());
                                player.setLifetimeGold(doc.getDouble("lifetimeGold"));
                                player.setLifetimeDistance(doc.getDouble("lifetimeDistance").floatValue());
                                player.setMulti(doc.getLong("multi").intValue());
                                player.setRadius(doc.getLong("radius").intValue());
                            }

                            initiateMapFragment();

                        } else {
                            Log.d(tag, "Error getting documents: ", task.getException());
                        }
                    });

        }

        //initialise interface
        setContentView(R.layout.activity_main);
        navigation = findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        spareChangeValue = 0.0;
        loginReward = 0.0;

        todaysDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Calendar.getInstance().getTime());


    }

    public void initiateMapFragment(){

        Mapbox.getInstance(this, getString(R.string.access_token));

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        //Make map fragment, shown by default
        MapboxMapOptions options = new MapboxMapOptions()
                .styleUrl(Style.DARK)
                .camera(new CameraPosition.Builder()
                        .target(new LatLng(55.944, -3.188396))
                        .zoom(15)
                        .build());
        mapFragment = MapFragment.newInstance(options);

        transaction.add(R.id.fragment_container, mapFragment, "mapFragment");
        transaction.commit();
        currentFragment = "mapFragment";

        //unlock navigation
        navBarReady = true;
    }



        private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //navbar locked until relevant variables populated to prevent unwanted behaviour
                if (navBarReady) {
                    Fragment fragment;
                    switch (item.getItemId()) {
                        case R.id.navigation_map:
                            loadFragment(mapFragment, "mapFragment");
                            Log.d(tag, "Switching to map");
                            return true;
                        case R.id.navigation_bank:
                            //show bank or transfer fragment, dependent on banking cap being reached
                            if (player.getBankedCount() < 25) {
                                fragment = new BankFragment();
                                loadFragment(fragment, "bankFragment");
                                Log.d(tag, "Switching to bank");
                            } else {
                                fragment = new TransferFragment();
                                loadFragment(fragment, "transferFragment");
                                Log.d(tag, "Switching to transfer");
                            }

                            return true;
                        case R.id.navigation_shop:
                            fragment = new ShopFragment();
                            loadFragment(fragment, "shopFragment");
                            Log.d(tag, "Switching to shop");
                            return true;
                        case R.id.navigation_profile:
                            fragment = new ProfileFragment();
                            loadFragment(fragment, "profileFragment");
                            Log.d(tag, "Switching to profile");
                            return true;
                    }
                }
                return false;
            }
        };


    public void loadFragment(Fragment fragment, String tag) {
        // load fragment - if switching from map, just hide instead of destroy
        if (!tag.equals(currentFragment)) {
            if (currentFragment.equals("mapFragment")) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(mapFragment);
                transaction.add(R.id.fragment_container, fragment, tag);
                currentFragment = tag;
                transaction.commit();
            } else {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Fragment old = fragmentManager. findFragmentByTag(currentFragment);
                transaction.detach(Objects.requireNonNull(old));
                if (tag.equals("mapFragment")) {
                    transaction.show(mapFragment);
                } else {
                    transaction.add(R.id.fragment_container, fragment, tag);
                }
                currentFragment = tag;
                transaction.commit();
            }
        }
    }

    //Download todays coin map and update preferences
    public void downloadTodaysMap() {

        Log.d(tag, "Player last launched app on " + player.getLastLogin());
        Log.d(tag, "Today's date is " + todaysDate);

        //On new day, delete old coins from Firebase and update most recent map date, calculate user login reward.
        if (!(todaysDate.equals(player.getLastLogin()))) {

            Log.d(tag, "First login today");

            player.setLastLogin(todaysDate);
            player.setConsecLogins(player.getConsecLogins() + 1);
            player.setBankedCount(0);
            loginReward = player.getConsecLogins() * 100;

            //call downloadmap after clearing wallet
            clearFBWallet("Wallet");

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("user").document(player.getEmail()).collection("Player").document(player.getEmail()).set(player);

        } else { //simply download coins again
            Log.d(tag, "Already logged in today");
            downloadMap(todaysDate);
        }
    }


    //Remove all entries from a coin collection on Firebase
    private void clearFBWallet(String walletToClear){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("/user/" + player.getEmail() + "/" + walletToClear)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())){
                            doc.getReference().delete();
                        }
                        downloadMap(todaysDate);
                    } else {
                        Log.d(tag, "Error getting documents: ", task.getException());
                    }
                });
    }



    private void downloadMap(String date){
        String downloadString = "https://homepages.inf.ed.ac.uk/stg/coinz/" + date + "/coinzmap.geojson";
        DownloadFileTask downloadMapTask = new DownloadFileTask();
        downloadMapTask.setDelegate(this);
        downloadMapTask.execute(downloadString);
    }

    @Override
    public void downloadFinish(String result){
        Log.d(tag, "JSON downloaded");
        this.jsonResult = result;
        downloadFBWallet();

    }

    //download players collected coins for today
    public void downloadFBWallet(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("/user/" + player.getEmail() + "/Wallet")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                            if(doc.getBoolean("banked")){
                                //create collection of coins marked as banked
                                bankedIDs.add(Objects.requireNonNull(doc.get("id")).toString());
                            }
                            //all coins present must have been collected
                            collectedIDs.add(Objects.requireNonNull(doc.get("id")).toString());
                        }
                        parseJson();
                    } else {
                        Log.d(tag, "Error getting documents: ", task.getException());
                    }

                });
    }


    private void parseJson(){

        try {
            //iterate over currencies, store mapping to rates
            JSONObject coindatajson = new JSONObject(jsonResult);

            exchangeRates = new HashMap<>();
            JSONObject exchanges = coindatajson.getJSONObject("rates");

            Iterator<String> currencies = exchanges.keys();

            while(currencies.hasNext()) {
                String currency = currencies.next();
                exchangeRates.put(currency, exchanges.getDouble(currency));
                Log.d(tag, currency + ": " + exchanges.getDouble(currency) + "");

            }

            //iterate over coins and create map from id to Coin object
            JSONArray coinsjson = coindatajson.getJSONArray("features");
            for (int i = 0; i < coinsjson.length(); i++) {
                Coin tempCoin = new Coin(coinsjson.getJSONObject(i));
                coinsCollection.put(tempCoin.getId(), tempCoin);
            }

            //set banked based on Firebase
            for (String id : bankedIDs){
                if(coinsCollection.containsKey(id)){ Objects.requireNonNull(coinsCollection.get(id)).setBanked(true); }
            }
            //set collected based on Firebase
            for (String id : collectedIDs){
                if(coinsCollection.containsKey(id)){ Objects.requireNonNull(coinsCollection.get(id)).setCollected(true); }
            }
        } catch (JSONException e){
            Log.d(tag, e.toString());
            mapFragment.displayJsonError();
        }
        mapFragment.addCoinsToMap();
        checkSpareChange();
    }

    //check if user has been transferred coins since last launch of app
    private void checkSpareChange(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("/user/" + player.getEmail() + "/SpareChange")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //calculate value of coins earned
                        for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                            Double value = doc.getDouble("value");
                            String currency = doc.getString("currency");
                            spareChangeValue += value * exchangeRates.get(currency);
                            Log.d(tag, spareChangeValue + "");
                        }
                        if (spareChangeValue > 0.0){
                            //give user option to convert
                            spawnSCPopup();
                        } else if (loginReward > 0.0){
                            //if user has login reward waiting, give popup
                            spawnRewardPopup();
                        }
                    }
                });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void spawnSCPopup(){
        Log.d(tag, "Spawning spare change popup");
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.sparechange_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        TextView spareLabel = popupView.findViewById(R.id.spareValue);
        spareLabel.setText(String.format("%.3f", spareChangeValue ) + " Gold");

        Button spareButton = popupView.findViewById(R.id.spareButton);
        spareButton.setOnClickListener(v -> {
            player.setGoldBalance(player.getGoldBalance() + spareChangeValue);
            player.setLifetimeGold(player.getLifetimeGold() + spareChangeValue);;
            spareChangeValue = 0.0;

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("user").document(player.getEmail()).collection("Player").document(player.getEmail()).set(player);

            clearFBWallet("SpareChange");

            popupWindow.dismiss();

            //check if reward is waiting
            if(loginReward > 0.0){ spawnRewardPopup(); }

        });

        // show the popup window
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            if(loginReward > 0.0){ spawnRewardPopup(); }
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void spawnRewardPopup(){
        Log.d(tag, "Spawning login reward popup");
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.reward_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        TextView rewardValue = popupView.findViewById(R.id.rewardValue);
        rewardValue.setText(player.getConsecLogins() + " days = " + loginReward + " Gold");

        player.setGoldBalance(player.getGoldBalance() + loginReward);
        player.setLifetimeGold(player.getLifetimeGold() + loginReward);
        loginReward = 0.0;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(player.getEmail()).collection("Player").document(player.getEmail()).set(player);

        Button spareButton = popupView.findViewById(R.id.rewardButton);
        spareButton.setOnClickListener(v -> {
            popupWindow.dismiss();

        });

        // show the popup window
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener((v, event) -> {

            popupWindow.dismiss();

            return true;
        });
    }

    public void requestLocation(){
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION );
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == REQUEST_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.d(tag, "Callback: Permissions granted");
            locationGranted = true;
            mapFragment.enableLocation();
        } else {
            Log.d(tag, "Callback: Permissions not granted");
            mapFragment.displayPermissionError();
            requestLocation();
        }
    }



    @Override
public void onPause(){
        super.onPause();
}

    public boolean isLocationGranted() {
        return locationGranted;
    }

    public Player getPlayer() {
        return player;
    }

    public HashMap<String, Coin> getCoinsCollection() {
        return coinsCollection;
    }

    public HashMap<String, Marker> getMarkers() {
        return markers;
    }

    public ArrayList<String> getCollectedIDs() {
        return collectedIDs;
    }

    public HashMap<String, Double> getExchangeRates() {
        return exchangeRates;
    }

    public boolean isCoinsReady() {
        return coinsReady;
    }

    public void setCoinsReady(boolean coinsReady) {
        this.coinsReady = coinsReady;
    }


}
