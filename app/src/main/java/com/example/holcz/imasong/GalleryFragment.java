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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GalleryFragment extends Fragment {

    static final String TAG = "GalleryFragment";

    private GridView gridView;
    private GridViewAdapter gridAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.gallery_fragment, container, false);

        gridView = rootView.findViewById(R.id.gallery_grid_view);
        gridAdapter = new GridViewAdapter(this.getContext(), inflater, R.layout.image_layout, loadImages());
        gridView.setAdapter(gridAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateImages();
    }

    private ArrayList<ImageItem> loadImages() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        File storageDir = this.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        for (File imageFile : storageDir.listFiles()) {
            ImageItem imageItem = loadImage(imageFile);
            if (imageItem != null) {
                imageItems.add(imageItem);
            }
        }
        return imageItems;
    }

    private void updateImages() {
        File storageDir = this.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        for (File imageFile : storageDir.listFiles()) {
            if (!gridAdapter.contains(imageFile.getAbsolutePath())) {
registerForContextMenu();                gridAdapter.add(loadImage(imageFile));
            }
        }
    }

    private ImageItem loadImage(File imageFile) {
        ImageItem loadedImageItem = null;
        // Get the dimensions of the View
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
        if (bitmap != null) {
            String song = getSongInfoFromExifData(imageFile.getAbsoluteFile());
            loadedImageItem = new ImageItem(bitmap, song, imageFile.getAbsolutePath());
        }
        return loadedImageItem;
    }

    private String getSongInfoFromExifData(File imageFile) {
        try {
            ExifInterface exifInterface = new ExifInterface(imageFile.getAbsolutePath());
            String song = exifInterface.getAttribute(ExifInterface.TAG_USER_COMMENT);
            if (song != null) {
                return song;
            }
        } catch (IOException e) {
            Log.e(GalleryFragment.TAG, e.getMessage(), e);
        }
        return "Unrecognized";
    }

}

class GridViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private List<ImageItem> data;
    private LayoutInflater layoutInflater;
    private Set<String> imageItemSet;

    public GridViewAdapter(Context context, LayoutInflater layoutInflater, int layoutResourceId, List<ImageItem> data) {
        super(context, layoutResourceId, data);
        this.layoutInflater = layoutInflater;
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.imageItemSet = initImageItemSet(data);
    }

    private Set<String> initImageItemSet(List<ImageItem> data) {
        final Set<String> imageItemSet = new HashSet<>();
        for (ImageItem imageItem : data) {
            imageItemSet.add(imageItem.getImagePath());
        }
        return imageItemSet;
    }

    public void add(@Nullable ImageItem imageItem) {
        if (imageItem != null ) {
            this.data.add(imageItem);
            this.imageItemSet.add(imageItem.getImagePath());
            super.add(imageItem);
        }
    }

    public boolean contains(ImageItem imageItem) {
        return this.contains(imageItem.getImagePath());
    }

    public boolean contains(String imagePath) {
        return imageItemSet.contains(imagePath);
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
    private final Bitmap image;
    private final String song;
    private final String imagePath;

    public ImageItem(Bitmap image, String song, String imagePath) {
        this.image = image;
        this.song = song;
        this.imagePath = imagePath;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getSong() {
        return song;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public int hashCode() {
        return imagePath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(this.getClass())) {
            return this.imagePath.equals(((ImageItem) obj).imagePath);
        }
        return false;
    }
}