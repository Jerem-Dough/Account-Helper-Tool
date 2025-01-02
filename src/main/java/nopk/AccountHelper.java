package nopk;

import java.util.ArrayList;
import java.util.List;

public class AccountHelper {
    public static void main(String[] args) {
        String inputFilePath = "path/to/your/input/file.xls";
        String outputFilePath = "path/to/your/output/file_processed.xls";

        try {
            FileHandler fileHandler = new FileHandler();

            List<String[]> records = fileHandler.readExcelFile(inputFilePath);

            List<String[]> processedData = new ArrayList<>();
            processedData.add(new String[]{"Formatted Name", "Formatted Address", "Status"});

            for (String[] record : records) {

                if (record.length < 15) {
                    System.out.println("Skipped row: Insufficient data");
                    continue;
                }

                String name = record[6];
                String address1 = record[10];
                String address2 = record[11];
                String city = record[12];
                String state = record[13];
                String zip = record[14];

                String validAddress = isValidAddress(address1) ? address1 : isValidAddress(address2) ? address2 : null;

                if (validAddress == null) {
                    System.out.println("Skipped row: Invalid addresses for " + name);
                    processedData.add(new String[]{name, "No valid address", "Skipped"});
                    continue;
                }

                String response;
                try {
                    response = GoogleAPIHandler.queryBusiness(name, validAddress, city, state, zip);
                } catch (Exception e) {
                    response = "Error querying API: " + e.getMessage();
                }

                System.out.println("Processed: " + name + ", " + validAddress + " -> " + response);

                processedData.add(new String[]{name, validAddress, response});
            }

            fileHandler.writeExcelFile(outputFilePath, processedData);
            System.out.println("Processed data exported to: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean isValidAddress(String address) {
        return address != null && address.matches(".*\\d.*"); // Address contains at least one digit
    }
}
