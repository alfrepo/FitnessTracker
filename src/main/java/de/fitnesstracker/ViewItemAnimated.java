package de.fitnesstracker;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by skip on 20.09.2014.
 */
public class ViewItemAnimated extends RelativeLayout implements AbstractFigure, ViewGroup.OnHierarchyChangeListener, IDragInViewIdentifier, IDummyContainer{

    private Context context;
    private AbstractFigure parent;
    private int heightFixed =  (int)getResources().getDimension(R.dimen.viewitem_fixed_height);
    private AnimatorOfDummy animatorOfDummy;
    private boolean isDraggingOverThis = false;

    private TextView textView;
    private ImageView imageView;

    private OnDummyDragOutDummyUnregister onDragOutDummyUnregister;

    public ViewItemAnimated(Context context) {
        super(context);
        init(context);
    }

    public ViewItemAnimated(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewItemAnimated(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public boolean isDraggingWithinView() {
        return isDraggingOverThis;
    }

    private void init(Context context){
        this.context = context;

        // create a dummy, but do not attach it to a parent yet. Will attach later
        this.animatorOfDummy = new AnimatorOfDummy(context);

        // create a listener which will unregister the dummy on drag out of this view and dummy
        // add it to the dummy then
        this.onDragOutDummyUnregister = new OnDummyDragOutDummyUnregister(this, this.animatorOfDummy, new AnimatorOfDummy[]{this.animatorOfDummy});
        this.animatorOfDummy.addDummyOnDragListener(this.onDragOutDummyUnregister);

        // create a listener which would start dragging a view when its already on the stack
        this.setOnLongClickListener(new LongClickListenerStartDragFromStack(this));

        // set layout
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_item_animated, this);

        // retrieve the embedded views
        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);

        setRandomBg();

        // every ViewGroup should have its own id
        setId(UUID.randomUUID().hashCode());
    }

    @Override
    public AbstractFigure getParentAbstractFigure() {
        return parent;
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        // beware ! The attachement/ detachement may have happened with one of the predescessors. Check if the child is the current view
        if(child == this){
            animatorOfDummy.attachToParent((ViewGroup)this.getParent(), this);
        }
    }


    @Override
    public void onChildViewRemoved(View parent, View child) {
        // beware ! The attachement/ detachement may have happened with one of the predescessors. Check if the child is the current view
        if(child ==this){
            // detachFromParent the dummy in old parent
            animatorOfDummy.detachFromParent();
        }
    }

    private OnDragListener onDragListener = null;
    @Override
    public void setOnDragListener(OnDragListener l) {
        // remember that we already have an onDragListener. Do not allow to override it!
        // l == null is the possibility to remove the onDragListener
        // onDragListener no listener was defined yet
        if(l == null || onDragListener == null){
            onDragListener = l;
            super.setOnDragListener(l);
        }else{
            throw new IllegalStateException("There already is an onDragListener");
        }
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        // check, whether we are dragging over the dummy
        Boolean change = Utils.isDraggingOverFromDragEvent(event);
        if(change != null){
            isDraggingOverThis = change;
        }

        // handle onDragStart, enter, end,
        this.animatorOfDummy.onDrag(this, event);

        // react on drag by manipulating the dummy
        if(event.getAction()==DragEvent.ACTION_DRAG_EXITED){
            onDragOutDummyUnregister.onDrag(this, event);
        }

        return true;
    }


    @Override
    public AnimatorOfDummy findResponsibleAnimatorOfDummy(View view) {
        if( UtilDropHandler.isReplaceableByFollowerAnimatorOfDummy(this, view, animatorOfDummy) ){
            return animatorOfDummy;
        }

        return  null;
    }
    @Override
    public List<AnimatorOfDummy> getAllAnimatorOfDummy() {
        return Arrays.asList(new AnimatorOfDummy[]{this.animatorOfDummy});
    }

    private void onDragOutRemoveDummyAnimation(){
        if(animatorOfDummy != null){
             animatorOfDummy.onDragOutDragEndRemoveDummyAnimation();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specHeight = MeasureSpec.makeMeasureSpec(heightFixed, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, specHeight);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    }

    private void setRandomBg(){
        int r = ((int) (Math.random()*255)) ;
        int g = ((int) (Math.random()*255)) ;
        int b = ((int) (Math.random()*255)) ;
        View mainView = findViewById(R.id.linearLayout);
        mainView.setBackgroundColor(Color.argb(255, r, g, b));

        String random = String.valueOf(Math.random());
        textView.setText(random);
    }

}
