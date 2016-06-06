package tk.parmclee.starfootprint;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

@SuppressWarnings("deprecation") // deprecated Camera
public class CameraPreview extends SurfaceView {

    private AppCompatActivity mActivity;
    Camera mCamera;
    SurfaceHolder mHolder;

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(new HolderCallback());
    }

    public CameraPreview(Context context, Activity activity) {
        this(context);
        mActivity = (AppCompatActivity) activity;
    }

    void initCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Can't open camera", Toast.LENGTH_SHORT).show();
        }
        mCamera = c;
    }

    void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    void setCameraPreview(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Can't start preview", Toast.LENGTH_SHORT).show();
        }
    }

    void startCameraPreview(int w, int h) {
        initCameraInstance();
        setPreviewSize(w, h);
        setCameraDisplayOrientation();
        setCameraPreview(mHolder);
    }

    void setCameraDisplayOrientation() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:   degrees = 0;   break;
            case Surface.ROTATION_90:  degrees = 90;  break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
            default:
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                degrees = (info.orientation + 360 - degrees) % 360;
            }// no need at else clause: open() returns only back camera (or null)
        }
        try {
            mCamera.setDisplayOrientation(degrees);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    void setPreviewSize(int w, int h) {
        RectF rectDisplay = new RectF();
        rectDisplay.set(0, 0, w, h);
        boolean landscape = w > h;

        Camera.Size cameraSize = mCamera.getParameters().getPreviewSize();
        RectF rectCamera = new RectF();
        if (landscape) rectCamera.set(0, 0, cameraSize.width, cameraSize.height);
        else rectCamera.set(0, 0, cameraSize.height, cameraSize.width);

        Matrix matrix = new Matrix();
        matrix.setRectToRect(rectDisplay, rectCamera, Matrix.ScaleToFit.START);
        matrix.invert(matrix);
        matrix.mapRect(rectCamera);
        int width = (int) rectCamera.right;
        int height = (int) rectCamera.bottom;
        RelativeLayout container = (RelativeLayout) mActivity.findViewById(R.id.previewContainer);
        if (container != null) {
            container.getLayoutParams().width = width;
            container.getLayoutParams().height = height;
        }
    }

    private class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mCamera == null) return;
            setCameraPreview(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mHolder.getSurface() == null || mCamera == null) return;
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }
            hideActionBar();
            setPreviewSize(width, height);
            setCameraDisplayOrientation();
            setCameraPreview(holder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // releasing Camera preview at onPause of Activity
        }
    }

    void hideActionBar() {
        if (mActivity == null) return;
        ActionBar actionBar = mActivity.getSupportActionBar();
        Point displaySize = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        if (actionBar != null) {
            if (actionBar.getHeight() > 0.13 * displaySize.y) actionBar.hide();
            else actionBar.show();
        }
    }
}
