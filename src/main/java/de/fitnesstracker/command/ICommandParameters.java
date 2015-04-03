package de.fitnesstracker.command;

/**
 * Class which stores command parameters outside of the command,
 * to enable delayed execution of commands.
 *
 * Created by skip on 29.12.2014.
 */
public interface ICommandParameters<T extends Command> {

    /* executes the command with concrete parameters */
    void execute(T command);
}
