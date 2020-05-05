package com.bernhardgruendling.dueprocess.ui.management;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bernhardgruendling.dueprocess.AppSettings;
import com.bernhardgruendling.dueprocess.R;
import com.bernhardgruendling.dueprocess.model.AppInfo;
import com.bernhardgruendling.dueprocess.model.AppInfoMapper;
import com.bernhardgruendling.dueprocess.util.HidingUtil;
import com.bernhardgruendling.dueprocess.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AppManagementFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AppInfo>>, AppListAdapter.RecyclerViewClickListener {
    public static final String FRAGMENT_TAG = "AppManagementFragment";
    private View view;
    private Context context;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponentName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminComponentName = Util.getAdminComponentName(context);
        devicePolicyManager = Util.getDevicePolicyManager(context);
        LoaderManager lM = LoaderManager.getInstance(this);
        lM.initLoader(0, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
    }

    private List<ApplicationInfo> getAllInstalledApplicationsSorted() {
        PackageManager pM = context.getPackageManager();
        List<ApplicationInfo> allApps = pM.getInstalledApplications(
                PackageManager.MATCH_UNINSTALLED_PACKAGES);
        AppSettings appSettings = new AppSettings(context);
        if (!appSettings.getPreferenceBoolean(AppSettings.SHOW_SYSTEM_APPS)) {
            allApps.removeIf(n -> (isAndroidSystemPackageWithoutLaunchIntent(pM, n) || isWeirdPackage(n)));
        }
        allApps.removeIf(n -> n.packageName.equals(context.getPackageName()));
        Collections.sort(allApps, new ApplicationInfo.DisplayNameComparator(pM));
        return allApps;
    }

    private boolean isWeirdPackage(ApplicationInfo n) {
        return n.packageName.equals("com.android.traceur") || n.packageName.equals("com.google.android.angle");
    }

    private boolean isAndroidSystemPackageWithoutLaunchIntent(PackageManager pM, ApplicationInfo n) {
        return pM.getLaunchIntentForPackage(n.packageName) == null && !devicePolicyManager.isApplicationHidden(adminComponentName, n.packageName);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_management, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void recyclerViewToggleHiddenClicked(View v, AppInfo appInfo) {
        HidingUtil.setAppHidden(context, appInfo, !appInfo.isHidden());
        appInfo.setIsHidden(!appInfo.isHidden());
    }

    @Override
    public void recyclerViewSwitchSensitiveClicked(View v, AppInfo appInfo, boolean active) {
        AppSettings appSettings = new AppSettings(context);
        appSettings.setAppMarkedSensitive(appInfo, active);
        Switch switchSensitive = (Switch) v;
        if (active) {
            switchSensitive.getTrackDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
        } else {
            switchSensitive.getTrackDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.SRC_IN));
        }

    }

    private void displayAppList(List<AppInfo> appInfoPackages) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        AppListAdapter mAdapter = new AppListAdapter(appInfoPackages, AppManagementFragment.this);
        recyclerView.setAdapter(mAdapter);
    }

    @NonNull
    @Override
    public Loader<List<AppInfo>> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<List<AppInfo>>(getActivity()) {
            @Override
            public List<AppInfo> loadInBackground() {
                List<ApplicationInfo> packages = getAllInstalledApplicationsSorted();
                AppInfoMapper appInfoMapper = new AppInfoMapper(context);
                return packages.stream().map(appInfoMapper::map).collect(Collectors.toList());
            }

            @Override
            protected void onStartLoading() {
                view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                view.findViewById(R.id.recycler_view).setVisibility(View.GONE);
                forceLoad();
            }
        };

    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<AppInfo>> loader, List<AppInfo> data) {
        view.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.recycler_view).setVisibility(View.VISIBLE);
        displayAppList(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        //not needed
    }
}
