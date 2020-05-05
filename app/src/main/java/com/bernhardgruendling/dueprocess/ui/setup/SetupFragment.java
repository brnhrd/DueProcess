package com.bernhardgruendling.dueprocess.ui.setup;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bernhardgruendling.dueprocess.DueProcessDeviceAdminReceiver;
import com.bernhardgruendling.dueprocess.R;


public class SetupFragment extends Fragment implements View.OnClickListener {
    public static final String FRAGMENT_TAG = "SetupFragment";
    private static final int REQUEST_PROVISION_MANAGED_PROFILE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.buttonContinue).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonContinue: {
                provisionManagedProfile();
                break;
            }
        }
    }

    private void provisionManagedProfile() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION, false);
        ComponentName component = new ComponentName(activity,
                DueProcessDeviceAdminReceiver.class.getName());
        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                component);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PROVISION_MANAGED_PROFILE);
        } else {
            Toast.makeText(activity, "Device provisioning is not enabled. Stopping.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PROVISION_MANAGED_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(FRAGMENT_TAG, "Provisioning OK");
                showPostSetupFragment();
            } else {
                Toast.makeText(getActivity(), "Provisioning failed.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showPostSetupFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container,
                new PostSetupFragment(),
                PostSetupFragment.FRAGMENT_TAG).commit();
    }

}
