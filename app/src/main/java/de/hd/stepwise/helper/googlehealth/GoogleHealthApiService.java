package de.hd.stepwise.helper.googlehealth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.helper.fitbit.FitbitSyncStateManager;

@Singleton
public class GoogleHealthApiService {

    static final String DAILY_STEPS_URL =
            "https://health.googleapis.com/v4/users/me/dataTypes/steps/dataPoints:dailyRollUp";

    private final GoogleHealthAuthManager authManager;

    @Inject
    public GoogleHealthApiService(GoogleHealthAuthManager authManager) {
        this.authManager = authManager;
    }

    public List<FitbitSyncStateManager.DailyStepRecord> getStepsData(
            LocalDate startDate, LocalDate endDate) throws Exception {
        String accessToken = authManager.getAccessTokenSync();
        ApiResponse response = executeRequest(accessToken, startDate, endDate);
        if (response.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            authManager.clearAccessTokenSync(accessToken);
            accessToken = authManager.getAccessTokenSync();
            response = executeRequest(accessToken, startDate, endDate);
        }
        if (response.statusCode < 200 || response.statusCode >= 300) {
            throw new IOException("Google Health API returned HTTP " + response.statusCode);
        }
        return parseDailyRollUpResponse(response.body);
    }

    private ApiResponse executeRequest(String accessToken, LocalDate startDate,
                                       LocalDate endDate) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(DAILY_STEPS_URL).openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setConnectTimeout(15_000);
            connection.setReadTimeout(30_000);
            connection.setDoOutput(true);

            byte[] requestBody = createRequestBody(startDate, endDate)
                    .getBytes(StandardCharsets.UTF_8);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody);
            }

            int statusCode = connection.getResponseCode();
            InputStream responseStream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = responseStream == null ? "" : readBody(responseStream);
            return new ApiResponse(statusCode, body);
        } finally {
            connection.disconnect();
        }
    }

    static String createRequestBody(LocalDate startDate, LocalDate endDate) {
        JsonObject body = new JsonObject();
        JsonObject range = new JsonObject();
        range.add("start", createCivilDateTime(startDate));
        // dailyRollUp uses a closed-open range. Include the requested end date.
        range.add("end", createCivilDateTime(endDate.plusDays(1)));
        body.add("range", range);
        body.addProperty("windowSizeDays", 1);
        return body.toString();
    }

    private static JsonObject createCivilDateTime(LocalDate date) {
        JsonObject dateObject = new JsonObject();
        dateObject.addProperty("year", date.getYear());
        dateObject.addProperty("month", date.getMonthValue());
        dateObject.addProperty("day", date.getDayOfMonth());

        JsonObject civilDateTime = new JsonObject();
        civilDateTime.add("date", dateObject);
        civilDateTime.add("time", new JsonObject());
        return civilDateTime;
    }

    static List<FitbitSyncStateManager.DailyStepRecord> parseDailyRollUpResponse(String json) {
        List<FitbitSyncStateManager.DailyStepRecord> records = new ArrayList<>();
        JsonObject root = new JsonParser().parse(json).getAsJsonObject();
        JsonArray dataPoints = root.has("rollupDataPoints")
                ? root.getAsJsonArray("rollupDataPoints")
                : new JsonArray();

        for (JsonElement element : dataPoints) {
            JsonObject dataPoint = element.getAsJsonObject();
            JsonObject date = dataPoint.getAsJsonObject("civilStartTime").getAsJsonObject("date");
            JsonObject steps = dataPoint.getAsJsonObject("steps");
            int count = 0;
            if (steps != null && steps.has("countSum")) {
                count = Math.toIntExact(steps.get("countSum").getAsLong());
            }
            records.add(new FitbitSyncStateManager.DailyStepRecord(
                    LocalDate.of(
                            date.get("year").getAsInt(),
                            date.get("month").getAsInt(),
                            date.get("day").getAsInt()),
                    count,
                    StepSource.FITBIT));
        }
        return records;
    }

    private static String readBody(InputStream inputStream) throws IOException {
        try (InputStream in = new BufferedInputStream(inputStream);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private record ApiResponse(int statusCode, String body) {
    }
}
