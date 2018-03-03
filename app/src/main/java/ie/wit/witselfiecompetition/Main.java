package ie.wit.witselfiecompetition;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final int PERMISSION_CODE = 1;
    private final int PIC_CAPTURE_CODE = 2;
    private final int LOAD_IMAGE_CODE = 3;
    private TextView fullNameTextView;
    private ImageView profileImage;
    private ProgressBar profileImageProgressBar;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                       // .setAction("Action", null).show();
                Helper.clearSharedPreferences(Main.this);
                FirebaseAuth.getInstance().signOut();

                Helper.redirect(Main.this, Login.class, false);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        fullNameTextView = header.findViewById(R.id.fullNameTextView);
        profileImage =  header.findViewById(R.id.profileImage);
        profileImageProgressBar = header.findViewById(R.id.profileImageProgressBar);

        //Helper.setPersonalImageAndNameFromDB(fullNameTextView, profileImage);
        Helper.setPersonalImageAndName(Main.this, fullNameTextView, profileImage);

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
                                if(Helper.grantPermission(Main.this, PERMISSION_CODE)){
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

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                Toast.makeText(this, "No permission to read external storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void takePicture() {
        String picturesDir = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        String newDirPath = picturesDir + "/witSelfieCompetition/";
        File newDir = new File(newDirPath);
        if(!newDir.exists()){newDir.mkdirs();}
        String picName = "/selfie-" + new SimpleDateFormat( "ddMMyy-hhmmss.SSS").format(new Date()) + ".jpg";

        try {
            File picfile = new File(newDir+picName);
            picfile.createNewFile();
            uri = Uri.fromFile(picfile);
            Intent camera = new Intent();
            camera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(camera, PIC_CAPTURE_CODE);
        }
        catch (IOException e) {
            Helper.showMessage(Main.this,"Error!", "Could not save image", false);
        }

    }



    public void uploadPicture() {
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        gallery.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(gallery, LOAD_IMAGE_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        /**** TAKING PICTURE USING CAMERA****/
        if(requestCode == PIC_CAPTURE_CODE && resultCode == RESULT_CANCELED) { new File(uri.getPath()).delete();}
        if (requestCode == PIC_CAPTURE_CODE && resultCode == RESULT_OK) {
            final File pic = new File(uri.getPath());
            Helper.toggleProgressBar(profileImage, profileImageProgressBar);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(pic.length()==0){
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.getLocalizedMessage();
                        }
                    }
                    final String thumbnail = Helper.encodeImage(Main.this,uri, true); //500 x 600
                    Map<String, String> thumbnailInfo = new HashMap<>();
                    thumbnailInfo.put("image", thumbnail);
                    Helper.addToSharedPreferences(Main.this,thumbnailInfo);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            profileImage.setImageBitmap(Helper.decodeImage(thumbnail));
                            Helper.toggleProgressBar(profileImage, profileImageProgressBar);

                        }
                    });

                    final String original = Helper.encodeImage(Main.this,uri, false);
                    Map<String, String> originalInfo = new HashMap<>();
                    originalInfo.put("image", original);
                    Helper.addToDatabase(Main.this,"Users", originalInfo, "Failed to add image to database!");


                }
            });
            thread.start();
        }

        /**** TAKING PICTURE USING CAMERA****/
        if (requestCode == LOAD_IMAGE_CODE && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            Helper.toggleProgressBar(profileImage, profileImageProgressBar);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final String thumbnail = Helper.encodeImage(Main.this,imageUri, true); //500 x 600
                    Map<String, String> thumbnailInfo = new HashMap<>();
                    thumbnailInfo.put("image", thumbnail);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            profileImage.setImageBitmap(Helper.decodeImage(thumbnail));
                            Helper.toggleProgressBar(profileImage, profileImageProgressBar);
                        }
                    });

                    Helper.addToSharedPreferences(Main.this,thumbnailInfo);

                    final String original = Helper.encodeImage(Main.this,imageUri, false);
                    Map<String, String> originalInfo = new HashMap<>();
                    originalInfo.put("image", original);
                    Helper.addToDatabase(Main.this,"Users", originalInfo, "Failed to add image to database!");


                }
            });
            thread.start();
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
