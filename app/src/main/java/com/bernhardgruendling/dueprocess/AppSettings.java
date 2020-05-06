package com.bernhardgruendling.dueprocess;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.bernhardgruendling.dueprocess.model.AppInfo;
import com.bernhardgruendling.dueprocess.model.AppInfoMapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AppSettings {

    public static final String THIRD_PARTY_SOURCES = "thirdPartySources";
    public static final String SHOW_SENSITIVE_APPS = "showSensitiveApps";
    public static final String LOCK_NOW_PROFILE = "lockNowProfile";
    public static final String LOCK_NOW_PROFILE_KEY_EVICT = "lockNowProfileKeyEvict";
    public static final String HIDE_LAUNCHER_ICON = "hideLauncherIcon";
    public static final String PASSWORD_FAILS_LOCKDOWN = "passwordFailsLockdown";
    public static final String PASSWORD_FAILS_WIPE = "passwordFailsWipe";
    public static final String LOCKDOWN_TRIGGER = "lockdownTrigger";
    public static final String WIPE_TRIGGER = "wipeTrigger";
    public static final String SHOW_LAUNCHER_ICON = "showLauncherIcon";
    public static final String ORG_NAME = "orgName";
    public static final String LOCKSCREEN_DESCRIPTION = "lockscreenDescription";
    public static final String IS_FIRST_INTRO_RUN = "isFirstIntroRun";
    public static final String UNLOCK_ATTEMPTS = "unlockAttempts";
    public static final String NEXT_ALLOWED_ATTEMPT_TIME = "nextAllowedAttemptTime";
    public static final String UNLOCK_CODE = "unlock_code";
    public static final String IS_PROVISIONING_DONE = "isProvisioningDone";
    public static final String SHOW_SYSTEM_APPS = "showSystemApps";
    public static final String REMOVE_GOOGLE_ACCOUNT = "removeGoogleAccount";
    public static final String STORAGE_GRANTED_APPS = "storageGrantedApps";

    private static final String SENSITIVE_APPS = "sensitiveApps";
    private static final String IS_FIRST_RUN = "isFirstRun";


    private Context context;
    private final SharedPreferences defaultPrefs;
    private final SharedPreferences appInfoPrefs;
    private final SharedPreferences authPrefs; //TODO Make secure prefs
    private static final String APP_INFO_PREFS = "app_info_prefs";


    public AppSettings(Context context) {
        this.context = context;
        appInfoPrefs = context.getSharedPreferences(APP_INFO_PREFS, Context.MODE_PRIVATE);
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        authPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
    }

    public void setAppMarkedSensitive(AppInfo appInfo, boolean setSensitive) {
        setAppMarkedSensitive(appInfo.getPackageName(), setSensitive);
    }

    public void setAppMarkedSensitive(String packageName, boolean setSensitive) {
        Set<String> sensitiveApps = new HashSet<>(getSensitivePackageNames());
        if (setSensitive) {
            sensitiveApps.add(packageName);
        } else {
            sensitiveApps.remove(packageName);
        }
        Log.d("SETTINGS", sensitiveApps.toString());
        appInfoPrefs.edit().putStringSet(SENSITIVE_APPS, sensitiveApps).apply();
    }

    public boolean getIsAppMarkedSensitive(AppInfo appInfo) {
        return getSensitivePackageNames().contains(appInfo.getPackageName());
    }

    private Set<String> getSensitivePackageNames() {
        return appInfoPrefs.getStringSet(SENSITIVE_APPS, new HashSet<>());
    }

    public List<AppInfo> getSensitiveApps() {
        AppInfoMapper appInfoMapper = new AppInfoMapper(context);
        Set<String> packageNames = getSensitivePackageNames();
        return packageNames.stream().map(appInfoMapper::mapFromPackageName).collect(Collectors.toList());
    }

    public void setAppsWithGrantedStoragePermissions(Set<String> packagenames) {
        appInfoPrefs.edit().putStringSet(STORAGE_GRANTED_APPS, packagenames).apply();
    }

    public Set<String> getAppsWithGrantedStoragePermissions() {
        return appInfoPrefs.getStringSet(STORAGE_GRANTED_APPS, new HashSet<>());
    }

    public int getPreferenceInt(String key) {
        return defaultPrefs.getInt(key, -1);
    }

    public boolean getPreferenceBoolean(String key) {
        return defaultPrefs.getBoolean(key, false);
    }

    public String getPreferenceString(String key) {
        return defaultPrefs.getString(key, "");
    }


    public List<Integer> getUnlockCode() {
        String codeString = authPrefs.getString(UNLOCK_CODE, "-1");
        return Arrays.stream(codeString.split(",")).map(Integer::valueOf).collect(Collectors.toList());
    }

    public void setUnlockCode(List<Integer> code) {
        authPrefs.edit().putString(UNLOCK_CODE, code.toString().replace("[", "").replace("]", "").replace(" ", "")).apply();
    }

    public int getUnlockAttempts() {
        return authPrefs.getInt(UNLOCK_ATTEMPTS, 0);
    }

    public void setUnlockAttempts(int attempts) {
        authPrefs.edit().putInt(UNLOCK_ATTEMPTS, attempts).commit();
    }

    public long getNextAllowedUnlockAttemptTime() {
        return authPrefs.getLong(NEXT_ALLOWED_ATTEMPT_TIME, 0);
    }

    public void setNextAllowedUnlockAttemptTime(long time) {
        authPrefs.edit().putLong(NEXT_ALLOWED_ATTEMPT_TIME, time).commit();
    }

    public boolean isFirstIntroRun() {
        return defaultPrefs.getBoolean(IS_FIRST_INTRO_RUN, true);
    }

    public void setFirstIntroRunDone() {
        defaultPrefs.edit().putBoolean(IS_FIRST_INTRO_RUN, false).apply();
    }

    public boolean getIsFirstRun() {
        return defaultPrefs.getBoolean(IS_FIRST_RUN, true);
    }

    public void setFirstRunDone() {
        defaultPrefs.edit().putBoolean(IS_FIRST_RUN, false).apply();
    }

    public boolean getIsPostProvisioningDone() {
        return defaultPrefs.getBoolean(IS_PROVISIONING_DONE, false);
    }

    public void setPostProvisioningDone() {
        defaultPrefs.edit().putBoolean(IS_PROVISIONING_DONE, true).apply();
    }
}
