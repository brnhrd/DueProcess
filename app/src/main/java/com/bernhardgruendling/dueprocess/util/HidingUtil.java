package com.bernhardgruendling.dueprocess.util;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.UserManager;

import com.bernhardgruendling.dueprocess.AppSettings;
import com.bernhardgruendling.dueprocess.model.AppInfo;
import com.bernhardgruendling.dueprocess.model.AppInfoMapper;
import com.bernhardgruendling.dueprocess.ui.MainActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


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

    public static void hideApp(Context context, String packagename) {
        setAppHidden(context, packagename, true);
    }

    public static void hideApp(Context context, AppInfo appInfo) {
        setAppHidden(context, appInfo, true);
    }

    public static void showApp(Context context, String packagename) {
        setAppHidden(context, packagename, false);
    }

    public static void showApp(Context context, AppInfo appInfo) {
        setAppHidden(context, appInfo, false);
    }

    public static void setAppHidden(Context context, AppInfo appInfo, boolean hidden) {
        setAppHidden(context, appInfo.getPackageName(), hidden);
    }

    public static void setAppHidden(Context context, String packagename, boolean hidden) {
        DevicePolicyManager devicePolicyManager = Util.getDevicePolicyManager(context);
        ComponentName adminComponentName = Util.getAdminComponentName(context);
        devicePolicyManager.setApplicationHidden(adminComponentName, packagename, hidden);
    }

    public static void endLockdownAndShowLauncherIcon(Context context) {
        showLauncherIcon(context, ALIAS_LAUNCH_ACTIVITY);
        endLockdown(context);
    }

    public static void endLockdown(Context context) {
        showApp(context, "com.google.android.gms");

        DevicePolicyManager devicePolicyManager = Util.getDevicePolicyManager(context);
        ComponentName adminComponentName = Util.getAdminComponentName(context);
        clearUserRestrictions(devicePolicyManager, adminComponentName);

        devicePolicyManager.setKeyguardDisabledFeatures(adminComponentName, DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE);

        restoreStoragePermissions(context, devicePolicyManager, adminComponentName, new AppSettings(context));

        AppSettings appSettings = new AppSettings(context);
        if (appSettings.getPreferenceBoolean(AppSettings.SHOW_SENSITIVE_APPS)) {
            showSensitiveApps(context);
        }
    }

    private static void clearUserRestrictions(DevicePolicyManager devicePolicyManager, ComponentName adminComponentName) {
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE);
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_INSTALL_APPS);
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_DEBUGGING_FEATURES);
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE);
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY);
        devicePolicyManager.clearUserRestriction(adminComponentName, UserManager.DISALLOW_APPS_CONTROL);
    }

    private static void restoreStoragePermissions(Context context, DevicePolicyManager devicePolicyManager, ComponentName adminComponentName, AppSettings appSettings) {
        List<ApplicationInfo> allApps = Util.getAllInstalledApplicationsSorted(context, false);
        Set<String> appsWithGrantedStoragePermission = appSettings.getAppsWithGrantedStoragePermissions();
        for (ApplicationInfo app : allApps) {
            String packageName = app.packageName;
            if (appsWithGrantedStoragePermission.contains(packageName)) {
                devicePolicyManager.setPermissionGrantState(adminComponentName, packageName, Manifest.permission.READ_EXTERNAL_STORAGE, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                devicePolicyManager.setPermissionGrantState(adminComponentName, packageName, Manifest.permission.WRITE_EXTERNAL_STORAGE, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
            }
            devicePolicyManager.setPermissionGrantState(adminComponentName, packageName, Manifest.permission.READ_EXTERNAL_STORAGE, DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT);
            devicePolicyManager.setPermissionGrantState(adminComponentName, packageName, Manifest.permission.WRITE_EXTERNAL_STORAGE, DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT);
        }
        devicePolicyManager.setPermissionPolicy(adminComponentName, DevicePolicyManager.PERMISSION_POLICY_PROMPT);
    }

    public static void lockdownNow(Context context) {
        hideSensitiveApps(context);
        AppSettings appSettings = new AppSettings(context);
        if (appSettings.getPreferenceBoolean(AppSettings.REMOVE_GOOGLE_ACCOUNT)) {
            hideApp(context, "com.google.android.gms");
        }

        DevicePolicyManager devicePolicyManager = Util.getDevicePolicyManager(context);
        ComponentName adminComponentName = Util.getAdminComponentName(context);
        setUserRestrictions(devicePolicyManager, adminComponentName);

        devicePolicyManager.setKeyguardDisabledFeatures(adminComponentName, DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS);

        disableStoragePermissions(context, devicePolicyManager, adminComponentName, appSettings);

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

    private static void setUserRestrictions(DevicePolicyManager devicePolicyManager, ComponentName adminComponentName) {
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE);
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_INSTALL_APPS);
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_DEBUGGING_FEATURES);
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE);
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY);
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_APPS_CONTROL);
    }

    private static void disableStoragePermissions(Context context, DevicePolicyManager devicePolicyManager, ComponentName adminComponentName, AppSettings appSettings) {
        PackageManager pM = context.getPackageManager();
        List<ApplicationInfo> allApps = Util.getAllInstalledApplicationsSorted(context, false);
        Set<String> appsWithGrantedStoragePermissions = new HashSet<>();
        for (ApplicationInfo app : allApps) {
            String packageName = app.packageName;
            if (pM.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, packageName) == PackageManager.PERMISSION_GRANTED) {
                appsWithGrantedStoragePermissions.add(packageName);
            }
            devicePolicyManager.setPermissionGrantState(adminComponentName, packageName, Manifest.permission.READ_EXTERNAL_STORAGE, DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED);
            devicePolicyManager.setPermissionGrantState(adminComponentName, packageName, Manifest.permission.WRITE_EXTERNAL_STORAGE, DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED);
        }
        appSettings.setAppsWithGrantedStoragePermissions(appsWithGrantedStoragePermissions);
        devicePolicyManager.setPermissionPolicy(adminComponentName, DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY);
    }
}
