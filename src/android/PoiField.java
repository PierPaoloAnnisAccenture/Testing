package $appid;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class PoiField implements Serializable {
    private String id;
    private String name;
    private String building;
    private int floor;
    private String bookingType;
    private int mapId;
    private double x;
    private double y;
    private boolean bookedByMe;

    public PoiField() {
    }

    public PoiField(String id, String name, String building, int floor, String bookingType, int mapId, double x, double y, boolean bookedByMe) {
        this.id = id;
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.bookingType = bookingType;
        this.mapId = mapId;
        this.x = x;
        this.y = y;
        this.bookedByMe = bookedByMe;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBuilding() {
        return building;
    }

    public int getFloor() {
        return floor;
    }

    public String getBookingType() {
        return bookingType;
    }

    public int getMapId() {
        return mapId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isBookedByMe() {
        return bookedByMe;
    }

    public PoiField fromJson(JSONObject o) throws JSONException {
        id = o.getString("id");
        name = o.getString("name");
        building = o.getString("building");
        floor = o.getInt("floor");
        bookingType = o.getString("bookingType");
        mapId = o.getInt("mapId");
        x = o.getDouble("x");
        y = o.getDouble("y");
        bookedByMe = o.getBoolean("bookedByMe");

        return new PoiField(id, name, building, floor, bookingType, mapId, x, y, bookedByMe);
    }
}
