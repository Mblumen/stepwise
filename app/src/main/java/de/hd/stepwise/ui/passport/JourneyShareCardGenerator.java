package de.hd.stepwise.ui.passport;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.hd.stepwise.pojos.JourneyPassport;
import de.hd.stepwise.R;

@Singleton
public class JourneyShareCardGenerator {
    private final Context context;
    private final Executor executor;

    @Inject
    public JourneyShareCardGenerator(@ApplicationContext Context context) {
        this(context, Executors.newSingleThreadExecutor());
    }

    JourneyShareCardGenerator(Context context, Executor executor) {
        this.context = context;
        this.executor = executor;
    }

    public void generate(JourneyPassport passport, Consumer<Uri> success,
                         Consumer<Exception> failure) {
        executor.execute(() -> {
            try {
                File directory = new File(context.getCacheDir(), "passport-shares");
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IllegalStateException("Could not create passport share directory");
                }
                File output = new File(directory, "journey-" + passport.progressId + ".png");
                render(passport, output);
                success.accept(FileProvider.getUriForFile(context,
                        context.getPackageName() + ".fileprovider", output));
            } catch (Exception exception) {
                failure.accept(exception);
            }
        });
    }

    public static Intent createShareIntent(Uri uri) {
        return new Intent(Intent.ACTION_SEND)
                .setType("image/png")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    private void render(JourneyPassport passport, File output) throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(1080, 720, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.rgb(245, 241, 228));
        Paint title = new Paint(Paint.ANTI_ALIAS_FLAG);
        title.setColor(Color.rgb(30, 60, 70));
        title.setTextSize(64);
        title.setFakeBoldText(true);
        Paint body = new Paint(Paint.ANTI_ALIAS_FLAG);
        body.setColor(Color.DKGRAY);
        body.setTextSize(38);
        canvas.drawText(context.getString(R.string.journey_passport), 64, 100, title);
        canvas.drawText(passport.displayTrackName(context.getString(R.string.passport_unknown_name)),
                64, 190, title);
        canvas.drawText(context.getString(R.string.passport_route,
                passport.displayStart(context.getString(R.string.passport_unknown_start)),
                passport.displayDestination(context.getString(R.string.passport_unknown_destination))),
                64, 260, body);
        String completed = passport.completedAt == null
                ? context.getString(R.string.passport_date_unknown)
                : DateFormat.getDateInstance().format(new Date(passport.completedAt));
        canvas.drawText(context.getString(R.string.passport_card_completed, completed),
                64, 340, body);
        canvas.drawText(context.getString(R.string.passport_card_stats,
                passport.stepsWalked, passport.distanceWalked / 1000f), 64, 410, body);
        List<Bitmap> stamps = new ArrayList<>();
        passport.reachedMilestones.forEach(item -> {
            if (stamps.size() >= 6) return;
            if (item.milestone.localStampImagePath == null) return;
            Bitmap stamp = BitmapFactory.decodeFile(item.milestone.localStampImagePath);
            if (stamp != null) stamps.add(stamp);
        });
        canvas.drawText(context.getResources().getQuantityString(R.plurals.passport_card_stamps,
                stamps.size(), stamps.size()), 64, 480, body);
        int stampX = 64;
        for (Bitmap stamp : stamps) {
            canvas.drawBitmap(stamp, null,
                    new android.graphics.Rect(stampX, 520, stampX + 120, 640), body);
            stamp.recycle();
            stampX += 140;
        }
        try (FileOutputStream stream = new FileOutputStream(output)) {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                throw new IllegalStateException("Could not encode passport share card");
            }
        } finally {
            bitmap.recycle();
        }
    }
}
