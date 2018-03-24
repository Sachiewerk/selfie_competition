package ie.wit.witselfiecompetition.model;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Execute Function(s) on specific data
 * after fetching them from Database
 * Created by yahya Almardeny on 13/03/18.
 */

public class DoWithDatabase{

    private Map<String,Object> data;
    private String node;

    /**
     * Constructor for many fields(keys)
     * @param node
     * @param fields
     */
    public DoWithDatabase(String node, String[] fields){
        this.node = node;
        data = new HashMap<>();
        for(String field : fields){
            data.put(field, new Object());
        }
    }

    /**
     * Constructor for one field(key)
     * @param node
     * @param field
     */
    public DoWithDatabase(String node, String field){
        this.node = node;
        data = new HashMap<>();
        data.put(field, new Object());
    }


    /**
     * Execute and Call a given Callable after
     * fetching required data from Database
     * @param postImplementation
     * @throws DoWithDatabaseException
     */
    public void execute(final Callable<Void> postImplementation) throws DoWithDatabaseException {
        FirebaseDatabase.getInstance().getReference().child(node)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapShot : dataSnapshot.getChildren()) {
                            String field = snapShot.getKey();
                            data.keySet().contains(field); // if we are interested in this field
                            data.put(field, snapShot.getValue());  // add it
                        }
                        // when finish fetching data
                        try {
                            postImplementation.call();
                        } catch (Exception e) {
                            try {
                                throw new DoWithDatabaseException("Error in calling post implementation", e);
                            } catch (DoWithDatabaseException e1) {}
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        try {
                            throw new DoWithDatabaseException("Error in calling post implementation",
                                    databaseError.toException());
                        } catch (DoWithDatabaseException e) {}
                    }
                });
    }



    /**
     * Get given field value
     * from data
     * @param field
     * @return
     */
    public Object getValue(String field){
        return data.get(field);
    }


    /**
     * Return the data fetched from database
     * @return
     */
    public Map<String, Object> getData() {
        return data;
    }


}
