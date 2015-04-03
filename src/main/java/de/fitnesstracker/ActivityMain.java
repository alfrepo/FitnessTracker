package de.fitnesstracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import de.fitnesstracker.command.Invoker;


public class ActivityMain extends Activity {

    ViewGroup topMostContainer;
    LinearLayout linearLayout;
    ViewGroupAnimated viewGroupAnimated;

    int itemHeight;
    int dragshadowWidth;
    int viewGroupMinHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linearlayout_parent);
        init();
    }

    void init() {
        linearLayout = (LinearLayout) findViewById(R.id.parentContainerMain);
        viewGroupAnimated = (ViewGroupAnimated) findViewById(R.id.group2);
        topMostContainer = (ViewGroup)findViewById(R.id.topMostContainer);

        // init the Context. WIll switch to DI here
        Context.invoker = new Invoker(this);
        Context.activity = this;

        // setup scroll control which would control the main scrollView
        ScrollView scrollViewMain = (ScrollView)findViewById(R.id.scrollviewMain);
        ViewScrollBarControl scrollControlMain = (ViewScrollBarControl)findViewById(R.id.scrollBarControlMain);
        ViewScrollAreaTopBottom scrollControlUpMain = (ViewScrollAreaTopBottom)findViewById(R.id.scrollControlTopMain);
        ViewScrollAreaTopBottom scrollControlDownMain = (ViewScrollAreaTopBottom)findViewById(R.id.scrollControlBottomMain);

        setUpScrollControls(scrollViewMain, scrollControlMain, scrollControlUpMain, scrollControlDownMain);

        // setup scroll control which would control the archive scrollView
        ScrollView scrollViewArchive = (ScrollView)findViewById(R.id.scrollViewArchive);
        ViewScrollBarControl scrollControlArchive = (ViewScrollBarControl)findViewById(R.id.scrollBarControlArchive);
        ViewScrollAreaTopBottom scrollControlUpArchive = (ViewScrollAreaTopBottom)findViewById(R.id.scrollControlTopArchive);
        ViewScrollAreaTopBottom scrollControlDownArchive = (ViewScrollAreaTopBottom)findViewById(R.id.scrollControlBottomArchive);

        setUpScrollControls(scrollViewArchive, scrollControlArchive, scrollControlUpArchive, scrollControlDownArchive);

        itemHeight = (int) getResources().getDimension(R.dimen.viewitem_fixed_height);
        dragshadowWidth = (int) getResources().getDimension(R.dimen.drag_shadow_width);
        viewGroupMinHeight = (int) getResources().getDimension(R.dimen.viewgroup_min_height);
    }

    private void setUpScrollControls(ScrollView scrollView, ViewScrollBarControl scrollControl, ViewScrollAreaTopBottom scrollControlUp, ViewScrollAreaTopBottom scrollControlDown) {
        scrollControl.set(scrollView);
        scrollControlUp.set(scrollView, ViewScrollAreaTopBottom.ScrollDirection.UP);
        scrollControlDown.set(scrollView, ViewScrollAreaTopBottom.ScrollDirection.DOWN);
    }


    private void startDrag(View view, int shadowRessourceId, int shadowWidth, int shadowHeight){
        // start dragging
        DragController.create().startDragFromToolbar(topMostContainer, shadowRessourceId, shadowWidth, shadowHeight, getLayoutInflater());
    }

}
