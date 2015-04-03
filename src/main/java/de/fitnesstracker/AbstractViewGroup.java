package de.fitnesstracker;

import java.util.List;

/**
 * Created by skip on 27.09.2014.
 */
public interface AbstractViewGroup extends AbstractFigure {

    void addSuccessor(AbstractFigure figure);
    void replace(AbstractFigure child);

    List<AbstractFigure> getChildren();
    void addChild(AbstractFigure child);
}
