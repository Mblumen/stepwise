package de.hd.stepwise.progresstracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import de.hd.stepwise.widget.JourneyWidgetUpdater;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            JourneyWidgetUpdater.requestUpdate(context);
            //ContextCompat.startForegroundService(context, new Intent(context, StepCounterService.class));
        }
    }
}
