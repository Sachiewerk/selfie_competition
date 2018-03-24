package ie.wit.witselfiecompetition;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import ie.wit.witselfiecompetition.model.Competition;
import ie.wit.witselfiecompetition.model.CompetitionsAdapter;
import ie.wit.witselfiecompetition.model.DoWithDatabase;
import ie.wit.witselfiecompetition.model.DoWithDatabaseException;


/**
 * TO-DO in CA2
 */
public class CompetitionFragment extends Fragment {

     List<Competition> competitionList  = new ArrayList<>();
     RecyclerView recyclerView;
     CompetitionsAdapter cAdapter;

    public CompetitionFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_competition, null);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Competition");
        recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL)); // divider line between rows

        cAdapter = new CompetitionsAdapter(competitionList, getActivity());
        recyclerView.setAdapter(cAdapter);

        fetchCompetitionData();
    }


    /**
     * fetch data from database and
     * fill them in competitionList
     * clear the list after every change
     * notify data changed after every change
     */
    private void fetchCompetitionData(){
        FirebaseDatabase.getInstance().getReference().child("Competition")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        competitionList.clear();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Competition competition = new Competition();
                            competition.setcId(child.getKey());
                            competition.setName(String.valueOf(child.child("name").getValue()));
                            competition.setOpenDate(String.valueOf(child.child("openDate").getValue()));
                            competition.setCloseDate(String.valueOf(child.child("closeDate").getValue()));
                            competitionList.add(competition);
                        }
                        Collections.reverse(competitionList);
                        cAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

}
