package de.hd.stepwise.helper;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import de.hd.stepwise.BuildConfig;

public class DownloadHelper {
    private static final AtomicFileDownloader FILE_DOWNLOADER =
            new AtomicFileDownloader(DownloadHelper::openDownloadStream);

    public static String downloadJson(String jsonUrl) throws IOException {
        HttpURLConnection connection = getHttpURLConnection(jsonUrl);

        int code = connection.getResponseCode();
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
        File dir = new File(context.getFilesDir(), "json_cache/");
        File file = new File(dir, cacheFileName);
        return FILE_DOWNLOADER.download(jsonUrl, file);
    }

    public static String downloadTrackImage(
            Context context,
            String imageUrl,
            Long trackId,
            Long milestoneId,
            Long milestoneImageId
    ) throws IOException {
        StringBuilder pathBuilder = new StringBuilder("images/tracks/");
        if (trackId != null) {
            pathBuilder.append(trackId);
        }
        if (milestoneId != null) {
            pathBuilder.append("/milestones/").append(milestoneId);
        }
        File dir = new File(context.getFilesDir(), pathBuilder.toString());
        String fileName = milestoneImageId != null ? "milestone_image_" + milestoneImageId + ".jpg" : milestoneId != null ? "milestone_" + milestoneId + ".jpg" : "track_" + trackId + ".jpg";
        File file = new File(dir, fileName);
        return FILE_DOWNLOADER.download(imageUrl, file);
    }

    public static String downloadMilestoneAsset(Context context, String assetUrl, long trackId,
                                                long milestoneId, String fileName)
            throws IOException {
        File directory = new File(context.getFilesDir(),
                "images/tracks/" + trackId + "/milestones/" + milestoneId);
        return FILE_DOWNLOADER.download(assetUrl, new File(directory, fileName));
    }

    private static InputStream openDownloadStream(String url) throws IOException {
        HttpURLConnection connection = getHttpURLConnection(url);
        return new FilterInputStream(connection.getInputStream()) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    connection.disconnect();
                }
            }
        };
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
