package de.hd.stepwise.ui.milestones;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;
import java.util.List;

import de.hd.stepwise.R;
import de.hd.stepwise.databinding.ImageItemBinding;
import de.hd.stepwise.pojos.MilestoneImage;
import de.hd.stepwise.ui.BaseAdapter;

public class MilestoneImageAdapter extends BaseAdapter<MilestoneImage, MilestoneImageAdapter.ImageViewHolder> {

    private int focusedPosition = RecyclerView.NO_POSITION;
    private final MilestoneViewModel viewModel;
    private final long trackId;
    private final long milestoneId;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener listener;
    private OnExpandButtonClickListener expandButtonClickListener;
    protected MilestoneImageAdapter(MilestoneViewModel viewModel, long trackId, long milestoneId) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull MilestoneImage oldItem, @NonNull MilestoneImage newItem) {
                return oldItem.position == newItem.position;
            }

            @Override
            public boolean areContentsTheSame(@NonNull MilestoneImage oldItem, @NonNull MilestoneImage newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.viewModel = viewModel;
        this.trackId = trackId;
        this.milestoneId = milestoneId;
    }
    public void updateImageAtPosition(int position, MilestoneImage newImage) {
        this.getCurrentList().get(position).localImagePath = newImage.localImagePath;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageItemBinding binding = ImageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        int actualPosition = toRealPosition(position);
        boolean isFocused = position == focusedPosition;
        MilestoneImage milestoneImage = getItem(actualPosition);
        if (milestoneImage.localImagePath == null || !new File(milestoneImage.localImagePath).exists()) {
                viewModel.downloadMilestoneDetailImageIfNeeded(milestoneImage,trackId, milestoneId);
        }

        Object model;
        if (milestoneImage.localImagePath != null) {
            model = new File(milestoneImage.localImagePath);
        } else {
            model = milestoneImage.imageUrl;
        }
        Glide.with(holder.imageView)
                .load(model)
                .skipMemoryCache(true)
                .signature(new ObjectKey(milestoneImage.localImagePath != null ? milestoneImage.localImagePath : milestoneImage.imageUrl))
                .placeholder(R.drawable.avatar_1)
                .into(holder.imageView);
        holder.expandButton.setOnClickListener(v -> {
            if(expandButtonClickListener != null) {
                expandButtonClickListener.onExpandButtonClick(milestoneImage.localImagePath);
            }
        });

        holder.descriptionTextView.setText(milestoneImage.description);
        holder.itemView.setOnClickListener(v -> {
            if (recyclerView != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    //recyclerView.smoothScrollToPosition(pos);
                    if (listener != null) {
                        listener.onItemClick(pos);
                    }
                }
            }
        });
        holder.itemView.post(() -> {
           // boolean isFocused = layoutManager.isViewInFocus(holder.itemView);
            Log.i("MilestoneImageAdapter", "Position: " + position + ", Actual Position: " + actualPosition + ", Is Focused: " + isFocused);
            if (holder.descriptionTextView.getVisibility() == View.VISIBLE && !isFocused) {
                holder.descriptionTextView.setVisibility(View.GONE);
            } else if (holder.descriptionTextView.getVisibility() == View.GONE && isFocused) {
                holder.descriptionTextView.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public void setFocusedPosition(int position) {
        if (position < 0 || position == focusedPosition) {
            return; // Invalid position
        }
        int oldPosition = focusedPosition;
        focusedPosition = position;

        recyclerView.post(() -> {
            if (oldPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPosition);
            }
            notifyItemChanged(focusedPosition);
        });
    }

    public int toRealPosition(int adapterPosition) {
        List<?> currentList = getCurrentList();
        return currentList.isEmpty() ? 0 : adapterPosition % currentList.size();
    }

    @Override
    public void onViewRecycled(@NonNull ImageViewHolder holder) {
        super.onViewRecycled(holder);
        holder.descriptionTextView.setVisibility(View.GONE);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void setOnExpandButtonClickedListener(OnExpandButtonClickListener listener) {
        this.expandButtonClickListener = listener;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView descriptionTextView;
        CardView cardView;

        ImageButton expandButton;
        public ImageViewHolder(@NonNull ImageItemBinding binding) {
            super(binding.getRoot());
            imageView = binding.milestoneImage;
            descriptionTextView = binding.milestoneImageDescription;
            cardView = binding.milestoneImageCard;
            expandButton = binding.expandButton;
        }
    }
}