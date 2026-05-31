package de.hd.stepwise.progresstracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.hd.stepwise.MainActivity;

public class MilestoneBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long milestoneId = intent.getLongExtra("milestone_id", -1);
        Log.i("MilestoneBroadcastReceiver", "Received broadcast for milestone ID: " + milestoneId);

        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.putExtra("navigate_to", "milestone_fragment");
        activityIntent.putExtra("milestone_id", milestoneId);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(activityIntent);
    }
}
