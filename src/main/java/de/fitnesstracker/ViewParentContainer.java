package de.fitnesstracker;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.Arrays;
import java.util.List;

/**
* Created by skip on 27.09.2014.
*/
public class ViewParentContainer extends LinearLayout implements AbstractViewGroup, IDummyContainer, ViewGroup.OnHierarchyChangeListener{

    AnimatorOfDummy animatorOfDummyInsider;
    int mParentTopMargins = 0;

    public ViewParentContainer(Context context) {
        super(context);
        init(null);
    }

    public ViewParentContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ViewParentContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        if(attrs != null){
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ViewParentContainer);
            this.mParentTopMargins = array.getInteger(R.styleable.ViewParentContainer_childMarginsTop, mParentTopMargins);
        }

        // pass the information about additon of new Children to the children themselves if they implement OnHierarchyChangeListener
        this.setOnHierarchyChangeListener(this);

        // create the dummy with this as parent
        animatorOfDummyInsider = new AnimatorOfDummy(getContext(), this);
    }

    // ENFORCE OnHierarchyChangeListener as children


    ViewDummyAnimated lastDummy = null;
    OnLayoutChangeListener lastDummyListener = null;
    @Override
    public void addView(final View child, int index, ViewGroup.LayoutParams params) {

        // check whether the view implements the OnHierarchyChangeListener
        if(!(child instanceof  OnHierarchyChangeListener)){
            throw new IllegalArgumentException(String.format("The child %s has to implemement OnHierarchyChangeListener", child.getClass().getSimpleName()));
        }
        super.addView(child, index, params);


        // add listener which would listen for dummy expansion in last item
        // check whether this one is a dummy
        if(child instanceof ViewDummyAnimated){

            // check whether this one is the last child
            boolean isNewLastChild = Utils.getViewIndexInParent(child) == getChildCount()-1;
            if(isNewLastChild){

                // unregister the dummy listener on the previous one
                if(lastDummy != null ){
                    lastDummy.removeOnLayoutChangeListener(lastDummyListener);
                }

                // find the scrollView
                final ScrollView scrollView = (ScrollView)de.fitnesstracker.Context.activity.findViewById((R.id.scrollviewMain));

                // register the layout listener on the new one
                lastDummy = (ViewDummyAnimated) child;

                // when the dummy height changes - make the scrollView completely display the dummy
                lastDummyListener = new OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            int y = child.getBottom();
                            scrollView.scrollTo(scrollView.getScrollX(), y);
                    }
                };
                lastDummy.addOnLayoutChangeListener(lastDummyListener);
            }
        }
    }



    void add(AbstractFigure figure, int index){
        // TODO
    }

    void addAfter(AbstractFigure figure){
        // TODO
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        // notify the filters
        notifyDragFilter(event);

        /*
            TODO: handle onDragEnter only when the stack becomes visible / is visible at dragStart
            this would avoid to moving the whole stack down on every drag
          */

        // notify the dummy
        this.animatorOfDummyInsider.onDrag(this, event);

        /** It is important to call the super.dispatch(). Or the children will never be able to receive drag.         */
        super.dispatchDragEvent(event);

        /** It is important to return "true" here. Or this view will not be marked as one, which wishes to receive drag.*/
        return true;
    }

    /* sends all Drag Events to the DragControler, which gives it the ability to handle on DRAG_START
       and DRAG_END events      */
    private void notifyDragFilter(DragEvent event){
        DragController dragController = de.fitnesstracker.Context.dragController;
        if(dragController != null){
            dragController.notifyDragFilter(this, event);
        }
    }

    // INTERFACES

    @Override
    public void addSuccessor(AbstractFigure figure) {
        // TODO
    }

    @Override
    public void replace(AbstractFigure child) {
        // TODO
    }

    @Override
    public List<AbstractFigure> getChildren() {
        // TODO
        return null;
    }

    @Override
    public void addChild(AbstractFigure child) {
        // TODO
    }

    @Override
    public AbstractFigure getParentAbstractFigure() {
        // TODO
        return null;
    }

    // IDummyContainer


    @Override
    public AnimatorOfDummy findResponsibleAnimatorOfDummy(View view) {
        boolean isReplaceableByInsiderAnimatorOfDummy = UtilDropHandler.isReplaceableByInsiderAnimatorOfDummy(this, view, animatorOfDummyInsider);
        if(isReplaceableByInsiderAnimatorOfDummy){
            return animatorOfDummyInsider;
        }
        return null;
    }

    @Override
    public List<AnimatorOfDummy> getAllAnimatorOfDummy() {
        return Arrays.asList(new AnimatorOfDummy[]{this.animatorOfDummyInsider});
    }

    // ViewGroup.OnHierarchyChangeListener


    @Override
    public void onChildViewAdded(View parent, View child) {
        ((OnHierarchyChangeListener)child).onChildViewAdded(parent, child);

        // add the topMargin
        ((LayoutParams)child.getLayoutParams()).setMargins(0, mParentTopMargins, 0, 0);
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
        ((OnHierarchyChangeListener)child).onChildViewRemoved(parent, child);

        // remove the topMargin
    }
}
