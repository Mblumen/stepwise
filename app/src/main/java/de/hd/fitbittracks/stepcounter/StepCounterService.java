package de.hd.fitbittracks.stepcounter;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.Executors;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.enums.ResultStatus;
import de.hd.fitbittracks.pojos.MethodResult;
import de.hd.fitbittracks.repositories.UserProgressRepository;

public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int lastUpdate = -1;
    private UserProgressRepository userProgressRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
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
                MethodResult result = userProgressRepository.updateStepsWalked(stepsWalked);
                if(result != null && result.status == ResultStatus.SUCCESS) {
                    Log.d("StepCounterService", "Steps updated: " + stepsWalked);
                    sendGoalNotification(result.message);
                }
            });
            lastUpdate = totalSteps;
        }
    }

    private void sendGoalNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "step_channel")
                .setSmallIcon(R.drawable.steps)
                .setContentTitle("🎉 Goal Reached!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

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