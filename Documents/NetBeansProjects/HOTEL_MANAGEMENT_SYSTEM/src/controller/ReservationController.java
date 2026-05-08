package controller;

import database.DBConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReservationController {

    @FXML private ComboBox<String> guestCombo, roomCombo, statusCombo;
    @FXML private DatePicker checkInDate, checkOutDate;
    @FXML private TextField searchField;

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> colGuest, colRoom, colCheckIn, colCheckOut, colStatus;

    @FXML private TableView<Reservation> historyTable;
    @FXML private TableColumn<Reservation, String> colHistoryGuest, colAction, colDate;

    private final ObservableList<Reservation> reservationList = FXCollections.observableArrayList();
    private final ObservableList<Reservation> historyList = FXCollections.observableArrayList();

    private String selectedGuest;
    private String selectedRoom;
    private int selectedReservationId;

    @FXML
    public void initialize() {

        reservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        statusCombo.getItems().addAll("Reserved", "Checked-in", "Cancelled", "Checked-out");

        colGuest.setCellValueFactory(data -> data.getValue().guestProperty());
        colRoom.setCellValueFactory(data -> data.getValue().roomProperty());
        colCheckIn.setCellValueFactory(data -> data.getValue().checkInProperty());
        colCheckOut.setCellValueFactory(data -> data.getValue().checkOutProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        colHistoryGuest.setCellValueFactory(data -> data.getValue().guestProperty());
        colAction.setCellValueFactory(data -> data.getValue().statusProperty());
        colDate.setCellValueFactory(data -> data.getValue().checkInProperty());

        autoCheckoutExpired();
        loadGuests();
        loadAvailableRooms();
        loadReservations();
        loadHistory();

        reservationTable.setOnMouseClicked(event -> {
            Reservation selected = reservationTable.getSelectionModel().getSelectedItem();

            if (selected != null) {
                selectedReservationId = selected.getId();
                selectedGuest = selected.getGuest();
                selectedRoom = selected.getRoom();

                guestCombo.setValue(selected.getGuest());
                roomCombo.setValue(selected.getRoom());
                checkInDate.setValue(java.time.LocalDate.parse(selected.getCheckIn()));
                checkOutDate.setValue(java.time.LocalDate.parse(selected.getCheckOut()));
                statusCombo.setValue(selected.getStatus());
            }
        });

        // ✅ Black bold headers
        Platform.runLater(() -> {
            reservationTable.lookupAll(".column-header .label").forEach(node -> {
                node.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
            });
            historyTable.lookupAll(".column-header .label").forEach(node -> {
                node.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
            });
        });
    }

    // ================= AUTO CHECKOUT =================
    private void autoCheckoutExpired() {
        try {
            Connection con = DBConnection.connect();

            String selectSql = "SELECT room FROM reservations WHERE check_out < CURDATE() AND status='Checked-in'";
            ResultSet rs = con.prepareStatement(selectSql).executeQuery();

            while (rs.next()) {
                updateRoomStatus(rs.getString("room"), "Available");
            }

            String updateSql = "UPDATE reservations SET status='Checked-out' WHERE check_out < CURDATE() AND status='Checked-in'";
            con.prepareStatement(updateSql).executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= LOAD DATA =================

    private void loadGuests() {
        try {
            guestCombo.getItems().clear();
            Connection con = DBConnection.connect();

            String sql = "SELECT name FROM guests WHERE status='Active' AND name NOT IN " +
                    "(SELECT guest FROM reservations WHERE status IN ('Reserved','Checked-in'))";

            ResultSet rs = con.prepareStatement(sql).executeQuery();

            while (rs.next()) {
                guestCombo.getItems().add(rs.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAvailableRooms() {
        try {
            roomCombo.getItems().clear();
            Connection con = DBConnection.connect();

            String sql = "SELECT room_no, type FROM rooms WHERE status='Available'";
            ResultSet rs = con.prepareStatement(sql).executeQuery();

            while (rs.next()) {
                roomCombo.getItems().add(rs.getString("room_no") + " - " + rs.getString("type"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadReservations() {
        reservationList.clear();

        try {
            Connection con = DBConnection.connect();
            ResultSet rs = con.prepareStatement("SELECT * FROM reservations").executeQuery();

            while (rs.next()) {
                reservationList.add(new Reservation(
                        rs.getInt("id"),
                        rs.getString("guest"),
                        rs.getString("room"),
                        rs.getString("check_in"),
                        rs.getString("check_out"),
                        rs.getString("status")
                ));
            }

            reservationTable.setItems(reservationList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadHistory() {
        historyList.clear();

        try {
            Connection con = DBConnection.connect();

            String sql = "SELECT * FROM reservation_history ORDER BY created_at DESC";
            ResultSet rs = con.prepareStatement(sql).executeQuery();

            while (rs.next()) {
                historyList.add(new Reservation(
                        rs.getString("guest"),
                        "",
                        rs.getString("created_at"),
                        "",
                        rs.getString("action")
                ));
            }

            historyTable.setItems(historyList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= CRUD =================

    @FXML
    private void createReservation() {

        if (!validateInputs()) return;

        try {
            String room = roomCombo.getValue().split(" - ")[0];

            Connection con = DBConnection.connect();

            PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO reservations (guest, room, check_in, check_out, status) VALUES (?,?,?,?,?)"
            );

            pst.setString(1, guestCombo.getValue());
            pst.setString(2, room);
            pst.setString(3, checkInDate.getValue().toString());
            pst.setString(4, checkOutDate.getValue().toString());
            pst.setString(5, statusCombo.getValue());

            pst.executeUpdate();

            updateRoomStatus(room, "Occupied");
            logHistory(guestCombo.getValue(), "Created");

            refreshAll();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateReservation() {

        if (selectedReservationId == 0) {
            showAlert("Select reservation first!");
            return;
        }

        try {
            Connection con = DBConnection.connect();

            String room = roomCombo.getValue().split(" - ")[0];
            String newStatus = statusCombo.getValue();

            PreparedStatement pst = con.prepareStatement(
                    "UPDATE reservations SET guest=?, room=?, check_in=?, check_out=?, status=? WHERE id=?"
            );

            pst.setString(1, guestCombo.getValue());
            pst.setString(2, room);
            pst.setString(3, checkInDate.getValue().toString());
            pst.setString(4, checkOutDate.getValue().toString());
            pst.setString(5, newStatus);
            pst.setInt(6, selectedReservationId);

            pst.executeUpdate();

            if (newStatus.equals("Checked-out") || newStatus.equals("Cancelled")) {

                // ✅ Room back to Available
                updateRoomStatus(room, "Available");

                // ✅ Guest back to Active (not Inactive)
                PreparedStatement pst2 = con.prepareStatement(
                        "UPDATE guests SET status='Active' WHERE name=?"
                );
                pst2.setString(1, guestCombo.getValue());
                pst2.executeUpdate();

            } else if (newStatus.equals("Checked-in")) {
                updateRoomStatus(room, "Occupied");

            } else if (newStatus.equals("Reserved")) {
                updateRoomStatus(room, "Occupied");
            }

            logHistory(guestCombo.getValue(), "Updated (" + newStatus + ")");
            refreshAll();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelReservation() {

        if (selectedReservationId == 0) {
            showAlert("Select reservation first!");
            return;
        }

        try {
            Connection con = DBConnection.connect();

            PreparedStatement pst = con.prepareStatement(
                    "DELETE FROM reservations WHERE id=?"
            );

            pst.setInt(1, selectedReservationId);
            pst.executeUpdate();

            updateRoomStatus(selectedRoom, "Available");

            // ✅ Guest back to Active after cancel
            PreparedStatement pst2 = con.prepareStatement(
                    "UPDATE guests SET status='Active' WHERE name=?"
            );
            pst2.setString(1, selectedGuest);
            pst2.executeUpdate();

            logHistory(selectedGuest, "Cancelled");
            refreshAll();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void searchReservation() {

        if (searchField.getText().isEmpty()) {
            loadReservations();
            return;
        }

        ObservableList<Reservation> filtered = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.connect();

            PreparedStatement pst = con.prepareStatement(
                    "SELECT * FROM reservations WHERE guest LIKE ?"
            );

            pst.setString(1, "%" + searchField.getText() + "%");

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                filtered.add(new Reservation(
                        rs.getInt("id"),
                        rs.getString("guest"),
                        rs.getString("room"),
                        rs.getString("check_in"),
                        rs.getString("check_out"),
                        rs.getString("status")
                ));
            }

            reservationTable.setItems(filtered);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= HELPERS =================

    private boolean validateInputs() {
        if (guestCombo.getValue() == null ||
                roomCombo.getValue() == null ||
                checkInDate.getValue() == null ||
                checkOutDate.getValue() == null ||
                statusCombo.getValue() == null) {

            showAlert("Complete all fields!");
            return false;
        }
        return true;
    }

    private void updateRoomStatus(String room, String status) {
        try {
            Connection con = DBConnection.connect();

            PreparedStatement pst = con.prepareStatement(
                    "UPDATE rooms SET status=? WHERE room_no=?"
            );

            pst.setString(1, status);
            pst.setString(2, room);
            pst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshAll() {
        loadReservations();
        loadAvailableRooms();
        loadGuests();
        loadHistory();
        clearFields();
    }

    private void clearFields() {
        guestCombo.setValue(null);
        roomCombo.setValue(null);
        checkInDate.setValue(null);
        checkOutDate.setValue(null);
        statusCombo.setValue(null);

        selectedGuest = null;
        selectedRoom = null;
        selectedReservationId = 0;
    }

    private void logHistory(String guest, String action) {
        try {
            Connection con = DBConnection.connect();

            PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO reservation_history(guest, action) VALUES(?,?)"
            );

            pst.setString(1, guest);
            pst.setString(2, action);
            pst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}