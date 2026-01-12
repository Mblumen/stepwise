package de.hd.stepwise.ui.layouthelper;

import androidx.recyclerview.widget.LinearSnapHelper;

public class LowSensitivitySnapHelper extends LinearSnapHelper {

    private static final int MIN_FLING_VELOCITY = 6000; // Increase this value to reduce sensitivity

    @Override
    public boolean onFling(int velocityX, int velocityY) {
        if (Math.abs(velocityX) < MIN_FLING_VELOCITY && Math.abs(velocityY) < MIN_FLING_VELOCITY) {
            return false; // Don't snap on weak flings
        }
        return super.onFling(velocityX, velocityY);
    }
}