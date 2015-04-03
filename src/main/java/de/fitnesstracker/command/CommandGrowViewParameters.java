package de.fitnesstracker.command;

import junit.framework.Assert;

/**
 * Parameters may store command's arguments to execute it later
 * Created by skip on 29.12.2014.
 */
public class CommandGrowViewParameters implements ICommandParameters<AbstractCommandModifyViewParameter>{
    public final AbstractCommandModifyViewParameter.Direction animationDirection;
    public Integer finalValue;
    public Integer fallbackToThisValueWhenUndoing;


    public CommandGrowViewParameters(AbstractCommandModifyViewParameter.Direction animationDirection){
        Assert.assertNotNull(animationDirection);
        this.animationDirection = animationDirection;
    }

    public CommandGrowViewParameters setFinalValue(Integer finalValue) {
        this.finalValue = finalValue;
        return this;
    }

    public CommandGrowViewParameters setFallbackToThisValueWhenUndoing(Integer fallbackToThisValueWhenUndoing) {
        this.fallbackToThisValueWhenUndoing = fallbackToThisValueWhenUndoing;
        return this;
    }

    @Override
    public void execute(AbstractCommandModifyViewParameter command) {
        if(finalValue != null){
            command.setFinalValue(finalValue);
        }
        if(fallbackToThisValueWhenUndoing != null) {
            command.setFallbackToThisValueWhenUndoing(fallbackToThisValueWhenUndoing);
        }
        command.execute(animationDirection);
    }
}
