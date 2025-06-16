package de.hd.fitbittracks.ui.layouthelper;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OverlapDecoration extends RecyclerView.ItemDecoration {
    private final int overlapWidth;

    public OverlapDecoration(int overlapWidth) {
        this.overlapWidth = overlapWidth;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
                               @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position != 0) {
            outRect.left = -overlapWidth; // Overlap
        }
    }
}