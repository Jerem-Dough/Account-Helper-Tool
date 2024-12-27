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
            processedData.add(new String[]{"Formatted Name", "Formatted Address", "Status"}); // Add header row

            for (String[] record : records) {
                // Ensure the record has at least 15 columns for all required fields
                if (record.length < 15) {
                    System.out.println("Skipped row: Insufficient data");
                    continue; // Skip rows with insufficient data
                }

                String name = record[6]; // Column G: `Name`
                String address1 = record[10]; // Column K: `Address 1`
                String address2 = record[11]; // Column L: `Address 2`
                String city = record[12]; // Column M: `City`
                String state = record[13]; // Column N: `State`
                String zip = record[14]; // Column O: `Zip Code`

                // Validate addresses
                String validAddress = isValidAddress(address1) ? address1 : isValidAddress(address2) ? address2 : null;

                if (validAddress == null) {
                    System.out.println("Skipped row: Invalid addresses for " + name);
                    processedData.add(new String[]{name, "No valid address", "Skipped"});
                    continue; // Skip if neither address is valid
                }

                // Query Google API
                String response;
                try {
                    response = GoogleAPIHandler.queryBusiness(name, validAddress, city, state, zip);
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
