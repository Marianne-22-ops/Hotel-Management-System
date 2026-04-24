package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Room {

    private final StringProperty roomNo;
    private final StringProperty type;
    private final StringProperty price;
    private final StringProperty status;

    public Room(String roomNo, String type,
                String price, String status) {

        this.roomNo = new SimpleStringProperty(roomNo);
        this.type = new SimpleStringProperty(type);
        this.price = new SimpleStringProperty(price);
        this.status = new SimpleStringProperty(status);

    }

    public String getRoomNo() {
        return roomNo.get();
    }

    public void setRoomNo(String value) {
        roomNo.set(value);
    }

    public StringProperty roomNoProperty() {
        return roomNo;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String value) {
        type.set(value);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public String getPrice() {
        return price.get();
    }

    public void setPrice(String value) {
        price.set(value);
    }

    public StringProperty priceProperty() {
        return price;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String value) {
        status.set(value);
    }

    public StringProperty statusProperty() {
        return status;
    }
}