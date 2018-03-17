package ie.wit.witselfiecompetition;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.Callable;

import ie.wit.witselfiecompetition.model.Helper;


/**
 * Splash Screen Class
 * Logo for the app
 * Created by Yahya Almardeny on 08/02/18.
 */
public class SplashScreen extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // check the phone orientation and set appropriate layout accordingly
        Helper.setContentAccordingToOrientation(this);


        // run asynchronously while displaying splash screen
        final Thread checkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(Helper.hasNetworkConnection(SplashScreen.this)) {
                    if(Helper.isLoggedInVerifiedUser(SplashScreen.this, false)) {
                        Helper.copyUserInfoFromDatabaseToSharedPref(SplashScreen.this, new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                if(Helper.sharedPreferencesDataExists(SplashScreen.this)){
                                    Helper.redirect(SplashScreen.this, Main.class, false);
                                }
                                else{
                                    Helper.redirect(SplashScreen.this, ProfileSetup.class, false);
                                }
                                return null;
                            }
                        });
                    }
                    else{
                        Helper.redirect(SplashScreen.this, Login.class, false);
                    }
                }
                else {
                    Helper.showMessage(SplashScreen.this, "No Internet Connection!",
                            "Please connect to Network and retry again", true);
                }
            }
        });

        // post event handler to move from current splash screen
        // activity to the proper next activity
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run () {
                checkThread.start();
            }
        }, 2000);
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