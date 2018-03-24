package ie.wit.witselfiecompetition.model;

import android.support.v4.app.FragmentActivity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ie.wit.witselfiecompetition.R;
import ie.wit.witselfiecompetition.SelfieNavigator;


/**
 * Created by yahya Almardeny on 21/03/18.
 */

public class OnCompetitionClickListener implements View.OnClickListener {

    private Competition competition;
    private FragmentActivity parent;
    private final FragmentManager fragmentManager;

    public OnCompetitionClickListener(Competition competition, FragmentActivity parent) {
        this.competition = competition;
        this.parent = parent;
        fragmentManager = parent.getSupportFragmentManager();
    }


    @Override
    public void onClick(View v) {
        // load all related-to-clicked-competition selfies in a list
        final List<Selfie> selfies = new ArrayList<>();


        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        FirebaseDatabase.getInstance().getReference().child("Selfie")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Selfie selfie = snapshot.getValue(Selfie.class);
                            // get only related to the clicked-on competition's selfies
                            if(selfie != null && selfie.getcId().equals(competition.getcId())){
                                selfies.add(selfie);
                            }
                        }
                        try {
                            if(App.isAfterNow(competition.getCloseDate())){ // if open, render data in new intent
                                final SelfieNavigator selfieNavigator = new SelfieNavigator();
                                fragmentTransaction.replace(R.id.content_area, selfieNavigator);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();


                            }else{// get the winner only

                                Collections.sort(selfies, new Comparator<Selfie>() {
                                    @Override
                                    public int compare(Selfie s1, Selfie s2) {
                                        return s1.getLikes().size() > s2.getLikes().size() ? -1 :
                                                s1.getLikes().size() < s2.getLikes().size() ? 1 : 0;
                                    }
                                });

                                final SelfieNavigator selfieNavigator = new SelfieNavigator();
                                fragmentTransaction.replace(R.id.content_area, selfieNavigator);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();

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
