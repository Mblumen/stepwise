package de.hd.fitbittracks.routing;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;

@Singleton
public class RouteService {

    @Inject
    public RouteService() {

    }
    public List<GeoPoint> getGeoData(UserProgressWithTrackAndMilestones userProgressWithTrackAndMilestones) throws JSONException, IOException {
        return RouteFetcher.fetchRoute(userProgressWithTrackAndMilestones.trackWithMilestones.track.trackRoute);
    }

    public GeoPoint getPosition(List<GeoPoint> geoPoints, float distanceWalked) {
        return PositionInterpolator.interpolatePosition(geoPoints, distanceWalked);
    }
}