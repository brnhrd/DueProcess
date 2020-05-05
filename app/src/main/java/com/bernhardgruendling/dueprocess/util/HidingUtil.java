package com.bernhardgruendling.dueprocess.util;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

import com.bernhardgruendling.dueprocess.AppSettings;
import com.bernhardgruendling.dueprocess.model.AppInfo;
import com.bernhardgruendling.dueprocess.model.AppInfoMapper;
import com.bernhardgruendling.dueprocess.ui.MainActivity;

import java.util.List;


public class HidingUtil {
    public static final String MAIN_LAUNCH_ACTIVITY = MainActivity.class.getName();
    public static final String ALIAS_LAUNCH_ACTIVITY = MainActivity.class.getName() + "Alias";
    private static final String TAG = "HidingUtil";

    public static void hideLauncherIcon(Context context, String name) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, name),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public static void showLauncherIcon(Context context, String name) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, name),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static void hideSensitiveApps(Context context) {
        AppSettings appSettings = new AppSettings(context);
        for (AppInfo appInfo : appSettings.getSensitiveApps()) {
            hideApp(context, appInfo);
        }
    }

    public static void showSensitiveApps(Context context) {
        AppSettings appSettings = new AppSettings(context);
        for (AppInfo appInfo : appSettings.getSensitiveApps()) {
            showApp(context, appInfo);
        }
    }

    public static void hideApp(Context context, AppInfo appInfo) {
        setAppHidden(context, appInfo, true);
    }

    public static void showApp(Context context, AppInfo appInfo) {
        setAppHidden(context, appInfo, false);
    }

    public static void setAppHidden(Context context, AppInfo appInfo, boolean hidden) {
        DevicePolicyManager devicePolicyManager = Util.getDevicePolicyManager(context);
        ComponentName adminComponentName = Util.getAdminComponentName(context);
        devicePolicyManager.setApplicationHidden(adminComponentName, appInfo.getPackageName(), hidden);
    }

    public static void endLockdownAndShowLauncherIcon(Context context) {
        showLauncherIcon(context, ALIAS_LAUNCH_ACTIVITY);
        endLockdown(context);
    }

    public static void endLockdown(Context context) {
        AppInfoMapper appInfoMapper = new AppInfoMapper(context);
        showApp(context, appInfoMapper.mapFromPackageName("com.google.android.gms"));

        DevicePolicyManager devicePolicyManager = Util.getDevicePolicyManager(context);
        ComponentName adminComponentName = Util.getAdminComponentName(context);
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE);
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_INSTALL_APPS);
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_DEBUGGING_FEATURES);
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE);
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY);
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_APPS_CONTROL);

        devicePolicyManager.setKeyguardDisabledFeatures(adminComponentName, DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE);

        AppSettings appSettings = new AppSettings(context);
        if (appSettings.getPreferenceBoolean(AppSettings.SHOW_SENSITIVE_APPS)) {
            showSensitiveApps(context);
        }
    }

    public static void lockdownNow(Context context) {
        hideSensitiveApps(context);
        AppSettings appSettings = new AppSettings(context);
        if(appSettings.getPreferenceBoolean(AppSettings.REMOVE_GOOGLE_ACCOUNT)) {
            AppInfoMapper appInfoMapper = new AppInfoMapper(context);
            hideApp(context, appInfoMapper.mapFromPackageName("com.google.android.gms"));
        }

        DevicePolicyManager devicePolicyManager = Util.getDevicePolicyManager(context);
        ComponentName adminComponentName = Util.getAdminComponentName(context);
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE);
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_INSTALL_APPS);
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_DEBUGGING_FEATURES);
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE);
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY);
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_APPS_CONTROL);
        devicePolicyManager.setKeyguardDisabledFeatures(adminComponentName, DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS);

        if (appSettings.getPreferenceBoolean(AppSettings.HIDE_LAUNCHER_ICON)) {
            hideLauncherIcon(context, ALIAS_LAUNCH_ACTIVITY);
        }
        if (appSettings.getPreferenceBoolean(AppSettings.LOCK_NOW_PROFILE) && !appSettings.getPreferenceBoolean(AppSettings.LOCK_NOW_PROFILE_KEY_EVICT)) {
            devicePolicyManager.lockNow(); //this locks the work profile without a notification
        } else if (appSettings.getPreferenceBoolean(AppSettings.LOCK_NOW_PROFILE) && appSettings.getPreferenceBoolean(AppSettings.LOCK_NOW_PROFILE_KEY_EVICT)) {
            devicePolicyManager.lockNow(DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY);
        }
        try {
            ((Activity) context).finish();
        } catch (ClassCastException e) {
            //activity not available
        }
    }
}
