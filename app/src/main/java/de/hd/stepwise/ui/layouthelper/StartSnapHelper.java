package de.hd.stepwise.ui.layouthelper;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class StartSnapHelper extends LinearSnapHelper {
    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        return super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
    }

    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (!(layoutManager instanceof LinearLayoutManager lm)) {
            return null;
        }
        int firstVisible = lm.findFirstVisibleItemPosition();
        return lm.findViewByPosition(firstVisible);
    }
}