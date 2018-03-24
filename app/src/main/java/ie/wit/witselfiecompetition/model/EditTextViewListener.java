package ie.wit.witselfiecompetition.model;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * This class listens to the text changes in a given
 * EditText/TextView and calculate the required width
 * to keep the imageView(icon) shown on the side of EditText/TextView
 * Also set the proper EditText/TextView Width onCreate() so it also
 * covers when phone rotates.
 * It accepts LinearLayout and RelativeLayout as parent
 * if parent is null, that means the parent is the phone screen itself
 * Created by Yahya Almardeny on 06/03/18.
 */

public class EditTextViewListener implements TextWatcher {

    private EditText editText;
    private TextView textView;
    private float FONT_WIDTH;
    private int UPPER_BOUND_WIDTH;
    private boolean isEditText = true;


    /**
     * Constructor for EditText
     * @param editText
     * @param editIcon
     * @param parent
     * @param margin
     */
    public EditTextViewListener(EditText editText, View editIcon, @Nullable Layout parent, int margin){
        this.editText = editText;
        load(editIcon, parent, margin);
        fixWidth(editText.getEditableText());
    }



    /**
     * Constructor for TextView
     * @param textView
     * @param editIcon
     * @param parent
     * @param margin
     */
    public EditTextViewListener(TextView textView, View editIcon, @Nullable Layout parent, int margin){
        isEditText = false;
        this.textView = textView;
        load(editIcon, parent, margin);
        fixWidth(new SpannableStringBuilder(textView.getText()));
    }


    /**
     * After Text has changed, fix the proper width
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {
        fixWidth(s);
    }


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }


    /**
     * Load and calculate the required parameters
     * @param editIcon
     * @param parent
     * @param margin
     */
    private void load(View editIcon, @Nullable Layout parent, int margin){
        if(isEditText) {FONT_WIDTH =  editText.getTextSize()/2;}
        else{FONT_WIDTH =  textView.getTextSize()/2;}

        int extras = margin;

        ViewGroup.LayoutParams lp = editIcon.getLayoutParams();

        extras += (lp instanceof LinearLayout.LayoutParams)? ((LinearLayout.LayoutParams) lp).leftMargin
                + ((LinearLayout.LayoutParams) lp).rightMargin
                + editIcon.getPaddingRight() + editIcon.getPaddingLeft()
                : (lp instanceof RelativeLayout.LayoutParams)? ((RelativeLayout.LayoutParams) lp).leftMargin
                + ((RelativeLayout.LayoutParams) lp).rightMargin
                + editIcon.getPaddingRight() + editIcon.getPaddingLeft() : 0;

        UPPER_BOUND_WIDTH = (parent==null)?
                App.getScreenWidth() - (editIcon.getWidth() + extras)
                : parent.getWidth() - editIcon.getMeasuredWidth() + extras;
    }


    /**
     * Calculate the width and fix it
     * Prevent user from adding new line (i.e. clicking enter)
     * @param s
     */
    private void fixWidth(Editable s){
        if ((s.length() * FONT_WIDTH) >= UPPER_BOUND_WIDTH) {
            if(isEditText){
                editText.setWidth(UPPER_BOUND_WIDTH);
            }else{
                textView.setWidth(UPPER_BOUND_WIDTH);
            }
        }

        for(int i = s.length(); i > 0; i--) {
            if(s.subSequence(i-1, i).toString().equals("\n")){
                s.replace(i-1, i, "");
            }
        }

    }



}
