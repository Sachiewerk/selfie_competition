package ie.wit.witselfiecompetition;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ie.wit.witselfiecompetition.model.Helper;

import static java.lang.Thread.sleep;

/**
 * Splash Screen Class
 * Logo for the app
 * Created by Yahya Almardeny on 08/02/18.
 */
public class SplashScreen extends AppCompatActivity {

    boolean login=false, main= false, profileSetup = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check the phone orientation and set layout accordingly
        Helper.setContentAccordingToOrientation(this);

        // run asynchronously while displaying splash screen
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(Helper.hasNetworkConnection(SplashScreen.this)) {
                    if(Helper.isLoggedInVerifiedUser(SplashScreen.this, false)) {

                        Helper.copyUserInfoFromDatabaseToSharedPref(SplashScreen.this);
                        try {
                            sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(Helper.sharedPreferencesExists(SplashScreen.this)){
                            main = true;
                        }
                        else{profileSetup = true;}
                    }
                    else{login = true;}
                }
            }
        }).start();

        // post event handler to move from current splash screen
        // activity to the proper next activity
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run () {
                if (main) {
                    Helper.redirect(SplashScreen.this, Main.class, false);
                } else if (profileSetup) {
                    Helper.redirect(SplashScreen.this, ProfileSetup.class, false);
                } else if (login) {
                    Helper.redirect(SplashScreen.this, Login.class, false);
                } else {
                    Helper.showMessage(SplashScreen.this, "No Internet Connection!", "Please connect to Network and retry again", true);
                }
            }
        }, 5000);
    }


    /**
     * This method is invoked upon
     * rotating the mobile phone
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Helper.setContentAccordingToOrientation(this);
    }


}