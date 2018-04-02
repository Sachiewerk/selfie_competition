package ie.wit.witselfiecompetition;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import ie.wit.witselfiecompetition.model.App;


/**
 * Fragment for Settings.
 * Change the App Theme.
 * Change and Update The Password.
 * Close and Delete the User Account.
 *
 * Create by Yahya Almardeny on 01/04/2018
 */
public class SettingsFragment extends Fragment {

    TextView about;
    Button changePassword, closeAccount;
    RelativeLayout changeHeader;
    AppCompatSpinner headerColors;
    String[] colors;
    LinearLayout header;
    Toolbar toolBar;
    GradientDrawable [] drawable;
    Dialog fullScreenProgressBar;
    String USER_ID;

    public SettingsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        USER_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fullScreenProgressBar = App.onTopProgressBar(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, null);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Settings");
        loadView();
        fillWithInfo();
        addViewListeners();


    }



    /**
     * Load Views into Fragment
     */
    private void loadView(){
        changeHeader = getView().findViewById(R.id.changeHeader);
        changePassword = getView().findViewById(R.id.changePassword);
        closeAccount  = getView().findViewById(R.id.closeAccount);
        about  = getView().findViewById(R.id.about);
        headerColors =  getView().findViewById(R.id.headerColors);
        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        header = navigationView.getHeaderView(0).findViewById(R.id.header);
        toolBar = getActivity().findViewById(R.id.toolbar);
        drawable = new GradientDrawable[]{new GradientDrawable(), new GradientDrawable(),new GradientDrawable()};
    }



    /**
     * Fill Views With Required Info
     */
    private void fillWithInfo(){
        colors = new String[]{"", "Blue", "Purple", "Green", "Cyan", "Orange", "Red", "Grey", "Yellow"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, colors){
            @Override
            public boolean isEnabled(int position){return position != 0;}
        };

        headerColors.setAdapter(adapter);
        // clear initial selection
        int initialSelectedPosition = headerColors.getSelectedItemPosition();
        headerColors.setSelection(initialSelectedPosition, false);
        int color = App.getThemeColor(getActivity());
        for(GradientDrawable draw : drawable){
            draw.setCornerRadius(25f);
            draw.setColor(color);
        }
        changeHeader.setBackground(drawable[0]);
        changePassword.setBackground(drawable[1]);
        closeAccount.setBackground(drawable[2]);
    }


    /**
     * Add Listeners to the Views
     */
    private void addViewListeners(){
        headerColors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String color = colors[i];
                if(!color.isEmpty()) {
                    App.addToSharedPreferences(getActivity(), "color", colors[i]);
                    App.changeHeaderImageTheme(colors[i], header, toolBar);
                    int colorValue = App.getThemeColor(getActivity());
                    for (GradientDrawable draw : drawable) {
                        draw.setColor(colorValue);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        changeHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                headerColors.performClick();
            }
        });
        closeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeAccountAlertDialog();
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePasswordDialog();
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog aboutDialog = new Dialog(getActivity());
                aboutDialog.setContentView(R.layout.about);
                aboutDialog.show();
            }
        });

    }

    ///////////////////////////////////////////////////////////////////////////
    /******The Correct Way to close a user account and remove all related data
     *  is by doing it on the server side not on the user side, but this just for now
     * as there is no server administration***********/
    //////////////////////////////////////////////////////////////////////////

    /**
     * Customized Alert Dialog to close
     * user's account after asking for and validating
     * user's email and password
     */
    private void closeAccountAlertDialog(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.delete_account_dialog, null);
        builder.setView(view);

        ConstraintLayout deleteAccountDialog = view.findViewById(R.id.deleteAccountDialog);
        deleteAccountDialog.setBackgroundColor(App.getThemeColor(getActivity()));

        final EditText emailET = view.findViewById(R.id.deleteAccountEmail);
        final EditText passwordET = view.findViewById(R.id.deleteAccountPassword);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });

        final AlertDialog dialog = builder.create();
        dialog.show();


        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                if (!email.isEmpty() && !password.isEmpty()) {
                    fullScreenProgressBar.show();
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        removeUserFromUsers();

                                    } else {
                                        fullScreenProgressBar.dismiss();
                                        Toast.makeText(getActivity(), "Incorrect Email or/and Password!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }else{
                    Toast.makeText(getActivity(), "Email and Password cannot be empty!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    /**
     * Change Password Customized Dialog
     * To Update Password after validating and asking
     * for the current one.
     */
    private void changePasswordDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.change_password_dialog, null);
        builder.setView(view);

        ConstraintLayout changePasswordDialog = view.findViewById(R.id.changePasswordDialog);
        changePasswordDialog.setBackgroundColor(App.getThemeColor(getActivity()));

        final EditText currentPassword = view.findViewById(R.id.currentPassword);
        final EditText newPassword = view.findViewById(R.id.newPassword);
        final EditText repeatPassword = view.findViewById(R.id.repeatPassword);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(App.isValidPassword(newPassword)){
                    String currentPass = currentPassword.getText().toString();
                    final String newPass = newPassword.getText().toString();
                    String repeatPass = repeatPassword.getText().toString();
                    if(newPass.equals(repeatPass)) {
                        fullScreenProgressBar.show();
                        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, currentPass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // change password
                                            FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPass)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getActivity(), "Your password has been updated successfully!", Toast.LENGTH_LONG).show();
                                                                FirebaseAuth.getInstance().signOut();
                                                                fullScreenProgressBar.dismiss();
                                                                dialog.dismiss();
                                                                App.redirect(getActivity(), Login.class, false);
                                                            } else {
                                                                fullScreenProgressBar.dismiss();
                                                                Toast.makeText(getActivity(), "Error while trying update password!", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });


                                        } else {
                                            fullScreenProgressBar.dismiss();
                                            Toast.makeText(getActivity(), "Error while trying update password!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }else{
                        Toast.makeText(getActivity(), "Passwords do not match!", Toast.LENGTH_LONG).show();

                    }
            }
        }});
    }


    /**
     * Remove user first from the Users collection
     * On successful removal, remove it from Selfie Collection
     */
    private void removeUserFromUsers(){
        FirebaseDatabase.getInstance().getReference()
                .child("Users").child(USER_ID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            removeUserFromSelfies();
                        }
                    }
                });
    }


    /**
     * Remove user secondly from the Selfie collection
     * On successful removal, remove it from Winner collection
     */
    private void removeUserFromSelfies(){
        final DatabaseReference root = FirebaseDatabase.getInstance().getReference()
                .child("Selfie");
        root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                if(count>0) {
                    for (DataSnapshot competitionId : dataSnapshot.getChildren()) {
                        if (competitionId.hasChild(USER_ID)) {
                            root.child(competitionId.getKey()).child(USER_ID).removeValue();
                        }
                        count--;
                    }
                    if(count==0){ removeUserFromWinners(); }
                }
                else { removeUserFromWinners(); }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }



    /**
     * Thirdly, Remove user from the Winner collection
     * if he/she won once any of the competitions
     */
    private void removeUserFromWinners(){
        final DatabaseReference root = FirebaseDatabase.getInstance().getReference()
                .child("Winner");
        root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                if(count>0) {
                    for (DataSnapshot competitionId : dataSnapshot.getChildren()) {
                        List<String> winners = (List<String>) competitionId.getValue();
                        if (winners!=null && winners.contains(USER_ID)) {
                            //start transaction
                            removeUserFromWinnersTransaction(root.child(competitionId.getKey()));
                        }
                        count--;
                    }
                    if(count==0){removeUserFromCompetitions();}
                }
                else { removeUserFromCompetitions(); }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }



    /**
     * Database Transaction to remove user id from the
     * winners' list in a given competition id
     * @param dbr
     */
    private void removeUserFromWinnersTransaction(final DatabaseReference dbr) {

        dbr.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() != null) {
                    List<String> temp = (List<String>) currentData.getValue();
                    temp.remove(USER_ID);
                    currentData.setValue(temp);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot currentData) {
                List<String> temp = (List<String>) currentData.getValue();
                if (temp != null) {
                    if (temp.contains(USER_ID)) {
                        removeUserFromWinnersTransaction(dbr);
                    }
                }
            }
        });
    }


    /**
     * Fourthly, Remove user from all competitions' SelfiesId children
     * On successful, remove user from Authentication
     */
    private void removeUserFromCompetitions(){
        final DatabaseReference root = FirebaseDatabase.getInstance().getReference()
                .child("Competition");
        root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                if(count>0){
                    for(DataSnapshot competition : dataSnapshot.getChildren()){
                        List<String> selfiesId = (List<String>) competition.child("selfiesId").getValue();
                        if(selfiesId!=null && selfiesId.contains(USER_ID)){
                            //start transaction
                            removeUserFromCompetitionTransaction(root.child(competition.getKey()).child("selfiesId"));
                        }
                        count--;
                    }
                    if(count==0){removeUserFromAuthentication();}
                }
                else{removeUserFromAuthentication(); }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                removeUserFromCompetitions();
            }
        });
    }




    /**
     * Database Transaction to remove the user from the selfies Ids list
     * in every competition
     * @param dbr
     */
    private void removeUserFromCompetitionTransaction(final DatabaseReference dbr) {

        dbr.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() != null) {
                    List<String> temp = (List<String>) currentData.getValue();
                    temp.remove(USER_ID);
                    currentData.setValue(temp);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot currentData) {
                List<String> temp = (List<String>) currentData.getValue();
                if (temp != null) {
                    if (temp.contains(USER_ID)) {
                        removeUserFromCompetitionTransaction(dbr);
                    }
                }
            }
        });
    }



    /**
     * Final step, remove user from the authentication
     * After this step, user will have no existence at all
     * except their likes will be kept
     */
    private void removeUserFromAuthentication(){
        App.clearSharedPreferences(getActivity());
        FirebaseAuth.getInstance().getCurrentUser().delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getActivity(),
                                    "Your account has been closed successfully !",
                                    Toast.LENGTH_LONG).show();
                            App.redirect(getActivity(), Login.class, false);

                        }else{
                            Toast.makeText(getActivity(),
                                    "Error while trying to close account !",
                                    Toast.LENGTH_LONG).show();
                            fullScreenProgressBar.dismiss();
                        }
                    }
                });
    }


}

