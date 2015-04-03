package de.fitnesstracker;

import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ScrollView;

import de.fitnesstracker.command.CommandGrowViewHeight;


/**
 * Created by skip on 02.02.2015.
 */
public class ViewScrollAreaTopBottom extends SurfaceView {

    public enum ScrollDirection {UP, DOWN}

    public static final int FPS = 24;
    public static final double SPEED_PX_PER_MS = 0.6;

    // computed from FPS
    private int pauseBetweenUpdatesMs = 1000 / FPS;
    private int distanceToScrollAfterPausePx = (int) ((double)pauseBetweenUpdatesMs * SPEED_PX_PER_MS);

    private int heightDragOnDragover    = (int) getResources().getDimension(R.dimen.scrollcontrol_width_drag_over);
    private int heightDragOff           = (int) getResources().getDimension(R.dimen.scrollcontrol_width_drag_off);
    private int heightDragOnOutside     = (int) getResources().getDimension(R.dimen.scrollcontrol_width_drag_out);
    private int animationDuration       = (int) Constants.SCROLL_CONTROL_ANIM_DURATION_MS;

    private CommandGrowViewHeight commandGrowViewHeight = new CommandGrowViewHeight(this, heightDragOff, heightDragOnOutside, animationDuration);

    private ScrollView scrollView;
    private ScrollDirection scrollDirection;
    private ScrollRunnable scrollRunnable;

    public ViewScrollAreaTopBottom(android.content.Context context) {
        super(context);
        init();
    }

    public ViewScrollAreaTopBottom(android.content.Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewScrollAreaTopBottom(android.content.Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init(){
        // make this view transparent
        this.setZOrderOnTop(true);    // necessary
        SurfaceHolder sfhTrackHolder = this.getHolder();
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);

        // timings
    }

    public void set(final ScrollView scrollView, ScrollDirection scrollDirection){
        this.scrollView = scrollView;
        this.scrollDirection = scrollDirection;

        switch (scrollDirection){
            case UP:
                distanceToScrollAfterPausePx = - Math.abs(this.distanceToScrollAfterPausePx);
                break;

            case DOWN:
                distanceToScrollAfterPausePx = Math.abs(this.distanceToScrollAfterPausePx);
                break;
        }
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        Log.d("onTouch","dispatchDragEvent");

        // block the drag events under this view by not calling the super method
//        super.dispatchDragEvent(event);

        // dispatch the drag
        if(Context.dragController != null){
            Context.dragController.notifyDragFilter(this, event);
        }

        if(event.getAction() == DragEvent.ACTION_DRAG_STARTED){
            commandGrowViewHeight.setFinalValue(this.heightDragOnOutside);
            commandGrowViewHeight.setFallbackToThisValueWhenUndoing(heightDragOff);
            commandGrowViewHeight.execute();

        }else if(event.getAction() == DragEvent.ACTION_DRAG_ENDED){
            commandGrowViewHeight.setFinalValue(this.heightDragOnOutside);
            commandGrowViewHeight.setFallbackToThisValueWhenUndoing(heightDragOff);
            commandGrowViewHeight.undo();
            Log.d("ScrollArea","ACTION_DRAG_ENDED");
            stopScroll();

        }else if(event.getAction() == DragEvent.ACTION_DRAG_ENTERED){
            commandGrowViewHeight.setFinalValue(this.heightDragOnDragover);
            commandGrowViewHeight.setFallbackToThisValueWhenUndoing(heightDragOnOutside);
            commandGrowViewHeight.execute();
            Log.d("ScrollArea","ACTION_DRAG_ENTERED");
            startScroll();

        }else if(event.getAction() == DragEvent.ACTION_DRAG_EXITED){
            commandGrowViewHeight.setFinalValue(this.heightDragOnDragover);
            commandGrowViewHeight.setFallbackToThisValueWhenUndoing(heightDragOnOutside);
            commandGrowViewHeight.undo();
            Log.d("ScrollArea","ACTION_DRAG_EXITED");
            stopScroll();

        }
        return true;
    }


    // private


    private void startScroll(){
        stopScroll();
        Log.d("ScrollArea","start");
        scrollRunnable = new ScrollRunnable();
        new Thread(scrollRunnable).start();
    }

    private void stopScroll(){
        if(scrollRunnable != null){
            Log.d("ScrollArea","stop");
            scrollRunnable.isRunning = false;
            scrollRunnable = null;
        }
    }

    /**
     * Moves the scrollbar while running
     */
    private class ScrollRunnable implements Runnable{
        boolean isRunning = true;

        @Override
        public void run() {
            while(isRunning){

                Context.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("ScrollArea","scroll by :"+distanceToScrollAfterPausePx);
                        scrollView.smoothScrollBy(0, distanceToScrollAfterPausePx);
                    }
                });
                try {
                    Thread.sleep(pauseBetweenUpdatesMs);
                } catch (InterruptedException e) {
                    // exception
                    e.printStackTrace();
                }
            }
        }
    }


}
