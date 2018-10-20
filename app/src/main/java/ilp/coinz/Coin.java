package ilp.coinz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Coin {


    private String id;
    private Double value;
    private Currency currency;
    private String symbol;
    private String longitude;
    private String latitude;

    public Coin(JSONObject coinjson){
        try {
            JSONObject tempprops = coinjson.getJSONObject("properties");
            this.id = tempprops.getString("id");
            this.value = Double.valueOf(tempprops.getString("value"));
            this.currency = Currency.valueOf(tempprops.getString("currency"));
            this.symbol = tempprops.getString("marker-symbol");
            JSONObject tempgeometry = coinjson.getJSONObject("geometry");
            JSONArray coords = coinjson.getJSONArray("coordinates");
            this.longitude = coords.getString(0);
            this.latitude = coords.getString(1);
        } catch (JSONException e){
            return;
        }
    }

    public String getId() {
        return id;
    }

    public Double getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }
}