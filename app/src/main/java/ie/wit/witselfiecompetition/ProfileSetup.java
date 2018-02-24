package ie.wit.witselfiecompetition;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Created by yahya on 18/02/18.
 */

public class ProfileSetup extends AppCompatActivity {

    EditText firstNameEditText, lastNameEditText;
    RadioGroup genderRadioGroup;
    RadioButton maleRadioButton, femaleRadioButton;
    Button joinButton;
    User.Gender gender;
    ProgressBar profileSetupProgressBar;


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_setup_dialog);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);;
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        maleRadioButton = findViewById(R.id.maleRadioButton);
        femaleRadioButton = findViewById(R.id.femaleRadioButton);
        joinButton = findViewById(R.id.joinButton);
        profileSetupProgressBar = findViewById(R.id.profileSetupProgressBar);

        joinButton .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidInput()){
                    Helper.toggleProgressBar(joinButton, profileSetupProgressBar);
                    String fName = firstNameEditText.getText().toString();
                    String lName = lastNameEditText.getText().toString();
                    User user = new User(fName, lName, gender, null, "", "" );
                    addNewUser(user);
                }
            }
        });



    }

    @Override
    public void onBackPressed(){/* To disable Back button*/}

    private User.Gender gender(){
        if (genderRadioGroup.getCheckedRadioButtonId() == -1) {
            // no gender is checked
            Toast.makeText(ProfileSetup.this,
                    "Please choose your gender",Toast.LENGTH_SHORT).show();
        }
        else {
            if(maleRadioButton.isChecked()){
                return User.Gender.MALE;
            }
            else if(femaleRadioButton.isChecked()){
                return User.Gender.FEMALE;
            }
        }
        return null;
    }

    private boolean isValidInput(){
        gender = gender();
        if(gender!=null){
            if(Helper.isValidName(ProfileSetup.this, firstNameEditText, "first name")){
                if(Helper.isValidName(ProfileSetup.this, lastNameEditText, "last name")){
                    return true;
                }
            }
        }
        return false;
    }

    private void addNewUser(final User user) {

        FirebaseDatabase.getInstance().getReference().getRoot()
                .child("Users").child(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).push().setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            HashMap<String, String> hmap = new HashMap<String, String>();
                            hmap.put(user.getfName(), user.getlName());
                            Helper.redirect(ProfileSetup.this, Main.class, false);
                        }

                        else{
                            Log.v("tag", task.getException().toString());
                            Toast.makeText(ProfileSetup.this, "Failed to add user", Toast.LENGTH_SHORT).show();
                            Helper.toggleProgressBar(joinButton, profileSetupProgressBar);
                        }
                    }
                });
    }

}
