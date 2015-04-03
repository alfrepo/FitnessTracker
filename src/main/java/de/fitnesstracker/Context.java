package de.fitnesstracker;

import android.app.Activity;

import de.fitnesstracker.command.Invoker;


/**
 * Created by skip on 02.01.2015.
 */
public class Context {
    /** Current activity     */
    public static Activity activity;

    public static Invoker invoker;

    /** This object has to be created whenever the drag process is started.
     *  This object will destroy it self on dragEnd.
     *  For that it should be notified about drag starts and ends*/
    public static DragController dragController;
}
