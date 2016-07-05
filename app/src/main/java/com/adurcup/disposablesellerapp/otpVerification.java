package com.adurcup.disposablesellerapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by kshivang on 01/07/16.
 * this activity is for automatically or manually verify OTP
 */
public class otpVerification extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstances){
        super.onCreate(savedInstances);

        setContentView(R.layout.activity_otp_verification);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UserLocalStore(otpVerification.this).clearUserData();
            }
        });

    }
}
