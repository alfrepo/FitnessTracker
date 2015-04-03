package de.fitnesstracker;

import android.view.View;

import junit.framework.Assert;

class LongClickListenerStartDragFromStack implements View.OnLongClickListener{

    View draggedView;

    LongClickListenerStartDragFromStack(View view){
        Assert.assertNotNull(view);
        draggedView = view;
    }

        @Override
        public boolean onLongClick(View v) {
            DragController.create().startDragFromStack(draggedView, v.getRootView());
            return true;
        }
    }