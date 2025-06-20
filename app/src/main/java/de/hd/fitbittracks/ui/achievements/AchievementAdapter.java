package de.hd.fitbittracks.ui.achievements;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.ImageItemBinding;
import de.hd.fitbittracks.databinding.ListAchievementBinding;
import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.enums.AchievementDifficulty;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.pojos.MilestoneImage;
import de.hd.fitbittracks.ui.BaseAdapter;
import de.hd.fitbittracks.ui.milestones.MilestoneImageAdapter;

public class AchievementAdapter extends BaseAdapter<Achievement, AchievementAdapter.AchievementViewHolder> {

    private Context context;

    public AchievementAdapter(Context context) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull Achievement oldItem, @NonNull Achievement newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Achievement oldItem, @NonNull Achievement newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.context = context;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListAchievementBinding binding = ListAchievementBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AchievementViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = getItem(position);
        holder.bind(achievement);
    }

    public static class AchievementViewHolder extends RecyclerView.ViewHolder {
        // Define views here
        private Context context;
        public MaterialCardView card;
        public RelativeLayout achievementContainer;
        public ImageView icon;
        public TextView title;
        public TextView description;
        public ProgressBar progressBar;
        public ImageView lockIcon;


        public AchievementViewHolder(ListAchievementBinding binding) {
            super(binding.getRoot());
            this.context = binding.getRoot().getContext();
            this.card = binding.achievementCard;
            this.achievementContainer = binding.achievementContainer;
            this.icon = binding.achievementIcon;
            this.title = binding.achievementTitle;
            this.description = binding.achievementDescription;
            this.progressBar = binding.achievementProgress;
            this.lockIcon = binding.lockIcon;
        }

        public void bind(Achievement achievement) {
            // Set the icon, title, description, and progress bar
            icon.setImageResource(AppImage.getResIdFor(achievement.icon));
            title.setText(achievement.title);
            description.setText(achievement.description);
            //progressBar.setProgress(achievement.progress);

            // Show or hide the lock icon based on achievement status
            if (achievement.unlocked) {
                lockIcon.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                achievementContainer.setAlpha(1.0f);

            } else {
                lockIcon.setVisibility(View.VISIBLE);
                progressBar.setProgress((int) (achievement.progressValue*100 / achievement.targetValue));
                progressBar.setVisibility(View.VISIBLE);
                achievementContainer.setAlpha(0.75f);
            }

            if (achievement.difficulty == AchievementDifficulty.BRONZE) {
                achievementContainer.setBackgroundResource(R.drawable.border_bronze);
                icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.bronze));
                progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.bronze));
            } else if (achievement.difficulty == AchievementDifficulty.SILVER) {
                achievementContainer.setBackgroundResource(R.drawable.border_silver);
                icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.silver));
                progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.silver));
            } else if (achievement.difficulty == AchievementDifficulty.GOLD) {
                achievementContainer.setBackgroundResource(R.drawable.border_gold);
                icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.gold));
                progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.gold));
            }
        }
    }
}
