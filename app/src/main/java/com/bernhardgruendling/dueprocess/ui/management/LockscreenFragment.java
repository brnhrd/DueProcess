package com.bernhardgruendling.dueprocess.ui.management;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bernhardgruendling.dueprocess.AppSettings;
import com.bernhardgruendling.dueprocess.R;

public class LockscreenFragment extends Fragment {

    public static final String FRAGMENT_TAG = "LockscreenFragment";
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lockscreen, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        AppSettings appSettings = new AppSettings(context);
        TextView tVOrgName = view.findViewById(R.id.tVOrgName);
        TextView tVLockScreenDescription = view.findViewById(R.id.tVLockscreenDescription);
        if (appSettings.getIsFirstRun()) {
            appSettings.setFirstRunDone();
            tVOrgName.setText("This is the secret lockscreen of your management app.");
            tVLockScreenDescription.setText("Enter your secret lockscreen pattern to enter the management app.\n\nThis message is only shown once. Next time you open the management app, you will see a decoy message. You can customize the decoy message in Settings.");
        } else {
            tVOrgName.setText(appSettings.getPreferenceString(AppSettings.ORG_NAME));
            tVLockScreenDescription.setText(appSettings.getPreferenceString(AppSettings.LOCKSCREEN_DESCRIPTION));
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}

