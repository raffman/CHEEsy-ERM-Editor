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

package gui.model;

import action.CombinedAction;
import action.ReferencedAction;
import action.UndoableAction;
import gui.DrawingPanel;
import gui.DrawingStatus;
import javafx.util.Pair;
import model.ErmAttribute;
import model.ErmCardinality;
import model.ErmEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import plugin.ErmPlugin;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * This class summarizes all objects necessary to describe an ERM model to the GUI.
 * The GUI will need to use this class to change the model.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class GuiModel {
    /**
     * distance for checking when selecting a point on a line
     */
    public static final double POINTCLICKDISTANCE = 25.0;
    /**
     * distance for checking when clicking on a line
     */
    public static final double LINECLICKDISTANCE = 30.0;
    /**
     * the plugin used for drawing
     */
    private ErmPlugin plugin;
    //object sets
    private Set<GuiEntity> entities = new HashSet<>();
    private Set<GuiRelation> relations = new HashSet<>();
    private Set<GuiGeneralization> generalizations = new HashSet<>();
    /**
     * last created object
     */
    private GuiObject lastCreated = null;
    /**
     * Caching dimension
     */
    private Pair<Point, Point> viewDimension = new Pair<>(new Point(0, 0), new Point(0, 0));
    /**
     * the panel to be notified, when changes occur
     */
    private DrawingPanel panel;
    /**
     * the phantom object to be drawn when adding a new object<br>
     * NOTE: this is necessary since adding actual new objects which might then not be added would complicate things very much
     */
    private DrawingStatus phantom = DrawingStatus.NONE;
    /**
     * position of phantom object
     */
    private Point phantomPos = new Point();
    /**
     * the phantom point when transforming lines
     * NOTE: this is necessary since adding actual new points which might then not be added would complicate things very much
     */
    private Point phantomPoint = new Point();
    /**
     * the line the phantom point should be added
     */
    private GuiLine phantomPointLine;
    /**
     * the index of the phantom point within the list of points of the line
     */
    private int phantomPointIndex;
    /**
     * whether the phantom point should be added or is actually a modified existing point
     */
    private boolean phantomPointAdded;
    /**
     * the size of the gridspaces
     */
    private int gridSize = 25;
    /**
     * whether the grid should be drawn
     */
    private boolean gridShow = true;
    /**
     * the fontSize to be used by the plugin for drawing.
     */
    private int fontSize = 12;

    /**
     * Ctor with a given drawing plugin.
     *
     * @param plugin the plugin used for drawing
     */
    public GuiModel(ErmPlugin plugin) {
        setPlugin(plugin);
    }

    /**
     * Sets the drawing plugin.
     *
     * @param plugin the plugin used for drawing
     */
    public void setPlugin(ErmPlugin plugin) {
        this.plugin = plugin;
        plugin.setFont(new Font("Arial", Font.PLAIN, fontSize));
    }

    /**
     * Returns the currently used plugin.
     *
     * @return currently used plugin
     */
    public ErmPlugin getPlugin() {
        return plugin;
    }

    /*******************************************************************************************************************
     * Selection management
     ******************************************************************************************************************/
    /**
     * Returns the last created object.
     *
     * @return last created object
     */
    public GuiObject getLastCreated() {
        return lastCreated;
    }

    /**
     * Clears the given Set and adds all objects whose center are within the given rectangle.
     *
     * @param left      left of rectangle
     * @param top       top of rectangle
     * @param right     right of rectangle
     * @param bottom    bottom of rectangle
     * @param selection the set to be filled
     */
    public void getSelected(int left, int top, int right, int bottom, Set<GuiObject> selection, boolean filterEntities) {
        selection.clear();
        //transform given coordinates to model coordinates
        left = toModelX(left);
        top = toModelY(top);
        right = toModelX(right);
        bottom = toModelY(bottom);
        for (GuiEntity ent : entities) {
            Point p = ent.getPosition();
            if (p.x >= left && p.x <= right && p.y >= top && p.y <= bottom) {
                selection.add(ent);
            }
            for (GuiLine<GuiAttribute, ErmAttribute> l : ent.getAttributes()) {
                p = l.getDestination().getPosition();
                if (p.x >= left && p.x <= right && p.y >= top && p.y <= bottom) {
                    selection.add(l.getDestination());
                }
            }
        }
        if (!filterEntities) {
            for (GuiGeneralization gen : generalizations) {
                Point p = gen.getPosition();
                if (p.x >= left && p.x <= right && p.y >= top && p.y <= bottom) {
                    selection.add(gen);
                }
            }
            for (GuiRelation rel : relations) {
                Point p = rel.getPosition();
                if (p.x >= left && p.x <= right && p.y >= top && p.y <= bottom) {
                    selection.add(rel);
                }
                for (GuiLine<GuiAttribute, ErmAttribute> l : rel.getAttributes()) {
                    p = l.getDestination().getPosition();
                    if (p.x >= left && p.x <= right && p.y >= top && p.y <= bottom) {
                        selection.add(l.getDestination());
                    }
                }
            }
        }
    }

    /**
     * Returns the clicked object or null if nothing found at given coordinates.
     *
     * @param x              x mouse
     * @param y              y mouse
     * @param filterEntities if true only entities are checked
     * @param snappoint      an offset to snap the object to the mouse
     * @return a selected object or null if nothing found
     */
    public GuiObject getSelected(int x, int y, boolean filterEntities, Point snappoint) {
        GuiObject res;
        if (!filterEntities) {
            res = getSelectedAttribute(x, y, snappoint);
            if (res != null) {
                return res;
            }
        }
        res = getSelectedEntity(x, y, snappoint);
        if (res == null && !filterEntities) {
            res = getSelectedRelation(x, y, snappoint);
            if (res != null) {
                return res;
            }
            res = getSelectedGeneralization(x, y, snappoint);
        }
        return res;
    }

    /**
     * Returns the clicked entity or null if none found at given coordinates.
     *
     * @param x         x mouse
     * @param y         y mouse
     * @param snappoint an offset to snap the object to the mouse
     * @return a selected entity or null if none found
     */
    private GuiEntity getSelectedEntity(int x, int y, Point snappoint) {
        for (GuiEntity ent : entities) {
            Pair<Point, Point> area = plugin.areaEntity(toView(ent.getPosition(), cachepoint, 0, 0), zoom);
            if (area.getKey().x < x && x < area.getValue().x) {
                if (area.getKey().y < y && y < area.getValue().y) {
                    if (snappoint != null) {
                        getSnappoint(x, y, area, snappoint);
                    }
                    return ent;
                }
            }
        }
        return null;
    }

    /**
     * Returns the clicked attribute or null if none found at given coordinates.
     *
     * @param x         x mouse
     * @param y         y mouse
     * @param snappoint an offset to snap the object to the mouse
     * @return a selected attribute or null if none found
     */
    private GuiAttribute getSelectedAttribute(int x, int y, Point snappoint) {
        for (GuiRelation rel : relations) {
            GuiAttribute temp = getSelectedAttributeBecauseJavaCantCallLambda(rel, x, y, snappoint);
            if (temp != null) {
                return temp;
            }
        }
        for (GuiEntity ent : entities) {
            GuiAttribute temp = getSelectedAttributeBecauseJavaCantCallLambda(ent, x, y, snappoint);
            if (temp != null) {
                return temp;
            }
        }
        return null;
    }

    /**
     * Don't ask.
     *
     * @param owner     the owner who's attributes should be checked
     * @param x         x mouse
     * @param y         y mouse
     * @param snappoint an offset to snap the object to the mouse
     * @return a selected entity or null if none found
     */
    private GuiAttribute getSelectedAttributeBecauseJavaCantCallLambda(GuiAttributed owner, int x, int y, Point snappoint) {
        for (GuiLine<GuiAttribute, ErmAttribute> l : owner.getAttributes()) {
            GuiAttribute attr = l.getDestination();
            Pair<Point, Point> area = plugin.areaAttribute(toView(attr.getPosition(), cachepoint, 0, 0), zoom);
            if (area.getKey().x < x && x < area.getValue().x) {
                if (area.getKey().y < y && y < area.getValue().y) {
                    if (snappoint != null) {
                        getSnappoint(x, y, area, snappoint);
                    }
                    return attr;
                }
            }
        }
        return null;
    }

    /**
     * Returns the clicked relation or null if none found at given coordinates.
     *
     * @param x         x mouse
     * @param y         y mouse
     * @param snappoint an offset to snap the object to the mouse
     * @return a selected relation or null if none found
     */
    private GuiRelation getSelectedRelation(int x, int y, Point snappoint) {
        for (GuiRelation rel : relations) {
            Pair<Point, Point> area = plugin.areaRelation(toView(rel.getPosition(), cachepoint, 0, 0), zoom);
            if (area.getKey().x < x && x < area.getValue().x) {
                if (area.getKey().y < y && y < area.getValue().y) {
                    if (snappoint != null) {
                        getSnappoint(x, y, area, snappoint);
                    }
                    return rel;
                }
            }
        }
        return null;
    }

    /**
     * Returns the clicked generalization or null if none found at given coordinates.
     *
     * @param x         x mouse
     * @param y         y mouse
     * @param snappoint an offset to snap the object to the mouse
     * @return a selected generalization or null if none found
     */
    private GuiGeneralization getSelectedGeneralization(int x, int y, Point snappoint) {
        for (GuiGeneralization gen : generalizations) {
            Pair<Point, Point> area = plugin.areaGeneralization(toView(gen.getPosition(), cachepoint, 0, 0), zoom);
            if (area.getKey().x < x && x < area.getValue().x) {
                if (area.getKey().y < y && y < area.getValue().y) {
                    if (snappoint != null) {
                        getSnappoint(x, y, area, snappoint);
                    }
                    return gen;
                }
            }
        }
        return null;
    }

    /**
     * Calculates the (offset) snappoint of the object to the mouse.
     *
     * @param x         x mouse
     * @param y         y mouse
     * @param area      clickable area of the object
     * @param snappoint the point for storing the offset
     */
    private void getSnappoint(int x, int y, Pair<Point, Point> area, Point snappoint) {
        int xHalf = area.getKey().x + (area.getValue().x - area.getKey().x) / 2;
        int yHalf = area.getKey().y + (area.getValue().y - area.getKey().y) / 2;
        int width = area.getValue().x - area.getKey().x;
        int height = area.getValue().y - area.getKey().y;
        snappoint.x = xHalf - x;
        snappoint.y = yHalf - y;
    }

    /**
     * Returns information about a selected/new point on a line or null if none found.
     *
     * @param x x mouse
     * @param y y mouse
     * @return information about the selected/new point
     */
    public PointInfo getSelectedPoint(int x, int y) {
        for (GuiGeneralization gen : generalizations) {
            PointInfo res = gen.hasSuperline() ? getSelectedPoint(x, y, gen.getSuperline()) : null;
            if (res != null) {
                return res;
            }
            for (GuiLine<GuiEntity, ErmEntity> l : gen.getConnections()) {
                res = getSelectedPoint(x, y, l);
                if (res != null) {
                    return res;
                }
            }
        }
        for (GuiEntity ent : entities) {
            for (GuiLine<GuiAttribute, ErmAttribute> l : ent.getAttributes()) {
                PointInfo res = getSelectedPoint(x, y, l);
                if (res != null) {
                    return res;
                }
            }
        }
        for (GuiRelation rel : relations) {
            for (GuiLine<GuiEntity, ErmEntity> l : rel.getConnections()) {
                PointInfo res = getSelectedPoint(x, y, l);
                if (res != null) {
                    return res;
                }
            }
            for (GuiLine<GuiAttribute, ErmAttribute> l : rel.getAttributes()) {
                PointInfo res = getSelectedPoint(x, y, l);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    /**
     * Returns information about a selected/new point on a line or null if none found.
     *
     * @param x x mouse
     * @param y y mouse
     * @param l the line to be checked
     * @return information about the selected/new point
     */
    private PointInfo getSelectedPoint(int x, int y, GuiLine l) {
        plugin.areaLine(toView(l.getPoints(), 0, 0, false, false));
        Iterator<Point> it = pointscopy.iterator();
        Point fp1 = null, p1 = it.next();
        Point cur = it.next();
        Point p2 = it.hasNext() ? it.next() : null;
        Point fp2 = it.hasNext() ? it.next() : null;
        int i = 1;
        while (cur != null) {
            if (p2 != null && isSamePoint(x, y, cur.x, cur.y, POINTCLICKDISTANCE)) {
                return new PointInfo(l, i, false, fp1, p1, p2, fp2);
            } else if (isOnLine(x, y, p1, cur)) {
                return new PointInfo(l, i, true, fp1, p1, cur, p2);
            }
            fp1 = p1;
            p1 = cur;
            cur = p2;
            p2 = fp2;
            if (it.hasNext()) {
                fp2 = it.next();
            } else {
                fp2 = null;
            }
            i++;
        }
        return null;
    }

    /**
     * Checks whether a point is on a line defined by two points, i.e. distance point-line < LINECLICKDISTANCE
     *
     * @param x  x of point
     * @param y  y of point
     * @param p1 first point of line
     * @param p2 second point of line
     * @return true if point is on line
     */
    public static boolean isOnLine(int x, int y, Point p1, Point p2) {
        int ax = p1.x - p2.x, ay = p1.y - p2.y;
        int nx = ay, ny = -ax;
        float t = (ny * (p1.x - x) + nx * (y - p1.y)) / (float) (ay * nx - ax * ny);
        if (t < -1 || t > 0) {
            return false;
        }
        int sx = (int) (p1.x + ax * t), sy = (int) (p1.y + ay * t);
        return isSamePoint(sx, sy, x, y, LINECLICKDISTANCE);
    }

    /**
     * Checks whether two points are the same, i.e. the distance between two points < distance.
     *
     * @param x1       x of point 1
     * @param y1       y of point 1
     * @param x2       x of point 2
     * @param y2       y of point 2
     * @param distance distance to determine equality
     * @return true if points are the same
     */
    public static boolean isSamePoint(int x1, int y1, int x2, int y2, double distance) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) <= distance;
    }

    /**
     * Inner class for describing a point on a line<br>
     * NOTE: this is used for transforming lines
     */
    public class PointInfo {
        /**
         * the line
         */
        public GuiLine line;
        /**
         * the index of the point in the list
         */
        public int index;
        /**
         * whether the point is new or alread exists
         */
        public boolean newPoint;
        /**
         * the neighbour points
         */
        public Point farneighbour1, neighbour1, neighbour2, farneighbour2;

        /**
         * Ctor
         *
         * @param l   line
         * @param i   index
         * @param n   is new point
         * @param fn1 far neighbour 1
         * @param n1  neighbour 1
         * @param n2  neighbour 2
         * @param fn2 far neighbour 2
         */
        private PointInfo(GuiLine l, int i, boolean n, Point fn1, Point n1, Point n2, Point fn2) {
            line = l;
            index = i;
            newPoint = n;
            neighbour1 = n1;
            neighbour2 = n2;
            farneighbour1 = fn1;
            farneighbour2 = fn2;
        }
    }

    /*******************************************************************************************************************
     *General management
     ******************************************************************************************************************/
    /**
     * Checks whether ALL the objects in the given set actually exist in the diagram.
     *
     * @param selection the set of objects
     * @return true if ALL objects exists
     */
    public boolean exists(Set<GuiObject> selection) {
        for (GuiObject obj : selection) {
            if (!exists(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether a given object exists int the diagram.
     *
     * @param obj an object
     * @return true if exists
     */
    public boolean exists(GuiObject obj) {
        if (obj instanceof GuiGeneralization) {
            return generalizations.contains(obj);
        } else if (obj instanceof GuiEntity) {
            return entities.contains(obj);
        } else if (obj instanceof GuiRelation) {
            return relations.contains(obj);
        } else if (obj instanceof GuiAttribute) {
            for (GuiEntity ent : entities) {
                for (GuiLine<GuiAttribute, ErmAttribute> l : ent.getAttributes()) {
                    if (l.getDestination().equals(obj)) {
                        return true;
                    }
                }
            }
            for (GuiRelation rel : relations) {
                for (GuiLine<GuiAttribute, ErmAttribute> l : rel.getAttributes()) {
                    if (l.getDestination().equals(obj)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Changes the name of an object.<br>
     * NOTE: the renaming UndoableAction is created somewhere else and needs a way to notify the DrawingPanel.
     *
     * @param namingAction the UndoableAction representing the renaming
     * @return an undoable/redoable action
     */
    public UndoableAction setName(UndoableAction namingAction) {
        UndoableAction temp = new ReferencedAction(namingAction) {
            @Override
            public void after(boolean isUndo) {
                notifyDraw(true, false);
            }
        };
        notifyDraw(true, false);
        return temp;
    }

    /**
     * This method is here to set the name of an object without creating a UndoableAction.
     *
     * @param obj  the object to be renamed
     * @param name the new name
     */
    public void setName(GuiObject obj, String name) {
        obj.setName(name);
        notifyDraw(false, false);
    }

    /**
     * Shifts the position of a set of objects.
     *
     * @param selection the set objects
     * @param x         x offset
     * @param y         y offset
     * @return an undoable/redoable action
     */
    public UndoableAction shiftPos(Set<GuiObject> selection, int x, int y) {
        CombinedAction actions = new CombinedAction();
        boolean first = true;
        UndoableAction last = null;
        for (GuiObject obj : selection) {
            //don't move attributes unless their owners are not selected
            if (!(obj instanceof GuiAttribute) || !selection.contains(((GuiAttribute) obj).getOwner())) {
                if (first) {
                    actions.add(new ReferencedAction(obj.shiftPos((int) (x / zoom), (int) (y / zoom), selection)) {
                        @Override
                        public void after(boolean isUndo) {
                            notifyDraw(false, false);
                        }
                    });
                    first = false;
                    last = checkEndPoints(actions, obj);
                } else {
                    if (last != null) {
                        actions.add(last);
                    }
                    last = obj.shiftPos((int) (x / zoom), (int) (y / zoom), selection);
                    UndoableAction tmp = checkEndPoints(actions, obj);
                    if (tmp != null) {
                        actions.add(tmp);
                    }
                }
            }
        }
        if (last != null) {
            actions.add(new ReferencedAction(last) {
                @Override
                public void after(boolean isUndo) {
                    notifyDraw(false, false);
                }
            });
        }
        notifyDraw(false, false);
        return actions;
    }

    /**
     * Checks for redundant points on all lines of a given object and removes them.
     * Any resulting UndoableActions are added to the given CombiendAction.
     *
     * @param actions the CombinedAction to add any resulting UndoableActions to
     * @param obj     the object who's line should be checked
     * @return the last UndoableAction in the list or null if none created
     */
    private UndoableAction checkEndPoints(CombinedAction actions, GuiObject obj) {
        if (obj instanceof GuiAttribute) {
            GuiAttribute att = (GuiAttribute) obj;
            Set<GuiLine<GuiAttribute, ErmAttribute>> lines = att.getOwner().getAttributes();
            for (GuiLine<GuiAttribute, ErmAttribute> line : lines) {
                if (line.getDestination().equals(att)) {
                    return line.checkEndPoints();
                }
            }
        } else if (obj instanceof GuiEntity) {
            UndoableAction last = null;
            GuiEntity ent = (GuiEntity) obj;
            for (GuiRelation rel : relations) {
                for (GuiLine<GuiEntity, ErmEntity> line : rel.getConnections()) {
                    if (line.getDestination().equals(ent)) {
                        if (last != null) {
                            actions.add(last);
                        }
                        last = line.checkEndPoints();
                    }
                }
            }
            return last;
        } else if (obj instanceof GuiConnection) {
            UndoableAction last = null;
            GuiConnection con = (GuiConnection) obj;
            Map<GuiLine<GuiEntity, ErmEntity>, ErmCardinality> map = new HashMap<>();
            for (GuiLine<GuiEntity, ErmEntity> line : con.getConnections(map).keySet()) {
                if (last != null) {
                    actions.add(last);
                }
                last = line.checkEndPoints();
            }
            return last;
        }
        return null;
    }

    /**
     * Removes all objects in a given set from the diagram.
     *
     * @param selection the set of objects
     * @return an undoable/redoable action
     */
    public UndoableAction remove(Set<GuiObject> selection) {
        CombinedAction actions = new CombinedAction();
        for (GuiObject obj : selection) {
            if (obj instanceof GuiEntity) {
                actions.add(remove((GuiEntity) obj, false));
            } else if (obj instanceof GuiAttribute) {
                actions.add(remove((GuiAttribute) obj, false));
            } else if (obj instanceof GuiRelation) {
                actions.add(remove((GuiRelation) obj, false));
            } else if (obj instanceof GuiGeneralization) {
                actions.add(remove((GuiGeneralization) obj, false));
            } else {
                throw new WrongGuiObjectException("called remove on something that cannot be part of the model");
            }
        }
        notifyDraw(true, true);
        return actions;
    }

    /*******************************************************************************************************************
     *Attributed management
     ******************************************************************************************************************/
    /**
     * Adds a new attribute to an entity or relation.
     *
     * @param name  name of attribute
     * @param owner owner of attribute
     * @param x     x position of attribute
     * @param y     y position of attribute
     * @return an undoable/redoable action
     */
    public UndoableAction newAttribute(String name, GuiAttributed owner, int x, int y) {
        lastCreated = new GuiAttribute(name, owner, toModel(new Point(x, y)));
        ReferencedAction temp = new ReferencedAction(owner.addAttribute((GuiAttribute) lastCreated)) {
            @Override
            public void after(boolean isUndo) {
                notifyDraw(true, isUndo);
            }
        };
        temp.after(false);
        return temp;
    }

    /**
     * Removes an attribute.
     *
     * @param attribute the attribute
     * @return an undoable/redoable action
     */
    public UndoableAction remove(GuiAttribute attribute) {
        return remove(attribute, true);
    }

    /**
     * Removes an attribute and notifies the DrawingPanel if requested.
     *
     * @param attribute the attribute
     * @param notyDrw   whether the DrawingPanel should be notified
     * @return an undoable/redoable action
     */
    private UndoableAction remove(GuiAttribute attribute, boolean notyDrw) {
        ReferencedAction temp = new ReferencedAction(attribute.getOwner().removeAttribute(attribute)) {
            @Override
            public void after(boolean isUndo) {
                notifyDraw(true, !isUndo);
            }
        };
        if (notyDrw) {
            temp.after(false);
        }
        return temp;
    }

    /**
     * Sets the primary property of an attribute.
     *
     * @param attribute the attribute
     * @param primary   whether the attribute is (part of) the primary key
     * @return an undoable/redoable action
     */
    public UndoableAction setPrimary(GuiAttribute attribute, boolean primary) {
        ReferencedAction temp = new ReferencedAction(attribute.setPrimary(primary)) {
            @Override
            public void after(boolean isUndo) {
                notifyDraw(true, false);
            }
        };
        temp.after(false);
        return temp;
    }

    /*******************************************************************************************************************
     *Connection management
     ******************************************************************************************************************/
    /**
     * Adds an entities to a relation or generalization (as a subtype).
     *
     * @param connection relation or generalization
     * @param selection  a set of objects (will filter for entites)
     * @return an undoable/redoable action
     */
    public UndoableAction addConnection(GuiConnection connection, Set<GuiObject> selection) {
        //NOTE: since the mouseReleased-Listener already notifyProperties() don't do it here
        CombinedAction actions = new CombinedAction();

        boolean first = true;
        UndoableAction last = null;
        for (GuiObject obj : selection) {
            if (obj instanceof GuiEntity) {
                if (first) {
                    actions.add(new ReferencedAction(connection.addConnection((GuiEntity) obj, ErmCardinality.MULTIPLE)) {
                        @Override
                        public void after(boolean isUndo) {
                            notifyDraw(true, false);
                        }
                    });
                    first = false;
                } else {
                    if (last != null) {
                        actions.add(last);
                    }
                    last = connection.addConnection((GuiEntity) obj, ErmCardinality.MULTIPLE);
                }

            }
        }
        if (last != null) {
            actions.add(new ReferencedAction(last) {
                @Override
                public void after(boolean isUndo) {
                    notifyDraw(true, false);
                }
            });
        }
        notifyDraw(true, false);
        return actions;
    }

    /**
     * Removes a connection to an entity from a relation or generalization.
     *
     * @param connection relation or generalization
     * @param entity     the line identifying a specific connection
     * @return an undoable/redoable action
     */
    public UndoableAction removeConnection(GuiConnection connection, GuiLine<GuiEntity, ErmEntity> entity) {
        ReferencedAction temp = new ReferencedAction(connection.removeConnection(entity)) {
            @Override
            public void after(boolean isUndo) {
                notifyDraw(true, false);
            }
        };
        temp.after(false);
        return temp;
    }

    /*******************************************************************************************************************
     *Entity management
     ******************************************************************************************************************/
    /**
     * Adds a new entity to the diagram and connects it to any relation or generalization in a given set.
     *
     * @param name      name of the entity
     * @param x         x position of the entity
     * @param y         y position of the entity
     * @param selection set of objects (will filter for relations and generalizations)
     * @return an undoable/redoable action
     */
    public UndoableAction newEntity(String name, int x, int y, Set<GuiObject> selection) {
        {
            boolean noConnection = true;
            for (GuiObject obj : selection) {
                if (obj instanceof GuiConnection) {
                    noConnection = false;
                    break;
                }
            }
            if (noConnection) {
                return newEntity(name, x, y);
            }
        }
        lastCreated = new GuiEntity(name, toModel(new Point(x, y)));
        CombinedAction actions = new CombinedAction();
        UndoableAction temp = new UndoableAction() {
            private GuiEntity entity = (GuiEntity) lastCreated;

            @Override
            public void undo() {
                entities.remove(entity);
//                notifyDraw(true, true);
            }

            @Override
            public void redo() {
                entities.add(entity);
                notifyDraw(true, false);
            }
        };
        actions.add(temp);
        UndoableAction last = null;
        for (GuiObject obj : selection) {
            if (obj instanceof GuiConnection) {
                GuiConnection connection = (GuiConnection) obj;
                if (last != null) {
                    actions.add(last);
                }
                last = connection.addConnection((GuiEntity) lastCreated, ErmCardinality.MULTIPLE);
            }
        }
        if (last == null) {
            throw new RuntimeException("wtf? got empty selection?");
        }
        actions.add(new ReferencedAction(last) {
            @Override
            public void after(boolean isUndo) {
                if (isUndo) {
                    notifyDraw(true, true);
                }
            }
        });
        temp.redo();
        return temp;
    }

    /**
     * Adds a new entity to the diagram.
     *
     * @param name name of the entity
     * @param x    x position of the entity
     * @param y    y position of the entity
     * @return an undoable/redoable action
     */
    public UndoableAction newEntity(String name, int x, int y) {
        lastCreated = new GuiEntity(name, toModel(new Point(x, y)));
        UndoableAction temp = new UndoableAction() {
            private GuiEntity entity = (GuiEntity) lastCreated;

            @Override
            public void undo() {
                entities.remove(entity);
                notifyDraw(true, true);
            }

            @Override
            public void redo() {
                entities.add(entity);
                notifyDraw(true, false);
            }
        };
        temp.redo();
        return temp;
    }

    /**
     * Removes an entity from the diagram.
     *
     * @param entity the entity to be removed
     * @return an undoable/redoable action
     */
    public UndoableAction remove(GuiEntity entity) {
        return remove(entity, true);
    }

    /**
     * Removes an entity and notifies the DrawingPanel if requested.
     *
     * @param entity  the entity to be removed
     * @param notyDrw whether the DrawingPanel should be notified
     * @return an undoable/redoable action
     */
    private UndoableAction remove(GuiEntity entity, boolean notyDrw) {
        CombinedAction actions = new CombinedAction();
        //deleting entity from list
        actions.add(new UndoableAction() {
            @Override
            public void undo() {
                entities.add(entity);
                notifyDraw(true, false);
            }

            @Override
            public void redo() {
                entities.remove(entity);
                notifyDraw(true, true);
            }
        });
        if (notyDrw) {
            actions.redo();
        } else {
            entities.remove(entity);
        }
        //deleting entity from any relation
        for (GuiRelation rel : relations) {
            if (rel.hasConnection(entity)) {
                actions.add(rel.removeAllConnections(entity));

            }
        }
        //deleting entity from any generalization
        for (GuiGeneralization gen : generalizations) {
            if (gen.hasSuperline() && gen.getSuperline().getDestination().equals(entity)) {
                //if is Superentity => set Superentity to null
                actions.add(gen.setSupertype(null));
            } else {
                //otherwise check if entity is the only subentity
                if (gen.hasConnection(entity)) {
                    actions.add(gen.removeAllConnections(entity));
                }
            }
        }
        return actions;
    }

    /*******************************************************************************************************************
     *Relation management
     ******************************************************************************************************************/
    /**
     * Adds a new relation to the diagram and connects it to any entities in the given set.
     *
     * @param name      name of the relation
     * @param x         x position of the relation
     * @param y         y position of the relation
     * @param selection set of objects (will filter for entities)
     * @return an undoable/redoable action
     */
    public UndoableAction newRelation(String name, int x, int y, Set<GuiObject> selection) {
        HashMap<GuiEntity, ErmCardinality> map = new HashMap<>();
        for (GuiObject obj : selection) {
            if (obj instanceof GuiEntity) {
                map.put((GuiEntity) obj, ErmCardinality.MULTIPLE);
            }
        }
        lastCreated = new GuiRelation(name, toModel(new Point(x, y)), map);
        UndoableAction temp = new UndoableAction() {
            private GuiRelation relation = (GuiRelation) lastCreated;

            @Override
            public void undo() {
                relations.remove(relation);
//                calcViewDimension();
                notifyDraw(true, true);
            }

            @Override
            public void redo() {
                relations.add(relation);
//                calcViewDimension();
                notifyDraw(true, false);
            }
        };
        temp.redo();
        return temp;
    }

    /**
     * Removes a relation from the diagram.
     *
     * @param relation relation to be removed
     * @return an undoable/redoable action
     */
    public UndoableAction remove(GuiRelation relation) {
        return remove(relation, true);
    }

    /**
     * Removes a relation and notifies the DrawingPanel if requested.
     *
     * @param relation relation to be removed
     * @param notyDrw  whether the DrawingPanel should be notified.
     * @return an undoable/redoable action
     */
    private UndoableAction remove(GuiRelation relation, boolean notyDrw) {
        UndoableAction temp = new UndoableAction() {
            @Override
            public void undo() {
                relations.add(relation);
                notifyDraw(true, false);
            }

            @Override
            public void redo() {
                relations.remove(relation);
                notifyDraw(true, true);
            }
        };
        if (notyDrw) {
            temp.redo();
        } else {
            relations.remove(relation);
        }
        return temp;
    }

    /**
     * Sets the cardinality of a connection between a relation and an entity to a new cardinality.
     *
     * @param relation    the relation
     * @param entity      the line to an entity
     * @param cardinality new cardinality
     * @return an undoable/redoable action
     */
    public UndoableAction setCardinality(GuiRelation relation, GuiLine<GuiEntity, ErmEntity> entity, ErmCardinality cardinality) {
        ReferencedAction temp = new ReferencedAction(relation.setCardinality(entity, cardinality)) {
            @Override
            public void after(boolean isUndo) {
                notifyDraw(false, false);
            }
        };
        temp.after(false);
        return temp;
    }

    /*******************************************************************************************************************
     *Generalization management
     ******************************************************************************************************************/
    /**
     * Adds a new relation to the diagram and connects it to any entities in the given set.
     *
     * @param x         x position of the relation
     * @param y         y position of the relation
     * @param selection set of objects (will filter for entities)
     * @return an undoable/redoable action
     */
    public UndoableAction newGeneralization(int x, int y, Set<GuiObject> selection) {
        HashSet<GuiEntity> set = new HashSet<>();
        for (GuiObject obj : selection) {
            if (obj instanceof GuiEntity) {
                set.add((GuiEntity) obj);
            }
        }
        lastCreated = new GuiGeneralization(toModel(new Point(x, y)), set);
        UndoableAction temp = new UndoableAction() {
            private GuiGeneralization generalization = (GuiGeneralization) lastCreated;

            @Override
            public void undo() {
                generalizations.remove(generalization);
                notifyDraw(true, true);
            }

            @Override
            public void redo() {
                generalizations.add(generalization);
                notifyDraw(true, false);
            }
        };
        temp.redo();
        return temp;
    }

    /**
     * Removes a generalization from the diagram.
     *
     * @param generalization the generalization to be removed
     * @return an undoable/redoable action
     */
    public UndoableAction remove(GuiGeneralization generalization) {
        return remove(generalization, true);
    }

    private UndoableAction remove(GuiGeneralization generalization, boolean notyDrw) {
        UndoableAction temp = new UndoableAction() {
            @Override
            public void undo() {
                generalizations.add(generalization);
                notifyDraw(true, false);
            }

            @Override
            public void redo() {
                generalizations.remove(generalization);
                notifyDraw(true, true);
            }
        };
        if (notyDrw) {
            temp.redo();
        } else {
            generalizations.remove(generalization);
        }
        return temp;
    }

    /**
     * Sets the supertype (super entity) of a generalization. If the given entity is a subtype as well it will be removed as such.
     *
     * @param generalization the generalization
     * @param entity         the new supertype (super entity)
     * @return an undoable/redoable action
     */
    public UndoableAction setSupertype(GuiGeneralization generalization, GuiEntity entity) {
        ReferencedAction temp = new ReferencedAction(generalization.setSupertype(entity)) {
            @Override
            public void after(boolean isUndo) {
                notifyDraw(true, false);
            }
        };
        temp.after(false);
        return temp;
    }

    /*******************************************************************************************************************
     * Line management
     ******************************************************************************************************************/
    /**
     * Adds a new point to a line.
     *
     * @param line  the line
     * @param p     the new point (coordinates)
     * @param index index of new point in the list
     * @return an undoable/redoable action
     */
    public UndoableAction addPoint(GuiLine line, Point p, int index) {
        return new ReferencedAction(line.addPoint(toModel(p), index)) {
            @Override
            public void after(boolean isUndo) {
                notifyDraw(false, false);
            }
        };
    }

    /**
     * Sets the coordinates of a point on a line.
     *
     * @param x     new x coordinate
     * @param y     new y coordinate
     * @param index index of the point to be set
     * @return an undoable/redoable action
     */
    public UndoableAction setPoint(GuiLine line, int x, int y, int index) {
        return new ReferencedAction(line.setPoint(toModelX(x), toModelY(y), index)) {
            @Override
            public void after(boolean isUndo) {
                notifyDraw(false, false);
            }
        };
    }

    /**
     * Removes a point from a line.
     *
     * @param line  the line
     * @param index index of point in list
     * @return an undoable/redoable action
     */
    public UndoableAction removePoint(GuiLine line, int index) {
        return new ReferencedAction(line.removePoint(index)) {
            @Override
            public void after(boolean isUndo) {
                notifyDraw(false, false);
            }
        };
    }

    /*******************************************************************************************************************
     * Utility functions
     ******************************************************************************************************************/
    /**
     * Increases the fontsize in the diagram. Max. is 100.
     */
    public void increaseFontSize() {
        if (fontSize < 100) {
            fontSize = Math.min(100, fontSize + 5);
            plugin.setFont(new Font("Arial", Font.PLAIN, fontSize));
            notifyDraw(false, false);
        }
    }

    /**
     * Decreases the fontsize in the diagram. Min. is 5.
     */
    public void decreaseFontSize() {
        if (fontSize > 5) {
            fontSize = Math.max(5, fontSize - 5);
            plugin.setFont(new Font("Arial", Font.PLAIN, fontSize));
            notifyDraw(false, false);
        }
    }

    /**
     * Sets whether the grid should be drawn.
     *
     * @param b whether grid should be drawn
     */
    public void showGrid(boolean b) {
        gridShow = b;
        notifyDraw(false, false);
    }

    /**
     * Returns the current setting whether grid should be drawn
     *
     * @return whether grid should be drawn
     */
    public boolean getGridShow() {
        return gridShow;
    }

    /**
     * Converts the coordinates of a point to the model coordinate system.
     *
     * @param p the point
     * @return the converted point
     */
    private Point toModel(Point p) {
        p.x = toModelX(p.x);
        p.y = toModelY(p.y);
        return p;
    }

    /**
     * Converts an x coordinate to the model coordinate system.
     *
     * @param x an x coordinate
     * @return the converted x coordinate
     */
    private int toModelX(int x) {
        return (int) (x / zoom) + viewDimension.getKey().x;
    }

    /**
     * Converts a y coordinate to the model coordinate system.
     *
     * @param y a y coordinate
     * @return the converted y coordinate
     */
    private int toModelY(int y) {
        return (int) (y / zoom) + viewDimension.getKey().y;
    }

    /**
     * Converts an x coordinate to the view coordinate system.
     *
     * @param x an x coordinate
     * @return the converted x coordinate
     */
    private int toViewX(int x) {
        return (int) ((x - viewDimension.getKey().x) * zoom);
    }

    /**
     * Converts an y coordinate to the view coordinate system.
     *
     * @param y an y coordinate
     * @return the converted y coordinate
     */
    private int toViewY(int y) {
        return (int) ((y - viewDimension.getKey().y) * zoom);
    }

    /**
     * Converts the coordinates of a point, adds an offset and stores it in another point.
     *
     * @param src  source point
     * @param dst  destination point
     * @param xOff x offset
     * @param yOff y offset
     * @return the destination point
     */
    private Point toView(Point src, Point dst, int xOff, int yOff) {
        dst.x = (int) ((src.x - viewDimension.getKey().x) * zoom) + xOff;
        dst.y = (int) ((src.y - viewDimension.getKey().y) * zoom) + yOff;
        return dst;
    }

    /**
     * Recalculates the dimension of the model. The result is saved in a cached pair of points.
     * The dimension of the model is defined as upmost left/top/right/bottom coordinates of all objects +/- an offset (so outermost objects are not cut off)
     *
     * @return the cached pair (copy, don't use directly)
     */
    public Pair<Point, Point> calcViewDimension() {
        viewDimension.getKey().x = 0;
        viewDimension.getKey().y = 0;
        viewDimension.getValue().x = Integer.MIN_VALUE;
        viewDimension.getValue().y = Integer.MIN_VALUE;
        Point pointArea = new Point(20, 20), objArea = new Point();
        for (GuiEntity ent : entities) {
            Pair<Point, Point> objDim = plugin.areaEntity(pointArea, 1.0f);
            objArea.setLocation(Math.abs(objDim.getValue().x - objDim.getKey().x), Math.abs(objDim.getValue().y - objDim.getKey().y));
            calcViewDimension(ent.getPosition(), objArea);
            for (GuiLine<GuiAttribute, ErmAttribute> att : ent.getAttributes()) {
                objDim = plugin.areaAttribute(att.getDestination().getPosition(), 1.0f);
                objArea.setLocation(Math.abs(objDim.getValue().x - objDim.getKey().x), Math.abs(objDim.getValue().y - objDim.getKey().y));
                calcViewDimension(att.getDestination().getPosition(), objArea);
                for (Point p : att.getPoints()) {
                    calcViewDimension(p, pointArea);
                }
            }
        }
        for (GuiRelation rel : relations) {
            Pair<Point, Point> objDim = plugin.areaRelation(pointArea, 1.0f);
            objArea.setLocation(Math.abs(objDim.getValue().x - objDim.getKey().x), Math.abs(objDim.getValue().y - objDim.getKey().y));
            calcViewDimension(rel.getPosition(), objArea);
            for (GuiLine<GuiAttribute, ErmAttribute> att : rel.getAttributes()) {
                for (Point p : att.getPoints()) {
                    calcViewDimension(p, pointArea);
                }
            }
            for (GuiLine<GuiEntity, ErmEntity> line : rel.getConnections()) {
                for (Point p : line.getPoints()) {
                    calcViewDimension(p, pointArea);
                }
            }
        }
        for (GuiGeneralization gen : generalizations) {
            Pair<Point, Point> objDim = plugin.areaGeneralization(pointArea, 1.0f);
            objArea.setLocation(Math.abs(objDim.getValue().x - objDim.getKey().x), Math.abs(objDim.getValue().y - objDim.getKey().y));
            calcViewDimension(gen.getPosition(), objArea);
            for (GuiLine<GuiEntity, ErmEntity> line : gen.getConnections()) {
                for (Point p : line.getPoints()) {
                    calcViewDimension(p, pointArea);
                }
            }
        }
        if (viewDimension.getKey().x > viewDimension.getValue().x) {
            viewDimension.getKey().x = 0;
            viewDimension.getValue().x = 0;
        }
        if (viewDimension.getKey().y > viewDimension.getValue().y) {
            viewDimension.getKey().y = 0;
            viewDimension.getValue().y = 0;
        }
        return viewDimension;
    }

    /**
     * Helper method for calculating the model dimension.
     *
     * @param p      position of an object
     * @param offset offset to add to the position
     */
    private void calcViewDimension(Point p, Point offset) {
        viewDimension.getKey().x = Math.min(viewDimension.getKey().x, p.x - offset.x);
        viewDimension.getKey().y = Math.min(viewDimension.getKey().y, p.y - offset.y);
        viewDimension.getValue().x = Math.max(viewDimension.getValue().x, p.x + offset.x);
        viewDimension.getValue().y = Math.max(viewDimension.getValue().y, p.y + offset.y);
    }

    /**
     * list to cache points for drawing
     */
    private ArrayList<Point> pointscopy = new ArrayList<>();
    /**
     * point caches
     */
    private Point cachepoint = new Point();
    /**
     * zoom factor of diagram
     */
    private float zoom = 1.5f;

    /**
     * Returns the current zoom factor of the diagram.
     *
     * @return current zoom factor
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * whether the zoom has changed
     */
    private boolean zoomed = false;

    /**
     * Returns whether the zoom has changed since the last call to this method.
     *
     * @return whether zoom has changed
     */
    public boolean hasZoomed() {
        if (zoomed) {
            zoomed = false;
            return true;
        }
        return false;
    }

    /**
     * Fits the zoom to show the complete diagram on the viewport with the given width and height.
     *
     * @param width  width of viewport
     * @param height height of viewport
     */
    public void fitZoom(int width, int height) {
        calcViewDimension();
        zoom = Math.max(Math.min((float) width / (viewDimension.getValue().x - viewDimension.getKey().x), (float) height / (viewDimension.getValue().y - viewDimension.getKey().y)), 0.2f);
        zoomed = true;
        notifyDraw(false, false);
    }

    /**
     * Changes the zoom by multiplying or dividing it by a factor.
     *
     * @param zoomin     whether the model should zoom in or out
     * @param zoomfactor factor to zoom in or out by
     * @return if actually has zoomed
     */
    public boolean changeZoom(boolean zoomin, float zoomfactor) {
        if (zoomin && zoom >= 4.995) {
            return false;
        } else if (!zoomin && zoom <= 0.205) {
            return false;
        }
        zoomed = true;
        zoom = Math.min(5f, Math.max(zoomin ? zoom / zoomfactor : zoom * zoomfactor, 0.2f));
        notifyDraw(false, false);
        return true;
    }

    /**
     * Converts all points in a given list to the view coordinate system, adds an offset and copies them to a cache list ({@link #pointscopy}).
     *
     * @param points        a list of points
     * @param xSelOff       an x offset to add to the points
     * @param ySelOff       an y offset to add to the points
     * @param offFirstPoint whether the first point should be offset
     * @param offLastPoint  whether the last point should be offset
     * @return the cache point list
     */
    private ArrayList<Point> toView(List<Point> points, int xSelOff, int ySelOff, boolean offFirstPoint, boolean offLastPoint) {
        Iterator<Point> it = points.iterator();
        //overwrite existing points in caching list
        for (int i = 0; i < pointscopy.size() && it.hasNext(); i++) {
            Point p = pointscopy.get(i);
            if (offFirstPoint && offLastPoint) {
                toView(it.next(), p, xSelOff, ySelOff);
            } else {
                toView(it.next(), p, 0, 0);
            }
        }
        //append points to caching list if not enough
        while (it.hasNext()) {
            Point p = new Point();
            if (offFirstPoint && offLastPoint) {
                toView(it.next(), p, xSelOff, ySelOff);
            } else {
                toView(it.next(), p, 0, 0);
            }
            pointscopy.add(p);
        }
        //trim down caching list to size if too big
        if (pointscopy.size() > points.size()) {
            pointscopy.subList(points.size(), pointscopy.size()).clear();
        }
        //if not complete line offset, maybe just first or last one
        if (offFirstPoint && !offLastPoint) {
            Point p = pointscopy.get(0);
            p.x += xSelOff;
            p.y += ySelOff;
        } else if (!offFirstPoint && offLastPoint) {
            Point p = pointscopy.get(pointscopy.size() - 1);
            p.x += xSelOff;
            p.y += ySelOff;
        }
        return pointscopy;
    }

    /**
     * Sets what phantom object should be drawn (for adding new objects).
     *
     * @param p the phantom DrawingStatus
     */
    public void setPhantom(DrawingStatus p) {
        phantom = p;
    }

    /**
     * Sets the position of the phantom object (for adding new objects).
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void setPhantomPos(int x, int y) {
        phantomPos.x = x;
        phantomPos.y = y;
    }

    /**
     * Sets the information of the phantom point (for transforming lines).
     *
     * @param l     the line
     * @param index index of the point
     * @param ins   whether the point is a new or an existing one
     */
    public void setPhantomPoint(GuiLine l, int index, boolean ins) {
        phantomPointLine = l;
        phantomPointIndex = index;
        phantomPointAdded = ins;
    }

    /**
     * Seths the position of the phantom point (for transforming lines).
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void setPhantomPoint(int x, int y) {
        phantomPoint.x = x;
        phantomPoint.y = y;
    }

    /**
     * Snaps an x coordinate to the next closest vertical grid line.
     *
     * @param x x coordinate
     * @return the x coordinate of the closest vertical grid line
     */
    public int snapToGridX(int x) {
        return toViewX((Math.round(toModelX(x) / (gridSize)) * (gridSize)));
    }

    /**
     * Snaps an y coordinate to the next closest horizontal grid line.
     *
     * @param y y coordinate
     * @return the y coordinate of the closest horizontal grid line
     */
    public int snapToGridY(int y) {
        return toViewY((Math.round(toModelY(y) / (gridSize)) * (gridSize)));
    }

    /*******************************************************************************************************************
     * Drawing functions
     ******************************************************************************************************************/
    /**
     * Draws the model onto a given graphics object.
     *
     * @param graphics  the graphics object
     * @param selection all selected objects
     * @param width     width of the viewport
     * @param height    height of the viewport
     * @param xSelOff   x offset of all selected objects (for dragging)
     * @param ySelOff   y offset of all selected objects (for dragging)
     */
    public void drawBoard(Graphics2D graphics, Set<GuiObject> selection, int width, int height, int xSelOff, int ySelOff) {
        calcViewDimension();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, Math.max(width, (int) ((viewDimension.getValue().x - viewDimension.getKey().x) * zoom)), Math.max(height, (int) ((viewDimension.getValue().y - viewDimension.getKey().y) * zoom)));
        if (gridShow) {
            drawGrid(graphics, width, height);
        }
        drawLines(graphics, selection, xSelOff, ySelOff);
        drawObjects(graphics, selection, xSelOff, ySelOff);
        drawPhantom(graphics);
        cachepoint = new Point();
    }

    /**
     * Draws the grid onto a graphics object.
     *
     * @param graphics the graphics object
     * @param width    width of viewport
     * @param height   height of viewport
     */
    private void drawGrid(Graphics2D graphics, int width, int height) {
        graphics.setColor(Color.LIGHT_GRAY);
        int left = toViewX((viewDimension.getKey().x / gridSize) * gridSize), top = toViewY((viewDimension.getKey().y / gridSize) * gridSize);
        float gridsizezoom = gridSize * zoom;
        for (int i = 0, curLeft, end = (int) ((width - left) / gridsizezoom) + 1; i < end; i++) {
            curLeft = (int) (left + i * gridsizezoom);
            graphics.drawLine(curLeft, 0, curLeft, height);
        }
        for (int i = 0, curTop, end = (int) ((height - top) / gridsizezoom) + 1; i < end; i++) {
            curTop = (int) (top + i * gridsizezoom);
            graphics.drawLine(0, curTop, width, curTop);
        }
    }

    /**
     * Draws the lines connecting attributes, entites, relations and generalizations onto a graphics object.
     *
     * @param graphics  the graphics object
     * @param selection all selected objects
     * @param xSelOff   x offset of selected objects (for dragging)
     * @param ySelOff   y offset of selected objects (for dragging)
     */
    private void drawLines(Graphics2D graphics, Set<GuiObject> selection, int xSelOff, int ySelOff) {
        //Entities - Attributes
        for (GuiEntity ent : entities) {
            boolean selected = selection.contains(ent);
            for (GuiLine<GuiAttribute, ErmAttribute> l : ent.getAttributes()) {
                toView(l.getPoints(), xSelOff, ySelOff, selected, selected || selection.contains(l.getDestination()));
                if (l.equals(phantomPointLine)) {
                    if (phantomPointAdded) {
                        pointscopy.add(phantomPointIndex, new Point(phantomPoint));
                    } else {
                        pointscopy.get(phantomPointIndex).setLocation(phantomPoint.x, phantomPoint.y);
                    }
                }
                plugin.drawLine(graphics, pointscopy);
            }
        }
        //Relations - Attributes, Relations - Entities
        for (GuiRelation rel : relations) {
            boolean selected = selection.contains(rel);
            for (GuiLine<GuiAttribute, ErmAttribute> l : rel.getAttributes()) {
                toView(l.getPoints(), xSelOff, ySelOff, selected, selected || selection.contains(l.getDestination()));
                if (l.equals(phantomPointLine)) {
                    if (phantomPointAdded) {
                        pointscopy.add(phantomPointIndex, new Point(phantomPoint));
                    } else {
                        pointscopy.get(phantomPointIndex).setLocation(phantomPoint.x, phantomPoint.y);
                    }
                }
                plugin.drawLine(graphics, pointscopy);
            }
            for (GuiLine<GuiEntity, ErmEntity> l : rel.getConnections()) {
                toView(l.getPoints(), xSelOff, ySelOff, selected, selection.contains(l.getDestination()));
                if (l.equals(phantomPointLine)) {
                    if (phantomPointAdded) {
                        pointscopy.add(phantomPointIndex, new Point(phantomPoint));
                    } else {
                        pointscopy.get(phantomPointIndex).setLocation(phantomPoint.x, phantomPoint.y);
                    }
                }
                plugin.drawLine(graphics, pointscopy, zoom, rel.getCardinality(l));
            }
        }
        //Generalizations - Entities
        for (GuiGeneralization gen : generalizations) {
            boolean selected = selection.contains(gen);
            Pair<Point, Point> area = plugin.areaGeneralization(toView(gen.getPosition(), cachepoint, selected ? xSelOff : 0, selected ? ySelOff : 0), zoom);
            if (gen.hasSuperline()) {
                toView(gen.getSuperline().getPoints(), xSelOff, ySelOff, selected, selection.contains(gen.getSuperline().getDestination()));
                pointscopy.get(0).y = area.getKey().y;
                if (gen.getSuperline().equals(phantomPointLine)) {
                    if (phantomPointAdded) {
                        pointscopy.add(phantomPointIndex, new Point(phantomPoint));
                    } else {
                        pointscopy.get(phantomPointIndex).setLocation(phantomPoint.x, phantomPoint.y);
                    }
                }
                plugin.drawLine(graphics, pointscopy);
            }
            for (GuiLine<GuiEntity, ErmEntity> l : gen.getConnections()) {
                toView(l.getPoints(), xSelOff, ySelOff, selected, selection.contains(l.getDestination()));
//                pointscopy.get(0).y = area.getValue().y + ySelOff;
                if (l.equals(phantomPointLine)) {
                    if (phantomPointAdded) {
                        pointscopy.add(phantomPointIndex, new Point(phantomPoint));
                    } else {
                        pointscopy.get(phantomPointIndex).setLocation(phantomPoint.x, phantomPoint.y);
                    }
                }
                plugin.drawLine(graphics, pointscopy);
            }
        }
    }

    /**
     * Draws the attributes, entities, relations and generalizations onto a graphics object.
     *
     * @param graphics  the graphics object
     * @param selection all selected objects
     * @param xSelOff   x offset of selection (for dragging)
     * @param ySelOff   y offset of selection (for dragging)
     */
    private void drawObjects(Graphics2D graphics, Set<GuiObject> selection, int xSelOff, int ySelOff) {
        for (GuiEntity ent : entities) {
            boolean selected = selection.contains(ent);
            plugin.drawEntity(graphics, toView(ent.getPosition(), cachepoint, selected ? xSelOff : 0, selected ? ySelOff : 0), zoom, ent.getName(), selection.contains(ent));
            for (GuiLine<GuiAttribute, ErmAttribute> l : ent.getAttributes()) {
                GuiAttribute att = l.getDestination();
                boolean selectedAtt = selection.contains(att);
                plugin.drawAttribute(graphics, toView(att.getPosition(), cachepoint, (selected || selectedAtt) ? xSelOff : 0, (selected || selectedAtt) ? ySelOff : 0), zoom, att.getName(), att.getPrimary(), selection.contains(att));
            }
        }
        for (GuiRelation rel : relations) {
            boolean selected = selection.contains(rel);
            plugin.drawRelation(graphics, toView(rel.getPosition(), cachepoint, selected ? xSelOff : 0, selected ? ySelOff : 0), zoom, rel.getName(), selection.contains(rel));
            for (GuiLine<GuiAttribute, ErmAttribute> l : rel.getAttributes()) {
                GuiAttribute att = l.getDestination();
                boolean selectedAtt = selection.contains(att);
                plugin.drawAttribute(graphics, toView(att.getPosition(), cachepoint, (selected || selectedAtt) ? xSelOff : 0, (selected || selectedAtt) ? ySelOff : 0), zoom, att.getName(), att.getPrimary(), selection.contains(att));
            }
        }
        for (GuiGeneralization gen : generalizations) {
            boolean selected = selection.contains(gen);
            plugin.drawGeneralization(graphics, toView(gen.getPosition(), cachepoint, selected ? xSelOff : 0, selected ? ySelOff : 0), zoom, selection.contains(gen));
        }

    }

    /**
     * Draws the phantom object onto a graphics object.
     *
     * @param graphics the graphics object
     */
    private void drawPhantom(Graphics2D graphics) {
        cachepoint.setLocation(phantomPos);
        switch (phantom) {
            case NEW_ATTRIBUTE:
                plugin.drawAttribute(graphics, cachepoint, zoom, "", false, false);
                break;
            case NEW_ENTITY:
            case NEW_ENTITY_TO_CONNECTION:
                plugin.drawEntity(graphics, cachepoint, zoom, "", false);
                break;
            case NEW_GENERALIZATION:
                plugin.drawGeneralization(graphics, cachepoint, zoom, false);
                break;
            case NEW_RELATION:
                plugin.drawRelation(graphics, cachepoint, zoom, "", false);
        }
    }

    /**
     * Sets the DrawingPanel to notify for redrawing the diagram.
     * This will also call {@link DrawingPanel#setModel(GuiModel)} to set this model as it's model.
     *
     * @param p the DrawingPanel
     */
    public void setPanel(DrawingPanel p) {
        panel = p;
        p.setModel(this);
    }

    /**
     * Notifies the DrawingPanel to redraw the diagram.
     *
     * @param updateProperties whether the DrawingPanel should in turn update the properties panel of the MainWindow
     * @param checkSelection   whether the DrawingPanel should check it's current selection for deleted objects.
     */
    private void notifyDraw(boolean updateProperties, boolean checkSelection) {
        if (panel == null) {
            return;
        }
        if (updateProperties) {
            panel.notifyProperties(checkSelection);
        }
        panel.notifyDraw();
    }

    /*******************************************************************************************************************
     * XML functions
     ******************************************************************************************************************/

    /**
     * Writes all Objects from the GuiModel to a Document
     *
     * @return the document with all the elements in it
     */
    private Document getXML() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element root = doc.createElement("cheese");
            root.setAttribute("fontSize", String.valueOf(fontSize));

            for (GuiEntity ent : entities) {
                Element entity = doc.createElement("entity");
                root.appendChild(entity);
                root.appendChild(ent.getXML(doc, entity));
            }
            for (GuiRelation rel : relations) {
                Element relation = doc.createElement("relation");
                root.appendChild(relation);
                root.appendChild(rel.getXML(doc, relation));
            }
            for (GuiGeneralization gen : generalizations) {
                Element generalization = doc.createElement("generalization");
                root.appendChild(generalization);
                root.appendChild(gen.getXML(doc, generalization));
            }
            doc.appendChild(root);
            return doc;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param file the file to write the XML document to
     * @throws IOException
     * @throws TransformerException
     */
    public void writeXML(File file) throws IOException, TransformerException {
        DOMSource source = new DOMSource(getXML());
        FileWriter writer = new FileWriter(file);
        StreamResult result = new StreamResult(writer);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(source, result);
    }

    /**
     * Loads all elements from a saved file to the GuiModel
     *
     * @param file the file from which to load the elements
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public void readXML(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        Element root = doc.getDocumentElement();
        fontSize = Integer.valueOf(root.getAttribute("fontSize"));
        plugin.setFont(new Font("Arial", Font.PLAIN, fontSize));
        Node child = root.getFirstChild();
        Map<String, GuiEntity> entityMap = new HashMap<>();
        while (child.getNextSibling() != null) {
            child = child.getNextSibling();
            NodeList nodes = child.getChildNodes();
            Element element = null;
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                element = (Element) child;
            }

            switch (child.getNodeName()) {
                case "entity":
                    GuiEntity entity = new GuiEntity(element);
                    entityMap.put(element.getAttribute("id"), entity);
                    entities.add(entity);
                    break;

                case "relation":
                    relations.add(new GuiRelation(element, entityMap));
                    break;

                case "generalization":
                    generalizations.add(new GuiGeneralization(element, entityMap));
                    break;
            }

        }
    }
}




