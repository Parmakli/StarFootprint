package tk.parmclee.starfootprint;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation") // deprecated Camera
public class AutoCapture extends SurfaceView {

    SurfaceHolder mHolder;
    Camera mCamera;
    WindowManager windowManager;
    boolean firstPicture;
    File mFile;
    AppCompatActivity mActivity;
    static int sCounter;

    public AutoCapture(Context context, final boolean first, File file, AppCompatActivity activity) {
        super(context);
        mActivity = activity;
        mHolder = getHolder();
        mHolder.addCallback(new HolderCallback());
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        firstPicture = first;
        mFile = file;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingIntent intent = PendingIntent.getBroadcast(getContext(), 0,
                        CaptureFragment.sAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                ((AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE)).cancel(intent);
                CaptureFragment.isRunning = false;
            }
        });

    }

    void initCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Can't open camera", Toast.LENGTH_SHORT).show();
        }
        if (c != null) c.setOneShotPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                takePicture();
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int type = Integer.parseInt(preferences.getString("cam_parameters", "0"));
        changeParameters(c, type);
        mCamera = c;
    }

    private void changeParameters(Camera camera, int type) {
        Camera.Parameters parameters = camera.getParameters();
        if (type == 1) { // advanced parameters
            if (parameters.getAntibanding() != null &&
                    parameters.getSupportedAntibanding().contains(Camera.Parameters.ANTIBANDING_AUTO)) {
                parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
            }
            if (parameters.getWhiteBalance() != null &&
                    parameters.getSupportedWhiteBalance().contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            }
            if (parameters.getSupportedPictureFormats().contains(PixelFormat.RGB_565)) {
                parameters.setPictureFormat(PixelFormat.RGB_565);
            } else if (parameters.getSupportedPictureFormats().contains(android.graphics.ImageFormat.NV21)) {
                parameters.setPictureFormat(android.graphics.ImageFormat.NV21);
            } else {
                parameters.setPictureFormat(PixelFormat.JPEG);
                parameters.setJpegQuality(100);
            }
            int minCompensation = parameters.getMinExposureCompensation();
            float step = parameters.getExposureCompensationStep();
            if (minCompensation < 0) {
                if (minCompensation * step < -1) {
                    parameters.setExposureCompensation(-Math.round(1 / step));
                } else parameters.setExposureCompensation(minCompensation);
            }
        } else { //default parameters
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setJpegQuality(100);
        }
        // both
        if (parameters.getFlashMode() != null) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        Camera.Size maxSize = parameters.getPictureSize();
        for (Camera.Size size : sizes) {
            if (size.height > maxSize.height || size.width > maxSize.width) maxSize = size;
        }
        parameters.setPictureSize(maxSize.width, maxSize.height);

        camera.setParameters(parameters);
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
            Toast.makeText(getContext(), "Can't start preview", Toast.LENGTH_SHORT).show();
        }
    }

    private class HolderCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            initCameraInstance();
            if (mHolder.getSurface() == null || mCamera == null) {
                return;
            }
            setCameraPreview(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    void saveBytesToFile(File file, byte[] data) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Can't find file", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Can't modify file", Toast.LENGTH_SHORT).show();
        }
    }

    void takePicture() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Toast.makeText(getContext(), getResources().getString(R.string.captured),
                        Toast.LENGTH_SHORT).show();
                if (firstPicture) {
                    saveBytesToFile(mFile, data);
                    AlarmReceiver.completeWakefulIntent(AlarmReceiver.sActivityIntent);
                } else {
                    String path = mFile.getAbsolutePath();
                    String tempName = path.substring(0, path.length() - 4) + //reduce extension
                            "_temp" + (sCounter++) + CaptureFragment.sPictureExtension;
                    File tempFile = new File(tempName);
                    try {
                        tempFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Can't modify file", Toast.LENGTH_SHORT).show();
                    }
                    saveBytesToFile(tempFile, data);

                    Intent service = new Intent(getContext(), CaptureService.class);
                    service.putExtra("filePath", path);
                    service.putExtra("tempFilePath", tempName);
                    AlarmReceiver.startWakefulService(getContext(), service);
                }
                releaseCamera();
                mActivity.finish();
            }
        });
    }
}
