package ie.wit.witselfiecompetition;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import ie.wit.witselfiecompetition.model.App;
import ie.wit.witselfiecompetition.model.Competition;
import ie.wit.witselfiecompetition.model.CompetitionDeadlineWatcher;
import ie.wit.witselfiecompetition.model.ImageAdapter;
import ie.wit.witselfiecompetition.model.Selfie;
import ie.wit.witselfiecompetition.model.SelfiePagerAdapter;
import ie.wit.witselfiecompetition.model.User;
import ie.wit.witselfiecompetition.model.UserProfileDialog;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * This Class responsible for rendering all selfies
 * and handling all actions related to the
 * submitted selfies in a given competition
 * User can submit selfie, react with already-submitted
 * selfies via giving likes and navigate submitted selfies
 * Also can go to selfies submitters profiles,
 * report inappropriate selfies or delete his own selfie
 * Care has been taken to manage the memory for handling the
 * bitmaps, however, if the device RAM capacity is low
 * and there are a lot of selfies submitted in the competition
 * low quality images may be produced
 * Created by Yahya Almardeny on 28/03/18.
 */

public class SelfieNavigator extends Fragment {

    private ImageView submit_selfie;
    private FrameLayout back;
    private LinearLayout beFirstLabel;
    private View selfie_ActionBar;
    private ActionBar actionBar;
    private GridView selfieGridView;
    private List<String> selfiesId, winners;
    private Competition competition;
    private String USER_ID;
    private DatabaseReference SELFIES_REFERENCE, COMPETITION_SELFIES_REFERENCE;
    private Dialog fullScreenProgressBar;
    private final int PERMISSION_CODE = 1, PIC_CAPTURE_CODE = 2, LOAD_IMAGE_CODE = 3;
    private Uri uri;
    private MenuItem delete, report, profile;
    private ViewPager viewPager = null;
    private SelfiePagerAdapter selfiePagerAdapter = null;
    private LayoutInflater inflater;
    private AtomicBoolean userSubmittedSelfie, halt, lastSelfie;
    private AtomicInteger selfieIndex;
    private long AVAILABLE_MEMORY;
    private volatile List<Bitmap> bitmaps;
    private Map<DatabaseReference, ValueEventListener> databaseListeners;
    private int selfieMemoryShare = -1;
    private CompetitionDeadlineWatcher competitionDeadlineWatcher;
    private ImageAdapter imageAdapter;
    private TextView title;
    private DrawerLayout drawer;
    private boolean OPEN = false;


    public SelfieNavigator() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // allow menu options in custom actionbar
        setHasOptionsMenu(true);

        // create full screen progress bar
        fullScreenProgressBar = App.onTopProgressBar(getActivity());


        // get the details of the clicked competition
        competition = (Competition) getArguments().getSerializable("competition");

        // define the Selfies reference in DB
        SELFIES_REFERENCE = FirebaseDatabase.getInstance().getReference()
                            .child("Selfie").child(competition.getcId());

        // define the DB reference of the selfies IDs list in the competition collection
        COMPETITION_SELFIES_REFERENCE = FirebaseDatabase.getInstance().getReference()
                                        .child("Competition").child(competition.getcId()).child("selfiesId");

        // get the current user id
        USER_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // create layout inflater
        inflater = getActivity().getLayoutInflater();

        // clean memory
        cleanMemory();

        // initialize atomic vars to be used in the DB listeners / DB transactions
        selfieIndex = new AtomicInteger(0);
        halt = new AtomicBoolean(false);
        userSubmittedSelfie = new AtomicBoolean(false);
        lastSelfie = new AtomicBoolean(false);

        // initialize map to save the DB listeners, so can be removed on fragment destroy
        databaseListeners = new HashMap<>();
        // save the used bitmaps, so can be recycled on destroy
        bitmaps = new ArrayList<>();
        // selfies ids list to be fetched from the competition collection
        selfiesId = new ArrayList<>();

        // initialize the GridView adapter
        imageAdapter = new ImageAdapter(getContext());

        // initialize the ViewPager adapter
        selfiePagerAdapter = new SelfiePagerAdapter();

        // deadline watcher, take the user out of the navigator if they still in it after
        // the competition closed

        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call(){
                Toast.makeText(getContext(), "The Competition is close now!", Toast.LENGTH_LONG).show();
                back.callOnClick();
                return null;
            }
        };

        competitionDeadlineWatcher = new CompetitionDeadlineWatcher(callable);
        if(App.isAfterNow(competition.getCloseDate())) {
            OPEN = true;
            competitionDeadlineWatcher.execute(competition.getCloseDate());
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selfie, null);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadView();
        addActionBarListener();
        initialize();
    }


    /**
     * Load the components of the views
     */
    private void loadView() {

        // Custom ActionBar
        selfie_ActionBar = inflater.inflate(R.layout.selfie_actionbar, null);
        back = selfie_ActionBar.findViewById(R.id.back);
        submit_selfie = selfie_ActionBar.findViewById(R.id.submit_selfie);
        title = selfie_ActionBar.findViewById(R.id.actionbar_title);

        // Fragment Selfie
        beFirstLabel = getView().findViewById(R.id.beFirstLabel);

        // View Pager
        viewPager = getView().findViewById(R.id.viewPager);
        viewPager.setAdapter(selfiePagerAdapter);

        // Grid view
        selfieGridView = getView().findViewById(R.id.selfieGridView);

        selfieGridView.setAdapter(imageAdapter);


        selfieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                viewPager.setCurrentItem(position);
                toggleVisibility(viewPager);
                toggleVisibility(selfieGridView);
                title.getLayoutParams().width = App.getScreenWidth()/2;
                if(OPEN) {
                    showMenu("profile", "report", "delete");
                }else {
                    showMenu("profile");
                }
            }
        });


    }



    /**
     * Remove the default actionbar from the
     * parent (main) activity and set the customized
     * one for the selfie fragment.
     * add listener to the back arrow icon so
     * user can back from gird view and also
     * the actionbar will back to its first state (appearance)
     * if user in the view pager, back to gallery
     * Also add listener to the sub,it_selfie icon
     * so On click, it shows a menu with two options:
     * Take Picture and Upload Picture (locally)
     */
    private void addActionBarListener(){

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        drawer = getActivity().findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (actionBar != null) {
            title.getLayoutParams().width = (int) (App.getScreenWidth()*0.65);
            title.setText(competition.getName());
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(selfie_ActionBar);
            toolbar.setNavigationIcon(null);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(viewPager.getVisibility()== View.VISIBLE){
                        toggleVisibility(viewPager);
                        toggleVisibility(selfieGridView);
                        title.getLayoutParams().width = (int) (App.getScreenWidth()*0.65);
                        hideMenu("profile", "report", "delete");
                    }
                    else {
                        actionBar.setDisplayShowCustomEnabled(false);
                        actionBar.setDisplayShowTitleEnabled(true);
                        toggle.syncState();
                        getActivity().onBackPressed();
                    }
                }
            });
        }

        //For Back Button Press As Well
        View view = getView();
        if (view != null) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            if(viewPager.getVisibility()== View.VISIBLE){
                                toggleVisibility(viewPager);
                                toggleVisibility(selfieGridView);
                                title.getLayoutParams().width = (int) (App.getScreenWidth()*0.65);
                                hideMenu("profile", "report", "delete");
                            }
                            else {
                                if (actionBar != null) {
                                    actionBar.setDisplayShowCustomEnabled(false);
                                    actionBar.setDisplayShowTitleEnabled(true);
                                }
                                toggle.syncState();
                                getActivity().onBackPressed();
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        submit_selfie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getActivity(), submit_selfie);
                popup.getMenuInflater().inflate(R.menu.change_picture_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()) {
                            case "Take Picture":
                                if (App.grantPermission(getActivity(), PERMISSION_CODE)) {
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


    /**
     * Initialize Selfie Navigator Lists
     * copy the selfies ids and put the current user id
     * at the beginning if he already submitted one
     * fill the view pager and grid view with empty views
     * that will be filled later from database
     */
    private void initialize(){
        List<String> temp = competition.getSelfiesId();
        if(temp!=null){
            int index = temp.indexOf(USER_ID);
            if(index!=-1){
                selfiesId.add(temp.get(index));
                temp.remove(index);
                userSubmittedSelfie.set(true);
                toggleVisibility(submit_selfie);
            }
            selfiesId.addAll(temp);
        }

        AVAILABLE_MEMORY = Runtime.getRuntime().freeMemory();

        if(OPEN){
            if(selfiesId.size()==0){
                beFirstLabel.setVisibility(View.VISIBLE); // it shows text saying "be the first who submit selfie"
            }else{
                fetchSelfie();
            }

        }else{ // get the winner only
            submit_selfie.setVisibility(View.GONE);
            if(selfiesId.size()==0){// if no winner
                beFirstLabel.setVisibility(View.VISIBLE);
                TextView selfieNote = getView().findViewById(R.id.selfieNote);
                String text = "No selfies submitted to this competition, thus there is no winner!";
                selfieNote.setText(text);

            }else{
                winners = new ArrayList<>();
                ValueEventListener vel = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        winners = (List<String>) dataSnapshot.getValue();
                        if (winners != null) {
                            fetchWinnersSelfies();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                };
                DatabaseReference dbr = FirebaseDatabase.getInstance().getReference()
                        .child("Winner").child(competition.getcId());
                databaseListeners.put(dbr, vel);
                dbr.addListenerForSingleValueEvent(vel);
            }
        }
    }


    /**
     * Take picture using the camera of mobile phone
     */
    private void takePicture() {
        String picturesDir = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        String newDirPath = picturesDir + "/witSelfieCompetition/";
        File newDir = new File(newDirPath);
        if (!newDir.exists()) {
            newDir.mkdirs();
        }
        String capturedImageName = String.format("selfie-%s.jpg", new SimpleDateFormat("ddMMyy-hhmmss.SSS", Locale.UK).format(new Date()));
        File picFile = new File(newDir + capturedImageName);

        try {
            picFile.createNewFile();
            uri = Uri.fromFile(picFile);
            Intent camera = new Intent();
            camera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(camera, PIC_CAPTURE_CODE);
        } catch (IOException e) {
            picFile.delete();
            App.showMessage(getActivity(), "Error!", "Could not save image", false);
        }

    }


    /**
     * Upload the selfie picture from gallery
     */
    private void uploadPicture() {
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        gallery.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(gallery, LOAD_IMAGE_CODE);
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
        if (requestCode == PIC_CAPTURE_CODE && resultCode == RESULT_CANCELED) {
            if (uri != null) {
                File f = new File(uri.getPath());
                f.delete();
            }
        } else if (requestCode == PIC_CAPTURE_CODE && resultCode == RESULT_OK) {
            final File pic = new File(uri.getPath());
            fullScreenProgressBar.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (pic.length() == 0) { // wait for the image to be created
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.getMessage();
                        }
                    }
                    // 500 kb = 0.5 mb
                    // (500 * 1024 * 8) / 16(bit depth) = 256000 pixel (505*505)px or (5.2*5.2)inch
                    final String encodedSelfie = App.encodeImage(getActivity(), uri, 500);
                    // add it to database
                    final Selfie selfie = new Selfie(USER_ID, encodedSelfie, new ArrayList<String>());
                    SELFIES_REFERENCE.child(USER_ID).setValue(selfie).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (!task.isSuccessful()) {
                                Toast.makeText(getContext(),
                                        "Error while trying to submit selfie", Toast.LENGTH_LONG).show();
                            } else {
                                selfiePagerAdapter.addView(getSelfieView(selfie),0);
                                imageAdapter.addView(bitmaps.get(bitmaps.size()-1),0);
                                selfiesId.add(0, USER_ID);
                                selfieIndex.incrementAndGet();
                                submit_selfie.setVisibility(View.INVISIBLE);
                                userSubmittedSelfie.set(true);
                                // add to competition selfiesId
                                addCurrentUserSelfieToCompetition();
                                viewPager.setCurrentItem(0);
                                beFirstLabel.setVisibility(View.GONE);
                                fullScreenProgressBar.dismiss();
                            }
                        }
                    });
                }
            });

            thread.start();
        }


        /**** UPLOADING PICTURE FROM GALLERY ****/
        else if (requestCode == LOAD_IMAGE_CODE && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            fullScreenProgressBar.show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final String encodedSelfie = App.encodeImage(getActivity(), imageUri, 500);
                    // add it to database
                    final Selfie selfie = new Selfie(USER_ID, encodedSelfie, new ArrayList<String>());
                    SELFIES_REFERENCE.child(USER_ID).setValue(selfie).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(getContext(),
                                        "Error while trying to submit selfie", Toast.LENGTH_LONG).show();
                            } else {
                                selfiePagerAdapter.addView(getSelfieView(selfie),0);
                                imageAdapter.addView(bitmaps.get(bitmaps.size()-1),0);
                                selfiesId.add(0, USER_ID);
                                selfieIndex.incrementAndGet();
                                submit_selfie.setVisibility(View.INVISIBLE);
                                userSubmittedSelfie.set(true);
                                // add to competition selfiesId
                                addCurrentUserSelfieToCompetition();
                                viewPager.setCurrentItem(0);
                                beFirstLabel.setVisibility(View.GONE);
                                fullScreenProgressBar.dismiss();
                            }
                        }
                    });
                }
            });
            thread.start();
        }
    }


    /**
     * Recursively fetch the winners selfies from database
     */
    private void fetchWinnersSelfies(){
      if(selfieIndex.get() < winners.size()){
          ValueEventListener vel = new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                  if (!halt.get()) {
                      final Selfie selfie = dataSnapshot.getValue(Selfie.class);
                      if (selfie != null) {
                          FirebaseDatabase.getInstance().getReference().child("Users").child(selfie.getuId())
                                  .addListenerForSingleValueEvent(new ValueEventListener() {
                                      @Override
                                      public void onDataChange(DataSnapshot dataSnapshot) {
                                          User user = dataSnapshot.getValue(User.class);
                                          selfiePagerAdapter.addView(getWinnerSelfieView(selfie, user));
                                          imageAdapter.addView(bitmaps.get(selfieIndex.get()));
                                          selfieIndex.getAndIncrement();
                                      }

                                      @Override
                                      public void onCancelled(DatabaseError databaseError) { }
                                  });

                      }
                  }
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {
              }
          };
          DatabaseReference dbr = SELFIES_REFERENCE.child(winners.get(selfieIndex.get()));
          databaseListeners.put(dbr, vel);
          dbr.addListenerForSingleValueEvent(vel);
          if (selfieIndex.get() == (winners.size() - 1)) lastSelfie.set(true);
      }
    }


    /**
     * Hide menu items
     */
    private void hideMenu(String...items) {
        for(String item : items){
            switch (item.toUpperCase()){
                case "PROFILE":
                    profile.setVisible(false);
                    break;
                case "REPORT":
                    report.setVisible(false);
                    break;
                case "DELETE":
                    delete.setVisible(false);
                    break;
            }
        }
    }


    /**
     * Show Menu Items
     */
    private void showMenu(String...items) {
        for(String item : items){
            switch (item.toUpperCase()){
                case "PROFILE":
                    profile.setVisible(true);
                    break;
                case "REPORT":
                    report.setVisible(true);
                    break;
                case "DELETE":
                    delete.setVisible(true);
                    break;
            }
        }
    }


    /**
     * Create a Selfie View and fill it
     * with data: Image, Likes and Number of Likes
     * Add click listener to like icon
     *
     * @param selfie
     * @return
     */
    private View getSelfieView(final Selfie selfie) {
        // load view
        View view = inflater.inflate(R.layout.selfie_view, null);
        ImageView selfieIm = view.findViewById(R.id.selfie);
        final ImageView likeIconClicked = view.findViewById(R.id.likeIconClicked);
        final ImageView likeIcon = view.findViewById(R.id.likeIcon);
        final TextView likesTextView = view.findViewById(R.id.likesTextView);

        // decode image string, create bitmap and set it
        byte[] decodedString = Base64.decode(selfie.getImage(), Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = availableBitmapSampleSize(selfie.getImage().length());
        options.inPurgeable = true;
        options.outHeight = App.getScreenHeight();
        options.outWidth = App.getScreenWidth();
        options.inPreferredConfig = Bitmap.Config.RGB_565; // no alpha is required, no transparency factor
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
        bitmaps.add(bitmap);
        selfieIm.setImageBitmap(bitmap);


        // set likes
        final List<String> likes = selfie.getLikes();
        if (likes != null) {
            likesTextView.setText(String.valueOf(likes.size()));
            if (likes.contains(USER_ID)) {
                likeIcon.setVisibility(View.GONE);
                likeIconClicked.setVisibility(View.VISIBLE);
                likesTextView.setTextColor(Color.RED);
            } else {
                likeIcon.setVisibility(View.VISIBLE);
                likeIconClicked.setVisibility(View.GONE);
                likesTextView.setTextColor(Color.WHITE);
            }
        } else {
            likeIcon.setVisibility(View.VISIBLE);
            likeIconClicked.setVisibility(View.GONE);
            likesTextView.setText("0");
            likesTextView.setTextColor(Color.WHITE);
        }

        // Add Like Listener
        likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeIcon.setEnabled(false);
                SELFIES_REFERENCE.child(selfie.getuId()).child("likes")
                        .runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData currentData) {
                                List<String> temp;
                                if (currentData.getValue() == null) {
                                    temp = new ArrayList<>();
                                    temp.add(USER_ID);
                                    currentData.setValue(temp);
                                } else {
                                    temp = (List<String>) currentData.getValue();
                                    temp.add(USER_ID);
                                    currentData.setValue(temp);
                                }
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(DatabaseError firebaseError, boolean committed,
                                                   DataSnapshot currentData) {
                                List<String> temp = (List<String>) currentData.getValue();
                                if (temp != null && temp.contains(USER_ID)) {
                                    likesTextView.setText(String.valueOf(temp.size()));
                                    likesTextView.setTextColor(Color.RED);
                                    likeIcon.setVisibility(View.GONE);
                                    likeIconClicked.setVisibility(View.VISIBLE);
                                }
                                likeIcon.setEnabled(true);
                            }
                        });
            }
        });

        likeIconClicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeIconClicked.setEnabled(false);
                SELFIES_REFERENCE.child(selfie.getuId()).child("likes")
                        .runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData currentData) {
                                List<String> temp;
                                if (currentData.getValue() != null) {
                                    temp = (List<String>) currentData.getValue();
                                    temp.remove(USER_ID);
                                    currentData.setValue(temp);
                                }
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(DatabaseError firebaseError, boolean committed,
                                                   DataSnapshot currentData) {
                                List<String> temp = (List<String>) currentData.getValue();
                                if (temp == null) {
                                    likesTextView.setText("0");
                                    likesTextView.setTextColor(Color.WHITE);
                                    likeIcon.setVisibility(View.VISIBLE);
                                    likeIconClicked.setVisibility(View.GONE);
                                } else if (!temp.contains(USER_ID)) {
                                    likesTextView.setText(String.valueOf(temp.size()));
                                    likesTextView.setTextColor(Color.WHITE);
                                    likeIcon.setVisibility(View.VISIBLE);
                                    likeIconClicked.setVisibility(View.GONE);
                                }
                                likeIconClicked.setEnabled(true);
                            }
                        });
            }
        });

        return view;
    }



    /**
     * Selfie View for each winner
     * @param selfie
     * @return
     */
    private View getWinnerSelfieView(Selfie selfie, User user) {
        // load view
        View view = inflater.inflate(R.layout.winner_view, null);
        ImageView selfieIm = view.findViewById(R.id.winnerSelfie);
        final TextView nameTextView = view.findViewById(R.id.winnerNameTextView);
        final TextView likesTextView = view.findViewById(R.id.winnerLikesTextView);

        // decode image string, create bitmap and set it
        byte[] decodedString = Base64.decode(selfie.getImage(), Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = availableBitmapSampleSize(selfie.getImage().length());
        options.inPurgeable = true;
        options.outHeight = App.getScreenHeight();
        options.outWidth = App.getScreenWidth();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
        bitmaps.add(bitmap);
        selfieIm.setImageBitmap(bitmap);

        String winnerName = user!=null? "Winner: " +  user.getfName() + " " + user.getlName() + "\n" : "";
        nameTextView.setText(winnerName);

        // get likes
        final List<String> likes = selfie.getLikes();
        if (likes != null) {
            likesTextView.setText(String.valueOf("Likes: " + likes.size()));
        }

        return view;
    }



    /**
     * Database Transaction to Add the current's user
     * Selfie Id to the Competition Collection
     */
    private void addCurrentUserSelfieToCompetition() {
        COMPETITION_SELFIES_REFERENCE.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                List<String> temp;
                if (currentData.getValue() == null) {
                    temp = new ArrayList<>();
                    temp.add(USER_ID);
                    currentData.setValue(temp);
                } else {
                    temp = (List<String>) currentData.getValue();
                    temp.add(USER_ID);
                    currentData.setValue(temp);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot currentData) {
                List<String> temp = (List<String>) currentData.getValue();
                if (userSubmittedSelfie.get()) {
                    if (temp == null || !temp.contains(USER_ID)) {
                        addCurrentUserSelfieToCompetition();
                    }
                }
            }
        });
    }


    /**
     * Database Transaction to remove current user's selfie's Id
     * from the competition collection
     */
    private void removeCurrentUserSelfieFromCompetition() {

        COMPETITION_SELFIES_REFERENCE.runTransaction(new Transaction.Handler() {
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
                if (!userSubmittedSelfie.get() && temp != null) {
                    if (temp.contains(USER_ID)) {
                        // recursion in case of failure due to concurrency in database
                        removeCurrentUserSelfieFromCompetition();
                    }
                } else if (!userSubmittedSelfie.get() && temp == null) {
                    // that means my selfie is the last one left
                    beFirstLabel.setVisibility(View.VISIBLE);
                    back.callOnClick();
                }
            }
        });
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.selfie_menu, menu);

        // a workaround to display icons in menu
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        super.onCreateOptionsMenu(menu, inflater);

        profile = menu.findItem(R.id.showProfileItem);
        report = menu.findItem(R.id.reportItem);
        delete = menu.findItem(R.id.deleteItem);
        hideMenu("profile", "report", "delete");
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(viewPager.getVisibility() == View.VISIBLE) {
            if (userSubmittedSelfie.get() && viewPager.getCurrentItem() == 0) {
                delete.setVisible(true);
            } else {
                delete.setVisible(false);
            }
        }
    }


    /**
     * Add implementation to the menu items
     * Show User's Profile
     * Report Selfie as Inappropriate
     * Delete Selfie
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showProfileItem:
                showProfile();
                return true;

            case R.id.reportItem:
                confirmDialog("Confirm Report",
                        "Are you sure that you want to report this selfie as inappropriate?", "Report",
                        new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {

                                fullScreenProgressBar.show();
                                DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Report")
                                        .child(competition.getcId()).push();
                                String value = OPEN ? selfiesId.get(viewPager.getCurrentItem()) : winners.get(viewPager.getCurrentItem());

                                root.setValue(value)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getContext(),
                                                            "Thank you for taking care of WIT Selfie Competition Community. \n" +
                                                                    "We will review your report and take the required action,",
                                                            Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(getContext(),
                                                            "Error while trying to report selfie", Toast.LENGTH_SHORT).show();
                                                }
                                                fullScreenProgressBar.dismiss();
                                            }
                                        });
                                return null;
                            }
                        });
                return true;

            case R.id.deleteItem:
                confirmDialog("Confirm Deletion",
                        "Are you sure that you want to delete your selfie?", "Delete",
                        new Callable<Void>() {
                            @Override
                            public Void call() {
                                fullScreenProgressBar.show();
                                // remove selfie from the relevant collections
                                SELFIES_REFERENCE.child(USER_ID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            selfiePagerAdapter.removeView(viewPager, 0);
                                            imageAdapter.removeView(0);
                                            bitmaps.remove(0);
                                            selfiesId.remove(0);
                                            selfieIndex.decrementAndGet();
                                            userSubmittedSelfie.set(false);
                                            removeCurrentUserSelfieFromCompetition();

                                            //if(bitmap!=null && !bitmap.isRecycled()) bitmap.recycle();
                                            submit_selfie.setVisibility(View.VISIBLE);
                                        } else {
                                            Toast.makeText(getContext(),
                                                    "Error while trying to delete selfie", Toast.LENGTH_SHORT).show();
                                        }
                                        fullScreenProgressBar.dismiss();
                                    }
                                });
                                return null;
                            }
                        });
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Invoke Garbage Collector
     * Remove Database Listeners
     * Recycle All Bitmaps
     * Destroy Drawing Cache
     * To clean and free memory
     * of the unwanted stuff
     */
    private void cleanMemory() {
        // show progressbar in case it takes long time
        fullScreenProgressBar.show();
        // stop database listeners internally from assigning fetched data
        if(halt!=null) {
            halt.set(true);
        }
        // remove all database listeners
        if (databaseListeners != null) {
            for (Map.Entry entry : databaseListeners.entrySet()) {
                DatabaseReference dbr = (DatabaseReference) entry.getKey();
                ValueEventListener vel = (ValueEventListener) entry.getValue();
                dbr.removeEventListener(vel);
            }
        }
        // recycle all bitmaps
        if (bitmaps != null) {
            for (Bitmap bitmap : bitmaps) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle(); // free memory from bitmaps
                    bitmap = null;
                }
            }
        }
        if (imageAdapter != null) {
            imageAdapter.format();
            imageAdapter = null;
        }
        if (selfieGridView != null) {
            selfieGridView = null;
        }
        // reset all ImageViews if not null
        if (selfiePagerAdapter != null) {
            selfiePagerAdapter.format();
            selfiePagerAdapter = null;
        }
        // destroy drawing cache in view pager
        if (viewPager != null) {
            viewPager.destroyDrawingCache();
            viewPager = null;
        }
        // Invoke Garbage Collector
        Runtime.getRuntime().gc();
        System.gc();
        // hide progressbar at the end
        fullScreenProgressBar.dismiss();
    }



    /**
     * To reduce the selfies sizes
     * to fit with the available free memory
     * on the user's device
     *
     * @param imageSize
     * @return
     */
    private int availableBitmapSampleSize(int imageSize) {
        // calculate the available space in RAM for each selfie
        // consider 50% of RAM as max threshold share for all selfies
        if (selfieMemoryShare == -1) {
            if(OPEN) {
                selfieMemoryShare = Math.round(AVAILABLE_MEMORY / ((selfiesId.size()+1) * 2));
            }
            else{
                selfieMemoryShare = Math.round(AVAILABLE_MEMORY / ((winners.size()+1) * 2));
            }
        }
        // assign the maximum number in case of error
        int bss = Integer.MAX_VALUE;
        // if there is no enough memory
        if (selfieMemoryShare == 0) {
            halt.set(true);
            // inform user and back to main activity
            App.showMessage(getActivity(), "Error: Out of Memory",
                    "No Enough Memory on your device to load the selfies!", new Callable<Void>() {
                        @Override
                        public Void call() {
                            cleanMemory(); // clean memory before go
                            App.redirect(getActivity(), Main.class, false);
                            return null;
                        }
                    });

        } else {// if there is free memory -> calculate the allowed Bitmap Sample Size for each selfie
            bss = (imageSize > selfieMemoryShare) ? (int) Math.ceil(imageSize / selfieMemoryShare) : 1;
            // give a hint to user if the quality is very poor
            if (bss > 10 && lastSelfie.get()) {
                Toast.makeText(getContext(),
                        "If selfies have poor quality, that's because of the low free memory on your device",
                        Toast.LENGTH_SHORT).show();
            }
        }

        return bss;
    }



    /**
     * Customizable dialog to confirm
     * doing the action with the user
     *
     * @param title
     * @param message
     * @param okText
     * @param after
     */
    private void confirmDialog(String title, String message, String okText, final Callable<Void> after) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), R.style.alertDialog);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(true);
        dialog.setPositiveButton(okText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    after.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }



    /**
     * Show the profile of the current selfie's user
     */
    private void showProfile() {

        fullScreenProgressBar.show();
        DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Users");
        DatabaseReference dbr = OPEN ? root.child(selfiesId.get(viewPager.getCurrentItem())) :
                                root.child(winners.get(viewPager.getCurrentItem()));


        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    UserProfileDialog userProfileDialog =
                            new UserProfileDialog(getContext(), user, R.style.userProfileDialog);
                    userProfileDialog.show();
                } else {
                    Toast.makeText(getContext(),
                            "User account of this selfie is no longer available", Toast.LENGTH_LONG).show();
                }
                fullScreenProgressBar.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        databaseListeners.put(dbr, vel);
        dbr.addListenerForSingleValueEvent(vel);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        competitionDeadlineWatcher.cancel(true);
        competitionDeadlineWatcher.cancelWatcher();
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        cleanMemory();
    }



    /**
     * Fetch Selfies one by one recursively
     * from database and set them in the views
     */
    private void fetchSelfie() {
        if (selfieIndex.get() < selfiesId.size()) {
            ValueEventListener vel = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!halt.get()) {
                        Selfie selfie = dataSnapshot.getValue(Selfie.class);
                        if (selfie != null) {
                            selfiePagerAdapter.addView(getSelfieView(selfie));
                            imageAdapter.addView(bitmaps.get(selfieIndex.get()));
                            selfieIndex.getAndIncrement();
                        } else {// in case a user deleted it
                            selfiesId.remove(selfieIndex.get());
                        }
                        fetchSelfie();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
            DatabaseReference dbr = SELFIES_REFERENCE.child(selfiesId.get(selfieIndex.get()));
            databaseListeners.put(dbr, vel);
            dbr.addListenerForSingleValueEvent(vel);
            if (selfieIndex.get() == (selfiesId.size() - 1)) lastSelfie.set(true);
        }
    }



    /**
     * Toggle the visibility of a given view
     * @param view
     */
    private void toggleVisibility(View view){
        if(view!=null) {
            if (view.getVisibility() == View.VISIBLE) {
                view.setVisibility(View.INVISIBLE);
            } else {
                view.setVisibility(View.VISIBLE);
            }
        }
    }


}