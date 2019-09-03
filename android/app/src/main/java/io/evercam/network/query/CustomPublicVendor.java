//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.evercam.network.query;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.evercam.network.Constants;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomPublicVendor {
    private JSONObject jsonObject;
    private static final String URL = "https://www.macvendorlookup.com/oui.php?mac=";
    private final String KEY_COMPANY = "company";
    private static final int CODE_OK = 200;
    private static final int CODE_NO_CONTENT = 204;

    private CustomPublicVendor(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public static CustomPublicVendor getByMac(String macAddress) {
        try {
            HttpResponse<JsonNode> response = Unirest.get(URL + macAddress).asJson();
            if (response.getStatus() == 200) {
                JSONObject vendorJsonObject = ((JsonNode)response.getBody()).getArray().getJSONObject(0);
                return new CustomPublicVendor(vendorJsonObject);
            }
        } catch (UnirestException var3) {
            if (Constants.ENABLE_LOGGING) {
                var3.printStackTrace();
            }
        } catch (JSONException var4) {
            if (Constants.ENABLE_LOGGING) {
                var4.printStackTrace();
            }
        }

        return null;
    }

    public String getCompany() {
        if (this.jsonObject != null) {
            try {
                return this.jsonObject.getString("company");
            } catch (JSONException var2) {
                if (Constants.ENABLE_LOGGING) {
                    var2.printStackTrace();
                }
            }
        }

        return "";
    }
}
