package com.pleek.app.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goandup.lib.animation.ResizeWidthAnimation;
import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.DownTouchListener;
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
import com.pleek.app.adapter.FriendsAdapter;
import com.pleek.app.bean.Friend;
import com.pleek.app.bean.Reaction;
import com.pleek.app.bean.ViewLoadingFooter;
import com.pleek.app.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by nicolas on 18/12/14.
 */
public class RecipientsActivity extends ParentActivity implements View.OnClickListener, FriendsAdapter.Listener
{
    private ImageView imgPiki;
    private View btnBack;
    private View txtTitle;
    private EditText editSearch;
    private View marginLeftBtnSearch;
    private View btnSearch;
    private View imgBnTopBar;
    private View backBtnTopBar;
    private View btnTopBar;
    private StickyListHeadersListView listView;
    private TextView txtNoFriends;
    private View btnSend;
    private View btnSendAll;
    private View layoutTxtButton;
    private TextView txtEveryone;
    private TextView txtNbFriendSelected;
    private ViewLoadingFooter footer;

    private static byte[] _pikiData;
    private byte[] pikiData;
    private String videoPath;
    private static String _videoPath;
    private int initialWidthMarginLeftBtnSearch;
    private FriendsAdapter adapter;
    private ArrayList<Friend> listFriend;

    public static void initActivity(byte[] pikiData) {
        _pikiData = pikiData;
    }

    public static void initActivity(String videoPath) {
        _videoPath = videoPath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipients);

        setup();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        init();
    }

    private void setup()
    {
        if(_pikiData != null)
        {
            pikiData = _pikiData;
            _pikiData = null;
        }

        if (_videoPath != null) {
            videoPath = _videoPath;
            _videoPath = null;
        }

        imgPiki = (ImageView)findViewById(R.id.imgPiki);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.overlay)));
        btnBack.setOnClickListener(this);
        imgPiki = (ImageView)findViewById(R.id.imgPiki);
        txtTitle = findViewById(R.id.txtTitle);
        editSearch = (EditText) findViewById(R.id.editSearch);
        editSearch.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                updateFiltre();
            }
        });
        marginLeftBtnSearch = findViewById(R.id.marginLeftBtnSearch);
        marginLeftBtnSearch.post(new Runnable()
        {
            @Override
            public void run()
            {
                initialWidthMarginLeftBtnSearch = marginLeftBtnSearch.getWidth();
                marginLeftBtnSearch.setLayoutParams(new LinearLayout.LayoutParams(initialWidthMarginLeftBtnSearch, marginLeftBtnSearch.getHeight()));//fix size
            }
        });
        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.firstColorDark)));
        btnSearch.setOnClickListener(this);
        backBtnTopBar = findViewById(R.id.backBtnTopBar);
        imgBnTopBar = findViewById(R.id.imgBnTopBar);
        btnTopBar = findViewById(R.id.btnTopBar);
        btnTopBar.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.firstColorDark)));
        btnTopBar.setOnClickListener(this);

        listView = (StickyListHeadersListView)findViewById(R.id.listView);
        listView.setOnScrollListener(new MyOnScrollListener());
        txtNoFriends = (TextView) findViewById(R.id.txtNoFriends);

        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.secondColorDark2), getResources().getColor(R.color.secondColor)));
        btnSend.setOnClickListener(this);
        btnSendAll = findViewById(R.id.btnSendAll);
        btnSendAll.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.secondColorDark2), getResources().getColor(R.color.secondColorDark)));
        btnSendAll.setOnClickListener(this);
        btnSendAll.post(new Runnable()
        {
            @Override
            public void run()
            {
                showBtnSendAll(false, 0);
            }
        });
        layoutTxtButton = findViewById(R.id.layoutTxtButton);
        txtEveryone = (TextView) findViewById(R.id.txtEveryone);
        txtNbFriendSelected = (TextView) findViewById(R.id.txtNbFriendSelected);

        footer = new ViewLoadingFooter(this);
    }

    private void init() {
        if ((pikiData != null && pikiData.length != 0) || !StringUtils.isStringEmpty(videoPath)) {
            currentPage = 0;
            lastItemShow = 0;
            isLoading = false;
            endOfLoading = false;
            listFriend = new ArrayList<Friend>();
            listBeforreRequest = new ArrayList<Friend>();
            //crash #25 BOB : retour depuis FriendsActivity

            if (StringUtils.isStringEmpty(videoPath)) {
                imgPiki.setImageBitmap(BitmapFactory.decodeByteArray(pikiData, 0, pikiData.length));
            }

            ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("Friend");
            innerQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);

            innerQuery.whereEqualTo("user", ParseUser.getCurrentUser());
            innerQuery.include("friend");

            innerQuery.orderByAscending("username");
            innerQuery.setSkip(currentPage * NB_BY_PAGE);
            innerQuery.setLimit(NB_BY_PAGE);
            innerQuery.findInBackground(new FindCallback<ParseObject>() {

                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (list != null && list.size() > 0) {
                        for (ParseObject obj : list) {
                            listFriend.add(new Friend((ParseUser) obj.get("friend")));
                        }

                        if(loadNext()) listView.addFooterView(footer);
                        txtNoFriends.setVisibility(View.GONE);
                    } else {
                        txtNoFriends.setVisibility(View.VISIBLE);
                    }

                    adapter = new FriendsAdapter(RecipientsActivity.this); //fix : CRASH #8 > http://stackoverflow.com/questions/24700588/i-am-using-the-listview-add-remove-footer-for-listview-cross-app-in-android-vers
                    listView.setAdapter(adapter);
                }
            });
        }
        else
        {
            finish();
        }
    }

    private boolean fromCache;
    private int currentPage;
    private final int NB_BY_PAGE = 50;
    private List<Friend> listBeforreRequest;
    private boolean isLoading;
    private boolean endOfLoading;
    private boolean loadNext()
    {
        //si déjà en train de loader, on fait rien
        if(isLoading) return false;

        //il n'y a plus rien a charger
        if(endOfLoading) return false;

        if (listFriend == null || listFriend.isEmpty()) return false;

        //copie de la liste actuel des piki
        listBeforreRequest = new ArrayList<Friend>(listFriend);

        isLoading = true;
        fromCache = false;

        ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("Friend");
        innerQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
        innerQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        innerQuery.include("friend");
        innerQuery.orderByAscending("username");
        innerQuery.setSkip(currentPage * NB_BY_PAGE);
        innerQuery.setLimit(NB_BY_PAGE);
        innerQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    if (currentPage > 0) {
                        listFriend = new ArrayList<Friend>(listBeforreRequest);
                    } else {
                        listFriend = new ArrayList<Friend>();
                    }

                    if (!fromCache) currentPage++;

                    for (ParseObject obj : list) {
                        ParseUser user = (ParseUser) obj.get("friend");
                        Friend friend = new Friend(null, R.string.friends_section, R.drawable.picto_recipient_off);
                        friend.username = user.getString("username");
                        friend.parseId = user.getObjectId();
                        listFriend.add(friend);
                    }

                    //si moins de résultat que d'el par page alors c'est la dernière page
                    endOfLoading = list.size() < NB_BY_PAGE;

                    updateFiltre();
                }
                else
                {
                    L.e(">>>>>>>>>>>>>> ERROR parsequery user - e=" + e.getMessage());
                    e.printStackTrace();
                }

                //si réponse network (2eme reponse)
                if(!fromCache)
                {
                    listView.removeFooterView(footer);
                    isLoading = false;
                }

                fromCache = false;
            }
        });

        return true;
    }

    @Override
    public void onClick(View view)
    {
        if(view == btnBack)
        {
            finish();
        }
        else if(view == btnSearch)
        {
            showEditTextSearch(true);
        }
        else if(view == btnTopBar)
        {
            if(isEditTextShow)
            {
                showEditTextSearch(false);
                editSearch.setText("");
                filtreSearchChange(null);
            }
            else
            {
                startActivity(new Intent(this, FriendsActivity.class));
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        }
        else if(view == btnSend)
        {
            boolean isPublic = true;
            if(listFriend != null)
            {
                for(Friend f : listFriend)//crash #3 listFriend=null
                {
                    if(f.image == R.drawable.picto_recipient_on)
                    {
                        isPublic = false;
                        break;
                    }
                }
            }
            sendPleek(isPublic);
        }
        else if(view == btnSendAll)
        {
            sendPleek(true);
        }
    }

    private void sendPleek(final boolean isPublic)
    {
        final Dialog dialoLoader = showLoader();

        //start time mixpanel for "Send Piki"
        mixpanel.timeEvent("Send Piki");

        ParseUser currentUser = ParseUser.getCurrentUser();

        //create listRecipients
        List<String> tmpListRecipients = null;
        if(!isPublic)
        {
            tmpListRecipients = new ArrayList<String>();
            for(Friend f : listFriend)
            {
                if(f.image == R.drawable.picto_recipient_on) tmpListRecipients.add(f.parseId);
            }
            tmpListRecipients.add(currentUser.getObjectId());
        }
        final List<String> listRecipients = tmpListRecipients;

        //ACL
        ParseACL acl = new ParseACL();
        if(isPublic)
        {
            acl.setPublicReadAccess(true);
        }
        else if(listRecipients != null)
        {
            for(String recipientId : listRecipients)
            {
                acl.setReadAccess(recipientId, true);
            }
        }
        acl.setWriteAccess(currentUser, true);

        //create PIKI
        final ParseObject piki = ParseObject.create("Piki");
        if (!StringUtils.isStringEmpty(videoPath)) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                File inputFile = new File(videoPath.replace("out.mp4", "out1.jpg"));
                FileInputStream fis = new FileInputStream(inputFile);
                byte[] buf = new byte[(int)inputFile.length()];
                for (int readNum; (readNum=fis.read(buf)) != -1;){
                    bos.write(buf,0,readNum);
                }
                piki.put("previewImage", new ParseFile("photo.jpg", bos.toByteArray()));

                inputFile = new File(videoPath);
                bos = new ByteArrayOutputStream();
                fis = new FileInputStream(inputFile);
                buf = new byte[(int)inputFile.length()];
                for (int readNum; (readNum = fis.read(buf)) != -1;){
                    bos.write(buf,0,readNum);
                }

                byte[] bytes = bos.toByteArray();
                piki.put("video", new ParseFile("video.mp4", bytes));
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } else {
            piki.put("photo", new ParseFile("photo.jpg", pikiData));
        }

        if(listRecipients != null) piki.put("recipients", listRecipients);
        piki.put("isPublic", isPublic);
        piki.put("user", currentUser);
        piki.setACL(acl);

        piki.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                {
                    //call function savePiki

                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("pikiId", piki.getObjectId());
                    params.put("type", isPublic ? "sendToAll" : "private");
                    if(listRecipients != null) params.put("recipients", listRecipients);

                    ParseCloud.callFunctionInBackground("savePiki", params, new FunctionCallback<Object>()
                    {
                        @Override
                        public void done(Object o, ParseException e)
                        {
                            hideDialog(dialoLoader);//fix : crash #23

                            if(e == null)
                            {
                                CaptureActivity.CLOSE_ME = true;
                                finish();

                                //mixpanel
                                mixpanel.getPeople().increment("Piki Sent", 1);
                            }
                            else
                            {
                                Utile.showToast(R.string.recipients_savepiki_nok, RecipientsActivity.this);
                            }

                            //send track mixpanel
                            try
                            {
                                JSONObject props = new JSONObject();
                                props.put("media", "photo");
                                props.put("public", isPublic);
                                mixpanel.track("Send Piki", props);
                            }
                            catch (JSONException ee)
                            {
                                ee.printStackTrace();
                            }
                            //send track fbappevents
                            Bundle parameters = new Bundle();
                            parameters.putString("media", "photo");
                            parameters.putString("public", isPublic+"");
                            fbAppEventsLogger.logEvent("Send Piki", parameters);
                        }
                    });
                }
                else
                {
                    hideDialog(dialoLoader);//fix : crash #44
                    Utile.showToast(R.string.recipients_savepiki_nok, RecipientsActivity.this);
                }
            }
        });
    }

    private void updateFiltre()
    {
        String filtreSearch = editSearch.getText().toString();
        if(filtreSearch.trim().length() == 0) filtreSearch = null;
        filtreSearchChange(filtreSearch);
    }

    private void filtreSearchChange(String filtreSearch)
    {
        try
        {
            ArrayList<Friend> listFiltred = new ArrayList<Friend>();

            boolean nofriend = listFriend == null || listFriend.size() == 0;
            boolean nofiltre = filtreSearch == null;

            if(!nofiltre) filtreSearch = filtreSearch.toLowerCase();

            if(!nofriend)
            {
                for(Friend friend : listFriend)
                {
                    if(filtreSearch == null || (friend.username != null && friend.username.toLowerCase().contains(filtreSearch)))
                    {
                        listFiltred.add(friend);
                    }
                }
            }

            boolean noresult = listFiltred.size() == 0;

            txtNoFriends.setVisibility(noresult ? View.VISIBLE : View.GONE);
            txtNoFriends.setText(nofriend && nofiltre ? R.string.friends_nofriend : R.string.friends_noresult);

            adapter.setListFriend(listFiltred);//fix crash : #48
            adapter.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            L.w(">>>>>> ERROR : filtreSearchChange - e="+e.getMessage());
        }
    }

    private final int TIME_ANIM = 250;//ms
    private boolean isAnimating;
    private boolean isEditTextShow;
    private void showEditTextSearch(final boolean show)
    {
        if(isAnimating || isEditTextShow == show) return;

        isAnimating = true;
        isEditTextShow = show;
        if(show)
        {
            Utile.fadeOut(backBtnTopBar, TIME_ANIM);
            Utile.fadeOut(txtTitle, TIME_ANIM);
        }
        else
        {
            Utile.fadeIn(backBtnTopBar, TIME_ANIM);
            Utile.fadeIn(txtTitle, TIME_ANIM);
        }
        ResizeWidthAnimation anim = new ResizeWidthAnimation(marginLeftBtnSearch, show ? 0 : initialWidthMarginLeftBtnSearch);
        anim.setDuration(TIME_ANIM);
        marginLeftBtnSearch.startAnimation(anim);
        RotateAnimation rotateAnim = new RotateAnimation(show ? 0 : 45, show ? 45 : 0, btnTopBar.getWidth() >> 1, btnTopBar.getHeight() >> 1);
        rotateAnim.setDuration(TIME_ANIM);
        rotateAnim.setFillAfter(true);
        imgBnTopBar.startAnimation(rotateAnim);
        rotateAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                if(!show)
                {
                    editSearch.setVisibility(View.GONE);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation)
            {
                if(show)
                {
                    Utile.fadeIn(editSearch, TIME_ANIM >> 2);
                    editSearch.requestFocus();
                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editSearch, 0);
                }
                else
                {
                    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
                }
                isAnimating = false;
            }
        });
    }

    private final int TIME_ANIM_SENDALL = 250;//ms
    private Animation currentAnimationBtn;
    private Animation currentAnimationTxt;
    private boolean isBtnSendAllShow = true;
    private void showBtnSendAll(boolean show)
    {
        showBtnSendAll(show, TIME_ANIM_SENDALL);
    }
    private void showBtnSendAll(final boolean show, int duration)
    {
        if(show == isBtnSendAllShow) return;
        isBtnSendAllShow = show;

        if(show) btnSendAll.setVisibility(View.VISIBLE);

        //animate btnSendAll
        float currentX = 0;
        if(currentAnimationBtn != null)
        {
            Transformation transformation = new Transformation();
            float[] matrix = new float[9];
            currentAnimationBtn.getTransformation(AnimationUtils.currentAnimationTimeMillis(), transformation);
            transformation.getMatrix().getValues(matrix);
            currentX = matrix[Matrix.MTRANS_X];
        }

        currentAnimationBtn = new TranslateAnimation(currentX, show ? 0 : -btnSendAll.getWidth(), 0, 0);
        currentAnimationBtn.setDuration(duration);
        currentAnimationBtn.setFillAfter(true);
        btnSendAll.startAnimation(currentAnimationBtn);

        //animate layoutTxtButton
        currentX = 0;
        if(currentAnimationTxt != null)
        {
            Transformation transformation = new Transformation();
            float[] matrix = new float[9];
            currentAnimationTxt.getTransformation(AnimationUtils.currentAnimationTimeMillis(), transformation);
            transformation.getMatrix().getValues(matrix);
            currentX = matrix[Matrix.MTRANS_X];
        }

        int translateX = ((btnSendAll.getWidth() + screen.dpToPx(25)) - ((screen.getWidth() - layoutTxtButton.getWidth()) >> 1));
        currentAnimationTxt = new TranslateAnimation(currentX, show ? translateX : 0, 0, 0);
        currentAnimationTxt.setDuration(duration);
        currentAnimationTxt.setFillAfter(true);
        layoutTxtButton.startAnimation(currentAnimationTxt);

        //animate textView in layoutTxtButton
        AlphaAnimation animShow = new AlphaAnimation(0, 1);
        animShow.setDuration(duration);
        animShow.setFillAfter(true);
        AlphaAnimation animHide = new AlphaAnimation(1, 0);
        animHide.setDuration(duration);
        animHide.setFillAfter(true);
        if(show)
        {
            txtNbFriendSelected.startAnimation(animShow);
            txtEveryone.startAnimation(animHide);
        }
        else
        {
            txtEveryone.startAnimation(animShow);
            txtNbFriendSelected.startAnimation(animHide);
        }
    }

    @Override
    public void clickOnName(Friend friend)
    {
        friend.image = friend.image == R.drawable.picto_recipient_off ? R.drawable.picto_recipient_on : R.drawable.picto_recipient_off;
        adapter.notifyDataSetChanged();

        int nbFirendChecked = 0;
        for(Friend f : listFriend)
        {
            if(f.image == R.drawable.picto_recipient_on) nbFirendChecked++;
        }

        if(nbFirendChecked <= 1) showBtnSendAll(nbFirendChecked > 0);

        txtNbFriendSelected.setText(Math.max(nbFirendChecked, 1) + " " + getString(R.string.recipients_selected));
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }

    private int lastItemShow;
    private class MyOnScrollListener implements AbsListView.OnScrollListener
    {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {}

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
        {
            //pagination
            int lastItem = firstVisibleItem + visibleItemCount;
            if(lastItem == totalItemCount)
            {
                boolean isFiltred = !editSearch.getText().toString().trim().isEmpty();
                if(lastItemShow < lastItem || isFiltred)
                {
                    if(loadNext())
                    {
                        listView.addFooterView(footer);
                        lastItemShow = lastItem;
                    }
                }
            }
        }
    }
}
