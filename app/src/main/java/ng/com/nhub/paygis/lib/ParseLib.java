package ng.com.nhub.paygis.lib;

import android.util.Log;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.HashMap;
import java.util.Map;

import ng.com.nhub.paygis.etc.AppData;
import ng.com.nhub.paygis.etc.User;


public class ParseLib {
    private static final String TAG = "ParseLib";

    static public void parseSignUpLogin(final User currentUser){
        ParseUser user = new ParseUser();
        user.setUsername(currentUser.userId);
        user.setPassword(currentUser.userId);
        user.setEmail(currentUser.username + "@logicaladdress.com");
        user.put("handle", "Anonymous");
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                    Log.i(TAG, "New Registration Success");
                    Map<String, String> parseState = new HashMap<>();
                    parseState.put("register", "true");
                    AppData.saveParseUserState(parseState);
                    loginUser(currentUser);
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    if (e.getCode() == ParseException.USERNAME_TAKEN ||
                            e.getCode() == ParseException.EMAIL_TAKEN){
                        Log.i(TAG, "User Exists, Proceed to Login");
                        loginUser(currentUser);
                    }else{
                        Log.d(TAG, "ParseLogin: An Error Occured");
                    }
                }
            }
        });
    }

    static private void loginUser(User currentUser){
        ParseUser.logInInBackground(currentUser.userId, currentUser.userId, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null && user != null) {
                    // Hooray! The user is logged in.
                    Map<String, String> parseState = new HashMap<>();
                    parseState.put("authenticated", "true");
                    AppData.saveParseUserState(parseState);
                    Log.i(TAG, "User Login Passed");
                } else {
                    // Signin failed. Look at the ParseException to see what happened.
                    Log.d(TAG, "User Login Failed");
                }
            }
        });
    }
}