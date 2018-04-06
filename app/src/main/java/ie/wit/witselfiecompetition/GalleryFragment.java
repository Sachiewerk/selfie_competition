package ie.wit.witselfiecompetition;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.ActionBar;

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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


import ie.wit.witselfiecompetition.model.App;
import ie.wit.witselfiecompetition.model.ImageAdapter;
import ie.wit.witselfiecompetition.model.Gallery;
import ie.wit.witselfiecompetition.model.ImageDetailsDialog;
import ie.wit.witselfiecompetition.model.SelfiePagerAdapter;
import ie.wit.witselfiecompetition.model.User;
import ie.wit.witselfiecompetition.model.UserProfileDialog;


/**
 * TO-DO in CA2
 */
public class GalleryFragment extends Fragment {


    private static final int PERMISSION_CODE = 200;
    private long AVAILABLE_MEMORY ;
    private View gallery_ActionBarView;
    private ImageView correctSign, deleteIcon;
    private LayoutInflater inflater;
    private LinearLayout selectTextViewLayout;
    private TextView selectTextView;
    private ActionBar actionBar;
    private MenuItem save, details, select, delete;
    private MenuItem selectOption;
    private PopupMenu popup;
    private Dialog fullScreenProgressBar;
    private DatabaseReference GALLERY_REFERENCE, COMPETITION_REFERENCE, SELFIE_REFERENCE;
    private String USER_ID;
    private AtomicBoolean halt, lastSelfie;
    private ImageAdapter imageAdapter;
    private SelfiePagerAdapter selfiePagerAdapter;
    private AtomicInteger compIndex, deleteId;
    private GridView galleryGridView;
    private ViewPager viewPager;
    private List<Bitmap> bitmaps;
    private List<Gallery> galleryList;
    private List<String> compsId;
    private Map<DatabaseReference, ValueEventListener> databaseListeners;
    private int selfieMemoryShare = -1;
    private List<Integer> selectedImages;
    private boolean selectClicked;

    public GalleryFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        inflater = getActivity().getLayoutInflater();

        fullScreenProgressBar = App.onTopProgressBar(getActivity());

        // get the current user id
        USER_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // get Selfie collection database reference
        GALLERY_REFERENCE = FirebaseDatabase.getInstance().getReference().child("Gallery").child(USER_ID);

        // get Competition database reference
        COMPETITION_REFERENCE = FirebaseDatabase.getInstance().getReference().child("Competition");

        // get Selfie database reference
        SELFIE_REFERENCE = FirebaseDatabase.getInstance().getReference().child("Selfie");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, null);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cleanMemory();
        initializeVariables();
        loadView();
        addActionBarListener();
        initializeData();
        getActivity().setTitle("Gallery");
    }


    /**
     * Initialize the variables that should be
     * re-initialized every time this fragment is refreshed
     */
    private void initializeVariables(){

        compsId = new ArrayList<>();

        // initialize atomic vars to be used in the DB listeners / DB transactions
        compIndex = new AtomicInteger(0);
        halt = new AtomicBoolean(false);
        lastSelfie = new AtomicBoolean(false);
        deleteId = new AtomicInteger(0);

        bitmaps = new ArrayList<>();
        galleryList = new ArrayList<>();

        // initialize the GridView adapter
        imageAdapter = new ImageAdapter(getContext(), true);

        // initialize the ViewPager adapter
        selfiePagerAdapter = new SelfiePagerAdapter();

        // to keep all the listeners DB references
        databaseListeners = new LinkedHashMap<>();

        // reference for all selected images
        selectedImages = new ArrayList<>();
    }


    private void loadView() {

        // Custom ActionBar
        gallery_ActionBarView = inflater.inflate(R.layout.gallery_actionbar, null);
        selectTextView = gallery_ActionBarView.findViewById(R.id.selectTextView);
        correctSign = gallery_ActionBarView.findViewById(R.id.correctSign);
        selectTextViewLayout = gallery_ActionBarView.findViewById(R.id.selectTextViewLayout);
        deleteIcon = gallery_ActionBarView.findViewById(R.id.deleteIcon);


        // View Pager
        viewPager = getView().findViewById(R.id.viewPager);
        viewPager.setAdapter(selfiePagerAdapter);

        // Grid view
        galleryGridView = getView().findViewById(R.id.galleryGridView);
        galleryGridView.setAdapter(imageAdapter);


        // show view pager when user clicks on an image
        galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(selectClicked){
                    ImageView highlighted = v.findViewById(R.id.highlightedImage);
                    if(selectedImages.contains(position)){
                        selectedImages.remove(position);
                        highlighted.setVisibility(View.INVISIBLE);
                        setNoOfSelectedImages();
                    }else{
                        selectedImages.add(position);
                        highlighted.setVisibility(View.VISIBLE);
                        setNoOfSelectedImages();
                    }
                }else{
                    viewPager.setCurrentItem(position);
                    galleryGridView.setVisibility(View.INVISIBLE);
                    viewPager.setVisibility(View.VISIBLE);
                    hideMenu("select");
                    showMenu("save", "delete", "details");
                }
            }
        });

        galleryGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v,int i, long arg3) {
                ImageView highlighted = v.findViewById(R.id.highlightedImage);
                selectedImages.add(i);
                highlighted.setVisibility(View.VISIBLE);
                showCustomActionBar();
                return true;
            }

        });
    }



    private void addActionBarListener(){

        // Custom Action Bar
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        correctSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideCustomActionBar();
                deselectAll();
            }
        });

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
                                galleryGridView.setVisibility(View.VISIBLE);
                                viewPager.setVisibility(View.INVISIBLE);
                                showMenu("select");
                                hideMenu("save", "delete", "details");
                            }
                            else {
                                getActivity().onBackPressed();
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        // Select All Popup Menu
        popup = new PopupMenu(getActivity(), selectTextViewLayout);
        popup.getMenuInflater().inflate(R.menu.select_menu, popup.getMenu());
        selectOption = popup.getMenu().findItem(R.id.selectOptionItem);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getTitle().toString()) {
                    case "Select all":
                        selectOption.setTitle("Deselect all");
                        selectAll();
                        break;
                    case "Deselect all":
                        selectOption.setTitle("Select all");
                        deselectAll();
                        break;
                }
                return true;
            }
        });


        // Click Listener to the Select Area (Layout)
        selectTextViewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.show();
            }
        });


        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = selectedImages.size() > 0 ? "Are you sure that you want to delete these "
                                    + selectedImages.size() + " images?" :
                                    "Are you sure that you want to delete this image?";

                confirmDialog("Confirm Deletion", message, "Delete", new Callable<Void>() {
                    @Override
                    public Void call(){
                        deleteIcon.setVisibility(View.INVISIBLE);
                        deleteImages();
                        return null;
                    }
                });
            }
        });

    }


    /**
     * Initialize Gallery Images Data
     * Get the competitions ids that the current user
     * submitted selfie to.
     * Fetch the images from the gallery
     * fill the view pager and grid view with images
     * that will be filled later from database
     */
    private void initializeData(){
        COMPETITION_REFERENCE.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot compId : dataSnapshot.getChildren()) {
                    List<String> selfiesId = (List<String>) compId.child("selfiesId").getValue();
                    if(selfiesId!=null && selfiesId.contains(USER_ID)) {
                        compsId.add(compId.getKey());
                    }
                }
                if(compsId.size()>0){
                    showMenu("select");
                    fetchData();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        AVAILABLE_MEMORY = Runtime.getRuntime().freeMemory();


    }


    /**
     * Hide menu items
     */
    private void hideMenu(String...items) {
        for(String item : items){
            switch (item.toUpperCase()){
                case "DETAILS":
                    details.setVisible(false);
                    break;
                case "SELECT":
                    select.setVisible(false);
                    break;
                case "DELETE":
                    delete.setVisible(false);
                    break;
                case "SAVE":
                    save.setVisible(false);
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
                case "DETAILS":
                    details.setVisible(true);
                    break;
                case "SELECT":
                    select.setVisible(true);
                    break;
                case "DELETE":
                    delete.setVisible(true);
                    break;
                case "SAVE":
                    save.setVisible(true);
                    break;
            }
        }
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.gallery_menu, menu);

        // a workaround to display icons in menu
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        super.onCreateOptionsMenu(menu, inflater);

        save = menu.findItem(R.id.saveItem);
        details = menu.findItem(R.id.detailsItem);
        select = menu.findItem(R.id.selectItem);
        delete = menu.findItem(R.id.deleteItem);
        hideMenu("save", "details", "select", "delete");
    }


    /**
     * Add implementation to the menu items
     * Select Item(s)
     * Show Selfie Image Details
     * Delete Selected Image(s)
     * Save Selected Image(s) in Gallery
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveItem:
                if (App.shouldAskPermissions()) {
                    askPermissions();
                }else{
                    saveImages();
                }
                return true;

            case R.id.detailsItem:
                showDetails();
                return true;

            case R.id.deleteItem:
                confirmDialog("Confirm Deletion", "Are you sure that you want to delete this image?",
                        "Delete", new Callable<Void>() {
                    @Override
                    public Void call(){
                        deleteIcon.setVisibility(View.INVISIBLE);
                        deleteImage();
                        return null;
                    }
                });
                return true;

            case R.id.selectItem:
                showCustomActionBar();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Fetch Gallery Images and their details
     */
    private void fetchData(){
        // remove the previous DB listener as it's no longer needed
        if (databaseListeners != null) {
            for (Map.Entry entry : databaseListeners.entrySet()) {
                DatabaseReference dbr = (DatabaseReference) entry.getKey();
                ValueEventListener vel = (ValueEventListener) entry.getValue();
                dbr.removeEventListener(vel);
            }
            databaseListeners.clear();
        }
        if(compIndex.get() < compsId.size()){
            ValueEventListener vel = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (halt.get()) return;
                    Gallery gallery = dataSnapshot.getValue(Gallery.class);
                    if (gallery != null) {
                        galleryList.add(gallery);
                        selfiePagerAdapter.addView(getGalleryImageView(gallery));
                        imageAdapter.addView(bitmaps.get(bitmaps.size()-1));
                        compIndex.getAndIncrement();
                    }

                    fetchData();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };

            DatabaseReference dbr = GALLERY_REFERENCE.child(compsId.get(compIndex.get()));
            databaseListeners.put(dbr, vel);
            dbr.addListenerForSingleValueEvent(vel);
            if (compIndex.get() == (compsId.size() - 1)) lastSelfie.set(true);
        }
    }


    /**
     * Create a Gallery ImageView and set the bitmap
     * @param gallery
     * @return
     */
    private View getGalleryImageView(final Gallery gallery) {
        ImageView selfieIm = new ImageView(getContext());
        // decode image string, create bitmap and set it
        byte[] decodedString = Base64.decode(gallery.getImage(), Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = availableBitmapSampleSize(gallery.getImage().length());
        options.inPurgeable = true;
        options.outHeight = App.getScreenHeight();
        options.outWidth = App.getScreenWidth();
        options.inPreferredConfig = Bitmap.Config.RGB_565; // no alpha is required, no transparency factor
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
        bitmaps.add(bitmap);
        selfieIm.setImageBitmap(bitmap);

        selfieIm.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));


        return selfieIm;
    }


    /**
     * To reduce the images sizes
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
            selfieMemoryShare = Math.round(AVAILABLE_MEMORY / ((compsId.size()+1) * 2));
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
                        "If images have poor quality, that's because of the low free memory on your device",
                        Toast.LENGTH_SHORT).show();
            }
        }

        return bss;
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
        if (galleryGridView != null) {
            galleryGridView = null;
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
     * Set the Number of Selected Images
     * And show/hide menus items accordingly
     */
    private void setNoOfSelectedImages(){
        int count = selectedImages.size();
        selectTextView.setText(String.valueOf(count + " Selected"));
        if(count>0){
            deleteIcon.setVisibility(View.VISIBLE);
            showMenu("save");
            String title = count == galleryGridView.getChildCount() ? "Deselect all" : "Select all";
            selectOption.setTitle(title);
        }
        else {
            deleteIcon.setVisibility(View.INVISIBLE);
            hideMenu("save");
            selectOption.setTitle("Select all");
        }
    }


    /**
     * Deselect All Images
     */
    private void deselectAll(){
        for(Integer i : selectedImages){
            galleryGridView.getChildAt(i)
                    .findViewById(R.id.highlightedImage).setVisibility(View.INVISIBLE);
        }
        selectedImages.clear();
        setNoOfSelectedImages();
    }


    /**
     * Select all images
     */
    private void selectAll(){
        int count = galleryGridView.getChildCount();
        selectedImages.clear();
        for(int i=0; i<count; i++){
            selectedImages.add(i);
            galleryGridView.getChildAt(i)
                    .findViewById(R.id.highlightedImage).setVisibility(View.VISIBLE);
        }
        setNoOfSelectedImages();
    }


    /**
     * Show the custom Actionbar
     */
    private void showCustomActionBar(){
        actionBar.setCustomView(gallery_ActionBarView);
        setNoOfSelectedImages();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        selectClicked = true;
    }


    /**
     *Hide the Custom Actionbar
     */
    private void hideCustomActionBar(){
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        setNoOfSelectedImages();
        selectClicked = false;
    }


    /**
     * Delete one or more images from
     * when user invokes delete icon
     * while GridView is being displayed
     */
    private void deleteImages(){
        fullScreenProgressBar.show();
        if(selectedImages.size()>0) {
            final int i = selectedImages.get(0);
            GALLERY_REFERENCE.child(compsId.get(i)).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    SELFIE_REFERENCE.child(compsId.get(i)).child(USER_ID).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            COMPETITION_REFERENCE.child(compsId.get(i)).child("selfiesId").runTransaction(new Transaction.Handler() {
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
                                    imageAdapter.removeView(i);
                                    selfiePagerAdapter.removeView(viewPager, i);
                                    if (bitmaps.get(i) != null && !bitmaps.get(i).isRecycled()) {
                                        bitmaps.get(i).recycle();
                                        bitmaps.remove(i);
                                    }
                                    compsId.remove(i);
                                    galleryList.remove(i);
                                    normalizeSelectionPositions();
                                    deleteImages();
                                }
                            });
                        }
                    });
                }
            });
        }
        else {
            if(compsId.size()==0) hideMenu("select");
            correctSign.callOnClick();
            for(int i=0; i<galleryGridView.getChildCount(); i++){
                galleryGridView.getChildAt(i)
                        .findViewById(R.id.highlightedImage).setVisibility(View.INVISIBLE);
            }
            fullScreenProgressBar.dismiss();
        }
    }


    /**
     * Delete one image when
     * user invokes delete menu
     * while ViewPager is being displayed
     */
    private void deleteImage(){
        fullScreenProgressBar.show();
        final int i = viewPager.getCurrentItem();
        GALLERY_REFERENCE.child(compsId.get(i)).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                SELFIE_REFERENCE.child(compsId.get(i)).child(USER_ID).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        COMPETITION_REFERENCE.child(compsId.get(i)).child("selfiesId").runTransaction(new Transaction.Handler() {
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
                                imageAdapter.removeView(i);
                                selfiePagerAdapter.removeView(viewPager, i);
                                if (bitmaps.get(i) != null && !bitmaps.get(i).isRecycled()) {
                                    bitmaps.get(i).recycle();
                                    bitmaps.remove(i);
                                }
                                compsId.remove(i);
                                if(compsId.size()==0) hideMenu("select");
                                correctSign.callOnClick();
                                fullScreenProgressBar.dismiss();
                            }
                        });
                    }
                });
            }
        });
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
     * Show the details of the selected selfie image
     */
    private void showDetails() {

        ImageDetailsDialog imageDetailsDialog =
                new ImageDetailsDialog(getContext(), galleryList.get(viewPager.getCurrentItem()), R.style.Details_Dialog);
        imageDetailsDialog.show();
    }


    /**
     * Remove first selection and adjust position
     * i.e normalize
     */
    private void normalizeSelectionPositions(){
        for(int i=0; i<selectedImages.size(); i++){
            selectedImages.set(i, selectedImages.get(i)-1);
        }
        selectedImages.remove(0);
    }


    /**
     * Save the selected images in case the GridView is displayed
     * Or the current Image in case the ViewPager displayed
     */
    private void saveImages(){
        fullScreenProgressBar.show();
        String message;
        if(viewPager.getVisibility() == View.VISIBLE){
            int position = viewPager.getCurrentItem();
            String imageName = galleryList.get(position).getCompName() + "_"
                    + galleryList.get(position).getDate() + ".jpg";
            saveImageFile(bitmaps.get(position), imageName);
            message = "The image has been saved on your phone successfully";
        }
        else {
            for (Integer position : selectedImages) {
                String imageName = galleryList.get(position).getCompName() + "_"
                        + galleryList.get(position).getDate() + ".jpg";
                saveImageFile(bitmaps.get(position), imageName);
            }
            if(selectedImages.size()==1) {
                message = "The image has been saved on your phone successfully";
            }else{
                message = "The images have been saved on your phone successfully";
            }
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), R.style.alertDialog);
        dialog.setTitle("Information");
        dialog.setMessage(message);
        dialog.setCancelable(true);
        dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dialog.show();
        if(selectedImages.size()>0) {correctSign.callOnClick();}
        fullScreenProgressBar.dismiss();
    }


    /**
     * Save bitmap image on external storage
     * @param bitmap
     * @param fileName
     */
    private void saveImageFile(Bitmap bitmap, String fileName) {

        final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/witSelfieCompetition/");
        if(!path.exists()) { path.mkdirs(); }

        final File file = new File(path, fileName.replace(" ", "_").replace("/", "_"));
        if (file.exists()) { file.delete(); }

        try {
            final FileOutputStream fos = new FileOutputStream(file);
            final BufferedOutputStream bos = new BufferedOutputStream(fos, 8192);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            MediaScannerConnection.scanFile(getContext(), new String[]{file.getPath()}, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        cleanMemory();
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        requestPermissions(permissions, PERMISSION_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImages();
            } else {
                Toast.makeText(getActivity(), "No permission to save image!", Toast.LENGTH_SHORT).show();
            }
        }
    }





}
