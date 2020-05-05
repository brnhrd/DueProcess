package com.bernhardgruendling.dueprocess.ui.intro;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bernhardgruendling.dueprocess.AppSettings;
import com.bernhardgruendling.dueprocess.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(new SliderPage("Welcome", "You are now in the management app. It resides in the work profile of your phone. It is recommended to uninstall the setup app from your personal profile.", R.drawable.slide_launcher, ContextCompat.getColor(this, R.color.colorPrimaryDark))));
        addSlide(SampleSlideFragment.newInstance(R.layout.slide_separate_challenge, R.id.bSetSepChallenge));
        addSlide(AppIntroFragment.newInstance(new SliderPage("Lockdown", "Lockdown mode hides all your sensitive apps, which are stored in your work profile.", R.drawable.slide_lockdown, ContextCompat.getColor(this, R.color.colorPrimaryDark))));
        addSlide(AppIntroFragment.newInstance(new SliderPage("Manage apps", "You can choose which apps should be hidden in lockdown mode, by marking them sensitive in the \"Manage apps\" screen", R.drawable.slide_mark_sensitive, ContextCompat.getColor(this, R.color.colorPrimaryDark))));
        addSlide(AppIntroFragment.newInstance(new SliderPage("Triggering Lockdown", "You can trigger a lockdown or a wipe of the work profile in the app or by entering the wrong PIN/pattern/password on the lockscreen.\nConfigure this behaviour in \"Settings\".", R.drawable.slide_triggers, ContextCompat.getColor(this, R.color.colorPrimaryDark))));
        addSlide(AppIntroFragment.newInstance(new SliderPage("Hidden management app", "The icon of the management app can also be hidden when you trigger lockdown, which makes it inaccessible.\nConfigure this in \"Settings\".", R.drawable.slide_hidden_launchericon, ContextCompat.getColor(this, R.color.colorPrimaryDark))));
        addSlide(SampleSlideFragment.newInstance(R.layout.slide_ending_lockdown, 0));
        addSlide(SampleSlideFragment.newInstance(R.layout.slide_overlay_permission, R.id.bGrantPermission));
        addSlide(SampleSlideFragment.newInstance(R.layout.slide_pattern_setup, R.id.bSetPattern));
        addSlide(AppIntroFragment.newInstance(new SliderPage("Completed!", "You went through all steps to setup the application. When you tap done, you will be greeted by the secret lockscreen you just set up, where you have to enter your secret pattern.", R.drawable.ic_done_all, ContextCompat.getColor(this, R.color.colorPrimaryDark))));

        //TODO remove notification for displaying over apps


        AppSettings appSettings = new AppSettings(this);
        showSkipButton(!appSettings.isFirstIntroRun());
        setProgressButtonEnabled(true);
        setColorTransitionsEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }


    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        AppSettings appSettings = new AppSettings(this);
        appSettings.setFirstIntroRunDone();
        finish();
    }
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

}
