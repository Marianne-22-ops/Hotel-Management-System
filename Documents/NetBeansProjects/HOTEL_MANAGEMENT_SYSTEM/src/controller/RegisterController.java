package controller;

import database.DBConnection;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RegisterController implements Initializable {

    @FXML
    private TextField txtFullName;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private void handleSignup(ActionEvent event) {

        try {

            Connection conn = DBConnection.connect();
            String sql = "INSERT INTO users(full_name,email,username,password) VALUES(?,?,?,?)";

            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, txtFullName.getText());
            pst.setString(2, txtEmail.getText());
            pst.setString(3, txtUsername.getText());
            pst.setString(4, txtPassword.getText());

            pst.executeUpdate();

            System.out.println("User registered successfully!");

            // Open Login page
            Parent root = FXMLLoader.load(getClass().getResource("/view/LOGIN.fxml"));

            Stage stage = (Stage) txtUsername.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException | SQLException e) {

            System.out.println("Registration failed: " + e.getMessage());

        }

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

}