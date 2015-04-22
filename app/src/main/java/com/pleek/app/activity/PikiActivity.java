package com.pleek.app.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AppEventsConstants;
import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Screen;
import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.ButtonRoundedMaterialDesign;
import com.goandup.lib.widget.CameraView;
import com.goandup.lib.widget.DisableTouchListener;
import com.goandup.lib.widget.DownTouchListener;
import com.goandup.lib.widget.EditTextFont;
import com.goandup.lib.widget.SwipeRefreshLayoutScrollingOff;
import com.goandup.lib.widget.TextViewFont;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pleek.app.R;
import com.pleek.app.adapter.ReactAdapter;
import com.pleek.app.bean.Emoji;
import com.pleek.app.bean.Font;
import com.pleek.app.bean.Overlay;
import com.pleek.app.bean.Piki;
import com.pleek.app.bean.Reaction;
import com.pleek.app.bean.VideoBean;
import com.pleek.app.bean.ViewLoadingFooter;
import com.pleek.app.utils.PicassoUtils;
import com.pleek.app.views.CustomGridView;
import com.pleek.app.views.EmojisFontsPopup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by nicolas on 18/12/14.
 */
public class PikiActivity extends ParentActivity implements View.OnClickListener, ReactAdapter.Listener, SurfaceHolder.Callback, VideoBean.LoadVideoEndListener, CameraView.ListenerStarted
{
    private final int DURATION_SHOWSHARE_ANIM = 300;//ms

    private View rootView;
    private View btnBack;
    private View btnShare;
    private TextView txtNamePiki;
    private CustomGridView gridViewPiki;

    // HEADER
    private TextView txtNbFriend;
    private ImageView imgEmojiEarth;
    private View pikiHeader;
    private ImageView imgPiki;
    private SurfaceView surfaceView;
    private ImageView imgPlay;
    private ImageView imgMute;
    private ImageView imgError;
    private ProgressBar progressBar;
    private MediaPlayer mediaPlayer;
    private RelativeLayout layoutVideo;
    private boolean isPreparePlaying;
    private boolean isPlaying = false;

    private TextView txtTroisPoints;
    private View layoutOverlayShare;
    private LinearLayout layoutShare;
    private ButtonRoundedMaterialDesign btnConfrimShare;
    private View layoutOverlayReply;
    private View layoutOverlayReplyTop;
    private View layoutTextReact;
    private EditTextFont edittexteReact;
    private View removeFocus;
    private View layoutCamera;
    private CameraView cameraView;
    private ViewLoadingFooter footer;
    private SwipeRefreshLayoutScrollingOff refreshSwipe;

    // NEW ELEMENTS
    private LinearLayout layoutTutorialReact;
    private View imgAddReact;
    private RelativeLayout layoutActionBarKeyboard;
    private ImageView imgStickers;
    private ImageView imgFonts;
    private ImageView imgSwitch;
    private ImageView imgViewReact;
    private ImageView imgReply;

    private static Piki _piki;
    private Piki piki;
    private ReactAdapter adapter;
    private int initialX;
    private List<Reaction> listReact;
    private boolean isKeyboardShow;
    private int keyboardHeight;
    private boolean isPreviewVisible;

    private int initMarginBottom = 0;

    private List<Emoji> emojis;
    private List<Font> fonts;
    private EmojisFontsPopup<Emoji> popupEmoji;
    private EmojisFontsPopup<Font> popupFont;

    public static void initActivity(Piki piki) {
        _piki = piki;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piki);

        setup();
        init();

        mixpanel.track("View Piki", null);
        fbAppEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT);
    }

    private void setup() {
        if (_piki != null) {
            piki = _piki;
            _piki = null;
        }

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
        btnBack.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.firstColorDark)));

        btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(this);
        int colorDown = getResources().getColor(R.color.firstColorDark);
        int colorUp = getResources().getColor(R.color.firstColor);
        btnShare.setOnTouchListener(new DownTouchListener(colorDown, colorUp));

        txtNamePiki = (TextView)findViewById(R.id.txtNamePiki);
        gridViewPiki = (CustomGridView) findViewById(R.id.gridViewPiki);
        gridViewPiki.setOnTouchListener(new MyListTouchListener());
        gridViewPiki.setOnScrollListener(new MyOnScrollListener());

        //header
        pikiHeader = LayoutInflater.from(this).inflate(R.layout.item_piki_header, null, false);
        pikiHeader.setLayoutParams(new AbsListView.LayoutParams(screen.getWidth(), screen.getWidth()));
        imgPiki = (ImageView) pikiHeader.findViewById(R.id.imgPiki);
        imgPiki.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgEmojiEarth = (ImageView) pikiHeader.findViewById(R.id.imgEmojiEarth);
        txtNbFriend = (TextView) pikiHeader.findViewById(R.id.txtNbFriend);
        txtNbFriend.setVisibility(View.GONE);
        if (!piki.isPublic()) {
            txtNbFriend.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.secondColor), getResources().getColor(R.color.blanc)));
            txtNbFriend.setOnClickListener(this);
        }

        imgMute = new ImageView(this);
        imgMute.setImageResource(R.drawable.picto_mute);

        imgPlay = (ImageView) pikiHeader.findViewById(R.id.imgPlay);
        if (piki.isVideo()) {
            imgPlay.setVisibility(View.VISIBLE);
            piki.loadVideoToTempFile(this);
            piki.setLoadVideoEndListener(this);
        }
        imgError = (ImageView) pikiHeader.findViewById(R.id.imgError);
        progressBar = (ProgressBar) pikiHeader.findViewById(R.id.progressBar);
        surfaceView = new SurfaceView(this);
        surfaceView.getHolder().addCallback(this);
        surfaceView.setZOrderOnTop(true);
        layoutVideo = (RelativeLayout) pikiHeader.findViewById(R.id.layoutVideo);

        txtTroisPoints = (TextView) pikiHeader.findViewById(R.id.txtTroisPoints);
        txtTroisPoints.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.secondColor), getResources().getColor(R.color.blanc)));
        txtTroisPoints.setOnClickListener(this);
        gridViewPiki.addHeaderView(pikiHeader);

        //footer listview
        footer = new ViewLoadingFooter(this);

        //share
        layoutOverlayShare = findViewById(R.id.layoutOverlayShare);
        layoutOverlayShare.setVisibility(View.GONE);
        layoutOverlayShare.setOnTouchListener(new DisableTouchListener());
        layoutShare = (LinearLayout) findViewById(R.id.layoutShare);
        btnConfrimShare = (ButtonRoundedMaterialDesign) findViewById(R.id.btnConfrimShare);
        btnConfrimShare.setOnPressedListener(new ButtonRoundedMaterialDesign.OnPressedListener()
        {
            @Override
            public void endPress(ButtonRoundedMaterialDesign button)
            {
                sharePiki();
            }
        });

        //reply
        layoutOverlayReply = findViewById(R.id.layoutOverlayReply);
        layoutOverlayReply.setOnClickListener(this);
        layoutOverlayReplyTop = findViewById(R.id.layoutOverlayReplyTop);
        layoutOverlayReplyTop.setOnClickListener(this);

        int edittexteReactPadding = screen.dpToPx(10);
        edittexteReact = (EditTextFont) findViewById(R.id.edittexteReact);
        edittexteReact.setSingleLine(false);
        int size = screen.getWidth() / 2 - edittexteReactPadding;
        layoutTextReact = findViewById(R.id.layoutTextReact);
        edittexteReact.setPadding(edittexteReactPadding, edittexteReactPadding, edittexteReactPadding, edittexteReactPadding);
        removeFocus = findViewById(R.id.removeFocus);

        layoutCamera = findViewById(R.id.layoutCamera);
        layoutCamera.setOnClickListener(this);

        pikiHeader.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int layoutCameraSize = screen.getWidth() / 2;
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutCamera.getLayoutParams();
                params.width = layoutCameraSize;
                params.height = layoutCameraSize - screen.dpToPx(1);
                initMarginBottom = (int) (rootView.getHeight() - pikiHeader.getHeight() - layoutCameraSize - 60 * screen.getDensity());
                params.setMargins(0, 0, 0, initMarginBottom); // 60 is the size of the share layout
                layoutCamera.setLayoutParams(params);

                pikiHeader.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        cameraView = (CameraView) findViewById(R.id.cameraView);
        cameraView.setCycleListener(this);

        // Open/Close Keyboard detection
        rootView = findViewById(R.id.rootView);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                keyboardHeight = rootView.getRootView().getHeight() - r.bottom;

                if (keyboardHeight > 100 && !isKeyboardShow) { //keyboard SHOW
                    isKeyboardShow = true;
                    Utile.fadeIn(layoutActionBarKeyboard, 300);

                    RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) rootView.getLayoutParams();
                    p.topMargin =  - pikiHeader.getHeight() - gridViewPiki.getChildAt(0).getTop() + btnShare.getHeight();
                    rootView.setLayoutParams(p);

                    if (popupEmoji == null) {
                        setUpPopupEmojis();
                    }

                    if (popupFont == null) {
                        setUpPopupFonts();
                    }
                }

                if (keyboardHeight > 100 && isKeyboardShow && popupEmoji != null && popupEmoji.isShowing()) {
                    popupEmoji.update(Screen.getInstance(PikiActivity.this).getWidth(), keyboardHeight);
                }

                if (keyboardHeight <= 100 && isKeyboardShow) { //keyboard HIDE
                    isKeyboardShow = false;
                    layoutActionBarKeyboard.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) rootView.getLayoutParams();
                    p.topMargin = btnShare.getHeight();
                    rootView.setLayoutParams(p);

                    isReplyButtonsShow = false;
                    endEditText();
                }
            }
        });

        adapter = new ReactAdapter(new ArrayList<Reaction>(), PikiActivity.this, piki.iamOwner());
        //wait gridViewPiki is created for get height
        gridViewPiki.post(new Runnable() {
            @Override
            public void run() {
                adapter.setHeightListView(gridViewPiki.getHeight());
                gridViewPiki.setAdapter(adapter);
            }
        });

        isPreviewVisible = true;

        refreshSwipe = (SwipeRefreshLayoutScrollingOff) findViewById(R.id.refreshSwipe);
        refreshSwipe.setColorSchemeResources(R.color.secondColor, R.color.firstColor, R.color.secondColor, R.color.firstColor);
        refreshSwipe.setScrollingEnabled(false);

        // NEW ELEMENTS
        layoutTutorialReact = (LinearLayout) findViewById(R.id.layoutTutorialReact);
        imgAddReact = findViewById(R.id.imgAddReact);
        layoutActionBarKeyboard = (RelativeLayout) findViewById(R.id.layoutActionBarKeyboard);
        imgFonts = (ImageView) findViewById(R.id.imgFonts);
        imgFonts.setOnClickListener(this);
        imgStickers = (ImageView) findViewById(R.id.imgStickers);
        imgStickers.setOnClickListener(this);
        imgSwitch = (ImageView) findViewById(R.id.imgSwitch);
        imgSwitch.setOnClickListener(this);
        imgViewReact = (ImageView) findViewById(R.id.imgViewReact);
        imgReply = (ImageView) findViewById(R.id.imgReply);
        imgReply.setOnClickListener(this);
    }


    private void init() {
        init(true);
    }
    private void init(boolean withCache) {
        if (piki != null) {
            //mark read
            readDataProvider.setReadDateNow(piki.getId());

            //header
            txtNbFriend.setVisibility(View.VISIBLE);
            if (!piki.isPublic()) {
                txtNbFriend.setText(piki.getNbRecipient() + " " + getString(R.string.piki_friend) + (piki.getNbRecipient() > 1 ? "s" : ""));
            } else {
                txtNbFriend.setText(getString(R.string.piki_public));
                imgEmojiEarth.setVisibility(View.VISIBLE);
            }

            txtNamePiki.setText(piki.getName());
            PicassoUtils.with(this)
                    .load(piki.getUrlPiki())
                    .resize((int) (pikiHeader.getLayoutParams().width * 0.80), (int) (pikiHeader.getLayoutParams().width * 0.80))
                    .placeholder(R.drawable.piki_placeholder)
                    .error(R.drawable.piki_placeholder)
                    .into(imgPiki);

            currentPage = 0;
            listReact = new ArrayList<Reaction>();

            final boolean loading = loadNext(withCache);

            refreshSwipe.post(new Runnable() {
                @Override
                public void run() {
                    refreshSwipe.setRefreshing(loading);
                }
            });

            //count nombre react
            ParseQuery<ParseObject> query = ParseQuery.getQuery("React");
            query.whereEqualTo("Piki", piki.getParseObject());
            query.countInBackground(new CountCallback()
            {
                public void done(int count, ParseException e)
                {
                    // TODO NB REPLIES
                }
            });

            emojis = new ArrayList<Emoji>();

            ParseQuery<ParseObject> queryStickers = ParseQuery.getQuery("stickers");
            queryStickers.addAscendingOrder("priorite");
            queryStickers.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            queryStickers.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null && parseObjects != null) {
                        for (ParseObject parseSticker : parseObjects) {
                            emojis.add(new Emoji(parseSticker));
                        }
                    } else if(e != null) {
                        Utile.showToast("Error getting stickers", PikiActivity.this);
                        e.printStackTrace();
                    }
                }
            });
        }
        else
        {
            L.e("Erreur : Activity not init, call PikiActivity.initActivity()");
            finish();
        }
    }

    private boolean fromCache;
    private int currentPage;
    private final int NB_BY_PAGE = 30;
    private List<Reaction> listBeforreRequest;
    private boolean isLoading;
    private boolean endOfLoading;
    private boolean loadNext(boolean withCache)
    {
        if(isLoading) return false;

        //il n'y a plus rien a charger
        if(endOfLoading) return false;

        if (footer.getParent() != null) {
            ViewGroup par = (ViewGroup) footer.getParent();
            par.removeView(footer);
        }
        gridViewPiki.addFooterView(footer);

        if(listReact == null) listReact = new ArrayList<Reaction>();
        listBeforreRequest = new ArrayList<Reaction>(listReact);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("React");
        query.setCachePolicy(withCache ? ParseQuery.CachePolicy.CACHE_THEN_NETWORK : ParseQuery.CachePolicy.NETWORK_ONLY);
        query.include("user");
        query.whereEqualTo("Piki", piki.getParseObject());
        query.orderByDescending("createdAt");
        query.setSkip(currentPage*NB_BY_PAGE);
        query.setLimit(currentPage > 0 ? NB_BY_PAGE : NB_BY_PAGE-1);//first laod que 29. pour faire pile 10 lignes.

        isLoading = true;
        fromCache = withCache;

        query.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e)
            {
                if(e == null && parseObjects != null)
                {
                    if(!fromCache) currentPage++;

                    listReact = new ArrayList<Reaction>(listBeforreRequest);
                    for (ParseObject parsePiki : parseObjects)
                    {
                        listReact.add(new Reaction(parsePiki));
                    }
                    adapter.setListReact(listReact);

                    //si moins de résultat que d'el par page alors c'est la dernière page
                    endOfLoading = parseObjects.size() < (currentPage > 1 ? NB_BY_PAGE : NB_BY_PAGE-1);//pour first load

                    //show tuto
                    //if (listReact != null && listReact.size() > 0) showTuto();

                }
                else if(e != null)
                {
                    if(!fromCache) Utile.showToast(R.string.piki_react_nok, PikiActivity.this);
                    e.printStackTrace();
                }

                if(!fromCache)
                {
                    refreshSwipe.setRefreshing(false);
                    gridViewPiki.removeFooterView(footer);
                    gridViewPiki.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    isLoading = false;
                }

                fromCache = false;
            }
        });

        return true;
    }

    @Override
    public void clickOnReaction(Reaction react) {
        if (react.isTmpReaction() && react.isLoadError()) {
            adapter.markLoadError(react, false);
            sendReact(react);
        }
    }

    @Override
    public void doubleTapReaction(Reaction react) {
        // NOTHING HERE
    }

    @Override
    public void showPlaceHolderItem(boolean show) {
        gridViewPiki.setScrollingEnabled(!show);
    }

    @Override
    public void onPlayVideo() {
        if (isPlaying) stopCurrentVideo();
    }

    @Override
    public void onDeleteReact(final int position) {
        showDialog(R.string.piki_popup_remove_title, R.string.piki_popup_remove_texte, new MyDialogListener() {
            @Override
            public void closed(boolean accept) {
                // DELETE
                if (accept) {
                    reportOrRemoveReact(adapter.removeReaction(position));
                }
            }
        });
    }

    @Override
    public void onReportReact(final int position) {
        showDialog(R.string.piki_popup_report_title, R.string.piki_popup_report_texte, new MyDialogListener() {
            @Override
            public void closed(boolean accept) {
                // REPORT
                if (accept) {
                    reportOrRemoveReact(adapter.getReaction(position));
                }
            }
        });
    }

    @Override
    public void onAddFriend(int position, final Reaction react, final ProgressBar pg, final ImageView img, final TextViewFont txt) {
        pg.setVisibility(View.VISIBLE);
        final String friendId = adapter.getReaction(position).getUserId();
        Set<String> friendsIds = getFriendsPrefs();

        final FunctionCallback callback = new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    getFriendsBg(new FunctionCallback() {
                        @Override
                        public void done(Object o, ParseException e) {
                            if (getFriendsPrefs() != null && getFriendsPrefs().contains(react.getUserId())) {
                                txt.setTextColor(Color.BLACK);
                                img.setVisibility(View.VISIBLE);
                                img.setImageResource(R.drawable.picto_added);
                            } else {
                                img.setVisibility(View.VISIBLE);
                                txt.setTextColor(getResources().getColor(R.color.grisTextDisable));
                                img.setImageResource(R.drawable.picto_add_user);
                            }

                            pg.setVisibility(View.GONE);
                        }
                    });
                } else {
                    pg.setVisibility(View.GONE);
                    img.setVisibility(View.VISIBLE);
                    Utile.showToast(R.string.pikifriends_action_nok, PikiActivity.this);
                }
            }
        };

        if (!friendsIds.contains(friendId)) {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("friendId", friendId);
            ParseCloud.callFunctionInBackground("addFriendV2", param, new FunctionCallback<Object>() {
                @Override
                public void done(Object o, ParseException e) {
                    callback.done(o, e);
                }
            });
        } else {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("friendId", friendId);
            ParseCloud.callFunctionInBackground("removeFriendV2", param, new FunctionCallback<Object>() {
                @Override
                public void done(Object o, ParseException e) {
                    callback.done(o, e);
                }
            });
        }
    }

    @Override
    public void onPreviewReact(int position) {

    }

    @Override
    public void onLikeReact(int position) {

    }

    private void reportOrRemoveReact(Reaction react) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("reactId", react.getId());
        ParseCloud.callFunctionInBackground("reportOrRemoveReact", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) Utile.showToast(R.string.piki_react_remove_nok, PikiActivity.this);
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        if (isPlaying) stopCurrentVideo();
        adapter.stopCurrentVideo();

        if (view == btnBack) {
            finish();
        } else if (view == btnShare) {
            generateShareLayout();
            showShareLayout();

            //send track mixpanel
            try
            {
                JSONObject props = new JSONObject();
                props.put("Canal", "Unknow");
                mixpanel.track("Share", props);
            }
            catch (JSONException ee)
            {
                ee.printStackTrace();
            }
        } else if (view == txtTroisPoints) {
            showDialog(R.string.piki_report_title, R.string.piki_report_texte, new MyDialogListener() {
                @Override
                public void closed(boolean accept) {
                    if (accept) {
                        Map<String, Object> param = new HashMap<String, Object>();
                        param.put("piki", piki.getId());
                        ParseCloud.callFunctionInBackground("reportPiki", param, new FunctionCallback<Object>() {
                            @Override
                            public void done(Object o, ParseException e) {
                                if (e == null)
                                    Utile.showToast(R.string.piki_report_ok, PikiActivity.this);
                                else Utile.showToast(R.string.piki_report_nok, PikiActivity.this);
                            }
                        });
                    }
                }
            });
        } else if (view == txtNbFriend) {
            PikiFriendsActivity.initActivity(piki);
            startActivity(new Intent(this, PikiFriendsActivity.class));
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        } else if (view == layoutOverlayReply || view == layoutOverlayReplyTop) {
            if (isKeyboardShow) {
                endEditText();
            } else {
                showReplyButtons(false);
            }
        } else if (view == layoutCamera) {
            imgAddReact.setVisibility(View.GONE);
            layoutTutorialReact.setVisibility(View.GONE);
            isReplyButtonsShow = true;
            startEditText();
        } else if (view == imgStickers) {
            if (!popupEmoji.isShowing()) {
                imgStickers.setImageResource(R.drawable.picto_stickers_selected_selector);
                if (popupEmoji.isKeyBoardOpen()) {
                    popupEmoji.showAtBottom();
                } else {
                    popupEmoji.showAtBottomPending();
                }

                edittexteReact.setTranslationY(Screen.getInstance(PikiActivity.this).getWidth());

                if (popupFont.isShowing()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideFonts();
                        }
                    }, 300);
                }
            } else {
                hideEmojis();
            }
        } else if (view == imgFonts) {
            if (!popupFont.isShowing()) {
                imgFonts.setImageResource(R.drawable.picto_fonts_selected_selector);
                if (popupFont.isKeyBoardOpen()) {
                    popupFont.showAtBottom();
                } else {
                    popupFont.showAtBottomPending();
                }

                if (popupEmoji.isShowing()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideEmojis();
                        }
                    }, 300);
                }
            } else {
                hideFonts();
            }
        } else if (view == imgSwitch) {
            toggleCamera();
        } else if (view == imgReply) {
            final Bitmap bitmapLayerReact = getBitmapLayerReact();
            final Dialog dialogLoader = showLoader();
            cameraView.captureCamera(new CameraView.CameraViewListener() {
                @Override
                public void repCaptureCamera(Drawable photo) {
                    byte[] reactData = getReactData(photo, bitmapLayerReact);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(reactData, 0, reactData.length);

                    final Reaction tmpReact = new Reaction(ParseUser.getCurrentUser().getUsername(), bitmap);
                    tmpReact.setNameUser(ParseUser.getCurrentUser().getUsername());
                    Reaction.Type type = Reaction.Type.PHOTO;
                    if (layoutTextReact.getVisibility() == View.VISIBLE) type = Reaction.Type.TEXTE;
                    else if (imgViewReact.getVisibility() == View.VISIBLE) type = Reaction.Type.EMOJI;
                    tmpReact.setType(type);
                    listReact = adapter.addReact(tmpReact);
                    sendReact(tmpReact);

                    hideDialog(dialogLoader);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            endEditText();
                        }
                    }, 500);
                }
            });
        }
    }

    private void hideEmojis() {
        imgViewReact.setVisibility(View.GONE);
        imgViewReact.setImageDrawable(null);
        startEditText();
        imgStickers.setImageResource(R.drawable.picto_stickers_selector);
        popupEmoji.dismiss();

        edittexteReact.setTranslationY(0);
    }

    private void hideFonts() {
        imgViewReact.setVisibility(View.GONE);
        imgViewReact.setImageDrawable(null);
        startEditText();
        imgFonts.setImageResource(R.drawable.picto_fonts_selector);
        popupFont.dismiss();
    }

    public void startCamera() {
        if (cameraView != null && cameraView.getCamera() == null) {
            cameraView.setFaceCamera(pref.getBoolean("selfie_camera", true));
            cameraView.start();
        }
    }

    public boolean toggleCamera() {
        boolean toggled = false;

        if (cameraView != null && cameraView.toggleFaceCamera()) {
            toggled = true;

            //save
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("selfie_camera", cameraView.isFaceCamera());
            editor.commit();
        }

        return toggled;
    }

    @Override
    public void finish() {
        super.finish();
        Reaction.deleteAllTempFileVideo(this);
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(edittexteReact.getWindowToken(), 0);
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }

    @Override
    public void done(boolean ok, VideoBean react) {
        playVideo();
    }

    @Override
    public void started() {
        if (!isReplyButtonsShow) {
            imgAddReact.setVisibility(View.VISIBLE);
            layoutTutorialReact.setVisibility(View.VISIBLE);
        }
    }

    private class MyListTouchListener implements View.OnTouchListener {
        private final int DURATION_DELETE_ANIM = 300;//ms
        private int WIDTH_BORDER_BACKVIEW;// 3/5 d'1/3 de l'écran
        private int WIDTH_BORDER_BACKVIEW_START_ALPHA;// WIDTH_BORDER_BACKVIEW / 2

        private float downX = 0, downY = 0;

        private View downItem;

        private MyListTouchListener() {
            WIDTH_BORDER_BACKVIEW = (Screen.getWidth(PikiActivity.this) / 3) * 3/5;
            WIDTH_BORDER_BACKVIEW_START_ALPHA = WIDTH_BORDER_BACKVIEW >> 1;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            try { //fix : crash #36
                int action = event.getAction();

                if (action == MotionEvent.ACTION_DOWN) {
                    downX = event.getX();
                    downY = event.getY();
                }

                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {

                    if (action == MotionEvent.ACTION_UP && gridViewPiki.getChildAt(0) != null && gridViewPiki.getFirstVisiblePosition() == 0
                            && event.getY() < pikiHeader.getBottom() + gridViewPiki.getChildAt(0).getTop() && event.getY() > pikiHeader.getTop()
                            && event.getX() > pikiHeader.getLeft() && event.getX() < pikiHeader.getRight()
                            && Math.abs(downX - event.getX()) < 30 && Math.abs(downY - event.getY()) < 30) {
                        if (piki.isVideo()) {
                            if (isPlaying) stopCurrentVideo();
                            else playVideo();
                        }
                    }
                }
            } catch (Exception e) {
                L.w("ERROR  >>  MyListTouchListener e="+e.getMessage());
            }

            return false;//no consume touch event
        }
    }

    private int lastItemShow;
    private class MyOnScrollListener implements AbsListView.OnScrollListener {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState)
        {
            if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
            {
                //start scroll
                if (isPlaying) stopCurrentVideo();
                adapter.stopCurrentVideo();
            }
            else if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
            {
                //end scroll
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            //anim layout CameraView
            if((view != null && view.getChildAt(1) != null)) {
                final boolean isVisible = firstVisibleItem <= 1;

                Runnable updateRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (isVisible) {
                            ViewGroup.MarginLayoutParams mlpCamera = (ViewGroup.MarginLayoutParams) layoutCamera.getLayoutParams();
                            mlpCamera.topMargin = (int) (1 * screen.getDensity()) + (pikiHeader.getBottom() + gridViewPiki.getChildAt(0).getTop());

                            layoutCamera.setLayoutParams(mlpCamera);
                            if (isVisible && !isPreviewVisible) {
                                startCamera();
                                imgAddReact.setVisibility(View.GONE);
                                layoutTutorialReact.setVisibility(View.GONE);
                                isPreviewVisible = true;
                            }
                        } else {
                            CameraView.release(new CameraView.ListenerRelease() {
                                @Override
                                public void end() {
                                    isPreviewVisible = false;
                                }
                            });
                        }
                        layoutCamera.setVisibility(isVisible ? View.VISIBLE : View.GONE);
                    }
                };

                if (layoutCamera.getVisibility() == View.GONE) {
                    layoutCamera.post(updateRunnable);//for first must be execute in post (for Piki with no react)
                } else {
                    updateRunnable.run();//all other must be execute same thread
                }
            }

            //pagination
            final int lastItem = firstVisibleItem + visibleItemCount;

            if (lastItem == totalItemCount) {
                if (lastItemShow < lastItem) {
                    if (loadNext(true)) {
                        lastItemShow = lastItem;
                    }
                }
            }
        }
    }


    ///// SHARE PIKI //////
    private final static int PIKI_SHARE_SIZE = 600;
    private File lastSharefile;
    private void sharePiki()
    {
        if(layoutShare.getChildCount() > 0)
        {
            if(lastSharefile == null || !lastSharefile.exists())
            {
                View view = layoutShare.getChildAt(0);
                view.setDrawingCacheEnabled(true);
                view.buildDrawingCache();
                Bitmap bitmap = view.getDrawingCache();
                //bitmap = Bitmap.createScaledBitmap(bitmap, PIKI_SHARE_SIZE, PIKI_SHARE_SIZE, false);//bad quality !
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                File imagesDir = new File(getFilesDir().getAbsolutePath() + "/images/");
                if(!imagesDir.exists())
                {
                    imagesDir.mkdir();
                }

                //String path = getFilesDir().getAbsolutePath() + "/peekee_"+((int)(Math.random()*1000000))+".jpg";
                lastSharefile = new File(imagesDir, "myimages.jpg");
                FileOutputStream fos = null;
                try
                {
                    fos = new FileOutputStream(lastSharefile);
                    fos.write(bytes.toByteArray());
                }
                catch (IOException e)
                {
                    if(fos != null)
                    {
                        try{
                            fos.close();
                        } catch(Exception e1){}
                    }
                    e.printStackTrace();
                }
                if(lastSharefile != null)
                {
                    lastSharefile.setReadable(true, false);
                    lastSharefile.deleteOnExit();
                }
            }

            if(lastSharefile != null || !lastSharefile.exists())
            {
                Uri uriOfImage = FileProvider.getUriForFile(this, "com.pleek.app.fileprovider", lastSharefile);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");
                share.putExtra(Intent.EXTRA_STREAM, uriOfImage);
                share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + " @" + piki.getName());

                startActivity(Intent.createChooser(share, getString(R.string.share_title)));
            }
            else
            {
                Utile.showToast(R.string.piki_share_impossible, this);
            }
        }
    }

    ///// SEND REACT //////
    public void sendReact(final Reaction tmpReact)
    {
        if(tmpReact.getTmpPhotoByte() != null)
        {
            mixpanel.timeEvent("Send React");

            ParseUser currentUser = ParseUser.getCurrentUser();

            //ACL
            List<String> listRecipients = piki.getParseObject().getList("recipients");
            ParseACL acl = new ParseACL();
            if(piki.getParseObject().getBoolean("isPublic"))
            {
                acl.setPublicReadAccess(true);
            }
            else if(listRecipients != null)
            {
                for(String recipientId : listRecipients)
                {
                    acl.setReadAccess(recipientId, true);//all recipients
                }
            }
            acl.setWriteAccess(currentUser, true);
            acl.setReadAccess(currentUser, true);

            //create PIKI
            final ParseObject react = ParseObject.create("React");
            react.put("Piki", piki.getParseObject());
            react.put("photo", new ParseFile("photo.jpg", tmpReact.getTmpPhotoByte()));
            react.put("user", currentUser);
            react.setACL(acl);

            react.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if(e == null)
                    {
                        Reaction newReact = new Reaction(react);
                        newReact.setTmpPhoto(tmpReact.getTmpPhoto());
                        listReact = adapter.addReact(newReact, tmpReact);
                        HomeActivity.AUTO_RELOAD = true;

                        ParseObject pikiParse = piki.getParseObject();

                        //call function savePiki
                        HashMap<String, Object> params = new HashMap<String, Object>();
                        params.put("isPublic", pikiParse.getBoolean("isPublic"));
                        params.put("pikiId", pikiParse.getObjectId());
                        params.put("ownerId", pikiParse.getParseUser("user").getObjectId());
                        params.put("recipients", pikiParse.getList("recipients"));

                        ParseCloud.callFunctionInBackground("sendPushNewComment", params, new FunctionCallback<Object>()
                        {
                            @Override
                            public void done(Object o, ParseException e)
                            {
                                if (e != null)
                                {
                                    Utile.showToast(R.string.piki_savereact_nok, PikiActivity.this);
                                }
                            }
                        });

                        //send track mixpanel
                        try
                        {
                            JSONObject props = new JSONObject();
                            props.put("React Type", tmpReact.getTypeStr());
                            mixpanel.track("Send React", props);
                            mixpanel.getPeople().increment("React Sent", 1);
                            if(tmpReact.getType() == Reaction.Type.PHOTO) mixpanel.getPeople().increment("React Photo Sent", 1);
                            else if(tmpReact.getType() == Reaction.Type.EMOJI) mixpanel.getPeople().increment("React Emoji Sent", 1);
                            else if(tmpReact.getType() == Reaction.Type.VIDEO) mixpanel.getPeople().increment("React Video Sent", 1);
                            else if(tmpReact.getType() == Reaction.Type.TEXTE) mixpanel.getPeople().increment("React Text Sent", 1);

                            //send track fbappevents
                            Bundle parameters = new Bundle();
                            parameters.putString("React Type", tmpReact.getTypeStr());
                            fbAppEventsLogger.logEvent("Send React", parameters);
                        }
                        catch (JSONException ee)
                        {
                            ee.printStackTrace();
                        }
                    }
                    else
                    {
                        adapter.markLoadError(tmpReact, true);
                        Utile.showToast(R.string.piki_savereact_nok, PikiActivity.this);
                    }
                }
            });
        }
        else
        {
            Utile.showToast(R.string.piki_savereact_nok, PikiActivity.this);
        }

    }

    private void generateShareLayout()
    {
        if(piki == null || listReact == null) return;

        layoutShare.removeAllViews();

        int nbReact = listReact.size();
        int nbReactShow;

        int layoutID = R.layout.share0;
        if(nbReact >= (nbReactShow = 44)) layoutID = R.layout.share4;
        else if(nbReact >= (nbReactShow = 24)) layoutID = R.layout.share3;
        else if(nbReact >= (nbReactShow = 11)) layoutID = R.layout.share2;
        else if(nbReact >= (nbReactShow = 6)) layoutID = R.layout.share1;

        int size = screen.dpToPx(310);

        View viewShare = LayoutInflater.from(this).inflate(layoutID, layoutShare, false);
        viewShare.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        viewShare = screen.adapt(viewShare);
        layoutShare.addView(viewShare);

        TextView txtUserName = (TextView) viewShare.findViewById(R.id.txtUserName);
        TextView txtDate = (TextView) viewShare.findViewById(R.id.txtDate);
        TextView txtReplies = (TextView) viewShare.findViewById(R.id.txtReplies);
        txtUserName.setText(piki.getName() + " " + getString(R.string.share_onpleek));
        txtDate.setText(DateFormat.getDateTimeInstance().format(piki.getCreatedAt()));
        if(txtReplies != null) txtReplies.setText(piki.getNbReact()+"+ "+getString(R.string.share_replies));

        ImageView imgPiki = (ImageView)viewShare.findViewById(R.id.imgPiki);
        PicassoUtils.with(this)
                .load(piki.getUrlPiki())
                .placeholder(R.drawable.piki_placeholder)
                .fit()
                .error(R.drawable.piki_placeholder)
                .into(imgPiki);

        for (int i = 0; i < nbReactShow; i++)
        {
            try
            {
                Reaction react = listReact.get(i);

                int id = R.id.class.getField("imgReact" + (i+1)).getInt(0);
                ImageView imgReact = (ImageView) viewShare.findViewById(id);
                if(!react.isTmpReaction())
                {
                    PicassoUtils.with(this)
                            .load(react.getUrlPhoto())
                            .placeholder(R.drawable.piki_placeholder)
                            .fit()
                            .error(R.drawable.piki_placeholder)
                            .into(imgReact);
                }
                else
                {
                    imgReact.setImageBitmap(react.getTmpPhoto());
                }

                View pictoPlay = ((ViewGroup)imgReact.getParent()).getChildAt(1);
                pictoPlay.setVisibility(react.isVideo() ? View.VISIBLE : View.GONE);
            }
            catch (Exception e)
            {
                L.e(">>> ERROR : clicl btnShare : id=[imgReact"+i+"] not existe in layout R.layout.share1");
                e.printStackTrace();
                break;
            }
        }
    }

    ///////////////////////
    ////// ANIMATION //////

    private boolean shareLayoutShow;
    private void showShareLayout()
    {
        if(!shareLayoutShow)
        {
            shareLayoutShow = true;

            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_share);
            anim.setFillAfter(true);
            anim.setDuration(DURATION_SHOWSHARE_ANIM);
            layoutShare.startAnimation(anim);
            btnConfrimShare.setVisibility(View.GONE);

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
                    btnConfrimShare.startAnimation(animBtn);
                    btnConfrimShare.setVisibility(View.VISIBLE);
                }
            });

            Utile.fadeIn(layoutOverlayShare, DURATION_SHOWSHARE_ANIM);
        }
    }

    private void hideShareLayout()
    {
        if(shareLayoutShow)
        {
            shareLayoutShow = false;

            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_share_reverse);
            anim.setFillAfter(true);
            anim.setDuration(DURATION_SHOWSHARE_ANIM);
            layoutShare.startAnimation(anim);
            Utile.fadeOut(layoutOverlayShare, DURATION_SHOWSHARE_ANIM);
        }
    }

    private final int TIME_ANIM = 300;//ms
    private boolean isReplyButtonsShow;
    private void showReplyButtons(final boolean show) {
        // STOP ALL VIDEOS
        if (isPlaying) stopCurrentVideo();
        adapter.stopCurrentVideo();

        isReplyButtonsShow = show;
    }

    private void startEditText() {
        edittexteReact.setVisibility(View.VISIBLE);
        edittexteReact.requestFocus();
        layoutOverlayReply.setVisibility(View.VISIBLE);
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(edittexteReact, 0);
    }

    private void endEditText() {
        edittexteReact.setVisibility(View.GONE);
        edittexteReact.clearFocus();
        edittexteReact.setText("");
        layoutOverlayReply.setVisibility(View.GONE);
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(edittexteReact.getWindowToken(), 0);
        imgAddReact.setVisibility(View.VISIBLE);
        layoutTutorialReact.setVisibility(View.VISIBLE);
        imgViewReact.setVisibility(View.GONE);
        imgViewReact.setImageDrawable(null);
        edittexteReact.setTranslationY(0);
    }

    ////// ANIMATION //////
    ///////////////////////


    private int SIZE_REACT = 360;
    private byte[] getReactData(Drawable photo, Bitmap layerReact) {
        if(photo == null) return new byte[0];//fix : crash #17

        ///////////
        // Bitmap de la photo
        Bitmap photoBitmap = ((BitmapDrawable) photo).getBitmap();
        int photoWidth = photoBitmap.getWidth();
        int photoHeight = photoBitmap.getHeight();

        float xScale = (float) SIZE_REACT / photoWidth;
        float yScale = (float) SIZE_REACT / photoHeight;
        float scale = Math.max(xScale, yScale);

        float scaledWidth = scale * photoWidth;
        float scaledHeight = scale * photoHeight;

        float left = (SIZE_REACT - scaledWidth) / 2;
        float top = (SIZE_REACT - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        ///////////
        // Bitmap du texte
        //resize and calcul position of textBitmap in final canvas
        float ratio = (float)SIZE_REACT / Math.min(layoutCamera.getWidth(), layoutCamera.getHeight());

        int texteWidth = (int) (layerReact.getWidth() * ratio);
        int texteHeight = (int) (layerReact.getHeight() * ratio);

        //Create resize Rect
        Rect srcTextRect = new Rect(0, 0, layerReact.getWidth(), layerReact.getHeight());
        Rect dstTextRect = new Rect(0, 0, texteWidth, texteHeight);

        ///////////
        // Create canvas finale
        Bitmap finalBitmap = Bitmap.createBitmap(SIZE_REACT, SIZE_REACT, photoBitmap.getConfig());
        Canvas finalCanvas = new Canvas(finalBitmap);
        finalCanvas.drawBitmap(photoBitmap, null, targetRect, null);
        finalCanvas.drawBitmap(layerReact, srcTextRect, dstTextRect, null);

        //create jpeg data
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private Bitmap getBitmapLayerReact() {
        removeFocus.requestFocus();
        Bitmap textBitmap = Bitmap.createBitmap(layoutCamera.getWidth(), layoutCamera.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(textBitmap);
        layoutTextReact.draw(c);
        return textBitmap;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if(shareLayoutShow)
            {
                hideShareLayout();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraView.release();
        if (adapter != null) adapter.stopCurrentVideo();
        if (isPlaying) stopCurrentVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPreviewVisible) startCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (piki.isLoadError()) {
            // show error
            imgError.setVisibility(View.VISIBLE);
            imgError.setImageResource(R.drawable.picto_loaderror);
            isPreparePlaying = false;
        } else if (piki.isLoaded()) {
            // play video
            layoutVideo.setVisibility(View.VISIBLE);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    imgPiki.setVisibility(View.GONE);
                    mediaPlayer.start();
                    isPreparePlaying = false;
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                    L.e("ERROR : play video in MediaPlayer - what=[" + what + "] - extra=[" + extra + "]");
                    imgError.setVisibility(View.VISIBLE);
                    isPreparePlaying = false;
                    return false;
                }
            });

            mediaPlayer.setVolume(1, 1);
            mediaPlayer.setLooping(true);
            mediaPlayer.setDisplay(surfaceView.getHolder());
            try {
                FileInputStream fis = new FileInputStream(piki.getTempFilePath(this));
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
                imgError.setVisibility(View.VISIBLE);
                imgError.setImageResource(R.drawable.picto_loaderror);
                isPlaying = false;
            }
        } else { //loading
            // wait loading
            progressBar.setVisibility(View.VISIBLE);
            piki.setLoadVideoEndListener(new VideoBean.LoadVideoEndListener() {
                @Override
                public void done(boolean ok, VideoBean react) {
                    progressBar.setVisibility(View.GONE);
                    isPreparePlaying = false;
                    playVideo();
                }
            });
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void playVideo() {
        adapter.stopCurrentVideo();

        if (isPreparePlaying) return;
        isPreparePlaying = true;

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            stopCurrentVideo();
        }

        imgPlay.setVisibility(View.GONE);
        imgError.setVisibility(View.GONE);
        txtNbFriend.setVisibility(View.GONE);
        imgEmojiEarth.setVisibility(View.GONE);
        txtTroisPoints.setVisibility(View.GONE);

        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutVideo.addView(surfaceView, params);
    }

    public void stopCurrentVideo() {
        layoutVideo.setVisibility(View.GONE);
        layoutVideo.removeAllViews();
        imgPiki.setVisibility(View.VISIBLE);
        imgPlay.setVisibility(View.VISIBLE);
        imgError.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        pikiHeader.invalidate();
        txtNbFriend.setVisibility(View.VISIBLE);
        imgEmojiEarth.setVisibility(View.VISIBLE);
        txtTroisPoints.setVisibility(View.VISIBLE);

        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        isPlaying = false;
    }

    private void showTuto()  {
        if (layoutTutorialReact.getVisibility() == View.GONE) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (preferences.getBoolean("first_tuto_piki", true)) {
                Utile.fadeIn(layoutTutorialReact, 300, new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        layoutTutorialReact.setAnimation(AnimationUtils.loadAnimation(PikiActivity.this, R.anim.floating));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("first_tuto_piki", false);
                editor.commit();
            } else {
                layoutTutorialReact.setVisibility(View.GONE);
            }
        }
    }

    private void setUpPopupEmojis() {
        // POPUP EMOJIS / FONTS ON TOP OF SHOWN KEYBOARD
        popupEmoji = new EmojisFontsPopup(rootView, this, emojis, EmojisFontsPopup.POPUP_STICKERS, keyboardHeight);
        popupEmoji.setSizeForSoftKeyboard();
        popupEmoji.setOnEmojiFontClickedListener(new EmojisFontsPopup.OnEmojiFontClickListener() {

            @Override
            public void onEmojiFontClick(Overlay overlay) {
                if (overlay instanceof Emoji) {
                    Emoji emoji = (Emoji) overlay;

                    if (emoji != null) {
                        imgViewReact.setVisibility(View.VISIBLE);
                        PicassoUtils.with(PikiActivity.this)
                                .load(emoji.getUrlPhoto())
                                .resize(layoutCamera.getWidth(), layoutCamera.getHeight())
                                .into(imgViewReact);
                    } else {
                        imgViewReact.setVisibility(View.GONE);
                        imgViewReact.setImageDrawable(null);
                    }
                }
            }
        });

        popupEmoji.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                // TODO ON DISMISS
            }
        });

        popupEmoji.setOnSoftKeyboardOpenCloseListener(new EmojisFontsPopup.OnSoftKeyboardOpenCloseListener() {
            @Override
            public void onKeyboardOpen(int keyBoardHeight) {}

            @Override
            public void onKeyboardClose() {
                if (popupEmoji.isShowing()) {
                    popupEmoji.dismiss();
                    edittexteReact.setText("");
                    edittexteReact.setVisibility(View.GONE);
                    imgViewReact.setVisibility(View.GONE);
                    imgViewReact.setImageDrawable(null);
                    imgStickers.setImageResource(R.drawable.picto_stickers_selector);
                    imgFonts.setImageResource(R.drawable.picto_fonts_selector);
                }
            }
        });
    }

    private void setUpPopupFonts() {
        // POPUP EMOJIS / FONTS ON TOP OF SHOWN KEYBOARD
        fonts = new ArrayList<Font>();
        fonts.add(new Font("banzaibros.otf"));
        fonts.add(new Font("voltebold.otf"));
        fonts.add(new Font("trashhand2.ttf"));
        fonts.add(new Font("plasticapro.otf"));
        fonts.add(new Font("impact.ttf"));
        fonts.add(new Font("story.otf"));
        fonts.add(new Font("americantypewriter.ttf"));
        fonts.add(new Font("higher.otf"));
        fonts.add(new Font("daftbrush.otf"));
        fonts.add(new Font("baronneueblack.otf"));

        popupFont = new EmojisFontsPopup(rootView, this, fonts, EmojisFontsPopup.POPUP_FONTS, keyboardHeight);
        popupFont.setSizeForSoftKeyboard();
        popupFont.setOnEmojiFontClickedListener(new EmojisFontsPopup.OnEmojiFontClickListener() {

            @Override
            public void onEmojiFontClick(Overlay overlay) {
                if (overlay instanceof Font) {
                    Font font = (Font) overlay;

                    if (font != null) {
                        imgViewReact.setVisibility(View.VISIBLE);
                        edittexteReact.setAllCaps(font.getName().equals("impact.ttf"));

                        if (edittexteReact.getText() == null || edittexteReact.getText().toString().isEmpty()) {
                            edittexteReact.setText("Yo");
                        }

                        edittexteReact.setCustomFont(PikiActivity.this, font.getName());
                        edittexteReact.setSelection(edittexteReact.getText().length());
                    } else {
                        imgViewReact.setVisibility(View.GONE);
                        imgViewReact.setImageDrawable(null);
                        edittexteReact.setAllCaps(false);
                    }
                }
            }
        });

        popupFont.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                // TODO ON DISMISS
            }
        });

        popupFont.setOnSoftKeyboardOpenCloseListener(new EmojisFontsPopup.OnSoftKeyboardOpenCloseListener() {
            @Override
            public void onKeyboardOpen(int keyBoardHeight) {
            }

            @Override
            public void onKeyboardClose() {
                if (popupFont.isShowing()) {
                    popupFont.dismiss();
                    edittexteReact.setText("");
                    edittexteReact.setVisibility(View.GONE);
                    imgViewReact.setVisibility(View.GONE);
                    imgViewReact.setImageDrawable(null);
                    imgStickers.setImageResource(R.drawable.picto_stickers_selector);
                    imgFonts.setImageResource(R.drawable.picto_fonts_selector);
                }
            }
        });
    }
}
