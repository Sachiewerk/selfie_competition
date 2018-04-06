package ie.wit.witselfiecompetition;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import ie.wit.witselfiecompetition.model.App;
import ie.wit.witselfiecompetition.model.Course;
import ie.wit.witselfiecompetition.model.DoWithDatabase;
import ie.wit.witselfiecompetition.model.DoWithDatabaseException;
import ie.wit.witselfiecompetition.model.EditTextViewListener;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * This Fragment for User's Profile
 * It shows editable user's information
 * and profile picture
 */
public class ProfileFragment extends Fragment {

    private static final int PERMISSION_CODE = 1;
    private static final int PIC_CAPTURE_CODE = 2;
    private static final int LOAD_IMAGE_CODE = 3;
    private ImageView profilePic, saveFullName, discardFullName, saveCourse, discardCourse,
            saveGender, discardGender, saveAboutMe, discardAboutMe, popupProfilePic;
    private EditText fullName, aboutMeEditText;
    private TextView courseTextView, genderTextView;
    private AppCompatSpinner coursesMenu;
    private RadioGroup genderRadioGroup;
    private FrameLayout courseEditIcon, fullNameEditIcon, genderEditIcon, aboutMeEditIcon;
    private LinearLayout changesControlFullName, changesControlCourse, changesControlGender, changesControlAboutMe;
    private String name, course, gender, aboutMe, image;
    private Dialog progressbar, imageDialog ;
    private Uri uri;
    private List<Bitmap> bitmaps;


    public ProfileFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bitmaps = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, null);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Profile");
        loadView();
        fillView();
        addEditIconListeners();
        addProfileImageListener();
    }



    /**
     * Load the views in the fragment
     */
    private void loadView(){
        profilePic = getView().findViewById(R.id.profilePic);
        fullNameEditIcon = getView().findViewById(R.id.settingsFullNameEditIcon);
        courseEditIcon = getView().findViewById(R.id.settingsCourseEditIcon);
        genderEditIcon = getView().findViewById(R.id.settingsGenderEditIcon);
        aboutMeEditIcon = getView().findViewById(R.id.settingsAboutMeEditIcon);
        fullName = getView().findViewById(R.id.settingsFullName);
        fullName.addTextChangedListener(new EditTextViewListener(fullName, fullNameEditIcon, null, 200));
        aboutMeEditText = getView().findViewById(R.id.settingsAboutMeEditText);
        courseTextView = getView().findViewById(R.id.settingsCourseTextView);
        courseTextView.addTextChangedListener(new EditTextViewListener(courseTextView, courseEditIcon, null, 110));
        genderTextView = getView().findViewById(R.id.settingsGenderTextView);
        coursesMenu = getView().findViewById(R.id.settingsCoursesMenu);
        genderRadioGroup = getView().findViewById(R.id.settingsGenderRadioGroup);
        changesControlFullName = getView().findViewById(R.id.settingsChangesControlFullName);
        changesControlCourse = getView().findViewById(R.id.settingsChangesControlCourse);
        changesControlGender = getView().findViewById(R.id.settingsChangesControlGender);
        changesControlAboutMe = getView().findViewById(R.id.settingsChangesControlAboutMe);
        saveFullName = getView().findViewById(R.id.settingsSaveFullName);
        discardFullName = getView().findViewById(R.id.settingsDiscardFullName);
        saveCourse  = getView().findViewById(R.id.settingsSaveCourse);
        discardCourse  = getView().findViewById(R.id.settingsDiscardCourse);
        saveGender  = getView().findViewById(R.id.settingsSaveGender);
        discardGender  = getView().findViewById(R.id.settingsDiscardGender);
        saveAboutMe  = getView().findViewById(R.id.settingsSaveAboutMe);
        discardAboutMe  = getView().findViewById(R.id.settingsDiscardAboutMe);
        progressbar = App.onTopProgressBar(getActivity());
    }


    /**
     * Fill view with exist data
     */
    private void fillView(){
        SharedPreferences pref = App.getCurrentUserSharedPreferences(getActivity());
        name  = pref.getString("fName", "") + " " + pref.getString("lName", "");
        fullName.setText(name);
        gender = pref.getString("gender", "");
        genderTextView.setText(gender);
        course = pref.getString("course", "");
        courseTextView.setText(course);
        aboutMe = pref.getString("aboutMe", "");
        aboutMeEditText.setText(aboutMe);
        image = pref.getString("image", "");
        if (image.isEmpty()) {
            switch (gender) {
                case "Male":
                    profilePic.setImageResource(R.drawable.male);
                    break;
                case "Female":
                    profilePic.setImageResource(R.drawable.female);
                    break;
            }
        } else {
            Bitmap bitmap = App.decodeImage(image);
            bitmaps.add(bitmap);
            profilePic.setImageBitmap(bitmap);
        }
    }


    /**
     * Add listeners to apply the functionality
     * of the edit, save and discard buttons
     */
    private void addEditIconListeners(){

        final Callable<Void> onFailure = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Toast.makeText(getActivity(), "Error in adding data to Database", Toast.LENGTH_SHORT).show();
                progressbar.cancel();
                return null;
            }
        };

        // Edit Full Name
        fullNameEditIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                toggleFocusable(fullName);
                App.toggleExistence(fullNameEditIcon, changesControlFullName);
            }
        });

        // Accept Full Name Changes
        saveFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(App.isValidNameToast(getActivity(),fullName, "Name")) {
                    App.hideSoftKeyboard(getActivity(), v);
                    progressbar.show();
                    final String fullN = fullName.getText().toString().trim();
                    int firstSpace = fullN.indexOf(" ");
                    String fName = fullN.substring(0, firstSpace).trim();
                    String lName = fullN.substring(firstSpace).trim();
                    final Map<String, Object> data = new HashMap<>();
                    data.put("fName", fName);
                    data.put("lName", lName);
                    final Callable<Void> onSucceed = new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            TextView fullNameTV = getActivity().findViewById(R.id.fullNameTextView);
                            fullNameTV.setText(fullN);
                            App.addToSharedPreferences(getActivity(), data);
                            toggleFocusable(fullName);
                            App.toggleExistence(fullNameEditIcon, changesControlFullName);
                            progressbar.cancel();
                            return null;
                        }
                    };

                    App.addToDatabase("Users", data, onSucceed, onFailure);
                }
            }
        });

        //Discard Full Name Changes
        discardFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullName.setText(name);
                toggleFocusable(fullName);
                App.toggleExistence(fullNameEditIcon, changesControlFullName);
            }
        });


        // Edit Course
        courseEditIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                course = courseTextView.getText().toString();
                App.toggleExistence(courseTextView, coursesMenu);
                App.toggleExistence(courseEditIcon, changesControlCourse);
                List<String> courses = new ArrayList<String>();
                courses.addAll(Arrays.asList(Course.courses()));
                String currentCourse = courseTextView.getText().toString();
                int index = courses.indexOf(currentCourse);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, courses);
                coursesMenu.setAdapter(adapter);
                coursesMenu.setSelection(index);
            }
        });

        // Accept Course Change
        saveCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressbar.show();
                final String newCourse = coursesMenu.getSelectedItem().toString();

                final Callable<Void> onSucceed = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        courseTextView.setText(newCourse);
                        App.addToSharedPreferences(getActivity(), "course", newCourse);
                        App.toggleExistence(courseTextView, coursesMenu);
                        App.toggleExistence(courseEditIcon, changesControlCourse);
                        progressbar.cancel();
                        return null;
                    }
                };

                App.addToDatabase("Users", "course", newCourse, onSucceed, onFailure);
            }
        });

        // Discard Course Change
        discardCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                courseTextView.setText(course);
                App.toggleExistence(courseTextView, coursesMenu);
                App.toggleExistence(courseEditIcon, changesControlCourse);
            }
        });

        // Edit Gender
        genderEditIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.toggleExistence(genderTextView, genderRadioGroup);
                App.toggleExistence(genderEditIcon, changesControlGender);
                gender = genderTextView.getText().toString();
                switch (gender){
                    case "Male":
                        genderRadioGroup.check(R.id.settingsGenderMale);
                        break;
                    case  "Female":
                        genderRadioGroup.check(R.id.settingsGenderFemale);
                        break;
                }

            }
        });

        // Accept Gender Change
        saveGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressbar.show();
                int id = genderRadioGroup.getCheckedRadioButtonId();
                final String gender = (id==R.id.settingsGenderMale) ? "Male" : "Female";

                final Callable<Void> onSucceed = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        genderTextView.setText(gender);
                        App.addToSharedPreferences(getActivity(), "gender", gender);
                        App.toggleExistence(genderTextView, genderRadioGroup);
                        App.toggleExistence(genderEditIcon, changesControlGender);
                        progressbar.cancel();
                        return null;
                    }
                };

                App.addToDatabase("Users", "gender", gender, onSucceed, onFailure);
            }
        });

        // Discard Gender Change
        discardGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderTextView.setText(gender);
                App.toggleExistence(genderTextView, genderRadioGroup);
                App.toggleExistence(genderEditIcon, changesControlGender);
            }
        });

        // Edit About Me
        aboutMeEditIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFocusable(aboutMeEditText);
                aboutMe = aboutMeEditText.getText().toString();
                App.toggleExistence(aboutMeEditIcon, changesControlAboutMe);
            }
        });

        // Accept About Me Changes
        saveAboutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.hideSoftKeyboard(getActivity(), v);
                progressbar.show();
                final String aboutMe = aboutMeEditText.getText().toString();
                final Callable<Void> onSucceed = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        toggleFocusable(aboutMeEditText);
                        App.toggleExistence(aboutMeEditIcon, changesControlAboutMe);
                        App.addToSharedPreferences(getActivity(), "aboutMe", aboutMe);
                        progressbar.cancel();
                        return null;
                    }
                };

                App.addToDatabase("Users", "aboutMe", aboutMe, onSucceed, onFailure);
            }
        });

        // Discard About Me Changes
        discardAboutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutMeEditText.setText(aboutMe);
                toggleFocusable(aboutMeEditText);
                App.toggleExistence(aboutMeEditIcon, changesControlAboutMe);
            }
        });

    }


    /**
     * Listen to the Profile Picture Clicks
     */
    private void addProfileImageListener(){
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageDialog = new Dialog(getActivity(), R.style.imageFrameDialog);
                imageDialog.setContentView(R.layout.profile_pic);
                popupProfilePic = imageDialog.findViewById(R.id.popupProfilePic);

                popupProfilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(getActivity(), popupProfilePic, Gravity.NO_GRAVITY, R.attr.actionOverflowMenuStyle, 0);
                        popup.getMenuInflater().inflate(R.menu.change_picture_menu, popup.getMenu());
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getTitle().toString()){
                                    case "Take Picture":
                                        if(App.grantPermission(getActivity(), PERMISSION_CODE)){
                                            takePicture();
                                        }
                                        break;
                                    case "Upload Picture":
                                        uploadPicture();
                                        break;
                                }
                                return true;
                            }
                        });
                        popup.show();
                    }

                });

                String encodedImage = App.getCurrentUserSharedPreferences(
                        getActivity()).getString("image", "");

                if(encodedImage.isEmpty()){
                    switch (gender){
                        case "Male":
                            popupProfilePic.setImageResource(R.drawable.male);
                            break;
                        case "Female":
                            popupProfilePic.setImageResource(R.drawable.female);
                            break;
                    }
                    progressbar.dismiss();
                }
                else {
                    popupProfilePic.setImageBitmap(App.decodeImage(encodedImage));

                    // now fetch the image from database (high quality)
                    final DoWithDatabase doWithDatabase = new DoWithDatabase("Users", "image");

                    Callable<Void> setImage = new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            String imageFromDB = doWithDatabase.getValue("image").toString();
                            Bitmap bitmap = App.decodeImage(imageFromDB);
                            bitmaps.add(bitmap);
                            popupProfilePic.setImageBitmap(bitmap);
                            doWithDatabase.removeDatabaseListener();
                            progressbar.dismiss();
                            return null;
                        }
                    };

                    try {
                        doWithDatabase.execute(setImage);
                    } catch (DoWithDatabaseException e) {
                        Log.e("DoWithDatabaseException", e.getMessage());
                    }
                }
                imageDialog.show();


            }
        });

    }


    /**
     * Toggle Focusable, Clickable, Cursor Visible
     * and FocusableInTouchMode
     * @param view
     */
    private void toggleFocusable(TextView view){
        if (view.isFocusable()) {
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
            view.setClickable(false);
            view.setCursorVisible(false);
            view.clearFocus();
            App.hideSoftKeyboard(getActivity(), view);
        }
        else{
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setClickable(true);
            view.setCursorVisible(true);
            view.requestFocus();
            App.showSoftKeyboard(getActivity(), view);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                Toast.makeText(getActivity(), "No permission to write to external storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**** TAKING PICTURE USING CAMERA****/
        if(requestCode == PIC_CAPTURE_CODE && resultCode == RESULT_CANCELED) {
            if(uri!=null){
                File f = new File(uri.getPath());
                f.delete();
            }
        }
        else if (requestCode == PIC_CAPTURE_CODE && resultCode == RESULT_OK) {
            final File pic = new File(uri.getPath());
            progressbar.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(pic.length()==0){
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.getMessage();
                        }
                    }
                    final String thumbnail = App.encodeImage(getActivity(),uri, 50);
                    Map<String, String> thumbnailInfo = new HashMap<>();
                    thumbnailInfo.put("image", thumbnail);
                    App.addToSharedPreferences(getActivity(),thumbnailInfo);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // refresh drawer image
                            ImageView profileImage = getActivity().findViewById(R.id.profileImage);
                            profileImage.setImageBitmap(App.decodeImage(thumbnail));

                            profilePic.setImageBitmap(App.decodeImage(thumbnail));

                            final String databaseImg = App.encodeImage(getActivity(),uri, 1200);
                            Map<String, String> databaseImgInfo = new HashMap<>();
                            databaseImgInfo.put("image", databaseImg);
                            App.addToDatabase(getActivity(),"Users", databaseImgInfo, "Failed to add image to database!");
                            popupProfilePic.setImageBitmap(App.decodeImage(databaseImg));
                            progressbar.dismiss();
                        }
                    });
                }
            });
            thread.start();
        }

        /**** UPLOADING PICTURE FROM GALLERY ****/
        else if (requestCode == LOAD_IMAGE_CODE && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            progressbar.show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final String thumbnail = App.encodeImage(getActivity(),imageUri, 50);
                    Map<String, String> thumbnailInfo = new HashMap<>();
                    thumbnailInfo.put("image", thumbnail);
                    App.addToSharedPreferences(getActivity(),thumbnailInfo);


                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            ImageView profileImage = getActivity().findViewById(R.id.profileImage);
                            profileImage.setImageBitmap(App.decodeImage(thumbnail));

                            profilePic.setImageBitmap(App.decodeImage(thumbnail));

                            final String databaseImg = App.encodeImage(getActivity(),imageUri, 1200);

                            Map<String, String> originalInfo = new HashMap<>();
                            originalInfo.put("image", databaseImg);
                            App.addToDatabase(getActivity(),"Users", originalInfo, "Failed to add image to database!");

                            popupProfilePic.setImageBitmap(App.decodeImage(databaseImg));
                            progressbar.dismiss();
                        }
                    });
                }
            });
            thread.start();
        }
    }



    /**
     * Take picture using the camera of mobile phone
     */
    private void takePicture() {
        File direct = new File(Environment.getExternalStorageDirectory() + "/witSelfieCompetition");

        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/witSelfieCompetition/");
            wallpaperDirectory.mkdirs();
        }

        String capturedImageName = String.format("selfie-%s.jpg",
                new SimpleDateFormat("ddMMyy-hhmmss.SSS", Locale.UK).format(new Date()));

        File file = new File(new File("/sdcard/witSelfieCompetition/"), capturedImageName);
        if (file.exists()) {
            file.delete();
        }

        try {
            file.createNewFile();
            uri = Uri.fromFile(file);
            Intent camera = new Intent();
            camera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(camera, PIC_CAPTURE_CODE);
        } catch (IOException e) {
            file.delete();
            App.showMessage(getActivity(), "Error!", "Could not save image", false);
        }

    }

    private void uploadPicture() {
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        gallery.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(gallery, LOAD_IMAGE_CODE);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(bitmaps!=null) {
            for (Bitmap bitmap : bitmaps) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle(); // free memory from bitmaps
                    bitmap = null;
                }
            }
        }
        Runtime.getRuntime().gc();
        System.gc();
    }
}
