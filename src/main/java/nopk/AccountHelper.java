package nopk;

import java.util.ArrayList;
import java.util.List;

public class AccountHelper {
    public static void main(String[] args) {
        String inputFilePath = "path/to/your/input/file.xls"; // Adjust dynamically if needed
        String outputFilePath = "path/to/your/output/file_processed.xls"; // Adjust dynamically if needed

        try {
            // Initialize FileHandler
            FileHandler fileHandler = new FileHandler();

            // Read input file
            List<String[]> records = fileHandler.readExcelFile(inputFilePath);

            // Process each record
            List<String[]> processedData = new ArrayList<>();
            processedData.add(new String[]{"Name", "Address", "Google API Response"}); // Add header row

            for (String[] record : records) {
                // Ensure the record has at least 12 columns to access indices 6 (G), 10 (K), and 11 (L)
                if (record.length < 12) {
                    System.out.println("Skipped row: insufficient data");
                    continue; // Skip rows with insufficient data
                }

                String name = record[6]; // Column G: `Name`
                String address1 = record[10]; // Column K: `Address 1`
                String address2 = record[11]; // Column L: `Address 2`

                // Validate addresses
                String validAddress = null;
                if (isValidAddress(address1)) {
                    validAddress = address1; // Use Address 1 if valid
                } else if (isValidAddress(address2)) {
                    validAddress = address2; // Fallback to Address 2 if Address 1 is invalid
                    System.out.println("Fallback: using Address 2 for " + name);
                }

                if (validAddress == null) {
                    System.out.println("Skipped row: invalid addresses for " + name);
                    continue; // Skip if neither address is valid
                }

                // Query Google API
                String response;
                try {
                    response = GoogleAPIHandler.queryBusiness(name, validAddress);
                } catch (Exception e) {
                    response = "Error querying API: " + e.getMessage();
                }

                // Log the response for debugging
                System.out.println("Processed: " + name + ", " + validAddress + " -> " + response);

                // Add processed data to output
                processedData.add(new String[]{name, validAddress, response});
            }

            // Write processed data to the output file
            fileHandler.writeExcelFile(outputFilePath, processedData);
            System.out.println("Processed data exported to: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to validate the address
    private static boolean isValidAddress(String address) {
        return address != null && address.matches(".*\\d.*"); // Address contains at least one digit
    }
}
