package ie.wit.witselfiecompetition;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ie.wit.witselfiecompetition.model.User;

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
                if(Helper.hasNetworkConnection(SplashScreen.this)) {
                    // check if user already logged in and verified
                    if (Helper.isLoggedInVerifiedUser(SplashScreen.this, false)) {
                        Helper.firstLoginCheck(SplashScreen.this, Main.class, Login.class);
                    } else { // go to login activity
                        Helper.redirect(SplashScreen.this, Login.class, false);
                    }
                }
                else{
                    Helper.showMessage(SplashScreen.this, "No Internet Connection!", "Please connect to Network then retry again", true);
                }
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