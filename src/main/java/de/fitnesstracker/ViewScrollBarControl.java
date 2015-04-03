package de.fitnesstracker;

import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ScrollView;

import de.fitnesstracker.command.AbstractCommandModifyViewParameter;
import de.fitnesstracker.command.CommandGrowViewParameters;
import de.fitnesstracker.command.CommandGrowViewWidth;

/**
 * Created by skip on 02.02.2015.
 */
public class ViewScrollBarControl extends SurfaceView {

    /*
        used to add an offset to the scroll control. It modifies the scroll position so that
        0% are reached a little bit (= offset) earlier than at the top and
        100% are reached a little bit (=offset) earlier that at the bottom
      */
    public double SCROLL_CONTROL_UP_DOWN_OFFSET_PX;

    public int WIDTH_DRAG_OFF = (int)getResources().getDimension(R.dimen.scrollcontrol_width_drag_off);
    public int WIDTH_MOUSE_OUT = (int)getResources().getDimension(R.dimen.scrollcontrol_width_drag_out);
    public int WIDTH_MOUSE_OVER = (int)getResources().getDimension(R.dimen.scrollcontrol_width_drag_over);
    public int DURATION = Constants.SCROLL_CONTROL_ANIM_DURATION_MS;

    public double insensitiveDragZoneInPercent = 0.1;
    public long pauseBetweenSrollsMs = 100;
    public int scrollByPx = 150;

    public CommandGrowViewWidth commandGrowViewWidth = new CommandGrowViewWidth(this, WIDTH_MOUSE_OVER, WIDTH_MOUSE_OVER, DURATION);

    ScrollView scrollView;
    double heightScrollView;
    double heightViewScrollBarControl;
    Float touchPosition = -99999f;

    ScrollRunnable scrollRunnable;

    public ViewScrollBarControl(android.content.Context context) {
        super(context);
        init();
    }

    public ViewScrollBarControl(android.content.Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewScrollBarControl(android.content.Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init(){
        // make this view transparent
        this.setZOrderOnTop(true);    // necessary
        SurfaceHolder sfhTrackHolder = this.getHolder();
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);


        pauseBetweenSrollsMs = 100;
        insensitiveDragZoneInPercent = 0.1;
        SCROLL_CONTROL_UP_DOWN_OFFSET_PX = getResources().getDimension(R.dimen.scrollcontrol_up_down_offset);

        WIDTH_DRAG_OFF = (int) getResources().getDimension(R.dimen.scrollcontrol_width_drag_off);
        WIDTH_MOUSE_OUT = (int) getResources().getDimension(R.dimen.scrollcontrol_width_drag_out);
        WIDTH_MOUSE_OVER = (int) getResources().getDimension(R.dimen.scrollcontrol_width_drag_over);

        scrollByPx = (int) getResources().getDimension(R.dimen.scrollcontrol_scroll_by);
    }

    public void set(final ScrollView scrollView){
        this.scrollView = scrollView;

        // compute center
        scrollView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                heightScrollView = scrollView.getChildAt(0).getMeasuredHeight();
                heightScrollView = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
                heightViewScrollBarControl = getMeasuredHeight();
            }
        });
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
            Log.d("ViewScrollBarControl","ACTION_DRAG_STARTED");
            Context.invoker.executeCommand(commandGrowViewWidth,
                    new CommandGrowViewParameters(AbstractCommandModifyViewParameter.Direction.EXECUTING).
                            setFinalValue(WIDTH_MOUSE_OUT));

        } else if(event.getAction() == DragEvent.ACTION_DRAG_ENDED){
            Log.d("ViewScrollBarControl","ACTION_DRAG_ENDED");
            stopScrolling();

            Context.invoker.executeCommand(commandGrowViewWidth,
                    new CommandGrowViewParameters(AbstractCommandModifyViewParameter.Direction.EXECUTING).
                            setFinalValue(WIDTH_DRAG_OFF).
                            setFallbackToThisValueWhenUndoing(WIDTH_DRAG_OFF));

        }else if(event.getAction() == DragEvent.ACTION_DRAG_ENTERED){
            Log.d("ViewScrollBarControl","ACTION_DRAG_ENTERED");
            // grow view
            Context.invoker.executeCommand(commandGrowViewWidth,
                    new CommandGrowViewParameters(AbstractCommandModifyViewParameter.Direction.EXECUTING).
                            setFinalValue(WIDTH_MOUSE_OVER));
            startScrolling();

        }else if(event.getAction() == DragEvent.ACTION_DRAG_EXITED){
            // stop on drag_exit is important, because otherwise the thread will chain the scrollposition to the current touchPosition
            stopScrolling();

            Context.invoker.executeCommand(commandGrowViewWidth,
                    new CommandGrowViewParameters(AbstractCommandModifyViewParameter.Direction.EXECUTING).
                            setFinalValue(WIDTH_MOUSE_OUT));



        } else if(event.getAction() == DragEvent.ACTION_DRAG_LOCATION){
            synchronized (touchPosition){
                touchPosition = event.getY();
            }
        }
        return true;
    }


    // private

    private void stopScrolling(){
        if(scrollRunnable != null){
            Log.d(Constants.LOGD,"stopScrolling");
            scrollRunnable.isRunning = false;
            scrollRunnable = null;
        }
    }

    private void startScrolling(){
        stopScrolling();
        if(scrollRunnable == null || scrollRunnable.isRunning == false){
            Log.d(Constants.LOGD,"startScrolling");
            scrollRunnable = new ScrollRunnable();
            new Thread(scrollRunnable).start();
        }
    }


    private class ScrollRunnable implements Runnable{
        boolean isRunning = true;

        @Override
        public void run() {
            while(isRunning){
                try {
                    synchronized (touchPosition){

                        if(touchPosition<0 || heightViewScrollBarControl<touchPosition){
                            continue;
                        }

                        // compute the position percentage relative to height of scroll-control
                        double scrollToPosPercentage, scrollToPosPx;
                        scrollToPosPercentage = (touchPosition - SCROLL_CONTROL_UP_DOWN_OFFSET_PX) / (heightViewScrollBarControl-(2* SCROLL_CONTROL_UP_DOWN_OFFSET_PX));
                        scrollToPosPx = heightScrollView * scrollToPosPercentage;

                        scrollView.smoothScrollTo(0, (int)scrollToPosPx);
                    }

                    Thread.sleep(pauseBetweenSrollsMs);
                }catch (InterruptedException exc){
                    // nothing
                }
            }
        }
    }


}
