package ie.wit.witselfiecompetition;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ie.wit.witselfiecompetition.model.App;


/**
 * The main activity for all other fragments.
 */
public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final int PERMISSION_CODE = 1;
    private final int PIC_CAPTURE_CODE = 2;
    private final int LOAD_IMAGE_CODE = 3;
    private ImageView profileImage;
    private Uri uri;
    private int lastSelectedItem;
    private NavigationView navigationView;
    private Dialog progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        progressBar = App.onTopProgressBar(this);
        /************** Navigation Drawer *****************/
        View header = navigationView.getHeaderView(0);
        navigationView.setCheckedItem(R.id.nav_profile);
        navigationView.getMenu().performIdentifierAction(R.id.nav_profile, 0);

        LinearLayout headerContainer = header.findViewById(R.id.header);
        String color = App.getCurrentUserSharedPreferences(Main.this).getString("color", "Blue");
        App.changeHeaderImageTheme(color, headerContainer, toolbar);

        TextView fullNameTextView = header.findViewById(R.id.fullNameTextView);
        profileImage =  header.findViewById(R.id.profileImage);

        App.setPersonalImageAndName(Main.this, fullNameTextView, profileImage);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(Main.this, profileImage);
                popup.getMenuInflater().inflate(R.menu.change_picture_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()){
                            case "Take Picture":
                                // check for permission to take photo and write to storage
                                // if granted, go ahead and take the pic
                                // otherwise, a permission request is sent and should be handled
                                // in the onRequestPermissionsResult() method
                                if(App.grantPermission(Main.this, PERMISSION_CODE)){
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

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }


        });

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                Toast.makeText(this, "No permission to write to external storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        /**** TAKING PICTURE USING CAMERA****/
        if(requestCode == PIC_CAPTURE_CODE && resultCode == RESULT_CANCELED) {
            if(uri!=null){
                File f = new File(uri.getPath());
                f.delete();
            }
        }
        if (requestCode == PIC_CAPTURE_CODE && resultCode == RESULT_OK) {
            final File pic = new File(uri.getPath());
            profileImage.setVisibility(View.INVISIBLE);
            progressBar.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(pic.length()==0){
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.getLocalizedMessage();
                        }
                    }
                    final String thumbnail = App.encodeImage(Main.this,uri, 50);
                    Map<String, String> thumbnailInfo = new HashMap<>();
                    thumbnailInfo.put("image", thumbnail);
                    App.addToSharedPreferences(Main.this,thumbnailInfo);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshProfilePicFrag();
                            profileImage.setImageBitmap(App.decodeImage(thumbnail));
                            profileImage.setVisibility(View.VISIBLE);
                            progressBar.dismiss();

                        }
                    });

                    final String databaseImg = App.encodeImage(Main.this,uri, 1200);
                    Map<String, String> databaseImgInfo = new HashMap<>();
                    databaseImgInfo.put("image", databaseImg);
                    App.addToDatabase(Main.this,"Users", databaseImgInfo, "Failed to add image to database!");

                }
            });
            thread.start();
        }

        /**** UPLOADING PICTURE FROM GALLERY ****/
        if (requestCode == LOAD_IMAGE_CODE && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            profileImage.setVisibility(View.INVISIBLE);
            progressBar.show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final String thumbnail = App.encodeImage(Main.this,imageUri, 50);
                    Map<String, String> thumbnailInfo = new HashMap<>();
                    thumbnailInfo.put("image", thumbnail);
                    App.addToSharedPreferences(Main.this,thumbnailInfo);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshProfilePicFrag();
                            profileImage.setImageBitmap(App.decodeImage(thumbnail));
                            profileImage.setVisibility(View.VISIBLE);
                            progressBar.dismiss();
                        }
                    });



                    final String original = App.encodeImage(Main.this,imageUri, 1200);
                    Map<String, String> originalInfo = new HashMap<>();
                    originalInfo.put("image", original);
                    App.addToDatabase(Main.this,"Users", originalInfo, "Failed to add image to database!");
                }
            });
            thread.start();
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////






    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        final FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
        String fragTag = null;

        final int id = item.getItemId();

        if (id != lastSelectedItem) {

            if (id == R.id.nav_profile) {
                fragTag = "fragmentProfile";
                fragment = fragmentManager.findFragmentByTag(fragTag);
                if(fragment == null){fragment = new ProfileFragment();}
            } else if (id == R.id.nav_competition) {
                fragTag = "fragmentCompetition";
                fragment = fragmentManager.findFragmentByTag(fragTag);
                if(fragment == null){fragment = new CompetitionFragment();}

            } else if (id == R.id.nav_gallery) {
                fragTag = "fragmentGallery";
                fragment = fragmentManager.findFragmentByTag(fragTag);
                if(fragment == null){fragment = new GalleryFragment();}

            } else if (id == R.id.nav_settings) {
                fragTag = "fragmentSettings";
                fragment = fragmentManager.findFragmentByTag(fragTag);
                if(fragment == null){fragment = new SettingsFragment();}

            } else if (id == R.id.nav_signout) {
                FirebaseAuth.getInstance().signOut();
                App.redirect(Main.this, Login.class, false);
                return true;
            }

            final String finalFragTag = fragTag;
            final Fragment finalFragment = fragment;

            // to reduce the hangs when moving between fragments
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragmentManager.beginTransaction().replace(R.id.content_area, finalFragment, finalFragTag)
                            .setTransition(FragmentTransaction.TRANSIT_NONE)
                            .addToBackStack(finalFragTag)
                            .commit();

                    lastSelectedItem = id;

                }
            }, 300);

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }


    /**
     * This method is invoked upon
     * rotating the mobile phone
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    /**
     * Refresh the profile picture in the profile fragment
     * when user changes it from the navigation header
     */
    private void refreshProfilePicFrag(){
        if(navigationView.getMenu().findItem(R.id.nav_profile).isChecked()){
            ImageView profilePic = getSupportFragmentManager()
                    .findFragmentByTag("fragmentProfile")
                    .getView().findViewById(R.id.profilePic);
            profilePic.setImageBitmap(
                    App.decodeImage(
                            App.getCurrentUserSharedPreferences(Main.this).getString("image", "")));
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
            App.showMessage(this, "Error!", "Could not save image", false);
        }
    }



    private void uploadPicture() {
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        gallery.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(gallery, LOAD_IMAGE_CODE);
    }


}
