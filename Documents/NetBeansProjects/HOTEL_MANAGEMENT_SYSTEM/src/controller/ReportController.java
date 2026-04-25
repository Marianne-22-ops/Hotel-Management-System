package controller;

import database.DBConnection;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Report;

import java.io.FileWriter;
import java.sql.*;

public class ReportController {

    @FXML private ComboBox<String> reportTypeCombo;

    @FXML private Label totalGuestsLabel, occupiedRoomsLabel, availableRoomsLabel, totalRevenueLabel;
    @FXML private Label checkInLabel, checkOutLabel, reservationLabel;

    @FXML private TableView<Report> reportTable;
    @FXML private TableColumn<Report, String> colGuest, colRoom, colAmount, colDate;

    private final ObservableList<Report> reportList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        reportTypeCombo.getItems().addAll(
                "Daily Report",
                "Room Occupancy",
                "Revenue Report",
                "Guest Report"
        );

        colGuest.setCellValueFactory(data -> data.getValue().guestProperty());
        colRoom.setCellValueFactory(data -> data.getValue().roomProperty());
        colAmount.setCellValueFactory(data -> data.getValue().amountProperty());
        colDate.setCellValueFactory(data -> data.getValue().dateProperty());

        loadSummary();
        loadTodayActivity();
        loadTransactions();
    }

    // ================= SUMMARY =================
    private void loadSummary() {
        try {
            Connection con = DBConnection.connect();

            ResultSet rs1 = con.prepareStatement("SELECT COUNT(*) FROM guests").executeQuery();
            rs1.next();
            totalGuestsLabel.setText("Total Guests: " + rs1.getInt(1));

            ResultSet rs2 = con.prepareStatement("SELECT COUNT(*) FROM rooms WHERE status='Occupied'").executeQuery();
            rs2.next();
            occupiedRoomsLabel.setText("Occupied Rooms: " + rs2.getInt(1));

            ResultSet rs3 = con.prepareStatement("SELECT COUNT(*) FROM rooms WHERE status='Available'").executeQuery();
            rs3.next();
            availableRoomsLabel.setText("Available Rooms: " + rs3.getInt(1));

            ResultSet rs4 = con.prepareStatement("SELECT SUM(amount) FROM payments").executeQuery();
            rs4.next();
            totalRevenueLabel.setText("Total Revenue: ₱" + rs4.getDouble(1));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= TODAY =================
private void loadTodayActivity() {
    try {
        Connection con = DBConnection.connect();

        // CURRENT CHECKED-IN
        ResultSet rs1 = con.prepareStatement(
                "SELECT COUNT(*) FROM reservations WHERE status='Checked-in'"
        ).executeQuery();
        rs1.next();
        checkInLabel.setText("Check-ins: " + rs1.getInt(1));

        // CURRENT CHECKED-OUT
        ResultSet rs2 = con.prepareStatement(
                "SELECT COUNT(*) FROM reservations WHERE status='Checked-out'"
        ).executeQuery();
        rs2.next();
        checkOutLabel.setText("Check-outs: " + rs2.getInt(1));

        // NEW RESERVATIONS TODAY
        String today = java.time.LocalDate.now().toString();

        PreparedStatement pst3 = con.prepareStatement(
                "SELECT COUNT(*) FROM reservations WHERE DATE(created_at)=?"
        );
        pst3.setString(1, today);

        ResultSet rs3 = pst3.executeQuery();
        rs3.next();
        reservationLabel.setText("New Reservations: " + rs3.getInt(1));

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    // ================= TABLE =================
    private void loadTransactions() {
        reportList.clear();

        try {
            Connection con = DBConnection.connect();

            ResultSet rs = con.prepareStatement(
                    "SELECT guest, room, amount, payment_date FROM payments ORDER BY payment_date DESC LIMIT 20"
            ).executeQuery();

            while (rs.next()) {
                reportList.add(new Report(
                        rs.getString("guest"),
                        rs.getString("room"),
                        rs.getString("amount"),
                        rs.getString("payment_date")
                ));
            }

            reportTable.setItems(reportList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= GENERATE =================
    @FXML
    private void generateReport() {
        if (reportTypeCombo.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Select report type").show();
            return;
        }

        loadSummary();
        loadTodayActivity();
        loadTransactions();
    }

    // ================= EXPORT =================
    @FXML
    private void exportReport() {
        try {
            FileWriter writer = new FileWriter("report.csv");

            for (Report r : reportList) {
                writer.write(r.guestProperty().get() + "," +
                             r.roomProperty().get() + "," +
                             r.amountProperty().get() + "," +
                             r.dateProperty().get() + "\n");
            }

            writer.close();
            new Alert(Alert.AlertType.INFORMATION, "Exported!").show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= PRINT =================
    @FXML
    private void printReport() {
        System.out.println("Printing report...");
    }
}