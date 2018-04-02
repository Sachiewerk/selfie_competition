package ie.wit.witselfiecompetition.model;


import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import ie.wit.witselfiecompetition.R;


/**
 * ViewPager Adapter for Selfies in Competition
 */

public class SelfiePagerAdapter extends PagerAdapter {

    private List<View> views = new ArrayList<>();


    @Override
    public int getItemPosition (Object object) {
        int index = views.indexOf (object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }


    @Override
    public Object instantiateItem (ViewGroup container, int position) {
        View v = views.get (position);
        container.addView (v);
        return v;
    }


    @Override
    public void destroyItem (ViewGroup container, int position, Object object) {
        container.removeView (views.get (position));
    }


    @Override
    public int getCount () {
        return views.size();
    }



    @Override
    public boolean isViewFromObject (View view, Object object) {
        return view == object;
    }



    public int addView (View v) {
        return addView (v, views.size());
    }



    public int addView (View v, int position) {
        views.add (position, v);
        notifyDataSetChanged();
        return position;
    }



    public void format(){
        if(views!=null) {
            for (View view : views) {
                if (view != null) {
                    ImageView img = view.findViewById(R.id.selfie);
                    if (img != null) {
                        img.setImageBitmap(null);
                    }
                    if (view.getDrawingCache() != null) {
                        view.getDrawingCache().recycle();
                    }
                }
            }
            views.clear();
            //notifyDataSetChanged();
        }
    }



    public int removeView (ViewPager pager, int position) {
        pager.setAdapter (null);
        views.remove (position);
        pager.setAdapter (this);
        notifyDataSetChanged();
        return position;
    }

}
