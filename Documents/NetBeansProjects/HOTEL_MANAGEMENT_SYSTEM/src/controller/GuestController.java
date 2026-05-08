package controller;

import database.DBConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Guest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class GuestController {

    @FXML private TextField nameField, contactField, emailField, addressField, idNumberField, searchField;
    @FXML private ComboBox<String> idTypeCombo;

    @FXML private TableView<Guest> guestTable;
    @FXML private TableColumn<Guest, String> colName, colContact, colEmail, colAddress, colIdType, colIdNumber;
    @FXML private TableColumn<Guest, String> colStatus;

    private final ObservableList<Guest> guestList = FXCollections.observableArrayList();

    private String selectedGuestName;

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {

        idTypeCombo.getItems().addAll("Passport", "Driver License", "National ID");

        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colContact.setCellValueFactory(data -> data.getValue().contactProperty());
        colEmail.setCellValueFactory(data -> data.getValue().emailProperty());
        colAddress.setCellValueFactory(data -> data.getValue().addressProperty());
        colIdType.setCellValueFactory(data -> data.getValue().idTypeProperty());
        colIdNumber.setCellValueFactory(data -> data.getValue().idNumberProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        loadGuests();

        // TABLE CLICK
        guestTable.setOnMouseClicked(event -> {

            Guest selected = guestTable.getSelectionModel().getSelectedItem();

            if (selected != null) {
                selectedGuestName = selected.getName();

                nameField.setText(selected.getName());
                contactField.setText(selected.getContact());
                emailField.setText(selected.getEmail());
                addressField.setText(selected.getAddress());
                idTypeCombo.setValue(selected.getIdType());
                idNumberField.setText(selected.getIdNumber());
            }
        });

        // ✅ Black bold headers
        Platform.runLater(() -> {
            guestTable.lookupAll(".column-header .label").forEach(node -> {
                node.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
            });
        });
    }

    // ================= LOAD =================
    private void loadGuests() {

        guestList.clear();

        try {
            Connection con = DBConnection.connect();

            String sql = "SELECT * FROM guests";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                guestList.add(new Guest(
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("id_type"),
                        rs.getString("id_number"),
                        rs.getString("status")
                ));
            }

            guestTable.setItems(guestList);

        } catch (SQLException e) {
            showError("Error loading guests");
        }
    }

    // ================= ADD =================
    @FXML
    private void addGuest() {

        if (nameField.getText().isEmpty()) {
            showError("Name is required");
            return;
        }

        try {
            Connection con = DBConnection.connect();

            String sql = "INSERT INTO guests(name, contact, email, address, id_type, id_number, status) VALUES(?,?,?,?,?,?, 'Active')";
            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, nameField.getText());
            pst.setString(2, contactField.getText());
            pst.setString(3, emailField.getText());
            pst.setString(4, addressField.getText());
            pst.setString(5, idTypeCombo.getValue());
            pst.setString(6, idNumberField.getText());

            pst.executeUpdate();

            showSuccess("Guest added successfully");

            loadGuests();
            clearFields();

        } catch (SQLException e) {
            showError("Failed to add guest");
        }
    }

    // ================= EDIT =================
    @FXML
    private void editGuest() {

        if (selectedGuestName == null) {
            showError("Select a guest first");
            return;
        }

        try {
            Connection con = DBConnection.connect();

            String sql = "UPDATE guests SET name=?, contact=?, email=?, address=?, id_type=?, id_number=? WHERE name=?";
            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, nameField.getText());
            pst.setString(2, contactField.getText());
            pst.setString(3, emailField.getText());
            pst.setString(4, addressField.getText());
            pst.setString(5, idTypeCombo.getValue());
            pst.setString(6, idNumberField.getText());
            pst.setString(7, selectedGuestName);

            pst.executeUpdate();

            showSuccess("Guest updated successfully");

            loadGuests();
            clearFields();

        } catch (SQLException e) {
            showError("Failed to update guest");
        }
    }

    // ================= DELETE =================
    @FXML
    private void deleteGuest() {

        if (selectedGuestName == null) {
            showError("Select a guest first");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Guest");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            try {
                Connection con = DBConnection.connect();

                String sql = "DELETE FROM guests WHERE name=?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, selectedGuestName);

                pst.executeUpdate();

                showSuccess("Guest deleted successfully");

                loadGuests();
                clearFields();

            } catch (SQLException e) {
                showError("Failed to delete guest");
            }
        }
    }

    // ================= ACTIVATE =================
    @FXML
    private void activateGuest() {

        if (selectedGuestName == null) {
            showError("Select guest first");
            return;
        }

        try {
            Connection con = DBConnection.connect();

            String sql = "UPDATE guests SET status='Active' WHERE name=?";
            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, selectedGuestName);
            pst.executeUpdate();

            showSuccess("Guest Activated");

            loadGuests();

        } catch (SQLException e) {
            showError("Failed to activate guest");
        }
    }

    // ================= SEARCH =================
    @FXML
    private void searchGuest() {

        if (searchField.getText().isEmpty()) {
            loadGuests();
            return;
        }

        ObservableList<Guest> filtered = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.connect();

            String sql = "SELECT * FROM guests WHERE name LIKE ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, "%" + searchField.getText() + "%");

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                filtered.add(new Guest(
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("id_type"),
                        rs.getString("id_number"),
                        rs.getString("status")
                ));
            }

            guestTable.setItems(filtered);

        } catch (SQLException e) {
            showError("Search failed");
        }
    }

    // ================= HELPERS =================
    private void clearFields() {
        nameField.clear();
        contactField.clear();
        emailField.clear();
        addressField.clear();
        idTypeCombo.setValue(null);
        idNumberField.clear();
        selectedGuestName = null;
    }

    private void showSuccess(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).show();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).show();
    }
}