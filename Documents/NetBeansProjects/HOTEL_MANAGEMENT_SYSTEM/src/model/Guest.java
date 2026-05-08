package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Guest {

    private final StringProperty name;
    private final StringProperty contact;
    private final StringProperty email;
    private final StringProperty address;
    private final StringProperty status;

    // ================= CONSTRUCTOR =================
    public Guest(String name,
                 String contact,
                 String email,
                 String address,
                 String status) {

        this.name = new SimpleStringProperty(name);
        this.contact = new SimpleStringProperty(contact);
        this.email = new SimpleStringProperty(email);
        this.address = new SimpleStringProperty(address);
        this.status = new SimpleStringProperty(status);
    }

    // ================= PROPERTIES =================
    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty contactProperty() {
        return contact;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty addressProperty() {
        return address;
    }

    public StringProperty statusProperty() {
        return status;
    }

    // ================= GETTERS =================
    public String getName() {
        return name.get();
    }

    public String getContact() {
        return contact.get();
    }

    public String getEmail() {
        return email.get();
    }

    public String getAddress() {
        return address.get();
    }

    public String getStatus() {
        return status.get();
    }

    // ================= SETTERS =================
    public void setStatus(String value) {
        status.set(value);
    }
}