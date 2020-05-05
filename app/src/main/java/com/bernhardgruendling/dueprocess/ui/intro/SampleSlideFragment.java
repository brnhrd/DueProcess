package com.bernhardgruendling.dueprocess.ui.intro;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bernhardgruendling.dueprocess.AppSettings;
import com.bernhardgruendling.dueprocess.R;
import com.bernhardgruendling.dueprocess.ui.management.CodeConfigActivity;
import com.bernhardgruendling.dueprocess.util.Util;
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.github.paolorotolo.appintro.ISlidePolicy;

public class SampleSlideFragment extends Fragment implements ISlideBackgroundColorHolder, ISlidePolicy {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private static final String ARG_BUTTON_RES_ID = "buttonResId";
    private int layoutResId;
    private int buttonResId;
    private View view;
    private Context context;
    private Button button;

    public static SampleSlideFragment newInstance(int layoutResId, int buttonResId) {
        SampleSlideFragment sampleSlideFragment = new SampleSlideFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        args.putInt(ARG_BUTTON_RES_ID, buttonResId);
        sampleSlideFragment.setArguments(args);

        return sampleSlideFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID) && getArguments().containsKey(ARG_BUTTON_RES_ID)) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
            buttonResId = getArguments().getInt(ARG_BUTTON_RES_ID);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        if (buttonResId != 0) {
            button = view.findViewById(buttonResId);
            button.setOnClickListener(view1 -> {
                Intent intent;
                switch (buttonResId) {
                    case R.id.bSetSepChallenge:
                        intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        break;
                    case R.id.bGrantPermission:
                        intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                        startActivityForResult(intent, 0);
                        break;
                    case R.id.bSetPattern:
                        intent = new Intent(context, CodeConfigActivity.class);
                        startActivityForResult(intent, 1);
                        break;
                }
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutResId, container, false);
    }


    @Override
    public int getDefaultBackgroundColor() {
        return ContextCompat.getColor(context, R.color.colorPrimaryDark);
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        if (view != null) {
            view.setBackgroundColor(backgroundColor);
        }
    }

    @Override
    public boolean isPolicyRespected() {
        switch (layoutResId) {
            case R.layout.slide_separate_challenge:
                return !Util.getDevicePolicyManager(context).isUsingUnifiedPassword(Util.getAdminComponentName(context));
            case R.layout.slide_overlay_permission:
                return Settings.canDrawOverlays(context);
            case R.layout.slide_pattern_setup:
                AppSettings appSettings = new AppSettings(context);
                return !appSettings.getUnlockCode().contains(-1);
        }
        return true;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        Animation wiggleAnim = AnimationUtils.loadAnimation(context, R.anim.wiggle);
        button.startAnimation(wiggleAnim);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPolicyRespected()) {
            if (button !=  null) {
                button.setVisibility(View.GONE);
                ImageView iVDone = view.findViewById(R.id.iVDone);
                iVDone.setVisibility(View.VISIBLE);
            }
        }
    }

}