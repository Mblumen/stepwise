package de.hd.fitbittracks.ui.achievements;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;

import java.util.Set;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.ListAchievementBinding;
import de.hd.fitbittracks.databinding.ListSeparatorBinding;
import de.hd.fitbittracks.entities.Achievement;
import de.hd.fitbittracks.enums.AchievementDifficulty;
import de.hd.fitbittracks.enums.AchievementType;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.enums.ListItemType;
import de.hd.fitbittracks.pojos.ListItem;
import de.hd.fitbittracks.pojos.Separator;
import de.hd.fitbittracks.ui.BaseAdapter;

public class AchievementAdapter extends BaseAdapter<ListItem, RecyclerView.ViewHolder> {

    private Context context;
    private Consumer<AchievementType> toggleCallback;

    public AchievementAdapter(Context context, Consumer<AchievementType> toggleCallback) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                if (oldItem.getType() != newItem.getType()) return false;
                return oldItem.equals(newItem);
            }
        });
        this.context = context;
        this.toggleCallback = toggleCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ListItemType.SEPARATOR.key) {
            ListSeparatorBinding binding = ListSeparatorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ListSeparatorViewHolder(binding);
        }
        ListAchievementBinding binding = ListAchievementBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AchievementViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder genericHolder, int position) {
        if(getItemViewType(position) == ListItemType.SEPARATOR.key) {
            ListSeparatorViewHolder separatorHolder = (ListSeparatorViewHolder) genericHolder;
            Separator<?> separator = (Separator<?>) getItem(position);
            if(separator != null) {
                separatorHolder.bind(separator);
                AchievementType separatorType = (AchievementType) separator.data;
                separatorHolder.itemView.setOnClickListener(v -> toggleCallback.accept(separatorType));
                separatorHolder.separatorText.setCompoundDrawablesWithIntrinsicBounds(0,0, separator.isExpanded ? R.drawable.arrow_up : R.drawable.arrow_down, 0);
                return;
            }
        }
        AchievementViewHolder holder = (AchievementViewHolder) genericHolder;
        Achievement achievement = (Achievement) getItem(position);
        holder.bind(achievement);
    }

    @Override
    public int getItemViewType(int position) {
        ListItem item = getItem(position);
        return item.getType().key;
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

    public static class AchievementViewHolder extends RecyclerView.ViewHolder {
        // Define views here
        private final Context context;
        public MaterialCardView card;
        public RelativeLayout achievementContainer;
        public ImageView icon;
        public TextView title;
        public TextView description;
        public ProgressBar progressBar;
        public ImageView lockIcon;
        public TextView medalText;


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
            this.medalText = binding.medalText;
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
                medalText.setVisibility(View.VISIBLE);

            } else {
                lockIcon.setVisibility(View.VISIBLE);
                progressBar.setProgress((int) (achievement.progressValue*100 / achievement.targetValue));
                progressBar.setVisibility(View.VISIBLE);
                achievementContainer.setAlpha(0.50f);
                medalText.setVisibility(View.GONE);
            }

            if (achievement.difficulty == AchievementDifficulty.BRONZE) {
                achievementContainer.setBackgroundResource(R.drawable.border_bronze);
                icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.bronze));
                progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.bronze));
                if(achievement.unlocked) medalText.setText("\uD83E\uDD49");
            } else if (achievement.difficulty == AchievementDifficulty.SILVER) {
                achievementContainer.setBackgroundResource(R.drawable.border_silver);
                icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.silver));
                progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.silver));
                if(achievement.unlocked) medalText.setText("\uD83E\uDD48");
            } else if (achievement.difficulty == AchievementDifficulty.GOLD) {
                achievementContainer.setBackgroundResource(R.drawable.border_gold);
                icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.gold));
                progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.gold));
                if(achievement.unlocked) medalText.setText("\uD83E\uDD47");
            }
        }
    }
}
