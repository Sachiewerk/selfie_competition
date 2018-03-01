package ie.wit.witselfiecompetition;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Map;
import java.util.regex.Pattern;
import static android.content.Context.MODE_PRIVATE;


/**
 * This Helper Class to accelerate and ease the work
 * and compact it, also to avoid loads of duplicates
 * Created by yahya on 20/02/18.
 */

public class Helper {


    /**
     * This method set the appropriate layout
     * based one the orientation of the mobile phone (Lnadscape vs Portrait)
     * Also this method is smart to link the appropriate layout to its activity
     */
    public static void setContentAccordingToOrientation(Activity activity){

        String portraitFieldName = "activity_".concat(activity.getClass().getSimpleName().toLowerCase()).concat("_portrait");
        String landscapeFieldName  = "activity_".concat(activity.getClass().getSimpleName().toLowerCase()).concat("_landscape");
        try{
            int portrait = R.layout.class.getField(portraitFieldName).getInt(null);
            int landscape = R.layout.class.getField(landscapeFieldName).getInt(null);
            if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                activity.setContentView(portrait);
            }
            else{
                activity.setContentView(landscape);
            }
        }catch (Exception e){
            Log.e("set content", e.getMessage());
        }
    }


    /**
     * This method checks if there is internet connection
     * @param context
     * @return
     */
    public static boolean hasNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }


    /**
     * This method to validate the email input
     * EMAIL_ADDRESS pattern is built in Patterns Class starting with API Level 8
     * @param emailEditText
     * @return
     */
    public static boolean isValidEmail (EditText emailEditText){
        String email = emailEditText.getText().toString().trim();
        if(email.isEmpty()){
            emailEditText.setError(Html.fromHtml("<font color='red'>Email can't be empty!</font>"));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(Html.fromHtml("<font color='red'>Invalid Email!</font>"));
            return false;
        }
        return true;
    }


    /**
     * This method to validate the password input
     * Password can't be empty and must be at least 8 characters long
     * must contain at least one digit and alphabet character
     * @param passwordEditText
     * @return
     */
    public static boolean isValidPassword (EditText passwordEditText){
        String password = passwordEditText.getText().toString().trim();
        if(password.isEmpty()){
            passwordEditText.setError(Html.fromHtml("<font color='red'>Password can't be empty</font>"));
            return false;
        }
        if (!(password.length()>=8) || !Pattern.compile("[0-9]").matcher(password).find() ||
                !Pattern.compile("[a-zA-z]").matcher(password).find()) {
            passwordEditText.setError(Html.fromHtml("<font color='red'>Password must be at least 8 characters long, " +
                    "contain at least one digit and alphabet character</font>"));
            return false;
        }
        return true;
    }


    /**
     * Generic method to display a popup dialog
     * @param activity
     * @param title
     * @param message
     */
    public static void showMessage(final Activity activity, String title, String message, final boolean finishParent){
        AlertDialog.Builder dialog  = new AlertDialog.Builder(activity, R.style.alertDialog);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(finishParent) {activity.finish();}
            }
        });
        dialog.create().show();
    }


    /**
     * This method to move to another activity after showing a message
     * @param activity
     * @param targetClass
     * @param title
     * @param message
     * @param backable
     */
    public static void redirectWithMessage(final Activity activity, final Class targetClass, String title, String message, final boolean backable){
        AlertDialog.Builder dialog  = new AlertDialog.Builder(activity, R.style.alertDialog);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                redirect(activity, targetClass, backable);
            }
        });
        dialog.create().show();
    }


    /**
     * Toggle visibility between given button and progressbar
     * @param button
     * @param progressBar
     */
    public static void toggleProgressBar(Button button, ProgressBar progressBar){
        if(button.getVisibility() == View.INVISIBLE){
            button.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
        else {
            button.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }


    /**
     * This method to move conditionally between activities
     * @param activity
     * @param targetClass
     * @param backable
     */
    public static void redirect(Activity activity, Class targetClass, boolean backable){
        Intent intent = new Intent(activity.getApplicationContext(), targetClass);
        if(!backable){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            activity.finish();
            activity.startActivity(intent);
            return;
        }
        activity.startActivity(intent);
    }


    /**
     * This method to hide the soft keyboard on request
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity){
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(new View(activity).getWindowToken(), 0);
    }


    /**
     * Check if the current user exists and verified
     * @return
     */
    public static boolean isVerifiedUser(Activity activity, boolean message){
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                if (message) {
                    Toast.makeText(activity, "This account is not Verified\nCheck your Inbox", Toast.LENGTH_LONG).show();
                }
                FirebaseAuth.getInstance().signOut();
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * This method toggles visibility of a given view
     * @param view
     */
    public static void toggleVisibility( View view){
        Log.v("Yahya", "Here");
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getAlpha() == 0.9f) {
                    v.setAlpha(0.0f);
                }
                else{
                    v.setAlpha(0.9f);
                }
            }
        });
    }

    /**This method to check if the given name is valid
     * and probably a real one
     * @param activity
     * @param nameEditText
     * @param field
     * @return
     */
    public static boolean isValidName(Activity activity, EditText nameEditText, String field){
        String name = nameEditText.getText().toString();
        if(name.isEmpty()){
            nameEditText.setError(field + " cannot be empty");
            return false;

        }
        for(char c : name.toCharArray()){
            if(!Character.isLetter(c)){
                Toast.makeText(activity, "Please insert your real "+field,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }


    /**
     * This method checks the SharedPreferences file
     * looks for firs name and last name (key, value) pairs
     * if they don't exist, that means it's the first login ever
     * @param activity
     * @return
     */
    public static boolean isFirstLogin(Activity activity){
        SharedPreferences pref = activity.getApplicationContext().getSharedPreferences("ie.wit.witselfiecompetition", MODE_PRIVATE);
        String firstName =pref.getString("fName", null);
        String lastName =pref.getString("lName", null);
        if(firstName==null || lastName==null){
            return true;
        }
        return false;
    }

    /**
     * This method to add to SharedPreferences file
     * @param activity
     * @param map
     */
    public static void addToSharedPreferences(Activity activity, Map<String,?> map) {
        SharedPreferences pref = activity.getApplicationContext().getSharedPreferences("ie.wit.witselfiecompetition", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        for (Map.Entry<String,?> entry : map.entrySet()) {
            Object v = entry.getValue();
            String k = entry.getKey();

            if(v instanceof String){
                editor.putString(k,(String)v).commit();
            }
            else if (v instanceof Integer){
                editor.putInt(k, (Integer)v).commit();
            }
            else if (v instanceof Float){
                editor.putFloat(k, (Float)v).commit();
            }
            else if (v instanceof Long){
                editor.putLong(k, (Long)v).commit();
            }
            else if (v instanceof Boolean){
                editor.putBoolean(k, (Boolean)v).commit();
            }
        }

    }

}
