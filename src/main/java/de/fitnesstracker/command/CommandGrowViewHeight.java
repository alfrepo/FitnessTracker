package de.fitnesstracker.command;

import android.animation.ValueAnimator;
import android.view.View;

/**
 * Command modifies the view height in an animated way
 * Created by skip on 29.12.2014.
 */
public class CommandGrowViewHeight extends AbstractCommandModifyViewParameter{

    public CommandGrowViewHeight(View receiver, int fallbackToThisValueWhenUndoing, int finalValue, int duration) {
        super(receiver, fallbackToThisValueWhenUndoing, finalValue, duration);
    }

    public CommandGrowViewHeight(View receiver, int finalValue, int duration) {
        super(receiver, finalValue, duration);
    }

    @Override
    int getStartValue(View receiverView) {
        return receiverView.getMeasuredHeight();
    }

    @Override
    void doOnUpdate(View receiverView, ValueAnimator animation) {
            Integer value = (Integer) animation.getAnimatedValue();
            receiverView.getLayoutParams().height = value.intValue();
            receiverView.requestLayout();
    }
}
