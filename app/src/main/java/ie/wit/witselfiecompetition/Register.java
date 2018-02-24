package ie.wit.witselfiecompetition;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * Created by yahya on 17/02/18.
 * Register User for first time with
 * WIT Selfie Competition
 */
public class Register extends AppCompatActivity {
    ProgressBar registerProgressBar;
    Button register;
    EditText emailEditText, pass1EditText, pass2EditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Helper.setContentAccordingToOrientation(Register.this);

        registerProgressBar = findViewById(R.id.registerProgressBar);
        register = findViewById(R.id.registerButton);
        emailEditText = findViewById(R.id.registerEmailEditText);
        pass1EditText = findViewById(R.id.registerPassword1EditText);
        pass2EditText = findViewById(R.id.registerPassword2EditText);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String pass1 = pass1EditText.getText().toString().trim();
                String pass2 = pass2EditText.getText().toString().trim();
                if(isValidRegistration(pass1, pass2)){
                    registerNewEmail(email, pass1);
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
        Helper.setContentAccordingToOrientation(this);
    }


    /**
     * Register a new email and password to FireBase Authentication
     * @param email
     * @param password
     */
    public void registerNewEmail(final String email, String password){
        Helper.toggleProgressBar(register, registerProgressBar);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.trim(), password.trim())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            sendVerificationEmail();
                            FirebaseAuth.getInstance().signOut();
                        }
                        else if (!task.isSuccessful()) {
                            Toast.makeText(Register.this, "Unable to Register, ", Toast.LENGTH_SHORT).show();
                        }
                        Helper.toggleProgressBar(register, registerProgressBar);
                    }
                });
    }


    /**
     * send verification link to the user's email
     */
    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Helper.redirectWithMessage(Register.this, Login.class, "",
                                    "A verification link has been sent to your email", true);
                }
                else {
                    Helper.showMessage(Register.this, "Error",
                                    "Couldn't send verification link", false);
                }
            }
        });
    }


    /**
     * This method to validate the registration details in total
     * @param pass1
     * @param pass2
     * @return
     */
    private boolean isValidRegistration(String pass1, String pass2){
        if(!Helper.hasNetworkConnection(Register.this)) {
            Helper.showMessage(Register.this, "Error", "No Internet Connection!", false);
            return false;
        }
        boolean flag = true;
        if(!pass1.equals(pass2)){
            Toast.makeText(Register.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            flag = false;
        }
        if(!Helper.isValidEmail(emailEditText)) {
            flag = false;
        }
        if(!Helper.isValidPassword(pass1EditText)) {
            flag = false;
        }

        return flag;
    }



}
