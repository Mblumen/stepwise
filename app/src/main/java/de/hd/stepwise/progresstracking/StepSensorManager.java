package de.hd.stepwise.progresstracking;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import de.hd.stepwise.entities.StepEvent;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.repositories.StepEventRepository;

@Singleton
public class StepSensorManager implements SensorEventListener {

    private final Context context;
    private final StepEventRepository stepEventRepository;
    private SensorManager sensorManager;

    public static final String STEP_PREFS = "step_prefs";
    public static final String KEY_LAST_SENSOR_VALUE = "last_sensor_value";
    private boolean isRegistered = false;

    @Inject
    public StepSensorManager(@ApplicationContext Context context, StepEventRepository stepEventRepository) {
        this.context = context;
        this.stepEventRepository = stepEventRepository;
    }

    public void start() {
        if (isRegistered) return;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) return;

        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor == null) return;

        sensorManager.registerListener(
                this,
                stepSensor,
                SensorManager.SENSOR_DELAY_NORMAL
        );
        setBaseline(-1);
        isRegistered = true;
    }

    public void stop() {
        if (sensorManager != null && isRegistered) {
            sensorManager.unregisterListener(this);
        }
        isRegistered = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int totalSteps = (int) event.values[0];

        int baseline = getBaseline();

        if (baseline < 0) {
            // first run
            setBaseline(totalSteps);
            return;
        }

        if (totalSteps < baseline) {
            // sensor reset / reboot
            setBaseline(totalSteps);
            return;
        }

        int delta = totalSteps - baseline;

        if (delta > 0) {
            // push ONLY raw delta
            stepEventRepository.addStepEvent(new StepEvent(delta, StepSource.STEP_COUNTER, (new Date()).getTime()));
        }

        setBaseline(totalSteps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private int getBaseline() {
        return context.getSharedPreferences(STEP_PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_LAST_SENSOR_VALUE, -1);
    }

    private void setBaseline(int value) {
        context.getSharedPreferences(STEP_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_LAST_SENSOR_VALUE, value)
                .apply();
    }
}