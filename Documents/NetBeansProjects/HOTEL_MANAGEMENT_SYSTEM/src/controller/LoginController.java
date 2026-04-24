package controller;

import database.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.Scene;

import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;


    // LOGIN BUTTON FUNCTION
    @FXML
    private void handleLogin(ActionEvent event) {

        String username = txtUsername.getText();
        String password = txtPassword.getText();

        try {

            Connection conn = DBConnection.connect();

            if (conn == null) {

                System.out.println("Database connection failed");
                return;

            }

            String sql = "SELECT * FROM users WHERE username=? AND password=?";

            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                System.out.println("Login successful!");

                Parent root = FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"));

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                stage.setScene(new Scene(root));
                stage.show();

            } else {

                System.out.println("Invalid username or password");

            }

        } catch (IOException | SQLException e) {

            System.out.println("Login error: " + e.getMessage());

        }

    }


    // REGISTER PAGE LINK FUNCTION
    @FXML
    private void openRegister(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(getClass().getResource("/view/REGISTER.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {

            System.out.println("Error loading Register page: " + e.getMessage());

        }

    }


    // EXIT BUTTON FUNCTION (optional if you add exit button later)
    @FXML
    private void handleExit() {

        System.exit(0);

    }

}