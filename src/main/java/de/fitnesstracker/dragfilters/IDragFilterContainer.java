package de.fitnesstracker.dragfilters;

import android.view.DragEvent;
import android.view.View;

import java.util.List;

/**
 * This Interface is implemented by containers, which would modify their {@link android.view.View#dispatchDragEvent(android.view.DragEvent)} method to notify filters about the events which are passing them.
 * Created by skip on 11.01.2015.
 */
public interface IDragFilterContainer {

    void addDragFilter(IOnDragFilter filter);
    void removeDragFilter(IOnDragFilter filter);
    List<IOnDragFilter> getDragFilters();
    void notifyDragFilter(View view, DragEvent dragEvent);

}

