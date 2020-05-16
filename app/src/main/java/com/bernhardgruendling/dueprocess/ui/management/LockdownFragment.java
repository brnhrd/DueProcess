package com.bernhardgruendling.dueprocess.ui.management;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bernhardgruendling.dueprocess.R;
import com.bernhardgruendling.dueprocess.util.HidingUtil;
import com.bernhardgruendling.dueprocess.util.Util;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;

public class LockdownFragment extends Fragment implements View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {
    public static final String FRAGMENT_TAG = "LockDownFragment";
    private Context context;
    private TextView tvTapAndHold;
    private TextView tVHold;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lockdown, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button bWipeNow = view.findViewById(R.id.bWipeNow);
        bWipeNow.setOnTouchListener(this);
        bWipeNow.setOnLongClickListener(this);

        Button bLockdownNow = view.findViewById(R.id.bLockdownNow);
        bLockdownNow.setOnTouchListener(this);
        bLockdownNow.setOnLongClickListener(this);

        view.findViewById(R.id.bHelpLockdown).setOnClickListener(this);

        tvTapAndHold = view.findViewById(R.id.tVTapAndHold);
        tVHold = view.findViewById(R.id.tVHold);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.bWipeNow:
                showConfirmationDialogue();
                break;
            case R.id.bLockdownNow:
                Runnable runnable = () -> HidingUtil.lockdownNow(context);
                new Thread(runnable).start();
                ((Activity) context).finishAndRemoveTask();
                break;
        }
        return true;
    }

    private void showConfirmationDialogue() {
        new AlertDialog.Builder(context)
                .setTitle("Caution")
                .setMessage("Do you really want to wipe all apps and data from your work profile?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> Util.getDevicePolicyManager(context).wipeData(DevicePolicyManager.WIPE_SILENTLY))
                .setNegativeButton(android.R.string.no, null).show();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.bHelpLockdown:
                LayoutInflater inflater;
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.slide_ending_lockdown, null);
                View bottomSpace = layout.findViewById(R.id.bottomSpace1);
                layout.removeView(bottomSpace);
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(context).
                                setPositiveButton(android.R.string.yes, (dialog, which) -> dialog.dismiss()).
                                setView(layout);

                builder.create().show();
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Button button = (Button) view;
        switch (motionEvent.getAction()) {
            case ACTION_DOWN:
                tvTapAndHold.setVisibility(View.GONE);
                tVHold.setVisibility(View.VISIBLE);
                break;
            case ACTION_UP:
                tVHold.setVisibility(View.GONE);
                tvTapAndHold.setVisibility(View.VISIBLE);
            default:
        }
        return false;
    }
}
