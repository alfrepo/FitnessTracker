package de.fitnesstracker.command;

/**
 * Created by skip on 18.10.2014.
 */
public interface Command {
    void execute();
    void undo();
    void cancel();
    boolean isRunning();

    void addListenerCommand(IListenerCommand l);
    void removeListenerCommand(IListenerCommand l);

}
