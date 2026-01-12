package de.hd.stepwise.pojos;

import java.util.Objects;

public class TrackRoute {
    public String geoDataUrl;
    public String localGeoDataPath; // optional, if downloaded
    public double startLat; // optional, for location-based milestones
    public double startLon; // optional, for location-based milestones
    public double endLat; // optional, for location-based milestones
    public double endLon; // optional, for location-based milestones

    public TrackRoute(String geoDataUrl, String localGeoDataPath, double startLat, double startLon, double endLat, double endLon) {
        this.geoDataUrl = geoDataUrl;
        this.localGeoDataPath = localGeoDataPath;
        this.startLat = startLat;
        this.startLon = startLon;
        this.endLat = endLat;
        this.endLon = endLon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrackRoute that)) return false;

        if (Double.compare(that.startLat, startLat) != 0) return false;
        if (Double.compare(that.startLon, startLon) != 0) return false;
        if (Double.compare(that.endLat, endLat) != 0) return false;
        if (Double.compare(that.endLon, endLon) != 0) return false;
        if (!Objects.equals(localGeoDataPath, that.localGeoDataPath)) return false;
        return Objects.equals(geoDataUrl, that.geoDataUrl);
    }
}