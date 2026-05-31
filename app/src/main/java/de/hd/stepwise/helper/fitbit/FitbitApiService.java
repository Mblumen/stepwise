package de.hd.stepwise.helper.fitbit;

import android.util.Log;

import net.openid.appauth.AuthState;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.helper.fitbit.auth.FitbitAuthStateManager;

@Singleton
public class FitbitApiService {

    public static enum ApiInterval {
        HOURLY("1h"),
        DAILY("1d"),
        WEEKLY("7d");
        private final String apiValue;
        ApiInterval(String apiValue) {
            this.apiValue = apiValue;
        }
        public String getApiValue() {
            return apiValue;
        }
    }

    public final String STEPS_REQUEST = "https://api.fitbit.com/1/user/-/activities/steps/date/";
    private final FitbitAuthStateManager fitbitAuthStateManager;
    @Inject
    public FitbitApiService(FitbitAuthStateManager authStateManager) {
        this.fitbitAuthStateManager = authStateManager;
    }

    public void getStepsData(@Nullable LocalDate startDate, LocalDate endDate, ApiInterval interval, Consumer<List<FitbitSyncStateManager.DailyStepRecord>> callback) {
        AuthState.AuthStateAction accessTokenAction = (accessToken, idToken, ex) -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        URL url = new URL(STEPS_REQUEST + (startDate != null ? startDate : endDate) + "/" + endDate + "/" + interval.getApiValue() + ".json");
                        Log.d("FitbitSync", "Requesting Fitbit API: " + url);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                        conn.setRequestMethod("GET");

                        int responseCode = conn.getResponseCode();
                        if (responseCode == 200) {
                            InputStream in = new BufferedInputStream(conn.getInputStream());
                            String body = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
                            Log.d("FitbitSync", "API Response: " + body);
                            List<FitbitSyncStateManager.DailyStepRecord> parsedResult = parseStepsResponse(body);
                            callback.accept(parsedResult);

                        } else {
                            Log.e("FitbitSync", "Fitbit API error: " + responseCode);
                        }
                        conn.disconnect();
                    } catch (Exception e) {
                Log.e("FitbitSync", "Error calling Fitbit API", e);
            }});
        };
        fitbitAuthStateManager.performActionWithFreshTokens(accessTokenAction);
    }

    private List<FitbitSyncStateManager.DailyStepRecord> parseStepsResponse(String json) throws Exception {
        List<FitbitSyncStateManager.DailyStepRecord> stepsByDate = new ArrayList<>();
        JSONObject obj = new JSONObject(json);
        JSONArray stepsArray = obj.getJSONArray("activities-steps");

        for (int i = 0; i < stepsArray.length(); i++) {
            JSONObject entry = stepsArray.getJSONObject(i);
            LocalDate date = LocalDate.parse(entry.getString("dateTime")); // "2026-02-23"
            int value = entry.getInt("value");
            stepsByDate.add(new FitbitSyncStateManager.DailyStepRecord(date, value, StepSource.FITBIT));
        }
        return stepsByDate;
    }
}
