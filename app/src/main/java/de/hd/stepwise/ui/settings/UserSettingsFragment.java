package de.hd.stepwise.ui.settings;

import static de.hd.stepwise.ui.ToastHelper.showCustomToast;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import de.hd.stepwise.databinding.FragmentSettingsBinding;
import de.hd.stepwise.entities.UserSettings;
import de.hd.stepwise.helper.DataInitializer;
import de.hd.stepwise.pojos.MethodResult;
import de.hd.stepwise.ui.BaseFragment;
import de.hd.stepwise.ui.UpdateViewModel;

public class UserSettingsFragment extends BaseFragment {

    private FragmentSettingsBinding binding;
    private EditText lastFocusedEditText;

    private UserSettingsViewHolder holder;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable saveRunnable;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        UserSettingsViewModel viewModel = new ViewModelProvider(this).get(UserSettingsViewModel.class);
        UpdateViewModel updateViewModel = new ViewModelProvider(requireActivity()).get(UpdateViewModel.class);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        holder = new UserSettingsViewHolder(binding);
        View root = binding.getRoot();
        updateViewModel.updateResult.observe(getViewLifecycleOwner(), event -> {
            MethodResult result = event.getContentIfNotHandled();
            if(result != null) showCustomToast(requireContext(), result.message, result.status, Toast.LENGTH_LONG);
        });
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

        DataInitializer.checkUpdateAvailable(requireContext(), DataInitializer.DataType.TRACKS, (updateAvailable) -> requireActivity().runOnUiThread(() ->{
            holder.updateTracksButton.setEnabled(updateAvailable);
            holder.updateTracksAvailable.setVisibility(updateAvailable ? View.VISIBLE : View.GONE);
        }));
        DataInitializer.checkUpdateAvailable(requireContext(), DataInitializer.DataType.ACHIEVEMENTS, (updateAvailable) ->  requireActivity().runOnUiThread(() -> {
            holder.updateAchievementsButton.setEnabled(updateAvailable);
            holder.updateAchievementsAvailable.setVisibility(updateAvailable ? View.VISIBLE : View.GONE);
        }));

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
                        } else {
                            holder.stepLengthInput.setError(null);
                            viewModel.updateStepLength(length);
                        }
                    } catch (NumberFormatException e) {
                        holder.stepLengthInput.setError("Invalid step length");
                    }
                };
                handler.postDelayed(saveRunnable, 500); // debounce by 500ms
            }
        });

        holder.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.updateUseDarkMode(isChecked));
        holder.showCompletedTracksSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.updateShowCompletedTracks(isChecked));
        holder.showLockedMilestonesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.updateShowLockedMilestones(isChecked));
        viewModel.getSettings().observe(getViewLifecycleOwner(), settings -> {
            holder.bind(settings);
        });
        holder.updateTracksButton.setOnClickListener(v -> {
            updateViewModel.update(getContext(), DataInitializer.DataType.TRACKS);
            holder.updateTracksButton.setEnabled(false);
            holder.updateTracksAvailable.setVisibility(View.GONE);
        });
        holder.updateAchievementsButton.setOnClickListener(v -> {
            updateViewModel.update(getContext(), DataInitializer.DataType.ACHIEVEMENTS);
            holder.updateAchievementsButton.setEnabled(false);
            holder.updateAchievementsAvailable.setVisibility(View.GONE);
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
        MaterialSwitch showLockedMilestonesSwitch;
        TextView updateTracksAvailable;
        TextView updateAchievementsAvailable;
        MaterialButton updateTracksButton;
        MaterialButton updateAchievementsButton;
        public UserSettingsViewHolder(@NonNull FragmentSettingsBinding binding) {
            super(binding.getRoot());
            stepLengthInput = binding.stepLengthInput;
            showCompletedTracksSwitch = binding.showCompletedTracksSwitch;
            darkModeSwitch = binding.darkModeSwitch;
            showLockedMilestonesSwitch = binding.showLockedMilestonesSwitch;
            updateTracksAvailable = binding.updateTracksAvailable;
            updateAchievementsAvailable = binding.updateAchievementsAvailable;
            updateTracksButton = binding.updateTracksButton;
            updateAchievementsButton = binding.updateAchievementsButton;
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

            if(showLockedMilestonesSwitch.isChecked() != settings.showLockedMilestones) {
                showLockedMilestonesSwitch.setChecked(settings.showLockedMilestones);
            }
        }
    }
}