package ie.wit.witselfiecompetition.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by yahya on 13/03/18.
 */

public class DoWithDatabase implements Callable<Void>{

    private Map<String,Object> data;
    private Callable<Void> callable;
    private String node;

    public DoWithDatabase(String node, String[] fields){
        this.node = node;
        data = new HashMap<>();
        for(String field : fields){
            data.put(field, new Object());
        }
    }

    public DoWithDatabase(String node, String field){
        this.node = node;
        data = new HashMap<>();
        data.put(field, new Object());
    }


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

    public Object getValue(String field){
        return data.get(field);
    }

    public Map<String, Object> getData() {
        return data;
    }


    @Override
    public Void call() throws Exception {
        this.callable.call();
        return null;
    }
}
