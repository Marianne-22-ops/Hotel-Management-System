package controller;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReservationController {

    @FXML private ComboBox<String> guestCombo, roomCombo, statusCombo;
    @FXML private DatePicker checkInDate, checkOutDate;
    @FXML private TextField searchField;

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> colGuest, colRoom, colCheckIn, colCheckOut, colStatus;

    // 📜 HISTORY TABLE
    @FXML private TableView<Reservation> historyTable;
    @FXML private TableColumn<Reservation, String> colHistoryGuest, colAction, colDate;

    private final ObservableList<Reservation> reservationList = FXCollections.observableArrayList();
    private final ObservableList<Reservation> historyList = FXCollections.observableArrayList();

    private String selectedGuest;
    private String selectedRoom;

    @FXML
    public void initialize() {
        
         reservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
         historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        statusCombo.getItems().addAll(
                "Reserved",
                "Checked-in",
                "Checked-out",
                "Cancelled"
        );

        // MAIN TABLE
        colGuest.setCellValueFactory(data -> data.getValue().guestProperty());
        colRoom.setCellValueFactory(data -> data.getValue().roomProperty());
        colCheckIn.setCellValueFactory(data -> data.getValue().checkInProperty());
        colCheckOut.setCellValueFactory(data -> data.getValue().checkOutProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        // HISTORY TABLE
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
                selectedGuest = selected.getGuest();
                selectedRoom = selected.getRoom();

                guestCombo.setValue(selected.getGuest());
                roomCombo.setValue(selected.getRoom());
                checkInDate.setValue(java.time.LocalDate.parse(selected.getCheckIn()));
                checkOutDate.setValue(java.time.LocalDate.parse(selected.getCheckOut()));
                statusCombo.setValue(selected.getStatus());
            }
        });
    }

    // ================= AUTO CHECKOUT =================
    private void autoCheckoutExpired() {
        try {
            Connection con = DBConnection.connect();

            String sql = "UPDATE reservations SET status='Checked-out' " +
                    "WHERE check_out < CURDATE() AND status='Checked-in'";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOAD DATA =================

    private void loadGuests() {
        try {
            guestCombo.getItems().clear();
            Connection con = DBConnection.connect();

            String sql = "SELECT name FROM guests WHERE name NOT IN " +
                    "(SELECT guest FROM reservations WHERE status='Checked-in')";

            ResultSet rs = con.prepareStatement(sql).executeQuery();

            while (rs.next()) {
                guestCombo.getItems().add(rs.getString("name"));
            }

        } catch (Exception e) {
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
                roomCombo.getItems().add(
                        rs.getString("room_no") + " - " + rs.getString("type")
                );
            }

        } catch (Exception e) {
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
                        rs.getString("guest"),
                        rs.getString("room"),
                        rs.getString("check_in"),
                        rs.getString("check_out"),
                        rs.getString("status")
                ));
            }

            reservationTable.setItems(reservationList);

        } catch (Exception e) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOGIC =================

    private boolean isRoomAvailable(String room, String in, String out) {
        try {
            Connection con = DBConnection.connect();

            String sql =
                    "SELECT * FROM reservations WHERE room=? " +
                            "AND status IN ('Reserved','Checked-in') " +
                            "AND (check_in <= ? AND check_out >= ?)";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, room);
            pst.setString(2, out);
            pst.setString(3, in);

            return !pst.executeQuery().next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CRUD =================

    @FXML
    private void createReservation() {

        if (!validateInputs()) return;

        try {
            String room = roomCombo.getValue().split(" - ")[0];

            if (!isRoomAvailable(room,
                    checkInDate.getValue().toString(),
                    checkOutDate.getValue().toString())) {

                showAlert("Room already booked!");
                return;
            }

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ NEW UPDATE FUNCTION
    @FXML
    private void updateReservation() {

        if (selectedGuest == null || selectedRoom == null) {
            showAlert("Select reservation first!");
            return;
        }

        try {
            Connection con = DBConnection.connect();

            String room = roomCombo.getValue().split(" - ")[0];

            PreparedStatement pst = con.prepareStatement(
                    "UPDATE reservations SET guest=?, room=?, check_in=?, check_out=?, status=? WHERE guest=? AND room=?"
            );

            pst.setString(1, guestCombo.getValue());
            pst.setString(2, room);
            pst.setString(3, checkInDate.getValue().toString());
            pst.setString(4, checkOutDate.getValue().toString());
            pst.setString(5, statusCombo.getValue());

            pst.setString(6, selectedGuest);
            pst.setString(7, selectedRoom);

            pst.executeUpdate();

            logHistory(guestCombo.getValue(), "Updated");
            refreshAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelReservation() {

        if (selectedGuest == null || selectedRoom == null) {
            showAlert("Select reservation first!");
            return;
        }

        try {
            Connection con = DBConnection.connect();

            PreparedStatement pst = con.prepareStatement(
                    "DELETE FROM reservations WHERE guest=? AND room=?"
            );

            pst.setString(1, selectedGuest);
            pst.setString(2, selectedRoom);
            pst.executeUpdate();

            updateRoomStatus(selectedRoom, "Available");
            logHistory(selectedGuest, "Cancelled");

            refreshAll();

        } catch (Exception e) {
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
                        rs.getString("guest"),
                        rs.getString("room"),
                        rs.getString("check_in"),
                        rs.getString("check_out"),
                        rs.getString("status")
                ));
            }

            reservationTable.setItems(filtered);

        } catch (Exception e) {
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

        } catch (Exception e) {
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
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}