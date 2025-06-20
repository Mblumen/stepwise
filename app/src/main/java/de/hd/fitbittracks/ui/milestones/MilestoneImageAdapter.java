package de.hd.fitbittracks.ui.milestones;

import static androidx.recyclerview.widget.LinearSmoothScroller.SNAP_TO_START;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.databinding.ImageItemBinding;
import de.hd.fitbittracks.databinding.MilestoneWithStatusBinding;
import de.hd.fitbittracks.enums.AppImage;
import de.hd.fitbittracks.pojos.MilestoneImage;
import de.hd.fitbittracks.ui.BaseAdapter;
import de.hd.fitbittracks.ui.layouthelper.CarouselLayoutManager;

public class MilestoneImageAdapter extends BaseAdapter<MilestoneImage, MilestoneImageAdapter.ImageViewHolder> {

    private int focusedPosition = RecyclerView.NO_POSITION;
    private CarouselLayoutManager layoutManager; // Assuming you have a custom layout manager

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener listener;
    protected MilestoneImageAdapter() {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull MilestoneImage oldItem, @NonNull MilestoneImage newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull MilestoneImage oldItem, @NonNull MilestoneImage newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageItemBinding binding = ImageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        int actualPosition = toRealPosition(position);
        //int actualFocusedPosition = toRealPosition(focusedPosition);
        boolean isFocused = position == focusedPosition;
        MilestoneImage milestoneImage = getItem(actualPosition);
        holder.imageView.setImageResource(AppImage.getResIdFor(milestoneImage.imagePath));
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
        //Log.i("focusedPosition", "Position: " + position + ", Actual Position: " + actualPosition + ", Focused Position: " + focusedPosition + ", Actual Focused Position: " + actualFocusedPosition);
        holder.itemView.post(() -> {
           // boolean isFocused = layoutManager.isViewInFocus(holder.itemView);
            Log.i("MilestoneImageAdapter", "Position: " + position + ", Actual Position: " + actualPosition + ", Is Focused: " + isFocused);
            if (holder.descriptionTextView.getVisibility() == View.VISIBLE && !isFocused) {
                holder.descriptionTextView.setVisibility(View.GONE);
            } else if (holder.descriptionTextView.getVisibility() == View.GONE && isFocused) {
                holder.descriptionTextView.setVisibility(View.VISIBLE);
            }
            //if(isFocused) holder.cardView.animate().scaleY(1.05f).scaleX(1.05f).setDuration(300).start();
            //else holder.cardView.animate().scaleY(1f).scaleX(1f).setDuration(300).start();
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
        /*if (oldPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPosition);
        }
        notifyItemChanged(focusedPosition);*/
    }

    private int toRealPosition(int adapterPosition) {
        List<?> currentList = getCurrentList();
        return currentList.isEmpty() ? 0 : adapterPosition % currentList.size();
    }
    public void setLayoutManager(CarouselLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onViewRecycled(@NonNull ImageViewHolder holder) {
        super.onViewRecycled(holder);
        holder.descriptionTextView.setVisibility(View.GONE);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView descriptionTextView;
        CardView cardView;

        public ImageViewHolder(@NonNull ImageItemBinding binding) {
            super(binding.getRoot());
            imageView = binding.milestoneImage;
            descriptionTextView = binding.milestoneImageDescription;
            cardView = binding.milestoneImageCard;
        }
    }
}