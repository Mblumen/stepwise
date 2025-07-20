package de.hd.fitbittracks.routing;


import org.maplibre.android.geometry.LatLng;
import org.osmdroid.util.GeoPoint;

import java.util.List;

public class PositionInterpolator {

    public static GeoPoint interpolatePosition(List<GeoPoint> path, double distanceTraveled) {
        double accumulatedDistance = 0;
        GeoPoint start = path.get(0);

        for (int i = 1; i < path.size(); i++) {
            GeoPoint end = path.get(i);
            double segmentDistance = DistanceUtils.calculateDistance(
                    start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());

            if (accumulatedDistance + segmentDistance >= distanceTraveled) {
                double remainingDistance = distanceTraveled - accumulatedDistance;
                double ratio = remainingDistance / segmentDistance;

                double lat = start.getLatitude() + ratio * (end.getLatitude() - start.getLatitude());
                double lon = start.getLongitude() + ratio * (end.getLongitude() - start.getLongitude());

                return new GeoPoint(lat, lon);
            }

            accumulatedDistance += segmentDistance;
            start = end;
        }

        return path.get(path.size() - 1); // Return the last point if distance exceeds path length
    }
}