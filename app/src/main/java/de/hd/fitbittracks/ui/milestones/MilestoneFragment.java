package de.hd.fitbittracks.ui.milestones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import de.hd.fitbittracks.databinding.FragmentTracksBinding;
import de.hd.fitbittracks.databinding.MilestoneBinding;
import de.hd.fitbittracks.entities.Milestone;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.interfaces.MapsItemClickedListener;
import de.hd.fitbittracks.pojos.MapsItem;
import de.hd.fitbittracks.pojos.MethodResult;
import de.hd.fitbittracks.ui.BaseFragment;
import de.hd.fitbittracks.ui.tracks.TracksFragment;
import de.hd.fitbittracks.ui.tracks.TracksViewModel;

public class MilestoneFragment extends BaseFragment {
    private MilestoneBinding binding;
    private MilestoneHolder holder;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        MilestoneViewModel viewModel = new ViewModelProvider(this).get(MilestoneViewModel.class);
        binding = MilestoneBinding.inflate(inflater, container, false);
        holder = new MilestoneHolder(binding);
        MilestoneFragmentArgs args = MilestoneFragmentArgs.fromBundle(getArguments());
        long milestoneId = args.getMilestoneId();
        viewModel.getMilestoneById(milestoneId).observe(getViewLifecycleOwner(), milestone -> {
                if (milestone != null) {
                    holder.bind(milestone, this);
                }
            });
        View root = binding.getRoot();
        return root;
    }

    private static class MilestoneHolder extends RecyclerView.ViewHolder {
        private final MilestoneBinding binding;

        public MilestoneHolder(MilestoneBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Milestone milestone, MapsItemClickedListener mapsItemClickedListener) {
            binding.title.setText(milestone.title);
            binding.stepCount.setText(String.valueOf(milestone.stepOffset));
            binding.description.setText(milestone.description);
            binding.image.setImageResource(AppImage.getResIdFor(milestone.image));
            if(!milestone.mapsUrl.isEmpty() || (milestone.latitude > 0 && milestone.longitude > 0)) {
                binding.buttonViewOnMap.setOnClickListener(v -> {
                    mapsItemClickedListener.onMapsItemClicked(new MapsItem(milestone.mapsUrl, milestone.latitude, milestone.longitude, milestone.title));
                });
            } else {
                binding.buttonViewOnMap.setVisibility(View.GONE);
            }
        }
    }
}
