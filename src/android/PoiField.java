package $appid;

import java.io.Serializable;

public class PoiField implements Serializable {
    private String name;
    private String bookingType;
    private int mapId;
    private double x;
    private double y;

    public PoiField(String name, String bookingType, int mapId, double x, double y) {
        this.name = name;
        this.bookingType = bookingType;
        this.mapId = mapId;
        this.x = x;
        this.y = y;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
