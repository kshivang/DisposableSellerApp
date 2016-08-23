package com.adurcup.disposablesellerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kshivang on 01/07/16.
 * This Activity if for mobile num verification
 */
public class mobileVerification extends AppCompatActivity {

    private AppController appController;
    private String tag_mobile_verification = "Mobile Verification",
            MobileNum;
    private int Nav;
    private UserLocalStore userLocalStore;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mobile_verification);

        /**
         * Initialisation of variable
         */
        appController = AppController.getInstance();
        userLocalStore = new UserLocalStore(this);
        Nav = getIntent().getIntExtra(Constant.KEY_NAV, Constant.FLAG_SIGN_UP);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final TextInputLayout tilMobile = (TextInputLayout) findViewById(R.id.mobileInput);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mobileVerification.this, login.class));
            }
        });

        /**
         * if mobile num already saved fill editView with it
         */
        String SavedContact = userLocalStore.getLoggedInUser(Constant.KEY_CONTACT);
        if (SavedContact == null || SavedContact.length() == 0) {
            tilMobile.setError(getString(R.string.empty_necessary_field));
        } else {
            if (tilMobile.getEditText() != null)
                tilMobile.getEditText().setText(SavedContact);
        }

        /**
         * Handle textInput Error Message
         */
        final inputTextHandler MobileNumHandler = new inputTextHandler(this,
                Constant.CHECK_MOBILE_NUM, tilMobile);
        MobileNum = MobileNumHandler.getValue();

        findViewById(R.id.verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                MobileNum = MobileNumHandler.getValue();

                if (MobileNum != null && MobileNum.length() != 0) {

                    progressBar.setVisibility(View.VISIBLE);

                    if (tilMobile.getError() == null) {

                        /**
                         * We have to take just last ten digit of mobile num
                         */
                        MobileNum = MobileNum.substring(MobileNum.length() - 10);
                        onCheckUserExist();

                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(mobileVerification.this,
                                tilMobile.getError().toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mobileVerification.this,
                            getString(R.string.empty_necessary_field), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
    }

    @Override
    protected void onStop(){
        super.onStop();
        appController.cancelPendingRequests(tag_mobile_verification);
    }

    /**
     * This check for Mobile num exist on server or not
     * and decide behaviour accordingly
     */
    private void onCheckUserExist() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                getString(R.string.check_user_url),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            Boolean NewUser = object.getBoolean(Constant.KEY_ERROR);

                            if (NewUser) {
                                onNewUser();
                            } else {
                                onExistedUser();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        progressBar.setVisibility(View.INVISIBLE);
                        VolleyLog.d(tag_mobile_verification, error.networkResponse);
                        Toast.makeText(mobileVerification.this, getString(
                                R.string.network_connection_error),
                                Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put(Constant.KEY_CONTACT, MobileNum);
                return params;
            }

            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }
        };
        stringRequest.setShouldCache(false);
        appController.addToRequestQueue(stringRequest, tag_mobile_verification);
    }

    /**
     * Show Dialog with positive and negative Button
     * @param title: This is title of Dialog
     * @param message: This is message to be shown in Dialog
     * @param PositiveButton: This is the Positive Button Name
     * @param ocPositive: This is the Positive Button onClickListener
     * @param NegativeButton: This is the Negative Button Name
     * @param ocNegative: This is the Negative Button onClickListener
     */
    private void ShowDialog(String title, CharSequence message, String PositiveButton,
                            DialogInterface.OnClickListener ocPositive, String NegativeButton,
                            DialogInterface.OnClickListener ocNegative) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) {
            builder.setTitle(title);
        }

        builder.setMessage(message);
        builder.setPositiveButton(PositiveButton, ocPositive);
        builder.setNegativeButton(NegativeButton, ocNegative);
        builder.show();
    }

    /**
     * For new User sign up or or prompt to sign up
     */
    private void onNewUser (){
        if (Nav == Constant.FLAG_SIGN_UP) {
            onOTPRequest(getString(R.string.otp_url) +
                    MobileNum + "&" + getString(R.string.otp_key));
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            ShowDialog(getString(R.string.not_registered),
                    getString(R.string.signup_or_login),
                    getString(R.string.signup),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Nav = Constant.FLAG_SIGN_UP;
                            onOTPRequest(getString(R.string.otp_url) + MobileNum + "&" +
                                    getString(R.string.otp_key));
                            dialogInterface.dismiss();
                        }
                    },
                    getString(R.string.login),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    });
        }
    }

    /**
     * For Existed User New Password or Prompt for new Password
     */
    private void onExistedUser () {
        if (Nav == Constant.FLAG_NEW_PASSWORD) {
            onOTPRequest(getString(R.string.otp_url) + MobileNum +
                    "&" + getString(R.string.otp_key));
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            ShowDialog(getString(R.string.already_registered),
                    getString(R.string.change_or_login),
                    getString(R.string.change_password),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Nav = Constant.FLAG_NEW_PASSWORD;
                            onOTPRequest(getString(R.string.otp_url) + MobileNum +
                                    "&" + getString(R.string.otp_key));
                            dialogInterface.dismiss();
                        }
                    },
                    getString(R.string.login),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    });
        }
    }

    /**
     * Handle OTP api requests
     * @param url: URL for get request
     */
    private void onOTPRequest (String url){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressBar.setVisibility(View.INVISIBLE);
                        /**
                         * Note: Nav - 2 change status from mobileVerification to waiting for SMS
                         */
                        userLocalStore.setUserStatus(MobileNum, Nav - 2);
                        startActivity(new Intent(mobileVerification.this, otpVerification.class));

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("mobileVerification", "Error:" + error.getMessage());
                Toast.makeText(mobileVerification.this, getString(R.string.
                        network_connection_error), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        }){
            @Override
            public Priority getPriority(){
                return Priority.IMMEDIATE;
            }
        };
        appController.addToRequestQueue(stringRequest, tag_mobile_verification);
    }
}
