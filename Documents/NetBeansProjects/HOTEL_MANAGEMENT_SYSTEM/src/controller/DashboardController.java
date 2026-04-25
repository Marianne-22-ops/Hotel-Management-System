package controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private AnchorPane workspacePane;


    @FXML
    private void handleDashboard() {

        // Clears loaded pages and returns to dashboard default view
        workspacePane.getChildren().clear();

    }


    @FXML
    private void handleLogout() {

        try {

            Parent root = FXMLLoader.load(getClass().getResource("/view/REGISTER.fxml"));

            Stage stage = (Stage) workspacePane.getScene().getWindow();

            stage.setScene(new Scene(root));

        } catch (IOException e) {

            System.out.println("REGISTER.fxml not found");

        }

    }


    @FXML
private void handleRooms() {

    System.out.println("Rooms button clicked");

    loadPage("/view/Room.fxml");

}

    @FXML
    private void handleGuests() {
        loadPage("/view/Guest.fxml");
    }

    @FXML
    private void handleReservations() {
        loadPage("/view/reservation.fxml");
    }

    @FXML
    private void handlePayments() {
        loadPage("/view/payment.fxml");
    }

    @FXML
    private void handleReports() {
        loadPage("/view/reports.fxml");
    }


   private void loadPage(String page) {

    try {

        System.out.println("Loading: " + page);

        Parent root = FXMLLoader.load(getClass().getResource(page));

        System.out.println("LOADED SUCCESSFULLY");

        workspacePane.getChildren().setAll(root);

        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);

    } catch (Exception e) {

        e.printStackTrace(); // 🔥 THIS IS THE FIX

    }

}

}