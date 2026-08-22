package de.hd.stepwise.progresstracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.entities.UserProgress;
import de.hd.stepwise.pojos.events.StepUpdateResult;

@RunWith(AndroidJUnit4.class)
public class NotificationHandlerInstrumentedTest {

    private Context context;
    private NotificationManager notificationManager;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.cancelAll();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().getUiAutomation()
                    .grantRuntimePermission(context.getPackageName(), Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Test
    public void applicationCreatesChannelAndKeepsReachedMilestonesSeparate() {
        assertNotNull(notificationManager.getNotificationChannel("step_channel"));

        StepUpdateResult result = new StepUpdateResult();
        result.progress = new UserProgress();
        result.progress.id = 7;
        result.reachedMilestones.add(milestone(101, "First milestone"));
        result.reachedMilestones.add(milestone(102, "Second milestone"));

        new NotificationHandler(context).handleStepUpdate(result);

        int milestoneNotificationCount = 0;
        for (int attempt = 0; attempt < 20 && milestoneNotificationCount < 2; attempt++) {
            milestoneNotificationCount = countMilestoneNotifications();
            if (milestoneNotificationCount < 2) {
                SystemClock.sleep(50);
            }
        }
        assertEquals(2, milestoneNotificationCount);
    }

    private MilestoneWithTotalDistance milestone(long id, String title) {
        MilestoneWithTotalDistance milestone = new MilestoneWithTotalDistance();
        milestone.id = id;
        milestone.title = title;
        return milestone;
    }

    private int countMilestoneNotifications() {
        int count = 0;
        for (StatusBarNotification notification : notificationManager.getActiveNotifications()) {
            if (notification.getTag() != null && notification.getTag().startsWith("milestone-")) {
                count++;
            }
        }
        return count;
    }
}
