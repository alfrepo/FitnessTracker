package de.fitnesstracker.command;

/**
 * Created by skip on 29.12.2014.
 */
public class CommandsDelayFilterDrop extends CommandsDelayFilter {

    private Class delayedCommandType = AbstractCommandModifyViewParameter.class;
    private CommandReplaceView commandReplaceView;

    public CommandsDelayFilterDrop(final Invoker invoker, CommandReplaceView commandReplaceView) {
        super(invoker);
        this.commandReplaceView = commandReplaceView;

        // listen for the commandReplaceView to be ready
        commandReplaceView.addListenerCommand(
            new ListenerCommandAdapter() {

                @Override
                public void onExecutionSucessfullyFinished() {
                    // remove itself from the invoker
                    invoker.removeFilter(CommandsDelayFilterDrop.this);

                    // undelay the CommandReplaceView commands
                    invoker.undelay(delayedCommandType);
                }
            }
        );
    }

    public Invoker.FILTER_RESULT filter(Command command) {
        // check whether the command is a CommandGrowView and delay it if yes
        if(delayedCommandType.isInstance( command) ){
            return Invoker.FILTER_RESULT.DELAY;
        }

        // do not delay others
        return Invoker.FILTER_RESULT.EXECUTE;
    }
}
