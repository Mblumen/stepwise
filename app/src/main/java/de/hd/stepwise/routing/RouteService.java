package de.hd.stepwise.routing;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.dtos.geodata.GeoDataJson;
import de.hd.stepwise.pojos.UserProgressWithTrackAndMilestones;

@Singleton
public class RouteService {

    @Inject
    public RouteService() {

    }
    public List<GeoPoint> getGeoData(UserProgressWithTrackAndMilestones userProgressWithTrackAndMilestones) throws JSONException, IOException {
        //return RouteFetcher.fetchRoute(userProgressWithTrackAndMilestones.trackWithMilestones.track.trackRoute);
        if(userProgressWithTrackAndMilestones.trackWithMilestones.track.trackRoute.localGeoDataPath == null || userProgressWithTrackAndMilestones.trackWithMilestones.track.trackRoute.localGeoDataPath.isEmpty()) {
            return Collections.emptyList();
        }
        return convertToGeoPoints(parseGeoData(userProgressWithTrackAndMilestones.trackWithMilestones.track.trackRoute.localGeoDataPath));
    }


    public GeoPoint getPosition(List<GeoPoint> geoPoints, float distanceWalked) {
        return PositionInterpolator.interpolatePosition(geoPoints, distanceWalked);
    }

    private GeoDataJson parseGeoData(String filePath) throws IOException {
        String geoJson = new String(Files.readAllBytes(java.nio.file.Paths.get(filePath)));
        return new Gson().fromJson(geoJson, new TypeToken<GeoDataJson>(){}.getType());
    }

    private List<GeoPoint> convertToGeoPoints(GeoDataJson geoDataJson) {
        List<GeoPoint> route = new ArrayList<>();
        geoDataJson.features.stream().filter(feature -> feature.type.equals("Feature")).findFirst().ifPresent(feature -> feature.geometry.coordinates.forEach(coordinates -> {
            route.add(new GeoPoint(coordinates.get(1), coordinates.get(0)));
        }));
        return route;
    }
}