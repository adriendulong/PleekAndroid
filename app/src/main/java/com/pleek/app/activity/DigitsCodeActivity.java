package com.pleek.app.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.pleek.app.R;

public class DigitsCodeActivity extends ParentActivity implements View.OnClickListener
{
    public final static String EXTRA_NAME_PHONENUMBER = "extra_phonenumber";
    public final static String EXTRA_NAME_GOODCODE = "extra_goodcode";
    public final static String EXTRA_NAME_USERNAME = "extra_username";

    private TextView txtPhoneNumber;
    private View btnBack;
    private EditText editCode1;
    private EditText editCode2;
    private EditText editCode3;
    private EditText editCode4;
    private View txtBadCode;

    private PhoneNumberUtil phoneUtil;
    private String phoneNumber;
    private String goodCode;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digitscode);

        setup();
        init();
    }

    private void setup()
    {
        if(getIntent() != null)
        {
            phoneNumber = getIntent().getStringExtra(EXTRA_NAME_PHONENUMBER);
            goodCode = getIntent().getStringExtra(EXTRA_NAME_GOODCODE);
            username = getIntent().getStringExtra(EXTRA_NAME_USERNAME);
        }

        phoneUtil = PhoneNumberUtil.getInstance();

        txtPhoneNumber = (TextView)findViewById(R.id.txtPhoneNumber);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        editCode1 = (EditText)findViewById(R.id.editCode1);
        editCode2 = (EditText)findViewById(R.id.editCode2);
        editCode3 = (EditText)findViewById(R.id.editCode3);
        editCode4 = (EditText)findViewById(R.id.editCode4);
        editCode1.addTextChangedListener(new CodeTextWatcher(editCode1));
        editCode2.addTextChangedListener(new CodeTextWatcher(editCode2));
        editCode3.addTextChangedListener(new CodeTextWatcher(editCode3));
        editCode4.addTextChangedListener(new CodeTextWatcher(editCode4));
        //TODO : KEY_DEL event

        txtBadCode = findViewById(R.id.txtBadCode);
        txtBadCode.setVisibility(View.INVISIBLE);
    }

    private void init()
    {
        //Toast.makeText(this, "Code : " + goodCode, Toast.LENGTH_SHORT).show();//TODO : remove

        try
        {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, "");
            String phoneNumberFormated = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            txtPhoneNumber.setText(phoneNumberFormated);
        }
        catch (Exception e)
        {
            L.e("NumberParseException was thrown: " + e.toString());
            e.printStackTrace();
            finish();
            return;
        }
    }

    @Override
    public void onClick(View view)
    {
        if(view == btnBack)
        {
            finish();
        }
    }

    private void valideCode()
    {
        String code = editCode1.getText().toString() + editCode2.getText().toString() + editCode3.getText().toString() + editCode4.getText().toString();
        if(code.equalsIgnoreCase(goodCode))
        {
            if(username != null)
            {
                final Dialog dialog = showLoader();
                ParseUser.logInInBackground(username, username, new LogInCallback()
                {
                    @Override
                    public void done(ParseUser parseUser, ParseException e)
                    {
                        hideDialog(dialog);
                        if(e == null && parseUser != null)
                        {
                            //create alias (Adrien)
                            mixpanel.identify(parseUser.getObjectId());
                            mixpanel.track("Log In", null);

                            //TODO : CHECK GOOD PHONE NUMBER !!!!!!!!!!!!!!!!!

                            //go home
                            Intent i = new Intent(DigitsCodeActivity.this, HomeActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                        }
                        else
                        {
                            Utile.showToast(R.string.phonenumber_no_network, DigitsCodeActivity.this);
                        }
                    }
                });
            }
            else
            {
                //go choose username
                Intent i = new Intent(this, UsernameActivity.class);
                i.putExtra(EXTRA_NAME_PHONENUMBER, phoneNumber);
                startActivity(i);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        }
        else
        {
            showMessageBadCode(true);
        }
    }

    private int TIME_TRANSITION_COLOR = 100;//ms
    private boolean btnBakIsShow;
    private void showMessageBadCode(boolean show)
    {
        txtBadCode.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        if(btnBakIsShow != show)
        {
            TransitionDrawable transition = (TransitionDrawable) btnBack.getBackground();
            if(show) transition.startTransition(TIME_TRANSITION_COLOR);
            else transition.reverseTransition(TIME_TRANSITION_COLOR);
            btnBakIsShow = show;
        }
    }

    /** editText for code */
    private class CodeTextWatcher implements TextWatcher
    {
        private EditText currentEditText;
        private boolean stayOneChar;

        private CodeTextWatcher(EditText currentEditText)
        {
            this.currentEditText = currentEditText;
        }

        @Override public void beforeTextChanged(CharSequence charSequence, int start, int count, int after)
        {
            stayOneChar = count == 1;

            showMessageBadCode(false);
        }

        @Override public void onTextChanged(CharSequence charSequence, int start, int before, int count)
        {
            if(charSequence.length() > 1)
            {
                currentEditText.setText(charSequence.toString().substring(start, start + 1));
            }
        }

        @Override
        public void afterTextChanged(Editable editable)
        {
            boolean emptyNow = editable.toString().isEmpty();

            EditText[] tabEditText = {editCode1, editCode2, editCode3, editCode4};
            boolean allAreFull = true;
            for(int i = 0; i < tabEditText.length; i++)
            {
                EditText et = tabEditText[i];

                //if erase and last char, set focus at before EditText
                if(i > 0 && et == currentEditText && stayOneChar && emptyNow)
                {
                    EditText beforeEditCode = tabEditText[i-1];
                    beforeEditCode.requestFocus();
                    beforeEditCode.setSelection(beforeEditCode.length());
                    allAreFull = false;
                    break;
                }

                //set focus to first empty EditText
                if(et.getText().length() == 0)
                {
                    et.requestFocus();
                    allAreFull = false;
                    break;
                }
            }

            //if any EditText is empty, valide code
            if(allAreFull)
            {
                valideCode();
            }
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }
}
