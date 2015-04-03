package de.fitnesstracker;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by skip on 10.09.2014.
 */
public class ViewGroupAnimated extends ViewGroup implements AbstractViewGroup, ViewGroup.OnHierarchyChangeListener, IDragInViewIdentifier, IDummyContainer {

    private List<AbstractFigure> children = new ArrayList<AbstractFigure>();
    private AbstractFigure parent;
    private Context context;
    private AnimatorOfDummy animatorOfDummyInsider;
    private AnimatorOfDummy animatorOfDummyFollower;

    // is triggered when the drag leaves the view or dummy to hide the dummy
    private OnDummyDragOutDummyUnregister onDragOutDummyUnregister;

    private int minHeight = (int) getResources().getDimension(R.dimen.viewgroup_min_height);
    private int minWidth = (int) getResources().getDimension(R.dimen.viewgroup_min_width);
    private int mPadding_left = (int) getResources().getDimension(R.dimen.viewgroup_padding_left);
    private int mPadding_top = (int) getResources().getDimension(R.dimen.viewgroup_padding_top);
    private int mPadding_bottom = (int) getResources().getDimension(R.dimen.viewgroup_padding_bottom);

    private boolean isDraggingOverThis = false;


    public ViewGroupAnimated(Context context) {
        super(context);
        init(context);
    }

    public ViewGroupAnimated(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewGroupAnimated(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    private void init(Context context){
        this.context = context;

        // pass the information about additon of new Children to the children themselves if they implement OnHierarchyChangeListener
        this.setOnHierarchyChangeListener(this);

        // dummy insider - the parent is this
        this.animatorOfDummyInsider = new AnimatorOfDummy(context, this);

        // dummy follower has the same parent as the view. Parent is attached later when the view is attached
        this.animatorOfDummyFollower = new AnimatorOfDummy(context);

        // create a listener which would hide, when drag leaves this view and dummy and add it to the follower
        this.onDragOutDummyUnregister = new OnDummyDragOutDummyUnregister(this, this.animatorOfDummyFollower, new AnimatorOfDummy[]{this.animatorOfDummyInsider, this.animatorOfDummyFollower});
        this.animatorOfDummyFollower.addDummyOnDragListener(this.onDragOutDummyUnregister);

        // create a listener which would start dragging a view when its already on the stack
        this.setOnLongClickListener(new LongClickListenerStartDragFromStack(this));

        setMinimumHeight(minHeight);
        setMinimumWidth(minWidth);

        // padding
        setPadding(mPadding_left, mPadding_top, 0, mPadding_bottom);

        setRandomBg();

        // every ViewGroup should have its own id
        setId(UUID.randomUUID().hashCode());
    }


    @Override
    public boolean isDraggingWithinView() {
        return isDraggingOverThis;
    }

    private void setRandomBg(){
        int r = ((int) (Math.random()*255)) ;
        int g = ((int) (Math.random()*255)) ;
        int b = ((int) (Math.random()*255)) ;
        setBackgroundColor(Color.argb(255, r, g, b));
    }

    @Override
    public AbstractFigure getParentAbstractFigure() {
        return parent;
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
        if(change != null) {
            isDraggingOverThis = change;
        }

        // pass dragEvent to animatorOfDummies
        // they will react on onDragStart, onDragOut, onDragIn
        animatorOfDummyFollower.onDrag(this, event);
        animatorOfDummyInsider.onDrag(this, event);

        if(event.getAction() == DragEvent.ACTION_DRAG_EXITED){
            // delayed reaction on drag-out on ALL dummies and view by hiding
            Log.d(Constants.LOGD, "Drag out");
            onDragOutDummyUnregister.onDrag(this, event);
        }

        /** It is important to call the super.dispatch(). Or the children will never be able to receive drag.         */
        super.dispatchDragEvent(event);

        /** It is important to return "true" here. Or this view will not be marked as one, which wishes to receive drag.*/
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int resultWidth = getMeasuredWidth();
        int resultHeight = getMeasuredHeight();

        Log.d(Constants.LOGD,"---");
        Log.d(Constants.LOGD,"measuredWidth: "+getMeasuredWidth());
        Log.d(Constants.LOGD,"measuredHeight: "+getMeasuredHeight());

        // min height or parent's constraints
        int minWidth = getSuggestedMinimumWidth();
        int minHeight = getSuggestedMinimumHeight();
        int defaultWidth = getDefaultSize(minWidth, widthMeasureSpec);
        int defaultHeight = getDefaultSize(minHeight, heightMeasureSpec);
        Log.d(Constants.LOGD,"minWidth: "+minWidth);
        Log.d(Constants.LOGD,"minHeight: "+minHeight);
        Log.d(Constants.LOGD,"defaultWidth: "+defaultWidth);
        Log.d(Constants.LOGD,"defaultHeight: "+defaultHeight);

        // data passed to us from parent
        int specHeight = MeasureSpec.getSize( heightMeasureSpec );
        int specHeightMode = MeasureSpec.getMode( heightMeasureSpec );
        int specWidth = MeasureSpec.getSize( widthMeasureSpec );
        int specWidthMode = MeasureSpec.getMode( widthMeasureSpec );

        Log.d(Constants.LOGD,"specHeight: "+specHeight);
        Log.d(Constants.LOGD,"specHeightMode: "+specHeightMode);
        Log.d(Constants.LOGD,"specWidth: "+specWidth);
        Log.d(Constants.LOGD,"specWidthMode: "+specWidthMode);


//        MeasureSpec.AT_MOST; // -2147483648
//        MeasureSpec.UNSPECIFIED; // 0
//        MeasureSpec.EXACTLY; // 1073741824

        // measure the children. Have to explicitely call that! Otherwise the children will not measure themselves and will have width and height of 0
        /*
         * - iterates all VISIBLE children
         * - measures them with the given measureSpec (what the parent told us to be!) There will be problems if parent tells to be max. x and all children will try to fill the child
         *  - calls measureChild() which considers padding of the current view
         */
        measureChildren(widthMeasureSpec, heightMeasureSpec);


        int childHeightSum = 0;
        int childWidthMax = 0;
        for(int i=0; i<getChildCount(); i++){
            View child = getChildAt(i);
            childHeightSum += child.getMeasuredHeight();
            childWidthMax = Math.max(childWidthMax, child.getMeasuredWidth());
        }
        // don't forget the padding of this view which was already considered for measuring the child,
        // to make the child smaller, if the space inside this view is limited
        childHeightSum += getPaddingTop() + getPaddingBottom();

        Log.d(Constants.LOGD,"child Count: "+getChildCount());
        Log.d(Constants.LOGD,"childHeightSum: "+childHeightSum);
        Log.d(Constants.LOGD,"childWidthMax: "+childWidthMax);

        // respect the parent's MeasureSpec constraints
        switch (specHeightMode){
            case MeasureSpec.EXACTLY:
                resultHeight = specHeight;
                break;

            case MeasureSpec.UNSPECIFIED:
                resultHeight = childHeightSum;
                break;

            case MeasureSpec.AT_MOST:
                // respect MAX constraints (received from parent)
                resultHeight = Math.min(childHeightSum, specHeight);
                break;
        }

        // respect the MIN constraints (set on view). Asume that minsize is set so that it does not injure parents constrains
        resultHeight = Math.max(minHeight, resultHeight);

        // store the size
        setMeasuredDimension(resultWidth, resultHeight);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = mPadding_left;
        int top=mPadding_top;
        for(int i=0; i<getChildCount(); i++){
            View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            child.layout(left, top, left + width, top + height);
            // increase counter
            top += child.getMeasuredHeight();
        }
    }



    /**
     * The view passed in may have been placed within a special ViewDummy.
     * THis method checks whether it is so, and if yes - it returns the reponsible AnimatorOfDummy
     *
     * @param viewNeedsToBeReplacedByDummy - the view which lays within a dummy
     * @return - the AnimatorOfDummy which is manages the dummy, which's space the view occupies
     */
    @Override
    public AnimatorOfDummy findResponsibleAnimatorOfDummy(View viewNeedsToBeReplacedByDummy){

        // check whether the view is my first child
        boolean  isReplaceableByInsiderAnimatorOfDummy = UtilDropHandler.isReplaceableByInsiderAnimatorOfDummy(this, viewNeedsToBeReplacedByDummy, animatorOfDummyInsider);
        if(isReplaceableByInsiderAnimatorOfDummy){
            return animatorOfDummyInsider;
        }

        // check whether the view is my next sibling
        boolean haveSameParent = (viewNeedsToBeReplacedByDummy.getParent()!=null && viewNeedsToBeReplacedByDummy.getParent().equals(this.getParent()));
        if(haveSameParent){
            boolean isReplaceableByFollowerAnimatorOfDummy = UtilDropHandler.isReplaceableByFollowerAnimatorOfDummy(this, viewNeedsToBeReplacedByDummy, this.animatorOfDummyFollower);
            if(isReplaceableByFollowerAnimatorOfDummy){
                return animatorOfDummyFollower;
            }
        }

        // non of this group's dummies may replace the given view
        return null;
    }

    @Override
    public List<AnimatorOfDummy> getAllAnimatorOfDummy() {
        return Arrays.asList(new AnimatorOfDummy[]{this.animatorOfDummyInsider, this.animatorOfDummyFollower});
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        // if the current child was modified and not some predecessor
        if(child == this){
            int indexInParent = Utils.getPositionInParent(this);
            // this view was added to a new parent - recreate it
            this.animatorOfDummyFollower.attachToParent((ViewGroup)this.getParent(), this);
        }
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
        // if the current child was modified and not some predecessor
        if(child == this){
            // detachFromParent the dummy in old parent
            animatorOfDummyFollower.detachFromParent();
        }
    }


    // should be inside the dummy
    private void onDropInReplaceDummyAndStopCommand(){

    }

    @Override
    public void addView(View child) {
        // check whether the view implements the OnHierarchyChangeListener
        if(!(child instanceof  OnHierarchyChangeListener)){
            throw new IllegalArgumentException(String.format("The child %s has to implemement OnHierarchyChangeListener", child.getClass().getSimpleName()));
        }
        // enforce height of the group to have the value WRAP_CONTENT
        // otherwise the ViewGroup will not react on (animated) changes of child heights
        if(getLayoutParams() != null){
            getLayoutParams().height = LayoutParams.WRAP_CONTENT;
        }

        super.addView(child);
    }



    // INTERFACES

    @Override
    public List<AbstractFigure> getChildren() {
        return null;
    }

    @Override
    public void addChild(AbstractFigure child) {
        // TODO
    }

    @Override
    public void addSuccessor(AbstractFigure figure) {
        // TODO
    }

    @Override
    public void replace(AbstractFigure child) {
        // TODO
    }


    // CLASSES



}
