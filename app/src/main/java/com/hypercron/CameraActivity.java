package com.hypercron;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

public class CameraActivity extends AppCompatActivity {
    private static final String HYPERCRON = "Hypercron";
    private static Camera camera = null;
    private static ProjectIndex projectIndex = new ProjectIndex();
    private static Vector<Project> projects = projectIndex.getProjects();
    private static WindowManager windowManager = null;
    private static CameraActivity cameraActivity;
    private static Camera.PictureCallback picture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            int currentProject = mViewPager.getCurrentItem();

            if (currentProject >= projects.size()) return;

            //  get to correct directory
            Project current = projects.elementAt(currentProject);
            String filename = String.valueOf(new Date().getTime()) + ".jpg";
            File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            picturesDirectory = new File(picturesDirectory, HYPERCRON);
            File albumDirectory = new File(picturesDirectory, current.getName());
            if (!albumDirectory.exists() && !albumDirectory.mkdirs()) {

                Log.e("file_write", "could not make album for project " + current.getName());
                return;
            }
            File pictureFile = new File(albumDirectory, filename);

            //  rotate image
            Bitmap original = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            switch (windowManager.getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                    matrix.postRotate(90);
                    break;
                case Surface.ROTATION_90:
                    matrix.postRotate(0);
                    break;
                case Surface.ROTATION_180:
                    matrix.postRotate(270);
                    break;
                case Surface.ROTATION_270:
                    matrix.postRotate(180);
                    break;
            }
            Bitmap image = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
            original.recycle();
            ByteArrayOutputStream outputStream_byteArray = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 10, outputStream_byteArray);
            data = outputStream_byteArray.toByteArray();

            current.getImageOverlay().setImageBitmap(image);

//            Log.d("current", String.valueOf(currentProject));
//            View view = mViewPager.getChildAt(currentProject);
//            if (view != null) {
//                Log.d("view type", view.getClass().getName());
//                view = view.findViewById(R.id.image_overlay);
//                if (view != null) {
//                    Log.d("view type", view.getClass().getName());
//                }
//            }
//            ImageView imageOverlay = (ImageView) mViewPager.getChildAt(currentProject).findViewById(R.id.image_overlay);
//            Log.d("overlay", String.valueOf(imageOverlay != null));
//            if (imageOverlay != null) imageOverlay.setImageBitmap(image);

//            Log.d("image overlay", String.valueOf(cameraActivity.getWindow().findViewById(R.id.image_overlay) == null));
//            ImageView imageOverlay = (ImageView) cameraActivity.getWindow().findViewById(R.id.image_overlay);
//            if (imageOverlay != null) imageOverlay.setImageBitmap(image);

//            if (current.getImageOverlay() != null) current.getImageOverlay().setImageBitmap(image);
//
//            if (imageOverlay != null) imageOverlay.setImageBitmap(image);
//            int position = mViewPager.getCurrentItem();
//            Log.d("debug", "position is: " + String.valueOf(position));
//            Fragment fragment = mSectionsPagerAdapter.getItem(position);
//            Log.d("debug", "fragment id: " + String.valueOf(fragment.getId()));
//            View view = fragment.getView();
//            Log.d("debug", "Fragment view is null: " + String.valueOf(view == null));
//            if (view == null) return;
//            ImageView imageOverlay = (ImageView) view.findViewById(R.id.image_overlay);
//            Log.d("debug", String.valueOf(imageOverlay == null));

            try {
                FileOutputStream outputStream = new FileOutputStream(pictureFile);
                outputStream.write(data);
                outputStream.close();
                projectIndex.addImage(current.getName(), filename);
                Log.i("debug", "finished taking picture");
            } catch (FileNotFoundException e) {
                Log.e("file_write", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.e("file_write", "Error accessing file: " + e.getMessage());
            }
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
    };

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private static SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private static ViewPager mViewPager;

    private void flashWhite() {
        final FrameLayout whiteFlash = (FrameLayout) findViewById(R.id.white_flash);
        if (whiteFlash == null) return;
        whiteFlash.setAlpha(1);
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation transformation) {

                if (interpolatedTime == 1) whiteFlash.setAlpha(0);
                else whiteFlash.setAlpha(1 - interpolatedTime);
            }
        };
        animation.setDuration(100);
        whiteFlash.startAnimation(animation);
    }

    public void clickTextView(View view) {

        Project project = projects.elementAt(mViewPager.getCurrentItem());
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra("name", project.getName());
        intent.putExtra("image_quantity", project.getImageQuantity());
        intent.putExtra("last_image", project.getLatest());
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String new_name = data.getStringExtra("new_name");
        if (new_name.length() > 0) {
            projectIndex.rename(projects.elementAt(mViewPager.getCurrentItem()).getName(), new_name);
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraActivity = this;

//        if (projects.size() == 0) projectIndex.addProject("New Project");

        windowManager = getWindowManager();
        ImageButton captureButton = (ImageButton) findViewById(R.id.button_capture);
        if (captureButton != null) captureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Log.i("debug", "starting to take picture");
                flashWhite();
                try {
                    camera.takePicture(null, null, picture);
                } catch (RuntimeException e) {
                    Log.e("runtime", e.getMessage());
                } catch (OutOfMemoryError e) {
                    Toast.makeText(getApplicationContext(), "Experiencing memory issues at the moment", Toast.LENGTH_LONG).show();
                }
            }
        });

        try {
            camera = Camera.open();
        } catch (Exception e) {
            //  Camera is not available
        }

        //  display camera feed
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        if (preview != null) preview.addView(new CameraPreview(this, camera, getWindowManager()));

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
//        Log.d("debug", String.valueOf(imageOverlay != null));

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
//            Log.d("debug", "position: " + String.valueOf(sectionNumber) + ", fragment id: " + String.valueOf(fragment.getId()));
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
//            rootView.requestFocus();

            //  display project title
            final TextView projectTitle = (TextView) rootView.findViewById(R.id.textView_project_title);
            final Project project = projects.elementAt(getArguments().getInt(ARG_SECTION_NUMBER));
            projectTitle.setText(project.getName());

            //  display image overlay
            ImageView imageOverlay = (ImageView) rootView.findViewById(R.id.image_overlay);
//            CameraActivity.imageOverlay = imageOverlay;
            if (imageOverlay != null) imageOverlay.setImageBitmap(BitmapFactory.decodeFile(project.getLatest()));

            project.setImageOverlay(imageOverlay);

//            projects.elementAt(getArguments().getInt(ARG_SECTION_NUMBER)).setImageOverlay(imageOverlay);

//            Log.d("view created", String.valueOf(getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {

            return projects.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return projects.elementAt(position).getName();
        }
    }
}
