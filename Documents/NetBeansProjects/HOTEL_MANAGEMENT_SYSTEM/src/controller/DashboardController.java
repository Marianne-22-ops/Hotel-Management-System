package controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class DashboardController {

    @FXML
    private AnchorPane workspacePane;


    @FXML
    private void handleRooms() {
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
        loadPage("/view/payments.fxml");
    }

    @FXML
    private void handleReports() {
        loadPage("/view/reports.fxml");
    }


    private void loadPage(String page) {

        try {

            Parent root = FXMLLoader.load(getClass().getResource(page));
            workspacePane.getChildren().setAll(root);

        } catch (IOException e) {

            System.out.println("Page not found: " + page);

        }

    }

}