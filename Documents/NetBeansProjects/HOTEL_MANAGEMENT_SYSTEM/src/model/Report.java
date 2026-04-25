package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Report {

    private final StringProperty guest;
    private final StringProperty room;
    private final StringProperty amount;
    private final StringProperty date;

    public Report(String guest, String room, String amount, String date) {
        this.guest = new SimpleStringProperty(guest);
        this.room = new SimpleStringProperty(room);
        this.amount = new SimpleStringProperty(amount);
        this.date = new SimpleStringProperty(date);
    }

    public StringProperty guestProperty() { return guest; }
    public StringProperty roomProperty() { return room; }
    public StringProperty amountProperty() { return amount; }
    public StringProperty dateProperty() { return date; }
}