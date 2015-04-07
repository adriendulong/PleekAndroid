package com.pleek.app.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.DownTouchListener;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.pleek.app.R;
import com.pleek.app.adapter.CountryPhoneNumberAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PhoneNumberActivity extends ParentActivity implements View.OnClickListener, CountryPhoneNumberAdapter.Listener
{
    private View btnDaft;
    private TextView txtCountryCode;
    private TextView txtCountryName;
    private EditText editPhoneNumber;
    private View containerSpinner;
    private View viewCountry;
    private View btnCloseChangeCountry;
    private ListView listviewCountry;
    private View btnNext;

    private PhoneNumberUtil phoneUtil;
    private CountryPhoneNumberAdapter adapter;
    private String countryCodeSelected;
    private String currentPhoneNumber;
    private MediaPlayer mediaplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonenumber);

        setup();
        init();
    }

    private void setup()
    {
        phoneUtil = PhoneNumberUtil.getInstance();

        btnDaft = findViewById(R.id.btnDaft);
        btnDaft.setOnClickListener(this);
        btnDaft.setSoundEffectsEnabled(false);
        txtCountryCode = (TextView)findViewById(R.id.txtCountryCode);
        txtCountryName = (TextView)findViewById(R.id.txtCountryName);
        editPhoneNumber = (EditText)findViewById(R.id.editPhoneNumber);
        editPhoneNumber.addTextChangedListener(new PhoneNumberTextWatcher());
        containerSpinner = findViewById(R.id.containerSpinner);
        containerSpinner.setOnClickListener(this);
        int colorDown = getResources().getColor(R.color.blancTouchDown);
        containerSpinner.setOnTouchListener(new DownTouchListener(colorDown));
        viewCountry = findViewById(R.id.viewCountry);
        viewCountry.setVisibility(View.GONE);
        listviewCountry = (ListView) findViewById(R.id.listCountry);
        btnCloseChangeCountry = findViewById(R.id.btnCloseChangeCountry);
        btnCloseChangeCountry.setOnTouchListener(new DownTouchListener(colorDown));
        btnCloseChangeCountry.setOnClickListener(this);
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
    }

    private void init()
    {
        //init adapter for list Country
        adapter = new CountryPhoneNumberAdapter(this);

        List<String> listCodeCountry = new LinkedList<String>(phoneUtil.getSupportedRegions());
        LinkedList<CountryPhoneNumberAdapter.Country> listCountry = new LinkedList<CountryPhoneNumberAdapter.Country>();

        //create list country, with code (ex : "FR") and name (ex : "France : +33")
        for (String countryCode : listCodeCountry)
        {
            String countryName = (new Locale("", countryCode).getDisplayCountry());// + " : +" + phoneUtil.getCountryCodeForRegion(countryCode);
            listCountry.add(adapter.new Country(countryCode, countryName));
        }

        //sort listCountry by country name
        //Collections.sort(listCountry, new CountryForSpinnerComparator());
        Collections.sort(listCountry);

        //search index of current Android default Country
        String countryCodeDefault = "US";
        for(int i = 0; i < listCountry.size(); i++)
        {
            CountryPhoneNumberAdapter.Country country = listCountry.get(i);
            if(Locale.getDefault().getCountry().equals(country.code))
            {
                countryCodeDefault = country.code;
                break;
            }
        }
        initWithCodeCountry(countryCodeDefault);

        adapter.setListCountry(listCountry);
        listviewCountry.setAdapter(adapter);
    }

    @Override
    public void onClick(View view)
    {
        if(view == containerSpinner)
        {
            showListCountry();
        }
        else if(view == btnCloseChangeCountry)
        {
            hideListCountry();
        }
        else if(view == btnNext)
        {
            if(currentPhoneNumber != null)
            {
                //close keyboard
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editPhoneNumber.getWindowToken(), 0);

                Map<String, String> params = new HashMap<String, String>();
                params.put("phoneNumber", currentPhoneNumber);

                final Dialog dialoLoader = showLoader();
                ParseCloud.callFunctionInBackground("confirmPhoneNumber", params, new FunctionCallback<ArrayList>() {
                    @Override
                    public void done(ArrayList rep, ParseException e)
                    {
                        String goodCodeTmp = null;
                        String usernameTmp = null;
                        if (e == null && rep != null && !rep.isEmpty() && rep.get(0) != null)
                        {
                            HashMap<String, Object> repMap = (HashMap<String, Object>) rep.get(0);
                            goodCodeTmp = repMap.get("randomNumber")+"";
                            usernameTmp = (String)repMap.get("username");
                            System.out.println("NUMBER : " + goodCodeTmp);
                        }
                        else
                        {
                            L.e("ERREUR confirmPhoneNumber erreur=[" + e.getMessage() + "]");
                            e.printStackTrace();
                        }
                        final String goodCode = goodCodeTmp;
                        final String username = usernameTmp;

                        //FIX BUG : in UI thread for bug with overridePendingTransition()
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                hideDialog(dialoLoader);//fix : crash #27
                                if (goodCode != null)
                                {
                                    Intent i = new Intent(PhoneNumberActivity.this, DigitsCodeActivity.class);
                                    i.putExtra(DigitsCodeActivity.EXTRA_NAME_GOODCODE, goodCode);
                                    i.putExtra(DigitsCodeActivity.EXTRA_NAME_PHONENUMBER, currentPhoneNumber);
                                    i.putExtra(DigitsCodeActivity.EXTRA_NAME_USERNAME, username);
                                    startActivity(i);
                                    overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                                }
                                else
                                {
                                    Utile.showToast(R.string.phonenumber_no_network, PhoneNumberActivity.this);
                                }
                            }
                        });
                    }
                });
            }
            else
            {
                Toast.makeText(getBaseContext(), R.string.phonenumber_numberinvalid, Toast.LENGTH_SHORT).show();
            }

//                //FIX GLITCH : timer of 50ms for completely cose keyboard before startActivity.
//                new Timer().schedule(new TimerTask()
//                {
//                    @Override
//                    public void run()
//                    {
//                        //FIX BUG : in UI thread for bug with overridePendingTransition()
//                        runOnUiThread(new Runnable()
//                        {
//                            @Override
//                            public void run()
//                            {
//                                Intent i = new Intent(PhoneNumberActivity.this, DigitsCodeActivity.class);
//                                i.putExtra(DigitsCodeActivity.EXTRA_NAME_PHONENUMBER, currentPhoneNumber);
//                                startActivity(i);
//                                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
//                            }
//                        });
//                    }
//                }, 50);
        }
        else if(view == btnDaft)
        {
            playDaftSong();
        }
    }

    private int nextSong;
    private void playDaftSong()
    {
        int[] tabSong = {
                R.raw.daft_harder,
                R.raw.daft_better,
                R.raw.daft_faster,
                R.raw.daft_stronger
        };

        stopSong();

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        btnDaft.startAnimation(animation);

        mediaplayer = MediaPlayer.create(this, tabSong[nextSong]);
        mediaplayer.setVolume(1f, 1f);
        mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                stopSong();
            }

        });
        mediaplayer.start();

        nextSong = (nextSong+1) % tabSong.length;
    }

    private void stopSong()
    {
        if (mediaplayer != null)
        {
            mediaplayer.stop();
            mediaplayer.release();
            mediaplayer = null;
        }
    }

    @Override
    public void clickOnClountry(CountryPhoneNumberAdapter.Country country)
    {
        initWithCodeCountry(country.code);
        hideListCountry();
        checkValidNumber();
    }

    private void initWithCodeCountry(String codeCountry)
    {
        countryCodeSelected = codeCountry;
        String countryName = (new Locale("", codeCountry).getDisplayCountry()).toUpperCase();
        String countryCode = "+" + phoneUtil.getCountryCodeForRegion(codeCountry);
        txtCountryCode.setText(countryCode);
        txtCountryName.setText(countryName);
    }

    private boolean listCountryIsShow;
    private void toggleListCountry()
    {
        if(listCountryIsShow) hideListCountry();
        else showListCountry();
    }

    private void showListCountry()
    {
        if(listCountryIsShow) return;

        //close keyboard
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editPhoneNumber.getWindowToken(), 0);


        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.view_popin_bottom);

        viewCountry.startAnimation(bottomUp);
        viewCountry.setVisibility(View.VISIBLE);

        listCountryIsShow = true;
    }

    private void hideListCountry()
    {
        if(!listCountryIsShow) return;

        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.view_popout_bottom);

        viewCountry.startAnimation(bottomUp);
        viewCountry.setVisibility(View.GONE);

        listCountryIsShow = false;
    }

    private int TIME_TRANSITION_COLOR = 100;//ms
    private void checkValidNumber()
    {
        boolean wasValid = currentPhoneNumber != null;

        //parse and format number
        int code = phoneUtil.getCountryCodeForRegion(countryCodeSelected);
        String tmpNum = "+" + code + " " + editPhoneNumber.getText();
        currentPhoneNumber = formatMobileNumber(tmpNum, countryCodeSelected);
        if(currentPhoneNumber == null)
        {
            currentPhoneNumber = formatMobileNumber(editPhoneNumber.getText().toString(), countryCodeSelected);
        }

        //change color button
        boolean isValid = currentPhoneNumber != null;
        TransitionDrawable transition = (TransitionDrawable) btnNext.getBackground();
        if(isValid && !wasValid)//if valid and previous was invalid
        {
            transition.startTransition(TIME_TRANSITION_COLOR);
        }
        if(!isValid && wasValid)//if invalid and previous was valid
        {
            transition.reverseTransition(TIME_TRANSITION_COLOR);
        }
    }

    private String formatMobileNumber(String number, String countryCode)
    {
        try
        {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, countryCode);
            PhoneNumberUtil.PhoneNumberType type = phoneUtil.getNumberType(phoneNumber);
            boolean isMobile = type == PhoneNumberUtil.PhoneNumberType.MOBILE || type == PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE;
            if(phoneUtil.isValidNumber(phoneNumber) && (isMobile || type == PhoneNumberUtil.PhoneNumberType.UNKNOWN))
            {
                return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        }
        catch (Exception e) {}

        return null;
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }

    /** editText for code */
    private class PhoneNumberTextWatcher implements TextWatcher
    {
        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
        @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

        @Override
        public void afterTextChanged(Editable editable)
        {
            checkValidNumber();
        }
    }
}
