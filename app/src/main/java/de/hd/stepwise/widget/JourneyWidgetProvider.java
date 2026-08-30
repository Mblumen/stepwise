package de.hd.stepwise.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class JourneyWidgetProvider extends AppWidgetProvider {
    @Inject JourneyWidgetUpdater updater;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updater.updateAll();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (JourneyWidgetUpdater.ACTION_REFRESH.equals(intent.getAction())) updater.updateAll();
    }
}
