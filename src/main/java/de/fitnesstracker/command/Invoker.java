package de.fitnesstracker.command;

import android.app.Activity;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by skip on 29.12.2014.
 */
public class Invoker {

    public static enum FILTER_RESULT{ DELAY, EXECUTE}

    // the filters which will be asked before the command is executed
    List<CommandsDelayFilter> commandFilters = new ArrayList<CommandsDelayFilter>();

    // already delayed commands
    List<DelayedCommandAndFilter> listDelayedCommands = new ArrayList<DelayedCommandAndFilter>();

    //activity
    Activity activity;

    public Invoker(Activity activity){
        Assert.assertNotNull(activity);
        this.activity = activity;
    }


    public void executeCommand(final Command command, final ICommandParameters commandParameters){

        // filter
        for(CommandsDelayFilter commandFilter : commandFilters){
            if(commandFilter.filter(command) == FILTER_RESULT.DELAY){
                // add command to the list of delayed commands
                this.listDelayedCommands.add(new DelayedCommandAndFilter(command, commandFilter, commandParameters));
                // break the execution
                return;
            }
        }

        // start the command on UI thread
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                commandParameters.execute(command);
            }
        });
    }



    // DELAYS OF COMMANDS

    public void addFilter(CommandsDelayFilter filter){
        this.commandFilters.add(filter);
    }

    public void removeFilter(CommandsDelayFilter filter){
        this.commandFilters.remove(filter);
    }

    public void undelay(Class<? extends Command> commandType){
        // find all delayed commands of this type and execute them again
        List<DelayedCommandAndFilter> undelayCommands = new ArrayList<DelayedCommandAndFilter>(this.listDelayedCommands);

        // remove commands which do not have the give type
        if(commandType != null){
            Iterator<DelayedCommandAndFilter> iterator = undelayCommands.iterator();
            // iterate all
            while(iterator.hasNext()){
                // remove commands with wrong type from the list-copy with listeners to undelay
                if(!commandType.isInstance(iterator.next().command)){
                    iterator.remove();
                }
            }
        }

        // remove all commands from the delayed list
        this.listDelayedCommands.removeAll(undelayCommands);

        // execute the rest of the commands
        for(DelayedCommandAndFilter d: undelayCommands){
            executeCommand(d.command, d.commandParameters);
        }
    }

    public void undelay(){
        // find all delayed commands and execute them again
        undelay(null);
    }



    // CLASSES
    public class DelayedCommandAndFilter {
        public Command command;                         // which command to execute?
        public ICommandParameters commandParameters;    // which parameters to use for execution?
        public CommandsDelayFilter filter;          // which filter caused the delay?


        public DelayedCommandAndFilter(Command command, CommandsDelayFilter filter, ICommandParameters commandParameters){
            this.command = command;
            this.commandParameters = commandParameters;
            this.filter = filter;
        }
    }
}
