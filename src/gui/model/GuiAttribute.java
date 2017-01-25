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

import action.UndoableAction;
import model.ErmAttribute;
import model.ErmAttributed;
import model.ErmObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.*;

/**
 * This class describes attributes to the GUI.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class GuiAttribute extends GuiObject {
    /**
     * the ERM equivalent of this GuiAttribute
     */
    private ErmAttribute attribute;
    /**
     * the owner of this attribute
     */
    private GuiAttributed owner;

    /**
     * Constructs a new attribute with the given name, owner and absolute position.
     * The relative position will be calculated/managed in GuiAttribute.
     *
     * @param name  name of attribute
     * @param owner owner of attribute
     * @param pos   absolute position
     */
    GuiAttribute(String name, GuiAttributed owner, Point pos) {
        super(pos);
        this.owner = owner;
        attribute = new ErmAttribute(name, (ErmAttributed) owner.getErmObject());
    }

    /**
     * Constructs a new attribute from a XML element for an owner
     *
     * @param element the XML element
     * @param owner   the owner of the attribute
     */
    GuiAttribute(Element element, GuiAttributed owner) {
        super(element);
        this.owner = owner;
        attribute = new ErmAttribute(element.getElementsByTagName("name").item(0).getTextContent(), (ErmAttributed) owner.getErmObject());
        setPrimary(Boolean.valueOf(element.getElementsByTagName("primary").item(0).getTextContent()));
    }

    /**
     * Returns the ERM equivalent of this GuiAttribute
     *
     * @return the ERM equivalent
     */
    ErmAttribute getErmAttribute() {
        return attribute;
    }

    /**
     * The owner of the attribute.
     *
     * @return the owner
     */
    GuiAttributed getOwner() {
        return owner;
    }

    /**
     * Sets the primary property of this attribute.
     *
     * @param primary primary key property
     * @return an undoable/redoable action
     */
    UndoableAction setPrimary(boolean primary) {
        return attribute.setPrimary(primary);
    }

    /**
     * Returns whether this attribute is (part of) the primary key.
     *
     * @return primary key property
     */
    public boolean getPrimary() {
        return attribute.getPrimary();
    }

    @Override
    ErmObject getErmObject() {
        return attribute;
    }


    /**
     *Returns the XML equivalent of this GuiAttribute
     *
     * @param doc the XMLDocument
     * @param element the XMLElement to be filled
     * @return the corresponding XMLElement
     */
    @Override
    Element getXML(Document doc, Element element) {
        Element attribute = super.getXML(doc, element);
        Element primary = doc.createElement("primary");
        primary.appendChild(doc.createTextNode(String.valueOf(getPrimary())));
        attribute.appendChild(primary);
        return attribute;
    }
}
