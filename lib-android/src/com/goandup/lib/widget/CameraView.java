package com.goandup.lib.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.goandup.lib.R;
import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Screen;

import java.util.List;

/**
 * Created by nicolas on 06/08/2014.
 */
public class CameraView extends FrameLayout
{
    private static Camera camera;

    private ProgressBar loader;
    private boolean faceCamera;
    private boolean changeByMe;
    private boolean takingPhoto;
    private boolean autoStart;
    private int width;
    private int height;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        if(attrs != null)
        {
            TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ParamCameraView);
            faceCamera = arr.getBoolean(R.styleable.ParamCameraView_faceCamera, false);
            autoStart = arr.getBoolean(R.styleable.ParamCameraView_autoStart, true);
            if (autoStart)
            {
                start();
            }
            arr.recycle();
        }
    }

    public void start()
    {
        loader = null;
        release(new ListenerRelease()
        {
            @Override
            public void end()
            {
                post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        removeAllViews();
                        addView(new CameraViewSurface(getContext()));
                    }
                });
            }
        });
    }

    //!\ Must be absolutely call in all onResume Activity with CameraView.
    public void resume()
    {
        if(camera == null && autoStart)
        {
            start();
        }
    }

    /**
     * Custom SurfaceView for preview
     */
    public class CameraViewSurface extends SurfaceView implements SurfaceHolder.Callback
    {
        SurfaceHolder holder;

        public CameraViewSurface(Context context)
        {
            super(context);

            holder = getHolder();
            holder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            setZOrderOnTop(false);

            //release();
        }

        @Override public void surfaceCreated(SurfaceHolder holder) { }
        @Override public void surfaceDestroyed(SurfaceHolder holder) { }

        @Override
        public void surfaceChanged(final SurfaceHolder holder, int format, final int wView, final int hView)
        {
            if ((changeByMe && camera != null) || this.holder.getSurface() == null)
            {
                changeByMe = false;
                return;
            }
            changeByMe = false;

            width = wView;
            height = hView;

            //show loadeur
            if(loader == null)
            {
                loader = new ProgressBar(getContext(), null, android.R.attr.progressBarStyle);
                int size = Math.min(Math.min(width, height) >> 1, Screen.dpToPx(25, getContext()));
                LayoutParams lp = new LayoutParams(size, size);
                lp.setMargins((width-size)>>1, (height-size)>>1, 0, 0);
                loader.setLayoutParams(lp);
                addView(loader);
            }
            loader.setVisibility(View.VISIBLE);

            //execute to another Thread
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {

                        if(camera == null)
                        {
                            //open camera
                            try
                            {
                                if(faceCamera || !hasBackCamera())
                                {
                                    camera = Camera.open(getFaceIndexCamera());
                                }
                                else
                                {
                                    camera = Camera.open();
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            // stop preview
                            try
                            {
                                camera.stopPreview();
                            }
                            catch (Exception e)
                            {
                                L.e("ERROR >> stopPreview : "+e.getMessage());
                                e.printStackTrace();
                            }
                        }

                        if(camera == null)
                        {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "Impossible d'ouvrir la caméra, elle doit être utilisée par une autre application.", Toast.LENGTH_LONG);
                                }
                            });
                            L.e("ERROR : Impossible d'ouvrir la caméra. Ajoutez la permission : android.permission.CAMERA");
                            return;
                        }

                        //set size
                        Camera.Parameters parameters = camera.getParameters();
                        Camera.Size bestSize = getBiggerPreviewSize();
                        if(bestSize == null) return;
                        Camera.Size imgSize = getBestPictureSize(width, height, (float)bestSize.width/(float)bestSize.height);
                        if(imgSize == null) return;
                        parameters.setPreviewSize(bestSize.width, bestSize.height);
                        parameters.setPictureSize(imgSize.width, imgSize.height);
                        camera.setParameters(parameters);

                        // rotation
                        int rotation = getRotationUniversal();
                        int camWidth = bestSize.width;
                        int camHeight = bestSize.height;
                        if(rotation == Surface.ROTATION_0)
                        {
                            camera.setDisplayOrientation(90);
                            camWidth = bestSize.height;
                            camHeight = bestSize.width;
                        }
                        if(rotation == Surface.ROTATION_180)
                        {
                            camera.setDisplayOrientation(270);
                            camWidth = bestSize.height;
                            camHeight = bestSize.width;
                        }
                        if(rotation == Surface.ROTATION_270)
                        {
                            camera.setDisplayOrientation(180);
                        }

                        // resize
                        float ratioView = (float)width / (float)height;
                        float ratioCam = (float)camWidth / (float)camHeight;
                        int w = width;
                        int h = height;
                        if(ratioView != ratioCam)
                        {
                            final LayoutParams lp = (LayoutParams) getLayoutParams();

                            //if the view is larger than the cam
                            if(ratioView > ratioCam)
                            {
                                h = (int) ((float)w / ratioCam);
                                lp.topMargin = -((h - height) >> 1);
                            }
                            //if the view is narrower than the cam
                            else
                            {
                                if(camHeight > height) w = (int) (camHeight * ratioView);
                                else w = (int) (height * ratioCam);
                                lp.leftMargin = -((w - width) >> 1);
                            }

                            lp.width = w;
                            lp.height = h;

                            //setLayoutParams on UI Thread
                            post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //"changeByMe" used to changes the size of the view,
                                    // without the method surfaceChanged() is executed again.
                                    changeByMe = (lp.height != height || lp.width != width);//true if we change val w or h
                                    setLayoutParams(lp);
                                }
                            });
                        }
                        //TODO : gérer margin pour taille view carré (else)

                        // start preview with new settings
                        try
                        {
                            camera.setPreviewDisplay(CameraViewSurface.this.holder);
                            camera.startPreview();
                        }
                        catch (Exception e)
                        {
                            L.e("ERROR >> startPreview : "+e.getMessage());
                            e.printStackTrace();
                        }

                        camera.setErrorCallback(new Camera.ErrorCallback()
                        {
                            @Override
                            public void onError(int i, Camera camera)
                            {
                                L.e(">>>>>>> CameraView ERROR : i=["+i+"]");
                            }
                        });

                        // hide loadeur
                        post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(loader != null) loader.setVisibility(View.GONE);//crash #5
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        L.e(">>>>>>> CameraView surfaceChanged : e=["+e.getMessage()+"]");
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * Utile Camera
     */
    public void captureCamera(final CameraViewListener listener)
    {
        if(takingPhoto) return;
        takingPhoto = true;
        //TODO : crash #10 >> http://stackoverflow.com/questions/28217691/android-java-lang-runtimeexception-takepicture-failed
        if(camera != null)
        {
            camera.takePicture(null, null, new Camera.PictureCallback()//TODO : crash #28
            {
                @Override
                public void onPictureTaken(byte[] data, Camera camera)
                {
                    if (listener != null)
                    {
                        //convert
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//TODO : crash #39 OutOfMemoryError
                        Matrix matrix = new Matrix();

                        //rotation
                        int rotation = getRotationUniversal();
                        int imgWidth = bitmap.getWidth();
                        int imgHeight = bitmap.getHeight();
                        if (rotation == Surface.ROTATION_0)
                        {
                            matrix.postRotate(90);
                            imgWidth = bitmap.getHeight();
                            imgHeight = bitmap.getWidth();
                        }
                        if (rotation == Surface.ROTATION_180)
                        {
                            matrix.postRotate(270);
                            imgWidth = bitmap.getHeight();
                            imgHeight = bitmap.getWidth();
                        }
                        if (rotation == Surface.ROTATION_270)
                        {
                            matrix.postRotate(180);
                        }
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);//TODO : crash #35 OutOfMemoryError

                        //resize
                        int x = 0;
                        int y = 0;
                        int w = imgWidth;
                        int h = imgHeight;
                        float ratioView = (float) width / (float) height;
                        float ratioImg = (float) imgWidth / (float) imgHeight;
                        //if the view is larger than the picture
                        if (ratioView > ratioImg)
                        {
                            h = (int) ((float) imgWidth / ratioView);
                            y = (imgHeight - h) >> 1;
                        }
                        //if the view is narrower than the picture
                        else
                        {
                            w = (int) (imgHeight * ratioView);
                            x = (imgWidth - w) >> 1;
                        }

                        //create Bitmap
                        Bitmap img;
                        if (isFaceCamera() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                        {
                            //if face camera, compensate for the mirror
                            float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                            Matrix miroir = new Matrix();
                            miroir.setValues(mirrorY);
                            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
                            {
                                miroir.preRotate(180);
                            }
                            img = Bitmap.createBitmap(bitmap, x, y, w, h, miroir, true);
                        } else
                        {
                            img = Bitmap.createBitmap(bitmap, x, y, w, h);//TODO crash : #47
                        }

                        if (listener != null)
                            listener.repCaptureCamera(new BitmapDrawable(getContext().getResources(), img));

                        camera.startPreview();

                        takingPhoto = false;
                    }
                }
            });
        }
        else
        {
            if(listener != null) listener.repCaptureCamera(null);
        }
    }

    //!\ Must be absolutely call in all onPause Activity with CameraView.
    private static boolean isRealasing;
    public static void release() {
        release(null);
    }
    public static void release(final ListenerRelease listener)
    {
        if(camera != null)
        {
            isRealasing = true;
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        camera.stopPreview();
                        camera.release();
                    }
                    catch (Exception e)
                    {
                        L.e("ERROR >> release : "+e.getMessage());
                        e.printStackTrace();
                    }
                    finally
                    {
                        camera = null;
                        isRealasing = false;
                        if(listener != null) listener.end();
                    }
                }
            }).start();
        }
        else if(listener != null) listener.end();
    }
    public interface ListenerRelease {
        public void end();
    }

    /** flash */
    public boolean toggleFlash()
    {
        return enableFlash(!flashIsEnable());
    }
    public boolean enableFlash(boolean enable)
    {
        if(!currentCameraHasFlash() || camera == null) return false;

        Camera.Parameters p = camera.getParameters();
        p.setFlashMode(enable ? Camera.Parameters.FLASH_MODE_ON : Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(p);

        return enable;
    }
    public boolean flashIsEnable()
    {
        return camera != null && Camera.Parameters.FLASH_MODE_ON.equals(camera.getParameters().getFlashMode());
    }
    public boolean currentCameraHasFlash()
    {
        if (camera == null)//if no camera initialized = general test.
        {
            return !faceCamera && getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        }

        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getFlashMode() == null) return false;

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF))
        {
            return false;
        }

        return true;
    }

    /** face camera */
    public boolean toggleFaceCamera()
    {
        return setFaceCamera(!isFaceCamera());
    }
    public boolean setFaceCamera(boolean faceCamera)
    {
        if(hasFaceCamera())
        {
            this.faceCamera = faceCamera;
            start();
            return true;
        }
        return false;
    }
    public boolean isFaceCamera()
    {
        int cameraId = 0;
        if(faceCamera) cameraId = getFaceIndexCamera();

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        return cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }
    public boolean hasFaceCamera()
    {
        return  getFaceIndexCamera() != 0;
    }
    private int getFaceIndexCamera()
    {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < Camera.getNumberOfCameras(); cameraIndex++)
        {
            Camera.getCameraInfo(cameraIndex, cameraInfo);//TODO : crash #60
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            {
                return cameraIndex;
            }
        }
        return 0;
    }
    public boolean hasBackCamera()
    {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < Camera.getNumberOfCameras(); cameraIndex++)
        {
            Camera.getCameraInfo(cameraIndex, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                return true;
            }
        }
        return false;
    }


    /** other */
    private Camera.Size getBiggerPreviewSize()
    {
        Camera.Size bigger = null;
        if(isRealasing || camera == null) return null;
        Camera.Parameters p = camera.getParameters();
        for (Camera.Size size : p.getSupportedPreviewSizes())
        {
            if(bigger == null || (size.width * size.height) >= (bigger.width * bigger.height))
            {
                bigger = size;
            }
        }

        return bigger;
    }
    private Camera.Size getBestPreviewSize(int width, int height)
    {
        int rotation = getRotationUniversal();
        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
        {
            int tmp = width;
            width = height;
            height = tmp;
        }

        Camera.Size best = null;
        Camera.Size bigger = null;
        if(isRealasing || camera == null) return null;
        Camera.Parameters p = camera.getParameters();
        for (Camera.Size size : p.getSupportedPreviewSizes())
        {
            if(bigger == null || (size.width * size.height) >= (bigger.width * bigger.height))
            {
                bigger = size;
            }

            if (size.width >= width && size.height >= height && (best == null || (size.width * size.height) < (best.width * best.height)))
            {
                best = size;
            }
        }

        return best != null ? best : bigger;
    }


    private Camera.Size getBestPictureSize(int width, int height) {
        return getBestPictureSize(width, height, 0f);
    }
    private Camera.Size getBestPictureSize(int width, int height, float ratio)
    {
        int rotation = getRotationUniversal();
        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
        {
            int tmp = width;
            width = height;
            height = tmp;
        }

        Camera.Size bigger = null;
        Camera.Parameters p = camera.getParameters();
        for (Camera.Size size : p.getSupportedPictureSizes())
        {
            float ratioSize = (float)size.width / (float)size.height;

            if((ratioSize == ratio || ratio == 0f) && (bigger == null || (size.width * size.height) >= (bigger.width * bigger.height)))
            {
                bigger = size;
            }
        }

        if(bigger == null) bigger = getBestPictureSize(width, height); //without ratio

        return bigger;
    }

    private int getRotationUniversal()
    {
        WindowManager windowManager =  (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Configuration config = getContext().getResources().getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();

        if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
          || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT))
        {
            rotation = (rotation+1) % 4;
        }

        return rotation;
    }

    public interface CameraViewListener
    {
        public void repCaptureCamera(Drawable image);
    }

    public static Camera getCamera()
    {
        return camera;
    }
}
