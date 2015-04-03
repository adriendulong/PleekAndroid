package com.pleek.app.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.DownTouchListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pleek.app.R;

import java.util.List;

/**
 * Created by nicolas on 25/02/15.
 */
public class ChangeUsernameActivity extends ParentActivity implements View.OnClickListener
{
    private View btnBack;
    private View btnTopBar;
    private EditText editUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_sername);

        setup();
        init();
    }

    private void setup()
    {
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.firstColorDark)));
        btnBack.setOnClickListener(this);
        btnTopBar = findViewById(R.id.btnTopBar);
        btnTopBar.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.firstColorDark)));
        btnTopBar.setOnClickListener(this);
        editUsername = (EditText) findViewById(R.id.editUsername);
    }

    private void init()
    {
    }

    @Override
    public void onClick(View view)
    {
        if(view == btnBack)
        {
            finish();
        }
        else if(view == btnTopBar)
        {
            final String newUsername = editUsername.getText().toString();
            if(newUsername != null && newUsername.length() >= UsernameActivity.MIN_SIZE_USERNAME)
            {
                final Dialog dialog = showLoader();
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username", newUsername);
                query.findInBackground(new FindCallback<ParseUser>()
                {
                    public void done(List<ParseUser> objects, ParseException e)
                    {
                        if (e == null)
                        {
                            boolean isValid = objects.isEmpty();
                            ParseUser currentUser = ParseUser.getCurrentUser();
                            if (isValid && currentUser != null)
                            {
                                currentUser.setUsername(newUsername);
                                currentUser.setPassword(newUsername);
                                currentUser.saveInBackground(new SaveCallback()
                                {
                                    @Override
                                    public void done(ParseException e)
                                    {
                                        hideDialog(dialog);
                                        if (e == null)
                                        {
                                            Utile.showToast(R.string.param_username_saveok, ChangeUsernameActivity.this);
                                            finish();
                                            overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
                                        }
                                        else
                                        {
                                            Utile.showToast(R.string.username_check_no_network, ChangeUsernameActivity.this);
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                if(hideDialog(dialog))
                                {
                                    showAlert(R.string.param_username_popup_title, R.string.param_username_popup_texte);
                                }
                                else
                                {
                                    Utile.showToast(R.string.param_username_popup_texte, ChangeUsernameActivity.this);
                                }
                            }
                        }
                        else
                        {
                            hideDialog(dialog);
                            Utile.showToast(R.string.username_check_no_network, ChangeUsernameActivity.this);
                            e.printStackTrace();
                        }
                    }
                });
            }
            else
            {
                Utile.showToast(R.string.param_username_tooshort, ChangeUsernameActivity.this);
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
