package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Guest {

    private final StringProperty name;
    private final StringProperty contact;
    private final StringProperty email;
    private final StringProperty address;
    private final StringProperty idType;
    private final StringProperty idNumber;
    private final StringProperty status; // ⭐ NEW

    public Guest(String name, String contact, String email,
                 String address, String idType, String idNumber, String status) {

        this.name = new SimpleStringProperty(name);
        this.contact = new SimpleStringProperty(contact);
        this.email = new SimpleStringProperty(email);
        this.address = new SimpleStringProperty(address);
        this.idType = new SimpleStringProperty(idType);
        this.idNumber = new SimpleStringProperty(idNumber);
        this.status = new SimpleStringProperty(status); // ⭐ NEW
    }

    // PROPERTIES
    public StringProperty nameProperty() { return name; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty emailProperty() { return email; }
    public StringProperty addressProperty() { return address; }
    public StringProperty idTypeProperty() { return idType; }
    public StringProperty idNumberProperty() { return idNumber; }
    public StringProperty statusProperty() { return status; } // ⭐ NEW

    // GETTERS
    public String getName() { return name.get(); }
    public String getContact() { return contact.get(); }
    public String getEmail() { return email.get(); }
    public String getAddress() { return address.get(); }
    public String getIdType() { return idType.get(); }
    public String getIdNumber() { return idNumber.get(); }
    public String getStatus() { return status.get(); } // ⭐ NEW

    // SETTERS
    public void setStatus(String value) { status.set(value); } // ⭐ NEW
}