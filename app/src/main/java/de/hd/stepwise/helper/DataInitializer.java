package de.hd.stepwise.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Transaction;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import de.hd.stepwise.database.AppDatabase;
import de.hd.stepwise.dtos.AchievementJson;
import de.hd.stepwise.dtos.AchievementJsonRoot;
import de.hd.stepwise.dtos.MilestoneJson;
import de.hd.stepwise.dtos.TrackJson;
import de.hd.stepwise.dtos.TrackJsonRoot;
import de.hd.stepwise.entities.Achievement;
import de.hd.stepwise.entities.Milestone;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.enums.AchievementDifficulty;
import de.hd.stepwise.enums.AchievementType;

public class DataInitializer {

    private static final String PREFS_NAME = "data_init_prefs";
    private static final String KEY_TRACKS_VERSION = "tracks_version_";
    private static final String KEY_ACHIEVEMENTS_VERSION = "achievements_version";
    private static final String TRACKS_JSON_URL = "https://raw.githubusercontent.com/Mblumen/virtual-tracks/main/json/tracks.json";
    private static final String ACHIEVEMENTS_JSON_URL = "https://raw.githubusercontent.com/Mblumen/virtual-tracks/main/json/achievements.json";

    public enum DataType {
        TRACKS,
        ACHIEVEMENTS,
        ALL
    }

    private static void runInNewThread(Runnable task) {
        Executors.newSingleThreadExecutor().execute(task);
    }

    public static void checkUpdateAvailable(Context context, DataType type, Consumer<Boolean> callback) {
        runInNewThread(() -> {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            try {
                switch (type) {
                    case TRACKS:
                        String tracksJson = DownloadHelper.downloadJson(TRACKS_JSON_URL);
                        TrackJsonRoot trackRoot = parseJson(tracksJson, new TypeToken<>() {});
                        for (var trackEntry : trackRoot.tracks) {
                            int currentVersion = prefs.getInt(KEY_TRACKS_VERSION + trackEntry.name, 0);
                            if (trackEntry.version > currentVersion) {
                                if(callback != null) callback.accept(true);
                                return;
                            }
                        }
                        break;
                    case ACHIEVEMENTS:
                        String achievementsJson = DownloadHelper.downloadJson(ACHIEVEMENTS_JSON_URL);
                        AchievementJsonRoot achievementRoot = parseJson(achievementsJson, new TypeToken<>() {});
                        int currentVersion = prefs.getInt(KEY_ACHIEVEMENTS_VERSION, 0);
                        if (achievementRoot.version > currentVersion) {
                            if(callback != null) callback.accept(true);
                            return;
                        }
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                if(callback != null) callback.accept(false);
            }
            if(callback != null) callback.accept(false);
        });
    }
    public static void updateDataset(Context context, AppDatabase db, DataType type, Runnable cleanUp, Consumer<String> onCompleteSuccess, Runnable onFailure, Runnable onCompleteNothingToUpdate) {
        runInNewThread(() -> {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            List<String> updatedTracks = new ArrayList<>();
            boolean updatedAchievements = false;
            try {
                switch (type) {
                    case TRACKS:
                        updatedTracks = updateTracks(db, context, prefs);
                        break;
                    case ACHIEVEMENTS:
                        updatedAchievements = updateAchievements(db, prefs);
                        break;
                    case ALL:
                        updatedTracks = updateTracks(db, context, prefs);
                        updatedAchievements = updateAchievements(db, prefs);
                        break;
                }
                if (updatedAchievements || !updatedTracks.isEmpty()) {
                    StringBuilder resultMessage = new StringBuilder("Updated: ");
                    if (updatedAchievements) {
                        resultMessage.append("Achievements ");
                    }
                    if (!updatedTracks.isEmpty()) {
                        if (updatedAchievements) resultMessage.append("| ");
                        resultMessage.append("Tracks ").append(String.join(", ", updatedTracks));
                    }
                    if (onCompleteSuccess != null) onCompleteSuccess.accept(resultMessage.toString());
                } else {
                    if (onCompleteNothingToUpdate != null) onCompleteNothingToUpdate.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (onFailure != null) onFailure.run();
            } finally {
                if (cleanUp != null) cleanUp.run();
            }
        });
    }

    private static List<String> updateTracks(AppDatabase db, Context context, SharedPreferences prefs) throws IOException {
        String json = DownloadHelper.downloadJson(TRACKS_JSON_URL);
        TrackJsonRoot root = parseJson(json, new TypeToken<>() {});
        List<String> updatedTracks = new ArrayList<>();
        if (root.tracks == null) return updatedTracks;
        root.tracks.forEach(trackEntry -> {
            int currentVersion = prefs.getInt(KEY_TRACKS_VERSION + trackEntry.name, 0);
            if (trackEntry.version > currentVersion) {
                try {
                    String trackJson = DownloadHelper.downloadJson(trackEntry.file);
                    TrackJson track = parseJson(trackJson, new TypeToken<>() {});
                    db.runInTransaction(() -> insertTrack(db, track));
                    downloadImagesIfNecessary(context, db, track);
                    downloadGeoDataIfNecessary(context, db, track);
                    updatedTracks.add(trackEntry.name);
                    prefs.edit().putInt(KEY_TRACKS_VERSION + trackEntry.name, trackEntry.version).apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return updatedTracks;
    }

    private static boolean updateAchievements(AppDatabase db, SharedPreferences prefs) throws IOException {
        int currentVersion = prefs.getInt(KEY_ACHIEVEMENTS_VERSION, 0);
        String json = DownloadHelper.downloadJson(ACHIEVEMENTS_JSON_URL);
        AchievementJsonRoot root = parseJson(json, new TypeToken<>() {});
        if (root.version <= currentVersion) return false;
        prefs.edit().putInt(KEY_ACHIEVEMENTS_VERSION, root.version).apply();
        db.runInTransaction(() -> insertAchievements(db, root.achievements));
        return true;
    }

    private static<T> T parseJson(String json, TypeToken<T> typeToken) {
        return new Gson().fromJson(json, typeToken.getType());
    }

    @Transaction
    private static void insertTrack(AppDatabase db, TrackJson json) {
        Track track = db.trackDao().getTrackByName(json.name);
        if (track == null) track = new Track();
        track.name = json.name;
        track.startLocation = json.startLocation;
        track.endLocation = json.endLocation;
        track.imageUrl = json.imageUrl;
        track.challengeDuration = json.challengeDuration;
        track.trackRoute = json.trackRoute;
        long newTrackId = db.trackDao().insertTrack(track);
        long trackId = track.id != 0 ? track.id : newTrackId;


        for (MilestoneJson mj : json.milestones) {
            Milestone m = db.milestoneDao().getMilestoneByTitleAndTrackId(mj.title, trackId);
            if (m == null) m = new Milestone();
            m.trackId = trackId;
            m.distanceOffsetToPrevious = mj.distanceOffsetToPrevious;
            m.title = mj.title;
            m.description = mj.description;
            m.mapsUrl = mj.mapsUrl;
            m.latitude = mj.latitude != null ? mj.latitude : 0;
            m.longitude = mj.longitude != null ? mj.longitude : 0;
            m.imageUrl = mj.imageUrl;
            m.extraImages = mj.extraImages;

            db.milestoneDao().insertMilestone(m);
        }
    }

    private static void downloadImagesIfNecessary(Context context, AppDatabase db, TrackJson json) {
        Track track = db.trackDao().getTrackByName(json.name);
        if (track == null) return;
        if (track.imageUrl != null) {
            try {
                String localPath = DownloadHelper.downloadTrackImage(context, track.imageUrl, track.id, null, null);
                db.trackDao().updateLocalImagePath(track.id, localPath);
            } catch (IOException e) { e.printStackTrace(); }
        }

        for (MilestoneJson mj : json.milestones) {
            Milestone m = db.milestoneDao().getMilestoneByTitleAndTrackId(mj.title, track.id);
            if(m == null) continue;
            if (m.imageUrl != null) {
                try {
                    String localPath = DownloadHelper.downloadTrackImage(context, m.imageUrl, m.trackId, m.id, null);
                    db.milestoneDao().updateLocalImagePath(m.id, localPath);
                } catch (IOException e) { e.printStackTrace(); }
            }

            if (m.extraImages != null && !m.extraImages.isEmpty()) {
                for (int i = 0; i < m.extraImages.size(); i++) {
                    String imageUrl = m.extraImages.get(i).imageUrl;
                    if (imageUrl != null) {
                        try {
                            m.extraImages.get(i).localImagePath = DownloadHelper.downloadTrackImage(context, imageUrl, m.trackId, m.id, m.extraImages.get(i).position);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                db.milestoneDao().updateExtraImages(m.id, m.extraImages);
            }
        }
    }

    private static void downloadGeoDataIfNecessary(Context context, AppDatabase db, TrackJson json) {
        Track track = db.trackDao().getTrackByName(json.name);
        if(track == null) return;
        if (track.trackRoute != null && track.trackRoute.geoDataUrl != null && !track.trackRoute.geoDataUrl.isEmpty()) {
            try {
                track.trackRoute.localGeoDataPath = DownloadHelper.downloadAndCacheJson(context, track.trackRoute.geoDataUrl, "track_route_" + track.id + ".json");
                db.trackDao().updateTrackRoute(track.id, track.trackRoute);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void insertAchievements(AppDatabase db, List<AchievementJson> achievements) {
        for (AchievementJson json : achievements) {
            Achievement achievement = db.achievementDao().getByKey(json.key);
            if (achievement == null) achievement = new Achievement();
            achievement.key = json.key;
            achievement.title = json.title;
            achievement.description = json.description;
            achievement.icon = json.icon;
            achievement.type = AchievementType.fromString(json.type);
            achievement.difficulty = AchievementDifficulty.fromString(json.difficulty);
            achievement.targetValue = json.targetValue;
            db.achievementDao().insert(achievement);
        }
    }
}
