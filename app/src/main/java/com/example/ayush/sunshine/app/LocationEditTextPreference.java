package com.example.ayush.sunshine.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by Ayush on 15-07-2016.
 */
public class LocationEditTextPreference extends EditTextPreference{
    static final private int DEFAULT_MIN_LENGTH = 2;
    private int mMinLen;
    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0, 0);
        try {
            mMinLen = a.getInt(R.styleable.LocationEditTextPreference_min_len,DEFAULT_MIN_LENGTH);
            Log.e("MinLen", String.valueOf(mMinLen));
        }finally {
            a.recycle();
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                Log.e("beforeTextChanged: ","start:"+start );
//                Log.e("beforeTextChanged: ","count:"+count );
//                Log.e("beforeTextChanged: ","after:"+after );
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.e("OnTextChanged: ","start:"+start );
//                Log.e("OnTextChanged: ","count:"+count );
//                Log.e("OnTextChanged: ","before:"+before );
            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog d = getDialog();
                if(d instanceof AlertDialog){
                    AlertDialog alertDialog = (AlertDialog) d;
                    if(s.length()<mMinLen){
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                    else{
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            }
        });
    }
}
