package ie.wit.witselfiecompetition.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import ie.wit.witselfiecompetition.R;

import static android.content.Context.MODE_PRIVATE;


/**
 * This App Class to accelerate and ease the work
 * and compact it, also to avoid loads of duplicates
 * of code among different activities and classes
 * Created by Yahya Almardeny on 20/02/18.
 */

public class App {


    /**
     * This method set the appropriate layout
     * based one the orientation of the mobile phone (Landscape vs Portrait)
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
            emailEditText.setError(Html.fromHtml("<font color='white'>Email can't be empty!</font>"));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(Html.fromHtml("<font color='white'>Invalid Email!</font>"));
            return false;
        }

        /*if(!email.split("@")[1].equals("wit.ie")){Toast.makeText(emailEditText.getContext(), "Only WIT Students can use this app\n" +
        "If you are already a WIT student, please use your WIT email", Toast.LENGTH_SHORT).show();}*/
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

        String password = String.valueOf(passwordEditText.getText()).trim();
        if(password.isEmpty()){
            passwordEditText.setError(Html.fromHtml("<font color='white'>Password can't be empty</font>"));
            return false;
        }
        if (!(password.length()>=8) || !Pattern.compile("[0-9]").matcher(password).find() ||
                !Pattern.compile("[a-zA-z]").matcher(password).find()) {
            passwordEditText.setError(Html.fromHtml("<font color='white'>Password must be at least 8 characters long, " +
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
     * Generic method to display a popup dialog
     * @param activity
     * @param title
     * @param message
     */
    public static void showMessage(final Activity activity, String title, String message, final Callable<Void> after){
        AlertDialog.Builder dialog  = new AlertDialog.Builder(activity, R.style.alertDialog);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    after.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
     * Toggle visibility between two views
     * visible and invisible
     * @param view1
     * @param view2
     */
    public static void toggleVisibility(View view1, View view2){
        if(view1.getVisibility() == View.INVISIBLE){
            view1.setVisibility(View.VISIBLE);
            view2.setVisibility(View.INVISIBLE);
        }
        else {
            view1.setVisibility(View.INVISIBLE);
            view2.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Toggle Visibility between tow views
     * visible and gone
     * @param view1
     * @param view2
     */
    public static void toggleExistence(View view1, View view2){
        if(view1.getVisibility() == View.GONE){
            view1.setVisibility(View.VISIBLE);
            view2.setVisibility(View.GONE);
        }
        else {
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.VISIBLE);
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
     * Check if the current user exists and verified
     * @return
     */
    public static boolean isLoggedInVerifiedUser(Activity activity, boolean message){
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null) {
            if(auth.getCurrentUser().reload().isSuccessful()) {
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
        return false;
    }


    /**This method to check if the given name is valid
     * and probably a real one
     * @param activity
     * @param nameEditText
     * @param field
     * @return
     */
    public static boolean isValidName(Activity activity, EditText nameEditText, String field){
        String name = nameEditText.getText().toString().trim();
        if(name.isEmpty()){
            nameEditText.setError(field + " cannot be empty");
            return false;

        }
        for(char c : name.toCharArray()){
            if(!Character.isLetter(c) && !Character.isSpaceChar(c)){
                Toast.makeText(activity, "Please insert your real "+field,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }


    /**This method to check if the given name is valid
     * and probably a real one
     * @param activity
     * @param textView
     * @param field
     * @return
     */
    public static boolean isValidNameToast(Activity activity, TextView textView, String field){
        String name = String.valueOf(textView.getText()).trim();
        if(name.isEmpty()){
           Toast.makeText(activity, field + " cannot be empty", Toast.LENGTH_LONG).show();
            return false;
        }

        for(char c : name.toCharArray()){
            if(!Character.isLetter(c) && !Character.isSpaceChar(c)){
                Toast.makeText(activity, "Please insert your real "+field,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if(!name.contains(" ")){
            Toast.makeText(activity, "Please insert full name ",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }



    /**
     * This method to add to SharedPreferences file
     * @param activity
     * @param map
     */
    public static void addToSharedPreferences(Activity activity, Map<String,?> map) {
        SharedPreferences pref = getCurrentUserSharedPreferences(activity);
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



    /**
     * This method to add to SharedPreferences file
     * @param activity
     * @param k
     * @param v
     */
    public static void addToSharedPreferences(Activity activity, String k , Object v) {
        SharedPreferences pref = getCurrentUserSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();

        if(v instanceof String){
            editor.putString(k,(String)v).apply();
        }
        else if (v instanceof Integer){
            editor.putInt(k, (Integer)v).apply();
        }
        else if (v instanceof Float){
            editor.putFloat(k, (Float)v).apply();
        }
        else if (v instanceof Long){
            editor.putLong(k, (Long)v).apply();
        }
        else if (v instanceof Boolean){
            editor.putBoolean(k, (Boolean)v).apply();
        }
    }


    /**
     * Check and Ask for permission to write to phone storage
     * @param activity
     * @param PERMISSION_CODE
     */
    public static boolean grantPermission(Activity activity, final int PERMISSION_CODE){
       boolean hasPermission = (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CODE);
        }
        return hasPermission;
    }


    /**
     * Conditionally set the user image and full name after login
     * from the sharedPreferences (local version of data)
     * @param fullName
     * @param profileImage
     */
    public static void setPersonalImageAndName(Activity activity, TextView fullName, ImageView profileImage){
        SharedPreferences pref = getCurrentUserSharedPreferences(activity);

        fullName.setText(pref.getString("fName", "") + " " + pref.getString("lName", ""));
        String gender = pref.getString("gender", "");
        String encodedImage = pref.getString("image", "");
        if (encodedImage.isEmpty()) {
            switch (gender) {
                case "Male":
                    profileImage.setImageResource(R.drawable.male);
                    break;
                case "Female":
                    profileImage.setImageResource(R.drawable.female);
                    break;
            }
        } else {
            profileImage.setImageBitmap(decodeImage(encodedImage));
        }
    }



    /**
     * Add a given data to database
     * data in format of map
     * keys are fields names
     * values are the data of the fields
     * @param activity
     * @param node
     * @param data
     * @param errorMessage
     */
    public static void addToDatabase(final Activity activity, String node, Map<String,?> data, final String errorMessage){
        for (Map.Entry<String,?> entry :data.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            FirebaseDatabase.getInstance().getReference().child(node)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(field).setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        if(errorMessage!=null){
                            Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }


    /**
     * Add a single a value to a filed in
     * the database and call a method onSuccess
     * and onFailure
     * @param node
     * @param field
     * @param value
     * @param onSuccess
     * @param onFailure
     */
    public static void addToDatabase(String node, String field, String value,
                                     @Nullable final Callable<Void> onSuccess, @Nullable final Callable<Void> onFailure ){
        FirebaseDatabase.getInstance().getReference().child(node)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(field).setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    try{
                        onSuccess.call();
                    }catch (Exception e){}
                }
                else{
                    try{
                        onFailure.call();
                    }catch (Exception e){}
                }

            }
        });
    }


    /**
     * Add data from map to the database
     * and call a method onSuccess
     * and onFailure
     * @param node
     * @param data
     * @param onSuccess
     * @param onFailure
     */
    public static void addToDatabase(String node, Map<String, Object> data,
                                   @Nullable final Callable<Void> onSuccess, @Nullable final Callable<Void> onFailure ) {
        FirebaseDatabase.getInstance().getReference().child(node)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(data).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            try {
                                onSuccess.call();
                            } catch (Exception e) {
                                Log.e("database error", e.getMessage());
                            }
                        } else {
                            try {
                                onFailure.call();
                            } catch (Exception e) {
                                Log.e("database error", e.getMessage());
                            }
                        }
                    }
                });
    }


    /**
     * Encode a given uri of image to a Base64 string
     * let user specify approximate resulted JPEG image size in kilobytes
     * @param activity
     * @param imageUri
     * @param dstSize
     * @return
     */
   public static String encodeImage(final Activity activity, Uri imageUri, int dstSize){
        try{
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);

            float aspectRatio = (float)bitmap.getWidth() / (float)bitmap.getHeight();

            int BIT_DEPTH = JPEGBitDepth(activity, bitmap);

            float dstSizeInBits = dstSize * 8 * 1024;

            // sizeInBits = width * height * BIT_DEPTH (bit per pixel)
            // int width = (sizeInBits) / (height * BIT_DEPTH);
            // but height = width / aspectRatio -> substitute height
            // keep evaluating : width = squareRoot((sizeIinBits * aspectRatio) / BIT_DEPTH)

            int dstWidth = (int)Math.sqrt((dstSizeInBits*aspectRatio)/(BIT_DEPTH));
            int dstHeight = (int) ((float)dstWidth / aspectRatio);

            bitmap = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false); // to maintain aspect ratio

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            bitmap.recycle();

            return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);

        } catch (Exception e){
            Log.e("encode image", e.getMessage());
            return null;
        }
   }


    /**
     * Encode bitmap to 64Based String
     * and approximate destination image file size
     * @param activity
     * @param bitmap
     * @param dstSize
     * @return
     */
    public static String encodeImage(final Activity activity, Bitmap bitmap, int dstSize){
            float aspectRatio = (float)bitmap.getWidth() / (float)bitmap.getHeight();

            int BIT_DEPTH = JPEGBitDepth(activity, bitmap);

            float dstSizeInBits = dstSize * 8 * 1024;

            // sizeInBits = width * height * BIT_DEPTH (bit per pixel)
            // int width = (sizeInBits) / (height * BIT_DEPTH);
            // but height = width / aspectRatio -> substitute height
            // keep evaluating : width = squareRoot((sizeIinBits * aspectRatio) / BIT_DEPTH)

            int dstWidth = (int)Math.sqrt((dstSizeInBits*aspectRatio)/(BIT_DEPTH));
            int dstHeight = (int) ((float)dstWidth / aspectRatio);

            bitmap = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false); // to maintain aspect ratio

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            bitmap.recycle();

            return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
    }


    /**
     * Practically check the JPEG Bit Depth used in JPEG compression
     * and store the value in SharedPReferences
     * If it's already stored, just retrieve it
     * @param activity
     * @param bitmap
     * @return
     */
   private static int JPEGBitDepth(Activity activity, Bitmap bitmap){

       File temp = new File(activity.getCacheDir(), "temp.jpg");
       int width = bitmap.getWidth();
       int height = bitmap.getHeight();

       try {
           temp.createNewFile();
           OutputStream os = new BufferedOutputStream(new FileOutputStream(temp));
           bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
           os.close();
           double lenInBits = temp.length() * Byte.SIZE;

           temp.delete();
           int BIT_DEPTH  = (int)Math.round(lenInBits/(width*height));
           if(BIT_DEPTH==0) BIT_DEPTH++;

           return BIT_DEPTH;

           }catch(Exception e){
               Log.e("JPEGBitDepth", e.getMessage());
               return 0;
           }
   }



    /**
     * Decode a given encoded image (string) to bitmap
     * @param encodedImage
     * @return
     */
   public static Bitmap decodeImage(String encodedImage) {
       byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
       return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
   }



    /**
     * Change cover photo upon user's request
     * @param color
     * @param header
     */
   public static void changeHeaderImageTheme(String color, LinearLayout header, Toolbar toolBar){

       switch (color){
           case "Blue":
               header.setBackgroundResource(R.drawable.header_blue);
               toolBar.setBackgroundResource(R.color.background);
               break;
           case "Red":
               header.setBackgroundResource(R.drawable.header_red);
               toolBar.setBackgroundResource(R.color.colorRed);
               break;
           case "Purple":
               header.setBackgroundResource(R.drawable.header_purple);
               toolBar.setBackgroundResource(R.color.colorPurple);
               break;
           case "Green":
               header.setBackgroundResource(R.drawable.header_green);
               toolBar.setBackgroundResource(R.color.colorGreen);
               break;
           case "Orange":
               header.setBackgroundResource(R.drawable.header_orange);
               toolBar.setBackgroundResource(R.color.colorOrange);
               break;
           case "Grey":
               header.setBackgroundResource(R.drawable.header_grey);
               toolBar.setBackgroundResource(R.color.colorGrey);
               break;
           case "Yellow":
               header.setBackgroundResource(R.drawable.header_yellow);
               toolBar.setBackgroundResource(R.color.colorYellow);
               break;
           case "Cyan":
               header.setBackgroundResource(R.drawable.header_cyan);
               toolBar.setBackgroundResource(R.color.colorCyan);
               break;
       }
   }




    /**
     * Clear sharedPreferences on request
     * @param activity
     */
    public static void clearSharedPreferences(Activity activity){
        SharedPreferences pref = getCurrentUserSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear().apply();
    }


    /**
     * Copy the current user's info from database
     * to the SharedPreferences and execute a job
     * after finish
     * @param activity
     * @param postSuccessfulCopy
     */
    public static void copyUserInfoFromDatabaseToSharedPref(final Activity activity, final Callable<Void> postSuccessfulCopy){

        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            String encodedImage = user.getImage();
                            if (!encodedImage.isEmpty()) {
                                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                user.setImage(encodeImage(activity, bitmap, 50));

                            }
                            Map<String, String> profileInfo = getUserInfoInMap(user);
                            addToSharedPreferences(activity, profileInfo);
                            try {
                                postSuccessfulCopy.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }




    /**
     * Return the user information in a map
     * @return
     */
    public static Map<String,String> getUserInfoInMap(User user){
        Map<String, String> profileData = new HashMap<>();
        profileData.put("fName", user.getfName());
        profileData.put("lName", user.getlName());
        profileData.put("gender", user.getGender());
        profileData.put("course", user.getCourse());
        profileData.put("image", user.getImage());
        return profileData;
    }




    /**
     * Get the sharedPreferences of the current user
     * @param context
     * @return
     */
    public static SharedPreferences getCurrentUserSharedPreferences(Context context){
        return context.getApplicationContext().getSharedPreferences(
                FirebaseAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
    }




    /**
     * Show Progressbar on the top of the page
     * (i.e. in front of everything)
     * @param activity
     */
    public static Dialog onTopProgressBar (Activity activity){
        Dialog topDialog = new Dialog(activity, R.style.progressBarDialog);
        topDialog.setContentView(R.layout.progressbar_dialog);
        topDialog.setCanceledOnTouchOutside(false);
        topDialog.setCancelable(false);
        topDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        topDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return topDialog;
    }




    /**
     * Show Soft Keyboard
     * @param activity
     * @param view
     */
    public static void showSoftKeyboard(Activity activity, View view){
        ((InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }



    /**
     * Hide Soft Keyboard
     * @param activity
     * @param view
     */
    public static void hideSoftKeyboard(Activity activity, View view){
        ((InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    /**
     * Get Mobile Screen Width
     * @return
     */
    public static int getScreenWidth(){
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }


    /**
     * Get Mobile Screen Height
     * @return
     */
    public static int getScreenHeight(){
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }



    /**
     * Check is SharedPreferences for
     * current user exists and contains info
     * @param activity
     * @return
     */
    public static boolean sharedPreferencesDataExists(Activity activity) {
        SharedPreferences pref = getCurrentUserSharedPreferences(activity);
        return pref.getAll().size()!=0 && !pref.getString("fName", "").isEmpty() &&
                !pref.getString("lName", "").isEmpty() &&
                !pref.getString("gender", "").isEmpty()
                && !pref.getString("course", "").isEmpty();
    }


    /**
     * Return the color value saved in shared preferences
     * as integer
     * @param activity
     * @return
     */
    public static int getThemeColor(Activity activity){
        String color = getCurrentUserSharedPreferences(activity)
                .getString("color", "Blue");
        switch (color){
            case "Blue":
                return activity.getResources().getColor(R.color.background);
            case "Red":
                return activity.getResources().getColor(R.color.colorRed);
            case "Purple":
                return activity.getResources().getColor(R.color.colorPurple);
            case "Green":
                return activity.getResources().getColor(R.color.colorGreen);
            case "Orange":
                return activity.getResources().getColor(R.color.colorOrange);
            case "Grey":
                return activity.getResources().getColor(R.color.colorGrey);
            case "Yellow":
                return activity.getResources().getColor(R.color.colorYellow);
            case "Cyan":
                return activity.getResources().getColor(R.color.colorCyan);
            default:
                return activity.getResources().getColor(R.color.background);
        }
    }




    /**
     * compare the given date with now
     * return true of now is less than
     * given date (dd/MM/yyyy HH:mm)
     * @param closeDate
     * @return
     * @throws ParseException
     */
    public static boolean isAfterNow(String closeDate){
        String [] nowSArr = new SimpleDateFormat("dd MM yyyy HH mm", Locale.UK)
                            .format(new Date())
                            .split(" ");

        int [] now = new int[nowSArr.length];
        for(int i=0; i<nowSArr.length; i++) now[i] = Integer.parseInt(nowSArr[i]);

        String [] temp = closeDate.split("/");
        int [] close = new int[temp.length+2];
        close[0] = Integer.parseInt(temp[0]);
        close[1] = Integer.parseInt(temp[1]);
        close[2] = Integer.parseInt(temp[2].split(" ")[0]);
        close[3] = Integer.parseInt(temp[2].split(" ")[1].split(":")[0]);
        close[4] = Integer.parseInt(temp[2].split(" ")[1].split(":")[1]);

        // index 0 : day, 1 : month, 2 : year, 3 : hour, 4 : minutes
        return (now[2] < close[2]) || (now[2] <= close[2] && ((now[1] < close[1])
                || (now[1] <= close[1] && ((now[0] < close[0])
                || (now[0] <= close[0] && ((now[3] < close[3])
                || (now[3] <= close[3] && ((now[4] < close[4])
                || ((now[4] > close[4]) ? false : false)))))))));
    }


}
