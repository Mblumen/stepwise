package de.hd.fitbittracks.routing;
import de.hd.fitbittracks.pojos.TrackRoute;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maplibre.android.geometry.LatLng;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RouteFetcher {

    private static final String API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImE5N2U4OTRjMWE0NTRkMWFiZDE3YWJhZTBjYzgzOWE0IiwiaCI6Im11cm11cjY0In0=";
    private static final OkHttpClient client = new OkHttpClient();

    public static List<GeoPoint> fetchRoute(TrackRoute trackRoute) throws IOException, JSONException {
        String url = "https://api.openrouteservice.org/v2/directions/foot-walking/geojson";
        double startLat = trackRoute.startLat;
        double startLon = trackRoute.startLon;
        double endLat = trackRoute.endLat;
        double endLon = trackRoute.endLon;

        JSONObject postData = new JSONObject();
        JSONArray coordinates = new JSONArray();
        coordinates.put(new JSONArray().put(startLon).put(startLat));
        coordinates.put(new JSONArray().put(endLon).put(endLat));
        postData.put("coordinates", coordinates);

        RequestBody body = RequestBody.create(postData.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String geoJson = response.body().string();
            return decodeGeoJsonCoords(geoJson);
        }
    }

    private static List<GeoPoint> decodeGeoJsonCoords(String geoJson) {
        List<GeoPoint> route = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(geoJson);
            JSONArray coordinates = jsonResponse.getJSONArray("features")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates");

            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray coord = coordinates.getJSONArray(i);
                route.add(new GeoPoint(coord.getDouble(1), coord.getDouble(0)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return route;
    }
}