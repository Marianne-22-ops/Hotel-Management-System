package controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;

import model.Room;
import database.DBConnection;
import java.sql.SQLException;

public class RoomController implements Initializable {

    @FXML private TextField roomNoField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField searchField;

    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, String> colRoomNo;
    @FXML private TableColumn<Room, String> colType;
    @FXML private TableColumn<Room, String> colPrice;
    @FXML private TableColumn<Room, String> colStatus;

    private final ObservableList<Room> roomList = FXCollections.observableArrayList();

    private String selectedRoomNo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        typeCombo.getItems().addAll("Single", "Double", "Suite", "Deluxe");

        statusCombo.getItems().addAll("Available", "Maintenance", "Reserved");

        colRoomNo.setCellValueFactory(data -> data.getValue().roomNoProperty());
        colType.setCellValueFactory(data -> data.getValue().typeProperty());
        colPrice.setCellValueFactory(data -> data.getValue().priceProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        loadRooms();

        roomTable.setOnMouseClicked(event -> {

            Room selected = roomTable.getSelectionModel().getSelectedItem();

            if (selected != null) {

                selectedRoomNo = selected.getRoomNo();

                roomNoField.setText(selected.getRoomNo());
                typeCombo.setValue(selected.getType());
                priceField.setText(selected.getPrice());
                statusCombo.setValue(selected.getStatus());
            }
        });

        // ✅ Black bold headers — runLater para hintayin muna na ma-render ang table
        Platform.runLater(() -> {
            roomTable.lookupAll(".column-header .label").forEach(node -> {
                node.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
            });
        });
    }

    private void loadRooms() {

        roomList.clear();

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM rooms";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                roomList.add(new Room(
                        rs.getString("room_no"),
                        rs.getString("type"),
                        rs.getString("price"),
                        rs.getString("status")
                ));
            }

            roomTable.setItems(roomList);


        } catch (SQLException e) {
            showError("Error loading rooms");
        }
    }

    @FXML
    private void addRoom() {

        if (roomNoField.getText().isEmpty()) {
            showError("Room number is required");
            return;
        }

        try {
            Connection conn = DBConnection.connect();

            String sql = "INSERT INTO rooms VALUES (?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, roomNoField.getText());
            pst.setString(2, typeCombo.getValue());
            pst.setString(3, priceField.getText());
            pst.setString(4, statusCombo.getValue());

            pst.executeUpdate();

            showSuccess("Room added successfully");

            loadRooms();
            clearFields();

        } catch (SQLException e) {
            showError("Failed to add room");
        }
    }

    @FXML
    private void updateRoom() {

        if (selectedRoomNo == null) {
            showError("Select a room first");
            return;
        }

        try {
            Connection conn = DBConnection.connect();

            String sql = "UPDATE rooms SET type=?, price=?, status=? WHERE room_no=?";
            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, typeCombo.getValue());
            pst.setString(2, priceField.getText());
            pst.setString(3, statusCombo.getValue());
            pst.setString(4, selectedRoomNo);

            int rows = pst.executeUpdate();

            if (rows > 0) {
                showSuccess("Room updated successfully");
            } else {
                showError("Update failed");
            }

            loadRooms();
            clearFields();

        } catch (SQLException e) {
            showError("Failed to update room");
        }
    }

    @FXML
    private void deleteRoom() {

        if (selectedRoomNo == null) {
            showError("Select a room first");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Room");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            try {
                Connection conn = DBConnection.connect();

                String sql = "DELETE FROM rooms WHERE room_no=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, selectedRoomNo);

                pst.executeUpdate();

                showSuccess("Room deleted successfully");

                loadRooms();
                clearFields();

            } catch (SQLException e) {
                showError("Failed to delete room");
            }
        }
    }

    @FXML
    private void changeStatus() {

        if (selectedRoomNo == null) {
            showError("Select a room first");
            return;
        }

        try {
            Connection conn = DBConnection.connect();

            String sql = "UPDATE rooms SET status=? WHERE room_no=?";
            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, statusCombo.getValue());
            pst.setString(2, selectedRoomNo);

            pst.executeUpdate();

            showSuccess("Status updated");

            loadRooms();

        } catch (SQLException e) {
            showError("Failed to update status");
        }
    }

    @FXML
    private void searchRoom() {

        if (searchField.getText().isEmpty()) {
            loadRooms();
            return;
        }

        ObservableList<Room> filteredList = FXCollections.observableArrayList();

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM rooms WHERE room_no LIKE ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + searchField.getText() + "%");

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                filteredList.add(new Room(
                        rs.getString("room_no"),
                        rs.getString("type"),
                        rs.getString("price"),
                        rs.getString("status")
                ));
            }

            roomTable.setItems(filteredList);

        } catch (SQLException e) {
            showError("Search failed");
        }
    }

    private void clearFields() {
        roomNoField.clear();
        typeCombo.setValue(null);
        priceField.clear();
        statusCombo.setValue(null);
        selectedRoomNo = null;
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}