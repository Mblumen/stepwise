package de.hd.stepwise.helper;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import de.hd.stepwise.BuildConfig;

public class DownloadHelper {
    public static String downloadJson(String jsonUrl) throws IOException {
        HttpURLConnection connection = getHttpURLConnection(jsonUrl);

        int code = connection.getResponseCode();
        Log.d("TokenTest", "Response Code: " + code);
        if (code != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP " + code);
        }

        try (InputStream is = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            connection.disconnect();
        }
    }

    public static String downloadAndCacheJson(
            Context context,
            String jsonUrl,
            String cacheFileName
    ) throws IOException {
        HttpURLConnection connection = getHttpURLConnection(jsonUrl);
        connection.connect();
        File dir = new File(context.getFilesDir(), "json_cache/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, cacheFileName);

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(file)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        return file.getAbsolutePath();
    }

    public static String downloadAchievementIcon(
            Context context,
            String imageUrl,
            Long achievementId
    ) throws IOException {
        HttpURLConnection connection = getHttpURLConnection(imageUrl);
        connection.connect();
        File dir = new File(context.getFilesDir(), "images/achievements/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "achievement_" + achievementId + ".xml");

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(file)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }

        return file.getAbsolutePath();
    }

    public static String downloadTrackImage(
            Context context,
            String imageUrl,
            Long trackId,
            Long milestoneId,
            Long milestoneImageId
    ) throws IOException {
        HttpURLConnection connection = getHttpURLConnection(imageUrl);
        connection.connect();
        StringBuilder pathBuilder = new StringBuilder("images/tracks/");
        if (trackId != null) {
            pathBuilder.append(trackId);
        }
        if (milestoneId != null) {
            pathBuilder.append("/milestones/").append(milestoneId);
        }
        File dir = new File(context.getFilesDir(), pathBuilder.toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = milestoneImageId != null ? "milestone_image_" + milestoneImageId + ".jpg" : milestoneId != null ? "milestone_" + milestoneId + ".jpg" : "track_" + trackId + ".jpg";
        File file = new File(dir, fileName);

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(file)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }

        return file.getAbsolutePath();
    }

    @NonNull
    private static HttpURLConnection getHttpURLConnection(String jsonUrl) throws IOException {
        URL url = new URL(jsonUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty(
                "Authorization",
                "Bearer " + BuildConfig.GITHUB_TOKEN
        );
        connection.setRequestProperty(
                "Accept",
                "application/vnd.github+json"
        );
        connection.connect();
        return connection;
    }


}
