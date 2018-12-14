package ilp.coinz;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;


public class TransferFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public MainActivity activity;

    private int selectedIndex;
    ArrayList<Coin> spinnerItems;

    String emailTo;
    TextInputEditText emailInput;
    Spinner spinner;

    public TransferFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transfer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        spinnerItems = new ArrayList<>();

        spinner = Objects.requireNonNull(getView()).findViewById(R.id.transferSpinner);
        spinner.setOnItemSelectedListener(this);

        emailInput = getView().findViewById(R.id.transferEmail);


        Button button = getView().findViewById(R.id.transferButton);
        button.setOnClickListener(v -> {
            emailTo = Objects.requireNonNull(emailInput.getText()).toString();
            if(!emailTo.equals(activity.getPlayer().getEmail())) {

                if (!TextUtils.isEmpty(emailTo) && Patterns.EMAIL_ADDRESS.matcher(emailTo).matches()) {

                    if (activity.getPlayer().getBankedCount() >= 25 && spinnerItems.get(selectedIndex) != null) {
                        Coin tempCoin = spinnerItems.get(selectedIndex);
                        tempCoin.setTransferred(true);

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        //rewrite coin as collected for current user
                        db.collection("user").document(activity.getPlayer().getEmail()).collection("Wallet").document(tempCoin.getId()).set(tempCoin);
                        //add to receiving users spare change collection
                        db.collection("user").document(emailTo).collection("SpareChange").document(tempCoin.getId() + "-" + activity.getPlayer().getEmail()).set(tempCoin);

                        spinnerItems.remove(selectedIndex);

                        ArrayAdapter<Coin> spinnerArrayAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.spinner_item, spinnerItems);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinner.setAdapter(spinnerArrayAdapter);
                    }

                } else {
                    Snackbar.make(getView(), R.string.transfer_invalid_email, Snackbar.LENGTH_LONG).show();
                }

                } else {
                Snackbar.make(getView(), R.string.transfer_not_self, Snackbar.LENGTH_LONG).show();
            }
        });

       setSpinner();

    }

    private void setSpinner(){
        //gets all collected but unbanked coins
        for (String id : activity.getCollectedIDs()){
            if (activity.getCoinsCollection().containsKey(id)) {
                Coin tempCoin = activity.getCoinsCollection().get(id);
                assert tempCoin != null;
                if(!tempCoin.isBanked()) {
                    spinnerItems.add(tempCoin);
                }
            }
        }

        //sort coins on gold value descending
        spinnerItems.sort((a, b) ->
                a.getValue() * activity.getExchangeRates().get(a.getCurrency())
                        < b.getValue() * activity.getExchangeRates().get(b.getCurrency()) ? 1 : -1);

        ArrayAdapter<Coin> spinnerArrayAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.spinner_item, spinnerItems);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        selectedIndex = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
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
