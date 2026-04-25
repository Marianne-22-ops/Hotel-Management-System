package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Reservation {

    private final int id;

    private final StringProperty guest;
    private final StringProperty room;
    private final StringProperty checkIn;
    private final StringProperty checkOut;
    private final StringProperty status;

    // ✅ MAIN CONSTRUCTOR (for reservations table)
    public Reservation(int id, String guest, String room, String checkIn, String checkOut, String status) {
        this.id = id;
        this.guest = new SimpleStringProperty(guest);
        this.room = new SimpleStringProperty(room);
        this.checkIn = new SimpleStringProperty(checkIn);
        this.checkOut = new SimpleStringProperty(checkOut);
        this.status = new SimpleStringProperty(status);
    }

    // ✅ SECOND CONSTRUCTOR (for history table)
    public Reservation(String guest, String room, String checkIn, String checkOut, String status) {
        this.id = 0; // not used in history
        this.guest = new SimpleStringProperty(guest);
        this.room = new SimpleStringProperty(room);
        this.checkIn = new SimpleStringProperty(checkIn);
        this.checkOut = new SimpleStringProperty(checkOut);
        this.status = new SimpleStringProperty(status);
    }

    // ✅ GETTERS
    public int getId() { return id; }

    public String getGuest() { return guest.get(); }
    public String getRoom() { return room.get(); }
    public String getCheckIn() { return checkIn.get(); }
    public String getCheckOut() { return checkOut.get(); }
    public String getStatus() { return status.get(); }

    // ✅ PROPERTY METHODS (for TableView)
    public StringProperty guestProperty() { return guest; }
    public StringProperty roomProperty() { return room; }
    public StringProperty checkInProperty() { return checkIn; }
    public StringProperty checkOutProperty() { return checkOut; }
    public StringProperty statusProperty() { return status; }
}