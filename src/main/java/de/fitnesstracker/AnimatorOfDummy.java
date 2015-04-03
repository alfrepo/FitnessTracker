package de.fitnesstracker;

import android.content.Context;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import de.fitnesstracker.command.AbstractCommandModifyViewParameter;
import de.fitnesstracker.command.CommandGrowViewHeight;
import de.fitnesstracker.command.CommandGrowViewParameters;
import de.fitnesstracker.command.IListenerCommand;
import de.fitnesstracker.command.ListenerCommandAdapter;


/**
 * This class is needed, because the {@link ViewItemAnimated} and {@link ViewGroupAnimated}
 * both have dummies, which has to be attached to different parents.
 * Both have some logic to add, remove the dummy on DragEvents.
 * This class encapsulates this common logic.
 *
 * <ul>
 *  <li> Creates a dummy lazily in an animated way when the method {@link #onDragInAddDummyAnimation} is called
 *  <li> Removes the dummy in an animated way, when method {@link #onDragOutDragEndRemoveDummyAnimation} is called
 *  <li> Implements multiple drag listeners on a dummy
 *  <li> Uses the command to animate the dummy
 * </ul>
 * Created by skip on 08.11.2014.
 */
public class AnimatorOfDummy implements IDragInViewIdentifier, View.OnDragListener {

    public int INITIAL_DUMMY_HEIGHT_BEFORE_EXPANDING;
    public int DUMMY_HEIGHT;
    public static final int DUMMY_ANIMATION_DURATION =  Constants.DUMMY_TIME_ANIMATION_DURATION_MS; // ms

    // manual mode - in this mode the dummy is controlled explicitely not by drag events
    private boolean isEnabled = true;

    // retrieve
    private Context context;
    private ViewGroup parentOfDummy;

    // the dummy which is added on drag in and removed on out
    private ViewDummyAnimated viewDummyAnimated = null;

    // command which is used to animate the dummy
    private AbstractCommandModifyViewParameter commandGrowView;

    // last event which may be passed to the dummy
    private DragEvent dragEvent;

    // dragListeners which are registered on the dummy
    private List<View.OnDragListener> dummyOnDragListeners = new ArrayList<View.OnDragListener>();

    // command listeners which are registered to the command which makes dummy expand
    private List<IListenerCommand> commandExpandDummyListeners = new ArrayList<IListenerCommand>();

    // after which view should the dummy be appended
    private View predescessorView = null;


    public AnimatorOfDummy(Context context, ViewGroup parentOfDummy){
        this(context, parentOfDummy, null);
    }

    /**
     * Create a new AnimatorOfDummy
     * @param context - the context
     * @param parentOfDummy - the parent which dummies will be added to
     * @param predecessorView - the predescessor of dummy in parent. May be null
     * */
    public AnimatorOfDummy(Context context, ViewGroup parentOfDummy, View predecessorView){
        this(context);
        attachToParent(parentOfDummy, predecessorView);
    }

    /**
     * Use this constructor only with method #attachToParent, when the parent is not known
     * at creation time.
     * Call #attachToParent as soon as the parent is known, otherwise animation will not be possible
     * @param context
     */
    public AnimatorOfDummy(Context context){
        this.context = context;
        init();
    }

    private void init(){
        DUMMY_HEIGHT = (int) context.getResources().getDimension(R.dimen.dummy_height); // fixed height of dummies
        INITIAL_DUMMY_HEIGHT_BEFORE_EXPANDING = (int) context.getResources().getDimension(R.dimen.dummy_initial_before_expanding);

        /* Listen for drag end to deactivate the Listener */
        addDummyOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                // exit if there is not dummy or if the dummy is closed
                if(viewDummyAnimated == null || commandGrowView==null || viewDummyAnimated.getHeight() == 0){
                    return false;
                }
                // check for drag end and hide the dummy
                if(event.getAction() == DragEvent.ACTION_DRAG_ENDED){
                    onDragOutDragEndRemoveDummyAnimation();
                }
                return false;
            }
        });
    }

    /**
     * Attachement to a new parent. Happens if a dummy is moved from one Parent to another,
     * together with it's view
     * @param parentOfDummy
     * @param predecessorView - predecessor of dummy, may be null
     */
    public void attachToParent(ViewGroup parentOfDummy, View predecessorView){
        // first detach from parent
        detachFromParent();

        // reattach to the new parent
        this.parentOfDummy = parentOfDummy;

        // cache the new position
        setPredescessor(predecessorView);

        // if the dummy already exists reattach it too
        if(viewDummyAnimated != null){
            initDummy();
        }
    }

    /** Cancel the animation and remove the dummy from parent. */
    public void detachFromParent(){
        // forget the parent
        parentOfDummy = null;

        // skip if there is no dummy yet
        if(viewDummyAnimated != null){

            // remove the dummy from parent
            ViewParent viewParent = viewDummyAnimated.getParent();
            if(viewParent!=null){
                ((ViewGroup)viewParent).removeView(viewDummyAnimated);
            }
        }

        // undo animation
        if(commandGrowView != null){
            this.commandGrowView.cancel();
        }
    }

    /**
     * Will Handle ACTION_DRAG_STARTED, ACTION_DRAG_ENTERED, ACTION_DRAG_ENDED
     * @param v - the view which owns this anmator. EIther a ViewItem or a ViewGroup
     * @param event
     * @return
     */
    @Override
    public boolean onDrag(View v, DragEvent event) {
        // react on drag by manipulating the dummy
        if(event.getAction() == DragEvent.ACTION_DRAG_STARTED ){
            onDragStarted(event);

        }else if(event.getAction() == DragEvent.ACTION_DRAG_ENTERED){
            Log.d(Constants.LOGD, "Drag in");
            onDragInAddDummyAnimation();


        } else if(event.getAction() == DragEvent.ACTION_DRAG_ENDED ){
            onDragEnded();

        }

        return true;
    }

    /** The AnimatorOfDummy has to be notified about drag start, to remember the DragEvent.
     *  It is needed to switch new dummies to drag mode.
     *  @param dragEvent
     */
    public void onDragStarted(DragEvent dragEvent){
        this.dragEvent = dragEvent;
    }

    /** The AnimatorOfDummy has to be notified about drag end, to remove the dummy from the parent.
     */
    public void onDragEnded(){
        this.dragEvent = null;
    }


    /** Notify the AnimatorOfDummy when some object is dragged into the parent.
     *  The dummy will be added to the Parent and grown to a large size
     */
    public void onDragInAddDummyAnimation() {

        // in manually mode - do not try to init a dummy
        if(!isEnabled){
            Log.d(Constants.LOGD, "The view is in disabled mode. No dummy will be initiated.");
            return;
        }

        // no parent yet
        if(parentOfDummy == null){
            Log.e(Constants.LOGE, "The Parent should be set by using #attachToParent(). Otherwise no animation will occur.");
            return;
        }

        // lazy creation of dummy on drag in
        initDummy();

        // animate the addition of the view
        de.fitnesstracker.Context.invoker.executeCommand(commandGrowView, new CommandGrowViewParameters(AbstractCommandModifyViewParameter.Direction.EXECUTING).setFinalValue(DUMMY_HEIGHT));
    }


    public void setEnabled(boolean isEnabled){
        this.isEnabled = isEnabled;
    }

    /**
     * notify about dragOut, so that the dummy may be removed from parent
     * */
    public void onDragOutDragEndRemoveDummyAnimation(){
        // in manually mode - do not try to init a dummy
        if(!isEnabled){
            Log.d(Constants.LOGD, "The view is in disabled mode. No dummy will be shrinked");
            return;
        }
        if(viewDummyAnimated != null){
            de.fitnesstracker.Context.invoker.executeCommand(commandGrowView, new CommandGrowViewParameters(AbstractCommandModifyViewParameter.Direction.UNDOING));
        }
    }

    /** On Drag Listener which will be triggered on the dummy to the list of listeners */
    public void addDummyOnDragListener(View.OnDragListener dummyOnDragListener) {
        this.dummyOnDragListeners.add(dummyOnDragListener);
    }
    public void removeDummyOnDragListener(View.OnDragListener dummyOnDragListener) {
        this.dummyOnDragListeners.remove(dummyOnDragListener);
    }
    public List<View.OnDragListener> getDummyOnDragListener() {
        return dummyOnDragListeners;
    }

    // PUBLIC HELPER


    @Override
    public boolean isDraggingWithinView() {
        if( viewDummyAnimated != null){
            return viewDummyAnimated.isDraggingWithinView();
        }
        return false;
    }


    /** Returns the current ViewGroup which the dummy will be added to, when the method #onDragInAddDummyAnimation is called */
    public ViewGroup getParentOfDummy() {
        return parentOfDummy;
    }

    /** Returns the encapsulated ViewDummyAnimated */
    public ViewDummyAnimated getViewDummyAnimated() {
        return viewDummyAnimated;
    }


    // TODO me define points when the predescessor is chosen
    public ViewDummyAnimated getViewDummyAnimatedInitIfNecessary(ViewGroup viewParent, View predecessorView) {
        if(viewDummyAnimated == null){
            attachToParent(viewParent, predecessorView);
            initDummy();
        }
        return viewDummyAnimated;
    }

    /** The dummy may be removed e.g. to be created on the next drag in! The method removes the dummy from its parent and from Animator     */
    public void removeDummy(){
        // remove the dummy from parent
        if(viewDummyAnimated != null && viewDummyAnimated.getParent() !=null){
            ((ViewGroup)viewDummyAnimated.getParent().getParent()).removeView(viewDummyAnimated);
        }
        // forget the dummy
        this.viewDummyAnimated = null;

    }

    public void addCommandListener(IListenerCommand listenerCommand){
        this.commandGrowView.addListenerCommand(listenerCommand);

        // now add to the current command if available
        if(commandGrowView != null){
            commandGrowView.addListenerCommand(listenerCommand);
        }
    }

    public void removeCommandListener(IListenerCommand listenerCommand){
        this.commandGrowView.removeListenerCommand(listenerCommand);

        // now remove from the current command if available
        if(commandGrowView != null){
            commandGrowView.removeListenerCommand(listenerCommand);
        }
    }

    // PRIVATE

    /**
     * WHich index should be used to append the dummy to the parent.
     * Depends on the predecessor.
     * If no predecessor is defined - 0 is used.
     *
     * @return index in parent
     */
    private int getIndexInParent(){
        // the default index is 0 when no predecessor is defined
        int indexInParent = 0;

        if(predescessorView != null){
            indexInParent = Utils.getPositionInParent(predescessorView) + 1;
        }
        return indexInParent;
    }

    /**
     * Storing predecessor is better, than storng the index, since the index may change,
     * when the dummy is reattached or when a new view is inserted before the predecessor
     * @param predescessorView
     */
    private void setPredescessor(View predescessorView){
        this.predescessorView = predescessorView;
    }

    private boolean isViewDummyExpanded(){
        return ((this.viewDummyAnimated != null) &&
                (viewDummyAnimated.getHeight() > INITIAL_DUMMY_HEIGHT_BEFORE_EXPANDING)) ||
                (commandGrowView!=null && commandGrowView.isRunning());
    }

    /** Creates a new dummy and initializes it into drag mode - notifyChildOfDrag
     */
    private void initDummy() {
        Assert.assertNotNull(parentOfDummy);
        int parentWidth = parentOfDummy.getWidth();

        // create the dummy command if they are null
        this.createDummyAndCommand();
        Log.d(Constants.LOGD, "Width: " + parentWidth);

        /*  add the dummy to the group.
            It will be removed from the group when the undo animation finishes
            prepend the follower at the very first positon        */
        if (viewDummyAnimated.getParent() == null) {
            // override the size with initial size of dummy. Important if the dummy already has been visible and now it has it's initial height
            initDummyParameters(viewDummyAnimated, parentWidth, INITIAL_DUMMY_HEIGHT_BEFORE_EXPANDING);

            // which indexInParent should we have?
            int indexInParent = getIndexInParent();

            parentOfDummy.addView(viewDummyAnimated, indexInParent);

            /* after adding  a Child to the parent, when dragging is already happening -
             * DRAGEVENT.START has to be passed to the parent again,
             * so that it may be passed to all children and to the new child too.
             * It will activate the new child and switch it to drag mode.
             */
            if(dragEvent != null){
                parentOfDummy.dispatchDragEvent(dragEvent);
            }else{
                Log.e(Constants.LOGE, "Something went wrong - there is no DragEvent to switch new dummy to drag mode");
            }
        }
    }

    private boolean createDummyAndCommand(){
        // create the command if necessary
        if(viewDummyAnimated == null || commandGrowView == null){
            Assert.assertNotNull(parentOfDummy);
            int maxDummyWidth = parentOfDummy.getWidth();

            Log.d(Constants.LOGD, "createDummyAndCommand");

            // create a dummy
            viewDummyAnimated = createADummy(maxDummyWidth, INITIAL_DUMMY_HEIGHT_BEFORE_EXPANDING);

            // create the command
            commandGrowView = new CommandGrowViewHeight(viewDummyAnimated, INITIAL_DUMMY_HEIGHT_BEFORE_EXPANDING, DUMMY_HEIGHT, DUMMY_ANIMATION_DURATION);

            // remove the dummy from the parent when the command undo is called
            commandGrowView.addListenerCommand(new ListenerCommandAdapter() {
                @Override
                public void onUndoFinished() {
                    parentOfDummy.removeView(viewDummyAnimated);
                }
            });

            // add the rest of the listeners, which were added to the animator of dummy
            for(IListenerCommand l:commandExpandDummyListeners){
                commandGrowView.addListenerCommand(l);
            }

            return true;
        }
        return false;
    }


    private ViewDummyAnimated createADummy(int initialDummyWidth, int initialDummyHeight) {
        ViewDummyAnimated dummy  = new ViewDummyAnimated(context);
        setDummyOnDragListener(dummy);

        // initial lp to avoid nullPointerException
        // assign the height of 0 to the dummy. WIll expand it soon
        initDummyParameters(dummy, initialDummyWidth, initialDummyHeight);

        return dummy;
    }

    private void initDummyParameters(ViewDummyAnimated dummy, int initialDummyWidth, int initialDummyHeight){
        ViewGroup.LayoutParams lp = dummy.getLayoutParams();

        if(lp == null){
            lp = new ViewGroup.LayoutParams(initialDummyWidth, initialDummyHeight);
        }
        dummy.setLayoutParams(lp);

        int widthMsaureSpec = View.MeasureSpec.makeMeasureSpec(initialDummyWidth, View.MeasureSpec.EXACTLY);
        int heightMsaureSpec = View.MeasureSpec.makeMeasureSpec(initialDummyHeight, View.MeasureSpec.EXACTLY);

        // measure
        dummy.measure(widthMsaureSpec, heightMsaureSpec);

        // layout
        // no layout - measuring is enough to store the new data in the view
    }

    private void setDummyOnDragListener(ViewDummyAnimated dummy){
        /** This should be the only way where a DragListener is added to the dummy.
         * Because otherwise it will be overridden */
        dummy.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if(!dummyOnDragListeners.isEmpty()){
                    for(View.OnDragListener l:dummyOnDragListeners){
                        l.onDrag(v, event);
                    }
                }
                return false;
            }
        });
    }

}
