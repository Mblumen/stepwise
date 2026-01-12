package de.hd.stepwise.ui.layouthelper;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class CenterSnapHelper extends LinearSnapHelper {

    private final int overlapOffset;

    public CenterSnapHelper(int overlapOffset) {
        this.overlapOffset = overlapOffset;
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                              @NonNull View targetView) {
        if (!(layoutManager instanceof LinearLayoutManager)) {
            return super.calculateDistanceToFinalSnap(layoutManager, targetView);
        }

        int[] out = new int[2];

        int childCenter = (targetView.getLeft() + targetView.getRight()) / 2;
        int containerCenter = layoutManager.getWidth() / 2;

        out[0] = childCenter - containerCenter + (overlapOffset / 2); // adjust for overlap
        out[1] = 0;

        return out;
    }
}