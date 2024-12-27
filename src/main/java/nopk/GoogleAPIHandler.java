package nopk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;

public class GoogleAPIHandler {
    private static final String API_KEY = "API";

    public static String queryBusiness(String name, String address, String city, String state, String zip) throws Exception {
        // Combine name, address, city, state, and ZIP for a more specific query
        String query = name + " " + address + " " + city + " " + state + " " + zip;
        String endpoint = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";
        String encodedQuery = URLEncoder.encode(query.trim(), "UTF-8");
        String url = endpoint + "?input=" + encodedQuery + "&inputtype=textquery&fields=name,formatted_address&key=" + API_KEY;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to query API: HTTP error code " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }
}
