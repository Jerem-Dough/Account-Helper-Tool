package nopk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoogleAPIHandler {
    private static final String API_KEY = "API";

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

    private static String parseResponse(String jsonResponse, String originalName, String originalAddress) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            // Check API status
            String status = jsonObject.optString("status", "UNKNOWN");
            if (!status.equals("OK")) {
                return "{\"status\":\"" + status + "\",\"error\":\"No match found\"}";
            }

            // Validate existence of candidates array
            if (!jsonObject.has("candidates") || jsonObject.getJSONArray("candidates").isEmpty()) {
                return "{\"status\":\"OK\",\"error\":\"No candidates found\"}";
            }

            // Extract first candidate
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            JSONObject candidate = candidates.optJSONObject(0); // Safely get the first candidate
            if (candidate == null) {
                return "{\"status\":\"OK\",\"error\":\"No candidates found\"}";
            }

            String matchedName = candidate.optString("name", originalName);
            String matchedAddress = candidate.optString("formatted_address", originalAddress);

            // Return structured JSON response
            return new JSONObject()
                    .put("status", "OK")
                    .put("name", matchedName)
                    .put("address", matchedAddress)
                    .toString();
        } catch (Exception e) {
            return "{\"status\":\"ERROR\",\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
