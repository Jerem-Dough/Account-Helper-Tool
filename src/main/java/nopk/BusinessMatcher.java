package nopk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONObject;

public class BusinessMatcher {

    public static List<String[]> matchBusinesses(List<String[]> inputData) {
        List<String[]> outputData = new ArrayList<>();
        outputData.add(new String[]{"Found Name", "Found Address", "Status"}); // Updated header row

        Pattern addressPattern = Pattern.compile(".*\\d+.*"); // Regex to check for numbers in the address

        for (String[] row : inputData) {
            if (row.length < 12) {
                System.out.println("Skipped row: Insufficient columns");
                continue;
            }

            String name = row[6].trim(); // Column G: `Name`
            String address1 = row[10].trim(); // Column K: `Address 1`
            String address2 = row[11].trim(); // Column L: `Address 2`

            // Explicitly skip placeholder rows
            if (name.equalsIgnoreCase("name") || address1.equalsIgnoreCase("address 1")) {
                System.out.println("Skipped row: Placeholder data");
                continue; // Skip entirely
            }

            // Validate addresses
            String validAddress = null;
            if (addressPattern.matcher(address1).matches()) {
                validAddress = address1;
            } else if (addressPattern.matcher(address2).matches()) {
                validAddress = address2;
                System.out.println("Using Address 2 for " + name);
            } else {
                System.out.println("Skipped row: Invalid address for " + name);
                continue; // Skip rows with no valid address
            }

            // Query Google API
            try {
                String apiResponse = GoogleAPIHandler.queryBusiness(name, validAddress);

                // Extract fields from the response
                String formattedName = extractField(apiResponse, "name");
                String formattedAddress = extractField(apiResponse, "formatted_address");
                String status = extractField(apiResponse, "status");

                if (formattedName == null || formattedAddress == null) {
                    System.out.println("Incomplete data from API for " + name);
                    continue; // Skip rows with incomplete data
                }

                outputData.add(new String[]{formattedName, formattedAddress, status != null ? status : "OK"});
                System.out.println("Processed: " + name + " -> " + status);
            } catch (Exception e) {
                System.err.println("Error querying Google API for " + name + ": " + e.getMessage());
                outputData.add(new String[]{name, validAddress, "Error querying API"});
            }
        }
        return outputData;
    }

    private static String extractField(String response, String fieldName) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.has("candidates")) {
                JSONObject candidate = jsonResponse.getJSONArray("candidates").optJSONObject(0);
                if (candidate != null && candidate.has(fieldName)) {
                    return candidate.getString(fieldName);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
        }
        return null;
    }
}
