package model;

import javafx.beans.property.SimpleStringProperty;

public class Payment {

    private final SimpleStringProperty guest;
    private final SimpleStringProperty room;
    private final SimpleStringProperty amount;
    private final SimpleStringProperty method;
    private final SimpleStringProperty reference;
    private final SimpleStringProperty date;

    public Payment(String g, String r, String a, String m, String ref, String d) {
        guest = new SimpleStringProperty(g);
        room = new SimpleStringProperty(r);
        amount = new SimpleStringProperty(a);
        method = new SimpleStringProperty(m);
        reference = new SimpleStringProperty(ref);
        date = new SimpleStringProperty(d);
    }

    public SimpleStringProperty guestProperty() { return guest; }
    public SimpleStringProperty roomProperty() { return room; }
    public SimpleStringProperty amountProperty() { return amount; }
    public SimpleStringProperty methodProperty() { return method; }
    public SimpleStringProperty referenceProperty() { return reference; }
    public SimpleStringProperty dateProperty() { return date; }

    public String getGuest() { return guest.get(); }
    public String getRoom() { return room.get(); }
    public String getAmount() { return amount.get(); }
    public String getMethod() { return method.get(); }
    public String getReference() { return reference.get(); }
    public String getDate() { return date.get(); }
}