package com.bernhardgruendling.dueprocess;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.UserHandle;

import androidx.annotation.NonNull;

import com.bernhardgruendling.dueprocess.ui.PatternUnlock;
import com.bernhardgruendling.dueprocess.ui.PatternUnlockListener;
import com.bernhardgruendling.dueprocess.util.HidingUtil;
import com.bernhardgruendling.dueprocess.util.PostProvisioningTask;
import com.bernhardgruendling.dueprocess.util.Util;

import java.util.ArrayList;

public class DueProcessDeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver implements PatternUnlockListener.MyCodeListener {
    private static final String TAG = "DueProcessDeviceAdminReceiver";
    private PatternUnlock patternUnlock;
    private Context context;

    @Override
    public void onProfileProvisioningComplete(@NonNull Context context, @NonNull Intent intent) {
        super.onProfileProvisioningComplete(context, intent);
        PostProvisioningTask task = new PostProvisioningTask(context);
        task.performPostProvisioningOperations();
    }

    @Override
    public void onPasswordFailed(@NonNull Context context, @NonNull Intent intent, @NonNull UserHandle user) {
        AppSettings appSettings = new AppSettings(context);
        if (appSettings.getPreferenceBoolean(AppSettings.WIPE_TRIGGER)) {
            if (getCurrentFailedPasswordAttempts(context) >= appSettings.getPreferenceInt(AppSettings.PASSWORD_FAILS_WIPE)) {
                Util.getDevicePolicyManager(context).wipeData(DevicePolicyManager.WIPE_SILENTLY);
            }
        }
        if (appSettings.getPreferenceBoolean(AppSettings.LOCKDOWN_TRIGGER)) { //TODO only when not already lockdown
            if (getCurrentFailedPasswordAttempts(context) >= appSettings.getPreferenceInt(AppSettings.PASSWORD_FAILS_LOCKDOWN)) {
                HidingUtil.lockdownNow(context);
            }
        }
    }

    private int getCurrentFailedPasswordAttempts(@NonNull Context context) {
        return Util.getDevicePolicyManager(context).getParentProfileInstance(Util.getAdminComponentName(context)).getCurrentFailedPasswordAttempts();
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        AppSettings appSettings;
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                appSettings = new AppSettings(context);
                if (!appSettings.isFirstIntroRun()) {
                    this.context = context;
                    Handler mHandler = new Handler();
                    mHandler.post(() -> {
                        patternUnlock = new PatternUnlock(context, appSettings.getUnlockCode());
                        patternUnlock.initOverlay(false, DueProcessDeviceAdminReceiver.this);
                    });
                    mHandler.postDelayed(() -> patternUnlock.clearOverlays(), 30 * 1000);
                }
                break;
            case Intent.ACTION_TIME_CHANGED:
                appSettings = new AppSettings(context);
                appSettings.setNextAllowedUnlockAttemptTime(System.currentTimeMillis() + appSettings.getUnlockAttempts() * 10000);
                break;
            default:
                super.onReceive(context, intent);
                break;
        }

    }

    @Override
    public void onCodeInputFinished(ArrayList<Integer> inputCode) {
        AppSettings appSettings = new AppSettings(context);

        if (appSettings.getNextAllowedUnlockAttemptTime() > System.currentTimeMillis()) {
            // Log.d("MainActivity", "attempt ignored until " + appSettings.getNextAllowedUnlockAttemptTime());
            return;
        }
        appSettings.setUnlockAttempts(appSettings.getUnlockAttempts() + 1);
        // Log.d("MainActivity", "attempt number " + appSettings.getUnlockAttempts());

        if (appSettings.getUnlockAttempts() > 2) {
            appSettings.setNextAllowedUnlockAttemptTime(System.currentTimeMillis() + 10000 * appSettings.getUnlockAttempts());
        }

        if (appSettings.getUnlockCode().equals(inputCode)) {
            appSettings.setUnlockAttempts(0);
            appSettings.setNextAllowedUnlockAttemptTime(0);
            patternUnlock.clearOverlays();
            HidingUtil.endLockdownAndShowLauncherIcon(context);
        }
    }

    @Override
    public void onCodeInputProgress(ArrayList<Integer> inputCode) {
        //only needed in pattern setup
    }

    @Override
    public void onCodeInputStarted() {
        //only needed in pattern setup
    }
}
