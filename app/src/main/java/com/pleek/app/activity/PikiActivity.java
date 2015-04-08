package com.pleek.app.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.goandup.lib.widget.FlipImageView;
import com.goandup.lib.widget.SwipeRefreshLayoutScrollingOff;
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
import com.pleek.app.adapter.PikiAdapter;
import com.pleek.app.adapter.ReactAdapter;
import com.pleek.app.bean.AutoResizeFontTextWatcher;
import com.pleek.app.bean.Piki;
import com.pleek.app.bean.Reaction;
import com.pleek.app.bean.ViewLoadingFooter;
import com.pleek.app.views.CustomGridView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nicolas on 18/12/14.
 */
public class PikiActivity extends ParentActivity implements View.OnClickListener, ReactAdapter.Listener
{
    private final int DURATION_SHOWSHARE_ANIM = 300;//ms

    private View rootView;
    private View btnBack;
    private View btnShare;
    private TextView txtNamePiki;
    private CustomGridView gridViewPiki;
    private TextView txtNbFriend;
    private ImageView imgEmojiEarth;
    private TextView txtNbReply;
    private View pikiHeader;
    private ImageView imgPiki;
    private ImageView imgEmojiEarthTopBar;
    private TextView txtNbFriendTopbar;
    private TextView txtTroisPoints;
    private View layoutTopInfo;
    private ImageView imgPikiMini;
    private View layoutOverlayShare;
    private LinearLayout layoutShare;
    private ButtonRoundedMaterialDesign btnConfrimShare;
    private View layoutOverlayReply;
    private View layoutOverlayReplyTop;
    private View btnReplyHearth;
    private View btnReplyOk;
    private View btnReplyStuck;
    private View btnReplyClose;
    private View btnReplyTexte;
    private View layoutBtnReply;
    private View layoutBtnRoundedReply;
    private ButtonRoundedMaterialDesign btnReply;
    private ButtonRoundedMaterialDesign btnCapture;
    private View layoutTextReact;
    private EditText edittexteReact;
    private ImageView imgviewReact;
    private View removeFocus;
    private View layoutCamera;
    private CameraView cameraView;
    private FlipImageView imgSwitch;
    private View layoutTuto;
    private View layoutTutoTop;
    private View layoutTutoCenter;
    private View layoutTutoReact;
    private ButtonRoundedMaterialDesign btnReplyTuto;
    private ViewLoadingFooter footer;
    private SwipeRefreshLayoutScrollingOff refreshSwipe;

    private static Piki _piki;
    private Piki piki;
    private ReactAdapter adapter;
    private int initialX;
    private List<Reaction> listReact;
    private boolean isKeyboardShow;
    private int keyboardHeight;
    private int initialLayoutBtnReplyMarginBottom;
    private AutoResizeFontTextWatcher fontTextWatcher;
    private boolean isPreviewVisible;

    private int initMarginBottom = 0;

    public static void initActivity(Piki piki)
    {
        _piki = piki;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piki);

        setup();
        init();

        mixpanel.track("View Piki", null);
        fbAppEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT);
    }

    private void setup()
    {
        if(_piki != null)
        {
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

        txtTroisPoints = (TextView) pikiHeader.findViewById(R.id.txtTroisPoints);
        txtTroisPoints.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.secondColor), getResources().getColor(R.color.blanc)));
        txtTroisPoints.setOnClickListener(this);
        gridViewPiki.addHeaderView(pikiHeader);

        //footer listview
        footer = new ViewLoadingFooter(this);

        //topbarinfo
        layoutTopInfo = findViewById(R.id.layoutTopInfo);
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) layoutTopInfo.getLayoutParams();
        mlp.topMargin = -screen.dpToPx(60);
        layoutTopInfo.setLayoutParams(mlp);
        imgPikiMini = (ImageView) findViewById(R.id.imgPikiMini);
        txtNbFriendTopbar = (TextView) findViewById(R.id.txtNbFriendTopbar);
        imgEmojiEarthTopBar = (ImageView) findViewById(R.id.imgEmojiEarthTopBar);
        txtNbReply = (TextView) findViewById(R.id.txtNbReply);

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
        btnReplyHearth = findViewById(R.id.btnReplyHearth);
        Drawable drawableUp = getResources().getDrawable(R.drawable.btn_ronded);
        Drawable drawableDown = getResources().getDrawable(R.drawable.btn_ronded_selected);
        btnReplyHearth.setOnTouchListener(new DownTouchListener(drawableDown, drawableUp));
        BtnReplyClickListener btnReplyClickListener = new BtnReplyClickListener();
        btnReplyHearth.setOnClickListener(btnReplyClickListener);
        btnReplyOk = findViewById(R.id.btnReplyOk);
        btnReplyOk.setOnTouchListener(new DownTouchListener(drawableDown, drawableUp));
        btnReplyOk.setOnClickListener(btnReplyClickListener);
        btnReplyStuck = findViewById(R.id.btnReplyStuck);
        btnReplyStuck.setOnTouchListener(new DownTouchListener(drawableDown, drawableUp));
        btnReplyStuck.setOnClickListener(btnReplyClickListener);
        btnReplyTexte = findViewById(R.id.btnReplyTexte);
        btnReplyTexte.setOnTouchListener(new DownTouchListener(drawableDown, drawableUp));
        btnReplyTexte.setOnClickListener(btnReplyClickListener);
        btnReplyClose = findViewById(R.id.btnReplyClose);
        btnReplyClose.setOnTouchListener(new DownTouchListener(getResources().getDrawable(R.drawable.overlay_ronded), null));
        btnReplyClose.setOnClickListener(this);
        layoutBtnRoundedReply = findViewById(R.id.layoutBtnRoundedReply);
        layoutBtnReply = findViewById(R.id.layoutBtnReply);
        layoutBtnReply.post(new Runnable()
        {
            @Override
            public void run()
            {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutBtnReply.getLayoutParams();
                initialLayoutBtnReplyMarginBottom = params.bottomMargin;

                layoutBtnRoundedReply.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //fix marginleft for show/hide animation
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        params.bottomMargin = initialLayoutBtnReplyMarginBottom;
                        params.leftMargin = (int) layoutBtnRoundedReply.getX();
                        layoutBtnRoundedReply.setLayoutParams(params);
                    }
                });
            }
        });
        btnReply = (ButtonRoundedMaterialDesign) findViewById(R.id.btnReply);
        btnReply.setOnPressedListener(new ButtonRoundedMaterialDesign.OnPressedListener()
        {
            @Override
            public void endPress(ButtonRoundedMaterialDesign button)
            {
                showReplyButtons(true);
            }
        });
        btnCapture = (ButtonRoundedMaterialDesign) findViewById(R.id.btnCapture);
        btnCapture.setOnPressedListener(new ButtonRoundedMaterialDesign.OnPressedListener()
        {
            @Override
            public void endPress(ButtonRoundedMaterialDesign button)
            {
                final Bitmap bitmapLayerReact = getBitmapLayerReact();
                final Dialog dialogLoader = showLoader();
                cameraView.captureCamera(new CameraView.CameraViewListener()
                {
                    @Override
                    public void repCaptureCamera(Drawable photo)
                    {
                        byte[] reactData = getReactData(photo, bitmapLayerReact);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(reactData, 0, reactData.length);

                        final Reaction tmpReact = new Reaction(ParseUser.getCurrentUser().getUsername(), bitmap);
                        tmpReact.setNameUser(ParseUser.getCurrentUser().getUsername());
                        Reaction.Type type = Reaction.Type.PHOTO;
                        if(layoutTextReact.getVisibility() == View.VISIBLE) type = Reaction.Type.TEXTE;
                        else if(imgviewReact.getVisibility() == View.VISIBLE) type = Reaction.Type.EMOJI;
                        tmpReact.setType(type);
                        listReact = adapter.addReact(tmpReact);
                        sendReact(tmpReact);

                        hideDialog(dialogLoader);
                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                showReplyButtons(false);
                            }
                        }, 500);
                    }
                });
            }
        });
        int edittexteReactPadding = screen.dpToPx(5);
        edittexteReact = (EditText) findViewById(R.id.edittexteReact);
        int size = screen.getWidth()/3 - edittexteReactPadding;
        fontTextWatcher = new AutoResizeFontTextWatcher(edittexteReact, size, size, screen.dpToPx(30));
        layoutTextReact = findViewById(R.id.layoutTextReact);
        edittexteReact.addTextChangedListener(fontTextWatcher);
        edittexteReact.setPadding(edittexteReactPadding, edittexteReactPadding, edittexteReactPadding, edittexteReactPadding);
        imgviewReact = (ImageView) findViewById(R.id.imgviewReact);
        removeFocus = findViewById(R.id.removeFocus);

        layoutCamera = findViewById(R.id.layoutCamera);
        layoutCamera.setOnClickListener(this);

        pikiHeader.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int layoutCameraSize = screen.getWidth() / 3;
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
        imgSwitch = (FlipImageView) findViewById(R.id.imgSwitch);

        // Open/Close Keyboard detection
        rootView = findViewById(R.id.rootView);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                Rect r = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                keyboardHeight = rootView.getRootView().getHeight() - r.bottom;
                if (keyboardHeight > 100 && !isKeyboardShow)//keyboard SHOW
                {
                    isKeyboardShow = true;
                    animateStartEditText();
                }
                if(keyboardHeight <= 100 && isKeyboardShow)//keyboard HIDE
                {
                    isKeyboardShow = false;
                    animateEndEditText();
                }
            }
        });

        adapter = new ReactAdapter(new ArrayList<Reaction>(), PikiActivity.this);
        //wait gridViewPiki is created for get height
        gridViewPiki.post(new Runnable()
        {
            @Override
            public void run()
            {
                adapter.setHeightListView(gridViewPiki.getHeight());
                gridViewPiki.setAdapter(adapter);
            }
        });

        isPreviewVisible = true;

        //TUTO
        layoutTuto = findViewById(R.id.layoutTuto);
        layoutTuto.setVisibility(View.GONE);
        layoutTuto.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                Utile.fadeOut(layoutTuto, 300);
                return true;
            }
        });
        layoutTutoTop = findViewById(R.id.layoutTutoTop);
        ViewGroup.LayoutParams lp = layoutTutoTop.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        int marginTop = lp.height = screen.dpToPx(60) + screen.getWidth() + screen.dpToPx(1);
        layoutTutoTop.setLayoutParams(lp);
        layoutTutoCenter = findViewById(R.id.layoutTutoCenter);
        lp = layoutTutoCenter.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = screen.getWidth() / 3 - screen.dpToPx(1);
        layoutTutoCenter.setLayoutParams(lp);
        layoutTutoReact = findViewById(R.id.layoutTutoReact);
        mlp = (ViewGroup.MarginLayoutParams) layoutTutoReact.getLayoutParams();
        mlp.setMargins(0, marginTop - screen.dpToPx(100), 0, 0);
        btnReplyTuto = (ButtonRoundedMaterialDesign) findViewById(R.id.btnReplyTuto);
        btnReplyTuto.setOnPressedListener(new ButtonRoundedMaterialDesign.OnPressedListener()
        {
            @Override
            public void endPress(ButtonRoundedMaterialDesign button)
            {
                Utile.fadeOut(layoutTuto, 300);
            }
        });
        refreshSwipe = (SwipeRefreshLayoutScrollingOff) findViewById(R.id.refreshSwipe);
        refreshSwipe.setColorSchemeResources(R.color.secondColor, R.color.firstColor, R.color.secondColor, R.color.firstColor);
        refreshSwipe.setScrollingEnabled(false);
    }


    private void init() {
        init(true);
    }
    private void init(boolean withCache)
    {
        if(piki != null)
        {
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

            txtNamePiki.setText("@" + piki.getName());
            Picasso.with(this)
                    .load(piki.getUrlPiki())
                    .placeholder(R.drawable.piki_placeholder)
                    .error(R.drawable.piki_placeholder)
                    .into(imgPiki);

            //topbar
            if (!piki.isPublic()) {
                txtNbFriendTopbar.setText(piki.getNbRecipient() + " " + getString(R.string.piki_friend) + (piki.getNbRecipient() > 1 ? "s" : ""));
            } else {
                txtNbFriendTopbar.setVisibility(View.GONE);
                imgEmojiEarthTopBar.setVisibility(View.VISIBLE);
            }

            txtNbReply.setText(piki.getNbReact()+"");
            Picasso.with(this)
                    .load(piki.getUrlPiki())
                    .placeholder(R.drawable.gris_label_back)
                    .error(R.drawable.gris_label_back)
                    .into(imgPikiMini);

            currentPage = 0;
            listReact = new ArrayList<Reaction>();

            refreshSwipe.setRefreshing(loadNext(withCache));

            //count nombre react
            ParseQuery<ParseObject> query = ParseQuery.getQuery("React");
            query.whereEqualTo("Piki", piki.getParseObject());
            query.countInBackground(new CountCallback()
            {
                public void done(int count, ParseException e)
                {
                    txtNbReply.setText((e == null ? count : 0)+"");
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
                    if(listReact != null && listReact.size() > 0) showTuto();
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

    private void showTuto()
    {
        if(layoutTuto.getVisibility() == View.GONE)
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if(preferences.getBoolean("first_tuto_piki", true))
            {
                Utile.fadeIn(layoutTuto, 300);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("first_tuto_piki", false);
                editor.commit();
            }
            else
            {
                layoutTuto.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void clickOnReaction(Reaction react)
    {
        if(react.isTmpReaction() && react.isLoadError())
        {
            adapter.markLoadError(react, false);
            sendReact(react);
        }
    }

    @Override
    public void doubleTapReaction(Reaction react)
    {
        if(react == null || react.getParseObject() == null) return;//fix : CRASH #11

        final ParseUser userReact = react.getParseObject().getParseUser("user");
        ParseUser currentUser = ParseUser.getCurrentUser();
        List<String> usersFriend = currentUser.getList("usersFriend");
        List<String> usersIMuted = currentUser.getList("usersIMuted");
        final boolean alreadyFriend = usersFriend != null && usersFriend.contains(userReact.getObjectId());
        final boolean alreadyMuted = usersIMuted != null && usersIMuted.contains(userReact.getObjectId());
        int icon = R.drawable.picto_adduser_noir;
        if(alreadyFriend) icon = R.drawable.picto_mute_user;
        if(alreadyMuted) icon = R.drawable.picto_mute_user_on;
        String popupTitle = getString(R.string.piki_popup_adduser_title);
        if(alreadyFriend) popupTitle = getString(R.string.piki_popup_mute_title);
        if(alreadyMuted) popupTitle = getString(R.string.piki_popup_unmute_title);

        showDialog(popupTitle, "@"+userReact.getUsername(), icon, R.drawable.picto_nok, new MyDialogListener()
        {
            @Override
            public void closed(boolean accept)
            {
                if(accept)
                {
                    FunctionCallback callback = new FunctionCallback<Object>()
                    {
                        @Override
                        public void done(Object o, ParseException e)
                        {
                            if(e == null)
                            {
                                ParseUser.getCurrentUser().fetchInBackground();
                            }
                            else
                            {
                                Utile.showToast(R.string.pikifriends_action_nok, PikiActivity.this);
                            }
                        }
                    };

                    if(alreadyFriend)
                    {
                        Map<String, Object> param = new HashMap<String, Object>();
                        param.put("friendId", userReact.getObjectId());
                        ParseCloud.callFunctionInBackground(alreadyMuted ? "unMuteFriend" : "muteFriend", param, callback);
                    }
                    else
                    {
                        Map<String, Object> param = new HashMap<String, Object>();
                        param.put("friendId", userReact.getObjectId());
                        ParseCloud.callFunctionInBackground("addFriendV2", param, callback);
                    }
                }
            }
        });
    }

    @Override
    public void showPlaceHolderItem(boolean show) {
        gridViewPiki.setScrollingEnabled(!show);
    }

    @Override
    public void onClick(View view)
    {
        if(view == btnBack)
        {
            finish();
        }
        else if(view == btnShare)
        {
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
        }
        else if(view == txtTroisPoints)
        {
            showDialog(R.string.piki_report_title, R.string.piki_report_texte, new MyDialogListener()
            {
                @Override
                public void closed(boolean accept)
                {
                    if(accept)
                    {
                        Map<String, Object> param = new HashMap<String, Object>();
                        param.put("piki", piki.getId());
                        ParseCloud.callFunctionInBackground("reportPiki", param, new FunctionCallback<Object>()
                        {
                            @Override
                            public void done(Object o, ParseException e)
                            {
                                if(e == null) Utile.showToast(R.string.piki_report_ok, PikiActivity.this);
                                else Utile.showToast(R.string.piki_report_nok, PikiActivity.this);
                            }
                        });
                    }
                }
            });
        }
        else if(view == txtNbFriend)
        {
            PikiFriendsActivity.initActivity(piki);
            startActivity(new Intent(this, PikiFriendsActivity.class));
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        }
        else if(view == btnReplyClose || view == layoutOverlayReply || view == layoutOverlayReplyTop)
        {
            if(isKeyboardShow)
            {
                endEditText();
            }
            else
            {
                showReplyButtons(false);
            }
        }
        else if(view == layoutCamera)
        {
            toggleCamera();
        }
    }

    public void startCamera()
    {
        if(cameraView != null && cameraView.getCamera() == null)
        {
            cameraView.setFaceCamera(pref.getBoolean("selfie_camera", true));
            cameraView.start();
        }
    }
    public boolean toggleCamera()
    {
        boolean toggled = false;
        if(cameraView != null && cameraView.toggleFaceCamera())
        {
            imgSwitch.setFlipped(!imgSwitch.isFlipped());
            toggled = true;

            //save
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("selfie_camera", cameraView.isFaceCamera());
            editor.commit();
        }
        return toggled;
    }

    @Override
    public void finish()
    {
        super.finish();
        Reaction.deleteAllTempFileVideo(this);
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }

    private class MyListTouchListener implements View.OnTouchListener
    {
        private final int DURATION_DELETE_ANIM = 300;//ms
        private int WIDTH_BORDER_BACKVIEW;// 3/5 d'1/3 de l'écran
        private int WIDTH_BORDER_BACKVIEW_START_ALPHA;// WIDTH_BORDER_BACKVIEW / 2

        private View downItem;

        private MyListTouchListener()
        {
            WIDTH_BORDER_BACKVIEW = (Screen.getWidth(PikiActivity.this) / 3) * 3/5;
            WIDTH_BORDER_BACKVIEW_START_ALPHA = WIDTH_BORDER_BACKVIEW >> 1;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            try//fix : crash #36
            {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_MOVE && gridViewPiki.isScrollingHorizontalLeft())
                {
                    if(downItem == null)
                    {
                        // 1) first scroll horizontal to an item (initialize)

                        initialX = (int) event.getRawX();

                        // Find the child view that was touched (perform a hit test)
                        Rect rect = new Rect();
                        int childCount = gridViewPiki.getChildCount();
                        int[] listViewCoords = new int[2];
                        gridViewPiki.getLocationOnScreen(listViewCoords);
                        int x = (int) event.getRawX() - listViewCoords[0];
                        int y = (int) event.getRawY() - listViewCoords[1];
                        for (int i = 0; i < childCount; i++)
                        {
                            View child = gridViewPiki.getChildAt(i);
                            child.getHitRect(rect);
                            if (rect.contains(x, y)) {
                                if(child instanceof ViewGroup) downItem = child;
                                break;
                            }
                        }

                        //fake view for no loop
                        if(downItem == null) downItem = new View(PikiActivity.this);

                        //initialize backview
                        View backView = downItem.findViewById(R.id.back);
                        if(backView != null)
                        {
                            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                            lp.setMargins(0, 0, 0, 0);
                            backView.setLayoutParams(lp);

                            backView.setPadding((screen.getWidth() / 3) - WIDTH_BORDER_BACKVIEW, 0, 0, 0);

                            //ERROR : java.lang.IndexOutOfBoundsException: Invalid index 5, size is 0

                            //change picto : delete or report
                            int numReaction = downItem != null ? (Integer)downItem.getTag() : -1;//fix : crash #6 > downItem==null (SOLVED)//fix : crash #36
                            Reaction react = numReaction >= 0 ? listReact.get(numReaction) : null;
                            ((ImageView)backView.findViewById(R.id.imgActionOn)).setImageResource(canDeleteReact(react) ? R.drawable.picto_delete_item_big_on : R.drawable.picto_report_item_on);
                            ((ImageView)backView.findViewById(R.id.imgActionOff)).setImageResource(canDeleteReact(react) ? R.drawable.picto_delete_item_big_off : R.drawable.picto_report_item_off);
                        }
                    }
                    else
                    {
                        // 2) When scroll horizontal to an item.

                        View frontView = downItem.findViewById(R.id.front);
                        View backView = downItem.findViewById(R.id.back);
                        if(frontView != null && backView != null && downItem.getTag() != null)
                        {
                            adapter.stopCurrentVideo();

                            int moveX = initialX - (int)event.getRawX();

                            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) frontView.getLayoutParams();
                            lp.setMargins(-moveX,0,moveX,0);
                            frontView.setLayoutParams(lp);

                            backView.setVisibility(moveX < 0 ? View.GONE : View.VISIBLE);

                            //change alpha action image rouge
                            if(moveX > WIDTH_BORDER_BACKVIEW_START_ALPHA)
                            {
                                //calculate actual alpha of imgDeleteItemOn
                                float alpha = (Math.abs(moveX) - WIDTH_BORDER_BACKVIEW_START_ALPHA) / (float)(WIDTH_BORDER_BACKVIEW - WIDTH_BORDER_BACKVIEW_START_ALPHA);
                                backView.findViewById(R.id.imgActionOn).setAlpha(Math.min(alpha, 1f));
                            }
                            else
                            {
                                backView.findViewById(R.id.imgActionOn).setAlpha(0f);//sure alpha is 0 if under WIDTH_BORDER_BACKVIEW_START_ALPHA
                            }

                            //move action image
                            if(moveX > WIDTH_BORDER_BACKVIEW)
                            {
                                //move item frontview of position X with margin
                                moveX -= WIDTH_BORDER_BACKVIEW;
                                lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                                lp.setMargins(-moveX, 0, moveX, 0);
                                backView.setLayoutParams(lp);
                            }
                            else
                            {
                                lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                                lp.setMargins(0, 0, 0, 0);
                                backView.setLayoutParams(lp);
                            }

                            return true;//consume touch event
                        }
                    }
                }


                if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
                {
                    if(downItem != null)
                    {
                        //3) return item view to original state
                        View frontView = downItem.findViewById(R.id.front);
                        View backView = downItem.findViewById(R.id.back);
                        if(frontView != null && backView != null)
                        {
                            final int moveX = initialX - (int)event.getRawX();
                            if(moveX > WIDTH_BORDER_BACKVIEW && action == MotionEvent.ACTION_UP)
                            {
                                final int position = (Integer)downItem.getTag();
                                if(canDeleteReact(listReact.get(position)))
                                {
                                    ///////////////
                                    // POPUP DELETE
                                    showDialog(R.string.piki_popup_remove_title, R.string.piki_popup_remove_texte, new MyDialogListener()
                                    {
                                        @Override
                                        public void closed(boolean accept)
                                        {
                                            // DELETE
                                            if(accept)
                                            {
                                                final int initialHeight = downItem.getMeasuredHeight();
                                                animItemDisappear(downItem, moveX, new Animation.AnimationListener()
                                                {
                                                    @Override
                                                    public void onAnimationEnd(Animation a)
                                                    {
                                                        //reset view for next
                                                        downItem.getLayoutParams().height = initialHeight;
                                                        downItem.requestLayout();
                                                        downItem.invalidate();
                                                        View frontView = downItem.findViewById(R.id.front);
                                                        View backView = downItem.findViewById(R.id.back);
                                                        if (frontView != null && backView != null)
                                                        {
                                                            //reset margin
                                                            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) frontView.getLayoutParams();
                                                            lp.setMargins(0, 0, 0, 0);
                                                            frontView.setLayoutParams(lp);
                                                            lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                                                            lp.setMargins(0, 0, 0, 0);
                                                            backView.setLayoutParams(lp);
                                                        }
                                                        downItem = null;

                                                        reportOrRemoveReact(adapter.removeReaction(position));
                                                    }

                                                    @Override public void onAnimationRepeat(Animation animation) {}
                                                    @Override public void onAnimationStart(Animation animation) {}
                                                });
                                            }
                                            else // NOT DELETE
                                            {
                                                animRestorePositionItem(downItem, moveX);
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    ///////////////
                                    // POPUP REPORT
                                    showDialog(R.string.piki_popup_report_title, R.string.piki_popup_report_texte, new MyDialogListener()
                                    {
                                        @Override
                                        public void closed(boolean accept)
                                        {
                                            // REPORT
                                            if(accept)
                                            {
                                                animRestorePositionItem(downItem, moveX);
                                                reportOrRemoveReact(adapter.getReaction(position));

                                                //showAlert(R.string.piki_popup_report_title, R.string.piki_alert_report_texte);
                                            }
                                            else // NOT REPORT
                                            {
                                                animRestorePositionItem(downItem, moveX);
                                            }
                                        }
                                    });
                                }
                            }
                            else
                            {
                                animRestorePositionItem(downItem, moveX);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                L.w("ERROR  >>  MyListTouchListener e="+e.getMessage());
            }
            return false;//no consume touch event
        }

        private void reportOrRemoveReact(Reaction react)
        {
            //Parse remove Piki
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("reactId", react.getId());
            ParseCloud.callFunctionInBackground("reportOrRemoveReact", params, new FunctionCallback<Object>()
            {
                @Override
                public void done(Object o, ParseException e)
                {
                    if (e != null) Utile.showToast(R.string.piki_react_remove_nok, PikiActivity.this);
                    //else init(false);
                }
            });
        }

        private void animItemDisappear(final View item, int moveX, final Animation.AnimationListener animListener)
        {
            if(downItem == null) return;

            final View frontView = downItem.findViewById(R.id.front);
            final View backView = downItem.findViewById(R.id.back);
            if(frontView != null && backView != null)
            {
                TranslateAnimation ta = new TranslateAnimation(-moveX, -screen.getWidth() / 3,0,0);
                ta.setDuration(DURATION_DELETE_ANIM);
                ta.setAnimationListener(animListener);
                frontView.startAnimation(ta);
                backView.startAnimation(ta);

                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) frontView.getLayoutParams();
                lp.setMargins(0, 0, 0, 0);
                frontView.setLayoutParams(lp);
                lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                lp.setMargins(0, 0, 0, 0);
                backView.setLayoutParams(lp);
            }

        }

        private void animRestorePositionItem(final View item, int moveX)
        {
            if(item == null) return;

            View frontView = item.findViewById(R.id.front);//TODO : crash #31
            View backView = item.findViewById(R.id.back);
            if(frontView != null && backView != null)
            {
                TranslateAnimation ta = new TranslateAnimation(-moveX,0,0,0);
                ta.setDuration(DURATION_DELETE_ANIM);
                frontView.startAnimation(ta);

                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) frontView.getLayoutParams();
                lp.setMargins(0, 0, 0, 0);
                frontView.setLayoutParams(lp);

                //anim alpha imgDeleteItemOn to origin
                if(moveX > WIDTH_BORDER_BACKVIEW_START_ALPHA)
                {
                    //calculate actual alpha of imgDeleteItemOn
                    float alpha = (moveX - WIDTH_BORDER_BACKVIEW_START_ALPHA) / (float)(WIDTH_BORDER_BACKVIEW - WIDTH_BORDER_BACKVIEW_START_ALPHA);

                    AlphaAnimation aa = new AlphaAnimation(alpha, 0);
                    aa.setDuration(DURATION_DELETE_ANIM);
                    //aa.setFillAfter(true); /!\ BUG
                    backView.findViewById(R.id.imgActionOn).startAnimation(aa);
                }

                //anim position backView to origin
                if(moveX > WIDTH_BORDER_BACKVIEW)
                {
                    //start animation
                    moveX -= WIDTH_BORDER_BACKVIEW;
                    ta = new TranslateAnimation(-moveX,0,0,0);
                    ta.setDuration(DURATION_DELETE_ANIM);
                    backView.startAnimation(ta);

                    //reset margin
                    lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                    lp.setMargins(0, 0, 0, 0);
                    backView.setLayoutParams(lp);
                }
            }

            downItem = null;
        }
    }

    private int lastItemShow;
    private class MyOnScrollListener implements AbsListView.OnScrollListener
    {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState)
        {
            if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
            {
                //start scroll
            }
            else if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
            {
                //end scroll
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
        {
            //anim topbarinfo
            if(view != null && view.getChildAt(0) != null)
            {
                int yPos = view.getChildAt(0).getTop();

                if(firstVisibleItem > 0) yPos -= screen.getWidth();
                if(firstVisibleItem > 1) yPos -= (firstVisibleItem-1) * screen.dpToPx(PikiAdapter.HEIGHT_ITEM);

                int marginTop = -Math.max(0, Math.min(yPos + screen.getWidth(), layoutTopInfo.getHeight()));

                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) layoutTopInfo.getLayoutParams();
                mlp.topMargin = marginTop;
                layoutTopInfo.setLayoutParams(mlp);
            }

            //anim layout CameraView
            if((view != null && view.getChildAt(1) != null))
            {
                final boolean isVisible = firstVisibleItem <= 1;
                final int yPos = isVisible ? view.getChildAt(1 - firstVisibleItem).getTop() : 0;

                Runnable updateRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(isVisible)
                        {
                            ViewGroup.MarginLayoutParams mlpCamera = (ViewGroup.MarginLayoutParams) layoutCamera.getLayoutParams();
                            mlpCamera.bottomMargin = initMarginBottom - yPos;
                            layoutCamera.setLayoutParams(mlpCamera);
                            if(isVisible && !isPreviewVisible)
                            {
                                startCamera();
                                isPreviewVisible = true;
                            }

                        }
                        else
                        {
                            CameraView.release(new CameraView.ListenerRelease()
                            {
                                @Override
                                public void end()
                                {
                                    isPreviewVisible = false;
                                }
                            });
                        }
                        layoutCamera.setVisibility(isVisible ? View.VISIBLE : View.GONE);
                    }
                };

                if(layoutCamera.getVisibility() == View.GONE)
                {
                    layoutCamera.post(updateRunnable);//for first must be execute in post (for Piki with no react)
                }
                else
                {
                    updateRunnable.run();//all other must be execute same thread
                }
            }

            //pagination
            final int lastItem = firstVisibleItem + visibleItemCount;
            if(lastItem == totalItemCount)
            {
                if(lastItemShow < lastItem)
                {
                    if(loadNext(true))
                    {
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
        txtUserName.setText("@"+piki.getName() + " " + getString(R.string.share_onpleek));
        txtDate.setText(DateFormat.getDateTimeInstance().format(piki.getCreatedAt()));
        if(txtReplies != null) txtReplies.setText(piki.getNbReact()+"+ "+getString(R.string.share_replies));

        ImageView imgPiki = (ImageView)viewShare.findViewById(R.id.imgPiki);
        Picasso.with(this)
                .load(piki.getUrlPiki())
                .placeholder(R.drawable.piki_placeholder)
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
                    Picasso.with(this)
                            .load(react.getUrlPhoto())
                            .placeholder(R.drawable.piki_placeholder)
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
    private void showReplyButtons(final boolean show)
    {
        isReplyButtonsShow = show;

        final View[] btns = {btnReplyClose, btnReplyTexte, btnReplyStuck, btnReplyOk, btnReplyHearth};

        if(show)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    for(int i = 0; i < btns.length; i++)
                    {
                        animatedReplyButton((View) btns[i].getParent());
                        try {
                            Thread.sleep(TIME_ANIM / (btns.length>>1), 0);
                        } catch (InterruptedException e) {}
                    }
                }
            }).start();

            Utile.fadeIn(layoutOverlayReply, TIME_ANIM);
            Utile.fadeIn(layoutOverlayReplyTop, TIME_ANIM);
            Utile.fadeIn(layoutTextReact, TIME_ANIM);
            Utile.fadeIn(btnCapture, TIME_ANIM);
            Utile.fadeOut(btnReply, TIME_ANIM);

            gridViewPiki.smoothScrollToPositionFromTop(2, gridViewPiki.getHeight() - screen.getWidth()/3, TIME_ANIM >> 1);
        }
        else
        {
            RelativeLayout.LayoutParams p1 = (RelativeLayout.LayoutParams) layoutBtnReply.getLayoutParams();
            p1.bottomMargin = initialLayoutBtnReplyMarginBottom;
            layoutBtnReply.setLayoutParams(p1);

            RelativeLayout.LayoutParams p2 = (RelativeLayout.LayoutParams) layoutBtnRoundedReply.getLayoutParams();
            p2.bottomMargin = initialLayoutBtnReplyMarginBottom;
            layoutBtnRoundedReply.setLayoutParams(p2);

            for(int i = 0; i < btns.length; i++)
            {
                ((View)btns[i].getParent()).setVisibility(View.GONE);
            }

            Utile.fadeOut(layoutOverlayReply, TIME_ANIM >> 1);
            Utile.fadeOut(layoutOverlayReplyTop, TIME_ANIM >> 1);
            Utile.fadeOut(layoutTextReact, TIME_ANIM >> 1);
            Utile.fadeOut(btnCapture, TIME_ANIM >> 1);
            Utile.fadeIn(btnReply, TIME_ANIM >> 1);

            gridViewPiki.smoothScrollToPosition(0);
        }

        final int leftMarginShow = (screen.getWidth() - layoutBtnRoundedReply.getWidth()) >> 1;
        final int leftMarginHide = screen.getWidth() - layoutBtnRoundedReply.getWidth() - screen.dpToPx(30);

        Animation translateAnim = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t)
            {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutBtnRoundedReply.getLayoutParams();
                params.leftMargin = (int) ((leftMarginHide - leftMarginShow) * (show ? 1-interpolatedTime : interpolatedTime)) + leftMarginShow;
                layoutBtnRoundedReply.setLayoutParams(params);
            }
        };
        translateAnim.setDuration(show ? TIME_ANIM : TIME_ANIM >> 1); // in ms
        layoutBtnRoundedReply.startAnimation(translateAnim);
    }

    private void animatedReplyButton(final View button)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(!isReplyButtonsShow) return;
                Animation animBtn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popin);
                animBtn.setDuration(TIME_ANIM);
                animBtn.setInterpolator(new OvershootInterpolator(4));
                button.startAnimation(animBtn);
                button.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startEditText()
    {
        edittexteReact.setVisibility(View.VISIBLE);
        edittexteReact.requestFocus();
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(edittexteReact, 0);
    }
    private void animateStartEditText()
    {
        edittexteReact.setVisibility(View.VISIBLE);
        imgviewReact.setVisibility(View.GONE);
        btnReplyTexte.setBackgroundResource(R.drawable.btn_ronded_selected);

        ((View)btnReplyHearth.getParent()).setVisibility(View.GONE);
        ((View)btnReplyOk.getParent()).setVisibility(View.GONE);
        ((View)btnReplyStuck.getParent()).setVisibility(View.GONE);
        ((View)btnReplyClose.getParent()).setVisibility(View.GONE);

        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) rootView.getLayoutParams();
        p.topMargin =  -(keyboardHeight - screen.getWidth()/6);// screen.getWidth()/6 >> half height of row
        rootView.setLayoutParams(p);

        RelativeLayout.LayoutParams p2 = (RelativeLayout.LayoutParams) layoutBtnReply.getLayoutParams();
        p2.bottomMargin = initialLayoutBtnReplyMarginBottom >> 1;//half of initialLayoutBtnReplyMarginBottom
        layoutBtnReply.setLayoutParams(p2);

        RelativeLayout.LayoutParams p3 = (RelativeLayout.LayoutParams) layoutBtnRoundedReply.getLayoutParams();
        p3.bottomMargin = initialLayoutBtnReplyMarginBottom >> 1;//half of initialLayoutBtnReplyMarginBottom
        layoutBtnRoundedReply.setLayoutParams(p3);
    }

    private void endEditText()
    {
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(edittexteReact.getWindowToken(), 0);
    }
    private void animateEndEditText()
    {
        removeFocus.requestFocus();//remove focus

        ((View)btnReplyHearth.getParent()).setVisibility(View.VISIBLE);
        ((View)btnReplyOk.getParent()).setVisibility(View.VISIBLE);
        ((View)btnReplyStuck.getParent()).setVisibility(View.VISIBLE);
        ((View)btnReplyClose.getParent()).setVisibility(View.VISIBLE);

        btnReplyTexte.setBackgroundResource(R.drawable.btn_ronded);
        edittexteReact.setVisibility(View.GONE);
        edittexteReact.setText("");
        fontTextWatcher.reset();

        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) rootView.getLayoutParams();
        p.topMargin = 0;
        rootView.setLayoutParams(p);

        RelativeLayout.LayoutParams p2 = (RelativeLayout.LayoutParams) layoutBtnReply.getLayoutParams();
        p2.bottomMargin = initialLayoutBtnReplyMarginBottom;
        layoutBtnReply.setLayoutParams(p2);

        RelativeLayout.LayoutParams p3 = (RelativeLayout.LayoutParams) layoutBtnRoundedReply.getLayoutParams();
        p3.bottomMargin = initialLayoutBtnReplyMarginBottom;
        layoutBtnRoundedReply.setLayoutParams(p3);

        gridViewPiki.post(new Runnable()
        {
            @Override
            public void run()
            {
                gridViewPiki.smoothScrollToPositionFromTop(2, gridViewPiki.getHeight() - screen.getWidth()/3, 0);
            }
        });
    }
    ////// ANIMATION //////
    ///////////////////////


    private int SIZE_REACT = 360;
    private byte[] getReactData(Drawable photo, Bitmap layerReact)
    {
        if(photo == null) return new byte[0];//fix : crash #17

        ///////////
        // Bitmap de la photo
        Bitmap photoBitmap = ((BitmapDrawable)photo).getBitmap();
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

    private Bitmap getBitmapLayerReact()
    {
        removeFocus.requestFocus();//remove focus
        Bitmap textBitmap = Bitmap.createBitmap(layoutCamera.getWidth(), layoutCamera.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(textBitmap);
        layoutTextReact.draw(c);
        return textBitmap;
    }

    private boolean canDeleteReact(Reaction react)
    {
        return (react != null && react.iamOwner()) || piki.iamOwner();
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
            else if(isReplyButtonsShow)
            {
                showReplyButtons(false);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraView.release();
        if(adapter != null) adapter.stopCurrentVideo();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(isPreviewVisible) startCamera();
    }

    private class BtnReplyClickListener implements View.OnClickListener
    {
        private int currentEmojiSelected;

        @Override
        public void onClick(View view)
        {
            if(view == btnReplyHearth)
            {
                clickOnBtnEmoji(btnReplyHearth, R.drawable.emoji_heart);
            }
            else if(view == btnReplyOk)
            {
                clickOnBtnEmoji(btnReplyOk, R.drawable.emoji_ok);
            }
            else if(view == btnReplyStuck)
            {
                clickOnBtnEmoji(btnReplyStuck, R.drawable.emoji_stuck);
            }
            else if(view == btnReplyTexte)
            {
                btnReplyHearth.setBackgroundResource(R.drawable.btn_ronded);
                btnReplyOk.setBackgroundResource(R.drawable.btn_ronded);
                btnReplyStuck.setBackgroundResource(R.drawable.btn_ronded);
                currentEmojiSelected = 0;

                if(isKeyboardShow) endEditText();
                else startEditText();
            }
        }

        private void clickOnBtnEmoji(View btn, int image)
        {
            btnReplyHearth.setBackgroundResource(R.drawable.btn_ronded);
            btnReplyOk.setBackgroundResource(R.drawable.btn_ronded);
            btnReplyStuck.setBackgroundResource(R.drawable.btn_ronded);
            btnReplyTexte.setBackgroundResource(R.drawable.btn_ronded);

            if(currentEmojiSelected == image)
            {
                currentEmojiSelected = 0;
                imgviewReact.setVisibility(View.GONE);
                btn.setBackgroundResource(R.drawable.btn_ronded);
            }
            else
            {
                int sticker = R.drawable.stickers_love;
                if(image == R.drawable.emoji_ok) sticker = R.drawable.stickers_ok;
                if(image == R.drawable.emoji_stuck) sticker = R.drawable.stickers_stuck;

                currentEmojiSelected = image;
                imgviewReact.setVisibility(View.VISIBLE);
                edittexteReact.setVisibility(View.GONE);
                imgviewReact.setImageResource(sticker);
                btn.setBackgroundResource(R.drawable.btn_ronded_selected);
            }

            endEditText();
        }
    }
}
