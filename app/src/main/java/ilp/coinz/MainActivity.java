package ilp.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);

    }

    @Override
    public void onStop(){
        super.onStop();
    }
}
