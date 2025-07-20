package de.hd.fitbittracks.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public abstract class BaseAdapter<T, H extends RecyclerView.ViewHolder> extends ListAdapter<T, H> {
    protected final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    protected final DecimalFormat df = new DecimalFormat("#,##0.0");
    protected final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
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
        return numberFormat.format(steps) + " (" + numberFormat.format(stepsRemaining) + " to go)";
    }

    protected String formatDuration(long durationMillis) {
        long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
        long totalHours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (totalHours >= 24) {
            long days = totalHours / 24;
            long hours = totalHours % 24;
            return days + "d " + hours + "h";
        } else {
            return (totalHours > 0 ? totalHours + "h " : "") + minutes + "m";
        }
    }

    protected String formatDate(long timestampMillis) {
        Date date = new Date(timestampMillis);
        return sdf.format(date);
    }

    public void setRecyclerView (RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

}
