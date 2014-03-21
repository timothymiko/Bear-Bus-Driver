package com.bearbusdriver;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by timothymiko on 3/19/14.
 */


public class ParseApplication extends Application {

    public static final String PARSE_APP_ID = "Hr5DPwQzhmzzST1sNzME8ssu3zaDxRZgtLO10Zxk";
    public static final String PARSE_CLIENT_KEY = "49AgCaNyWzaFFCgHFPgS3NK0lEjTpLNPDDYBrswX";

    public static String CURRENT_BUS_ID;

    public static ArrayList<Bus> activeBusLines;

    @Override
    public void onCreate() {
        super.onCreate();

        // Add your initialization code here
        Parse.initialize(this, PARSE_APP_ID, PARSE_CLIENT_KEY);


        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();

        // If you would like all objects to be private by default, remove this line.
        defaultACL.setPublicReadAccess(true);

        ParseACL.setDefaultACL(defaultACL, true);

        activeBusLines = new ArrayList<Bus>();
    }
}