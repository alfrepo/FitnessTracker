package de.fitnesstracker;

import android.util.Log;
import android.view.DragEvent;
import android.view.View;

import junit.framework.Assert;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Drag Listener which will hide the given dummy, when drag leaves view and dummy.
 * Drag out is delayed by {@link Constants#DUMMY_TIMEOUT_TILL_DEACTIVATION_MS}
 */
public class OnDummyDragOutDummyUnregister implements View.OnDragListener {

    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    // The View which owns the dummy
    private IDragInViewIdentifier dragInViewIdentifier;

    // the dummy which is spawn by the owner view and should be hidden on exit of both
    private AnimatorOfDummy animatorOfDummyToMonitorDragOut;

    // animator of dummies
    private AnimatorOfDummy[] animatorOfDummiesToHide;

    /**
     * Creates the listener.
     * @param dragInViewIdentifierToMonitorDragOut - view to check, when drag is moved out of this
     * @param animatorOfDummyToMonitorDragOut - dummy to check, when drag is moved out of it
     * @param animatorOfDummiesToHide - those dummies will be hidden when the drag out happens
     */
    public OnDummyDragOutDummyUnregister(IDragInViewIdentifier dragInViewIdentifierToMonitorDragOut, AnimatorOfDummy animatorOfDummyToMonitorDragOut, AnimatorOfDummy[] animatorOfDummiesToHide) {
        Assert.assertNotNull(dragInViewIdentifierToMonitorDragOut);
        Assert.assertNotNull(animatorOfDummyToMonitorDragOut);
        Assert.assertNotNull(animatorOfDummiesToHide);

        this.animatorOfDummyToMonitorDragOut = animatorOfDummyToMonitorDragOut;
        this.animatorOfDummiesToHide = animatorOfDummiesToHide;
        this.dragInViewIdentifier = dragInViewIdentifierToMonitorDragOut;
    }

    @Override
    public boolean onDrag(final View v, DragEvent event) {
        // need this to be able to start processes on ui thread
        Assert.assertNotNull(v);

        if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                // check whether over dummy or over current Item
                if (!dragInViewIdentifier.isDraggingWithinView() && !animatorOfDummyToMonitorDragOut.isDraggingWithinView()) {

                    // start on ui thread if yes : onDragOutDragEndRemoveDummyAnimation();
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(Constants.LOGD, "onDragOutDragEndRemoveDummyAnimation()");
                            onDragOutRemoveDummyAnimation();
                        }
                    });
                }
                }
            };

            worker.schedule(runnable, Constants.DUMMY_TIMEOUT_TILL_DEACTIVATION_MS, TimeUnit.MILLISECONDS);
        }
        return false;
    }

    private void onDragOutRemoveDummyAnimation(){
        for(AnimatorOfDummy dummyAnimator: animatorOfDummiesToHide){
            // pass onDragOut to the dummyAnimator and make it hide the dummy
            dummyAnimator.onDragOutDragEndRemoveDummyAnimation();
        }
    }
}