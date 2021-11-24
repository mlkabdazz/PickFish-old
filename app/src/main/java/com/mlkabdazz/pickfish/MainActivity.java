package com.mlkabdazz.pickfish;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.matadesigns.spotlight.SpotlightBuilder;
import com.matadesigns.spotlight.SpotlightView;
import com.matadesigns.spotlight.abstraction.SpotlightListener;

import org.jetbrains.annotations.Nullable;

import github.com.st235.lib_expandablebottombar.ExpandableBottomBar;
import github.com.st235.lib_expandablebottombar.Menu;
import github.com.st235.lib_expandablebottombar.MenuItemDescriptor;

public class MainActivity extends AppCompatActivity {

    ExpandableBottomBar expandableBottomBar;
    MenuItemDescriptor midFishGray;
    MenuItemDescriptor midGallery;
    MenuItemDescriptor midInformation;
    Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expandableBottomBar = findViewById(R.id.expandable_bottom_bar);
        menu = expandableBottomBar.getMenu();
        midFishGray = new MenuItemDescriptor.Builder(this, R.id.icon_fish_gray, R.drawable.ic_fish_gray, R.string.realtime, Color.GRAY).build();
        midGallery = new MenuItemDescriptor.Builder(this, R.id.icon_gallery, R.drawable.ic_gallery, R.string.gallery, Color.GRAY).build();
        midInformation = new MenuItemDescriptor.Builder(this, R.id.icon_information, R.drawable.ic_information, R.string.information, Color.GRAY).build();

        menu.add(midFishGray);
        menu.add(midGallery);
        menu.add(midInformation);

        expandableBottomBar.setOnItemSelectedListener((view, item, byUser) -> {
            newSpotlight(view);
            return null;
        });

        expandableBottomBar.setOnItemReselectedListener((view, item, byUser) -> {
            newSpotlight(view);
            return null;
        });
    }

    SpotlightView builder;
    private void newSpotlight(View view) {
        String description;
        String title;
        if (midInformation.getItemId() == view.getId()) {
            view = findViewById(R.id.icon_information);
            title = "Information";
            description = "i'll give you some introduction";

        } else if (midGallery.getItemId() == view.getId()) {
            view = findViewById(R.id.icon_gallery);
            title = "Gallery";
            description = "Do prediction from pict in gallery";
        } else if (midFishGray.getItemId() == view.getId()) {
            view = findViewById(R.id.icon_fish_gray);
            title = "Realtime";
            description = "Do realtime prediction";
        }else{
            return;
        }

        builder = new SpotlightBuilder(this)
                .setInset(20)
                .setTargetView(view)
                .setTitle(title)
                .setDescription(description)
                .setListener(new SpotlightListener() {
                    @Override
                    public void onEnd(@Nullable View view) {
                        builder.setTitle("");
                    }

                    @Override
                    public void onStart(@Nullable View view) {

                    }
                })
                .build();
        builder.startSpotlight();
    }

}