package ie.wit.witselfiecompetition;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

import ie.wit.witselfiecompetition.model.Course;
import ie.wit.witselfiecompetition.model.Helper;
import ie.wit.witselfiecompetition.model.User;

/**
 * This Class to setup the user profile after the
 * very first login
 * @author Yahya Almardeny
 * @version 01/03/2018
 */

public class ProfileSetup extends AppCompatActivity {

    EditText firstNameEditText, lastNameEditText;
    RadioGroup genderRadioGroup;
    RadioButton maleRadioButton, femaleRadioButton;
    Button joinButton;
    String gender;
    ProgressBar profileSetupProgressBar;
    Spinner coursesMenu;
    TextView signoutTextView;
    String course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Helper.setContentAccordingToOrientation(ProfileSetup.this);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);;
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        maleRadioButton = findViewById(R.id.maleRadioButton);
        femaleRadioButton = findViewById(R.id.femaleRadioButton);
        joinButton = findViewById(R.id.joinButton);
        profileSetupProgressBar = findViewById(R.id.profileSetupProgressBar);
        coursesMenu = findViewById(R.id.coursesMenu);
        signoutTextView = findViewById(R.id.signoutTextView);
        final String[] courses = Course.courses();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, courses);
        coursesMenu.setAdapter(adapter);

        coursesMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                course = courses[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        joinButton .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidInput()){
                    Helper.toggleVisibility(joinButton, profileSetupProgressBar);
                    String fName = firstNameEditText.getText().toString().trim();
                    String lName = lastNameEditText.getText().toString().trim();
                    User user = new User(fName, lName, gender, course, "", "" );
                    addNewUser(user);
                }
            }
        });

        signoutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Helper.redirect(ProfileSetup.this, Login.class, false);
            }
        });



    }


    @Override
    public void onBackPressed(){/* To disable Back button*/}



    /**
     * Validate and return the chosen gender
     * @return
     */
    private String gender(){
        if (genderRadioGroup.getCheckedRadioButtonId() == -1) {
            // no gender is checked
            Toast.makeText(ProfileSetup.this,
                    "Please choose your gender",Toast.LENGTH_SHORT).show();
        }
        else {
            if(maleRadioButton.isChecked()){
                return "Male";
            }
            else if(femaleRadioButton.isChecked()){
                return "Female";
            }
        }
        return null;
    }



    /**
     * validate the input form before proceeding
     * @return
     */
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



    /**
     * This private method to add new user to
     * FireBase database and sharedPreferences
     * @param user
     */
    private void addNewUser(final User user) {

        FirebaseDatabase.getInstance().getReference().getRoot()
                .child("Users").child(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Map<String, String> profileData = Helper.getUserInfoInMap(user);
                            Helper.addToSharedPreferences(ProfileSetup.this, profileData);
                            Helper.redirect(ProfileSetup.this, Main.class, false);
                        }

                        else{
                            Toast.makeText(ProfileSetup.this, "Failed to add user", Toast.LENGTH_SHORT).show();
                            Helper.toggleVisibility(joinButton, profileSetupProgressBar);
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


}
