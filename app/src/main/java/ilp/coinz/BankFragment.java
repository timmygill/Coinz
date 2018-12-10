package ilp.coinz;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public class BankFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public MainActivity activity;

    private int selectedIndex;
    private HashMap<String, String> spinnerItemToTag;
    private ArrayList<String> spinnerItems;
    private Spinner spinner;

    private TextView bankValue;
    private TextView countValue;

    public BankFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bank, container, false);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){


        spinnerItems = new ArrayList<>();
        spinnerItemToTag = new HashMap<>();

        bankValue = Objects.requireNonNull(getView()).findViewById(R.id.balanceValue);
        bankValue.setText(String.format("%.3f", activity.getPlayer().getGoldBalance()) + " Gold");
        countValue = getView().findViewById(R.id.countValue);
        countValue.setText(activity.getPlayer().getBankedCount() + "/25");

        spinner = getView().findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        Button button = getView().findViewById(R.id.bankButton);
        button.setOnClickListener(v -> {
            if (spinnerItems.size() > 0 && selectedIndex < spinnerItems.size()) {
                if (activity.getPlayer().getBankedCount() < 25 && spinnerItems.get(selectedIndex) != null) {
                    String id = spinnerItemToTag.get(spinnerItems.get(selectedIndex));
                    Objects.requireNonNull(activity.getCoinsCollection().get(id)).setBanked(true);

                    activity.getPlayer().setBankedCount(activity.getPlayer().getBankedCount() + 1);
                    countValue.setText(activity.getPlayer().getBankedCount() + "/25");

                    Coin tempCoin = activity.getCoinsCollection().get(id);
                    assert tempCoin != null;
                    Double value = activity.getPlayer().getMulti() * tempCoin.getValue() * activity.getExchangeRates().get(tempCoin.getCurrency());
                    activity.getPlayer().setGoldBalance(activity.getPlayer().getGoldBalance() + value);
                    activity.getPlayer().setLifetimeGold( activity.getPlayer().getLifetimeGold() + value);

                    bankValue.setText(activity.getPlayer().getGoldBalance()+ " Gold");


                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String email = Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
                    db.collection("user").document(email).collection("Wallet").document(Objects.requireNonNull(id)).set(Objects.requireNonNull(activity.getCoinsCollection().get(id)));
                    db.collection("user").document(email).collection("Player").document(email).set(activity.getPlayer());


                    spinnerItems.remove(selectedIndex);

                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.spinner_item, spinnerItems);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinner.setAdapter(spinnerArrayAdapter);

                } else {
                    Snackbar.make(getView(), R.string.bank_all_used, Snackbar.LENGTH_LONG).show();

                }
            } else {
                Snackbar.make(getView(), R.string.bank_no_coins, Snackbar.LENGTH_LONG).show();

            }
        });


        for (String id : activity.getCollectedIDs()){
            Log.d("bank", id);
            if (activity.getCoinsCollection().containsKey(id)) {
                Coin tempCoin = activity.getCoinsCollection().get(id);
                assert tempCoin != null;
                if(!tempCoin.isBanked()) {
                    String item = tempCoin.getCurrency() + " : " + String.format("%.3f", tempCoin.getValue());
                    spinnerItemToTag.put(item, id);
                    Log.d("bank", item);
                    spinnerItems.add(item);
                }
            }
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.spinner_item, spinnerItems);
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
}
