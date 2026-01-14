package de.hd.stepwise.ui;

import static de.hd.stepwise.ui.ToastHelper.showCustomToast;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import dagger.hilt.android.AndroidEntryPoint;
import de.hd.stepwise.enums.ResultStatus;
import de.hd.stepwise.interfaces.MapsItemClickedListener;
import de.hd.stepwise.pojos.MapsItem;
import de.hd.stepwise.ui.dialog.ImagePreviewDialogFragment;

@AndroidEntryPoint
public abstract class BaseFragment extends Fragment implements MapsItemClickedListener {

    public void onMapsItemClicked(MapsItem mapsItem) {
        if(mapsItem.url.isEmpty() && (mapsItem.latitude <= 0 || mapsItem.longitude <= 0)) {
            showCustomToast(requireContext(), "No valid location provided.", ResultStatus.ERROR, Toast.LENGTH_SHORT);
            return;
        }
        Uri gmmIntentUri = Uri.parse(!mapsItem.url.isEmpty() ? mapsItem.url : "geo:" + mapsItem.latitude + "," + mapsItem.longitude + "?q=" + mapsItem.latitude + "," + mapsItem.longitude + "(" + Uri.encode(mapsItem.title) + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            requireContext().startActivity(mapIntent);
        } else {
            showCustomToast(requireContext(), "Google Maps app is not installed.", ResultStatus.ERROR, Toast.LENGTH_SHORT);
        }
    }

    protected void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }

    protected void expandImage(String imagePath, String text) {
        if(imagePath == null) return;
        ImagePreviewDialogFragment dialog = ImagePreviewDialogFragment.newInstance(imagePath, text);
        dialog.show(getChildFragmentManager(), "image_preview");
    }
}
