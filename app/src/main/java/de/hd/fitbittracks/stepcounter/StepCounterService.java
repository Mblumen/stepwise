package de.hd.fitbittracks.stepcounter;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.Executors;

import de.hd.fitbittracks.MainActivity;
import de.hd.fitbittracks.R;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.entities.UserProgressMilestoneStatus;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.enums.ResultStatus;
import de.hd.fitbittracks.pojos.MethodResultWithData;
import de.hd.fitbittracks.pojos.Pair;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;
import de.hd.fitbittracks.repositories.UserProgressRepository;

public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int lastUpdate = -1;
    private UserProgressRepository userProgressRepository;


    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase db = AppDatabase.getInstance(this);
        userProgressRepository = new UserProgressRepository(db.userProgressDao(), db.trackDao());

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

        return new NotificationCompat.Builder(this, "step_channel")
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
                MethodResultWithData<Pair<UserProgress, Milestone>> result = userProgressRepository.updateStepsWalked(stepsWalked);
                if(result != null && result.status == ResultStatus.SUCCESS) {
                    Log.d("StepCounterService", "Steps updated: " + stepsWalked);
                    sendGoalNotification(result.message, result.data);
                }
            });
            lastUpdate = totalSteps;
        }
    }

    private void sendGoalNotification(String message, Pair<UserProgress, Milestone> pair) {
        if(pair == null) {
            Log.e("StepCounterService", "No milestone data available for notification.");
            return;
        }
        UserProgress userProgress = pair.first;
        Milestone milestone = pair.second;
        PendingIntent pendingIntent = createMessageIntent(message, pair);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "step_channel")
                .setSmallIcon(R.drawable.steps)
                .setContentTitle("🎉 Goal Reached!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(BitmapFactory.decodeResource(getResources(), AppImage.getResIdFor(milestone.image)))
                        .bigLargeIcon(null)) // optional
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        NotificationManagerCompat.from(this).notify(2, builder.build());

    }

    private PendingIntent createMessageIntent(String message, Pair<UserProgress, Milestone> pair) {
        Log.d("StepCounterService", "Creating intent for message: " + message);
        Log.d("StepCounterService", "UserProgress ID: " + pair.first.id + ", Milestone ID: " + pair.second.id);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigate_to", "progress_fragment");
        intent.putExtra("progress_id", pair.first.id);
        intent.putExtra("milestone_id", pair.second.id);
        // unique per milestone
        return PendingIntent.getActivity(
                this,
                (int) pair.second.id, // unique per milestone
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
}