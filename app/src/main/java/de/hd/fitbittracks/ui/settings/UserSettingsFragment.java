package de.hd.fitbittracks.ui.settings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;

import de.hd.fitbittracks.databinding.FragmentSettingsBinding;
import de.hd.fitbittracks.entities.UserSettings;
import de.hd.fitbittracks.enums.ResultStatus;
import de.hd.fitbittracks.repositories.UserSettingsRepository;
import de.hd.fitbittracks.ui.BaseFragment;
import de.hd.fitbittracks.ui.milestones.MilestoneFragment;

public class UserSettingsFragment extends BaseFragment {

    private FragmentSettingsBinding binding;
    private EditText lastFocusedEditText;

    private UserSettingsViewHolder holder;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable saveRunnable;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        UserSettingsViewModel userSettingsViewModel = new ViewModelProvider(this).get(UserSettingsViewModel.class);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        holder = new UserSettingsViewHolder(binding);
        View root = binding.getRoot();

        root.setOnClickListener(v -> {
            if (lastFocusedEditText != null) {
                lastFocusedEditText.clearFocus();
            }
            hideKeyboard(v);
        });

        holder.stepLengthInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                lastFocusedEditText = (EditText) v;
            }
        });

        holder.stepLengthInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(saveRunnable);
                saveRunnable = () -> {
                    String input = s.toString().trim();
                    if (input.isEmpty()) return;

                    try {
                        float length = Float.parseFloat(input);
                        if (length <= 0f) {
                            holder.stepLengthInput.setError("Step length must be greater than 0");
                            return;
                        } else {
                            holder.stepLengthInput.setError(null);
                            userSettingsViewModel.updateStepLength(length);
                        }
                    } catch (NumberFormatException e) {
                        holder.stepLengthInput.setError("Invalid step length");
                    }
                };
                handler.postDelayed(saveRunnable, 500); // debounce by 500ms
            }
        });

        holder.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> userSettingsViewModel.updateUseDarkMode(isChecked));
        holder.showCompletedTracksSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> userSettingsViewModel.updateShowCompletedTracks(isChecked));

        userSettingsViewModel.getSettings().observe(getViewLifecycleOwner(), settings -> {
            holder.bind(settings);
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class UserSettingsViewHolder extends RecyclerView.ViewHolder {

        EditText stepLengthInput;
        MaterialSwitch showCompletedTracksSwitch;
        MaterialSwitch darkModeSwitch;
        public UserSettingsViewHolder(@NonNull FragmentSettingsBinding binding) {
            super(binding.getRoot());
            stepLengthInput = binding.stepLengthInput;
            showCompletedTracksSwitch = binding.showCompletedTracksSwitch;
            darkModeSwitch = binding.darkModeSwitch;
        }

        public void bind(UserSettings settings) {
            if (settings == null) return;
            String newText = String.valueOf(settings.stepLengthInMeters);
            if (!stepLengthInput.getText().toString().equals(newText)) {
                stepLengthInput.setText(newText);
            }

            if (showCompletedTracksSwitch.isChecked() != settings.showCompletedTracks) {
                showCompletedTracksSwitch.setChecked(settings.showCompletedTracks);
            }

            if (darkModeSwitch.isChecked() != settings.useDarkMode) {
                darkModeSwitch.setChecked(settings.useDarkMode);
            }
        }
    }
}