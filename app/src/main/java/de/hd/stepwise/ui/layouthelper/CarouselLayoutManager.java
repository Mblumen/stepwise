package de.hd.stepwise.ui.layouthelper;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CarouselLayoutManager extends LinearLayoutManager {

    private final float shrinkAmount = 0.4f; // how much to shrink side items
    private final float shrinkDistance = 0.9f; // distance from center to start shrinking
    private RecyclerView recyclerView;

    public CarouselLayoutManager(Context context) {
        super(context, HORIZONTAL, false);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        scaleChildren();
    }

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position);
        scaleChildren();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrolled = super.scrollHorizontallyBy(dx, recycler, state);
        scaleChildren();
        return scrolled;
    }

    private void scaleChildren() {
        float midpoint = getWidth() / 2f;
        float d0 = 0f;
        float d1 = shrinkDistance * midpoint;
        float s0 = 1f;
        float s1 = 1f - shrinkAmount;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) continue;

            float childMidpoint = (getDecoratedLeft(child) + getDecoratedRight(child)) / 2f;
            float d = Math.min(d1, Math.abs(midpoint - childMidpoint));
            float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
            child.setScaleX(scale);
            child.setScaleY(scale);
        }
    }

    public boolean isViewInFocus(View view) {
        if (view == null || recyclerView == null) return false;

        int[] viewLocation = new int[2];
        int[] recyclerLocation = new int[2];

        view.getLocationOnScreen(viewLocation);
        recyclerView.getLocationOnScreen(recyclerLocation);

        int viewCenter = viewLocation[0] + view.getWidth() / 2;
        int recyclerCenter = recyclerLocation[0] + recyclerView.getWidth() / 2;

        Log.i("CarouselLayoutManager", "View center: " + viewCenter + ", Layout center: " + recyclerCenter);
        return Math.abs(viewCenter - recyclerCenter) < view.getWidth() / 2;
    }


    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        recyclerView = view;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }
}