package com.bernhardgruendling.dueprocess.util;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.UserManager;
import android.util.Log;

import com.bernhardgruendling.dueprocess.AppSettings;
import com.bernhardgruendling.dueprocess.R;

import java.util.ArrayList;
import java.util.List;

import static android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED;

public class PostProvisioningTask {
    private static final String TAG = "PostProvisioningTask";

    private final DevicePolicyManager devicePolicyManager;
    private final Context context;
    private final AppSettings appSettings;

    public PostProvisioningTask(Context context) {
        this.context = context;
        devicePolicyManager = Util.getDevicePolicyManager(context);
        appSettings = new AppSettings(this.context);
    }

    public void performPostProvisioningOperations() {
        enableProfile(context);
        if (appSettings.getIsPostProvisioningDone()) {
            return;
        }

        devicePolicyManager.addUserRestriction(Util.getAdminComponentName(context), UserManager.DISALLOW_UNIFIED_PASSWORD);

        installDefaultApps();
        appSettings.setAppMarkedSensitive("com.android.vending", true);

        HidingUtil.hideLauncherIcon(context, HidingUtil.MAIN_LAUNCH_ACTIVITY);
        HidingUtil.showLauncherIcon(context, HidingUtil.ALIAS_LAUNCH_ACTIVITY);

        appSettings.setPostProvisioningDone();
    }

    private void enableProfile(Context context) {
        DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = Util.getAdminComponentName(context);
        manager.setProfileName(componentName, context.getString(R.string.profile_name));
        manager.setProfileEnabled(componentName);
    }

    private void installDefaultApps() {
        devicePolicyManager.enableSystemApp(Util.getAdminComponentName(context), "com.android.vending");
        devicePolicyManager.enableSystemApp(Util.getAdminComponentName(context), "com.android.chrome");
        devicePolicyManager.enableSystemApp(Util.getAdminComponentName(context), "com.google.android.gm");
    }

    private void autoGrantRequestedPermissionsToSelf() {
        String packageName = context.getPackageName();
        ComponentName adminComponentName = Util.getAdminComponentName(context);

        List<String> permissions = getRuntimePermissions(context.getPackageManager(), packageName);
        for (String permission : permissions) {
            boolean success = devicePolicyManager.setPermissionGrantState(adminComponentName,
                    packageName, permission, PERMISSION_GRANT_STATE_GRANTED);
            Log.d(TAG, "Auto-granting " + permission + ", success: " + success);
            if (!success) {
                Log.e(TAG, "Failed to auto grant permission to self: " + permission);
            }
        }
    }

    private List<String> getRuntimePermissions(PackageManager packageManager, String packageName) {
        List<String> permissions = new ArrayList<>();
        PackageInfo packageInfo;
        try {
            packageInfo =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not retrieve info about the package: " + packageName, e);
            return permissions;
        }

        if (packageInfo != null && packageInfo.requestedPermissions != null) {
            for (String requestedPerm : packageInfo.requestedPermissions) {
                if (isRuntimePermission(packageManager, requestedPerm)) {
                    permissions.add(requestedPerm);
                }
            }
        }
        return permissions;
    }

    private boolean isRuntimePermission(PackageManager packageManager, String permission) {
        try {
            PermissionInfo pInfo = packageManager.getPermissionInfo(permission, 0);
            if (pInfo != null) {
                if ((pInfo.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE)
                        == PermissionInfo.PROTECTION_DANGEROUS) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "Could not retrieve info about the permission: " + permission);
        }
        return false;
    }


}
