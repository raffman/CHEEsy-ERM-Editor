/**
 * Copyright 2017 Gerd Holweg, Raffael Lorup, Ary Obenholzner, Robert Pinnisch, William Wang
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
 * <p>
 * The repository for this project can be found at <https://github.com/raffman/CHEEsy-ERM-Editor>.
 */

package plugin;

import javafx.util.Pair;
import model.ErmCardinality;

import java.awt.*;
import java.util.List;

/**
 * This interface describes the functionality a plugin needs to provide to draw ERM diagrams.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public interface ErmPlugin {
    /**
     * Sets the font to be used for the diagram.
     *
     * @param f the Font to be used
     */
    void setFont(Font f);

    /**
     * Returns the clickable area of an entity as a pair of corners.
     *
     * @param p     center of entity
     * @param scale scale factor
     * @return a pair of corners (&lt upper left, lower right &gt)
     */
    Pair<Point, Point> areaEntity(Point p, float scale);

    /**
     * Draws an entity.
     *
     * @param g        the Graphics object for drawing
     * @param p        center of entity
     * @param scale    scale factor
     * @param name     name of entity.
     * @param selected whether the entity has been selected
     */
    void drawEntity(Graphics2D g, Point p, float scale, String name, boolean selected);

    /**
     * Returns the clickable area of an attribute as a pair of corners.
     *
     * @param p     center of attribute
     * @param scale scale factor
     * @return a pair of corners (&lt upper left, lower right &gt)
     */
    Pair<Point, Point> areaAttribute(Point p, float scale);

    /**
     * Draws an attribute.
     *
     * @param g        the Graphics object for drawing
     * @param p        center of attribute
     * @param scale    scale factor
     * @param name     name of attribute
     * @param primary  is (part of) primary key
     * @param selected whether the attribute has been selected
     */
    void drawAttribute(Graphics2D g, Point p, float scale, String name, boolean primary, boolean selected);

    /**
     * Returns the clickable area of a generalization as a pair of corners.
     *
     * @param p     center of generalization
     * @param scale scale factor
     * @return a pair of corners (&lt upper left, lower right &gt)
     */
    Pair<Point, Point> areaGeneralization(Point p, float scale);

    /**
     * Draws a generalization.
     *
     * @param g        the Graphics object for drawing
     * @param p        center of generalization
     * @param scale    scale factor
     * @param selected whether the generalization has been selected
     */
    void drawGeneralization(Graphics2D g, Point p, float scale, boolean selected);

    /**
     * Returns the clickable area of a relation as a pair of corners.
     *
     * @param p     center of relation
     * @param scale scale factor
     * @return a pair of corners (&lt upper left, lower right &gt)
     */
    Pair<Point, Point> areaRelation(Point p, float scale);

    /**
     * Draws a relation.
     *
     * @param g        the Graphics object for drawing
     * @param p        center of relation
     * @param scale    scale factor
     * @param name     name of relation
     * @param selected whether the relation has been selected
     */
    void drawRelation(Graphics2D g, Point p, float scale, String name, boolean selected);

    /**
     * Takes a list of sequential points and transforms each point to represent its own clickable position.
     * The clickable position should match the positions of the point when drawing the line so users no where to click to select a point.
     *
     * @param pointList list of points
     */
    void areaLine(List<Point> pointList);

    /**
     * Draws a line from a list of sequential points.
     *
     * @param g         the Graphics object for drawing
     * @param pointList list of points
     */
    void drawLine(Graphics2D g, List<Point> pointList);

    /**
     * Draws a line from a list of sequential points and a cardinality next to the line.
     *
     * @param g           the Graphics object for drawing
     * @param pointList   list of points
     * @param cardinality the cardinality
     * @param scale       scale factor
     */
    void drawLine(Graphics2D g, List<Point> pointList, float scale, ErmCardinality cardinality);
}
