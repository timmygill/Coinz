package ilp.coinz;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Coin {


    private String id;
    private Double value;
    private Currency currency;
    private String symbol;
    private Double longitude;
    private Double latitude;

    public Coin(JSONObject coinjson){
        try {
            JSONObject tempprops = coinjson.getJSONObject("properties");
            this.id = tempprops.getString("id");
            this.value = Double.valueOf(tempprops.getString("value"));
            this.currency = Currency.valueOf(tempprops.getString("currency"));
            this.symbol = tempprops.getString("marker-symbol");
            JSONObject tempgeometry = coinjson.getJSONObject("geometry");
            JSONArray coords = tempgeometry.getJSONArray("coordinates");
            this.longitude = Double.valueOf(coords.getString(0));
            this.latitude = Double.valueOf(coords.getString(1));
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

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }
}
