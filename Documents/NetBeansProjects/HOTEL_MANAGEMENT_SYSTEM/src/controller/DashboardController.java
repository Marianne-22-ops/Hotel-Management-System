package controller;

import database.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

public class DashboardController {

    @FXML
    private AnchorPane workspacePane;

    // dashboard components (INSIDE dashboard.fxml)
    @FXML private Label lblRooms, lblGuests, lblRevenue;
    @FXML private PieChart roomChart;

    @FXML
    public void initialize() {
        loadDashboardData(); // auto load on login
    }

    // ================= DASHBOARD =================
    private void loadDashboardData() {

        try {
            Connection con = DBConnection.connect();

            // TOTAL ROOMS
            ResultSet rs1 = con.createStatement().executeQuery("SELECT COUNT(*) FROM rooms");
            if (rs1.next()) lblRooms.setText(rs1.getString(1));

            // TOTAL GUESTS
            ResultSet rs2 = con.createStatement().executeQuery("SELECT COUNT(*) FROM guests");
            if (rs2.next()) lblGuests.setText(rs2.getString(1));

            // TOTAL REVENUE
            ResultSet rs3 = con.createStatement().executeQuery("SELECT SUM(amount) FROM payments");
            if (rs3.next()) {
                double total = rs3.getDouble(1);
                lblRevenue.setText("₱" + total);
            }

            // ROOM STATUS CHART (PRO LOOK)
            ResultSet rs4 = con.createStatement().executeQuery(
                    "SELECT status, COUNT(*) as total FROM rooms GROUP BY status"
            );

            roomChart.getData().clear();

            while (rs4.next()) {
                roomChart.getData().add(
                        new PieChart.Data(
                                rs4.getString("status"),
                                rs4.getInt("total")
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= NAVIGATION =================

    @FXML
    private void handleDashboard() {
        workspacePane.getChildren().clear();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"));

            AnchorPane newPane = (AnchorPane) root.lookup("#workspacePane");
            workspacePane.getChildren().setAll(newPane.getChildren());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        loadPage("/view/payment.fxml");
    }

    @FXML
    private void handleReports() {
        loadPage("/view/reports.fxml");
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/REGISTER.fxml"));
            Stage stage = (Stage) workspacePane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPage(String page) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(page));

            workspacePane.getChildren().setAll(root);

            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}