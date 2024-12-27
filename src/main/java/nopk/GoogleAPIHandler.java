package nopk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GoogleAPIHandler {
    private static final String API_KEY = "AIzaSyBhkIa5uQtn4CW2i0SYgDvzjpzKxEnmho8";

    public static String queryBusiness(String name, String address) throws Exception {
        String query = name + " " + address; // Combine name and address for query
        String endpoint = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
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

        return parseResponse(response.toString(), name, address);
    }

    private static String parseResponse(String jsonResponse, String name, String address) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String status = jsonObject.get("status").getAsString();

            if (!status.equals("OK")) {
                return "No match found (status: " + status + ")";
            }

            JsonObject candidate = jsonObject.getAsJsonArray("candidates").get(0).getAsJsonObject();
            String matchedName = candidate.get("name").getAsString();
            String matchedAddress = candidate.get("formatted_address").getAsString();

            return "Matched Name: " + matchedName + ", Matched Address: " + matchedAddress;
        } catch (Exception e) {
            return "Error parsing API response: " + e.getMessage();
        }
    }
}
