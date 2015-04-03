package de.fitnesstracker;

import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by skip on 09.11.2014.
 */
public final class Utils {

    public static final int getViewIndexInParent(View child) {
        ViewGroup parent = (ViewGroup)child.getParent();
        for(int i=0; i<parent.getChildCount(); i++){
            if(parent.getChildAt(i).equals(child)){
                return i;
            }
        }
        throw new IllegalStateException("Failed to retrieve the index of the view");
    }

    /**
     * Finds the sibling of the given view.
     * @param view - the view which's sibling we are looking for
     * @param relativeOffset - the offset, e.g. -1 to get the previous sibling, or 1 go get the next sibling
     * @return the sibling or null if non sibling was found or on indexOutOfBounds
     */
    public static final View getSibling(View view, int relativeOffset) {
        // check whether a perent ViewGroup exists
        ViewParent viewParent = view.getParent();
        if(viewParent == null || !(viewParent instanceof ViewGroup)){
            return null;
        }
        ViewGroup viewGroup = (ViewGroup)viewParent;

        // calculate the sibling index and retrieve it
        int indexViewInParent = Utils.getViewIndexInParent(view);
        int siblingIndex = indexViewInParent + relativeOffset;
        try{
            return viewGroup.getChildAt(siblingIndex);
        }catch (IndexOutOfBoundsException e){
            // will return null
        }

        // return null since we could not find any siblings
        return null;
    }

    public static String getDragEventName(int dragEvent){
        switch (dragEvent){
            case DragEvent.ACTION_DRAG_STARTED:
                return "ACTION_DRAG_STARTED";
            case DragEvent.ACTION_DRAG_EXITED:
                return "ACTION_DRAG_EXITED";
            case DragEvent.ACTION_DROP:
                return "ACTION_DROP";
            case DragEvent.ACTION_DRAG_ENDED:
                return "ACTION_DRAG_ENDED";
            case DragEvent.ACTION_DRAG_ENTERED:
                return "ACTION_DRAG_ENTERED";
            case DragEvent.ACTION_DRAG_LOCATION:
                return "ACTION_DRAG_LOCATION";
        }
        return "unknown event";
    }

    /** Checks whether the Event  tells something about an update of the drag position over view*/
    public static Boolean isDraggingOverFromDragEvent(DragEvent event){
        switch (event.getAction()){
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
            case DragEvent.ACTION_DRAG_ENDED:
                return false;
            default:
                return null;
        }
    }

    /** removes the view from parent if the parent exists */
    public static final void removeFromParent(View view){
        ViewParent replaceByParent = view.getParent();
        if(replaceByParent!=null && replaceByParent instanceof ViewGroup){
            ((ViewGroup) replaceByParent).removeView(view);
        }
    }

    /** removes the view from parent if the parent exists */
    public static final int getPositionInParent(View view){
        if(view == null){
            throw new IllegalStateException("Can not determine potion in parent of null");
        }
        ViewGroup viewGroup = ((ViewGroup)view.getParent());
        for(int i=0; i<viewGroup.getChildCount(); i++){
            View child = viewGroup.getChildAt(i);
            if(child.equals(view)){
                return i;
            }
        }
        // does not make sence!
        throw new IllegalStateException("Could not find the view within it's parent!");
    }



}
