package ie.wit.witselfiecompetition;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import ie.wit.witselfiecompetition.model.Helper;

/**
 * This class to reset the forgotten password on user request
 * Created on 17/02/18.
 * @author Yahya Almardeny
 */

public class ForgotPassword extends Activity {

    EditText forgotPasswordEmailEditText;
    ProgressBar forgotPasswordProgressBar;
    Button sendPasswordButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_dialog);

        sendPasswordButton = findViewById(R.id.sendPasswordButton);
        forgotPasswordEmailEditText = findViewById(R.id.forgotPasswordEmailEditText);
        forgotPasswordProgressBar = findViewById(R.id.forgotPasswordProgressBar);

        sendPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = forgotPasswordEmailEditText.getText().toString().trim();
                if(Helper.hasNetworkConnection(ForgotPassword.this)) {
                     if(Helper.isValidEmail(forgotPasswordEmailEditText)){
                        resetPassword(email);
                     }
                }
                else{
                    Helper.hideSoftKeyboard(ForgotPassword.this, view);
                    Helper.showMessage(ForgotPassword.this, "Error",
                            "No Internet Connection!", false);
                }
            }
        });

    }


    /**
     * This method to reset the password upon user request
     * @param email
     */
    private void resetPassword(String email){
        Helper.toggleVisibility(sendPasswordButton, forgotPasswordProgressBar);
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Helper.hideSoftKeyboard(ForgotPassword.this);
                            Helper.showMessage(ForgotPassword.this, "",
                                    "Reset password link has been sent to your email", true);
                        }
                        else {
                            Helper.hideSoftKeyboard(ForgotPassword.this, new View(ForgotPassword.this));
                            Helper.showMessage(ForgotPassword.this, "",
                                    "Unable to send reset password link", true);
                        }
                        Helper.toggleVisibility(sendPasswordButton, forgotPasswordProgressBar);
                    }
                });
    }

}
