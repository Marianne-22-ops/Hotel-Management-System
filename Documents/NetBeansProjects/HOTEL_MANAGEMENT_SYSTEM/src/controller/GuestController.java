package controller;

import database.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Guest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class GuestController {

    @FXML private TextField nameField, contactField, emailField, addressField, idNumberField, searchField;
    @FXML private ComboBox<String> idTypeCombo;

    @FXML private TableView<Guest> guestTable;
    @FXML private TableColumn<Guest, String> colName, colContact, colEmail, colAddress, colIdType, colIdNumber;

    private final ObservableList<Guest> guestList = FXCollections.observableArrayList();

    private String selectedGuestName;

    @FXML
    public void initialize() {

        idTypeCombo.getItems().addAll("Passport", "Driver License", "National ID");

        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colContact.setCellValueFactory(data -> data.getValue().contactProperty());
        colEmail.setCellValueFactory(data -> data.getValue().emailProperty());
        colAddress.setCellValueFactory(data -> data.getValue().addressProperty());
        colIdType.setCellValueFactory(data -> data.getValue().idTypeProperty());
        colIdNumber.setCellValueFactory(data -> data.getValue().idNumberProperty());

        loadGuests();

        // Table click
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
    }

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
                        rs.getString("id_number")
                ));
            }

            guestTable.setItems(guestList);

        } catch (Exception e) {
            showError("Error loading guests");
            e.printStackTrace();
        }
    }

    @FXML
    private void addGuest() {

        if (nameField.getText().isEmpty()) {
            showError("Name is required");
            return;
        }

        try {
            Connection con = DBConnection.connect();

            String sql = "INSERT INTO guests(name, contact, email, address, id_type, id_number) VALUES(?,?,?,?,?,?)";
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

        } catch (Exception e) {
            showError("Failed to add guest");
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteGuest() {

        if (selectedGuestName == null) {
            showError("Select a guest first");
            return;
        }

        // Confirmation dialog
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

            } catch (Exception e) {
                showError("Failed to delete guest");
                e.printStackTrace();
            }
        }
    }

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

            int rowsUpdated = pst.executeUpdate();

            if (rowsUpdated > 0) {
                showSuccess("Guest updated successfully");
            } else {
                showError("Update failed");
            }

            loadGuests();
            clearFields();

        } catch (Exception e) {
            showError("Failed to update guest");
            e.printStackTrace();
        }
    }

    @FXML
    private void searchGuest() {

        if (searchField.getText().isEmpty()) {
            loadGuests(); // auto refresh
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
                        rs.getString("id_number")
                ));
            }

            guestTable.setItems(filtered);

        } catch (Exception e) {
            showError("Search failed");
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nameField.clear();
        contactField.clear();
        emailField.clear();
        addressField.clear();
        idTypeCombo.setValue(null);
        idNumberField.clear();
        selectedGuestName = null;
    }

    @FXML
    private void viewHistory() {
        System.out.println("View Guest History clicked");
    }

    // ✅ Helper Alerts
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }
}