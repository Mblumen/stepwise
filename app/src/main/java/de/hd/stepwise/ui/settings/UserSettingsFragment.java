package de.hd.stepwise.ui.settings;

import static de.hd.stepwise.ui.ToastHelper.showCustomToast;

import android.annotation.SuppressLint;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

import de.hd.stepwise.R;
import de.hd.stepwise.databinding.FragmentSettingsBinding;
import de.hd.stepwise.entities.UserSettings;
import de.hd.stepwise.enums.ResultStatus;
import de.hd.stepwise.enums.StepSource;
import de.hd.stepwise.helper.DataInitializer;
import de.hd.stepwise.pojos.MethodResult;
import de.hd.stepwise.pojos.DailyGoalState;
import de.hd.stepwise.ui.BaseFragment;
import de.hd.stepwise.ui.UpdateViewModel;

public class UserSettingsFragment extends BaseFragment {

    private static final String STATE_ACCOUNT_PICKER_FOR_DISCONNECT =
            "account_picker_for_disconnect";

    private FragmentSettingsBinding binding;
    private EditText lastFocusedEditText;
    private UserSettingsViewHolder holder;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable saveRunnable;
    private ActivityResultLauncher<IntentSenderRequest> googleHealthAuthLauncher;
    private ActivityResultLauncher<Intent> googleAccountPickerLauncher;
    private boolean accountPickerForDisconnect;
    private UserSettingsViewModel viewModel;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        accountPickerForDisconnect = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_ACCOUNT_PICKER_FOR_DISCONNECT);
        registerGoogleHealthAuthorizationLauncher();
        registerGoogleAccountPickerLauncher();
        viewModel = new ViewModelProvider(this).get(UserSettingsViewModel.class);
        viewModel.googleHealthAuthorizationResolution.observe(getViewLifecycleOwner(), event -> {
            android.app.PendingIntent pendingIntent = event.getContentIfNotHandled();
            if (pendingIntent != null) {
                googleHealthAuthLauncher.launch(new IntentSenderRequest.Builder(pendingIntent).build());
            }
        });
        UpdateViewModel updateViewModel = new ViewModelProvider(requireActivity()).get(UpdateViewModel.class);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        holder = new UserSettingsViewHolder(binding);
        View root = binding.getRoot();
        viewModel.fitbitLoginResult.observe(getViewLifecycleOwner(), event -> {
            MethodResult result = event.getContentIfNotHandled();
            if(result == null) return;
            showCustomToast(requireContext(), result.message, result.status, Toast.LENGTH_LONG);
            if(result.status == ResultStatus.SUCCESS) {
                if (viewModel.isAuthorized()) {
                    updateGoogleHealthStatus();
                    holder.fitbitActionButton.setText(R.string.disconnect);
                    holder.refreshTimeFitbit.setEnabled(true);
                    holder.refreshTimeFitbitInput.setEnabled(true);
                } else {
                    holder.fitbitStatusDesc.setText(R.string.not_connected);
                    holder.fitbitActionButton.setText(R.string.connect);
                    holder.refreshTimeFitbit.setEnabled(false);
                    holder.refreshTimeFitbitInput.setEnabled(false);
                }
            }
        });
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

        holder.refreshTimeFitbitInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                lastFocusedEditText = (EditText) v;
            }
        });

        holder.dailyStepGoalInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) lastFocusedEditText = (EditText) v;
        });

        UserSettingsViewHolder updateCheckHolder = holder;
        DataInitializer.checkUpdateAvailable(requireContext(), DataInitializer.DataType.TRACKS,
                updateAvailable -> handler.post(() -> {
                    if (binding == null || holder != updateCheckHolder) return;
                    updateCheckHolder.updateTracksButton.setEnabled(updateAvailable);
                    updateCheckHolder.updateTracksAvailable.setVisibility(
                            updateAvailable ? View.VISIBLE : View.GONE);
                }));
        DataInitializer.checkUpdateAvailable(requireContext(), DataInitializer.DataType.ACHIEVEMENTS,
                updateAvailable -> handler.post(() -> {
                    if (binding == null || holder != updateCheckHolder) return;
                    updateCheckHolder.updateAchievementsButton.setEnabled(updateAvailable);
                    updateCheckHolder.updateAchievementsAvailable.setVisibility(
                            updateAvailable ? View.VISIBLE : View.GONE);
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

        holder.refreshTimeFitbitInput.addTextChangedListener(new TextWatcher() {
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
                        int length = Integer.parseInt(input);
                        if (length < 2) {
                            holder.refreshTimeFitbitInput.setError("Refresh time must not be lower than 5 minutes");
                        } else {
                            holder.refreshTimeFitbitInput.setError(null);
                            viewModel.updateRefreshTimeInMinutesFitbit(length);
                        }
                    } catch (NumberFormatException e) {
                        holder.stepLengthInput.setError("Invalid refresh time");
                    }
                };
                handler.postDelayed(saveRunnable, 500); // debounce by 500ms
            }
        });

        holder.dailyStepGoalInput.addTextChangedListener(new TextWatcher() {
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
                        int steps = Integer.parseInt(input);
                        if (!viewModel.isValidDailyGoal(steps)) {
                            holder.dailyStepGoalInput.setError(
                                    getString(R.string.daily_step_goal_invalid));
                        } else {
                            holder.dailyStepGoalInput.setError(null);
                            viewModel.updateDailyGoal(steps);
                        }
                    } catch (NumberFormatException exception) {
                        holder.dailyStepGoalInput.setError(
                                getString(R.string.daily_step_goal_invalid));
                    }
                };
                handler.postDelayed(saveRunnable, 500);
            }
        });

        holder.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.updateUseDarkMode(isChecked));
        holder.showCompletedTracksSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.updateShowCompletedTracks(isChecked));
        holder.showLockedMilestonesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.updateShowLockedMilestones(isChecked));
        viewModel.getSettings().observe(getViewLifecycleOwner(), settings -> holder.bind(settings, requireContext()));
        viewModel.getDailyGoalState().observe(getViewLifecycleOwner(), holder::bindDailyGoal);
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

        try {
            if (viewModel.isAuthorized()) {
                updateGoogleHealthStatus();
                holder.fitbitActionButton.setText(R.string.disconnect);
                holder.refreshTimeFitbit.setEnabled(true);
                holder.refreshTimeFitbitInput.setEnabled(true);
            } else {
                holder.fitbitStatusDesc.setText(R.string.not_connected);
                holder.fitbitActionButton.setText(R.string.connect);
                holder.refreshTimeFitbit.setEnabled(false);
                holder.refreshTimeFitbitInput.setEnabled(false);
            }
            holder.fitbitActionButton.setOnClickListener(v -> {

                if (viewModel.isAuthorized()) {
                    String accountName = viewModel.getConnectedGoogleAccountName();
                    if (accountName != null && !accountName.isBlank()) {
                        viewModel.clearAuthorization(accountName);
                    } else {
                        launchGoogleAccountPicker(true);
                    }
                } else {
                    launchGoogleAccountPicker(false);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        holder.sensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                Log.d("UserSettingsFragment", "Selected sensor from spinner: " + selected);
                StepSource selectedStepSource = StepSource.getFromDisplayName(selected);
                if(selectedStepSource != null) {
                    if(selectedStepSource.equals(StepSource.FITBIT) && !viewModel.isAuthorized()) {
                        showCustomToast(requireContext(), "Please connect Google Health first", ResultStatus.ERROR, Toast.LENGTH_LONG);
                        // Revert to previous selection
                        UserSettings currentSettings = viewModel.getSettings().getValue();
                        if (currentSettings != null) {
                            int previousPosition = currentSettings.stepSource.key;
                            holder.sensorSpinner.setSelection(previousPosition);
                        }
                        return;
                    }
                    Log.d("UserSettingsFragment", "Selected sensor: " + selectedStepSource.name());
                    viewModel.updateSelectedSensor(selectedStepSource);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.setToday(LocalDate.now());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_ACCOUNT_PICKER_FOR_DISCONNECT,
                accountPickerForDisconnect);
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
        TextView fitbitStatusDesc;
        MaterialButton fitbitActionButton;
        Spinner sensorSpinner;
        TextView refreshTimeFitbit;
        EditText refreshTimeFitbitInput;
        EditText dailyStepGoalInput;
        TextView dailyStepGoalStatus;
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
            fitbitStatusDesc = binding.fitbitStatusDesc;
            fitbitActionButton = binding.fitbitActionButton;
            sensorSpinner = binding.sensorSpinner;
            refreshTimeFitbit = binding.refreshTimeFitbit;
            refreshTimeFitbitInput = binding.refreshTimeFitbitInput;
            dailyStepGoalInput = binding.dailyStepGoalInput;
            dailyStepGoalStatus = binding.dailyStepGoalStatus;
        }

        public void bind(UserSettings settings, Context context) {
            if (settings == null) return;
            String textStepLength = String.valueOf(settings.stepLengthInMeters);
            if (!stepLengthInput.getText().toString().equals(textStepLength)) {
                stepLengthInput.setText(textStepLength);
            }

            String textRefreshTimeFitbit = String.valueOf(settings.refreshTimeInMinutesFitbit);
            if (!refreshTimeFitbitInput.getText().toString().equals(textRefreshTimeFitbit)) {
                refreshTimeFitbitInput.setText(textRefreshTimeFitbit);
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
            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(context, R.array.sensor_array , android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sensorSpinner.setAdapter(spinnerAdapter);
            sensorSpinner.setSelection(settings.stepSource.key);
        }

        public void bindDailyGoal(DailyGoalState goalState) {
            if (goalState == null) return;
            int displayedGoal = goalState.pendingSteps == null
                    ? goalState.activeSteps : goalState.pendingSteps;
            String input = String.valueOf(displayedGoal);
            if (!dailyStepGoalInput.getText().toString().equals(input)) {
                dailyStepGoalInput.setText(input);
            }
            NumberFormat format = NumberFormat.getIntegerInstance(Locale.getDefault());
            if (goalState.pendingSteps == null) {
                dailyStepGoalStatus.setText(itemView.getContext().getString(
                        R.string.daily_step_goal_current, format.format(goalState.activeSteps)));
            } else {
                dailyStepGoalStatus.setText(itemView.getContext().getString(
                        R.string.daily_step_goal_pending,
                        format.format(goalState.activeSteps),
                        format.format(goalState.pendingSteps)));
            }
        }
    }

    private void registerGoogleHealthAuthorizationLauncher() {
        googleHealthAuthLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                Intent data = result.getData();
                if (result.getResultCode() != android.app.Activity.RESULT_OK || data == null) {
                    showCustomToast(requireContext(), "Google Health authorization cancelled",
                            ResultStatus.ERROR, Toast.LENGTH_LONG);
                    return;
                }
                viewModel.processGoogleHealthAuthorizationResult(data);
            }
        );
    }

    private void registerGoogleAccountPickerLauncher() {
        googleAccountPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (result.getResultCode() != android.app.Activity.RESULT_OK || data == null) {
                        return;
                    }
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName == null || accountName.isBlank()) {
                        showCustomToast(requireContext(),
                                getString(R.string.google_health_account_missing),
                                ResultStatus.ERROR, Toast.LENGTH_LONG);
                        return;
                    }
                    if (accountPickerForDisconnect) {
                        viewModel.clearAuthorization(accountName);
                    } else {
                        viewModel.connectGoogleHealth(accountName);
                    }
                });
    }

    private void launchGoogleAccountPicker(boolean forDisconnect) {
        accountPickerForDisconnect = forDisconnect;
        Intent accountPickerIntent = AccountManager.newChooseAccountIntent(
                null, null, new String[]{"com.google"},
                getString(forDisconnect
                        ? R.string.google_health_choose_account
                        : R.string.google_health_choose_account_to_connect),
                null, null, null);
        googleAccountPickerLauncher.launch(accountPickerIntent);
    }

    private void updateGoogleHealthStatus() {
        String accountName = viewModel.getConnectedGoogleAccountName();
        holder.fitbitStatusDesc.setText(accountName == null || accountName.isBlank()
                ? getString(R.string.connected)
                : getString(R.string.google_health_connected_as, accountName));
    }
}
