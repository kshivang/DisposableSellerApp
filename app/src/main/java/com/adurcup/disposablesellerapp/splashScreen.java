package com.adurcup.disposablesellerapp;

import android.content.Intent;

import com.daimajia.androidanimations.library.Techniques;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

public class splashScreen extends AwesomeSplash {

    Boolean animationFinished = false;
    @Override
    public void initSplash(ConfigSplash configSplash) {

            /* you don't have to override every property */

        //Customize Circular Reveal
        configSplash.setBackgroundColor(R.color.colorPrimary); //any color you want form colors.xml
        configSplash.setAnimCircularRevealDuration(500); //int ms
        configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);  //or Flags.REVEAL_LEFT
        configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM); //or Flags.REVEAL_TOP

        //Choose LOGO OR PATH; if you don't provide String value for path it's logo by default

        //Customize Logo
        configSplash.setLogoSplash(R.drawable.white_logo); //or any other drawable
        configSplash.setAnimLogoSplashDuration(2000); //int ms
        configSplash.setAnimLogoSplashTechnique(Techniques.FadeIn); //choose one form Techniques (ref: https://github.com/daimajia/AndroidViewAnimations)


        //Customize Path
        configSplash.setPathSplash(Constant.DROID_LOGO); //set path String
        configSplash.setOriginalHeight(300); //in relation to your svg (path) resource
        configSplash.setOriginalWidth(300); //in relation to your svg (path) resource
        configSplash.setAnimPathStrokeDrawingDuration(0);
        configSplash.setPathSplashStrokeSize(5); //I advise value be <5
        configSplash.setPathSplashFillColor(R.color.colorOffWhite); //path object filling color
        configSplash.setPathSplashStrokeColor(R.color.colorPrimary); //any color you want form colors.xml
        configSplash.setAnimPathFillingDuration(1000);


        //Customize Title
        configSplash.setTitleSplash("Welcome to Adurcup");
        configSplash.setTitleTextColor(R.color.colorWhite);
        configSplash.setTitleTextSize(30f); //float value
        configSplash.setAnimTitleDuration(500);
        configSplash.setAnimTitleTechnique(Techniques.DropOut);
        configSplash.setTitleFont("fonts/myfont.ttf"); //provide string to your font located in assets/fonts/

    }

    @Override
    public void animationsFinished() {

        animationFinished = true;
        //transit to another activity here
        //or do whatever you want
        UserLocalStore userLocalStore = new UserLocalStore(this);

        switch (userLocalStore.getUserLoggedIn()){

            case Constant.FLAG_LOGGED_IN : startActivity(new Intent(splashScreen.this, main.class));
                return;
            case Constant.FLAG_LOGGED_OUT : startActivity(new Intent(splashScreen.this, login.class));
                return;
            case Constant.FLAG_SIGN_UP : startActivity(new Intent(splashScreen.this, signUp.class));
                return;
            case Constant.FLAG_NEW_PASSWORD : startActivity(new Intent(splashScreen.this, newPassword.class));
                return;
            case Constant.FLAG_WAITING_FOR_SMS_FORGOT :
            case Constant.FLAG_WAITING_FOR_SMS_SIGNUP: startActivity(new Intent(splashScreen.this, otpVerification.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(animationFinished) {
            finish();
        }
    }
}
