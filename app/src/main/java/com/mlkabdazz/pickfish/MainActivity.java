package com.mlkabdazz.pickfish;

import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import github.com.st235.lib_expandablebottombar.ExpandableBottomBar;
import github.com.st235.lib_expandablebottombar.Menu;
import github.com.st235.lib_expandablebottombar.MenuItemDescriptor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    ExpandableBottomBar expandableBottomBar;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expandableBottomBar = findViewById(R.id.expandable_bottom_bar);
        menu = expandableBottomBar.getMenu();

        menu.add(
                new MenuItemDescriptor.Builder(this, R.id.icon_fish_gray, R.drawable.ic_fish_gray, R.string.realtime, Color.GRAY).build()
        );
        menu.add(
                new MenuItemDescriptor.Builder(this, R.id.icon_gallery, R.drawable.ic_gallery, R.string.gallery, Color.GRAY).build()
        );

        menu.add(
                new MenuItemDescriptor.Builder(this, R.id.icon_information, R.drawable.ic_information, R.string.information, Color.GRAY).build()
        );


        expandableBottomBar.setOnItemReselectedListener((view, item, byUser) -> {
            Log.d(TAG, "selected: " + item.toString());
            return null;
        });

        expandableBottomBar.setOnItemReselectedListener((view, item, byUser) -> {
            Log.d(TAG, "reselected: " + item.toString());
            return null;
        });

    }
}