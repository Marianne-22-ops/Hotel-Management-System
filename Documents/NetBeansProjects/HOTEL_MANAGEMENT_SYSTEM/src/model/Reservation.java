package model;

import javafx.beans.property.SimpleStringProperty;

public class Reservation {

    private final SimpleStringProperty guest;
    private final SimpleStringProperty room;
    private final SimpleStringProperty checkIn;
    private final SimpleStringProperty checkOut;
    private final SimpleStringProperty status;

    public Reservation(String guest, String room, String checkIn, String checkOut, String status) {
        this.guest = new SimpleStringProperty(guest);
        this.room = new SimpleStringProperty(room);
        this.checkIn = new SimpleStringProperty(checkIn);
        this.checkOut = new SimpleStringProperty(checkOut);
        this.status = new SimpleStringProperty(status);
    }

    public String getGuest() { return guest.get(); }
    public String getRoom() { return room.get(); }
    public String getCheckIn() { return checkIn.get(); }
    public String getCheckOut() { return checkOut.get(); }
    public String getStatus() { return status.get(); }

    public SimpleStringProperty guestProperty() { return guest; }
    public SimpleStringProperty roomProperty() { return room; }
    public SimpleStringProperty checkInProperty() { return checkIn; }
    public SimpleStringProperty checkOutProperty() { return checkOut; }
    public SimpleStringProperty statusProperty() { return status; }
}