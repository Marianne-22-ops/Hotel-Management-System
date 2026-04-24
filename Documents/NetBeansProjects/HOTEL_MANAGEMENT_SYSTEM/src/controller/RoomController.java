package controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;

import model.Room;
import database.DBConnection;

public class RoomController implements Initializable {

    @FXML
    private TextField roomNoField;

    @FXML
    private ComboBox<String> typeCombo;

    @FXML
    private TextField priceField;

    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Room> roomTable;

    @FXML
    private TableColumn<Room, String> colRoomNo;

    @FXML
    private TableColumn<Room, String> colType;

    @FXML
    private TableColumn<Room, String> colPrice;

    @FXML
    private TableColumn<Room, String> colStatus;


    private ObservableList<Room> roomList =
            FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        typeCombo.getItems().addAll(
                "Single",
                "Double",
                "Suite",
                "Deluxe"
        );

        statusCombo.getItems().addAll(
                "Available",
                "Occupied",
                "Maintenance",
                "Reserved"
        );

        colRoomNo.setCellValueFactory(data ->
                data.getValue().roomNoProperty());

        colType.setCellValueFactory(data ->
                data.getValue().typeProperty());

        colPrice.setCellValueFactory(data ->
                data.getValue().priceProperty());

        colStatus.setCellValueFactory(data ->
                data.getValue().statusProperty());

        loadRooms();

        roomTable.setOnMouseClicked(event -> {

            Room selected = roomTable
                    .getSelectionModel()
                    .getSelectedItem();

            if (selected != null) {

                roomNoField.setText(selected.getRoomNo());
                typeCombo.setValue(selected.getType());
                priceField.setText(selected.getPrice());
                statusCombo.setValue(selected.getStatus());

            }

        });

    }


    private void loadRooms() {

        roomList.clear();

        try {

            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM rooms";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

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

        } catch (Exception e) {

            e.printStackTrace();

        }

    }


    @FXML
    private void addRoom() {

        try {

            Connection conn = DBConnection.connect();

            String sql =
                    "INSERT INTO rooms VALUES (?, ?, ?, ?)";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setString(1,
                    roomNoField.getText());

            pst.setString(2,
                    typeCombo.getValue());

            pst.setString(3,
                    priceField.getText());

            pst.setString(4,
                    statusCombo.getValue());

            pst.executeUpdate();

            loadRooms();

            clearFields();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }


    @FXML
    private void updateRoom() {

        try {

            Connection conn = DBConnection.connect();

            String sql =
                    "UPDATE rooms SET type=?, price=?, status=? WHERE room_no=?";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setString(1,
                    typeCombo.getValue());

            pst.setString(2,
                    priceField.getText());

            pst.setString(3,
                    statusCombo.getValue());

            pst.setString(4,
                    roomNoField.getText());

            pst.executeUpdate();

            loadRooms();

            clearFields();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }


    @FXML
    private void deleteRoom() {

        try {

            Connection conn = DBConnection.connect();

            String sql =
                    "DELETE FROM rooms WHERE room_no=?";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setString(1,
                    roomNoField.getText());

            pst.executeUpdate();

            loadRooms();

            clearFields();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }


    @FXML
    private void changeStatus() {

        try {

            Connection conn = DBConnection.connect();

            String sql =
                    "UPDATE rooms SET status=? WHERE room_no=?";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setString(1,
                    statusCombo.getValue());

            pst.setString(2,
                    roomNoField.getText());

            pst.executeUpdate();

            loadRooms();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }


    @FXML
    private void searchRoom() {

        ObservableList<Room> filteredList =
                FXCollections.observableArrayList();

        try {

            Connection conn = DBConnection.connect();

            String sql =
                    "SELECT * FROM rooms WHERE room_no LIKE ?";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setString(1,
                    "%" + searchField.getText() + "%");

            ResultSet rs =
                    pst.executeQuery();

            while (rs.next()) {

                filteredList.add(new Room(

                        rs.getString("room_no"),
                        rs.getString("type"),
                        rs.getString("price"),
                        rs.getString("status")

                ));

            }

            roomTable.setItems(filteredList);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }


    private void clearFields() {

        roomNoField.clear();

        typeCombo.setValue(null);

        priceField.clear();

        statusCombo.setValue(null);

    }

}