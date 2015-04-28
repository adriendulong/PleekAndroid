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
public class ChangeNameActivity extends ParentActivity implements View.OnClickListener {

    private View btnBack;
    private View btnTopBar;
    private EditText editName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);

        setup();
        init();
    }

    private void setup() {
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.firstColorDark)));
        btnBack.setOnClickListener(this);
        btnTopBar = findViewById(R.id.btnTopBar);
        btnTopBar.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.firstColorDark)));
        btnTopBar.setOnClickListener(this);
        editName = (EditText) findViewById(R.id.editName);
    }

    private void init() {}

    @Override
    public void onClick(View view) {
        if (view == btnBack) {
            finish();
        } else if (view == btnTopBar) {
            final String newName = editName.getText().toString();

            if (newName != null) {
                final Dialog dialog = showLoader();
                ParseUser currentUser = ParseUser.getCurrentUser();

                if (currentUser != null) {
                    currentUser.put("name", newName);
                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            hideDialog(dialog);
                            if (e == null) {
                                Utile.showToast(R.string.param_name_saveok, ChangeNameActivity.this);
                                finish();
                                overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
                            } else {
                                Utile.showToast(R.string.name_no_network, ChangeNameActivity.this);
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }
}
