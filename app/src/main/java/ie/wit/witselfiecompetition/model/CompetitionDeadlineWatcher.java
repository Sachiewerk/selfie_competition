package ie.wit.witselfiecompetition.model;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;


import java.text.ParseException;
import java.util.concurrent.Callable;

import ie.wit.witselfiecompetition.CompetitionFragment;
import ie.wit.witselfiecompetition.R;

/**
 * Async Task to watch the open competition close date
 * while user is navigating selfies, if deadline passed,
 * the Selfie Navigator will close and will take user back
 * to Competition Fragment
 * Created by Yahya Almardeny on 30/03/18.
 */

public class CompetitionDeadlineWatcher extends  AsyncTask<String, Void, Boolean> {

    private Callable <Void> callable;
    private volatile boolean isOpen;

    public CompetitionDeadlineWatcher(Callable <Void> callable) {
        this.callable = callable;
        isOpen = true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        try {
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        isOpen = false;
    }


    @Override
    protected Boolean doInBackground(String... params) {
        String closeDate =params[0];
        while(App.isAfterNow(closeDate) && isOpen) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public void cancelWatcher(){
        this.isOpen = false;
    }



}
