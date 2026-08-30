package de.hd.stepwise.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.hd.stepwise.R;
import de.hd.stepwise.repositories.JourneySummaryRepository;

@Singleton
public class JourneyWidgetUpdater {
    public static final String ACTION_REFRESH = "de.hd.stepwise.widget.REFRESH";

    private final Context context;
    private final JourneySummaryRepository repository;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public JourneyWidgetUpdater(@ApplicationContext Context context,
                                JourneySummaryRepository repository) {
        this.context = context;
        this.repository = repository;
    }

    public static void requestUpdate(Context context) {
        context.sendBroadcast(new Intent(context, JourneyWidgetProvider.class)
                .setAction(ACTION_REFRESH));
    }

    public void updateAll() {
        executor.execute(() -> {
            JourneyWidgetState state;
            try {
                state = JourneyWidgetState.from(repository.getCurrentJourneySync());
            } catch (RuntimeException exception) {
                state = JourneyWidgetState.unavailable();
            }
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(
                    new ComponentName(context, JourneyWidgetProvider.class));
            for (int id : ids) manager.updateAppWidget(id, render(state));
        });
    }

    RemoteViews render(JourneyWidgetState state) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_journey);
        boolean journey = state.kind == JourneyWidgetState.Kind.ACTIVE
                || state.kind == JourneyWidgetState.Kind.PAUSED;
        views.setViewVisibility(R.id.widget_journey_content,
                journey ? android.view.View.VISIBLE : android.view.View.GONE);
        views.setViewVisibility(R.id.widget_empty_content,
                journey ? android.view.View.GONE : android.view.View.VISIBLE);
        if (journey) {
            views.setTextViewText(R.id.widget_track_name,
                    textOr(state.trackName, R.string.widget_current_journey));
            views.setTextViewText(R.id.widget_status, state.kind == JourneyWidgetState.Kind.PAUSED
                    ? context.getString(R.string.widget_paused)
                    : context.getString(R.string.widget_active));
            views.setProgressBar(R.id.widget_progress, 100, state.progressPercent, false);
            views.setTextViewText(R.id.widget_percentage,
                    context.getString(R.string.widget_percentage, state.progressPercent));
            views.setTextViewText(R.id.widget_distance,
                    context.getString(R.string.widget_distance,
                            NumberFormat.getNumberInstance(Locale.getDefault())
                                    .format(state.distanceWalked / 1000f)));
            views.setTextViewText(R.id.widget_next_milestone,
                    context.getString(R.string.widget_next_milestone,
                            textOr(state.nextMilestone, state.nextMilestone == null
                                    ? R.string.widget_finish_line
                                    : R.string.widget_next_unknown)));
            Bitmap image = decodeImage(state.imagePath);
            if (image == null) views.setImageViewResource(R.id.widget_track_image, R.drawable.map);
            else views.setImageViewBitmap(R.id.widget_track_image, image);
            views.setOnClickPendingIntent(R.id.widget_journey_content,
                    PendingIntent.getActivity(context, (int) state.progressId,
                            JourneyWidgetIntents.progress(context, state.progressId),
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        } else {
            views.setTextViewText(R.id.widget_empty_text,
                    state.kind == JourneyWidgetState.Kind.UNAVAILABLE
                            ? context.getString(R.string.widget_temporarily_unavailable)
                            : context.getString(R.string.widget_empty));
            views.setOnClickPendingIntent(R.id.widget_empty_content,
                    PendingIntent.getActivity(context, 0, JourneyWidgetIntents.tracks(context),
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        }
        return views;
    }

    private static Bitmap decodeImage(String path) {
        if (path == null || path.isBlank()) return null;
        File file = new File(path);
        if (!file.isFile()) return null;
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
        int sampleSize = 1;
        while (bounds.outWidth / sampleSize > 288 || bounds.outHeight / sampleSize > 288) {
            sampleSize *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(path, options);
    }

    private String textOr(String value, int fallbackResource) {
        return value == null || value.isBlank() ? context.getString(fallbackResource) : value;
    }
}
