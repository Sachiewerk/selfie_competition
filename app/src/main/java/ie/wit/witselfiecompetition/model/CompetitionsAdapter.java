package ie.wit.witselfiecompetition.model;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.util.List;

import ie.wit.witselfiecompetition.R;

/**
 * Custom RecyclerView Adapter Class that accepts
 * a Competition View Holder as data type
 * to render the given data on the recycler view
 * and setting the appropriate competition data to each row.
 * Created by yahya Almardeny on 18/03/18.
 */

public class CompetitionsAdapter extends RecyclerView.Adapter<CompetitionsAdapter.CompViewHolder> {

    private List<Competition> competitions;
    private FragmentActivity parent;
    public class CompViewHolder extends RecyclerView.ViewHolder {
        TextView compName, compCloseDate;
        ImageView compOpenImage;


        public CompViewHolder(View item) {
            super(item);
            this.compName = item.findViewById(R.id.compName);
            this.compCloseDate = item.findViewById(R.id.compCloseDate);
            this.compOpenImage = item.findViewById(R.id.compOpenImage);
        }
    }

    public CompetitionsAdapter(List<Competition> competitions, FragmentActivity parent){
        this.competitions = competitions;
        this.parent = parent;
    }

    @Override
    public CompViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.competition_list_row, parent, false);
        return new CompViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(CompViewHolder holder, int position) {
        Competition competition = competitions.get(position);
        holder.compName.setText(competition.getName());
        String closeDate = competition.getCloseDate();
        try {
            if(App.isAfterNow(closeDate)){
                closeDate = "Close on: " + closeDate;
                holder.compOpenImage.setVisibility(View.VISIBLE);
            }
            else{
                closeDate = "Closed on: " + closeDate;
            }
            holder.compCloseDate.setText(closeDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.itemView.setOnClickListener(new OnCompetitionClickListener(competition, parent));

    }


    @Override
    public int getItemCount() {
        return competitions.size();
    }



}
