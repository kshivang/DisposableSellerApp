package com.adurcup.disposablesellerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by kshivang on 01/07/16.
 * this activity is for automatically or manually verify OTP
 */
public class otpVerification extends AppCompatActivity{

    UserLocalStore userLocalStore;
    AppController appController;
    String Tag = "OTP Verification", MobileNo;
    TextView tvCountdown, tvResendOTP;

    @Override
    protected void onCreate(Bundle savedInstances){
        super.onCreate(savedInstances);

        setContentView(R.layout.activity_otp_verification);

        appController = AppController.getInstance();

        userLocalStore = new UserLocalStore(otpVerification.this);

        tvCountdown = (TextView) findViewById(R.id.countDown);

        tvResendOTP = (TextView) findViewById(R.id.resendOtp);

        MobileNo = userLocalStore.getLoggedInUser(Constant.KEY_CONTACT);

        ((TextView) findViewById(R.id.mobile)).setText(MobileNo);

        findViewById(R.id.mobileLL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int IntentFlag = userLocalStore.getUserLoggedIn() - 2;
                startActivity(new Intent(otpVerification.this, mobileVerification.class).
                        putExtra(Constant.KEY_NAV, IntentFlag));
            }
        });

        findViewById(R.id.verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = ((EditText) findViewById(R.id.otp)).getText().toString();

                if (otp.length() == 6){
                    String url = getString(R.string.otp_url) + MobileNo + "&" +
                            getString(R.string.otp_key) + "&code=" + otp;
                    onOtpRequest(url);
                } else {
                    Toast.makeText(otpVerification.this, getString(R.string.wrong_otp_format),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UserLocalStore(otpVerification.this).clearUserData();
                startActivity(new Intent(otpVerification.this, login.class));
            }
        });

        tvResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(otpVerification.this, getString(R.string.otp_expired), Toast.LENGTH_SHORT).show();
                onCountDown();
                String url = getString(R.string.otp_url) + MobileNo + "&"
                        + getString(R.string.otp_key);
                onOtpRequest(url);
            }
        });

        onCountDown();
    }

    private void onCountDown() {
        tvResendOTP.setVisibility(View.GONE);
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                tvCountdown.setText(getString(R.string.zero));
                tvResendOTP.setVisibility(View.VISIBLE);
            }
        }.start();
    }


    private void onOtpRequest(String url){

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        StringRequest otpRequest = new StringRequest(Request.Method.GET, url
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                switch (response){
                    case ("success | 100 | New code generated and code was sent the user"):
                    case ("success | 103 | Previous code is expired and new code was sent to the user again"):
                        Toast.makeText(otpVerification.this, getString(R.string.new_otp_send),
                                Toast.LENGTH_SHORT).show();
                        onCountDown();
                        break;
                    case ("success | 101 | User already in the system and code was sent to the user again"):
                        Toast.makeText(otpVerification.this, getString(R.string.resend_old_otp),
                                Toast.LENGTH_SHORT).show();
                        onCountDown();
                        break;
                    case ("success | 200 | Code matched successfully and user has been verified"):
                        if (userLocalStore.getUserLoggedIn() ==
                                Constant.FLAG_WAITING_FOR_SMS_SIGNUP){
                            startActivity(new Intent(otpVerification.this, signUp.class));
                            userLocalStore.setUserStatus(MobileNo, Constant.FLAG_SIGN_UP);
                            finish();
                        } else {
                            startActivity(new Intent(otpVerification.this, newPassword.class));
                            userLocalStore.setUserStatus(MobileNo, Constant.FLAG_NEW_PASSWORD);
                            finish();
                        }
                        break;
                    case ("error | 907 | Code is expired"):
                        Toast.makeText(otpVerification.this, getString(R.string.otp_expired), Toast.LENGTH_SHORT).show();
                        onCountDown();
                        String url = getString(R.string.otp_url) + MobileNo + "&"
                                + getString(R.string.otp_key);
                        onOtpRequest(url);
                        break;
                    case ("error | 903 | Invalid code"):
                        Toast.makeText(otpVerification.this, getString(R.string.invalid_otp), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(otpVerification.this, getString(R.string.down_server), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("otpVerification", error);

                progressBar.setVisibility(View.GONE);

                Toast.makeText(otpVerification.this,
                        getString(R.string.network_connection_error),
                        Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }
        };
        appController.addToRequestQueue(otpRequest, Tag);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        onCountDown();

        setIntent(intent);
    }

    @Override
    protected void onStop(){
        super.onStop();
        appController.cancelPendingRequests(Tag);
    }
}
