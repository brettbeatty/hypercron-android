package com.hypercron;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by brett on 11/11/16.
 *
 */
public class Project {
    private ImageView imageOverlay = null;
    private int imageQuantity = 0;
    private String latest = null;
    private String name;

    public Project(String name) {

        this.name = name;
    }

    public ImageView getImageOverlay() {

        return this.imageOverlay;
    }

    public int getImageQuantity() {

        return this.imageQuantity;
    }

    public String getLatest() {

        return this.latest;
    }

    public String getName() {

        return this.name;
    }

    public void setImageOverlay(ImageView imageOverlay) {

        this.imageOverlay = imageOverlay;
    }

    public void setImageQuantity(int imageQuantity) {

        this.imageQuantity = imageQuantity;
    }

    public void setLatest(String latest) {

        this.latest = latest;
    }

    public void setName(String name) {

        this.name = name;
    }
}
