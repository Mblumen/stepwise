package de.hd.fitbittracks.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import de.hd.fitbittracks.R;
import de.hd.fitbittracks.enums.ResultStatus;

public abstract class BaseFragment extends Fragment {

    protected ViewGroup container;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, android.os.Bundle savedInstanceState) {
        this.container = container;
        return null;
    }
    public void showCustomToast(Context context, String message, ResultStatus status) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, container, false);

        LinearLayout container = layout.findViewById(R.id.custom_toast_container);
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
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
