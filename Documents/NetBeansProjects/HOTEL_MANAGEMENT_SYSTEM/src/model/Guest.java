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

    public Guest(String name, String contact, String email,
                 String address, String idType, String idNumber) {

        this.name = new SimpleStringProperty(name);
        this.contact = new SimpleStringProperty(contact);
        this.email = new SimpleStringProperty(email);
        this.address = new SimpleStringProperty(address);
        this.idType = new SimpleStringProperty(idType);
        this.idNumber = new SimpleStringProperty(idNumber);
    }

    // PROPERTIES
    public StringProperty nameProperty() { return name; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty emailProperty() { return email; }
    public StringProperty addressProperty() { return address; }
    public StringProperty idTypeProperty() { return idType; }
    public StringProperty idNumberProperty() { return idNumber; }

    // GETTERS
    public String getName() { return name.get(); }
    public String getContact() { return contact.get(); }
    public String getEmail() { return email.get(); }
    public String getAddress() { return address.get(); }
    public String getIdType() { return idType.get(); }
    public String getIdNumber() { return idNumber.get(); }

    // SETTERS
    public void setName(String value) { name.set(value); }
    public void setContact(String value) { contact.set(value); }
    public void setEmail(String value) { email.set(value); }
    public void setAddress(String value) { address.set(value); }
    public void setIdType(String value) { idType.set(value); }
    public void setIdNumber(String value) { idNumber.set(value); }
}