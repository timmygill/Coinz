package ilp.coinz;


import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.annotation.Nullable;


public class TransferFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public MainActivity activity;

    private int selectedIndex;
    HashMap<String, String> spinnerItemToTag;
    ArrayList<String> spinnerItems;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transfer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        spinnerItems = new ArrayList<>();
        spinnerItemToTag = new HashMap<>();

        spinner = getView().findViewById(R.id.transferSpinner);
        spinner.setOnItemSelectedListener(this);

        emailInput = getView().findViewById(R.id.transferEmail);


        Button button = getView().findViewById(R.id.transferButton);
        button.setOnClickListener(v -> {
            emailTo = emailInput.getText().toString();
            if(!emailTo.equals(activity.getPlayer().getEmail())) {

                if (!TextUtils.isEmpty(emailTo) && Patterns.EMAIL_ADDRESS.matcher(emailTo).matches()) {

                    if (activity.getPlayer().getBankedCount() >= 25 && spinnerItems.get(selectedIndex) != null) {
                        String id = spinnerItemToTag.get(spinnerItems.get(selectedIndex));
                        activity.getCoinsCollection().get(id).setTransferred(true);

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        //rewrite coin as collected for current user
                        db.collection("user").document(email).collection("Wallet").document(id).set(activity.getCoinsCollection().get(id));
                        //add to receiving users spare change collection
                        db.collection("user").document(emailTo).collection("SpareChange").document(id).set(activity.getCoinsCollection().get(id));

                        spinnerItems.remove(selectedIndex);

                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, spinnerItems);
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

        for (String id : activity.getCollectedIDs()) {
            if (activity.getCoinsCollection().containsKey(id)) {
                Coin tempCoin = activity.getCoinsCollection().get(id);
                if(!tempCoin.isBanked() && !tempCoin.isTransferred()) {
                    String item = tempCoin.getCurrency() + " : " + String.format("%.3f", tempCoin.getValue());
                    spinnerItemToTag.put(item, id);
                    spinnerItems.add(item);
                }

            }
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, spinnerItems);
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
