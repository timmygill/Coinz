package ilp.coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

private FirebaseAuth mAuth;

private final String preferencesFile = "MyPrefsFile";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
        //TODO:change after testing
        //if(1==0){
            startActivity(new Intent(this, LoginActivity.class));

        } else {
            startActivity(new Intent(this, MapActivity.class));
        }

    }

    @Override
    public void onStop(){
        super.onStop();
    }
}
