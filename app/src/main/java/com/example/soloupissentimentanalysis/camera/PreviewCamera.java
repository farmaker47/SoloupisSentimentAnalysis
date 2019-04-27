package com.example.soloupissentimentanalysis.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.soloupissentimentanalysis.EditActivity;
import com.example.soloupissentimentanalysis.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PreviewCamera extends Activity {

    public static final int ACTION_TAKE_PHOTO_B = 1;
    public static Activity fa;
    public static boolean launchingVal;
    public static int rot;
    private boolean flashmode;
    private Camera mCamera;
    private int mCurrentCamera;
    private Camera.PictureCallback mPicture;
    private CameraPreview mPreview;
    private FrameLayout mPreviewContainer;
    Camera.ShutterCallback mShutterCallback;

    private Button buttonflash;
    private Button buttonfotopreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(ACTION_TAKE_PHOTO_B);
        setContentView(R.layout.activity_prev_cam);

        buttonflash  = (Button)findViewById(R.id.buttonflash);
        buttonfotopreview = (Button)findViewById(R.id.buttonfotopreview);
        this.mPreviewContainer = (FrameLayout) findViewById(R.id.camerapreview);

        launchingVal = true;
        fa=this;

        if (checkCameraHardware(this)) {
            this.mCurrentCamera = 0;
            this.mCamera = getCameraInstance(this.mCurrentCamera);
            if (this.mCamera != null) {
                initPreview();
                /*captureImage();
                camera.setOnClickListener(new C07421(camera));
                flash.setOnClickListener(new C07442());
                return;*/
            }
            return;
        }

    }
    private void initPreview() {
        rot = getWindowManager().getDefaultDisplay().getRotation();
        mPreview = new CameraPreview (this, this.mCamera);
        mPreviewContainer.removeAllViews();
        mPreviewContainer.addView(mPreview);
    }



/*
    private String getAlbumName() {
        return getString(R.string.album_name);
    }
*/


    protected void onPause() {
        super.onPause();
        if (this.mCamera != null) {
            this.mCamera.release();
            this.mCamera = null;
        }
    }



    static {
        rot = 0;
    }

    public void flashflash(View view) {
        if (this.mCamera != null) {
            try {
                Camera.Parameters param = this.mCamera.getParameters();
                param.setFlashMode(!this.flashmode ? "torch" : "off");
                this.mCamera.setParameters(param);
                this.flashmode = !this.flashmode;
            } catch (Exception e) {
            }
        }
    }


    /////////////////////////////////////////

    public void picturecamm(View view){
        captureImage();
    }

    private void captureImage() {
        this.mCamera.takePicture(this.mShutterCallback, null, this.mPicture);
    }

    public PreviewCamera() {
        this.flashmode = false;
        this.mPicture = new C07453();

    }

    class C07453 implements PictureCallback {
        C07453() {
        }

        public void onPictureTaken(byte[] data, Camera camera) {
            mPreview.initPreview();
            File pictureFile = getOutputMediaFile();
            String TAG = "Camera Activity";
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions.");
                Toast.makeText(getApplicationContext(), "error creating media file", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                mediaScanIntent.setData(Uri.fromFile(pictureFile));
                sendBroadcast(mediaScanIntent);
                Intent nextScreen = new Intent(getApplicationContext(), EditActivity.class);
                nextScreen.putExtra("CameraPath", Environment.getExternalStorageDirectory().toString() + "/" + "Sentiment" + "/" + "IMG_" + "cropped_bitmap" + ".jpg");
                nextScreen.putExtra("Choose", ACTION_TAKE_PHOTO_B);
                startActivity(nextScreen);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e2) {
                Log.d(TAG, "Error accessing file: " + e2.getMessage());
            }
        }
    }

    /////////////////////////////////////////

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature("android.hardware.camera")) {
            return true;
        }
        return false;
    }

    private Camera getCameraInstance(int type) {
        Camera c = null;
        int numberOfCameras = Camera.getNumberOfCameras();
        int i = 0;
        while (i < numberOfCameras) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == type) {
                try {
                    c = Camera.open(i);
                    break;
                } catch (Exception e) {
                    /*Toast.makeText(getApplicationContext(), "Η συσκευή δεν έχει κάμερα", Toast.LENGTH_LONG).show();*/
                }
            } else {
                i += ACTION_TAKE_PHOTO_B;
            }
        }
        return c;
    }

    public static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Sentiment");
        if (mediaStorageDir.exists() || mediaStorageDir.mkdirs()) {
            return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + "cropped_bitmap" + ".jpg");
        }
        Log.d("MyCameraApp", "failed to create directory");
        return null;
    }


}
