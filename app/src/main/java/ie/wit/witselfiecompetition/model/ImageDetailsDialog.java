package ie.wit.witselfiecompetition.model;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import ie.wit.witselfiecompetition.R;

/**
 * Created by yahya Almardeny on 30/03/18.
 */

public class ImageDetailsDialog extends Dialog{

    private Gallery gallery;



    public ImageDetailsDialog(@NonNull Context context, Gallery gallery, int themeResId) {
        super(context, themeResId);
        setContentView(R.layout.gallery_image_details);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        setTitle("Details");

        this.gallery = gallery;
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        loadView();
    }



    private void loadView() {

        // load view
        TextView competitionName = findViewById(R.id.competitionName);
        TextView selfieDate = findViewById(R.id.selfieDate);
        TextView selfieLikes = findViewById(R.id.selfieLikes);
        Button closeButton = findViewById(R.id.closeButton);

        String name = gallery.getCompName();
        String date = gallery.getDate();
        String likes = gallery.getLikes();

        competitionName.setText(name);
        selfieDate.setText(date);
        selfieLikes.setText(likes);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }



}
