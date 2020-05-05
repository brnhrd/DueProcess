package com.bernhardgruendling.dueprocess.ui.management;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bernhardgruendling.dueprocess.R;
import com.bernhardgruendling.dueprocess.AppSettings;
import com.bernhardgruendling.dueprocess.ui.MainActivity;
import com.bernhardgruendling.dueprocess.ui.PatternUnlock;
import com.bernhardgruendling.dueprocess.ui.PatternUnlockListener;

import java.util.ArrayList;

public class CodeConfigActivity extends AppCompatActivity implements PatternUnlockListener.MyCodeListener {
    public static final String FRAGMENT_TAG = "CodeConfigActivity";
    public static final int PATTERN_MIN_LENGTH = 4;
    private Context context = this;
    TextView tVCodeLengthInfo;
    private TextView tVCodeConfigMessage;
    private ArrayList<Integer> setCode;
    private PatternUnlock patternUnlock;
    private ImageButton bHelpCorners;
    private int defaultThemeColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_code_config);
        MainActivity.initTheme(this);

        this.tVCodeLengthInfo = findViewById(R.id.tVCodeLengthInfo);
        this.tVCodeConfigMessage = findViewById(R.id.tVCodeConfigMessage);
        patternUnlock = new PatternUnlock(context, new ArrayList<>());
        patternUnlock.initSetupOverlay(false, this);
        patternUnlock.startPulseAnimation();
        defaultThemeColor = tVCodeLengthInfo.getCurrentTextColor();
        bHelpCorners = findViewById(R.id.bHelpCorners);
        bHelpCorners.setOnClickListener(view -> patternUnlock.startPulseAnimation());

        setResult(1);
    }

    @Override
    public void onCodeInputFinished(ArrayList<Integer> inputCode) {
        if (setCode == null && inputCode.size() < PATTERN_MIN_LENGTH) {
            tVCodeConfigMessage.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            tVCodeConfigMessage.setText("Your pattern should consist of at least 4 points.");
        } else if (setCode == null) {
            setCode = new ArrayList<>(inputCode);
            tVCodeConfigMessage.setTextColor(defaultThemeColor);
            tVCodeConfigMessage.setText("Repeat your pattern one more time.");
        } else if (inputCode.equals(setCode)) {
            patternUnlock.clearOverlays();
            AppSettings appSettings = new AppSettings(context);
            appSettings.setUnlockCode(inputCode);
            tVCodeConfigMessage.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            tVCodeConfigMessage.setText("Your pattern is set!");
            TextView tVCodeInstructions = findViewById(R.id.tVCodeInstructions);
            tVCodeInstructions.setVisibility(View.GONE);
            bHelpCorners.setVisibility(View.GONE);
            Button bDone = findViewById(R.id.bDone);
            bDone.setVisibility(View.VISIBLE);
            bDone.setOnClickListener(view -> this.finish());
        } else {
            setCode = null;
            tVCodeConfigMessage.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            tVCodeConfigMessage.setText("Your input did not match. Try again!");
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        patternUnlock.clearOverlays();
    }

    @Override
    public void onCodeInputProgress(ArrayList<Integer> inputCode) {
        tVCodeLengthInfo.setText("" + inputCode.size());
        if (inputCode.size()>=PATTERN_MIN_LENGTH) {
            tVCodeLengthInfo.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        }
    }

    @Override
    public void onCodeInputStarted() {
        tVCodeLengthInfo.setText("0");
        tVCodeLengthInfo.setTextColor(defaultThemeColor);
        tVCodeConfigMessage.setTextColor(defaultThemeColor);
        tVCodeConfigMessage.setText("Release your finger when you are done.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setResult(0);
        finish();
    }
}
