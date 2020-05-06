/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bernhardgruendling.dueprocess.util;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.bernhardgruendling.dueprocess.DueProcessDeviceAdminReceiver;

import java.util.Collections;
import java.util.List;


public class Util {
    private static final String TAG = "Util";

    public static boolean isProfileOwnerApp(Context context) {
        return Util.getDevicePolicyManager(context).isProfileOwnerApp(context.getPackageName());
    }

    public static DevicePolicyManager getDevicePolicyManager(Context context) {
        return (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    public static ComponentName getAdminComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), DueProcessDeviceAdminReceiver.class);
    }

    public static List<ApplicationInfo> getAllInstalledApplicationsSorted(Context context, boolean withSystemApps) {
        PackageManager pM = context.getPackageManager();
        List<ApplicationInfo> allApps = pM.getInstalledApplications(
                PackageManager.MATCH_UNINSTALLED_PACKAGES);
        if (!withSystemApps) {
            allApps.removeIf(n -> (isAndroidSystemPackageWithoutLaunchIntent(context, pM, n) || isWeirdPackage(n)));
        }
        allApps.removeIf(n -> n.packageName.equals(context.getPackageName()));
        Collections.sort(allApps, new ApplicationInfo.DisplayNameComparator(pM));
        return allApps;
    }

    private static boolean isWeirdPackage(ApplicationInfo n) {
        return n.packageName.equals("com.android.traceur") || n.packageName.equals("com.google.android.angle");
    }

    private static boolean isAndroidSystemPackageWithoutLaunchIntent(Context context, PackageManager pM, ApplicationInfo n) {
        return pM.getLaunchIntentForPackage(n.packageName) == null && !Util.getDevicePolicyManager(context).isApplicationHidden(Util.getAdminComponentName(context), n.packageName);
    }
}
