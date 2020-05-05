package com.bernhardgruendling.dueprocess.ui.management;

import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import com.bernhardgruendling.dueprocess.AppSettings;
import com.bernhardgruendling.dueprocess.R;
import com.bernhardgruendling.dueprocess.util.Util;

@SuppressWarnings("ConstantConditions")
public class ConfigFragment extends PreferenceFragmentCompat {
    private static final String TAG = "ConfigFragment";
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        setEditTextSummaryProvider(AppSettings.ORG_NAME);
        setEditTextSummaryProvider(AppSettings.LOCKSCREEN_DESCRIPTION);

        findPreference(AppSettings.ORG_NAME).setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
        findPreference(AppSettings.PASSWORD_FAILS_LOCKDOWN).setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
        findPreference(AppSettings.PASSWORD_FAILS_WIPE).setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
        findPreference(AppSettings.WIPE_TRIGGER).setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
        findPreference(AppSettings.HIDE_LAUNCHER_ICON).setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
        findPreference(AppSettings.SHOW_LAUNCHER_ICON).setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
        findPreference(AppSettings.THIRD_PARTY_SOURCES).setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());
        findPreference(AppSettings.LOCK_NOW_PROFILE).setOnPreferenceChangeListener(new MyOnPreferenceChangeListener());

        makePasswordFailsLogic(((SeekBarPreference) findPreference(AppSettings.PASSWORD_FAILS_WIPE)).getValue());
    }

    private void setEditTextSummaryProvider(String prefKey) {
        EditTextPreference preference = findPreference(prefKey);
        preference.setSummaryProvider(new MySummaryProvider(preference.getSummary()));
    }

    private static class MySummaryProvider implements Preference.SummaryProvider<EditTextPreference> {
        private CharSequence originalSummary;

        MySummaryProvider(CharSequence summary) {
            originalSummary = summary;
        }

        @Override
        public CharSequence provideSummary(EditTextPreference preference) {
            String currentValue = preference.getText();
            if (currentValue == null || currentValue.equals("")) {
                return (String.format(originalSummary.toString(), "not set"));
            } else {
                return (String.format(originalSummary.toString(), currentValue));
            }
        }
    }

    private class MyOnPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case AppSettings.THIRD_PARTY_SOURCES:
                    if (!(boolean) newValue) {
                        Util.getDevicePolicyManager(context).addUserRestriction(Util.getAdminComponentName(context), UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
                    } else {
                        Util.getDevicePolicyManager(context).clearUserRestriction(Util.getAdminComponentName(context), UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
                    }
                    break;
                case AppSettings.ORG_NAME:
                    Util.getDevicePolicyManager(context).setOrganizationName(Util.getAdminComponentName(context), (String) newValue);
                    Util.getDevicePolicyManager(context).setProfileName(Util.getAdminComponentName(context), (String) newValue);
                    break;
                case AppSettings.PASSWORD_FAILS_LOCKDOWN:
                    if ((int) newValue >= ((SeekBarPreference) findPreference(AppSettings.PASSWORD_FAILS_WIPE)).getValue()) {
                        ((SeekBarPreference) findPreference(AppSettings.PASSWORD_FAILS_WIPE)).setValue((int) newValue + 1);
                    }
                    break;
                case AppSettings.PASSWORD_FAILS_WIPE:
                    makePasswordFailsLogic((int) newValue);
                    break;
                case AppSettings.WIPE_TRIGGER:
                    ((SeekBarPreference) findPreference(AppSettings.PASSWORD_FAILS_WIPE)).setValue(((SeekBarPreference) findPreference(AppSettings.PASSWORD_FAILS_LOCKDOWN)).getValue() + 1);
                    ((SeekBarPreference) findPreference(AppSettings.PASSWORD_FAILS_LOCKDOWN)).setEnabled(true);
                    break;
                case AppSettings.HIDE_LAUNCHER_ICON:
                    ((SwitchPreference) findPreference(AppSettings.SHOW_LAUNCHER_ICON)).setChecked((boolean) newValue);
                    break;
                case AppSettings.SHOW_LAUNCHER_ICON:
                    ((SwitchPreference) findPreference(AppSettings.HIDE_LAUNCHER_ICON)).setChecked((boolean) newValue);
                    break;
                case AppSettings.LOCK_NOW_PROFILE:
                    if (!(boolean) newValue) {
                        ((SwitchPreference) findPreference(AppSettings.LOCK_NOW_PROFILE_KEY_EVICT)).setChecked((boolean) newValue);
                    }
            }
            return true;
        }
    }

    private void makePasswordFailsLogic(int newValue) {
        if (newValue == 1) {
            ((SeekBarPreference) findPreference(AppSettings.PASSWORD_FAILS_LOCKDOWN)).setValue(1);
            findPreference(AppSettings.PASSWORD_FAILS_LOCKDOWN).setEnabled(false);
        } else if (newValue <= ((SeekBarPreference) findPreference(AppSettings.PASSWORD_FAILS_LOCKDOWN)).getValue()) {
            ((SeekBarPreference) findPreference(AppSettings.PASSWORD_FAILS_LOCKDOWN)).setValue(newValue - 1);
            findPreference(AppSettings.PASSWORD_FAILS_LOCKDOWN).setEnabled(true);
        } else {
            findPreference(AppSettings.PASSWORD_FAILS_LOCKDOWN).setEnabled(true);
        }
    }
}
