package de.hd.stepwise.ui.passport;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hd.stepwise.R;
import de.hd.stepwise.databinding.FragmentJourneyPassportBinding;
import de.hd.stepwise.pojos.JourneyPassport;
import de.hd.stepwise.pojos.JourneyReachedMilestone;
import de.hd.stepwise.ui.BaseFragment;

public class JourneyPassportFragment extends BaseFragment {
    private FragmentJourneyPassportBinding binding;
    private JourneyPassport passport;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentJourneyPassportBinding.inflate(inflater, container, false);
        JourneyPassportViewModel viewModel = new ViewModelProvider(this)
                .get(JourneyPassportViewModel.class);
        long progressId = JourneyPassportFragmentArgs.fromBundle(requireArguments()).getProgressId();
        binding.passportContent.setVisibility(View.GONE);
        binding.passportError.setVisibility(View.VISIBLE);
        viewModel.passport(progressId).observe(getViewLifecycleOwner(), value -> {
            passport = value;
            if (value == null) {
                binding.passportContent.setVisibility(View.GONE);
                binding.passportError.setVisibility(View.VISIBLE);
                return;
            }
            binding.passportError.setVisibility(View.GONE);
            binding.passportContent.setVisibility(View.VISIBLE);
            bind(value);
            viewModel.loadRoute(value);
        });
        viewModel.route().observe(getViewLifecycleOwner(), this::drawRoute);
        viewModel.shareUri().observe(getViewLifecycleOwner(), event -> {
            var uri = event.getContentIfNotHandled();
            if (uri == null) return;
            Intent intent = JourneyShareCardGenerator.createShareIntent(uri);
            startActivity(Intent.createChooser(intent, getString(R.string.share_passport)));
            binding.sharePassport.setEnabled(true);
        });
        viewModel.shareFailed().observe(getViewLifecycleOwner(), event -> {
            Boolean failed = event.getContentIfNotHandled();
            if (Boolean.TRUE.equals(failed)) {
                binding.shareStatus.setText(R.string.passport_share_failed);
                binding.sharePassport.setEnabled(true);
            }
        });
        binding.sharePassport.setOnClickListener(view -> {
            if (passport == null) return;
            binding.sharePassport.setEnabled(false);
            binding.shareStatus.setText(R.string.passport_share_preparing);
            viewModel.share(passport);
        });
        return binding.getRoot();
    }

    private void bind(JourneyPassport value) {
        binding.passportTitle.setText(value.displayTrackName(getString(R.string.passport_unknown_name)));
        binding.passportRouteName.setText(getString(R.string.passport_route,
                value.displayStart(getString(R.string.passport_unknown_start)),
                value.displayDestination(getString(R.string.passport_unknown_destination))));
        binding.passportDates.setText(getString(R.string.passport_dates,
                formatDate(value.startedAt), formatDate(value.completedAt)));
        binding.passportStats.setText(getString(R.string.passport_stats,
                value.stepsWalked, value.distanceWalked / 1000f,
                formatDuration(value.totalDuration), formatDuration(value.activeDuration)));
        Object image = value.track.localImagePath == null ? value.track.imageUrl
                : new File(value.track.localImagePath);
        Glide.with(binding.passportTrackImage).load(image)
                .placeholder(R.drawable.avatar_1).into(binding.passportTrackImage);
        binding.passportMilestones.removeAllViews();
        binding.passportStamps.removeAllViews();
        for (JourneyReachedMilestone item : value.reachedMilestones) {
            TextView milestone = new TextView(requireContext());
            milestone.setPadding(0, 8, 0, 8);
            milestone.setText(getString(R.string.passport_milestone,
                    formatDate(item.reached.reachedAt), item.milestone.title));
            binding.passportMilestones.addView(milestone);
            if (item.milestone.localStampImagePath != null) {
                ImageView stamp = new ImageView(requireContext());
                stamp.setLayoutParams(new ViewGroup.LayoutParams(160, 160));
                stamp.setContentDescription(getString(R.string.passport_stamp,
                        item.milestone.title));
                Glide.with(stamp).load(new File(item.milestone.localStampImagePath))
                        .placeholder(R.drawable.avatar_1).error(R.drawable.avatar_1).into(stamp);
                binding.passportStamps.addView(stamp);
            }
        }
        binding.noStamps.setVisibility(binding.passportStamps.getChildCount() == 0
                ? View.VISIBLE : View.GONE);
    }

    private void drawRoute(List<GeoPoint> points) {
        if (points == null || points.isEmpty()) {
            binding.passportMap.setVisibility(View.GONE);
            binding.routeUnavailable.setVisibility(View.VISIBLE);
            return;
        }
        binding.routeUnavailable.setVisibility(View.GONE);
        binding.passportMap.setVisibility(View.VISIBLE);
        binding.passportMap.setTileSource(TileSourceFactory.MAPNIK);
        binding.passportMap.setMultiTouchControls(true);
        Polyline route = new Polyline();
        route.setPoints(points);
        binding.passportMap.getOverlays().clear();
        binding.passportMap.getOverlays().add(route);
        IMapController controller = binding.passportMap.getController();
        controller.setZoom(10.0);
        controller.setCenter(points.get(0));
    }

    private String formatDate(Long value) {
        return value == null || value <= 0 ? getString(R.string.passport_date_unknown)
                : DateFormat.getDateInstance().format(new Date(value));
    }

    private String formatDuration(Long millis) {
        if (millis == null) return getString(R.string.passport_value_unknown);
        long minutes = millis / 60_000;
        return String.format(Locale.getDefault(), "%dh %02dm", minutes / 60, minutes % 60);
    }

    @Override public void onResume() { super.onResume(); if (binding != null) binding.passportMap.onResume(); }
    @Override public void onPause() { if (binding != null) binding.passportMap.onPause(); super.onPause(); }
    @Override public void onDestroyView() { binding = null; passport = null; super.onDestroyView(); }
}
