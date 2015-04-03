package de.fitnesstracker;

/**
 * A listener which is able to listen to any of the command events. Usually there is one listener per event
 * Created by skip on 22.02.2015.
 */
public interface IAnimatorOfDummyCommandListener {
    void onExecutionStartedListener();

    void onUndoStartedListener();

    void onExecutionSucessfullyFinishedListener();

    void onUndoFinishedListener();

    void onExecutionCanceled();
}
