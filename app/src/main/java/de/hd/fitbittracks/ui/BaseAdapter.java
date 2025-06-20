package de.hd.fitbittracks.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public abstract class BaseAdapter<T, H extends RecyclerView.ViewHolder> extends ListAdapter<T, H> {
    protected final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    protected final DecimalFormat df = new DecimalFormat("#,##0.0");
    protected float stepLength = 1f;
    protected RecyclerView recyclerView;
    protected BaseAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public void setStepLength(float stepLength) {
        this.stepLength = stepLength;
        notifyDataSetChanged();
    }

    protected String formatDistance(double value) {
        return value >= 10000 ? df.format(value/1000) + " km" : numberFormat.format(value) + " m";
    }

    protected String formatDistanceProgress(double value, double total) {
        return total >= 10000 ? df.format(value/1000) + "/" + df.format(total/1000) + " km" : numberFormat.format(value) + "/" + numberFormat.format(total) + " m";
    }

    protected String formatNumber(int value) {
        return numberFormat.format(value);
    }

    protected String formatSteps(int steps, int stepsRemaining) {
        return numberFormat.format(steps) + " (" + numberFormat.format(stepsRemaining) + ")";
    }

    public void setRecyclerView (RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

}
