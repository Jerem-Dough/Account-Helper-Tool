package nopk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class BusinessMatcher {

    public static List<String[]> matchBusinesses(List<String[]> inputData) {
        List<String[]> outputData = new ArrayList<>();
        outputData.add(new String[]{"Formatted Name", "Formatted Address", "Status", "Match Source"});

        Set<String> seenEntries = new HashSet<>();

        for (String[] row : inputData) {
            if (row.length < 15 || "Name".equalsIgnoreCase(row[6])) {
                continue;
            }

            String name = row[6].trim();
            String address1 = row[10].trim();
            String address2 = row[11].trim();
            String city = row[12].trim();
            String state = row[13].trim();
            String zip = row[14].trim();

            String validAddress = isValidAddress(address1) ? address1 : isValidAddress(address2) ? address2 : null;

            if (validAddress == null) {
                outputData.add(new String[]{name, "No valid address", "Skipped", "N/A"});
                continue;
            }

            boolean foundMatch = false;

            // Primary Search
            try {
                String apiResponse = GoogleAPIHandler.queryBusiness(name, validAddress, city, state, zip);
                foundMatch = processApiResponse(name, validAddress, apiResponse, "Primary Search", outputData, seenEntries);
            } catch (Exception e) {
                System.err.println("Error during Primary Search for " + name + ": " + e.getMessage());
            }

            // Fallback Search
            if (!foundMatch) {
                try {
                    String fallbackResponse = GoogleAPIHandler.fallbackQuery(name, city, state, zip);
                    foundMatch = processApiResponse(name, validAddress, fallbackResponse, "Fallback Search", outputData, seenEntries);
                } catch (Exception e) {
                    System.err.println("Error during Fallback Search for " + name + ": " + e.getMessage());
                }
            }

            // No Match Found
            if (!foundMatch) {
                outputData.add(new String[]{name, validAddress, "No match found", "N/A"});
            }
        }

        return outputData;
    }

    private static boolean processApiResponse(
            String name,
            String validAddress,
            String apiResponse,
            String source,
            List<String[]> outputData,
            Set<String> seenEntries) {
        try {
            JSONObject jsonResponse = new JSONObject(apiResponse);
            String status = jsonResponse.optString("status", "UNKNOWN");

            if (!"OK".equalsIgnoreCase(status)) {
                return false;
            }

            JSONArray candidates = jsonResponse.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject candidate = candidates.optJSONObject(0);
                String formattedName = candidate.optString("name", name);
                String formattedAddress = candidate.optString("formatted_address", validAddress);

                if (isLikelyAddress(formattedName)) {
                    System.out.println("Address-like entry detected in Formatted Name for " + name + ", retrying with fallback...");
                    return false;
                }

                String uniqueEntry = formattedName + "|" + formattedAddress;
                if (!seenEntries.contains(uniqueEntry)) {
                    seenEntries.add(uniqueEntry);
                    outputData.add(new String[]{formattedName, formattedAddress, "OK", source});
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error parsing API response: " + e.getMessage());
        }
        return false;
    }

    private static boolean isValidAddress(String address) {
        return address != null && address.matches(".*\\d.*");
    }

    private static boolean isLikelyAddress(String nameOrAddress) {
        return nameOrAddress.matches(".*\\d{1,5}.*") || nameOrAddress.matches(".*(St|Ave|Blvd|Rd|Dr|Ln|Ct|Way|Pl|Circle).*");
    }
}
