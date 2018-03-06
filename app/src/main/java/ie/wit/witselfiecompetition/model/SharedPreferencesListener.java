package ie.wit.witselfiecompetition.model;

import android.content.SharedPreferences;
import android.util.Log;
import java.util.Map;
import java.util.concurrent.Callable;


/**
 * This class listens to the changes in value given list of fields
 * in a given shared preferences file
 * then invoke a given method once all fields are updated
 * Created by Yahya on 06/03/18.
 */
public class SharedPreferencesListener{

    private SharedPreferences pref;
    private Map<String,?> oldContent;
    private String[] fields;

    /**
     * Constructor of SharedPreferencesListener Class
     * @param fields
     * @param pref
     */
    public SharedPreferencesListener(String[] fields, SharedPreferences pref) {
        this.pref = pref;
        oldContent = pref.getAll();
        this.fields = fields;
    }


    /**
     * Run in a new thread and check for all
     * required updates to happen in the
     * sharedPreferences then invoke the
     * passed method
     * @param method
     */
    public void invokeAfterUpdate (final Callable<Void> method){
        new Thread( new Runnable() {
            @Override
            public void run() {
                while(!updated()){
                    try{
                        Thread.sleep(100); // wait a bit for updates
                    } catch (Exception e){
                        Log.v("sharedPref Listener", e.getMessage());
                    }
                }
                try{
                    method.call();
                }catch (Exception e){
                    Log.v("sharedPref Listener", e.getMessage());
                }
            }
        }).start();
    }


    /**
     * Compare fields equality
     * @return
     */
    private boolean updated(){
        Map<String,?> temp = pref.getAll();
        for(String field : fields){
            Object v1 = oldContent.get(field);
            Object v2 = temp.get(field);
            if(v1==null && v2==null){
                return false;
            }
            if(v1!=null && v2!=null && v1.equals(v2)){
                return false;
            }
        }
        return true;
    }
}
