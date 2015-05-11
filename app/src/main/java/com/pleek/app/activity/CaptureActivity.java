package com.pleek.app.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.CameraView;
import com.goandup.lib.widget.DisableTouchListener;
import com.goandup.lib.widget.EditTextFont;
import com.goandup.lib.widget.FlipImageView;
import com.goandup.lib.widget.SquareOverlay;
import com.pleek.app.R;
import com.pleek.app.bean.AutoResizeFontTextWatcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by nicolas on 15/01/15.
 */
public class CaptureActivity extends ParentActivity implements View.OnClickListener, SquareOverlay.Listener, TextureView.SurfaceTextureListener
{
    public static boolean CLOSE_ME;

    private int SIZE_PIKI = 640;
    private int SIZE_PIKI_VIDEO = 480;
    private int GALLERY_INTENT_CALLED = 1;
    private int GALLERY_KITKAT_INTENT_CALLED = 2;
    private int MAX_FONTSIZE = 60;//dp

    private RelativeLayout layoutCamera;
    private CameraView camera;
    private ImageView imgPreview;
    private View btnBack;
    private SquareOverlay layoutOverlay;
    private RelativeLayout layoutTexte;
    private View layoutTouch;
    private EditTextFont edittextePhoto;
    private View layoutBottomControl1;
    private FlipImageView btnReverse;
    private ImageView btnCapture;
    private ImageView imgGallerie;
    private View layoutBottomControl2;
    private View btnBackCapture;
    private ImageView btnNext;
    private View btnSave;
    private View removeFocus;

    private int timeRecording = 12000;
    private MediaRecorder mediaRecorder;
    private boolean isRecording;
    private Handler handler;
    private Camera.Size optimalSize = null;
    private Dialog loader;
    private View viewVideoProgress;
    private long timerStart = 0L;
    private long timeDown;
    private boolean isDown;
    private int longClickDuration = 700;
    private boolean isVideo;

    private MediaPlayer mediaPlayer;
    private RelativeLayout layoutVideo;
    private boolean isPreparePlaying;
    private boolean isPlaying = false;
    private TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        setup();
        init();

        mixpanel.track("View Take Photo Screen", null);
        fbAppEventsLogger.logEvent("View Take Photo Screen");
    }

    private void setup()
    {
        handler = new Handler();
        camera = (CameraView)findViewById(R.id.camera);
        camera.setFaceCamera(pref.getBoolean("selfie_camera", true));
        layoutCamera = (RelativeLayout) findViewById(R.id.layoutCamera);
        layoutCamera.setVisibility(View.VISIBLE);
        imgPreview = (ImageView)findViewById(R.id.imgPreview);
        imgPreview.setVisibility(View.GONE);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
        layoutOverlay = (SquareOverlay)findViewById(R.id.layoutOverlay);
        layoutOverlay.setListener(this);
        layoutTexte = (RelativeLayout)findViewById(R.id.layoutTexte);
        layoutTouch = findViewById(R.id.layoutTouch);
        layoutTouch.setOnTouchListener(new TexteTouchListener());
        edittextePhoto = (EditTextFont) findViewById(R.id.edittextePhoto);
        int maxWidth = screen.getWidth() - screen.dpToPx(40);
        int maxHeight = maxWidth * 3/4;
        edittextePhoto.addTextChangedListener(new AutoResizeFontTextWatcher(edittextePhoto, maxWidth, maxHeight, screen.dpToPx(MAX_FONTSIZE)));
        edittextePhoto.setListener(new MyEditTexteListener());
        edittextePhoto.setTextSize(TypedValue.COMPLEX_UNIT_PX, screen.dpToPx(MAX_FONTSIZE));
        edittextePhoto.setOnTouchListener(new DisableTouchListener());
        layoutBottomControl1 = findViewById(R.id.layoutBottomControl1);
        layoutBottomControl1.setVisibility(View.VISIBLE);
        btnReverse = (FlipImageView) findViewById(R.id.btnReverse);
        btnReverse.setOnClickListener(this);
        btnCapture = (ImageView) findViewById(R.id.btnCapture);
        btnCapture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    timeDown = System.currentTimeMillis();
                    isDown = true;
                    handler.postDelayed(recordVideoRunnable, longClickDuration);

                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    isDown = false;

                    if (event.getAction() == MotionEvent.ACTION_UP && !isRecording && (System.currentTimeMillis() - timeDown) < longClickDuration) {
                        camera.captureCamera(new CameraView.CameraViewListener() {
                            @Override
                            public void repCaptureCamera(Drawable image) {
                                showPreview(image);
                            }
                        });

                        return true;
                    } else if (isRecording) {
                        stopMediaRecorder();
                    }
                }

                return true;
            }
        });

        imgGallerie = (ImageView)findViewById(R.id.imgGallerie);
        imgGallerie.setOnClickListener(this);
        layoutBottomControl2 = findViewById(R.id.layoutBottomControl2);
        layoutBottomControl2.setVisibility(View.GONE);
        btnBackCapture = findViewById(R.id.btnBackCapture);
        btnBackCapture.setOnClickListener(this);
        btnNext = (ImageView) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] pikiData = getPikiData();
                if(pikiData != null || isVideo)
                {
                    if (!isVideo) {
                        RecipientsActivity.initActivity(pikiData);
                    } else {
                        final File videosDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/pikis/");
                        final File tmpFile = new File(videosDir, "out.mp4");
                        RecipientsActivity.initActivity(tmpFile.getAbsolutePath());
                    }
                    startActivity(new Intent(CaptureActivity.this, RecipientsActivity.class));
                    overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                }
                else
                {
                    Utile.showToast(R.string.error_pikidata, CaptureActivity.this);
                }
            }
        });
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        removeFocus = findViewById(R.id.removeFocus);

        layoutVideo = (RelativeLayout) findViewById(R.id.layoutVideo);
        viewVideoProgress = findViewById(R.id.videoProgress);

        textureView = new TextureView(this);
        textureView.setSurfaceTextureListener(this);
    }

    private void init()
    {
        Bitmap lastBm = getLastUserPhoto();
        if(lastBm != null) imgGallerie.setImageBitmap(lastBm);
    }

    @Override
    public void onClick(View view)
    {
        if(view == btnBack)
        {
            finish();
        }
        else if(view == btnReverse)
        {
            if(camera.toggleFaceCamera())
            {
                btnReverse.setFlipped(!btnReverse.isFlipped());
            }

            //save
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("selfie_camera", camera.isFaceCamera());
            editor.commit();
        }
        else if(view == imgGallerie)
        {
            //http://stackoverflow.com/questions/19834842/android-gallery-on-kitkat-returns-different-uri-for-intent-action-get-content
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
            {
                Intent intent = new Intent();
                intent.setType("image/jpeg");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)),GALLERY_INTENT_CALLED);
            }
            else
            {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/jpeg");
                startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
            }

        }
        else if(view == btnBackCapture)
        {
            showCapture();
        }
        else if(view == btnSave)
        {
            byte[] pikiData = getPikiData();
            if(pikiData != null)
            {
                String description = edittextePhoto.getText().toString().trim();
                if(description.length() == 0) description = getString(R.string.reactwithyoutcamera);
                Bitmap bitmap = BitmapFactory.decodeByteArray(pikiData , 0, pikiData .length);
                saveImage(getContentResolver(), bitmap, getString(R.string.app_name), getString(R.string.app_name)+" : " + description);
                Utile.showToast(R.string.capture_saveok, this);
            }
            else
            {
                Utile.showToast(R.string.error_pikidata, CaptureActivity.this);
            }
        }
    }

    @Override
    @SuppressLint("NewApi")
    @SuppressWarnings("ResourceType")
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;
        if (null == data) return;
        Uri originalUri = null;
        if (requestCode == GALLERY_INTENT_CALLED)
        {
            originalUri = data.getData();
        }
        else if (requestCode == GALLERY_KITKAT_INTENT_CALLED)
        {
            originalUri = data.getData();
            int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            getContentResolver().takePersistableUriPermission(originalUri, takeFlags);
        }

        if(originalUri != null)
            showPreview(new BitmapDrawable(getResources(), BitmapFactory.decodeFile(getPath(originalUri))));//TODO crash : #38
    }

    //http://stackoverflow.com/questions/27038051/pick-photo-from-gallery-in-android-5-0
    private String getPath(Uri uri)
    {
        if( uri == null ) {
            return null;
        }

        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor;
        if(Build.VERSION.SDK_INT > 19)
        {
            // Will return "image:x*"
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, sel, new String[]{ id }, null);
        }
        else
        {
            cursor = getContentResolver().query(uri, projection, null, null, null);
        }
        String path = null;
        try
        {
            int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index).toString();
            cursor.close();
        }
        catch(NullPointerException e) {

        }
        return path;
    }

    private void showPreview(Drawable drawable)
    {
        if(drawable == null) return;

        imgPreview.setImageDrawable(drawable);
        imgPreview.setVisibility(View.VISIBLE);
        layoutCamera.setVisibility(View.GONE);
        showLayoutControl2();
    }

    private void showCapture()
    {
        isVideo = false;
        imgPreview.setVisibility(View.GONE);
        imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        stopCurrentVideo();
        layoutCamera.setVisibility(View.VISIBLE);
        camera.getCamera().startPreview();
        edittextePhoto.setVisibility(View.VISIBLE);
        showLayoutControl1();
    }

    private void showLayoutControl1()
    {
        if(layoutBottomControl1.getVisibility() == View.VISIBLE) return;
        Utile.fadeIn(layoutBottomControl1, 300);
        Utile.fadeOut(layoutBottomControl2, 300);
    }

    private void showLayoutControl2()
    {
        btnSave.setVisibility(isVideo ? View.GONE : View.VISIBLE);

        if(layoutBottomControl2.getVisibility() == View.VISIBLE) return;
        Utile.fadeOut(layoutBottomControl1, 300);
        Utile.fadeIn(layoutBottomControl2, 300);
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }

    @Override
    public void sizeChange(final int sizeSquare)
    {
        layoutTexte.post(new Runnable()
        {
            @Override
            public void run()
            {
                ViewGroup.LayoutParams params = layoutTexte.getLayoutParams();
                params.height = sizeSquare;
                params.width = sizeSquare;
                layoutTexte.requestLayout();
            }
        });

        imgPreview.post(new Runnable()
        {
            @Override
            public void run()
            {
                ViewGroup.LayoutParams params = imgPreview.getLayoutParams();
                params.height = sizeSquare;
                params.width = sizeSquare;
                imgPreview.requestLayout();
            }
        });

        layoutVideo.post(new Runnable()
        {
            @Override
            public void run()
            {
                ViewGroup.LayoutParams params = layoutVideo.getLayoutParams();
                params.height = sizeSquare;
                params.width = sizeSquare;
                layoutVideo.requestLayout();
            }
        });

        viewVideoProgress.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewVideoProgress.getLayoutParams();
                params.bottomMargin = (layoutOverlay.getHeight() - sizeSquare) / 2;
                viewVideoProgress.requestLayout();
            }
        });
    }


    private void startEditTexte()
    {
        edittextePhoto.requestFocus();
        //open keyboard
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(edittextePhoto, InputMethodManager.SHOW_IMPLICIT);
    }
    private void endEditTexte()
    {
        //close keyboard
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(edittextePhoto.getWindowToken(), 0);
    }

    private byte[] getPikiData()
    {
        try
        {
            ///////////
            // Bitmap de la photo
            Bitmap photoBitmap = ((BitmapDrawable)imgPreview.getDrawable()).getBitmap();
            int photoWidth = photoBitmap.getWidth();
            int photoHeight = photoBitmap.getHeight();
            int pikiSize = Math.min(photoWidth, photoHeight);

//        float xScale = (float) SIZE_PIKI / photoWidth;
//        float yScale = (float) SIZE_PIKI / photoHeight;
//        float scale = Math.max(xScale, yScale);

//        float scaledWidth = scale * photoWidth;
//        float scaledHeight = scale * photoHeight;

            float left = (pikiSize - photoWidth) >> 1;
            float top = (pikiSize - photoHeight) >> 1;

            RectF targetRect = new RectF(left, top, left + photoWidth, top + photoHeight);

            ///////////
            // Bitmap du texte
            removeFocus.requestFocus();//remove focus
            Bitmap textBitmap = edittextePhoto.getBitmapOfView();

            //calcule margin top of texte
            int marginTop = ((ViewGroup.MarginLayoutParams) edittextePhoto.getLayoutParams()).topMargin;
            marginTop += (layoutTexte.getHeight() - edittextePhoto.getHeight()) >> 1;//because gravity center

            //resize and calcul position of textBitmap in final canvas
            float ratio = (float)pikiSize / Math.min(layoutTexte.getWidth(), layoutTexte.getHeight());
            int texteLeft = (int) (((layoutTexte.getWidth() - textBitmap.getWidth()) >> 1) * ratio);
            int texteTop = (int) (marginTop * ratio);
            int texteWidth = (int) (textBitmap.getWidth() * ratio);
            int texteHeight = (int) (textBitmap.getHeight() * ratio);

            //Create resize Rect
            Rect srcTextRect = new Rect(0, 0, textBitmap.getWidth(), textBitmap.getHeight());
            Rect dstTextRect = new Rect(texteLeft, texteTop, texteLeft+texteWidth, texteTop+texteHeight);

            ///////////
            // Create canvas finale
            Bitmap finalBitmap = Bitmap.createBitmap(pikiSize, pikiSize, photoBitmap.getConfig());//TODO crash : #42 OutOfMemory
            Canvas finalCanvas = new Canvas(finalBitmap);
            finalCanvas.drawBitmap(photoBitmap, null, targetRect, null);
            finalCanvas.drawBitmap(textBitmap, srcTextRect, dstTextRect, null);

            //create jpeg data
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        }
        catch (Exception e)
        {
            L.e(">>>>>>> Erreur getPikiData - e=" + e.getMessage());
        }

        return null;
    }

    /**
     * Texte of Image
     */
    public class TexteTouchListener implements View.OnTouchListener
    {
        private ViewGroup.MarginLayoutParams mlp;

        private int lastY;
        private int maxMargin;
        private boolean hasMoved;

        private ClickRunnable clickRunnable;

        public TexteTouchListener()
        {
            clickRunnable = new ClickRunnable();
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent)
        {
            if(mlp == null) mlp = (ViewGroup.MarginLayoutParams) edittextePhoto.getLayoutParams();

            int action = motionEvent.getAction();
            int y = (int)motionEvent.getY();
            if(action == MotionEvent.ACTION_DOWN)
            {
                lastY = y;
                maxMargin = (view.getHeight() - edittextePhoto.getHeight()) >> 1;

                isUpJustAfterDown = true;
                handler.postDelayed(clickRunnable, CLICK_TIME);//this runnable just pass "isUpJustAfterDown" to false after 100ms
            }
            else if(action == MotionEvent.ACTION_MOVE)
            {
                int topMargin = mlp.topMargin - (lastY - y);
                if(Math.abs(topMargin) >= maxMargin) {
                    topMargin = topMargin > 0 ? maxMargin : -(maxMargin);
                }
                mlp.topMargin = topMargin;
                edittextePhoto.setLayoutParams(mlp);

                if(!hasMoved) endEditTexte();

                lastY = y;
                hasMoved = true;
            }
            else if(action == MotionEvent.ACTION_UP)
            {
                if(isUpJustAfterDown)
                {
                    //if no texte, place editText under finger
                    if(edittextePhoto.getText().length() == 0)
                    {
                        int topMargin = y - (view.getHeight() >> 1);
                        topMargin = Math.min(topMargin, maxMargin);
                        topMargin = Math.max(topMargin, -maxMargin);
                        mlp.topMargin = topMargin;
                        edittextePhoto.setLayoutParams(mlp);
                    }

                    startEditTexte();
                }
            }

            return true;
        }

        final Handler handler = new Handler();
        private final int CLICK_TIME = 250;//ms
        private boolean isUpJustAfterDown;
        class ClickRunnable implements Runnable
        {
            @Override
            public void run()
            {
                isUpJustAfterDown = false;
            }
        }
    }
    private class MyEditTexteListener implements EditTextFont.Listener
    {
        @Override
        public void sizeChange(int w, int h, int oldw, int oldh)
        {
            //verify if texte out of view.
            int maxMargin = (layoutTexte.getHeight() - h) >> 1;
            TableLayout.MarginLayoutParams mlpEdit = (TableLayout.MarginLayoutParams) edittextePhoto.getLayoutParams();
            int topMargin = mlpEdit.topMargin;
            topMargin = Math.min(topMargin, maxMargin);
            topMargin = Math.max(topMargin, -maxMargin);
            mlpEdit.topMargin = topMargin;
            edittextePhoto.setLayoutParams(mlpEdit);
            edittextePhoto.post(new Runnable() {
                @Override
                public void run() {
                    edittextePhoto.requestLayout();
                }
            });
        }

        @Override
        public boolean keyPress(int keyCode)
        {
            return false;
        }
    }

    // >> https://gist.github.com/samkirton/0242ba81d7ca00b475b9
    public static final String saveImage(ContentResolver cr, Bitmap bitmap, String title, String description)
    {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        long date = new Date().getTime();
        values.put(MediaStore.Images.Media.DATE_ADDED, date);
        values.put(MediaStore.Images.Media.DATE_TAKEN, date);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, date/1000);

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try
        {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (bitmap != null)
            {
                OutputStream imageOut = cr.openOutputStream(url);
                try
                {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOut);
                }
                finally
                {
                    imageOut.close();
                }
            }
            else
            {
                cr.delete(url, null, null);
                url = null;
            }
        }
        catch (Exception e)
        {
            if (url != null)
            {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null)
        {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if(imgPreview.getVisibility() == View.VISIBLE || layoutVideo.getVisibility() == View.VISIBLE)
            {
                showCapture();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        CameraView.release();
        stopCurrentVideo();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(CLOSE_ME)
        {
            CLOSE_ME = false;
            HomeActivity.AUTO_RELOAD = true;
            finish();
        }

        if (isVideo) playVideo();
        else camera.resume();
    }

    private Bitmap getLastUserPhoto()
    {
        Bitmap bm = null;

        // Find the last picture
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        // Put it in the image view
        if (cursor != null && cursor.moveToFirst())//fix crash : #58
        {
            String imageLocation = cursor.getString(1);
            File imageFile = new File(imageLocation);
            if (imageFile.exists())
            {
                bm = BitmapFactory.decodeFile(imageLocation);
            }
        }

        return bm;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private boolean prepareMediaRecorder() {
        mediaRecorder = new MediaRecorder();

        Camera.Size size = getCameraSizes();
        android.hardware.Camera cameraObj = camera.getCamera();
        cameraObj.unlock();
        mediaRecorder.setCamera(cameraObj);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = size.width;
        profile.videoFrameHeight = size.height;
        mediaRecorder.setProfile(profile);

        File videosDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/pikis/");
        if (!videosDir.exists()) videosDir.mkdir();

        File tmpFile = new File(videosDir, "myvideo.mp4");

        if (!tmpFile.exists()) {
            tmpFile.deleteOnExit();
        }

        mediaRecorder.setOutputFile(tmpFile.getAbsolutePath());
        mediaRecorder.setMaxDuration(timeRecording); // Set max duration 6 sec.

        mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
            }
        });

        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == 800) {
                    stopMediaRecorder();
                }
            }
        });

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private void stopMediaRecorder() {
        try {
            isRecording = false;

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewVideoProgress.getLayoutParams();
            params.width = 0;
            viewVideoProgress.setLayoutParams(params);
            timerStart = 0;

            mediaRecorder.stop();
            releaseMediaRecorder();
            handler.removeCallbacks(runnableTimeRecording);

            isVideo = true;

            layoutCamera.setVisibility(View.GONE);
            camera.getCamera().stopPreview();
            showLayoutControl2();

            processVideo();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Camera.Size getCameraSizes() {
        Camera.Parameters p = camera.getCamera().getParameters();
        List<Camera.Size> videoSizes = p.getSupportedVideoSizes();

        p.set("cam_mode", 1);
        p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

        final double ASPECT_TOLERANCE = 0.0;
        double targetRatio = (double) p.getPreviewSize().width / p.getPreviewSize().height;

        if (videoSizes == null)
            return null;

        double minDiff = Double.MAX_VALUE;
        int targetWidth = 700;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : videoSizes) {
            Log.d("Camera", "Checking size " + size.width + "w " + size.height
                    + "h");
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.width - targetWidth) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.width - targetWidth);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the
        // requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : videoSizes) {
                if (Math.abs(size.width - targetWidth) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.width - targetWidth);
                }
            }
        }

        return optimalSize;
    }

    public void processVideo() {
        loader = showLoader();
        final FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            final File videosDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/pikis/");
            final File tmpFile = new File(videosDir, "myvideo.mp4");

            ffmpeg.execute("-y -i " + tmpFile + " -vf scale=-2:480" + (optimalSize.width > SIZE_PIKI_VIDEO + 100 ? ",crop=" + (SIZE_PIKI_VIDEO > optimalSize.height ? optimalSize.height : SIZE_PIKI_VIDEO) + ":" + (SIZE_PIKI_VIDEO > optimalSize.height ? optimalSize.height : SIZE_PIKI_VIDEO) : "") + ",transpose=" + (camera.isFaceCamera() ? 3:1)  + " -threads 5 -preset ultrafast -strict -2 " + videosDir + "/out.mp4", new ExecuteBinaryResponseHandler() {

                @Override
                public void onFinish() {
                    generateTempPiki();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private Runnable runnableTimeRecording = new Runnable() {
        @Override
        public void run() {
            if (timerStart == 0) {
                timerStart = System.currentTimeMillis();
            }

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewVideoProgress.getLayoutParams();
            params.width = (int) (screen.getWidth() * (System.currentTimeMillis() - timerStart) / timeRecording);
            viewVideoProgress.setLayoutParams(params);
            handler.post(runnableTimeRecording);
        }
    };

    private void generateTempPiki() {
        final FFmpeg ffmpeg = FFmpeg.getInstance(this);
        final File videosDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/pikis/");
        File tmpFile = new File(videosDir, "out.mp4");
        try {
            ffmpeg.execute("-y -ss 00:00:01 -i " + tmpFile + " -frames:v 1 " + videosDir + "/out1.jpg", new ExecuteBinaryResponseHandler() {

                @Override
                public void onFinish() {
                    hideDialog(loader);
                    playVideo();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void playVideo() {
        if (isPreparePlaying) return;
        isPreparePlaying = true;

        if (textureView.getParent() != null) {
            ((ViewGroup) textureView.getParent()).removeView(textureView);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutVideo.addView(textureView, params);
        layoutVideo.setVisibility(View.VISIBLE);
    }

    public void stopCurrentVideo() {
        layoutVideo.setVisibility(View.GONE);
        layoutVideo.removeAllViews();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Surface s = new Surface(surface);
        // play video
        layoutVideo.setVisibility(View.VISIBLE);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                isPreparePlaying = false;
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                L.e("ERROR : play video in MediaPlayer - what=[" + what + "] - extra=[" + extra + "]");
                isPreparePlaying = false;
                return false;
            }
        });

        mediaPlayer.setVolume(1, 1);
        mediaPlayer.setLooping(true);
        mediaPlayer.setSurface(s);
        try {
            final File videosDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/pikis/");
            final File tmpFile = new File(videosDir, "out.mp4");
            FileInputStream fis = new FileInputStream(tmpFile.getAbsolutePath());
            FileDescriptor fd = fis.getFD();
            long size = fis.getChannel().size();

            if (fd != null) {
                mediaPlayer.setDataSource(fd, 0, size);
                mediaPlayer.prepareAsync();
                isPlaying = true;
            } else {
                L.e("ERROR WITH FILE DESCRIPTOR");
                isPlaying = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            isPlaying = false;
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private Runnable recordVideoRunnable = new Runnable() {
        @Override
        public void run() {
            if (isDown && !isRecording) {
                isRecording = true;
                edittextePhoto.setVisibility(View.GONE);

                if (!prepareMediaRecorder()) {
                    Toast.makeText(CaptureActivity.this, "Fail in recording the video ! Please try again.", Toast.LENGTH_LONG).show();
                    isRecording = false;
                }

                handler.post(runnableTimeRecording);

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            mediaRecorder.start();
                        } catch (final Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }
    };
}
