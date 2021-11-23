package com.mlkabdazz.pickfish;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wooplr.spotlight.SpotlightView;
import com.wooplr.spotlight.prefs.PreferencesManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String IB_GENERAL_BUTTON = "ib_general_model";
    private static final String IB_SPECIFIC_BUTTON = "ib_specific_model";
    private static final String IB_GALLERY = "ib_gallery";
    private static final String IB_INFORMATION = "ib_information";
    private SpotlightView spotlight;

    ImageButton ibGeneralButton;
    ImageButton ibSpecificModel;
    ImageButton ibGallery;
    ImageButton ibInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ibGeneralButton = findViewById(R.id.ib_general_model);
        ibSpecificModel = findViewById(R.id.ib_specific_model);
        ibGallery = findViewById(R.id.ib_gallery);
        ibInformation = findViewById(R.id.ib_information);

        ibInformation.setOnClickListener(this);
        ibSpecificModel.setOnClickListener(this);
        ibGeneralButton.setOnClickListener(this);
        ibGallery.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        PreferencesManager preferencesManager = new PreferencesManager(MainActivity.this);
        switch (v.getId()) {
            case R.id.ib_information:
                preferencesManager.resetAll();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    showIntro(ibInformation, IB_INFORMATION, "Information", "UX not ready yet!");
                }, 400);
                break;
            case R.id.ib_general_model:
                preferencesManager.resetAll();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    showIntro(ibGeneralButton, IB_GENERAL_BUTTON, "Coming soon!", "A deep learning model for all or general fish.");
                }, 400);
                break;
            case R.id.ib_specific_model:
                preferencesManager.resetAll();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    showIntro(ibSpecificModel, IB_SPECIFIC_BUTTON, "Coming soon!", "A deep learning model for specific fish");
                }, 400);
                break;
            case R.id.ib_gallery:
                preferencesManager.resetAll();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    showIntro(ibGallery, IB_GALLERY, "Coming soon!", "Select pict of fish from gallery");
                }, 400);
                break;
        }
    }

    private void showIntro(View view, String usageId, String heading, String subHeading) {
        new SpotlightView.Builder(this)
                .introAnimationDuration(100)
                .enableRevealAnimation(true)
                .performClick(true)
                .fadeinTextDuration(400)
                .headingTvColor(Color.parseColor("#eb273f"))
                .headingTvSize(32)
                .headingTvText(heading)
                .subHeadingTvColor(Color.parseColor("#ffffff"))
                .subHeadingTvSize(16)
                .subHeadingTvText(subHeading)
                .maskColor(Color.parseColor("#dc000000"))
                .target(view)
                .lineAnimDuration(400)
                .lineAndArcColor(Color.parseColor("#eb273f"))
                .dismissOnTouch(true)
                .dismissOnBackPress(true)
                .enableDismissAfterShown(true)
                .usageId(usageId)
                .show();
    }

}