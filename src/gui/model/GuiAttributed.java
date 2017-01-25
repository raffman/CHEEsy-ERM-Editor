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
import action.UndoableAction;
import model.ErmAttribute;
import model.ErmAttributed;
import model.ErmLine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract class to summarize attribute management.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public abstract class GuiAttributed extends GuiObject {
    /**
     * set of lines connecting the owned attributes
     */
    private Set<GuiLine<GuiAttribute, ErmAttribute>> attributes = new HashSet<>();

    /**
     * Ctor to pass a given position to the super Ctor.
     *
     * @param pos the position
     */
    GuiAttributed(Point pos) {
        super(pos);
    }

    /**
     * Ctor to pass on the XML Element to the super Ctor.
     *
     * @param element the XML element
     */
    GuiAttributed(Element element) {
        super(element);
    }

    /**
     * Returns the set of lines connecting the attributes.
     *
     * @return set of lines
     */
    public Set<GuiLine<GuiAttribute, ErmAttribute>> getAttributes() {
        return attributes;
    }

    /**
     * Adds an attribute.
     *
     * @param attribute the new attribute
     * @return an undoable/redoable action
     */
    UndoableAction addAttribute(GuiAttribute attribute) {
        CombinedAction actions = new CombinedAction();
        //update Erm Object
        ErmLine<ErmAttribute> newermline = new ErmLine<>(attribute.getErmAttribute());
        actions.add(((ErmAttributed) getErmObject()).addAttribute(newermline));
        //UndoableAction for Gui
        UndoableAction temp = new UndoableAction() {

            private GuiLine<GuiAttribute, ErmAttribute> newline = new GuiLine<>(newermline, GuiAttributed.this, attribute);

            @Override
            public void undo() {
                attributes.remove(newline);
            }

            @Override
            public void redo() {
                attributes.add(newline);
            }
        };
        actions.add(temp);
        //update Gui Object
        temp.redo();
        return actions;
    }

    @Override
    UndoableAction shiftPos(int x, int y, Set<GuiObject> selection) {
        if (attributes.size() > 0) {
            CombinedAction actions = new CombinedAction();
            actions.add(super.shiftPos(x, y, selection));
            for (GuiLine<GuiAttribute, ErmAttribute> l : attributes) {
                actions.add(l.shiftPoints(x, y));
                actions.add(l.getDestination().shiftPos(x, y, selection));
            }
            return actions;
        }
        return super.shiftPos(x, y, selection);
    }

    /**
     * Removes an attribute.
     *
     * @param attribute the attribute
     * @return an undoable/redoable action
     */
    UndoableAction removeAttribute(GuiAttribute attribute) {
        CombinedAction actions = new CombinedAction();
        //find correct GuiLine

        GuiLine<GuiAttribute, ErmAttribute> ol = null;
        for (GuiLine<GuiAttribute, ErmAttribute> l : attributes) {
            if (l.getDestination().equals(attribute)) {
                ol = l;
                break;
            }
        }
        if (ol == null) {
            throw new WrongGuiObjectException("GuiAttribute is not connected to this GuiAttributed");
        }
        final GuiLine<GuiAttribute, ErmAttribute> oldline = ol;
        //update Erm Object
        actions.add(((ErmAttributed) getErmObject()).removeAttribute(oldline.getErmline()));
        //UndoableAction for Gui
        UndoableAction temp = new UndoableAction() {
            @Override
            public void undo() {
                attributes.add(oldline);
            }

            @Override
            public void redo() {
                attributes.remove(oldline);
            }
        };
        actions.add(temp);
        //update Gui Object
        temp.redo();
        return actions;
    }

    /**
     * Returns a List of XMLElements of all attributes of this GuiAttributed
     *
     * @param doc the document to create the attribute element
     * @return a list of all attributes
     */
    ArrayList<Element> getAttributesLinesXML(Document doc) {
        ArrayList<Element> lineList = new ArrayList<>();

        for (GuiLine<GuiAttribute, ErmAttribute> att : attributes) {
            Element attribute = doc.createElement("attribute");
            attribute = att.getDestination().getXML(doc, attribute);
            Element line = getLineXML(att.getPoints(), doc);
            if (line != null) {
                attribute.appendChild(line);
            }
            lineList.add(attribute);
        }

        return lineList;
    }

    /**
     * Loads all attributes and their Lines
     *
     * @param element the parent element of the attributes to be loaded
     */
    void loadAttributes(Element element) {
        NodeList attributes = element.getElementsByTagName("attribute");

        for (int i = 0; i < attributes.getLength(); i++) {
            Element attElement = (Element) (attributes.item(i));
            GuiAttribute att = new GuiAttribute(attElement, this);

            this.addAttribute(att);

            for (GuiLine guiLine : this.getAttributes()) {
                if (guiLine.getDestination() == att) {
                    loadLine(attElement.getElementsByTagName("line").item(0), guiLine.getPoints());
                }
            }
        }
    }
}
