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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.matadesigns.spotlight.SpotlightBuilder;
import com.matadesigns.spotlight.abstraction.SpotlightListener;
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

    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    public static final int TF_OD_API_INPUT_SIZE = 416;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TF_OD_API_MODEL_FILE = "yolov4-basic-416-fp16.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/classes.txt";
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
    Menu menu;
    ImageView imageView;
    TextView tvPlaceholder;
    private Classifier detector;

    private void initBox() {
        try {
            detector = YoloV4Classifier.create(
                    getAssets(),
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
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
        midInformation = new MenuItemDescriptor.Builder(this, R.id.icon_information, R.drawable.ic_info, R.string.information, Color.GRAY).build();
        imageView = findViewById(R.id.iv_image);
        tvPlaceholder = findViewById(R.id.tv_placeholder);

        menu.add(midFishGray);
        menu.add(midGallery);
        menu.add(midInformation);

        expandableBottomBar.setOnItemSelectedListener((view, item, byUser) -> {
            int selectedId = view.getId();
            if (selectedId == R.id.icon_information) {
                spotlight();
            }

            if (selectedId == R.id.icon_gallery) {
                openGallery();
            }

            if (selectedId == R.id.icon_camera) {
                openCamera();
            }
            return null;
        });

        expandableBottomBar.setOnItemReselectedListener((view, item, byUser) -> {
            int selectedId = view.getId();
            if (selectedId == R.id.icon_information) {
                spotlight();
            }
            if (selectedId == R.id.icon_gallery) {
                openGallery();
            }

            if (selectedId == R.id.icon_camera) {
                openCamera();
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
                .setDescription("Do realtime prediction")
                .setListener(new SpotlightListener() {
                    @Override
                    public void onEnd(@Nullable View targetView) {
                        if (realtime.equals(targetView)) {
                            builder.setTitle("Gallery");
                            builder.setDescription("Do prediction from pict in gallery");
                            builder.setTargetView(gallery);
                        } else if (gallery.equals(targetView)) {
                            builder.setTitle("Information");
                            builder.setDescription("Every information about decs and version in here!");
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
//        Toast.makeText(this, "Not Ready Yet :)", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, DetectorActivity.class));
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
     *
     * @param bitmap
     * @param results
     */
    private void handleResult(Bitmap bitmap, List<Classifier.Recognition> results) {
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.0f);
        String text = "";

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {

                if (result.getTitle().equalsIgnoreCase("Fish")) {
                    paint.setColor(Color.GREEN);
                } else if (result.getTitle().contains("Eyes")) {
                    paint.setColor(Color.YELLOW);
                } else if (result.getTitle().contains("Skins")) {
                    paint.setColor(Color.RED);
                }

                text = result.getTitle() + "(" + (int) (result.getConfidence() * 100) + "%)";

                RectF tagSize = new RectF(0, 0, 0, 0);
                float margin = (location.width() - tagSize.width()) / 2.0f;
                if (margin < 0F) margin = 0F;

                canvas.drawRect(location, paint);
                canvas.drawText(text, location.left + margin, location.top - tagSize.height(), paint);
            }
        }
        imageView.setImageBitmap(bitmap);
    }

}
