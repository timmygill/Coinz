package ilp.coinz;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

public class BankFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public MainActivity activity;

    private int selectedIndex;
    HashMap<String, String> spinnerItemToTag;
    ArrayList<String> spinnerItems;
    Spinner spinner;

    TextView bankValue;
    TextView countValue;

    public BankFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bank, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){

        Log.d("bank", activity.collectedIDs.toString());

        spinnerItems = new ArrayList<>();
        spinnerItemToTag = new HashMap<>();

        bankValue = (TextView) getView().findViewById(R.id.balanceValue);
        bankValue.setText(activity.goldBalance + " Gold");
        countValue = (TextView) getView().findViewById(R.id.countValue);
        countValue.setText(activity.bankedCount + "/25");

        spinner = (Spinner) getView().findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        Button button = (Button) getView().findViewById(R.id.bankButton);
        button.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                if (spinnerItems.size() > 0 && selectedIndex < spinnerItems.size()) {
                    if (activity.bankedCount < 25 && spinnerItems.get(selectedIndex) != null) {
                        String id = spinnerItemToTag.get(spinnerItems.get(selectedIndex));
                        activity.coinsCollection.get(id).setBanked(true);

                        activity.bankedCount += 1;
                        countValue.setText(activity.bankedCount + "/25");

                        Coin tempCoin = activity.coinsCollection.get(id);
                        activity.goldBalance += tempCoin.getValue() * activity.exchangeRates.get(tempCoin.getCurrency());
                        bankValue.setText(activity.goldBalance + " Gold");

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        db.collection("user").document(email).collection("Wallet").document(id).set(activity.coinsCollection.get(id));
                        db.collection("user").document(email).collection("Bank").document(email).set(new GoldBalance(activity.goldBalance));


                        spinnerItems.remove(selectedIndex);

                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, spinnerItems);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinner.setAdapter(spinnerArrayAdapter);

                    } else {
                        //TODO: tell user all deposits used
                    }
                } else {
                    //TODO: tell user no coin to bank
                }
            }
        } );







        for (String id : activity.collectedIDs){
            Log.d("bank", id);
            if (activity.coinsCollection.containsKey(id)) {
                Coin tempCoin = activity.coinsCollection.get(id);
                if(!tempCoin.isBanked()) {
                    String item = tempCoin.getCurrency() + " : " + String.format("%.3f", tempCoin.getValue());
                    spinnerItemToTag.put(item, id);
                    Log.d("bank", item);
                    spinnerItems.add(item);
                }
            }
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, spinnerItems);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(spinnerArrayAdapter);



    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        selectedIndex = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }



}
