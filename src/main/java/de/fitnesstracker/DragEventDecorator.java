package de.fitnesstracker;

import android.content.ClipData;
import android.content.ClipDescription;
import android.view.DragEvent;

/**
 * Created by skip on 17.01.2015.
 */
public class DragEventDecorator {

    int mAction;
    float mX, mY;
    ClipDescription mClipDescription;
    ClipData mClipData;
    Object mLocalState;
    boolean mDragResult;

    private DragEvent dragEvent;

    public static DragEventDecorator obtain(ClipData clipData, int x, int y){
        DragEventDecorator dragEventDecorator = new DragEventDecorator();
        dragEventDecorator.mX = x;
        dragEventDecorator.mY = y;
        dragEventDecorator.mClipData = clipData;
        return dragEventDecorator;
    }

    private DragEventDecorator(){}

    public DragEventDecorator(DragEvent dragEvent){
        this.mAction = dragEvent.getAction();
        this.mX = dragEvent.getX();
        this.mY = dragEvent.getY();
        this.mClipData = dragEvent.getClipData();
        this.mLocalState = dragEvent.getLocalState();
        this.mDragResult = dragEvent.getResult();
    }

    int getAction(){
        return this.mAction;
    }
    float getX(){
        return this.mX;
    }
    float getY(){
        return this.mY;
    }
    ClipData getClipData(){
        return this.mClipData;
    }
    Object getLocalState(){
        return this.getLocalState();
    }
    boolean getResult(){
        return this.mDragResult;
    }
}
