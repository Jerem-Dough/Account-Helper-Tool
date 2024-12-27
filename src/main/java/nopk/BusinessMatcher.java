package nopk;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class BusinessMatcher {

    public static List<String[]> matchBusinesses(List<String[]> inputData) {
        List<String[]> outputData = new ArrayList<>();

        for (String[] row : inputData) {
            // Skip header rows or invalid rows
            if (row.length < 12 || "Name".equalsIgnoreCase(row[6])) {
                continue;
            }

            String name = row[6]; // Column G: `Name`
            String address1 = row[10]; // Column K: `Address 1`
            String address2 = row[11]; // Column L: `Address 2`

            // Validate addresses
            String validAddress = isValidAddress(address1) ? address1 : isValidAddress(address2) ? address2 : null;

            if (validAddress == null) {
                System.out.println("Skipped row: Invalid address for " + name);
                outputData.add(new String[]{name, "No valid address", "Skipped"});
                continue;
            }

            // Query Google API
            try {
                String apiResponse = GoogleAPIHandler.queryBusiness(name, validAddress);
                JSONObject jsonResponse = new JSONObject(apiResponse);

                String status = jsonResponse.optString("status", "UNKNOWN");
                if (!"OK".equalsIgnoreCase(status)) {
                    outputData.add(new String[]{name, validAddress, "No match found"});
                    continue;
                }

                // Extract the first candidate
                JSONArray candidates = jsonResponse.optJSONArray("candidates");
                JSONObject candidate = (candidates != null && candidates.length() > 0) ? candidates.optJSONObject(0) : null;

                if (candidate != null) {
                    String formattedName = candidate.optString("name", name);
                    String formattedAddress = candidate.optString("formatted_address", validAddress);
                    outputData.add(new String[]{formattedName, formattedAddress, "OK"});
                } else {
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
