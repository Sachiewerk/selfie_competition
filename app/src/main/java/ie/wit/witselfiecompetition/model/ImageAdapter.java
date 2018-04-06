package ie.wit.witselfiecompetition.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import ie.wit.witselfiecompetition.R;


/**
 * Adapter for the Gallery / GridView Images
 * Created by yahya Almardeny on 31/03/18.
 */

public class ImageAdapter extends BaseAdapter {

    private List<Bitmap> bitmaps;
    private LayoutInflater inflater;
    private boolean gallery;


    public ImageAdapter(Context context, boolean gallery) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        bitmaps = new ArrayList<>();
        this.gallery = gallery;
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
       View view = convertView;
       if(gallery){
           GalleryViewHolder holder;
           if(convertView == null){
               view = inflater.inflate(R.layout.one_gallery_image, parent, false);
               holder = new GalleryViewHolder(view);
               view.setTag(holder);
           }else{
               holder = (GalleryViewHolder) view.getTag();
           }
           holder.galleryImage.setImageBitmap(bitmaps.get(position));

       }else{
           SelfieViewHolder holder;
           if(convertView == null){
               view = inflater.inflate(R.layout.one_selfie, parent, false);
               holder = new SelfieViewHolder(view);
               view.setTag(holder);
           }else{
               holder = (SelfieViewHolder) view.getTag();
           }
           holder.oneSelfie.setImageBitmap(bitmaps.get(position));
       }

        GridView grid = (GridView)parent;
        int size = grid.getColumnWidth();
        view.setLayoutParams(new GridView.LayoutParams(size, size));

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


    private static class SelfieViewHolder{
        ImageView oneSelfie;

        SelfieViewHolder(View view){
            oneSelfie =  view.findViewById(R.id.oneSelfie);
        }
    }

    private static class GalleryViewHolder{
        ImageView galleryImage, highlightedImage;

        GalleryViewHolder(View view){

            galleryImage =  view.findViewById(R.id.galleryImage);
            highlightedImage =  view.findViewById(R.id.highlightedImage);
        }
    }

}