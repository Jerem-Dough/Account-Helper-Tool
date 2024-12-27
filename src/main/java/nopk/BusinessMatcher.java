package nopk;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class BusinessMatcher {

    public static List<String[]> matchBusinesses(List<String[]> inputData) {
        List<String[]> outputData = new ArrayList<>();
        outputData.add(new String[]{"Formatted Name", "Formatted Address", "Status"}); // Header row

        for (String[] row : inputData) {
            // Skip invalid rows
            if (row.length < 15 || "Name".equalsIgnoreCase(row[6])) {
                continue;
            }

            String name = row[6].trim(); // Column G: `Name`
            String address1 = row[10].trim(); // Column K: `Address 1`
            String address2 = row[11].trim(); // Column L: `Address 2`
            String city = row[12].trim(); // Column M: `City`
            String state = row[13].trim(); // Column N: `State`
            String zip = row[14].trim(); // Column O: `Zip Code`

            // Validate addresses
            String validAddress = isValidAddress(address1) ? address1 : isValidAddress(address2) ? address2 : null;

            if (validAddress == null) {
                System.out.println("Skipped row: Invalid address for " + name);
                outputData.add(new String[]{name, "No valid address", "Skipped"});
                continue;
            }

            // Query Google API
            try {
                System.out.println("Querying Google API: Name: " + name + ", Address: " + validAddress + ", City: " + city + ", State: " + state + ", Zip: " + zip);
                String apiResponse = GoogleAPIHandler.queryBusiness(name, validAddress, city, state, zip);

                // Parse the API response
                JSONObject jsonResponse = new JSONObject(apiResponse);
                String status = jsonResponse.optString("status", "UNKNOWN");

                if (!"OK".equalsIgnoreCase(status)) {
                    System.out.println("No match found for " + name + " at " + validAddress);
                    outputData.add(new String[]{name, validAddress, "No match found"});
                    continue;
                }

                // Extract the first candidate
                JSONArray candidates = jsonResponse.optJSONArray("candidates");
                if (candidates != null && candidates.length() > 0) {
                    JSONObject candidate = candidates.optJSONObject(0);
                    String formattedName = candidate.optString("name", name);
                    String formattedAddress = candidate.optString("formatted_address", validAddress);
                    outputData.add(new String[]{formattedName, formattedAddress, "OK"});
                } else {
                    System.out.println("No candidates found for " + name + " at " + validAddress);
                    outputData.add(new String[]{name, validAddress, "No candidates found"});
                }
            } catch (Exception e) {
                System.err.println("Error querying Google API for " + name + ": " + e.getMessage());
                outputData.add(new String[]{name, validAddress, "Error querying API"});
            }
        }
        return outputData;
    }

    private static boolean isValidAddress(String address) {
        return address != null && address.matches(".*\\d.*"); // Address contains at least one digit
    }
}
