package controller;

import database.DBConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private AnchorPane workspacePane;

    // Summary cards
    @FXML private Label lblRooms, lblGuests, lblRevenue, lblReservations;

    // Room status cards (right sidebar)
    @FXML private Label lblAvailable, lblOccupied, lblMaintenance, lblReservedRooms;

    // Charts & activity
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private VBox todayActivityBox;

    // Header clock
    @FXML private Label lblDateTime;

    // Sidebar buttons
    @FXML private Button btnDashboard, btnRooms, btnGuests;
    @FXML private Button btnReservations, btnPayments, btnReports;

    private static final String ACTIVE_STYLE =
            "-fx-background-color:#1d4ed8; -fx-text-fill:white; -fx-font-size:13px; " +
            "-fx-background-radius:7; -fx-alignment:CENTER_LEFT; -fx-padding:9 12 9 12; -fx-cursor:hand;";

    private static final String INACTIVE_STYLE =
            "-fx-background-color:transparent; -fx-text-fill:#94a3b8; -fx-font-size:13px; " +
            "-fx-background-radius:7; -fx-alignment:CENTER_LEFT; -fx-padding:9 12 9 12; -fx-cursor:hand;";

    @FXML
    public void initialize() {
        startClock();
        loadDashboardData();
        setActive(btnDashboard);
    }

    // ================= LIVE CLOCK =================
    private void startClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd yyyy  |  hh:mm:ss a");
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            lblDateTime.setText(LocalDateTime.now().format(formatter));
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    // ================= ACTIVE BUTTON =================
    private void setActive(Button active) {
        Button[] all = {btnDashboard, btnRooms, btnGuests, btnReservations, btnPayments, btnReports};
        for (Button btn : all) btn.setStyle(INACTIVE_STYLE);
        active.setStyle(ACTIVE_STYLE);
    }

    // ================= LOAD ALL DASHBOARD DATA =================
    private void loadDashboardData() {
        try {
            Connection con = DBConnection.connect();

            // Total rooms
            ResultSet rs1 = con.createStatement().executeQuery("SELECT COUNT(*) FROM rooms");
            if (rs1.next()) lblRooms.setText(rs1.getString(1));

            // Total guests
            ResultSet rs2 = con.createStatement().executeQuery("SELECT COUNT(*) FROM guests");
            if (rs2.next()) lblGuests.setText(rs2.getString(1));

            // Total revenue
            ResultSet rs3 = con.createStatement().executeQuery("SELECT SUM(amount) FROM payments");
            if (rs3.next()) {
                double total = rs3.getDouble(1);
                lblRevenue.setText("₱" + String.format("%,.0f", total));
            }

            // Total reservations
            // Total reservations — Reserved + Checked-in only
ResultSet rs4 = con.createStatement().executeQuery(
    "SELECT COUNT(*) FROM reservations WHERE status IN ('Reserved','Checked-in')"
);
if (rs4.next()) lblReservations.setText(rs4.getString(1));

            // Room status breakdown
            ResultSet rs5 = con.createStatement().executeQuery(
                "SELECT status, COUNT(*) as cnt FROM rooms GROUP BY status"
            );
            while (rs5.next()) {
                String status = rs5.getString("status");
                String cnt = rs5.getString("cnt");
                switch (status) {
                    case "Available"   -> lblAvailable.setText(cnt);
                    case "Occupied"    -> lblOccupied.setText(cnt);
                    case "Maintenance" -> lblMaintenance.setText(cnt);
                    case "Reserved"    -> lblReservedRooms.setText(cnt);
                }
            }

            // Monthly revenue BarChart
            loadRevenueChart(con);

            // Today's check-in / check-out
            loadTodayActivity(con);

        } catch (SQLException e) {
        }
    }

    // ================= BARCHART =================
    private void loadRevenueChart(Connection con) throws SQLException {
        revenueChart.getData().clear();
        revenueChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        String sql = "SELECT DATE_FORMAT(payment_date, '%b') as month, " +
                     "MONTH(payment_date) as month_num, SUM(amount) as total " +
                     "FROM payments " +
                     "WHERE YEAR(payment_date) = YEAR(CURDATE()) " +
                     "GROUP BY month_num, month ORDER BY month_num";

        ResultSet rs = con.createStatement().executeQuery(sql);
        while (rs.next()) {
            series.getData().add(new XYChart.Data<>(
                rs.getString("month"),
                rs.getDouble("total")
            ));
        }

        revenueChart.getData().add(series);

        // Style bars blue
        revenueChart.lookupAll(".default-color0.chart-bar").forEach(node ->
            node.setStyle("-fx-bar-fill: #2563eb;")
        );
    }

    // ================= TODAY'S ACTIVITY =================
    private void loadTodayActivity(Connection con) throws SQLException {
        todayActivityBox.getChildren().clear();

        String today = LocalDate.now().toString();

        String sql = "SELECT guest, room, check_in, check_out, status FROM reservations " +
                     "WHERE check_in = '" + today + "' OR check_out = '" + today + "' " +
                     "ORDER BY check_in";

        ResultSet rs = con.createStatement().executeQuery(sql);

        boolean hasData = false;

        while (rs.next()) {
            hasData = true;
            String guest    = rs.getString("guest");
            String room     = rs.getString("room");
            String checkIn  = rs.getString("check_in");
            String checkOut = rs.getString("check_out");

            boolean isCheckIn  = checkIn.equals(today);
            boolean isCheckOut = checkOut.equals(today);

            // Row container
            HBox row = new HBox();
            row.setSpacing(10);
            row.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:7; -fx-padding:8 12;");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Badge
            Label badge = new Label(isCheckIn ? "Check-in" : "Check-out");
            badge.setStyle(isCheckIn
                ? "-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-font-size:11px; " +
                  "-fx-font-weight:bold; -fx-background-radius:20; -fx-padding:2 8;"
                : "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-font-size:11px; " +
                  "-fx-font-weight:bold; -fx-background-radius:20; -fx-padding:2 8;"
            );
            badge.setMinWidth(76);

            // Guest + room info
            Label info = new Label(guest + "  —  Room " + room);
            info.setStyle("-fx-font-size:12px; -fx-text-fill:#334155;");

            // Date label
            Label dateLabel = new Label(isCheckIn ? "In: " + checkIn : "Out: " + checkOut);
            dateLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8;");

            HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

            row.getChildren().addAll(badge, info, dateLabel);
            todayActivityBox.getChildren().add(row);
        }

        if (!hasData) {
            Label empty = new Label("No check-ins or check-outs today.");
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px; -fx-padding:8 0;");
            todayActivityBox.getChildren().add(empty);
        }
    }

    // ================= NAVIGATION =================
    @FXML private void handleDashboard() {
        setActive(btnDashboard);
        workspacePane.getChildren().clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"));
            AnchorPane newPane = (AnchorPane) root.lookup("#workspacePane");
            workspacePane.getChildren().setAll(newPane.getChildren());
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleRooms() {
        setActive(btnRooms);
        loadPage("/view/Room.fxml");
    }

    @FXML private void handleGuests() {
        setActive(btnGuests);
        loadPage("/view/Guest.fxml");
    }

    @FXML private void handleReservations() {
        setActive(btnReservations);
        loadPage("/view/reservation.fxml");
    }

    @FXML private void handlePayments() {
        setActive(btnPayments);
        loadPage("/view/payment.fxml");
    }

    @FXML private void handleReports() {
        setActive(btnReports);
        loadPage("/view/reports.fxml");
    }

    @FXML private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/LOGIN.fxml"));
            Stage stage = (Stage) workspacePane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadPage(String page) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(page));
            workspacePane.getChildren().setAll(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
        } catch (IOException e) { e.printStackTrace(); }
    }
}