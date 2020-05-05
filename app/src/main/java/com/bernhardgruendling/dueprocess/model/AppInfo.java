package com.bernhardgruendling.dueprocess.model;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

public class AppInfo {

    private final ApplicationInfo applicationInfo;

    private Drawable applicationIcon;
    private String appName;
    private boolean hidden;
    private boolean sensitive;

    public AppInfo(ApplicationInfo applicationInfo) {
        this.applicationInfo = applicationInfo;
    }

    public AppInfo(ApplicationInfo applicationInfo, String appName, Drawable applicationIcon) {
        this.applicationInfo = applicationInfo;
        this.appName = appName;
        this.applicationIcon = applicationIcon;
    }

    public String getPackageName() {
        return applicationInfo.packageName;
    }

    public String getAppName() {
        return appName;
    }

    public Drawable getApplicationIcon() {
        return applicationIcon;
    }

    public void setIsHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }
}
