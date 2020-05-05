package com.bernhardgruendling.dueprocess.model;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.bernhardgruendling.dueprocess.AppSettings;
import com.bernhardgruendling.dueprocess.model.AppInfo;
import com.bernhardgruendling.dueprocess.util.Util;

public class AppInfoMapper {
    private Context context;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponentName;
    private PackageManager packageManager;

    public AppInfoMapper(Context context) {
        this.context = context;
        this.devicePolicyManager = Util.getDevicePolicyManager(context);
        this.adminComponentName = Util.getAdminComponentName(context);
        this.packageManager = context.getPackageManager();
    }

    public AppInfo map(ApplicationInfo applicationInfo) {
        AppInfo appInfo = new AppInfo(applicationInfo, packageManager.getApplicationLabel(applicationInfo).toString(), packageManager.getApplicationIcon(applicationInfo));
        appInfo.setIsHidden(devicePolicyManager.isApplicationHidden(adminComponentName, applicationInfo.packageName));
        AppSettings appSettings = new AppSettings(context);
        appInfo.setSensitive(appSettings.getIsAppMarkedSensitive(appInfo));
        return appInfo;
    }

    public AppInfo mapFromPackageName(String packageName) {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            return map(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
