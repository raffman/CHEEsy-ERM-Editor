/**
 * Copyright 2017 Raffael Lorup, Ary Obenholzner, Robert Pinnisch, William Wang
 * <p>
 * This file is part of CHEEsy.
 * CHEEsy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * CHEEsy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with CHEEsy. If not, see <http://www.gnu.org/licenses/>.
 */

package gui.model;

import action.UndoableAction;
import model.ErmLine;
import model.ErmObject;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class describes lines connecting GuiAttributes/GuiEntities to GuiEntites/GuiRelations/GuiGeneralizations.
 * It manages the list of sequential points forming the line.
 * NOTE: the first and the last point MUST NOT be edited since they are the positions of the connected GuiObjects.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class GuiLine<D extends GuiObject, E extends ErmObject> {
    /**
     * the ErmLine equivalent to this GuiLine
     */
    private ErmLine<E> ermline;
    /**
     * the list of points
     * NOTE: first and last point are the actual positions of the connected GuiObjects
     */
    private List<Point> points = new LinkedList<>();
    /**
     * destination object
     */
    private D destination;

    /**
     * Ctor with given ErmLine, origin object and destination object.
     *
     * @param ermLine     ErmLine equivalent
     * @param origin      origin object
     * @param destination destination object
     */
    GuiLine(ErmLine<E> ermLine, GuiObject origin, D destination) {
        this.ermline = ermLine;
        this.destination = destination;
        points.add(origin.getPosition());
        points.add(destination.getPosition());
    }

    /**
     * Returns the ErmLine equivalent of this GuiLine.
     *
     * @return ErmLine equivalent
     */
    ErmLine<E> getErmline() {
        return ermline;
    }

    /**
     * Returns the destination object.
     *
     * @return destination object
     */
    public D getDestination() {
        return destination;
    }

    /**
     * Adds a new point to the line.
     *
     * @param p     the new point (coordinates)
     * @param index index of new point in the list
     * @return an undoable/redoable action
     */
    UndoableAction addPoint(Point p, int index) {
        points.add(index, p);

        return new UndoableAction() {
            @Override
            public void undo() {
                points.remove(index);
            }

            @Override
            public void redo() {
                points.add(index, p);
            }
        };
    }

    /**
     * Sets the coordinates of a point.
     *
     * @param xNew  new x coordinate
     * @param yNew  new y coordinate
     * @param index index of the point to be set
     * @return an undoable/redoable action
     */
    UndoableAction setPoint(int xNew, int yNew, int index) {
        UndoableAction temp = new UndoableAction() {
            private int xPrev = points.get(index).x, yPrev = points.get(index).y;

            @Override
            public void undo() {
                points.get(index).x = xPrev;
                points.get(index).y = yPrev;
            }

            @Override
            public void redo() {

                points.get(index).x = xNew;
                points.get(index).y = yNew;

            }
        };
        temp.redo();
        return temp;
    }

    /**
     * Shifts a point by a x- and y-offset.
     *
     * @param x x-offset
     * @param y y-offset
     * @return an undoable/redoable action
     */
    UndoableAction shiftPoints(int x, int y) {
        UndoableAction temp = new UndoableAction() {
            @Override
            public void undo() {
                Iterator<Point> it = points.iterator();
                it.next();
                while (it.hasNext()) {
                    Point p = it.next();
                    if (it.hasNext()) {
                        p.translate(-x, -y);
                    }
                }
            }

            @Override
            public void redo() {
                Iterator<Point> it = points.iterator();
                it.next();
                while (it.hasNext()) {
                    Point p = it.next();
                    if (it.hasNext()) {
                        p.translate(x, y);
                    }
                }
            }
        };
        temp.redo();
        return temp;
    }

    /**
     * Removes a point from the line.
     *
     * @param index index of point in list
     * @return an undoable/redoable action
     */
    UndoableAction removePoint(int index) {
        Point temp = points.get(index);
        points.remove(index);

        return new UndoableAction() {
            @Override
            public void undo() {
                points.add(index, temp);
            }

            @Override
            public void redo() {
                points.remove(index);
            }
        };
    }

    /**
     * Returns the list of points.
     *
     * @return list of points
     */
    List<Point> getPoints() {
        return points;
    }

    /**
     * Checks for redundant points and removes them.
     *
     * @return an undoable/redoable action or null if no change
     */
    UndoableAction checkEndPoints() {
        if (points.size() > 2) {
            Point p = points.get(1);
            if (GuiModel.isOnLine(p.x, p.y, points.get(0), points.get(2))) {
                return removePoint(1);
            } else if (points.size() > 3) {
                p = points.get(points.size() - 2);
                if (GuiModel.isOnLine(p.x, p.y, points.get(points.size() - 3), points.get(points.size() - 1))) {
                    return removePoint(points.size() - 2);
                }
            }
        }
        return null;
    }
}
