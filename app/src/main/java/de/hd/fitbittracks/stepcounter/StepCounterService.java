package de.hd.fitbittracks.stepcounter;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.hd.fitbittracks.MainActivity;
import de.hd.fitbittracks.R;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.MilestoneWithTotalDistance;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.repositories.UserProgressRepository;


@AndroidEntryPoint
public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int lastUpdate = -1;
    @Inject
    UserProgressRepository userProgressRepository;
    private static final String CHANNEL_ID = "step_channel";
    protected final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    protected final DecimalFormat df = new DecimalFormat("#,##0.0");


    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase db = AppDatabase.getInstance(this);
        userProgressRepository.getMilestoneProgressEvents().observeForever(event -> {
            sendGoalNotification(event.getContentIfNotHandled());
        });
        userProgressRepository.getAchievementEvents().observeForever(event -> {
            Achievement e = event.getContentIfNotHandled();
            if (e != null) {
                Log.i("StepCounterService", "Achievement event received: " + e);
                sendAchievementNotification(e);
            }
            //Log.i("StepCounterService", "Achievement event received: " + event.getContentIfNotHandled());
            //sendAchievementNotification(event.getContentIfNotHandled());
        });
        userProgressRepository.getTrackWithProgressEvents().observeForever(event -> {
            sendTrackFinishedNotification(event.getContentIfNotHandled());
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        startForeground(1, createNotification("Tracking steps..."));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private Notification createNotification(String content) {
        /*NotificationChannel channel = new NotificationChannel("step_channel", "Step Tracker", NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);*/

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Tracker")
                .setContentText(content)
                .setSmallIcon(R.drawable.steps)
                .build();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int totalSteps = (int) event.values[0];

        if(lastUpdate < 0) {
            lastUpdate = totalSteps;
            return;
        }

        int stepsWalked = totalSteps - lastUpdate;
        if(stepsWalked > 5) {
            Executors.newSingleThreadExecutor().execute(() -> {
                 userProgressRepository.updateStepsWalked(stepsWalked);
            });
            lastUpdate = totalSteps;
        }
    }

    private void sendGoalNotification(Pair<MilestoneWithTotalDistance, UserProgress> pair) {
        if(pair == null) {
            Log.e("StepCounterService", "No milestone data available for notification.");
            return;
        }
        UserProgress userProgress = pair.second;
        MilestoneWithTotalDistance milestone = pair.first;
        RemoteViews collapsedView = getCollapsedGoalView(milestone);
        RemoteViews expandedView = getExpandedGoalView(userProgress, milestone);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.map)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return; // Permission not granted, do not show notification
        }
        NotificationManagerCompat.from(this).notify(2, builder.build());
    }

    private void sendAchievementNotification(Achievement achievement) {
        if (achievement == null) {
            Log.e("StepCounterService", "Achievement data is null for notification.");
            return;
        }
        Log.i("Achievement", "Achievement unlocked: " + achievement);
        RemoteViews remoteViews = getAchievementView(achievement);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(AppImage.getResIdFor(achievement.icon)) // Fallback for system tray
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(createAchievementIntent(achievement))
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return; // Permission not granted, do not show notification
        }
        NotificationManagerCompat.from(this).notify(2, builder.build());
    }

    private void sendTrackFinishedNotification(Pair<Track, UserProgress> pair) {
        if (pair == null) {
            Log.e("StepCounterService", "Track finished data is null for notification.");
            return;
        }
        Track track = pair.first;
        UserProgress userProgress = pair.second;

        RemoteViews collapsedView = getTrackFinishedCollapsedView(track, userProgress);
        RemoteViews expandedView = getTrackFinishedExpandedView(track, userProgress);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.map)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return; // Permission not granted, do not show notification
        }
        NotificationManagerCompat.from(this).notify(3, builder.build());
    }

    private RemoteViews getCollapsedGoalView(MilestoneWithTotalDistance milestone) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_collapsed);
        int textColor = ContextCompat.getColor(this, R.color.notification_text_color);
        contentView.setTextColor(R.id.action_milestone, textColor);
        contentView.setTextColor(R.id.action_progress, textColor);
        contentView.setTextColor(R.id.title, textColor);
        contentView.setTextColor(R.id.success_message, textColor);
        contentView.setTextViewText(R.id.title, milestone.title);
        contentView.setImageViewResource(R.id.milestone_image, AppImage.getResIdFor(milestone.image));

        return contentView;
    }

    private RemoteViews getExpandedGoalView(UserProgress userProgress, MilestoneWithTotalDistance milestone) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_expanded);
        int textColor = ContextCompat.getColor(this, R.color.notification_text_color);
        contentView.setTextColor(R.id.action_milestone, textColor);
        contentView.setTextColor(R.id.action_progress, textColor);
        contentView.setTextColor(R.id.title, textColor);
        contentView.setTextViewText(R.id.title, milestone.title);
        contentView.setImageViewResource(R.id.milestone_image, AppImage.getResIdFor(milestone.image));

        contentView.setOnClickPendingIntent(R.id.action_milestone, createMilestoneIntent(milestone));
        contentView.setOnClickPendingIntent(R.id.action_progress, createProgressIntent(userProgress));
        return contentView;
    }

    private RemoteViews getAchievementView(Achievement achievement) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_achievement);
        int iconColor = switch (achievement.difficulty) {
            case BRONZE -> ContextCompat.getColor(this, R.color.bronze);
            case SILVER -> ContextCompat.getColor(this, R.color.silver);
            case GOLD -> ContextCompat.getColor(this, R.color.gold);
            default -> ContextCompat.getColor(this, R.color.dark_gray);
        };
        int textColor = ContextCompat.getColor(this, R.color.notification_text_color);
        contentView.setTextColor(R.id.notification_achievement_title, textColor);
        contentView.setTextColor(R.id.notification_achievement_desc, textColor);
        contentView.setTextViewText(R.id.notification_achievement_desc, achievement.description);
        //contentView.setImageViewResource(R.id.notification_achievement_icon, AppImage.getResIdFor(achievement.icon));
        contentView.setInt(R.id.notification_achievement_icon, "setColorFilter", iconColor);
        return contentView;
    }

    private RemoteViews getTrackFinishedCollapsedView(Track track, UserProgress userProgress) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_finish_track_collapsed);
        int textColor = ContextCompat.getColor(this, R.color.notification_text_color);
        contentView.setTextColor(R.id.notification_title, textColor);
        contentView.setTextColor(R.id.track_title, textColor);
        contentView.setTextViewText(R.id.track_title, track.name);
        contentView.setImageViewResource(R.id.track_image, AppImage.getResIdFor(track.image));
        return contentView;
    }

    private RemoteViews getTrackFinishedExpandedView(Track track, UserProgress userProgress) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_finish_track_expanded);
        int textColor = ContextCompat.getColor(this, R.color.notification_text_color);
        contentView.setTextColor(R.id.notification_title, textColor);
        contentView.setTextColor(R.id.track_title, textColor);
        contentView.setTextColor(R.id.start_text, textColor);
        contentView.setTextColor(R.id.end_text, textColor);
        contentView.setTextColor(R.id.distance_text, textColor);

        contentView.setInt(R.id.start_icon, "setColorFilter", textColor);
        contentView.setInt(R.id.end_icon, "setColorFilter", textColor);
        contentView.setInt(R.id.distance_icon, "setColorFilter",textColor);

        contentView.setTextViewText(R.id.track_title, track.name);
        contentView.setTextViewText(R.id.start_text, "Start: " + track.startLocation);
        contentView.setTextViewText(R.id.end_text, "End: " + track.endLocation);
        contentView.setTextViewText(R.id.distance_text, formatDistance(userProgress.distanceWalked) + "(" + userProgress.stepsWalked + " steps)");
        contentView.setImageViewResource(R.id.track_image, AppImage.getResIdFor(track.image));

        contentView.setOnClickPendingIntent(R.id.action_track_finished, createTrackFinishIntent(userProgress));
        return contentView;
    }

    private PendingIntent createMilestoneIntent(MilestoneWithTotalDistance milestone) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("milestone_id", milestone.id);
        intent.putExtra("navigate_to", "milestone_fragment");
        // different request code
        return PendingIntent.getActivity(
                this,
                (int) milestone.id + 1000, // different request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent createProgressIntent(UserProgress progress) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigate_to", "progress_fragment");
        intent.putExtra("progress_id", progress.id);
        // unique per milestone
        return PendingIntent.getActivity(
                this,
                (int) progress.id, // unique per milestone
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent createAchievementIntent(Achievement achievement) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("achievement_id", achievement.id);
        intent.putExtra("navigate_to", "achievement_fragment");
        return PendingIntent.getActivity(
                this,
                (int) achievement.id + 5000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent createTrackFinishIntent(UserProgress userProgress) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigate_to", "progress_fragment");
        intent.putExtra("track_finished", true);
        intent.putExtra("progress_id", userProgress.id);
        return PendingIntent.getActivity(
                this,
                (int) userProgress.id + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String formatDistance(double value) {
        return value >= 10000 ? df.format(value/1000) + " km" : numberFormat.format(value) + " m";
    }
}