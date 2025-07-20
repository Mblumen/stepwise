package de.hd.fitbittracks.ui.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.osmdroid.views.MapView;

public class AccessibleMapView extends MapView {
    public AccessibleMapView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        this.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    // Prevent parent from intercepting so map can pan/zoom
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    v.performClick();  // 🔔 Important: notify of “click”
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });
    }

    @Override
    public boolean performClick() {
        super.performClick();
        // any custom click behavior if needed
        return true;
    }


}
