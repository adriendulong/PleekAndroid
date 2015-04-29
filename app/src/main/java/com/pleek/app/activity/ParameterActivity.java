package com.pleek.app.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.DownTouchListener;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.common.Constants;
import com.pleek.app.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by nicolas on 25/02/15.
 */
public class ParameterActivity extends ParentActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private TextView txtVersion;
    private View btnTopBar;
    private View btnUsername;
    private View btnNotification;
    private View btnPopularAccounts;
    private View btnName;
    private View btnCreatePleekId;
    private TextView txtUsername;
    private TextView txtName;
    private CheckBox chbxNotif;
    private View btnEmail;
    private View btnTwitter;
    private View btnSendapp;
    private View txtGoandup;
    private View hand;
    private RelativeLayout layoutShare;
    private RelativeLayout layoutOverlayShare;
    private View btnConfirmShare;
    private TextView txtUsernamePleekId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter);

        setup();
        init();
    }

    private void setup() {
        int colorDown = getResources().getColor(R.color.overlay);
        txtVersion = (TextView) findViewById(R.id.txtVersion);
        txtVersion.setText("V "+ Utile.getVersionName(this));
        btnTopBar = findViewById(R.id.btnTopBar);
        btnTopBar.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.firstColorDark), getResources().getColor(R.color.firstColorLight)));
        btnTopBar.setOnClickListener(this);
        btnUsername = findViewById(R.id.btnUsername);
        btnUsername.setOnTouchListener(new DownTouchListener(colorDown));
        btnUsername.setOnClickListener(this);
        btnName = findViewById(R.id.btnName);
        btnName.setOnTouchListener(new DownTouchListener(colorDown));
        btnName.setOnClickListener(this);
        btnPopularAccounts = findViewById(R.id.btnPopularAccounts);
        btnPopularAccounts.setOnTouchListener(new DownTouchListener(colorDown));
        btnPopularAccounts.setOnClickListener(this);
        txtUsername = (TextView) findViewById(R.id.txtUserName);
        txtName = (TextView) findViewById(R.id.txtName);
        btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnTouchListener(new DownTouchListener(colorDown));
        btnNotification.setOnClickListener(this);
        chbxNotif = (CheckBox) findViewById(R.id.chbxNotif);
        chbxNotif.setOnCheckedChangeListener(this);
        btnEmail = findViewById(R.id.btnEmail);
        btnEmail.setOnTouchListener(new DownTouchListener(colorDown));
        btnEmail.setOnClickListener(this);
        btnTwitter = findViewById(R.id.btnTwitter);
        btnTwitter.setOnTouchListener(new DownTouchListener(colorDown));
        btnTwitter.setOnClickListener(this);
        btnSendapp = findViewById(R.id.btnSendapp);
        btnSendapp.setOnTouchListener(new DownTouchListener(colorDown));
        btnSendapp.setOnClickListener(this);
        txtGoandup = findViewById(R.id.txtGoandup);
        txtGoandup.setOnClickListener(this);
        hand = findViewById(R.id.hand);
        btnConfirmShare = findViewById(R.id.btnConfirmShare);
        btnConfirmShare.setOnClickListener(this);
        layoutShare = (RelativeLayout) findViewById(R.id.layoutShare);
        layoutOverlayShare = (RelativeLayout) findViewById(R.id.layoutOverlayShare);
        layoutOverlayShare.setOnClickListener(this);
        btnCreatePleekId = findViewById(R.id.btnCreatePleekID);
        btnCreatePleekId.setOnClickListener(this);
        txtUsernamePleekId = (TextView) findViewById(R.id.txtUserNameId);
        //hand.startAnimation(AnimationUtils.loadAnimation(this, R.anim.hand_point));
    }

    final Handler handHandler = new Handler();
    Runnable handRunnable = null;
    private void init() {
        chbxNotif.setChecked(ParseInstallation.getCurrentInstallation().getBoolean("notificationsEnabled"));
        updateUser();

//        handRunnable = new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                hand.setVisibility(View.VISIBLE);
//            }
//        };
//        handHandler.postDelayed(handRunnable, 5000);
    }

    @Override
    public void onClick(View view) {
        if (view == btnTopBar) {
            finish();
        } else if (view == btnUsername) {
            startActivityForResult(new Intent(this, ChangeUsernameActivity.class), 0);
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        } else if (view == btnName) {
            startActivityForResult(new Intent(this, ChangeNameActivity.class), 0);
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        } else if (view == btnNotification) {
            chbxNotif.setChecked(!chbxNotif.isChecked());
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("notificationsEnabled", !chbxNotif.isChecked());
            installation.saveInBackground();
        } else if (view == btnEmail) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.param_mail_to)});
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.param_mail_subject));
            i.putExtra(Intent.EXTRA_TEXT, getString(R.string.param_mail_texte));

            startActivity(Intent.createChooser(i, getString(R.string.param_mail)));
        } else if (view == btnTwitter) {
            String text = getString(R.string.param_twitter_texte);

            Intent tweetIntent = new Intent(Intent.ACTION_SEND);
            tweetIntent.putExtra(Intent.EXTRA_TEXT, text);
            tweetIntent.setType("text/plain");

            PackageManager packManager = getPackageManager();
            List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent,  PackageManager.MATCH_DEFAULT_ONLY);

            boolean resolved = false;
            for (ResolveInfo resolveInfo: resolvedInfoList) {
                if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                    tweetIntent.setClassName( resolveInfo.activityInfo.packageName,  resolveInfo.activityInfo.name );
                    resolved = true;
                    break;
                }
            }

            if (resolved) {
                startActivity(tweetIntent);
            } else {
                Intent i = new Intent();
                i.putExtra(Intent.EXTRA_TEXT, text);
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://twitter.com/intent/tweet?text="+text));
                startActivity(i);
            }
        } else if (view == btnSendapp) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_TEXT, getString(R.string.param_sendapp_text));
            i.setType("text/plain");
            startActivity(Intent.createChooser(i, getString(R.string.param_sendapp)));
        } else if (view == txtGoandup) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://bit.ly/goandup_pleek")));
        } else if (view == btnPopularAccounts) {
            Intent intent = new Intent(this, AddUserActivity.class);
            intent.putExtra(Constants.EXTRA_FROM_FRIENDS, true);
            startActivity(intent);
        } else if (view == btnCreatePleekId) {
            showShareLayout();
        } else if (view == layoutOverlayShare) {
            hideShareLayout();
        } else if (view == btnConfirmShare) {
            sharePleekId();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        ParseInstallation.getCurrentInstallation().put("notificationsEnabled", b);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        updateUser();
    }

    private void updateUser() {
        ParseUser user = ParseUser.getCurrentUser();

        if (user != null) {
            txtUsername.setVisibility(View.VISIBLE);
            txtUsername.setText(user.getUsername());
            txtUsernamePleekId.setText("@" + user.getUsername());

            if (!StringUtils.isStringEmpty(user.getString("name"))) {
                txtName.setVisibility(View.VISIBLE);
                txtName.setText(user.getString("name"));
            }
        }
    }

    private final int DURATION_SHOWSHARE_ANIM = 300;//ms

    private boolean shareLayoutShow;
    private void showShareLayout()  {
        if (!shareLayoutShow) {
            shareLayoutShow = true;

            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_share);
            anim.setFillAfter(true);
            anim.setDuration(DURATION_SHOWSHARE_ANIM);
            layoutShare.startAnimation(anim);
            btnConfirmShare.setVisibility(View.GONE);

            anim.setAnimationListener(new Animation.AnimationListener()
            {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation)
                {
                    Animation animBtn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popin);
                    animBtn.setDuration(400);
                    animBtn.setInterpolator(new OvershootInterpolator(5));
                    btnConfirmShare.startAnimation(animBtn);
                    btnConfirmShare.setVisibility(View.VISIBLE);
                }
            });

            Utile.fadeIn(layoutOverlayShare, DURATION_SHOWSHARE_ANIM);
        }
    }

    private void hideShareLayout() {
        if (shareLayoutShow) {
            shareLayoutShow = false;

            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_share_reverse);
            anim.setFillAfter(true);
            anim.setDuration(DURATION_SHOWSHARE_ANIM);
            layoutShare.startAnimation(anim);
            Utile.fadeOut(layoutOverlayShare, DURATION_SHOWSHARE_ANIM);
        }
    }

    ///// SHARE PIKI //////
    private File lastSharefile;
    private void sharePleekId()  {
        Dialog dialog = showLoader();

        if (layoutShare.getChildCount() > 0) {
            if (lastSharefile == null || !lastSharefile.exists() || layoutShare.findViewById(R.id.layoutReplies) == null) {
                View view = layoutShare.getChildAt(0);
                view.setDrawingCacheEnabled(true);
                view.buildDrawingCache();
                Bitmap bitmap = view.getDrawingCache();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                File imagesDir = new File(getFilesDir().getAbsolutePath() + "/images/");
                if (!imagesDir.exists()) {
                    imagesDir.mkdir();
                }

                lastSharefile = new File(imagesDir, "pleekid.jpg");
                FileOutputStream fos = null;

                try {
                    fos = new FileOutputStream(lastSharefile);
                    fos.write(bytes.toByteArray());
                } catch (IOException e) {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch(Exception e1){}
                    }
                    e.printStackTrace();
                }

                if (lastSharefile != null) {
                    lastSharefile.setReadable(true, false);
                    lastSharefile.deleteOnExit();
                }
            }

            if (lastSharefile != null || !lastSharefile.exists()) {
                Uri uriOfImage = FileProvider.getUriForFile(this, "com.pleek.app.fileprovider", lastSharefile);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");
                share.putExtra(Intent.EXTRA_STREAM, uriOfImage);
                share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.param_pleekid_iamonpleek));

                startActivity(Intent.createChooser(share, getString(R.string.share_title)));

                hideDialog(dialog);

                if (layoutShare.findViewById(R.id.layoutReplies) == null)
                    lastSharefile = null;
            } else {
                Utile.showToast(R.string.piki_share_impossible, this);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (shareLayoutShow) {
                hideShareLayout();
                return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
