package controller;

import database.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

public class DashboardController {

    @FXML private AnchorPane workspacePane;
    @FXML private Label lblRooms, lblGuests, lblRevenue;
    @FXML private PieChart roomChart;

    // ================= SIDEBAR BUTTONS =================
    @FXML private Button btnDashboard;
    @FXML private Button btnRooms;
    @FXML private Button btnGuests;
    @FXML private Button btnReservations;
    @FXML private Button btnPayments;
    @FXML private Button btnReports;

    private static final String ACTIVE_STYLE =
            "-fx-background-color:#1d4ed8; -fx-text-fill:white; -fx-font-size:13px; " +
            "-fx-background-radius:7; -fx-alignment:CENTER_LEFT; -fx-padding:9 12 9 12; -fx-cursor:hand;";

    private static final String INACTIVE_STYLE =
            "-fx-background-color:transparent; -fx-text-fill:#94a3b8; -fx-font-size:13px; " +
            "-fx-background-radius:7; -fx-alignment:CENTER_LEFT; -fx-padding:9 12 9 12; -fx-cursor:hand;";

    @FXML
    public void initialize() {
        loadDashboardData();
        setActive(btnDashboard);
    }

    // ================= ACTIVE BUTTON =================
    private void setActive(Button active) {
        Button[] all = {btnDashboard, btnRooms, btnGuests, btnReservations, btnPayments, btnReports};
        for (Button btn : all) {
            btn.setStyle(INACTIVE_STYLE);
        }
        active.setStyle(ACTIVE_STYLE);
    }

    // ================= DASHBOARD =================
    private void loadDashboardData() {
        try {
            Connection con = DBConnection.connect();

            ResultSet rs1 = con.createStatement().executeQuery("SELECT COUNT(*) FROM rooms");
            if (rs1.next()) lblRooms.setText(rs1.getString(1));

            ResultSet rs2 = con.createStatement().executeQuery("SELECT COUNT(*) FROM guests");
            if (rs2.next()) lblGuests.setText(rs2.getString(1));

            ResultSet rs3 = con.createStatement().executeQuery("SELECT SUM(amount) FROM payments");
            if (rs3.next()) {
                double total = rs3.getDouble(1);
                lblRevenue.setText("₱" + total);
            }

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
        setActive(btnDashboard);
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
        setActive(btnRooms);
        loadPage("/view/Room.fxml");
    }

    @FXML
    private void handleGuests() {
        setActive(btnGuests);
        loadPage("/view/Guest.fxml");
    }

    @FXML
    private void handleReservations() {
        setActive(btnReservations);
        loadPage("/view/reservation.fxml");
    }

    @FXML
    private void handlePayments() {
        setActive(btnPayments);
        loadPage("/view/payment.fxml");
    }

    @FXML
    private void handleReports() {
        setActive(btnReports);
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