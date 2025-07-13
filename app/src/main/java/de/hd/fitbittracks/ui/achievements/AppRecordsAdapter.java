package de.hd.fitbittracks.ui.achievements;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import de.hd.fitbittracks.databinding.RecordItemBinding;
import de.hd.fitbittracks.entities.AppRecord;
import de.hd.fitbittracks.ui.BaseAdapter;

public class AppRecordsAdapter extends BaseAdapter<AppRecord, AppRecordsAdapter.AppRecordViewHolder> {


    public AppRecordsAdapter() {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull AppRecord oldItem, @NonNull AppRecord newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull AppRecord oldItem, @NonNull AppRecord newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public AppRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecordItemBinding binding = RecordItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppRecordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppRecordViewHolder holder, int position) {
        AppRecord record = getItem(position);
        holder.bind(record);
    }
    public static class AppRecordViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView front;
        private final MaterialCardView back;
        private final TextView name;
        private final TextView value;
        private final TextView timestamp;
        private final TextView description;
        private final ImageView recordIcon;
        private final ImageView flipIcon;

        private boolean isFlipped = false;

        public AppRecordViewHolder(RecordItemBinding binding) {
            super(binding.getRoot());
            name = binding.recordName;
            value = binding.recordValue;
            timestamp = binding.recordTimestamp;
            description = binding.recordDescription;
            recordIcon = binding.recordIcon;
            flipIcon = binding.flipHint;

            front = binding.cardFront;
            back = binding.cardBack;
        }

        public void bind(AppRecord appRecord) {
            name.setText(appRecord.name);
            recordIcon.setImageResource(appRecord.type.iconResId);
            if(appRecord.value > 0f) {
                value.setText(appRecord.getValueWithUnit());
                value.setVisibility(View.VISIBLE);
            }
            else value.setVisibility(View.GONE);
            timestamp.setText(appRecord.getFormattedDate());
            description.setText(appRecord.description);
            front.setVisibility(View.VISIBLE);
            back.setVisibility(View.GONE);
            isFlipped = false;

            itemView.setOnClickListener(v -> {
                flipCard();
            });
        }

        private void flipCard() {
            Log.i("AppRecordsAdapter", "Flipping card: " + isFlipped);
            View visible = isFlipped ? back : front;
            View hidden = isFlipped ? front : back;
            flipIcon.setVisibility(View.GONE);
            visible.animate()
                    .rotationY(90)
                    .setDuration(150)
                    .withEndAction(() -> {
                        visible.setVisibility(View.GONE);
                        hidden.setRotationY(-90);
                        hidden.setVisibility(View.VISIBLE);
                        hidden.animate().rotationY(0).setDuration(150).withEndAction(() -> flipIcon.setVisibility(View.VISIBLE)).start();
                    })
                    .start();
            isFlipped = !isFlipped;
        }
    }
}