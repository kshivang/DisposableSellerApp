package com.adurcup.disposablesellerapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);

        appController = AppController.getInstance();

        setContentView(R.layout.activity_login);

        final EditText etMobile = (EditText) findViewById(R.id.mobile),
                etPassword = (EditText) findViewById(R.id.password);

        final UserLocalStore userLocalStore = new UserLocalStore(this);

        etMobile.setText(userLocalStore.getLoggedInUser(Constant.KEY_CONTACT));

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final Intent iMobileVerification = new Intent(login.this, mobileVerification.class);

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
                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if ((etMobile.getText().length() > 0) && (etPassword.getText().length() > 0)) {
                    appController.cancelPendingRequests(tag_login_req);
                    progressBar.setVisibility(View.VISIBLE);
                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                            getString(R.string.login_url),
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        Boolean error = response.getBoolean(Constant.KEY_ERROR);

                                        if (error) {
                                            String message = response.getString(Constant.KEY_MESSAGE);
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(login.this, message, Toast.LENGTH_SHORT)
                                                    .show();
                                        } else {
                                            userLocalStore.storeUserData(
                                                    response.getString(Constant.KEY_API_KEY),
                                                    response.getString(Constant.KEY_NAME),
                                                    response.getString(Constant.KEY_EMAIL),
                                                    response.getString(Constant.KEY_CONTACT));

                                            progressBar.setVisibility(View.GONE);
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

                            params.put(Constant.KEY_CONTACT, etMobile.getText().toString());
                            params.put(Constant.KEY_PASSWORD, etPassword.getText().toString());

                            return params;
                        }

                        @Override
                        public Priority getPriority(){
                            return Priority.IMMEDIATE;
                        }
                    };
                    jsonObjReq.setShouldCache(false);
                    appController.addToRequestQueue(jsonObjReq, tag_login_req);
                } else
                    Toast.makeText(login.this, getString(R.string.necessary_fields_missing),
                            Toast.LENGTH_SHORT).show();
            }
        });

        final PropertyAction fabAction = PropertyAction.newPropertyAction(findViewById(R.id.body)).
                scaleX(0).
                scaleY(0).
                duration(750).
                interpolator(new AccelerateDecelerateInterpolator()).
                build();
        final PropertyAction headerAction = PropertyAction.newPropertyAction(findViewById(R.id.adurcup_logo)).
                interpolator(new DecelerateInterpolator()).
                translationY(-200).
                duration(750).
                alpha(0.4f).
                build();
        final PropertyAction bottomAction = PropertyAction.newPropertyAction(findViewById(R.id.footer)).
                translationY(500).
                duration(750).
                alpha(0f).
                build();

        Player.init().animate(headerAction)
                .animate(fabAction)
                .animate(bottomAction).
                play();
    }

    @Override
    protected void onStop(){
        super.onStop();
        appController.cancelPendingRequests(tag_login_req);
    }
}