package ie.wit.witselfiecompetition.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import ie.wit.witselfiecompetition.R;


/**
 * Created by yahya Almardeny on 31/03/18.
 */

public class ImageAdapter extends BaseAdapter {

    private List<Bitmap> bitmaps;
    private LayoutInflater inflater;

    public ImageAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        bitmaps = new ArrayList<>();
    }

    public int getCount() {
        if(bitmaps!=null){
            return bitmaps.size();
        }else{
            return 0;
        }
    }

    public Object getItem(int position) {
        return bitmaps.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
       /* View view = convertView;
        ViewHolder holder;
        if(convertView == null){
            view = inflater.inflate(R.layout.one_selfie, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        holder.oneSelfie.setImageBitmap(bitmaps.get(position));

        return view;*/
        View view = inflater.inflate(R.layout.one_selfie, parent, false);
        ImageView oneSelfie =  view.findViewById(R.id.oneSelfie);
        ProgressBar oneSelfieProgressbar = view.findViewById(R.id.oneSelfieProgressbar);
        oneSelfie.setImageBitmap(bitmaps.get(position));

        return view;
    }


    public void addView(Bitmap bitmap){
        this.bitmaps.add(bitmap);
        notifyDataSetChanged();
    }

    public void addView(Bitmap bitmap, int position){
        this.bitmaps.add(position, bitmap);
        notifyDataSetChanged();
    }

    public void removeView(int position){
        bitmaps.remove(position);
        notifyDataSetChanged();
    }

    public void setView(Bitmap bitmap, int position){
        this.bitmaps.set(position, bitmap);
        notifyDataSetChanged();
    }

    public void format(){
        if(bitmaps!=null){
            for(Bitmap bitmap : bitmaps){
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
            bitmaps = null;
        }
    }


    private static class ViewHolder{
        ImageView oneSelfie;
        ProgressBar oneSelfieProgressbar;

        ViewHolder(View view){
            oneSelfie =  view.findViewById(R.id.oneSelfie);
            oneSelfieProgressbar = view.findViewById(R.id.oneSelfieProgressbar);
        }
    }

}