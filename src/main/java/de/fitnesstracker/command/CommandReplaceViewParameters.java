package de.fitnesstracker.command;

import junit.framework.Assert;

/**
 * Parameters may store command's arguments to execute it later
 * Created by skip on 19.10.2014.
 */
public class CommandReplaceViewParameters  implements ICommandParameters<CommandReplaceView>{
    public CommandReplaceView.DIRECTION animationDirection;


    public CommandReplaceViewParameters(CommandReplaceView.DIRECTION animationDirection){
        Assert.assertNotNull(animationDirection);
        this.animationDirection = animationDirection;
    }

    @Override
    public void execute(CommandReplaceView command) {
        command.execute(animationDirection);
    }
}
