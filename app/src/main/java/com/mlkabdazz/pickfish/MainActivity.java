package com.mlkabdazz.pickfish;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.matadesigns.spotlight.SpotlightBuilder;
import com.matadesigns.spotlight.abstraction.SpotlightListener;
import com.mlkabdazz.pickfish.env.InferenceUtils;
import com.mlkabdazz.pickfish.env.Logger;
import com.mlkabdazz.pickfish.env.Utils;
import com.mlkabdazz.pickfish.tflite.Classifier;
import com.mlkabdazz.pickfish.tflite.YoloV4Classifier;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

import github.com.st235.lib_expandablebottombar.ExpandableBottomBar;
import github.com.st235.lib_expandablebottombar.Menu;
import github.com.st235.lib_expandablebottombar.MenuItemDescriptor;

public class MainActivity extends AppCompatActivity {

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.7f;
    public static final int TF_OD_API_INPUT_SIZE = 416;
    private static final int RESULT_LOAD_IMAGE = 1;
//    private static final String TF_OD_API_MODEL_FILE = "yolov4-basic-fp16.tflite";
    private static final String TF_OD_API_MODEL_FILE = "yolov4-tiny-fp16.tflite";
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final Logger LOGGER = new Logger();
    Uri imageUri;
    Bitmap cropBitmap;
    Bitmap sourceBitmap;
    SpotlightBuilder builder;
    ExpandableBottomBar expandableBottomBar;
    MenuItemDescriptor midFishGray;
    MenuItemDescriptor midGallery;
    MenuItemDescriptor midInformation;
    MenuItemDescriptor midHelp;
    Menu menu;
    ImageView imageView;
    private Classifier detector;

    private void initBox() {
        try {
            detector = YoloV4Classifier.create(
                    getAssets(),
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_IS_QUANTIZED
            );
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast.makeText(this, "Classifier could not be initialized", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expandableBottomBar = findViewById(R.id.expandable_bottom_bar);
        menu = expandableBottomBar.getMenu();
        midFishGray = new MenuItemDescriptor.Builder(this, R.id.icon_camera, R.drawable.ic_camera, R.string.realtime, Color.GRAY).build();
        midGallery = new MenuItemDescriptor.Builder(this, R.id.icon_gallery, R.drawable.ic_gallery, R.string.gallery, Color.GRAY).build();
        midInformation = new MenuItemDescriptor.Builder(this, R.id.icon_information, R.drawable.ic_info, R.string.about, Color.GRAY).build();
        midHelp = new MenuItemDescriptor.Builder(this, R.id.icon_help, R.drawable.ic_help, R.string.help, Color.GRAY).build();
        imageView = findViewById(R.id.iv_image);

        menu.add(midFishGray);
        menu.add(midGallery);
        menu.add(midInformation);
        menu.add(midHelp);

        expandableBottomBar.setOnItemSelectedListener((view, item, byUser) -> {
            int selectedId = view.getId();
            if (selectedId == R.id.icon_information) {
                dialogInformation();
            }

            if (selectedId == R.id.icon_gallery) {
                openGallery();
            }

            if (selectedId == R.id.icon_camera) {
                openCamera();
            }

            if (selectedId == R.id.icon_help) {
                spotlight();
            }
            return null;
        });

        expandableBottomBar.setOnItemReselectedListener((view, item, byUser) -> {
            int selectedId = view.getId();
            if (selectedId == R.id.icon_information) {
                dialogInformation();
            }
            if (selectedId == R.id.icon_gallery) {
                openGallery();
            }

            if (selectedId == R.id.icon_camera) {
                openCamera();
            }
            if (selectedId == R.id.icon_help) {
                spotlight();
            }
            return null;
        });

        initBox();
    }

    /**
     * Spotlight menu's
     */
    private void spotlight() {
        View realtime = findViewById(R.id.icon_camera);
        View gallery = findViewById(R.id.icon_gallery);
        View information = findViewById(R.id.icon_information);

        builder = new SpotlightBuilder(this)
                .setInset(20)
                .setTargetView(realtime)
                .setTitle("Realtime")
                .setDescription("Realtime Prediction")
                .setListener(new SpotlightListener() {
                    @Override
                    public void onEnd(@Nullable View targetView) {
                        if (realtime.equals(targetView)) {
                            builder.setTitle("Gallery");
                            builder.setDescription("Prediction from Images");
                            builder.setTargetView(gallery);
                        } else if (gallery.equals(targetView)) {
                            builder.setTitle("About");
                            builder.setDescription("Information about model and apps");
                            builder.setTargetView(information);
                        } else if (information.equals(targetView)) {
                            return;
                        }
                        builder.build().startSpotlight();
                    }

                    @Override
                    public void onStart(@Nullable View view) {
                    }
                });
        builder.build().startSpotlight();
    }

    /**
     * open gallery
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    private void openCamera() {
        startActivity(new Intent(this, DetectorActivity.class));
    }

    private void dialogInformation() {
        new InformationDialog().show(getSupportFragmentManager(), "Dialog Information opened");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMAGE) {
            imageUri = data.getData();
            if (imageUri != null) {
                try {
                    sourceBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    cropBitmap = Utils.processBitmap(sourceBitmap, TF_OD_API_INPUT_SIZE);
//                    Bitmap x = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Handler handler = new Handler();
                    new Thread(() -> {
                        final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap);
                        handler.post(() -> handleResult(cropBitmap, results));
                    }).start();

//                    imageView.setImageBitmap(sourceBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * handle result from model and do some drawing process.
     */
    private void handleResult(Bitmap bitmap, List<Classifier.Recognition> results) {
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.0f);
        String text = "";

        String fishCondition = InferenceUtils.getInferences(results);

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {

                if (result.getTitle().equalsIgnoreCase("Fish")) {
                    paint.setColor(Color.GREEN);
                    text = result.getTitle() + ": " + fishCondition;
                } else if (result.getTitle().contains("Eyes")) {
                    paint.setColor(Color.YELLOW);
                    text = result.getTitle() + "(" + (int) (result.getConfidence() * 100) + "%)";
                } else if (result.getTitle().contains("Skins")) {
                    paint.setColor(Color.RED);
                    text = result.getTitle() + "(" + (int) (result.getConfidence() * 100) + "%)";
                }

                RectF tagSize = new RectF(0, 0, 0, 0);
                float margin = (location.width() - tagSize.width()) / 2.0f;
                if (margin < 0F) margin = 0F;

                canvas.drawRect(location, paint);
                canvas.drawText(text, location.left + margin, location.top - tagSize.height(), paint);
            }
        }
        imageView.setImageBitmap(bitmap);
    }

//    /**
//     * Inference for get final condition of fish
//     *
//     * @return Fresh, Medium, and Spoiled
//     */
//    private String getInferences(List<Classifier.Recognition> recognitions) {
//        String eyesCondition = "";
//        String skinsCondition = "";
//        String[] eyes = {"fresh_eyes", "normal_eyes", "spoil_eyes"};
//        String[] skins = {"fresh_skins", "normal_skins", "spoil_skins"};
//
//        for (Classifier.Recognition recognition : recognitions) {
//            if (recognition.getTitle().contains("Eyes")) {
//                eyesCondition = recognition.getTitle();
//            } else if (recognition.getTitle().contains("Skins")) {
//                skinsCondition = recognition.getTitle();
//            }
//        }
//
//        if ((eyesCondition.equalsIgnoreCase(eyes[0]) && skinsCondition.equalsIgnoreCase(skins[0]))) {
//            return "Fresh";
//        } else if (
//                (eyesCondition.equalsIgnoreCase(eyes[1]) && skinsCondition.equalsIgnoreCase(skins[0])) ||
//                        (eyesCondition.equalsIgnoreCase(eyes[0]) && skinsCondition.equalsIgnoreCase(skins[1])) ||
//                        (eyesCondition.equalsIgnoreCase(eyes[1]) && skinsCondition.equalsIgnoreCase(skins[1])) ||
//                        (eyesCondition.equalsIgnoreCase(eyes[2]) && skinsCondition.equalsIgnoreCase(skins[0])) ||
//                        (eyesCondition.equalsIgnoreCase(eyes[2]) && skinsCondition.equalsIgnoreCase(skins[1])) ||
//                        (eyesCondition.equalsIgnoreCase(eyes[0]) && skinsCondition.equalsIgnoreCase(skins[2]))) {
//            return "Medium";
//        } else if (
//                (eyesCondition.equalsIgnoreCase(eyes[2]) && skinsCondition.equalsIgnoreCase(skins[2])) ||
//                        (eyesCondition.equalsIgnoreCase(eyes[1]) && skinsCondition.equalsIgnoreCase(skins[2]))) {
//            return "Spoil";
//        } else {
//            if (eyesCondition.isEmpty()) {
//                return "Fail Inference - Eyes Not Found";
//            } else if (skinsCondition.isEmpty()) {
//                return "Fail Inference - Skins Not Found";
//            } else {
//                return "Fail Inference";
//            }
//        }
//    }

}
