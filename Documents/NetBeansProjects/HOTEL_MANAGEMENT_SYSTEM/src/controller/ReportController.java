package controller;

import database.DBConnection;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Report;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
                "Monthly Report"
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
            totalGuestsLabel.setText(String.valueOf(rs1.getInt(1)));

            ResultSet rs2 = con.prepareStatement("SELECT COUNT(*) FROM rooms WHERE status='Occupied'").executeQuery();
            rs2.next();
            occupiedRoomsLabel.setText(String.valueOf(rs2.getInt(1)));

            ResultSet rs3 = con.prepareStatement("SELECT COUNT(*) FROM rooms WHERE status='Available'").executeQuery();
            rs3.next();
            availableRoomsLabel.setText(String.valueOf(rs3.getInt(1)));

            ResultSet rs4 = con.prepareStatement("SELECT SUM(amount) FROM payments").executeQuery();
            rs4.next();
            totalRevenueLabel.setText("₱" + rs4.getDouble(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= TODAY =================
    private void loadTodayActivity() {
        try {
            Connection con = DBConnection.connect();

            ResultSet rs1 = con.prepareStatement(
                    "SELECT COUNT(*) FROM reservations WHERE status='Checked-in'"
            ).executeQuery();
            rs1.next();
            checkInLabel.setText(String.valueOf(rs1.getInt(1)));

            ResultSet rs2 = con.prepareStatement(
                    "SELECT COUNT(*) FROM reservations WHERE status='Checked-out'"
            ).executeQuery();
            rs2.next();
            checkOutLabel.setText(String.valueOf(rs2.getInt(1)));

            String today = LocalDate.now().toString();
            PreparedStatement pst3 = con.prepareStatement(
                    "SELECT COUNT(*) FROM reservations WHERE DATE(created_at)=?"
            );
            pst3.setString(1, today);
            ResultSet rs3 = pst3.executeQuery();
            rs3.next();
            reservationLabel.setText(String.valueOf(rs3.getInt(1)));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= TRANSACTIONS =================
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= DAILY REPORT =================
    private void loadDailyReport() {
        reportList.clear();
        try {
            Connection con = DBConnection.connect();
            String today = LocalDate.now().toString();

            PreparedStatement pst = con.prepareStatement(
                    "SELECT guest, room, amount, payment_date FROM payments " +
                    "WHERE DATE(payment_date) = ? ORDER BY payment_date DESC"
            );
            pst.setString(1, today);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                reportList.add(new Report(
                        rs.getString("guest"),
                        rs.getString("room"),
                        rs.getString("amount"),
                        rs.getString("payment_date")
                ));
            }
            reportTable.setItems(reportList);

            PreparedStatement rev = con.prepareStatement(
                    "SELECT SUM(amount) FROM payments WHERE DATE(payment_date) = ?"
            );
            rev.setString(1, today);
            ResultSet revRs = rev.executeQuery();
            revRs.next();
            totalRevenueLabel.setText("₱" + revRs.getDouble(1));

            PreparedStatement ci = con.prepareStatement(
                    "SELECT COUNT(*) FROM reservations WHERE DATE(check_in) = ?"
            );
            ci.setString(1, today);
            ResultSet ciRs = ci.executeQuery();
            ciRs.next();
            checkInLabel.setText(String.valueOf(ciRs.getInt(1)));

            PreparedStatement co = con.prepareStatement(
                    "SELECT COUNT(*) FROM reservations WHERE DATE(check_out) = ?"
            );
            co.setString(1, today);
            ResultSet coRs = co.executeQuery();
            coRs.next();
            checkOutLabel.setText(String.valueOf(coRs.getInt(1)));

            new Alert(Alert.AlertType.INFORMATION, "Daily Report: " + today).show();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= MONTHLY REPORT =================
    private void loadMonthlyReport() {
        reportList.clear();
        try {
            Connection con = DBConnection.connect();
            String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

            PreparedStatement pst = con.prepareStatement(
                    "SELECT guest, room, amount, payment_date FROM payments " +
                    "WHERE DATE_FORMAT(payment_date, '%Y-%m') = ? ORDER BY payment_date DESC"
            );
            pst.setString(1, month);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                reportList.add(new Report(
                        rs.getString("guest"),
                        rs.getString("room"),
                        rs.getString("amount"),
                        rs.getString("payment_date")
                ));
            }
            reportTable.setItems(reportList);

            PreparedStatement rev = con.prepareStatement(
                    "SELECT SUM(amount) FROM payments WHERE DATE_FORMAT(payment_date, '%Y-%m') = ?"
            );
            rev.setString(1, month);
            ResultSet revRs = rev.executeQuery();
            revRs.next();
            totalRevenueLabel.setText("₱" + revRs.getDouble(1));

            PreparedStatement ci = con.prepareStatement(
                    "SELECT COUNT(*) FROM reservations WHERE DATE_FORMAT(check_in, '%Y-%m') = ?"
            );
            ci.setString(1, month);
            ResultSet ciRs = ci.executeQuery();
            ciRs.next();
            checkInLabel.setText(String.valueOf(ciRs.getInt(1)));

            PreparedStatement co = con.prepareStatement(
                    "SELECT COUNT(*) FROM reservations WHERE DATE_FORMAT(check_out, '%Y-%m') = ?"
            );
            co.setString(1, month);
            ResultSet coRs = co.executeQuery();
            coRs.next();
            checkOutLabel.setText(String.valueOf(coRs.getInt(1)));

            new Alert(Alert.AlertType.INFORMATION, "Monthly Report: " + month).show();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= ROOM OCCUPANCY =================
    private void loadRoomOccupancy() {
        reportList.clear();
        try {
            Connection con = DBConnection.connect();

            ResultSet rs = con.prepareStatement(
                    "SELECT r.guest, r.room, rm.type, r.status FROM reservations r " +
                    "JOIN rooms rm ON r.room = rm.room_no " +
                    "WHERE r.status = 'Checked-in' ORDER BY r.room"
            ).executeQuery();

            while (rs.next()) {
                reportList.add(new Report(
                        rs.getString("guest"),
                        rs.getString("room"),
                        rs.getString("type"),
                        rs.getString("status")
                ));
            }
            reportTable.setItems(reportList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= REVENUE REPORT =================
    private void loadRevenueReport() {
        reportList.clear();
        try {
            Connection con = DBConnection.connect();

            ResultSet rs = con.prepareStatement(
                    "SELECT guest, room, amount, payment_date FROM payments ORDER BY payment_date DESC"
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= GUEST REPORT =================
    private void loadGuestReport() {
        reportList.clear();
        try {
            Connection con = DBConnection.connect();

            ResultSet rs = con.prepareStatement(
                    "SELECT g.name, r.room, r.status, r.check_in FROM guests g " +
                    "LEFT JOIN reservations r ON g.name = r.guest ORDER BY g.name"
            ).executeQuery();

            while (rs.next()) {
                reportList.add(new Report(
                        rs.getString("name"),
                        rs.getString("room") != null ? rs.getString("room") : "N/A",
                        rs.getString("status") != null ? rs.getString("status") : "N/A",
                        rs.getString("check_in") != null ? rs.getString("check_in") : "N/A"
                ));
            }
            reportTable.setItems(reportList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= GENERATE =================
    @FXML
    private void generateReport() {
        String selected = reportTypeCombo.getValue();

        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a report type first!").show();
            return;
        }

        switch (selected) {
            case "Daily Report"   -> loadDailyReport();
            case "Monthly Report" -> loadMonthlyReport();
            case "Room Occupancy" -> loadRoomOccupancy();
            case "Revenue Report" -> loadRevenueReport();
            case "Guest Report"   -> loadGuestReport();
        }

        loadSummary();
    }

    // ================= EXPORT =================
    @FXML
    private void exportReport() {
        if (reportList.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No data to export! Generate a report first.").show();
            return;
        }
        try {
            String filename = (reportTypeCombo.getValue() != null
                    ? reportTypeCombo.getValue().replace(" ", "_")
                    : "report") + "_" + LocalDate.now() + ".csv";

            try (FileWriter writer = new FileWriter(filename)) {
                writer.write("Guest,Room,Amount/Type,Date/Status\n");
                for (Report r : reportList) {
                    writer.write(r.guestProperty().get() + "," +
                            r.roomProperty().get() + "," +
                            r.amountProperty().get() + "," +
                            r.dateProperty().get() + "\n");
                }
            }
            new Alert(Alert.AlertType.INFORMATION, "Exported to: " + filename).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ================= PRINT =================
    @FXML
    private void printReport() {
        if (reportList.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No data to print! Generate a report first.").show();
            return;
        }
        new Alert(Alert.AlertType.INFORMATION,
                "Printing: " + reportTypeCombo.getValue() +
                "\nTotal records: " + reportList.size()).show();
    }
}