package com.example.holcz.imasong;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryFragment extends Fragment {

    static final String TAG = "GalleryFragment";
    private final Map<String, ImageItem> preLoadedImages = new HashMap();

    private GridView gridView;
    private GridViewAdapter gridAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.gallery_fragment, container, false);

        preLoadImages();

        gridView = rootView.findViewById(R.id.gallery_grid_view);
        gridAdapter = new GridViewAdapter(this.getContext(), inflater, R.layout.image_layout, new ArrayList<>(preLoadedImages.values()));
        gridView.setAdapter(gridAdapter);

        return rootView;
    }

    private void preLoadImages() {
        File storageDir = this.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        for (File imageFile : storageDir.listFiles()) {
            if (!preLoadedImages.containsKey(imageFile.getAbsolutePath())) {
                preLoadedImages.put(imageFile.getAbsolutePath(), new ImageItem(imageFile.getAbsolutePath()));
            }
        }
    }
}

class GridViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private List<ImageItem> data;
    private LayoutInflater layoutInflater;

    public GridViewAdapter(Context context, LayoutInflater layoutInflater, int layoutResourceId, List<ImageItem> data) {
        super(context, layoutResourceId, data);
        this.layoutInflater = layoutInflater;
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            row = this.layoutInflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = row.findViewById(R.id.song_text_view);
            holder.image = row.findViewById(R.id.photo_image_view);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        ImageItem item = data.get(position);
        holder.imageTitle.setText(item.getSong());
        holder.image.setImageBitmap(item.getImage());
        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}

class ImageItem {
    private String imageAbsolutePath;
    private Bitmap image;
    private boolean loaded = false;
    private String song;

    public ImageItem(String imageAbsolutePath) {
        this.imageAbsolutePath = imageAbsolutePath;
    }

    public Bitmap getImage() {
        if (!this.loaded) {
            this.image = loadImage(imageAbsolutePath);
        }
        return image;
    }

    public String getSong() {
        return song != null ? song : "Unrecognized";
    }

    private Bitmap loadImage(String path) {
        // Get the dimensions of the View
        File imageFile = new File(path);
        int targetW = 100; // mImageView.getWidth();
        int targetH = 100; //mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
        String song = null;
        if (bitmap != null) {
            try {
                ExifInterface exifInterface = new ExifInterface(imageFile.getAbsolutePath());
                song = exifInterface.getAttribute(ExifInterface.TAG_USER_COMMENT);
                this.song = song != null ? song : "Unrecognized";
            } catch (IOException e) {
                Log.e(GalleryFragment.TAG, e.getMessage(), e);
            }
        }
        this.loaded = true;
        return bitmap;
    }
}