package nopk;

import java.io.File;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AccountHelperUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Account Helper Tool");

        Label instructionLabel = new Label("Choose an Excel file to process:");
        Button browseButton = new Button("Browse...");
        Button processButton = new Button("Process File");
        Label statusLabel = new Label("");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"));

        final File[] selectedFile = {null};
        browseButton.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                selectedFile[0] = file;
                statusLabel.setText("Selected File: " + file.getAbsolutePath());
            }
        });

        processButton.setOnAction(e -> {
            if (selectedFile[0] == null) {
                showAlert(AlertType.ERROR, "No File Selected", "Please select an Excel file before proceeding.");
                return;
            }

            String inputFilePath = selectedFile[0].getAbsolutePath();
            String outputFilePath = inputFilePath.replace(".xls", "_processed.xls");

            try {
                FileHandler fileHandler = new FileHandler();
                BusinessMatcher businessMatcher = new BusinessMatcher();

                // Read input file
                List<String[]> records = fileHandler.readExcelFile(inputFilePath);

                // Process each record with improved logic
                List<String[]> processedData = businessMatcher.matchBusinesses(records);

                // Write to output file
                fileHandler.writeExcelFile(outputFilePath, processedData);

                showAlert(AlertType.INFORMATION, "Processing Complete", 
                          "File processed successfully!\nOutput saved at: " + outputFilePath);

            } catch (Exception ex) {
                showAlert(AlertType.ERROR, "Error", 
                          "An error occurred while processing the file:\n" + ex.getMessage());
                ex.printStackTrace();
            }
        });

        VBox layout = new VBox(10, instructionLabel, browseButton, statusLabel, processButton);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
