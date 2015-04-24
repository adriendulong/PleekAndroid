package com.pleek.app.bean;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.EditText;

import com.goandup.lib.utile.Screen;
import com.goandup.lib.utile.Utile;

/**
 * Created by nicolas on 11/02/15.
 */
public class AutoResizeFontTextWatcher implements TextWatcher {
    private EditText editText;
    private int maxWidth;
    private int maxHeight;
    private int maxFontSize;

    private boolean changeByMe;
    private int beforeLength;
    private int currentTextSize;
    private Screen screen;

    public AutoResizeFontTextWatcher(EditText editText, int maxWidth, int maxHeight, int maxFontSize) {
        screen = Screen.getInstance(editText.getContext());

        this.editText = editText;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxFontSize = maxFontSize;

        currentTextSize = maxFontSize;
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (changeByMe) changeByMe = false;
        else {
            String str = editable.toString();
            boolean containNewline = str.contains("\n");
            String strNoSpace = str.replaceAll("\\s+", " ");
            boolean containMutipleSpace = strNoSpace.length() != str.length();
            if (containNewline || containMutipleSpace) {
                if(containNewline) str = str.replaceAll("\\n", "");
                if(containMutipleSpace) str = strNoSpace;
                changeByMe = true;
                editText.setText(str);
                editText.setSelection(str.length());
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        beforeLength = s.length();
    }

    @Override public void onTextChanged(final CharSequence s, int start, int before, int count) {
        int length = s.toString().length();
        final boolean addText = length > beforeLength;
        int newHeight = Utile.getTextHeight(s.toString(), maxWidth, editText.getTextSize(), editText.getTypeface(), 1f);

        if (addText) {
            if (newHeight > maxHeight) {
                currentTextSize *= .8f;
                editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, currentTextSize);
            }
        } else {
            float newPotentialSize = (currentTextSize / .8f);

            if (newPotentialSize <= maxFontSize) {
                int newPotentialHeight = Utile.getTextHeight(s.toString(), maxWidth, newPotentialSize, editText.getTypeface(), 1.2f);

                if (newPotentialHeight <= maxHeight) {
                    editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, newPotentialSize);
                    currentTextSize = (int) newPotentialSize;
                }
            }
        }
    }

    public void reset() {
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, maxFontSize);
        currentTextSize = maxFontSize;
    }
}
