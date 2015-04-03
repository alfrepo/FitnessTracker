package de.fitnesstracker.command;

/**
 * Created by skip on 29.12.2014.
 */
public abstract class CommandsDelayFilter {

    // knows invoker to trigger undelay of commands.
    protected Invoker invoker;

    public CommandsDelayFilter(Invoker invoker){
        this.invoker = invoker;
    }

    public abstract Invoker.FILTER_RESULT filter(Command command);
}
