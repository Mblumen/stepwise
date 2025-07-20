package de.hd.fitbittracks.ui.tracksprogress;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.PolyOverlayWithIW;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.DetailsListItemSharedBinding;
import de.hd.fitbittracks.databinding.ItemProgressBinding;
import de.hd.fitbittracks.databinding.ListSeparatorBinding;
import de.hd.fitbittracks.entities.MilestoneWithTotalDistance;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.entities.UserProgress;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.enums.ListItemType;
import de.hd.fitbittracks.enums.ProgressStatus;
import de.hd.fitbittracks.interfaces.MapsItemClickedListener;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.pojos.Separator;
import de.hd.fitbittracks.pojos.UserProgressWithTrackAndMilestones;
import de.hd.fitbittracks.ui.BaseAdapter;
import de.hd.fitbittracks.ui.milestones.MilestoneListItemBaseAdapter;

public class TracksProgressAdapter extends BaseAdapter<ListItem, RecyclerView.ViewHolder> {
    private int expandedPosition = RecyclerView.NO_POSITION;
    private float lastDistanceWalked = Integer.MIN_VALUE;
    private Marker currentMarker = null;
    private MapView mapView = null;
    private boolean userTouchedMap = false;
    private final Context context;
    private final TracksProgressViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;
    private RecyclerView recyclerView;
    private final MapsItemClickedListener mapsItemClickedListener;
    private long progressId = RecyclerView.NO_POSITION;
    private final MilestoneListItemBaseAdapter.OnMilestoneClickListener onMilestoneClickListener;
    private final Consumer<ProgressStatus> toggleCallback;


    public TracksProgressAdapter(Context context, TracksProgressViewModel viewModel, LifecycleOwner liveCycleOwner, MapsItemClickedListener mapsItemClickedListener,
                                 MilestoneListItemBaseAdapter.OnMilestoneClickListener onMilestoneClickListener, Consumer<ProgressStatus> toggleCallback) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                return oldItem.getId() == newItem.getId() && oldItem.getType() == newItem.getType();
            }

            @Override
            public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                if (oldItem.getType() != newItem.getType()) return false;
                return oldItem.equals(newItem);
            }
        });
        this.context = context;
        this.viewModel = viewModel;
        this.lifecycleOwner = liveCycleOwner;
        this.mapsItemClickedListener = mapsItemClickedListener;
        this.onMilestoneClickListener = onMilestoneClickListener;
        this.toggleCallback = toggleCallback;
        viewModel.geoData.observe(lifecycleOwner, event -> {
            List<GeoPoint> gps = event.getContentIfNotHandled();
            Log.i("TracksProgressAdapter", "Position update received");
            if (gps != null) {
                drawPathOnMap(gps);
                //updatePositionOnMap(p);
            }
        });
        viewModel.pos.observe(lifecycleOwner, event -> {
            GeoPoint geoPoint = event.getContentIfNotHandled();
            Log.i("TracksProgressAdapter", "Position update received: " + geoPoint);
            if (geoPoint != null) {
                updatePositionOnMap(geoPoint);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        ListItem item = getItem(position);
        return item.getType().key;
    }

    public void setProgressId(long progressId) {
        this.progressId = progressId;
        if(progressId > 0) {
            for(int i = 0; i < getItemCount(); i++) {
                if(getItem(i).getId() == progressId) {
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ListItemType.SEPARATOR.key) {
            ListSeparatorBinding binding = ListSeparatorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ListSeparatorViewHolder(binding);
        }
        else {
            ItemProgressBinding binding = ItemProgressBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            TracksProgressViewHolder holder = new TracksProgressViewHolder(binding);
            LayoutTransition transition = new LayoutTransition();
            transition.setDuration(2000);
            ((ViewGroup) holder.itemView).setLayoutTransition(transition);
            return holder;
        }
    }

    private boolean checkEnoughDistanceWalked (float distanceWalked) {
        long now = System.currentTimeMillis();

        if (distanceWalked - lastDistanceWalked >= 8) {
            lastDistanceWalked = distanceWalked;
            return true;
        }
        return false;
    }

    private void loadPositionData(UserProgressWithTrackAndMilestones userProgressWithTrackAndMilestones) {
        //boolean enoughDistanceWalked = checkEnoughDistanceWalked(userProgressWithTrackAndMilestones.userProgress.distanceWalked);
        //if(!enoughDistanceWalked) return;
        viewModel.calculateAndPostPosition(userProgressWithTrackAndMilestones);
    }

    public void drawPathOnMap(List<GeoPoint> path) {
        if(mapView == null || path == null || path.isEmpty()) {
            Log.w("TracksProgressAdapter", "MapView or path is null/empty, cannot draw path.");
            return;
        }
        Polyline polyline = new Polyline();
        polyline.setPoints(path);
        polyline.getOutlinePaint().setColor(Color.BLUE);
        polyline.getOutlinePaint().setStrokeWidth(8);
        mapView.getOverlays().add(polyline);
        mapView.invalidate(); // Refresh the map
        mapView.getController().setCenter(path.get(0));
        mapView.invalidate();
    }

    private void updatePositionOnMap(GeoPoint geoPoint) {
        Log.i("TracksProgressAdapter", "Updating position on map: " + geoPoint);
        if (mapView == null || geoPoint == null) {
            Log.w("TracksProgressAdapter", "MapView or GeoPoint is null, cannot update position.");
            return;
        }

        if (currentMarker != null) {
            mapView.getOverlays().remove(currentMarker);
            mapView.invalidate(); // refresh the map
            currentMarker = null;
        }

        IMapController mapController = mapView.getController();
        Marker currentMarker = new Marker(mapView);
        currentMarker.setPosition(geoPoint);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        currentMarker.setIcon(ContextCompat.getDrawable(context, R.drawable.person_pin_circle));
        mapView.getOverlays().add(currentMarker);
        mapController.setCenter(geoPoint);
        mapView.invalidate();
    }

    private void initMap() {
        //GeoPoint calculatedPoint = new GeoPoint(trackRoute.startLat, trackRoute.startLon);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        // Berlin
        //mapController.setCenter(calculatedPoint);

/*        Marker currentMarker = new Marker(mapView);
        currentMarker.setPosition(calculatedPoint);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        currentMarker.setIcon(ContextCompat.getDrawable(context, R.drawable.person_pin_circle));
        mapView.getOverlays().add(currentMarker);*/

        //mapView.invalidate();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder genericHolder, int position) {
        if(getItemViewType(position) == ListItemType.SEPARATOR.key) {
            ListSeparatorViewHolder separatorHolder = (ListSeparatorViewHolder) genericHolder;
            Separator<?> separator = (Separator<?>) getItem(position);
            if(separator != null) {
                separatorHolder.bind(separator);
                ProgressStatus separatorType = (ProgressStatus) separator.data;
                separatorHolder.itemView.setOnClickListener(v -> toggleCallback.accept(separatorType));
                separatorHolder.separatorText.setCompoundDrawablesWithIntrinsicBounds(0,0, separator.isExpanded ? R.drawable.arrow_up : R.drawable.arrow_down, 0);
            }
            return;
        }

        if(progressId > 0 && getItem(position).getId() == progressId) {
            // Automatically expand the item if it matches the progressId
            progressId = -1;
            expandedPosition = genericHolder.getAbsoluteAdapterPosition();
        }

        TracksProgressViewHolder holder = (TracksProgressViewHolder) genericHolder;
        UserProgressWithTrackAndMilestones userProgressWithTrackAndMilestones = (UserProgressWithTrackAndMilestones) getItem(position);
        UserProgress userProgress = userProgressWithTrackAndMilestones.userProgress;
        Track track = userProgressWithTrackAndMilestones.trackWithMilestones.track;

        List<MilestoneWithTotalDistance> milestones = userProgressWithTrackAndMilestones.trackWithMilestones.milestones;
        boolean isExpanded = position == expandedPosition;
        holder.baseTitle.setText(track.name);
        holder.baseImageView.setImageResource(AppImage.getResIdFor(track.image));
        if(!milestones.isEmpty()) {
            int totalDistance = milestones.get(milestones.size() - 1).totalDistance;
            String formattedDistance = formatDistanceProgress(userProgress.distanceWalked, totalDistance);
            holder.baseDistance.setText(context.getString(R.string.label_distance, formattedDistance));
            holder.detailsDistance.setText(context.getString(R.string.label_distance, formattedDistance));
            String formattedSteps = formatSteps(userProgress.stepsWalked, (int) Math.floor((totalDistance - userProgress.distanceWalked)/ stepLength + 0.5f));
            holder.baseSteps.setText(context.getString(R.string.label_steps, formattedSteps));
            holder.detailsSteps.setText(context.getString(R.string.label_steps, formattedSteps));
        }

        int finishedMilestones = userProgressWithTrackAndMilestones.userProgressMilestoneStatus.size();
        String milestoneText = context.getString(R.string.label_milestones, finishedMilestones + "/" + milestones.size());
        holder.baseMilestoneCount.setText(milestoneText);
        holder.detailsMilestoneCount.setText(milestoneText);
        holder.detailsTitle.setText(track.name);
        holder.detailsImageView.setImageResource(AppImage.getResIdFor(track.image));
        holder.detailsStart.setText(context.getString(R.string.label_start,track.startLocation));
        holder.detailsEnd.setText(context.getString(R.string.label_end,track.endLocation));

        holder.baseLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        holder.expandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.baseLayout.setSelected(userProgress.status == ProgressStatus.ACTIVE);
        holder.itemSelectedAccent.setVisibility(userProgress.status == ProgressStatus.ACTIVE ? View.VISIBLE : View.GONE);

        if(isExpanded) {
            TracksProgressMilestoneListItemAdapter milestoneAdapter = new TracksProgressMilestoneListItemAdapter(context, mapsItemClickedListener, onMilestoneClickListener, stepLength);
            if (holder.milestoneRecycler.getItemDecorationCount() == 0) {
                holder.milestoneRecycler.addItemDecoration(new TracksProgressMilestoneListItemAdapter.DistanceTrackDecoration(context, holder.milestoneRecycler, milestoneAdapter));
            }
            holder.milestoneRecycler.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.milestoneRecycler.setAdapter(milestoneAdapter);

            if(track.trackRoute == null) holder.mapView.setVisibility(View.GONE);
            else {
                mapView = holder.mapView;
                holder.mapView.setVisibility(View.VISIBLE);
                initMap();
                loadPositionData(userProgressWithTrackAndMilestones);
            }

            viewModel.setProgressId(userProgress.id);
            viewModel.setTrack(track);
            viewModel.setDistanceWalked(userProgress.distanceWalked);
            viewModel.setStepsWalked(userProgress.stepsWalked);
            viewModel.getAllMilestones().observe(lifecycleOwner, milestoneAdapter::submitList);

            if(userProgress.status.equals(ProgressStatus.COMPLETED)) {
                holder.activeTime.setVisibility(View.VISIBLE);
                holder.pausedTime.setVisibility(View.VISIBLE);
                holder.totalTime.setVisibility(View.VISIBLE);
                Long totalDuration = userProgress.getTotalDuration();
                Long pausedDuration = userProgress.getPausedDuration();
                Long activeDuration = userProgress.getActiveDuration();

                if(totalDuration != null && totalDuration > 0) {
                    holder.totalTime.setText(context.getString(R.string.label_total_time,formatDuration(totalDuration)));
                }
                if(pausedDuration != null && pausedDuration > 0) {
                    holder.pausedTime.setText(context.getString(R.string.label_paused_time,formatDuration(pausedDuration)));
                } else {
                    holder.pausedTime.setVisibility(View.GONE);
                }
                if(activeDuration != null && activeDuration > 0) {
                    holder.activeTime.setText(context.getString(R.string.label_actual_time,formatDuration(activeDuration)));
                } else {
                    holder.activeTime.setVisibility(View.GONE);
                }
            } else {
                holder.activeTime.setVisibility(View.GONE);
                holder.pausedTime.setVisibility(View.GONE);
                holder.totalTime.setVisibility(View.GONE);
            }
            if(milestones.isEmpty()) {
                holder.actionButton.setVisibility(View.GONE);
            } else if(userProgress.distanceWalked >= milestones.get(milestones.size() - 1).totalDistance
                    && userProgress.status != ProgressStatus.COMPLETED) {
                holder.actionButton.setText(R.string.finish_progress);
                holder.actionButton.setOnClickListener(v -> {
                    viewModel.finishTrack(userProgress.id);
                });
            } else if(userProgress.status == ProgressStatus.ACTIVE) {
                holder.actionButton.setText(R.string.pause_progress);
                holder.actionButton.setOnClickListener(v -> {
                    viewModel.pauseTrackProgress(userProgress.id);
                });
            } else if(userProgress.status == ProgressStatus.PAUSED) {
                holder.actionButton.setText(R.string.resume_progress);
                holder.actionButton.setOnClickListener(v -> {
                    viewModel.resumeTrackProgress(userProgress.id);
                });
            } else {
                holder.actionButton.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPos = expandedPosition;
            expandedPosition = isExpanded ? -1 : position;
            int currentAdapterPosition = holder.getAbsoluteAdapterPosition();

            if(!isExpanded) {
                // Reset lastDistanceWalked when collapsing
                lastDistanceWalked = Integer.MIN_VALUE;
                userTouchedMap = false;
                currentMarker = null;
            }

            notifyItemChanged(oldPos);
            notifyItemChanged(position);

            if (!isExpanded && recyclerView != null) {
                recyclerView.post(() -> {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        layoutManager.scrollToPositionWithOffset(currentAdapterPosition, 0); // Align to top
                    }
                });
            }
        });
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public static class ListSeparatorViewHolder extends RecyclerView.ViewHolder {
        private final TextView separatorText;

        public ListSeparatorViewHolder(ListSeparatorBinding binding) {
            super(binding.getRoot());
            separatorText = binding.separatorText;
        }

        public void bind(Separator separator) {
            separatorText.setText(separator.title);
        }
    }
    public static class TracksProgressViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout baseLayout;
        private final ImageView baseImageView;
        private final TextView baseTitle;
        private final TextView baseSteps;
        private final TextView baseDistance;
        private final TextView baseMilestoneCount;
        private final View itemSelectedAccent;
        private final CardView expandedLayout;
        private final ImageView detailsImageView;
        private final TextView detailsTitle;
        private final TextView detailsStart;
        private final TextView detailsEnd;
        private final TextView detailsSteps;
        private final TextView detailsDistance;
        private final TextView detailsMilestoneCount;
        private final TextView totalTime;
        private final TextView pausedTime;
        private final TextView activeTime;
        private final RecyclerView milestoneRecycler;
        private final MaterialButton actionButton;
        private final MapView mapView;

        public TracksProgressViewHolder(ItemProgressBinding binding) {
            super(binding.getRoot());
            baseLayout = binding.progressItemBase;
            baseImageView = binding.progressItemBaseImage;
            baseTitle = binding.progressItemBaseTitle;
            baseSteps = binding.progressItemBaseSteps;
            baseDistance = binding.progressItemBaseDistance;
            baseMilestoneCount = binding.progressItemBaseMilestones;
            itemSelectedAccent = binding.itemSelectedAccent;

            expandedLayout = binding.progressItemDetails;
            DetailsListItemSharedBinding detailsSharedBinding = binding.sharedDetailsItem;
            detailsImageView = detailsSharedBinding.image;
            detailsTitle = detailsSharedBinding.title;
            detailsStart = detailsSharedBinding.start;
            detailsEnd = detailsSharedBinding.end;
            detailsSteps = detailsSharedBinding.steps;
            detailsDistance = detailsSharedBinding.distance;
            detailsMilestoneCount = detailsSharedBinding.milestones;
            totalTime = detailsSharedBinding.totalTime;
            pausedTime = detailsSharedBinding.pausedTime;
            activeTime = detailsSharedBinding.activeTime;

            milestoneRecycler = binding.progressDetailsMilestones;
            actionButton = binding.actionButton;

            mapView = binding.mapView;
        }
    }
}