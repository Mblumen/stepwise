package de.hd.stepwise.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import de.hd.stepwise.R;
import de.hd.stepwise.enums.ResultStatus;

public class ToastHelper {
    public static void showCustomToast(Context context, String message, ResultStatus status, int duration) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null, false);

        TextView text = layout.findViewById(R.id.toast_text);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        text.setText(message);

        // Set colors and icons based on status
        int color;
        int iconRes = switch (status) {
            case SUCCESS -> {
                color = ContextCompat.getColor(context, R.color.success_green);
                yield R.drawable.check_circle;
            }
            case ERROR -> {
                color = ContextCompat.getColor(context, R.color.error_red);
                yield R.drawable.error;
            }
            case WARNING -> {
                color = ContextCompat.getColor(context, R.color.warning_yellow);
                yield R.drawable.warning;
            }
            default -> {
                color = Color.GRAY;
                yield R.drawable.error;
            }
        };

        icon.setColorFilter(color);
        icon.setImageResource(iconRes);

        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }
}
