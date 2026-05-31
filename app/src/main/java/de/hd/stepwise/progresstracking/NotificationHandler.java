package de.hd.stepwise.progresstracking;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.hd.stepwise.MainActivity;
import de.hd.stepwise.R;
import de.hd.stepwise.entities.Achievement;
import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.Track;
import de.hd.stepwise.entities.UserProgress;
import de.hd.stepwise.enums.AppImage;
import de.hd.stepwise.pojos.events.StepUpdateResult;

@Singleton
public class NotificationHandler {
    private final Context context;
    private static final String CHANNEL_ID = "step_channel";
    protected final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    protected final DecimalFormat df = new DecimalFormat("#,##0.0");

    @Inject
    public NotificationHandler(@ApplicationContext Context context) {
        this.context = context;
    }

    public void handleStepUpdate(StepUpdateResult stepUpdateResult) {
        if(stepUpdateResult == null) return;
        if (stepUpdateResult.reachedMilestones != null) {
            stepUpdateResult.reachedMilestones.forEach(mileStone -> {
                Log.i("NotificationHandler", "Milestone reached: " + mileStone.title);
                showGoalNotification(new Pair<>(mileStone, stepUpdateResult.progress));
            });
        }
        if (stepUpdateResult.unlockedAchievements != null) {
            stepUpdateResult.unlockedAchievements.forEach(achievement -> {
                Log.i("NotificationHandler", "Achievement unlocked: " + achievement.title);
                showAchievementNotification(achievement);
            });
        }
        if (stepUpdateResult.finishedTrack != null) {
            Log.i("NotificationHandler", "Track finished: " + stepUpdateResult.finishedTrack);
            showTrackFinishedNotification(new Pair<>(stepUpdateResult.finishedTrack, stepUpdateResult.progress));
        }
    }

    public void showGoalNotification(Pair<MilestoneWithTotalDistance, UserProgress> pair) {
        if(pair == null) {
            Log.e("StepCounterService", "No milestone data available for notification.");
            return;
        }
        UserProgress userProgress = pair.second;
        MilestoneWithTotalDistance milestone = pair.first;
        RemoteViews collapsedView = getCollapsedGoalView(milestone);
        RemoteViews expandedView = getExpandedGoalView(userProgress, milestone);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.map)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return; // Permission not granted, do not show notification
        }
        NotificationManagerCompat.from(context).notify(2, builder.build());
    }

    public void showAchievementNotification(Achievement achievement) {
        if (achievement == null) {
            Log.e("StepCounterService", "Achievement data is null for notification.");
            return;
        }
        Log.i("Achievement", "Achievement unlocked: " + achievement);
        RemoteViews remoteViews = getAchievementView(achievement);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(AppImage.getResIdFor(achievement.icon)) // Fallback for system tray
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(createAchievementIntent(achievement))
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return; // Permission not granted, do not show notification
        }
        NotificationManagerCompat.from(context).notify(2, builder.build());
    }

    private void showTrackFinishedNotification(Pair<Track, UserProgress> pair) {
        if (pair == null) {
            Log.e("StepCounterService", "Track finished data is null for notification.");
            return;
        }
        Track track = pair.first;
        UserProgress userProgress = pair.second;

        RemoteViews collapsedView = getTrackFinishedCollapsedView(track, userProgress);
        RemoteViews expandedView = getTrackFinishedExpandedView(track, userProgress);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.map)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return; // Permission not granted, do not show notification
        }
        NotificationManagerCompat.from(context).notify(3, builder.build());
    }

    private RemoteViews getCollapsedGoalView(MilestoneWithTotalDistance milestone) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_collapsed);
        int textColor = ContextCompat.getColor(context, R.color.notification_text_color);
        contentView.setTextColor(R.id.action_milestone, textColor);
        contentView.setTextColor(R.id.action_progress, textColor);
        contentView.setTextColor(R.id.title, textColor);
        contentView.setTextColor(R.id.success_message, textColor);
        contentView.setTextViewText(R.id.title, milestone.title);
        Bitmap bitmap = loadBitmap(milestone.localImagePath);
        if (bitmap != null) {
            contentView.setImageViewBitmap(R.id.milestone_image, bitmap);
        }else {
            contentView.setImageViewResource(R.id.milestone_image, R.drawable.map);
        }

        return contentView;
    }

    private RemoteViews getExpandedGoalView(UserProgress userProgress, MilestoneWithTotalDistance milestone) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);
        int textColor = ContextCompat.getColor(context, R.color.notification_text_color);
        contentView.setTextColor(R.id.action_milestone, textColor);
        contentView.setTextColor(R.id.action_progress, textColor);
        contentView.setTextColor(R.id.title, textColor);
        contentView.setTextViewText(R.id.title, milestone.title);
        Bitmap bitmap = loadBitmap(milestone.localImagePath);
        if (bitmap != null) {
            contentView.setImageViewBitmap(R.id.milestone_image, bitmap);
        }else {
            contentView.setImageViewResource(R.id.milestone_image, R.drawable.map);
        }

        contentView.setOnClickPendingIntent(R.id.action_milestone, createMilestoneIntent(milestone));
        contentView.setOnClickPendingIntent(R.id.action_progress, createProgressIntent(userProgress));
        return contentView;
    }

    private RemoteViews getAchievementView(Achievement achievement) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_achievement);
        int iconColor = switch (achievement.difficulty) {
            case STONE -> ContextCompat.getColor(context, R.color.stone);
            case BRONZE -> ContextCompat.getColor(context, R.color.bronze);
            case SILVER -> ContextCompat.getColor(context, R.color.silver);
            case GOLD -> ContextCompat.getColor(context, R.color.gold);
            default -> ContextCompat.getColor(context, R.color.dark_gray);
        };
        int textColor = ContextCompat.getColor(context, R.color.notification_text_color);
        contentView.setTextColor(R.id.notification_achievement_title, textColor);
        contentView.setTextColor(R.id.notification_achievement_desc, textColor);
        contentView.setTextViewText(R.id.notification_achievement_desc, achievement.description);
        contentView.setImageViewResource(R.id.notification_achievement_icon, AppImage.getResIdFor(achievement.icon));
        contentView.setInt(R.id.notification_achievement_icon, "setColorFilter", iconColor);
        return contentView;
    }

    private RemoteViews getTrackFinishedCollapsedView(Track track, UserProgress userProgress) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_finish_track_collapsed);
        int textColor = ContextCompat.getColor(context, R.color.notification_text_color);
        contentView.setTextColor(R.id.notification_title, textColor);
        contentView.setTextColor(R.id.track_title, textColor);
        contentView.setTextViewText(R.id.track_title, track.name);
        Bitmap bitmap = loadBitmap(track.localImagePath);
        if (bitmap != null) {
            contentView.setImageViewBitmap(R.id.track_image, bitmap);
        }else {
            contentView.setImageViewResource(R.id.track_image, R.drawable.map);
        }
        return contentView;
    }

    private RemoteViews getTrackFinishedExpandedView(Track track, UserProgress userProgress) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_finish_track_expanded);
        int textColor = ContextCompat.getColor(context, R.color.notification_text_color);
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
        Bitmap bitmap = loadBitmap(track.localImagePath);
        if (bitmap != null) {
            contentView.setImageViewBitmap(R.id.track_image, bitmap);
        }else {
            contentView.setImageViewResource(R.id.track_image, R.drawable.map);
        }
        contentView.setOnClickPendingIntent(R.id.action_track_finished, createTrackFinishIntent(userProgress));
        return contentView;
    }

    private PendingIntent createMilestoneIntent(MilestoneWithTotalDistance milestone) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("milestone_id", milestone.id);
        intent.putExtra("navigate_to", "milestone_fragment");
        // different request code
        return PendingIntent.getActivity(
                context,
                (int) milestone.id + 1000, // different request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent createProgressIntent(UserProgress progress) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("navigate_to", "progress_fragment");
        intent.putExtra("progress_id", progress.id);
        // unique per milestone
        return PendingIntent.getActivity(
                context,
                (int) progress.id, // unique per milestone
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent createAchievementIntent(Achievement achievement) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("achievement_id", achievement.id);
        intent.putExtra("navigate_to", "achievement_fragment");
        return PendingIntent.getActivity(
                context,
                (int) achievement.id + 5000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent createTrackFinishIntent(UserProgress userProgress) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("navigate_to", "progress_fragment");
        intent.putExtra("track_finished", true);
        intent.putExtra("progress_id", userProgress.id);
        return PendingIntent.getActivity(
                context,
                (int) userProgress.id + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private String formatDistance(double value) {
        return value >= 10000 ? df.format(value/1000) + " km" : numberFormat.format(value) + " m";
    }

    private Bitmap loadBitmap(String localImagePath) {
        if (localImagePath == null) return null;

        File file = new File(localImagePath);
        if (!file.exists()) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }
}
