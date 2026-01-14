package de.hd.stepwise.ui.milestones;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.vipulasri.timelineview.TimelineView;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.hd.stepwise.R;
import de.hd.stepwise.databinding.MilestoneWithStatusBinding;
import de.hd.stepwise.databinding.MilestoneWithStatusTimelineBinding;
import de.hd.stepwise.entities.MilestoneWithTotalDistance;
import de.hd.stepwise.interfaces.MapsItemClickedListener;
import de.hd.stepwise.pojos.MapsItem;
import de.hd.stepwise.ui.BaseAdapter;
import de.hd.stepwise.ui.tracks.BaseTracksViewModel;
import de.hd.stepwise.ui.tracks.TracksMilestoneListItemAdapter;
import de.hd.stepwise.ui.tracksprogress.TracksProgressMilestoneListItemAdapter;

public abstract class MilestoneListItemBaseAdapter<T extends MilestoneItem> extends BaseAdapter<T, MilestoneListItemBaseAdapter.MilestoneBaseViewHolder> {
    protected static final int TYPE_DEFAULT = 0;

    protected Context context;
    protected final MapsItemClickedListener mapsItemClickedListener;
    protected final BaseTracksViewModel trackViewModel;

    public interface OnMilestoneClickListener {
        void onItemClick(MilestoneWithTotalDistance milestone);
    }

    private final OnExpandButtonClickListener expandButtonClickListener;

    private final OnMilestoneClickListener listener;
    public MilestoneListItemBaseAdapter(Context context, @NonNull DiffUtil.ItemCallback<T> diffCallback, MapsItemClickedListener mapsItemClickedListener, BaseTracksViewModel trackViewModel, OnMilestoneClickListener listener, OnExpandButtonClickListener expandButtonClickListener, float stepLength) {
        super(diffCallback);
        this.mapsItemClickedListener = mapsItemClickedListener;
        this.trackViewModel = trackViewModel;
        this.listener = listener;
        this.stepLength = stepLength;
        this.context = context;
        this.expandButtonClickListener = expandButtonClickListener;
    }

    public void openMap(MapsItem mapsItem) {
        mapsItemClickedListener.onMapsItemClicked(mapsItem);
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_DEFAULT; // override in subclass if needed
    }

    @NonNull
    @Override
    public MilestoneBaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MilestoneWithStatusTimelineBinding binding = MilestoneWithStatusTimelineBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MilestoneBaseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MilestoneBaseViewHolder holder, int position) {
        T milestoneItem = getItem(position);
        MilestoneWithTotalDistance milestone = milestoneItem.getMilestone();
        holder.title.setText(milestone.title);
        //holder.description.setText(milestone.description);
        //holder.milestoneImage.setImageResource(AppImage.getResIdFor(milestone.imageUrl));
        //TODO: Adjust image url
        if (milestone.localImagePath == null || !new File(milestone.localImagePath).exists()) {
            trackViewModel.downloadMilestoneImageIfNeeded(milestone);
        }

        Object model;
        if (milestone.localImagePath != null) {
            model = new File(milestone.localImagePath);
        } else {
            model = milestone.imageUrl;
        }
        Glide.with(holder.milestoneImage)
                .load(model)
                .placeholder(R.drawable.avatar_1)
                .into(holder.milestoneImage);

        if((milestone.mapsUrl != null && !milestone.mapsUrl.isEmpty()) || (milestone.latitude > 0 && milestone.longitude > 0)) {
            holder.mapsButton.setOnClickListener(v -> openMap(new MapsItem(milestone.mapsUrl, milestone.latitude, milestone.longitude, milestone.title)));
        } else {
            holder.mapsButton.setVisibility(View.GONE);
        }
        holder.milestoneImage.setOnClickListener(v -> {
            if(expandButtonClickListener != null) {
                expandButtonClickListener.onExpandButtonClick(milestoneItem.getMilestone().localImagePath);
            }
        });
        onBindExtendedViewHolder(holder, milestoneItem);
    }

    protected void addOpenMilestoneClickListener(MilestoneBaseViewHolder holder, MilestoneWithTotalDistance milestone) {
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onItemClick(milestone));
        }
    }

    protected abstract<E extends MilestoneBaseViewHolder> void onBindExtendedViewHolder(E holder, T item);

    public static class MilestoneBaseViewHolder extends RecyclerView.ViewHolder {
        public final TimelineView timeline;
        public final MilestoneWithStatusBinding milestone;
        public final TextView title;
        public final TextView steps;
        public final TextView distance;
        //public TextView description;
        public final ImageView milestoneImage;
        public final ImageButton mapsButton; // If you want to add an image button for actions
        public final LinearLayout lockedContainer;
        public final TextView milestoneLocked;
        public final LinearLayout milestoneUnlocked;
        public final TextView distanceText;

        public MilestoneBaseViewHolder(MilestoneWithStatusTimelineBinding binding) {
            super(binding.getRoot());
            timeline = binding.timeline;
            milestone = binding.milestone;
            milestoneImage = milestone.milestoneImage;
            title = milestone.milestoneTitle;
            steps = milestone.milestoneSteps;
            distance = milestone.milestoneDistance;
            //description = milestone.milestoneDescription;
            mapsButton = milestone.milestoneMapButton;
            lockedContainer = milestone.lockedContainer;
            milestoneLocked = milestone.milestoneLocked;
            milestoneUnlocked = milestone.milestoneUnlocked;
            distanceText = binding.distanceText;
        }
    }

    public static class DistanceTrackDecoration extends RecyclerView.ItemDecoration {
        private final Paint linePaint;
        private final Paint textPaint;
        private final int space; // vertical spacing in px
        private final RecyclerView.Adapter<?> adapter;

        public DistanceTrackDecoration(Context ctx, RecyclerView.Adapter<?> adapter) {
            this.adapter = adapter;
            this.space = ctx.getResources().getDimensionPixelSize(R.dimen.track_connection_height);
            linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(ContextCompat.getColor(ctx, R.color.dark_gray));
            linePaint.setStrokeWidth(
                    ctx.getResources().getDimension(R.dimen.line_width));

            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(ContextCompat.getColor(ctx, R.color.dark_gray));
            textPaint.setTextSize(
                    ctx.getResources().getDimension(R.dimen.font_size_small));

        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int pos = parent.getChildAdapterPosition(view);
            if (pos < adapter.getItemCount() - 1) {
                outRect.bottom = space;
            }
        }

        @Override
        public void onDrawOver(@NonNull Canvas canvas, RecyclerView parent, @NonNull RecyclerView.State state) {
            int count = parent.getChildCount();
            int parentWidth = parent.getWidth();
            float lineX = parentWidth / 2f;  // center X
            for (int i = 0; i < count - 1; i++) {
                View top = parent.getChildAt(i);
                View bottom = parent.getChildAt(i + 1);
                int posTop = parent.getChildAdapterPosition(top);
                if (posTop == RecyclerView.NO_POSITION) continue;
                List<MilestoneWithTotalDistance> items;
                // Retrieve milestone list and compute
                if (adapter instanceof TracksProgressMilestoneListItemAdapter tracksProgressMilestoneListItemAdapter)
                    items = tracksProgressMilestoneListItemAdapter.getCurrentList().stream().map(list -> list.milestone).collect(Collectors.toList());
                else if (adapter instanceof TracksMilestoneListItemAdapter tracksMilestoneListItemAdapter)
                    items = tracksMilestoneListItemAdapter.getCurrentList();
                else return;
                if (posTop + 1 >= items.size()) continue;

                float distance = computeDistanceBetween(items.get(posTop), items.get(posTop + 1));
                String text = String.format(Locale.getDefault(), "%.1f km", distance);

                float startY = top.getBottom();
                float endY = bottom.getTop();
                float midY = (startY + endY) / 2;

                // Draw top half line
                canvas.drawLine(lineX, startY, lineX, midY - textPaint.getTextSize(), linePaint);

                // Draw text
                float textWidth = textPaint.measureText(text);
                canvas.drawText(text, lineX - textWidth / 2, midY + textPaint.getTextSize() / 2, textPaint);

                // Draw bottom half line
                canvas.drawLine(lineX, midY + textPaint.getTextSize(), lineX, endY, linePaint);
            }
        }

        // Provide a logic to compute distance between two milestones
        private float computeDistanceBetween(Object a, Object b) {
            if (a instanceof MilestoneWithTotalDistance ma && b instanceof MilestoneWithTotalDistance mb) {
                float da = ma.totalDistance;
                float db = mb.totalDistance;
                return (db - da) / 1000f;
            }
            return 0f;
        }
    }
}