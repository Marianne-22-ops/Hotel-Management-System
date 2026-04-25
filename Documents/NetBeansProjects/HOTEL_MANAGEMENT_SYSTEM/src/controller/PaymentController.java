package controller;

import database.DBConnection;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Payment;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PaymentController {

    @FXML private ComboBox<String> guestCombo, methodCombo;
    @FXML private TextField roomField, amountField, referenceField, searchField;

    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, String> colGuest, colRoom, colAmount, colMethod, colReference, colDate;

    private final ObservableList<Payment> paymentList = FXCollections.observableArrayList();

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {

        methodCombo.getItems().addAll(
                "Cash", "GCash", "Credit Card", "Bank Transfer"
        );

        colGuest.setCellValueFactory(data -> data.getValue().guestProperty());
        colRoom.setCellValueFactory(data -> data.getValue().roomProperty());
        colAmount.setCellValueFactory(data -> data.getValue().amountProperty());
        colMethod.setCellValueFactory(data -> data.getValue().methodProperty());
        colReference.setCellValueFactory(data -> data.getValue().referenceProperty());
        colDate.setCellValueFactory(data -> data.getValue().dateProperty());

        loadGuests();
        loadPayments();

        // 🔥 AUTO FILL WHEN GUEST IS SELECTED
        guestCombo.setOnAction(e -> {
            if (guestCombo.getValue() != null) {
                autoFillDetails(guestCombo.getValue());
            }
        });
    }

    // ================= LOAD =================

    private void loadGuests() {
        try {
            guestCombo.getItems().clear();

            Connection con = DBConnection.connect();

            // ONLY unpaid reservations
            String sql = "SELECT DISTINCT guest FROM reservations WHERE status IS NULL OR status != 'Paid'";

            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                guestCombo.getItems().add(rs.getString("guest"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPayments() {
        paymentList.clear();

        try {
            Connection con = DBConnection.connect();

            String sql = "SELECT * FROM payments ORDER BY payment_date DESC";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                paymentList.add(new Payment(
                        rs.getString("guest"),
                        rs.getString("room"),
                        rs.getString("amount"),
                        rs.getString("method"),
                        rs.getString("reference_no"),
                        rs.getString("payment_date")
                ));
            }

            paymentTable.setItems(paymentList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= AUTO COMPUTE =================

    private void autoFillDetails(String guestName) {
        try {
            Connection con = DBConnection.connect();

            String sql = "SELECT r.room, r.check_in, r.check_out, rm.price " +
                         "FROM reservations r " +
                         "JOIN rooms rm ON r.room = rm.room_no " +
                         "WHERE r.guest = ? AND (r.status IS NULL OR r.status != 'Paid') " +
                         "ORDER BY r.check_in DESC LIMIT 1";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, guestName);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                String room = rs.getString("room");
                LocalDate checkIn = rs.getDate("check_in").toLocalDate();
                LocalDate checkOut = rs.getDate("check_out").toLocalDate();
                double price = rs.getDouble("price");

                // 👉 SET ROOM
                roomField.setText(room);

                // 👉 COMPUTE DAYS
                long days = ChronoUnit.DAYS.between(checkIn, checkOut);
                if (days <= 0) days = 1;

                // 👉 COMPUTE TOTAL
                double total = days * price;

                // 👉 DISPLAY
                amountField.setText(String.format("%.2f", total));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= VALIDATION =================

    private boolean isAlreadyPaid(String guest, String room) {
        try {
            Connection con = DBConnection.connect();

            String sql = "SELECT COUNT(*) FROM payments WHERE guest=? AND room=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, guest);
            pst.setString(2, room);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= CRUD =================

    @FXML
    private void recordPayment() {

        if (guestCombo.getValue() == null ||
            roomField.getText().isEmpty() ||
            amountField.getText().isEmpty() ||
            methodCombo.getValue() == null) {

            showAlert("Complete all fields!");
            return;
        }

        // 🔥 PREVENT DOUBLE PAYMENT
        if (isAlreadyPaid(guestCombo.getValue(), roomField.getText())) {
            showAlert("This reservation is already PAID!");
            return;
        }

        try {
            Connection con = DBConnection.connect();

            // INSERT PAYMENT
            String sql = "INSERT INTO payments (guest, room, amount, method, reference_no) VALUES (?,?,?,?,?)";
            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, guestCombo.getValue());
            pst.setString(2, roomField.getText());
            pst.setDouble(3, Double.parseDouble(amountField.getText()));
            pst.setString(4, methodCombo.getValue());
            pst.setString(5, referenceField.getText());

            pst.executeUpdate();

            // 🔥 UPDATE RESERVATION STATUS → PAID
            PreparedStatement update = con.prepareStatement(
                    "UPDATE reservations SET status='Paid' WHERE guest=? AND room=?"
            );
            update.setString(1, guestCombo.getValue());
            update.setString(2, roomField.getText());
            update.executeUpdate();

            showAlert("Payment Recorded Successfully!");

            loadPayments();
            loadGuests(); // refresh dropdown
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void searchPayment() {

        if (searchField.getText().isEmpty()) {
            loadPayments();
            return;
        }

        ObservableList<Payment> filtered = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.connect();

            String sql = "SELECT * FROM payments WHERE guest LIKE ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, "%" + searchField.getText() + "%");

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                filtered.add(new Payment(
                        rs.getString("guest"),
                        rs.getString("room"),
                        rs.getString("amount"),
                        rs.getString("method"),
                        rs.getString("reference_no"),
                        rs.getString("payment_date")
                ));
            }

            paymentTable.setItems(filtered);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= HELPERS =================

    private void clearFields() {
        guestCombo.setValue(null);
        roomField.clear();
        amountField.clear();
        methodCombo.setValue(null);
        referenceField.clear();
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}