package de.hd.stepwise.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

import de.hd.stepwise.R;

public class ImagePreviewDialogFragment extends DialogFragment {

    private static final String ARG_IMAGE_PATH = "image_path";
    private static final String ARG_TEXT = "image_text";

    public static ImagePreviewDialogFragment newInstance(String imagePath, String text) {
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);
        if(text != null && !text.isEmpty()) args.putString(ARG_TEXT, text);

        ImagePreviewDialogFragment fragment = new ImagePreviewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.dialog_image_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PhotoView photoView = view.findViewById(R.id.previewImage);
        TextView textView = view.findViewById(R.id.previewImageText);
        if(getArguments() != null) {
            String imagePath = getArguments().getString(ARG_IMAGE_PATH);
            String text = getArguments().getString(ARG_TEXT);
            if (imagePath == null) {
                dismiss();
                return;
            }
            // Load local image
            photoView.setImageURI(Uri.fromFile(new File(imagePath)));
            if(text != null && !text.isEmpty()) {
                textView.setText(text);
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }

            // Tap to close
            view.setOnClickListener(v -> dismiss());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make dialog fullscreen
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}