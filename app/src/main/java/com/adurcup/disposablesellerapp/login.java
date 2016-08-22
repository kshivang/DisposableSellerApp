package com.adurcup.disposablesellerapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.eftimoff.androidplayer.Player;
import com.eftimoff.androidplayer.actions.property.PropertyAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kshivang on 01/07/16.
 * This is login activity.
 */
public class login extends AppCompatActivity {

    String tag_login_req = "login_request";
    AppController appController;
    String MobileNo;

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);

        /**
         * appController control all request instance, login request has highest priority
         */
        appController = AppController.getInstance();

        setContentView(R.layout.activity_login);

        /**
         * Variable Initialization
         */
        final TextInputLayout tilPassword = (TextInputLayout) findViewById(R.id.passwordInput);
        final TextInputLayout tilMobile = (TextInputLayout) findViewById(R.id.mobileInput);
        final UserLocalStore userLocalStore = new UserLocalStore(this);
        final inputTextHandler mobileNumHandler = new inputTextHandler(this,
                Constant.CHECK_MOBILE_NUM, tilMobile);
        final inputTextHandler passwordHandler = new inputTextHandler(this,
                Constant.CHECK_PASSWORD, tilPassword);
        final Intent iMobileVerification = new Intent(login.this, mobileVerification.class);

        /**
         * Check for previously entered mobile num and if exist fill it in text field
         */
        String SavedContact = userLocalStore.getLoggedInUser(Constant.KEY_CONTACT);
        if (SavedContact == null || SavedContact.length() == 0) {
            tilMobile.setError(getString(R.string.empty_necessary_field));
        } else {
            if (tilMobile.getEditText() != null)
                tilMobile.getEditText().setText(SavedContact);
        }
        tilPassword.setError(getString(R.string.empty_necessary_field));

        /**
         * Check for entered mobile num format
         * Note: mobileNumHandler.getValue() give the current value of text input
         */
        MobileNo = mobileNumHandler.getValue();


        /**
         * on click Listeners
         */
        findViewById(R.id.registerHere).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iMobileVerification.putExtra(Constant.KEY_NAV, Constant.FLAG_SIGN_UP);
                startActivity(iMobileVerification);
            }
        });

        findViewById(R.id.forgotPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iMobileVerification.putExtra(Constant.KEY_NAV, Constant.FLAG_NEW_PASSWORD);
                startActivity(iMobileVerification);
            }
        });

        Button btLogin = (Button) findViewById(R.id.login);

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * if soft keyboard is on the screen, hide it
                 */
                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                /**
                 * Check whether all necessary field are not empty
                 * Note: We must update the MobileNo variable for latest value of text input
                 */
                MobileNo = mobileNumHandler.getValue();
                String Password = passwordHandler.getValue();
                if ((MobileNo != null) &&( MobileNo.length() > 0) &&
                        (Password != null) &&
                        (Password.length() > 0)) {
                    onLogin(MobileNo, Password, userLocalStore);
                } else
                    Toast.makeText(login.this, getString(R.string.necessary_fields_missing),
                            Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * Login activity animation
         */
        final PropertyAction fabAction = PropertyAction.
                newPropertyAction(findViewById(R.id.body)).
                scaleX(0).
                scaleY(0).
                duration(750).
                interpolator(new AccelerateDecelerateInterpolator()).
                build();
        final PropertyAction headerAction = PropertyAction.
                newPropertyAction(findViewById(R.id.adurcup_logo)).
                interpolator(new DecelerateInterpolator()).
                translationY(-200).
                duration(750).
                alpha(0.4f).
                build();
        final PropertyAction bottomAction = PropertyAction.
                newPropertyAction(findViewById(R.id.footer)).
                translationY(500).
                duration(750).
                alpha(0f).
                build();

        Player.init().animate(headerAction)
                .animate(fabAction)
                .animate(bottomAction).
                play();
    }

    /**
     * This function handle login request
     * @param mobile: This is etMobile text input
     * @param password: This is etPassword text input
     * @param userLocalStore: UserLocalStore is Store data Locally
     */
    private void onLogin(final String mobile, final String password,
                        final UserLocalStore userLocalStore){

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        appController.cancelPendingRequests(tag_login_req);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                getString(R.string.login_url),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String stResponse) {
                        try {
                            JSONObject response = new JSONObject(stResponse);
                            Boolean error = response.getBoolean(Constant.KEY_ERROR);

                            progressBar.setVisibility(View.GONE);
                            if (error) {
                                String message = response.getString(Constant.KEY_MESSAGE);
                                Toast.makeText(login.this, message, Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                userLocalStore.storeUserData(
                                        response.getString(Constant.KEY_API_KEY),
                                        response.getString(Constant.KEY_NAME),
                                        response.getString(Constant.KEY_EMAIL),
                                        response.getString(Constant.KEY_CONTACT));

                                startActivity(new Intent(login.this, main.class));
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(login.this, getString(
                                R.string.network_connection_error),
                                Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError{

                Map<String, String> params = new HashMap<>();

                params.put(Constant.KEY_CONTACT, mobile);
                params.put(Constant.KEY_PASSWORD, password);

                return params;
            }

            @Override
            public Priority getPriority(){
                return Priority.IMMEDIATE;
            }
        };
        stringRequest.setShouldCache(false);
        appController.addToRequestQueue(stringRequest, tag_login_req);
    }

    /**
     * onStop clear all pending requests
     */
    @Override
    protected void onStop(){
        super.onStop();
        appController.cancelPendingRequests(tag_login_req);
    }
}