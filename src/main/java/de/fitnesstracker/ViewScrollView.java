package de.fitnesstracker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by skip on 18.02.2015.
 */
public class ViewScrollView extends ScrollView {

    Context context;
    private float OFFSET_WHERE_TO_START_SCROLLING;
    private int touchPositionY;

    public ViewScrollView(Context context) {
        super(context);
        init(context);
    }

    public ViewScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    void init(Context context){
        this.context = context;
        this.OFFSET_WHERE_TO_START_SCROLLING = context.getResources().getDimension(R.dimen.scrollview_up_down_offset);
    }

}
