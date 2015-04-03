package de.fitnesstracker;

import android.content.ClipData;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import de.fitnesstracker.dragfilters.IDragFilterContainer;
import de.fitnesstracker.dragfilters.IOnDragFilter;



/*
    TODO this class should implement an own dragShadow which would be movable programmatically
    to let it move to the source on drag fail
 */

/**
 * Controls the dragShadow.
 * Is notified about drag & drop.
 *
 * Created by skip on 04.01.2015.
 */
public class DragController implements IDragFilterContainer {

    private boolean isDropSuccessfullyDone = false;
    private Runnable doWhenDropFails;
    private DragEvent startDragEvent;

    // CACHE of items
    private View viewFromWhichWeStartedDragging;
    /* Need to remember this to be able to put the view back when drag fails */
    private AnimatorOfDummy animatorOfDummyWhichReplacesDraggedView;
    /*  when dragging a View hierarchy - store it here
        single Views may easily be remembered by id.
        TODO: implement converting any ViewHierarchy to XML and back for persistance and storing in ClipData
     */
    private View currentlyDraggingThisView = null;

    /** List of drag filters which will be notified about drag events. FIlter */
    private List<IOnDragFilter> dragFilters = new ArrayList<IOnDragFilter>();

    // this listener will react on drag end
    private IOnDragFilter onDragEndListener = new IOnDragFilter() {
        @Override
        public boolean onDrag(View v, DragEvent event) {

            if(event.getAction() == DragEvent.ACTION_DRAG_STARTED && animatorOfDummyWhichReplacesDraggedView!=null && currentlyDraggingThisView!=null){
                onDragStarted(event);
            }

            if(event.getAction() == DragEvent.ACTION_DRAG_ENDED){
                onDragEnded(event);
            }
            Log.d(Constants.LOGD, " Drag event in DragController: "+event.getAction());
            return true;
        }
    };

    public static DragController create(){
        if(Context.dragController != null) {
            // stop previous drag
            Context.dragController.onDragEnded(null);
        }
        Context.dragController = new DragController();
        return Context.dragController;
    }

    private ClipData createDragClipData(Integer ressourceIdDraggedView) {
        Log.d(Constants.LOGD, String.valueOf(ressourceIdDraggedView));
        return ClipData.newPlainText(UtilDropHandler.CLIPDATA_KEY_DRAG_VIEW_RESSOURCE_ID, String.valueOf(ressourceIdDraggedView) );
    }

    public View createViewFromClipData(android.content.Context context, ClipData clipData, LayoutInflater layoutInflater) {

        // check the label
        if(!clipData.getDescription().getLabel().equals(UtilDropHandler.CLIPDATA_KEY_DRAG_VIEW_RESSOURCE_ID)){
            throw new IllegalArgumentException("Wrong clipData passed");
        }

        // retrive the ressourceId
        String text = clipData.getItemAt(0).getText().toString();
        int clipDataId = Integer.valueOf(text);

        // are we dragging a whole view stack?
        if(this.currentlyDraggingThisView != null && clipDataId == this.currentlyDraggingThisView.getId() ){
            return this.currentlyDraggingThisView;
        }

        // Inflate from ressourceId
        if(clipDataId == R.layout.view_group_animated){
            ViewGroupAnimated v = new ViewGroupAnimated(context);
            /*  it is important that the ViewGroup has this LayoutParams.
                otherwise it won't adopt it's size when it's children should grow larger than the viewGroup
                ACHTUNG: just doing
                    LinearLayout.LayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                does not work
              */
            v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return v;

        }else if(clipDataId == R.layout.view_item_animated){
            return new ViewItemAnimated(context);
        }

        // the View which needs layout
        return layoutInflater.inflate(clipDataId, null);
    }


    public void startDragFromToolbar(View viewFromWhichWeStartedDragging, int shadowRessourceId, int shadowWidth, int shadowHeight, LayoutInflater layoutInflater){
        Log.d(Constants.LOGD, "startDragFromStack - dragging item with ressourceId "+shadowRessourceId);

        // which data to pass on drop
        ClipData clipData = createDragClipData(shadowRessourceId);

        //  measure the item view and use it as shadow
        View.DragShadowBuilder dragShadowBuilder = UtilDropHandler.createDragShadowBuilder(shadowRessourceId, layoutInflater, shadowWidth, shadowHeight);

        // does not replace the dragged view by a dummy
        startDrag(viewFromWhichWeStartedDragging, clipData, dragShadowBuilder);

        // when drag fails - moves the shadow to the toolbar and make it disappear
        doWhenDropFails(new Runnable() {
            @Override
            public void run() {
                // TODO implement moving the DragShadow to the ToolBar

            }
        });
    }

    /**
     * You can not start drag on views detached from parent.
     * It will reult in a NullPointerException.
     * @param currentlyDraggingThisView
     * @param startDragOnThis
     */
    public void startDragFromStack(View currentlyDraggingThisView, View startDragOnThis){
        Log.d(Constants.LOGD, "startDragFromStack - dragging item "+currentlyDraggingThisView.getClass().getSimpleName());

        // TODO me - implement converting between xml and ViewHierarchies. Pack those data into the ClipData instead saving Views in vars
        // which data to pass on drop
        Assert.assertTrue(currentlyDraggingThisView.getId() != View.NO_ID);
        ClipData clipData = createDragClipData(currentlyDraggingThisView.getId());
        this.currentlyDraggingThisView = currentlyDraggingThisView;

        //  measure the item view and use it as shadow
        int shadowWidth = currentlyDraggingThisView.getMeasuredWidth();
        int shadowHeight = currentlyDraggingThisView.getMeasuredHeight();
        View.DragShadowBuilder dragShadowBuilder = UtilDropHandler.createDragShadowBuilder(currentlyDraggingThisView, shadowWidth, shadowHeight);

        // when drag fails - moves the shadow to the dummy. Replaces the dummy by the view again
        // creates DragStartEvents within AnimatorOfDummy objects
        startDrag(startDragOnThis, clipData, dragShadowBuilder);

        //TODO me - move that into a special listener which would react on dragStart
        // replaces the dragged view by a dummy. Will only work if done after drag-start, because DragStartEvents are required to initiate dummies
        replaceDraggedViewByDummy(currentlyDraggingThisView);

        Log.d(Constants.LOGD, "startDrag was executed - replaceDraggedViewByDummy too");
    }

    /** What to do when drop fails? The runnable will be executed on the UI thread*/
    public void doWhenDropFails(Runnable runnable){
        // e.g. move the shadow back to toolbar, make it disapear
        // e.g. move the shadow back to the dummy which has replaced the stack we a re dragging and replace the dummy back again

        // TODO me implement a list of runnables!
        doWhenDropFails = runnable;
    }


    public void onDrop(View viewWhichHandlesDrop, DragEventDecorator dragEvent){
        // remember that the drop was done
        isDropSuccessfullyDone = true;

        //TODO me move the shadow to the view, which handles the drop

    }


    // IDragFilterContainer

    @Override
    public void addDragFilter(IOnDragFilter filter) {
        dragFilters.add(filter);
    }

    @Override
    public void removeDragFilter(IOnDragFilter filter) {
        dragFilters.remove(filter);
    }

    @Override
    public List<IOnDragFilter> getDragFilters() {
        return dragFilters;
    }

    //TODO: every Root View class should call this. Only then it will be possible to capture drag Start / end events
    @Override
    public void notifyDragFilter(View view, DragEvent dragEvent) {
        if(dragFilters.isEmpty()) return;
        for(IOnDragFilter filter:new ArrayList<IOnDragFilter>(dragFilters)){
            filter.onDrag(view, dragEvent);
        }
    }


    // PRIVATE

    /** Inits the dragStart     */
    private void startDrag(View viewFromWhichWeStartedDragging, ClipData clipData, View.DragShadowBuilder dragShadowBuilder){

        // remember the view which we started the drag on. Will need it to post processes on UI thread
        this.viewFromWhichWeStartedDragging = viewFromWhichWeStartedDragging;

        // listen for all drag events in the Activity
        this.addDragFilter(onDragEndListener);

        // start dragging
        viewFromWhichWeStartedDragging.startDrag(clipData, dragShadowBuilder, null, 0);

        // TODO try to hide the bar
        Context.activity.getActionBar().hide();
    }

    private void onDragStarted(DragEvent dragStartEvent){
        // remember the start drag event to init the dummy in order to undo the drag
        this.startDragEvent = dragStartEvent;
    }

    /**
     * The view on which View.startDrag  is done - is responsible for notifying the DragController about dragEnd
     * @param dragEvent
     */
    void onDragEnded(DragEvent dragEvent){

        // TODO try to hide the bar
        Context.activity.getActionBar().show();

        // the fallback when drop fails
        if(isDropSuccessfullyDone){
            // drop is implemented in the View which the drop is done on

        } else{
            // DRAG failed!

            // move the shadow back and replace the dummy
            if( this.currentlyDraggingThisView!=null && this.animatorOfDummyWhichReplacesDraggedView != null ){

                // DRAG failed! move the dragged view back to it's dummy
                // TODO

                // replace the dummy by view
                ViewDummyAnimated  viewDummyAnimated = animatorOfDummyWhichReplacesDraggedView.getViewDummyAnimated();

                // REPLACING IS WRONG! It removes the dummy from its parent! Do teh same what happens on drop!
                // create a simulated drop event
                DragEventDecorator dropEvent = DragEventDecorator.obtain( createDragClipData(this.currentlyDraggingThisView.getId()), 0,0);
                // do the drop on the old dummy
                viewDummyAnimated.onDropOnDummy(dropEvent);
            }

            // fallback runnable
            if(this.doWhenDropFails!=null){
                viewFromWhichWeStartedDragging.post(this.doWhenDropFails);
            }
        }

        // reEnable the animatorIfDummy if it was disabled during dragging
        if(animatorOfDummyWhichReplacesDraggedView != null){
            animatorOfDummyWhichReplacesDraggedView.setEnabled(true);
            animatorOfDummyWhichReplacesDraggedView.onDragOutDragEndRemoveDummyAnimation();
            animatorOfDummyWhichReplacesDraggedView.onDragEnded();
            animatorOfDummyWhichReplacesDraggedView = null;
        }

        // CLEANUP

        // remove the listener which notifies us about drag end
        this.removeDragFilter(onDragEndListener);

        // forget the dragged View
        this.currentlyDraggingThisView = null;

        // self destroy
        Context.dragController = null;
    }



    /** When one drags a view out of the stack - the stack is replaced by dummy */
    private void replaceDraggedViewByDummy(View draggedView){

        Assert.assertTrue("The old animatorOfDummyWhichReplacesDraggedView should be empty here", animatorOfDummyWhichReplacesDraggedView==null);
        animatorOfDummyWhichReplacesDraggedView = findResponsibleAnimatorOfDummy(draggedView);
        Assert.assertTrue("Did not find any AnimatorOfDummy which could provide a dummy to replace dragged view", animatorOfDummyWhichReplacesDraggedView!=null);

        // create a new drag Shadow which would look like the stack
        // TODO me implement an own shadow which we will push around programmatically

        // replace the View by a dummy

              // 0. whats the position of this view within the parent?
              int positionInParent = Utils.getViewIndexInParent(draggedView);

              // 1. remove the dragged view from it's parent in an not animated way
              Utils.removeFromParent(draggedView);

              // 2. put the dragEvent into the animator
              animatorOfDummyWhichReplacesDraggedView.onDragStarted(startDragEvent);

              // 3. show the dummy as usual on drag in
              animatorOfDummyWhichReplacesDraggedView.onDragInAddDummyAnimation();

            // pin the dummy by switching it into manual mode. It should not disappear on drag out! It will be deactivated manually on drag end
            animatorOfDummyWhichReplacesDraggedView.setEnabled(false);
    }

    /* Searching the sibling and parent for the dummy */
    private AnimatorOfDummy findResponsibleAnimatorOfDummy(View draggedView){
        AnimatorOfDummy result = null;
        ViewGroup viewParent = (ViewGroup) draggedView.getParent();
        int childCount = viewParent.getChildCount();

        // check siblings. ask every sibling IDummyContainer whether it has an AnimatorOfDummy for us
        for(int i = 0; i<childCount; i++){
            View viewSibling = viewParent.getChildAt(i);
            if(viewSibling instanceof IDummyContainer){
                result = ((IDummyContainer)viewSibling).findResponsibleAnimatorOfDummy(draggedView);
                if(result != null){
                    return result;
                }
            }
        }


        // check the parent
        if(viewParent!=null && viewParent instanceof IDummyContainer){
            result = ((IDummyContainer)viewParent).findResponsibleAnimatorOfDummy(draggedView);
            // if found - return it!
            if(result != null){
                return result;
            }
        }

        return null;
    }



}
