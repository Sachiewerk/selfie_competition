package ie.wit.witselfiecompetition;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import ie.wit.witselfiecompetition.model.App;


/**
 * Login Activity to sign in to the account
 * Created by Yahya on 18/02/18.
 */
public class Login extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button signIn;
    ProgressBar signInProgressBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.setContentAccordingToOrientation(this);

        TextView forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        TextView createAccountTextView = findViewById(R.id.createAccountTextView);
        signIn = findViewById(R.id.signInButton);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInProgressBar = findViewById(R.id.signInProgressBar);

        //onClick handler of forgot password? TextView
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.redirect(Login.this, ForgotPassword.class, true);
            }
        });

        //onClick handler of Create an Account. TextView
        createAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.redirect(Login.this, Register.class, true);
            }
        });

        // onClick handler of the Sign In Button
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidLoginForm()) {
                    App.toggleVisibility(signIn, signInProgressBar);
                    String email = emailEditText.getText().toString().trim();
                    String password  = passwordEditText.getText().toString().trim();
                    // attempt to sign in
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) { // if logged in successfully
                                        // check if verified
                                        if(App.isLoggedInVerifiedUser(Login.this, true)){
                                            if(App.sharedPreferencesDataExists(Login.this)){
                                                App.redirect(Login.this, Main.class, false);
                                            }
                                            else{
                                                App.redirect(Login.this, ProfileSetup.class, false);
                                            }
                                        }
                                    } else {
                                        Toast.makeText(Login.this,
                                                "Could not sign in\n Incorrect Email or/and Password", Toast.LENGTH_LONG).show();
                                    }
                                    App.toggleVisibility(signIn, signInProgressBar);
                                }
                            });
                }
            }
        });

    }


    /**
     * This method is invoked upon
     * rotating the mobile phone
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        App.setContentAccordingToOrientation(this);
    }


    /**
     * check if it's valid email and non empty password
     * check connection to internet
     * point out to the error to inform user
     */
    private boolean isValidLoginForm(){
        if(!App.hasNetworkConnection(Login.this)) {
            App.showMessage(Login.this, "Error", "No Internet Connection!", false);
            return false;
        }
        boolean flag = true;
        if(!App.isValidEmail(emailEditText)) {
            flag = false;
        }
        if(String.valueOf(passwordEditText.getText()).trim().isEmpty()){
            flag = false;
            Toast.makeText(getApplicationContext(), "Password cannot be empty!", Toast.LENGTH_SHORT).show();
        }
        return flag;
    }


}
