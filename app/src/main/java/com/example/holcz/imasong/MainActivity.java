package com.example.holcz.imasong;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";
//    private static final List<Fragment> FRAGMENTS = new ArrayList<>();
    private static final Fragment[] FRAGMENTS = new Fragment[2];

    FragmentAdapterImpl adapter;
    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new FragmentAdapterImpl(getSupportFragmentManager());

        pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);

        initFragments();
    }

    protected void initFragments() {
//        FRAGMENTS.add(new CameraFragment());
//        FRAGMENTS.add(new GalleryFragment());
        FRAGMENTS[0] = new CameraFragment();
        FRAGMENTS[1] = new GalleryFragment();
    }

    class FragmentAdapterImpl extends FragmentPagerAdapter {
        public FragmentAdapterImpl(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
//            return FRAGMENTS.size();
            return FRAGMENTS.length;
        }

        @Override
        public Fragment getItem(int position) {
            return FRAGMENTS[position];
//            return FRAGMENTS.get(position);
        }
    }

    private void requestRecordAudioPermission() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.RECORD_AUDIO)
//                != PackageManagernager.PERMISSION_GRANTED) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.READ_CONTACTS)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//
//                // No explanation needed, we can request the permission.
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_CONTACTS},
//                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // resultTextView of the request.
//            }
//        }
    }
}
