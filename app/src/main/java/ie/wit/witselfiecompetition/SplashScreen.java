package ie.wit.witselfiecompetition;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * Splash Screen Class
 * Logo for the app
 * Created by Yahya on 08/02/18.
 */
public class SplashScreen extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check the phone orientation and set layout accordingly
        Helper.setContentAccordingToOrientation(this);
        // post event handler to move from current splash screen
        // activity to the next activity
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run (){
                // check if user already logged in and verified
                if (Helper.isVerifiedUser(SplashScreen.this, false)){
                    if(Helper.isFirstLogin(SplashScreen.this)){
                        Helper.redirect(SplashScreen.this, ProfileSetup.class, false); // go tp profilesetup activity
                    }
                    else {
                        Helper.redirect(SplashScreen.this, Main.class, false);// go to main activity
                    }
                }
                else{
                    Helper.redirect(SplashScreen.this, Login.class, false); // go to login activity
                }
            }

        }, 1000);
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