package de.hd.fitbittracks;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.hd.fitbittracks.database.AppDatabase;
import de.hd.fitbittracks.databinding.ActivityMainBinding;
import de.hd.fitbittracks.stepcounter.StepCounterService;
import de.hd.fitbittracks.ui.MainSharedViewModel;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private BottomNavigationView bottomNavigationView;

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.FOREGROUND_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
        startStepService();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                startStepService();
            } else {
                Toast.makeText(this, "Permissions required for step tracking", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startStepService() {
        NotificationChannel channel = new NotificationChannel("step_channel", "Step Tracker", NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
        Intent intent = new Intent(this, StepCounterService.class);
        ContextCompat.startForegroundService(this, intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        deleteDatabase("fitbit_tracks_db");
        AppDatabase db = AppDatabase.getInstance(this);
        db.userSettingsDao().getSettingsLive().observe(this, userSettings -> {
            if(userSettings != null) {
                AppCompatDelegate.setDefaultNightMode(userSettings.useDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            }
        });

        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        bottomNavigationView = getBottomNavigationView(binding, navController);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_tracks_progress, R.id.nav_tracks, R.id.nav_settings, R.id.nav_achievements)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        //NavigationUI.setupWithNavController(bottomNavigationView, navController);

        checkAndRequestPermissions();
        // Handle the intent and navigate to a specific fragment
        handleNavigationIntent(getIntent());
    }

    @NonNull
    private static BottomNavigationView getBottomNavigationView(ActivityMainBinding binding, NavController navController) {
        BottomNavigationView bottomNavigationView = binding.appBarMain.contentMain.bottomNavView;
        bottomNavigationView.setOnItemReselectedListener(item -> {
            if(item.getItemId() == R.id.nav_tracks_progress) {
                navController.popBackStack(R.id.nav_tracks_progress, false);
                bottomNavigationView.getMenu().findItem(R.id.nav_tracks_progress).setChecked(true);
            }
            if(item.getItemId() == R.id.nav_tracks) {
                navController.popBackStack(R.id.nav_tracks, false);
                bottomNavigationView.getMenu().findItem(R.id.nav_tracks).setChecked(true);
            }
            if(item.getItemId() == R.id.nav_settings) {
                navController.popBackStack(R.id.nav_settings, false);
                bottomNavigationView.getMenu().findItem(R.id.nav_settings).setChecked(true);
            }
            if(item.getItemId() == R.id.nav_achievements) {
                navController.popBackStack(R.id.nav_achievements, false);
                bottomNavigationView.getMenu().findItem(R.id.nav_achievements).setChecked(true);
            }
        });
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_tracks_progress) {
                navController.navigate(R.id.nav_tracks_progress, null,
                        new NavOptions.Builder()
                                .setLaunchSingleTop(true)
                                .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                                .build());
                bottomNavigationView.getMenu().findItem(R.id.nav_tracks_progress).setChecked(true);
                return true;
            }
            if(item.getItemId() ==  R.id.nav_tracks) {
                navController.navigate(R.id.nav_tracks, null,
                        new NavOptions.Builder()
                                .setLaunchSingleTop(true)
                                .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                                .build());
                bottomNavigationView.getMenu().findItem(R.id.nav_tracks).setChecked(true);
                return true;
            }
            if(item.getItemId() ==  R.id.nav_settings) {
                navController.navigate(R.id.nav_settings, null,
                        new NavOptions.Builder()
                                .setLaunchSingleTop(true)
                                .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                                .build());
                bottomNavigationView.getMenu().findItem(R.id.nav_settings).setChecked(true);
                return true;
            }
            if(item.getItemId() ==  R.id.nav_achievements) {
                navController.navigate(R.id.nav_achievements, null,
                        new NavOptions.Builder()
                                .setLaunchSingleTop(true)
                                .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                                .build());
                bottomNavigationView.getMenu().findItem(R.id.nav_achievements).setChecked(true);
                return true;
            }
            return false;
        });
        return bottomNavigationView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        // Using findViewById because NavigationView exists in different layout files
        // between w600dp and w1240dp
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_settings);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // important if you want getIntent() to return latest
        handleNavigationIntent(intent);
    }

    private void handleNavigationIntent(Intent intent) {
        if (intent == null) return;

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        MainSharedViewModel sharedViewModel = new ViewModelProvider(this).get(MainSharedViewModel.class);

        NavOptions navOptions = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                .build();

        String destination = intent.getStringExtra("navigate_to");
        intent.removeExtra("navigate_to");
        if (destination == null) {
            Log.w("MainActivity", "No navigation destination specified in intent");
            return;
        }

        if ("progress_fragment".equals(destination)) {
            long progressId = intent.getLongExtra("progress_id", -1);
            intent.removeExtra("progress_id");
            boolean trackFinished = intent.getBooleanExtra("track_finished", false);
            Log.e("MainActivity", "Navigating to progress_fragment with ID: " + progressId + ", finished: " + trackFinished);
            if(trackFinished) {
                sharedViewModel.openTrackFinishedEvent.setValue(progressId);
            }
            else {
                sharedViewModel.openTrackWithProgressEvent.setValue(progressId);
            }
            navController.navigate(R.id.nav_tracks_progress, null, navOptions);
            bottomNavigationView.getMenu().findItem(R.id.nav_tracks_progress).setChecked(true);
        }

        if("milestone_fragment".equals(destination)) {
            long milestoneId = intent.getLongExtra("milestone_id", -1);
            intent.removeExtra("milestone_id");
            Bundle args = new Bundle();
            args.putLong("milestone_id", milestoneId);
            navController.navigate(R.id.nav_milestone, args, navOptions);
        }

        if("achievement_fragment".equals(destination)) {
            long achievementId = intent.getLongExtra("achievement_id", -1);
            intent.removeExtra("achievement_id");
            sharedViewModel.openAchievementReachedEvent.setValue(achievementId);
            navController.navigate(R.id.nav_achievements, null, navOptions);
            bottomNavigationView.getMenu().findItem(R.id.nav_achievements).setChecked(true);
        }
        setIntent(new Intent());
    }
}