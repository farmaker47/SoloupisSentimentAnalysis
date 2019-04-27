package com.example.soloupissentimentanalysis.camera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements Callback {

    private final String TAG;
    private int cameraId;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    private int rotation;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.TAG = "Camera Preview";
        this.mCamera = camera;
        this.cameraId = 0;
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        this.mHolder.setType(3);
        this.mSupportedPreviewSizes = this.mCamera.getParameters().getSupportedPreviewSizes();
        setIfAutoFocusSupported();
    }

    public boolean onTouchEvent(MotionEvent e) {

        setIfAutoFocusSupported();
        return true;
    }

    public void initPreview() {
        if (this.mCamera != null) {
            Parameters parameters = this.mCamera.getParameters();
            parameters.setPreviewSize(this.mPreviewSize.width, this.mPreviewSize.height);
            this.mCamera.setParameters(parameters);
            this.mCamera.startPreview();
        }
    }

    private void setUpCamera(Camera c) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(this.cameraId, info);
        this.rotation = PreviewCamera.rot;
        int degree = 0;

        if (info.facing == 1) {
            this.rotation = (info.orientation + degree) % 330;
            this.rotation = (360 - this.rotation) % 360;
        } else {
            this.rotation = ((info.orientation - degree) + 360) % 360;
        }
        c.setDisplayOrientation(this.rotation);
        Parameters params = c.getParameters();
        List<String> focusModes = params.getSupportedFlashModes();
        if (focusModes != null && focusModes.contains("continuous-picture")) {
            params.setFlashMode("continuous-picture");
        }
        params.setRotation(this.rotation);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (this.mCamera != null) {
                this.mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e("Camera Preview", "IOException caused by setPreviewDisplay()", exception);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        double targetRatio = ((double) w) / ((double) h);
        if (sizes == null) {
            return null;
        }
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        for (Size size : sizes) {
            if (Math.abs((((double) size.width) / ((double) size.height)) - targetRatio) <= 0.1d && ((double) Math.abs(size.height - h)) < minDiff) {
                optimalSize = size;
                minDiff = (double) Math.abs(size.height - h);
            }
        }
        if (optimalSize != null) {
            return optimalSize;
        }
        minDiff = Double.MAX_VALUE;
        for (Size size2 : sizes) {
            if (((double) Math.abs(size2.height - h)) < minDiff) {
                optimalSize = size2;
                minDiff = (double) Math.abs(size2.height - h);
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (this.mHolder.getSurface() != null) {
            try {
                this.mCamera.stopPreview();
            } catch (Exception e) {
            }
            if (this.mSupportedPreviewSizes != null) {
                this.mPreviewSize = getOptimalPreviewSize(this.mSupportedPreviewSizes, w, h);
            }
            try {
                setUpCamera(this.mCamera);
                this.mCamera.setPreviewDisplay(this.mHolder);
                Parameters params = this.mCamera.getParameters();
                params.setPreviewSize(this.mPreviewSize.width, this.mPreviewSize.height);
                params.setRotation(this.rotation);
                Size pictureSize = getSmallestPictureSize(params);
                if (pictureSize != null) {
                    params.setPictureSize(pictureSize.width, pictureSize.height);
                    Log.d("Camera Preview", "Preview Size: " + this.mPreviewSize.width + "X" + this.mPreviewSize.height + "PS" + pictureSize.width + "Y" + pictureSize.height);
                    this.mCamera.setParameters(params);
                }
                this.mCamera.startPreview();
            } catch (Exception e2) {
                Log.d("Camera Preview", "Error starting camera preview: " + e2.getMessage());
            }
        }
    }

    private Size getSmallestPictureSize(Parameters parameters) {
        Size result = null;
        for (Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            } else if (size.width * size.height > result.width * result.height) {
                result = size;
            }
        }
        return result;
    }

    private void setIfAutoFocusSupported() {
        Parameters params = this.mCamera.getParameters();
        if (params.getSupportedFocusModes().contains("continuous-picture")) {
            params.setFocusMode("continuous-picture");
            this.mCamera.setParameters(params);
        }
    }


}
