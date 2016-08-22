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
 * This Activity if for mobile no. verification
 */
public class mobileVerification extends AppCompatActivity {

    private AppController appController;
    private String tag_mobile_verification = "Mobile Verification",
            MobileNo;
    private int Nav;
    private UserLocalStore userLocalStore;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mobile_verification);

        Nav = getIntent().getIntExtra(Constant.KEY_NAV, Constant.FLAG_SIGN_UP);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        appController = AppController.getInstance();

        userLocalStore = new UserLocalStore(this);

        final TextInputLayout tilMobile = (TextInputLayout) findViewById(R.id.mobileInput);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mobileVerification.this, login.class));
            }
        });

        String SavedContact = userLocalStore.getLoggedInUser(Constant.KEY_CONTACT);

        if (SavedContact == null || SavedContact.length() == 0) {
            tilMobile.setError(getString(R.string.empty_necessary_field));
        } else {
            if (tilMobile.getEditText() != null)
                tilMobile.getEditText().setText(SavedContact);
        }

        final inputTextHandler MobileNumHandler = new inputTextHandler(this,
                Constant.CHECK_MOBILE_NUM, tilMobile);
        MobileNo = MobileNumHandler.getValue();

        findViewById(R.id.verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                MobileNo = MobileNumHandler.getValue();
                if (MobileNo != null && MobileNo.length() != 0) {

                    progressBar.setVisibility(View.VISIBLE);

                    if (tilMobile.getError() == null) {
                        MobileNo = MobileNo.substring(MobileNo.length() - 10);
                        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                                getString(R.string.check_user_url),
                                new Response.Listener<String>() {

                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject object = new JSONObject(response);
                                            Boolean NewUser = object.getBoolean(Constant.KEY_ERROR);

                                            if (NewUser) {
                                                if (Nav == Constant.FLAG_SIGN_UP) {
                                                    otpRequest(getString(R.string.otp_url) + MobileNo + "&" + getString(R.string.otp_key));
                                                } else {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    ShowDialog(getString(R.string.not_registered),
                                                            getString(R.string.signup_or_login),
                                                            getString(R.string.signup),
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    Nav = Constant.FLAG_SIGN_UP;
                                                                    otpRequest(getString(R.string.otp_url) + MobileNo + "&" + getString(R.string.otp_key));
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
                                            } else {
                                                if (Nav == Constant.FLAG_NEW_PASSWORD) {
                                                    otpRequest(getString(R.string.otp_url) + MobileNo + "&" + getString(R.string.otp_key));
                                                } else {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    ShowDialog(getString(R.string.already_registered),
                                                            getString(R.string.change_or_login),
                                                            getString(R.string.change_password),
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    Nav = Constant.FLAG_NEW_PASSWORD;
                                                                    otpRequest(getString(R.string.otp_url) + MobileNo + "&" + getString(R.string.otp_key));
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
                                params.put(Constant.KEY_CONTACT, MobileNo);
                                return params;
                            }

                            @Override
                            public Priority getPriority() {
                                return Priority.IMMEDIATE;
                            }
                        };
                        stringRequest.setShouldCache(false);
                        appController.addToRequestQueue(stringRequest, tag_mobile_verification);
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

    private void otpRequest(String url){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressBar.setVisibility(View.INVISIBLE);
                        userLocalStore.setUserStatus(MobileNo, Nav - 2);
                        startActivity(new Intent(mobileVerification.this, otpVerification.class));

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("mobileVerification", "Error:" + error.getMessage());
                Toast.makeText(mobileVerification.this, getString(R.string.network_connection_error),
                        Toast.LENGTH_SHORT).show();
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
}
