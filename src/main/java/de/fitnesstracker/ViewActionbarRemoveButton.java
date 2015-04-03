package de.fitnesstracker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.fitnesstracker.command.AbstractCommandModifyViewParameter;
import de.fitnesstracker.command.CommandGrowViewHeight;
import de.fitnesstracker.command.CommandGrowViewParameters;


/**
 * Created by skip on 06.02.2015.
 */
public class ViewActionbarRemoveButton extends LinearLayout {

    TextView textView;

    public static final int DURATION_MS = 200;
//    public static final int HEIGHT_PX =  R.dimen.button_remove_height;


    Context mContext;

    int heightPx = (int)getResources().getDimension(R.dimen.button_remove_height);

    CommandGrowViewHeight commandGrowViewHeight = new CommandGrowViewHeight(this, heightPx, DURATION_MS);
    CommandGrowViewParameters commandGrowViewParametersExecuting = new CommandGrowViewParameters(AbstractCommandModifyViewParameter.Direction.EXECUTING).setFinalValue(heightPx).setFallbackToThisValueWhenUndoing(0);
    CommandGrowViewParameters commandGrowViewParametersUndo = new CommandGrowViewParameters(AbstractCommandModifyViewParameter.Direction.UNDOING).setFinalValue(heightPx).setFallbackToThisValueWhenUndoing(0);

    public ViewActionbarRemoveButton(Context context) {
        super(context);
        init(context);
    }

    public ViewActionbarRemoveButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewActionbarRemoveButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context){
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.toolbar_removebutton_ersatz, this, true);

        textView = (TextView)findViewById(R.id.text);
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        if(event.getAction() == DragEvent.ACTION_DRAG_STARTED){
            onDragStart();
        } else if(event.getAction() == DragEvent.ACTION_DRAG_ENDED){
            onDragEnd();
        } else if(event.getAction() == DragEvent.ACTION_DRAG_ENTERED){
            onDragIn();
        } else if(event.getAction() == DragEvent.ACTION_DRAG_EXITED){
            onDragOut();
        } else if(event.getAction() == DragEvent.ACTION_DROP){
            /*
                tell that we will handle the drop from now on. This one will just do nothing
                and so the view will be removed but not attached to any other view
              */
            de.fitnesstracker.Context.dragController.onDrop(this, new DragEventDecorator(event));
        }
        return true;
    }

    private void onDragStart(){
        setVisibleAnimatedWay(true);
    }

    private void onDragEnd(){
        setTextColorWhite();
        setVisibleAnimatedWay(false);
    }

    private void onDragIn(){
        setTextColorRed();
    }

    private void onDragOut(){
        setTextColorWhite();
    }

    private void setVisibleAnimatedWay(boolean isVisible){
        if(isVisible){
            de.fitnesstracker.Context.invoker.executeCommand(commandGrowViewHeight, commandGrowViewParametersExecuting);
        }else{
            de.fitnesstracker.Context.invoker.executeCommand(commandGrowViewHeight, commandGrowViewParametersUndo);
        }

    }

    private void setTextColorRed(){
        textView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }

    private void setTextColorWhite(){
        textView.setTextColor(getResources().getColor(android.R.color.white));
    }

}
