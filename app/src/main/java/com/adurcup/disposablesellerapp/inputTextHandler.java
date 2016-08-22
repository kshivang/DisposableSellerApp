package com.adurcup.disposablesellerapp;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by kshivang on 22/08/16.
 * This class is for handling input text in TextInputLayout with proper error
 */
public class inputTextHandler {

    /**
     * Variables
     */
    private String value;
    private Context mContext;

    /**
     * Getter for variables
     */

    public String getValue() {
        return value;
    }

    /**
     * inputTextHandler: Check whether given no. input is proper consumer no or not
     * and show alert accordingly
     * @param checkParam: This specify the check type
     * @param textInputLayout: This is TextInputLayout which is used to show alert
     *                       text just below the field
     */
    public inputTextHandler (final Context mContext, final int checkParam,
                                  final TextInputLayout textInputLayout){

        this.mContext = mContext;

        final EditText editText = textInputLayout.getEditText();

        if(editText != null) {

            editText.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    value = editText.getText().toString();
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    onCheck(checkParam, editText, textInputLayout);
                }
            });
        }
    }

    /**
     * This function check for text input and select alert behaviour accordingly
     * @param checkParam: Parameter to be checked
     * @param editText: EditText of that parameter
     * @param textInputLayout: TextInputLayout of that parameter
     */
    private void onCheck(int checkParam, EditText editText,
                         TextInputLayout textInputLayout) {

        switch (checkParam){
            case Constant.CHECK_MOBILE_NUM:
                value = editText.getText().toString();
                if (value.length() == 0) {
                    textInputLayout.setError(mContext.getString(R.string.empty_necessary_field));
                } else if (!value.matches("^(((\\+)?91)?|(0)?)([6-9])[0-9]{9}(?!\\d)")) {
                    textInputLayout.setErrorEnabled(true);
                    if (!value.matches("^(\\+)?[0-9]+")) {
                        textInputLayout.setError(mContext.getString(R.string.invalid_character));
                    } else if (value.length() < 13) {
                        textInputLayout.setError(mContext.getString(R.string.insufficient_digit));
                    } else {
                        textInputLayout.setError(mContext.getString(R.string.check_your_mobile_number));
                    }
                } else
                    textInputLayout.setErrorEnabled(false);
                break;

            case Constant.CHECK_PASSWORD:
                value = editText.getText().toString();
                if (value.length() == 0) {
                    textInputLayout.setError(mContext.getString(R.string.empty_necessary_field));
                } else if (value.length() < 6) {
                    textInputLayout.setError(mContext.getString(R.string.short_password));
                } else {
                    textInputLayout.setErrorEnabled(false);
                }
                break;
        }
    }
}
