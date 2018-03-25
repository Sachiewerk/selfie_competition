package ie.wit.witselfiecompetition;

import android.annotation.SuppressLint;
import android.content.Context;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ie.wit.witselfiecompetition.model.App;
import ie.wit.witselfiecompetition.model.Competition;
import ie.wit.witselfiecompetition.model.Selfie;


/**
 * Created by yahya Almardeny on 22/03/18.
 */

public class SelfieNavigator extends Fragment{

    ImageView submit_selfie, leftArrow, rightArrow, selfie, likeIcon, likeIconClicked;
    TextView likesTextView;
    FrameLayout back;
    LinearLayout beFirstLabel;
    View  selfie_ActionBar;
    int SCREEN_WIDTH = App.getScreenWidth();
    int SCREEN_HEIGHT = App.getScreenHeight();
    ActionBar actionBar;
    List<Selfie> selfies;
    private Competition competition;


    public SelfieNavigator() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.competition = (Competition) getArguments().getSerializable("competition");
        Log.v("Tag", String.valueOf(competition));


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selfie, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadView();
        showSelfieActionBar();
        setSelfieDemin();
        fillWithData();
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.selfie_menu, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }


    private void loadView(){
        // ActionBar View
        LayoutInflater inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selfie_ActionBar = inflater.inflate(R.layout.selfie_actionbar, null);
        back = selfie_ActionBar.findViewById(R.id.back);
        submit_selfie = selfie_ActionBar.findViewById(R.id.submit_selfie);

        // Fragment Selfie
        leftArrow = getView().findViewById(R.id.leftArrow);
        rightArrow = getView().findViewById(R.id.rightArrow);
        likeIcon = getView().findViewById(R.id.likeIcon);
        likeIconClicked = getView().findViewById(R.id.likeIconClicked);
        likesTextView = getView().findViewById(R.id.likesTextView);
        beFirstLabel = getView().findViewById(R.id.beFirstLabel);
        selfie = getView().findViewById(R.id.selfie);


    }

    private void setSelfieDemin(){
        selfie.getLayoutParams().width = SCREEN_WIDTH -
                (leftArrow.getDrawable().getIntrinsicWidth() + rightArrow.getDrawable().getIntrinsicWidth() + 10);
       selfie.getLayoutParams().height = SCREEN_HEIGHT -
                ((likeIcon.getDrawable().getIntrinsicHeight() + likesTextView.getHeight() +  actionBar.getHeight())*2);
        selfie.requestLayout();
        likesTextView.setText("");
    }


    private void showSelfieActionBar() {

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(selfie_ActionBar);
            toolbar.setNavigationIcon(null);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionBar.setDisplayShowCustomEnabled(false);
                    actionBar.setDisplayShowTitleEnabled(true);
                    toggle.syncState();
                    getActivity().onBackPressed();
                }
            });
        }

        View view = getView();
        //For Back Button Press As Well
        if (view != null) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            if (actionBar != null) {
                                actionBar.setDisplayShowCustomEnabled(false);
                                actionBar.setDisplayShowTitleEnabled(true);
                            }
                            toggle.syncState();
                            getActivity().onBackPressed();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showProfileItem:
                Toast.makeText(getContext(), "Show Profile Item", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.reportItem:
                return true;
            case R.id.deleteItem:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void fillWithData(){

        FirebaseDatabase.getInstance().getReference().child("Selfie").child(competition.getcId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // load all related-to-clicked-competition selfies in a list
                        selfies = new ArrayList<>();

                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Selfie selfie = snapshot.getValue(Selfie.class);
                            if (selfie != null) {selfies.add(selfie);}
                        }

                        try {
                            if(App.isAfterNow(competition.getCloseDate())){ // if open, render data in new intent

                            }else{// get the winner only
                                Collections.sort(selfies, new Comparator<Selfie>() {
                                    @Override
                                    public int compare(Selfie s1, Selfie s2) {
                                        return s1.getLikes().size() > s2.getLikes().size() ? -1 :
                                                s1.getLikes().size() < s2.getLikes().size() ? 1 : 0;
                                    }
                                });
                                Log.v("Tag", String.valueOf(selfies));

                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }


}
