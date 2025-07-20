package de.hd.fitbittracks.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import de.hd.fitbittracks.R;
import de.hd.fitbittracks.entities.Track;
import de.hd.fitbittracks.enums.ResultStatus;
import de.hd.fitbittracks.interfaces.MapsItemClickedListener;
import de.hd.fitbittracks.pojos.MapsItem;
import okhttp3.OkHttpClient;

@AndroidEntryPoint
public abstract class BaseFragment extends Fragment implements MapsItemClickedListener {


    protected ViewGroup container;
    protected Context context;
    protected NavController navController;

    public BaseFragment() {
    }
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, android.os.Bundle savedInstanceState) {
        this.container = container;
        this.context = requireContext();
        navController = NavHostFragment.findNavController(this);
        return null;
    }
    public void showCustomToast(String message, ResultStatus status) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, container, false);

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

    public void onMapsItemClicked(MapsItem mapsItem) {
        if(mapsItem.url.isEmpty() && (mapsItem.latitude <= 0 || mapsItem.longitude <= 0)) {
            showCustomToast("No valid location provided.", ResultStatus.ERROR);
            return;
        }
        Uri gmmIntentUri = Uri.parse(!mapsItem.url.isEmpty() ? mapsItem.url : "geo:" + mapsItem.latitude + "," + mapsItem.longitude + "?q=" + mapsItem.latitude + "," + mapsItem.longitude + "(" + Uri.encode(mapsItem.title) + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            showCustomToast("Google Maps app is not installed.", ResultStatus.ERROR);
        }
    }

    protected void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }
}
