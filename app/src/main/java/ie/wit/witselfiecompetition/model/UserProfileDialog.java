package ie.wit.witselfiecompetition.model;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import ie.wit.witselfiecompetition.R;

/**
 * Created by yahya Almardeny on 30/03/18.
 */

public class UserProfileDialog extends Dialog{

    private ImageView popupProfilePic;
    private Dialog imageDialog;
    private User user;
    private Bitmap bitmap;



    public UserProfileDialog(@NonNull Context context, User user, int themeResId) {
        super(context, themeResId);
        setContentView(R.layout.dialog_user_profile);
        this.user = user;
        Runtime.getRuntime().gc();
        System.gc();
        setCanceledOnTouchOutside(false);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        createProfile();

    }



    private void createProfile() {

        // load view
        ImageView profilePic = findViewById(R.id.userProfilePic);
        TextView fullNameTextView = findViewById(R.id.userFullNameTextView);
        TextView aboutMeTextView = findViewById(R.id.userAboutMeTextView);
        TextView courseTextView = findViewById(R.id.userCourseTextView);
        TextView genderTextView = findViewById(R.id.userGenderTextView);
        ImageView closeProfileIcon = findViewById(R.id.closeProfileIcon);

        // fill view with data
        String name = user.getfName() + " " + user.getlName();
        fullNameTextView.setText(name);
        genderTextView.setText(user.getGender());
        courseTextView.setText(user.getCourse());
        if(user.getAboutMe().isEmpty()){
            String object = user.getGender().equals("Male") ? " him " : " her ";
            String emptyAboutMe = name + " did not write anything about" + object + "yet!";
            aboutMeTextView.setTextColor(Color.GRAY);
            aboutMeTextView.setTypeface(null, Typeface.ITALIC);
            aboutMeTextView.setText(emptyAboutMe);
        }else{
            aboutMeTextView.setText(user.getAboutMe());
        }

        String image = user.getImage();
        if (image.isEmpty()) {
            switch (user.getGender()) {
                case "Male":
                    profilePic.setImageResource(R.drawable.male);
                    break;
                case "Female":
                    profilePic.setImageResource(R.drawable.female);
                    break;
            }
        } else {
            bitmap = App.decodeImage(image);
            profilePic.setImageBitmap(bitmap);
        }

        // listen to profile pic clicks
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageDialog = new Dialog(getContext(), R.style.imageFrameDialog);
                imageDialog.setContentView(R.layout.profile_pic);
                popupProfilePic = imageDialog.findViewById(R.id.popupProfilePic);
                popupProfilePic.setImageBitmap(bitmap);
                imageDialog.show();
            }

        });

        closeProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmap!=null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap=null;
                }
                Runtime.getRuntime().gc();
                System.gc();
                dismiss();
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(bitmap!=null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap=null;
        }
        Runtime.getRuntime().gc();
        System.gc();
    }


}
