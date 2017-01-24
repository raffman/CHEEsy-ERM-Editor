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
import model.ErmObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * This abstract class summarizes general functionality of objects (entities, attributes, generalizations and relations) to the GUI.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public abstract class GuiObject {
    /**
     * the position of the object within the model (not the view)
     */
    private Point pos;

    /**
     * Ctor
     * @param pos the position
     */
    GuiObject(Point pos) {
        this.pos = pos;
    }

    /**
     * Ctor for loading XML
     * @param element the xml element representing the object
     */
    GuiObject(Element element) {
        int xPos = Integer.valueOf(element.getElementsByTagName("xPos").item(0).getTextContent());
        int yPos = Integer.valueOf(element.getElementsByTagName("yPos").item(0).getTextContent());
        pos = new Point(xPos, yPos);

    }

    /**
     * Name of the object
     * @return name
     */
    public String getName() {
        return getErmObject().getName();
    }

    /**
     * Sets the name. An UndoableAction is created higher up the food chain.
     * @param name the new name
     */
    void setName(String name) {
        getErmObject().setName(name);
    }

    /**
     * Sets the position to a given absolute position.
     *
     * @param xNew x of new position
     * @param yNew y of new position
     * @return an undoable/redoable action
     */
    UndoableAction setPos(int xNew, int yNew) {
        UndoableAction temp = new UndoableAction() {
            private int xPrev = pos.x, yPrev = pos.y;

            @Override
            public void undo() {
                pos.x = xPrev;
                pos.y = yPrev;
            }

            @Override
            public void redo() {
                pos.x = xNew;
                pos.y = yNew;
            }
        };
        temp.redo();
        return temp;
    }

    /**
     * Shifts the object's position by given x and y values.
     * GuiAttributed, Relations and Generalizations will have to overwrite their respective super methods to shift lines as well depending on selection.
     * Note: do not call for GuiAttributes who's owners get shifted as well!
     *
     * @param x x shift
     * @param y y shift
     * @param selection currently selected objects
     * @return an undoable/redoable action
     */
    UndoableAction shiftPos(int x, int y, Set<GuiObject> selection) {
        UndoableAction temp = new UndoableAction() {
            @Override
            public void undo() {
                pos.x -= x;
                pos.y -= y;
            }

            @Override
            public void redo() {
                pos.x += x;
                pos.y += y;
            }
        };
        temp.redo();
        return temp;
    }

    /**
     * Returns the absolute position.
     * The point should not be changed outside of GuiObject!
     *
     * @return absolute position
     */
    Point getPosition() {
        return pos;
    }

    /**
     * Returns the ErmObject equivalent of this GuiObject.
     * @return the ErmObject
     */
    abstract ErmObject getErmObject();

    /**
     * Takes an element and fills in the information for export as XML.
     *
     * @param doc the XMLDocument
     * @param element the XMLElement to be filled
     * @return the filled XMLElement
     */
    Element getXML(Document doc, Element element) {
        if (!(this instanceof GuiGeneralization)) {
            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(getErmObject().getName()));
            element.appendChild(name);
        }
        element.setAttribute("id", String.valueOf(getErmObject().hashCode()));
        Element xPos = doc.createElement("xPos");
        xPos.appendChild(doc.createTextNode(String.valueOf(pos.x)));
        Element yPos = doc.createElement("yPos");
        yPos.appendChild(doc.createTextNode(String.valueOf(pos.y)));
        element.appendChild(xPos);
        element.appendChild(yPos);


        return element;
    }

    /**
     * Returns a XMLElement which contains all the points from the guiLine
     *
     * @param guiLine the line which to extract the points from
     * @param doc the XMLDocument
     * @return the filled line element
     */
    Element getLineXML(List<Point> guiLine, Document doc) {
        if (guiLine.size() > 2) {
            Element line = doc.createElement("line");
            for (int i = 1; i < guiLine.size() - 1; i++) {
                Element point = doc.createElement("point");
                point.setAttribute("key", String.valueOf(i));
                Element posX = doc.createElement("xPos");
                posX.appendChild(doc.createTextNode(String.valueOf(guiLine.get(i).x)));
                point.appendChild(posX);
                Element posY = doc.createElement("yPos");
                posY.appendChild(doc.createTextNode(String.valueOf(guiLine.get(i).y)));
                point.appendChild(posY);
                line.appendChild(point);
            }
            return line;
        }
        return null;
    }

    /**
     * Loads the line of an object back into the GuiModel
     *
     * @param line the line witch to extract the points from
     * @param pointList the List to write the points to
     */
    void loadLine(Node line, List<Point> pointList){
        try {
            Element lineElement = (Element) line;
            NodeList nodes = lineElement.getElementsByTagName("point");
            for (int i = 0; i < nodes.getLength(); i++) {
                Point pt = new Point(Integer.valueOf(((Element) nodes.item(i)).getElementsByTagName("xPos").item(0).getTextContent()), Integer.valueOf(((Element) nodes.item(i)).getElementsByTagName("yPos").item(0).getTextContent()));
                pointList.add(i + 1, pt);
            }
        } catch (NullPointerException e){
            //not important if caught nullptex
            //System.out.println("No line found");
        }
    }
}
