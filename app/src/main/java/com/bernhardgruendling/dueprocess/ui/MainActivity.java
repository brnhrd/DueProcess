package com.bernhardgruendling.dueprocess.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.bernhardgruendling.dueprocess.AppSettings;
import com.bernhardgruendling.dueprocess.R;
import com.bernhardgruendling.dueprocess.ui.intro.IntroActivity;
import com.bernhardgruendling.dueprocess.ui.management.CodeConfigActivity;
import com.bernhardgruendling.dueprocess.ui.management.LockscreenFragment;
import com.bernhardgruendling.dueprocess.ui.management.ManagementNavFragment;
import com.bernhardgruendling.dueprocess.ui.setup.SetupFragment;
import com.bernhardgruendling.dueprocess.util.HidingUtil;
import com.bernhardgruendling.dueprocess.util.Util;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, PatternUnlockListener.MyCodeListener {

    private static final int REQUEST_INTRO_ACTIVITY = 1;
    private static final int REQUEST_CODE_CONFIG_ACTIVITY = 2;
    private int activityRequestCode; //TODO is this secure?
    private PatternUnlock patternUnlock;
    private TimerTask timerTask;

    public static void initTheme(AppCompatActivity activity) {
        switch (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTheme(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        if (savedInstanceState == null) {
            if (!Util.isProfileOwnerApp(this)) {
                showSetupVersionOfApp();
            } else {
                showManagementVersionOfApp();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        if (isSwitchFromOrToTrustedActivity()) {
            activityRequestCode = 0;
            return;
        }
        if (Util.isProfileOwnerApp(this)) {
            // If the managed profile is already set up, we show the lockscreen.
            AppSettings appSettings = new AppSettings(this);
            if (!appSettings.isFirstIntroRun()) {
                showPatternLockScreenWithOverlay();
            }
        }
    }

    private boolean isSwitchFromOrToTrustedActivity() {
        return activityRequestCode == REQUEST_CODE_CONFIG_ACTIVITY;
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        if (isSwitchFromOrToTrustedActivity()) {
            return;
        }
        if (Util.isProfileOwnerApp(this)) {
            // If the managed profile is already set up, we show the lockscreen.
            AppSettings appSettings = new AppSettings(this);
            if (!appSettings.isFirstIntroRun()) {
                showPatternLockScreenWithoutOverlay();
            }
        }
    }

    private void showManagementVersionOfApp() {
        AppSettings appSettings = new AppSettings(this);
        if (appSettings.isFirstIntroRun()) {
            Intent intent = new Intent(this, IntroActivity.class);
            startActivityForResult(intent, REQUEST_INTRO_ACTIVITY);
        }
    }

    private void showPatternLockScreenWithoutOverlay() {
        patternUnlock.clearOverlays();
        getSupportActionBar().setTitle("");
        showLockScreenFragment();
    }

    private void showPatternLockScreenWithOverlay() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        getSupportActionBar().setTitle("");
        showLockScreenFragment();
        initPatternUnlock();
    }

    private void initPatternUnlock() {
        AppSettings appSettings = new AppSettings(this);
        if (patternUnlock == null) {
            patternUnlock = new PatternUnlock(this, appSettings.getUnlockCode());
        } else {
            patternUnlock.setCode(appSettings.getUnlockCode()); //in case code changed during this lifecycle
        }
        patternUnlock.initOverlay(false, this);
        if (timerTask != null) timerTask.cancel();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> patternUnlock.clearOverlays());
            }
        };
        new Timer().schedule(timerTask, 30 * 1000);
    }

    private void showLockScreenFragment() {
        this.getSupportFragmentManager().beginTransaction().replace(R.id.container,
                new LockscreenFragment(),
                LockscreenFragment.FRAGMENT_TAG).commit();
    }

    private void showSetupVersionOfApp() {
        this.getSupportFragmentManager().beginTransaction().add(R.id.container,
                new SetupFragment(),
                SetupFragment.FRAGMENT_TAG).commit();
    }

    private void showManagementFragment() {
        this.getSupportFragmentManager().beginTransaction().replace(R.id.container,
                new ManagementNavFragment(),
                ManagementNavFragment.FRAGMENT_TAG).commit();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        Intent intent;
        switch (pref.getFragment()) {
            case "CodeConfigActivity":
                activityRequestCode = REQUEST_CODE_CONFIG_ACTIVITY;
                intent = new Intent(this, CodeConfigActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CONFIG_ACTIVITY);
                break;
            case "IntroActivity":
                intent = new Intent(this, IntroActivity.class);
                startActivityForResult(intent, REQUEST_INTRO_ACTIVITY);
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        activityRequestCode = requestCode;
        if (resultCode == 0) {
            activityRequestCode = 0; //making sure to show lockscreen if codeconfigactivity has been paused
        }
    }

    @Override
    public void onCodeInputFinished(ArrayList<Integer> inputCode) {
        AppSettings appSettings = new AppSettings(this);
        if (appSettings.getNextAllowedUnlockAttemptTime() > System.currentTimeMillis()) {
            return;
        }
        appSettings.setUnlockAttempts(appSettings.getUnlockAttempts() + 1);

        if (appSettings.getUnlockAttempts() > 2) {
            appSettings.setNextAllowedUnlockAttemptTime(System.currentTimeMillis() + 10000 * appSettings.getUnlockAttempts());
        }

        if (appSettings.getUnlockCode().equals(inputCode)) {
            appSettings.setUnlockAttempts(0);
            appSettings.setNextAllowedUnlockAttemptTime(0);
            HidingUtil.endLockdown(this);
            showManagementFragment();
            patternUnlock.clearOverlays();
            timerTask.cancel();
        }
    }


    @Override
    public void onCodeInputProgress(ArrayList<Integer> inputCode) {
        //only needed when setting up
    }

    @Override
    public void onCodeInputStarted() {
        //only needed when setting up
    }
}
