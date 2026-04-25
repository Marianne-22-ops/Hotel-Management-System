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

    private int selectedReservationId; // ⭐ IMPORTANT

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {

        methodCombo.getItems().addAll("Cash", "GCash", "Credit Card", "Bank Transfer");

        colGuest.setCellValueFactory(data -> data.getValue().guestProperty());
        colRoom.setCellValueFactory(data -> data.getValue().roomProperty());
        colAmount.setCellValueFactory(data -> data.getValue().amountProperty());
        colMethod.setCellValueFactory(data -> data.getValue().methodProperty());
        colReference.setCellValueFactory(data -> data.getValue().referenceProperty());
        colDate.setCellValueFactory(data -> data.getValue().dateProperty());

        loadGuests();
        loadPayments();

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

            // ✅ ONLY reservations that are NOT yet paid
            String sql = "SELECT DISTINCT r.guest FROM reservations r " +
             "LEFT JOIN payments p ON r.id = p.reservation_id " +
             "WHERE p.reservation_id IS NULL " +
             "AND r.status IN ('Reserved','Checked-in')";

            ResultSet rs = con.prepareStatement(sql).executeQuery();

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
            ResultSet rs = con.prepareStatement(sql).executeQuery();

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

        String sql = "SELECT r.id, r.room, r.check_in, r.check_out, rm.price " +
                "FROM reservations r " +
                "JOIN rooms rm ON r.room = rm.room_no " +
                "LEFT JOIN payments p ON r.id = p.reservation_id " +
                "WHERE r.guest = ? " +
                "AND p.reservation_id IS NULL " +
                "AND r.status IN ('Reserved','Checked-in') " +
                "ORDER BY r.check_in DESC LIMIT 1";

        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, guestName);

        ResultSet rs = pst.executeQuery();

        if (rs.next()) {

            selectedReservationId = rs.getInt("id");

            String room = rs.getString("room");
            LocalDate checkIn = rs.getDate("check_in").toLocalDate();
            LocalDate checkOut = rs.getDate("check_out").toLocalDate();
            double price = rs.getDouble("price");

            // ✅ AUTO SET ROOM
            roomField.setText(room);

            // ✅ COMPUTE DAYS
            long days = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (days <= 0) days = 1;

            // ✅ COMPUTE TOTAL
            double total = days * price;

            // ✅ DISPLAY AMOUNT
            amountField.setText(String.format("%.2f", total));

        } else {
            // 🔥 DEBUG (important)
            roomField.clear();
            amountField.clear();
            selectedReservationId = 0;
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // ================= VALIDATION =================

    private boolean isAlreadyPaid(int reservationId) {
        try {
            Connection con = DBConnection.connect();

            PreparedStatement pst = con.prepareStatement(
                    "SELECT 1 FROM payments WHERE reservation_id=?"
            );

            pst.setInt(1, reservationId);

            return pst.executeQuery().next();

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

        if (isAlreadyPaid(selectedReservationId)) {
            showAlert("This reservation is already PAID!");
            return;
        }

        try {
            Connection con = DBConnection.connect();

            String sql = "INSERT INTO payments (reservation_id, guest, room, amount, method, reference_no) VALUES (?,?,?,?,?,?)";
            PreparedStatement pst = con.prepareStatement(sql);

            pst.setInt(1, selectedReservationId);
            pst.setString(2, guestCombo.getValue());
            pst.setString(3, roomField.getText());
            pst.setDouble(4, Double.parseDouble(amountField.getText()));
            pst.setString(5, methodCombo.getValue());
            pst.setString(6, referenceField.getText());

            pst.executeUpdate();

            showAlert("Payment Recorded Successfully!");

            loadPayments();
            loadGuests();
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
        selectedReservationId = 0;
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}