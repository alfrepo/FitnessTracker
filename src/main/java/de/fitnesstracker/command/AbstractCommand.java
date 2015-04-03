package de.fitnesstracker.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by skip on 18.10.2014.
 */
public abstract class AbstractCommand implements Command {

    private List<IListenerCommand> listeners = Collections.synchronizedList(new ArrayList<IListenerCommand>());

//    private List<IListenerCommand> listenerOnUndoStarts = Collections.synchronizedList(new ArrayList<IListenerCommand>());
//    private List<IListenerCommand> listenerOnExecutionStarts = Collections.synchronizedList(new ArrayList<IListenerCommand>());
//    private List<IListenerCommand> listenerOnUndoFinishs = Collections.synchronizedList(new ArrayList<IListenerCommand>());
//    private List<IListenerCommand> listenerOnExecutionSucessfullyFinishs = Collections.synchronizedList(new ArrayList<IListenerCommand>());
//    private List<IListenerCommand> listenerOnExecutionCanceled = Collections.synchronizedList(new ArrayList<IListenerCommand>());


    @Override
    public void addListenerCommand(IListenerCommand l) {
        listeners.add(l);
    }

    @Override
    public void removeListenerCommand(IListenerCommand l) {
        listeners.remove(l);
    }

//    @Override
//    public void addOnExecutionStartedListener(IListenerCommand l) {
//        listenerOnExecutionStarts.add(l);
//    }
//
//    @Override
//    public void removeOnExecutionStartedListener(IListenerCommand l) {
//        listenerOnExecutionStarts.remove(l);
//    }
//
//    @Override
//    public void addOnUndoStartedListener(IListenerCommand l) {
//        listenerOnUndoStarts.add(l);
//    }
//
//    @Override
//    public void removeOnUndoStartedListener(IListenerCommand l) {
//        listenerOnUndoStarts.remove(l);
//    }
//
//    @Override
//    public void addOnExecutionSucessfullyFinishedListener(IListenerCommand l) {
//        listenerOnExecutionSucessfullyFinishs.add(l);
//    }
//
//    @Override
//    public void addOnUndoFinishedListener(IListenerCommand l) {
//        listenerOnUndoFinishs.add(l);
//    }
//
//    @Override
//    public void removeOnExecutionSuccessfullyFinishedListener(IListenerCommand l) {
//        listenerOnExecutionSucessfullyFinishs.remove(l);
//    }
//
//    @Override
//    public void removeOnUndoFinishedListener(IListenerCommand l) {
//        listenerOnUndoFinishs.remove(l);
//    }
//
//
//
//    @Override
//    public void addOnExecutionCanceled(IListenerCommand l) {
//        listenerOnExecutionCanceled.add(l);
//    }
//
//    @Override
//    public void removeOnExecutionCanceled(IListenerCommand l) {
//        listenerOnExecutionCanceled.remove(l);
//    }


    protected void notifyOnUndoFinishsListener(){
        for(IListenerCommand l:listeners){
            l.onUndoFinished();
        }
    }

    protected void notifyOnExecutionSuccessfullyFinishsListener(){
        for(IListenerCommand l: listeners){
            l.onExecutionSucessfullyFinished();
        }
    }

    protected void notifyOnExecutionCanceledListener(){
        for(IListenerCommand l:listeners ){
            l.onExecutionCanceled();
        }
    }

    protected void notifyOnExecutionStartsListener(){
        for(IListenerCommand l:listeners){
            l.onExecutionStarted();
        }
    }

    protected void notifyOnUndoStartsListener(){
        for(IListenerCommand l:listeners){
            l.onUndoStarted();
        }
    }
}
