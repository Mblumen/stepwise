package de.hd.stepwise.pojos;

public class MapsItem {
    public String url;
    public double latitude;
    public double longitude;
    public String title;

    public MapsItem(String url, double latitude, double longitude, String title) {
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
    }
}
