package de.fitnesstracker.command;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;

import de.fitnesstracker.Constants;


/**
 * Created by skip on 18.10.2014.
 */
public abstract class AbstractCommandModifyViewParameter extends AbstractCommand {

    private final View receiverView;
    private Integer fallbackToThisValueWhenUndoing = 0;
    private Integer finalValue = 0;
    private final long duration;

    private ValueAnimator valueAnimator;
    private Direction direction = Direction.EXECUTING;

    public AbstractCommandModifyViewParameter(View receiver, int fallbackToThisValueWhenUndoing, Integer finalValue, Integer duration){
        this(receiver, finalValue, duration);
        this.fallbackToThisValueWhenUndoing = fallbackToThisValueWhenUndoing;
    }

    public AbstractCommandModifyViewParameter(View receiver, int finalValue, int duration){
        this.receiverView = receiver;
        this.finalValue = finalValue;
        this.duration = duration;

        // on default fallback to the start value. May change that by using another constructor
        this.fallbackToThisValueWhenUndoing = getStartValue(receiverView);
    }

    public AbstractCommandModifyViewParameter setFallbackToThisValueWhenUndoing(int fallbackToThisValueWhenUndoing) {
        cancel();
        this.fallbackToThisValueWhenUndoing = fallbackToThisValueWhenUndoing;
        return this;
    }

    public AbstractCommandModifyViewParameter setFinalValue(int finalValue) {
        // setting a new value stopps the animation
        cancel();
        this.finalValue = finalValue;
        return this;
    }

    @Override
    public boolean isRunning() {
        if(valueAnimator!= null && valueAnimator.isRunning()){
            return true;
        }
        return false;
    }

    @Override
    public void execute() {
        execute(Direction.EXECUTING);
    }

    @Override
    public void undo() {
        execute(Direction.UNDOING);
    }

    @Override
    public void cancel() {
        if(valueAnimator != null){
            valueAnimator.cancel();
        }
    }

    protected void execute(final Direction animationDirection){
        // if already executing in the given direction
        if(this.direction == animationDirection && isRunning()){
            return;
        }

        // use measuredHeight since the view may have already been measured but not drawn yet. In this case the Height would be = 0
        int startValue = getStartValue(receiverView);

        Log.d(Constants.LOGD,"receiverView.getHeight(): "+startValue);

        int finalValue = this.finalValue;
        long currentDuration = duration;

        // remember new direction
        this.direction = animationDirection;

        if(animationDirection == Direction.UNDOING){
            // undo by shrinking the view back to its previous height
            finalValue = this.fallbackToThisValueWhenUndoing;
        }

        // cancel if in progress
        if(valueAnimator != null && valueAnimator.isRunning()){
            // cancel the animation
            valueAnimator.cancel();
            Log.d(Constants.LOGD, "Cancel previous animation which was already running");
        }


        // now animate
        Log.d(Constants.LOGD,String.format("From %s px to %s px", startValue, finalValue));
        valueAnimator = getAnimator(startValue, finalValue, currentDuration);

        // listen for animation end
        valueAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                if(animationDirection == Direction.EXECUTING){
                    notifyOnExecutionStartsListener();
                }else{
                    notifyOnUndoStartsListener();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(animationDirection == Direction.EXECUTING){
                    notifyOnExecutionSuccessfullyFinishsListener();
                }else{
                    notifyOnUndoFinishsListener();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                notifyOnExecutionCanceledListener();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // nothing
            }
        });

        // start the animationNow
        valueAnimator.start();
    }

    // explicitely tell the direction
    private ValueAnimator getAnimator(final int startValue, final int finalValue, final long durationOfAnimation) {

        if(valueAnimator != null && valueAnimator.isRunning()){
            valueAnimator.cancel();
        }

        // animate dummy height from current height to final height
        valueAnimator = ValueAnimator.ofInt(startValue, finalValue);
        valueAnimator.setDuration(durationOfAnimation);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {

                doOnUpdate(receiverView, animation);
            }
        });

        return valueAnimator;
    }

    /**
     * When the command starts - with what value should it start the animation?
     * @param receiverView
     * @return
     */
    abstract int getStartValue(View receiverView);

    /**
     * What to do when animation is updated
     * @param receiverView
     * @param animaton
     */
    abstract void doOnUpdate(View receiverView, ValueAnimator animaton);


    // CLASSES

    public enum Direction {
        EXECUTING, UNDOING;

        public Direction toggle(){
            if(this == EXECUTING){
                return UNDOING;
            }
            return EXECUTING;
        };
    }
}
