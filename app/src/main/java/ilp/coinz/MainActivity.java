package ilp.coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    final String tag = "MainActivity";

    public String todaysDate = ""; //YYYY/MM/DD
    private String downloadDate = ""; //YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile";

    public HashMap<String, Coin> coinsCollection = new HashMap<>();
    public HashMap<String, Marker> markers = new HashMap<>();
    public ArrayList<String> collectedIDs = new ArrayList<>();
    public ArrayList<String> bankedIDs = new ArrayList<>();
    public int bankedCount;
    public double goldBalance;
    public HashMap<String, Double> exchangeRates;
    public String jsonResult;
    public double spareChangeValue;

    public boolean coinsReady = false;

    private MapFragment mapFragment;

    private FragmentManager fragmentManager = getSupportFragmentManager();

    private String currentFragment;

    public int playerMulti;
    public int playerRadius;

    public int lifetimeCoins;
    public double lifetimeGold;
    public float lifetimeDistance;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(this, LoginActivity.class));
        }

        setContentView(R.layout.activity_main);
        Mapbox.getInstance(this, getString(R.string.access_token));

        bankedCount = 0;
        goldBalance = 0.0;
        spareChangeValue = 0.0;

        playerMulti = 1;
        playerRadius = 25;

        lifetimeCoins = 0;
        lifetimeGold = 0.0;
        lifetimeDistance = 0;

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


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_map:
                    loadFragment(mapFragment, "mapFragment");
                    Log.d(tag, "map");
                    return true;
                case R.id.navigation_bank:
                    if(bankedCount < 25) {
                        fragment = new BankFragment();
                    } else {
                        fragment = new TransferFragment();
                    }
                    loadFragment(fragment, "bankFragment");
                    Log.d(tag, "bank");
                    return true;
                case R.id.navigation_shop:
                    fragment = new ShopFragment();
                    loadFragment(fragment, "shopFragment");
                    Log.d(tag, "shop");
                    return true;
                case R.id.navigation_profile:
                    fragment = new ProfileFragment();
                    loadFragment(fragment, "profileFragment");
                    Log.d(tag, "profile");
                    return true;
            }
            return false;
        }
    };


    private void loadFragment(Fragment fragment, String tag) {
        // load fragment
        if (!tag.equals(currentFragment)) {
            if (currentFragment.equals("mapFragment")) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(mapFragment);
                transaction.add(R.id.fragment_container, fragment, tag);
                currentFragment = tag;
                transaction.commit();
            } else {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Fragment old = fragmentManager.findFragmentByTag(currentFragment);
                transaction.detach(old);
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
    public void downloadTodaysMap(){

        //Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        downloadDate = settings.getString("lastDownloadDate", "");
        Log.d(tag, "[onStart] Recalled lastDownloadDate is '" + downloadDate + "'");

        //On new map, delete old coins from Firebase and update most recent map date.
        if (!(todaysDate.equals(downloadDate))) {
            clearFBWallet("Wallet");

            downloadDate = todaysDate;

            SharedPreferences.Editor editor = settings.edit();
            editor.putString("lastDownloadDate", downloadDate);
            editor.commit();
        } else {
            downloadMap(todaysDate);
        }
    }
    public void downloadMap(String date){
        String downloadString = "https://homepages.inf.ed.ac.uk/stg/coinz/" + date + "/coinzmap.geojson";
        DownloadFileTask downloadMapTask = new DownloadFileTask();
        downloadMapTask.delegate = (DownloadFileResponse) fragmentManager.findFragmentByTag("mapFragment");
        downloadMapTask.execute(downloadString);
    }

    public void parseJson(){
        try {
            JSONObject coindatajson = new JSONObject(jsonResult);

            exchangeRates = new HashMap<>();
            JSONObject exchanges = coindatajson.getJSONObject("rates");

            Iterator<String> currencies = exchanges.keys();

            while(currencies.hasNext()) {
                String currency = currencies.next();
                Log.d(tag, currency);
                exchangeRates.put(currency, exchanges.getDouble(currency));
                Log.d(tag, exchanges.getDouble(currency) + "");

            }

            JSONArray coinsjson = coindatajson.getJSONArray("features");
            for (int i = 0; i < coinsjson.length(); i++) {
                Coin tempCoin = new Coin(coinsjson.getJSONObject(i));
                coinsCollection.put(tempCoin.getId(), tempCoin);
            }

            for (String id : bankedIDs){
                if(coinsCollection.containsKey(id)){ coinsCollection.get(id).setBanked(true); }
            }
            for (String id : collectedIDs){
                if(coinsCollection.containsKey(id)){ coinsCollection.get(id).setCollected(true); }
            }


        } catch (JSONException e){
            Log.d(tag, e.toString());
        }
        mapFragment.addCoinsToMap();
        checkSpareChange();
    }

    public void clearFBWallet(String walletToClear){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        db.collection("/user/" + email + "/" + walletToClear)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot doc : task.getResult()){
                                doc.getReference().delete();
                            }
                            downloadMap(todaysDate);
                        } else {
                            Log.d(tag, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void downloadFBWallet(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        db.collection("/user/" + email + "/Wallet")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if(doc.getBoolean("banked")){
                                    bankedCount += 1;
                                    bankedIDs.add(doc.get("id").toString());
                                }
                                collectedIDs.add(doc.get("id").toString());
                            }
                            parseJson();
                        } else {
                            Log.d(tag, "Error getting documents: ", task.getException());
                        }

                    }
                });
        db.collection("/user/" + email + "/Bank")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                goldBalance = doc.getDouble("balance");
                            }
                        }
                    }
                });

        db.collection("/user/" + email + "/Player")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                playerMulti = doc.getLong("multi").intValue();
                                playerRadius = doc.getLong("radius").intValue();
                                lifetimeCoins = doc.getLong("lifetimeCoins").intValue();
                                lifetimeGold = doc.getDouble("lifetimeGold");
                                lifetimeDistance = doc.getDouble("lifetimeDistance").floatValue();
                                Log.d(tag, "multi: " + playerMulti + " radius: " + playerRadius);
                            }
                        }
                    }
                });




    }

    public void checkSpareChange(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        db.collection("/user/" + email + "/SpareChange")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Double value = doc.getDouble("value");
                                String currency = doc.getString("currency");
                                spareChangeValue += value * exchangeRates.get(currency);
                                Log.d(tag, spareChangeValue + "");
                            }
                            if (spareChangeValue > 0.0){
                                spawnSCPopup();
                                Log.d(tag, "spawn sc");
                            }
                        }
                    }
                });

    }

    public void spawnSCPopup(){
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.sparechange_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        TextView spareLabel = popupView.findViewById(R.id.spareValue);
        spareLabel.setText(String.format("%.3f", spareChangeValue ) + " Gold");

        Button spareButton = popupView.findViewById(R.id.spareButton);
        spareButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                goldBalance += spareChangeValue;
                spareChangeValue = 0.0;
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                db.collection("user").document(email).collection("Bank").document(email).set(new GoldBalance(goldBalance));
                clearFBWallet("SpareChange");

                popupWindow.dismiss();

            }
        });

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }


    @Override
    public void onStart(){
        super.onStart();
        if(mapFragment.locationEngine != null){
            try {
                mapFragment.locationEngine.requestLocationUpdates();
            } catch(SecurityException ignored) {}
            mapFragment.locationEngine.addLocationEngineListener(mapFragment);
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mapFragment.locationEngine != null) {
            mapFragment.locationEngine.removeLocationEngineListener(mapFragment);
            mapFragment.locationEngine.removeLocationUpdates();
        }
    }
}
