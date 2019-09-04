package io.evercam.network.query;

import org.json.JSONException;
import org.json.JSONObject;

import com.aiwatch.Logger;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class CustomPublicVendor {
    private static final Logger LOGGER = new Logger();
    private JSONObject jsonObject;
    private static final String URL = "https://www.macvendorlookup.com/oui.php?mac=";
    private final String KEY_COMPANY = "company";
    private final static int CODE_OK = 200;
    private final static int CODE_NO_CONTENT = 204;

    private CustomPublicVendor(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public static CustomPublicVendor getByMac(String macAddress) {
        try {
            HttpResponse<JsonNode> response = Unirest.get(URL + macAddress)
                    .asJson();
            if (response.getStatus() == CODE_OK) {
                JSONObject vendorJsonObject = response.getBody().getArray()
                        .getJSONObject(0);
                return new CustomPublicVendor(vendorJsonObject);
            }
        } catch (UnirestException e) {
            LOGGER.e(e, "Error getting vendor by mac");
        } catch (JSONException e) {
            LOGGER.e(e, "Error parsing vendor by mac");
        }
        return null;
    }

    public String getCompany() {
        if (jsonObject != null) {
            try {
                return jsonObject.getString(KEY_COMPANY);
            } catch (JSONException e) {
                LOGGER.e(e, "Error getting company name by mac");
            }
        }
        return "";
    }
}
