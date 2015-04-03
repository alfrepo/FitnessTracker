package de.fitnesstracker.command;

public interface IListenerCommand {
//        void onTrigger();

        void onExecutionStarted();

        void onUndoStarted();

        void onExecutionSucessfullyFinished();

        void onUndoFinished();

        void onExecutionCanceled();
    }