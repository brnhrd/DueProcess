package com.bernhardgruendling.dueprocess.ui.setup;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.CrossProfileApps;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bernhardgruendling.dueprocess.R;
import com.bernhardgruendling.dueprocess.util.HidingUtil;
import com.bernhardgruendling.dueprocess.util.NotificationUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PostSetupFragment extends Fragment {

    public static final String FRAGMENT_TAG = "PostSetupFragment";
    private Context context;
    private TextView tVAppLabel;
    private ImageView iVAppIcon;
    private CrossProfileApps crossProfileApps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tVAppLabel = view.findViewById(R.id.tVcrossProfileAppLabel);
        iVAppIcon = view.findViewById(R.id.iVcrossProfileAppIcon);
        crossProfileApps = getActivity().getSystemService(CrossProfileApps.class);

        prepareUninstallReminderNotification();
        waitForWorkProfileReady();

    }

    private void prepareUninstallReminderNotification() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_DELETE);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                NotificationUtil.showNotification(context, "Uninstall Setup", "To remove any traces from your personal profile, tap to delete the Setup app from your personal profile. You don't need it anymore.", NotificationUtil.DEFAULT_NOTIFICATION_ID, intent);

            }
        }, 1000 * 60 * 5);
    }

    private void waitForWorkProfileReady() {
        try {
            while (!refreshUi()) {
                Thread.sleep(1000 );
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshUi();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private boolean refreshUi() {
        List<UserHandle> targetUserProfiles = crossProfileApps.getTargetUserProfiles();
        if (targetUserProfiles.isEmpty()) {
            showNoTargetUserUi();
            return false;
        } else {
            showHasTargetUserUi(targetUserProfiles.get(0));
            return true;
        }
    }

    private void showNoTargetUserUi() {
        tVAppLabel.setText("Something went wrong and you cannot jump to the management app.");
        iVAppIcon.setImageDrawable(null);
        iVAppIcon.setOnClickListener(null);
    }


    private void showHasTargetUserUi(UserHandle userHandle) {
        tVAppLabel.setText(crossProfileApps.getProfileSwitchingLabel(userHandle));
        iVAppIcon.setImageDrawable(
                crossProfileApps.getProfileSwitchingIconDrawable(userHandle));
        ComponentName componentName = ComponentName.createRelative(context, HidingUtil.ALIAS_LAUNCH_ACTIVITY);
        iVAppIcon.setOnClickListener(
                view -> crossProfileApps.startMainActivity(componentName, userHandle));
    }
}
