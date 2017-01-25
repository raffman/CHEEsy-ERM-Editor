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

import model.ErmEntity;
import model.ErmObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.*;

/**
 * This class describes entities to the GUI.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class GuiEntity extends GuiAttributed {
    /**
     * the ErmEntity equivalent to this GuiEntity
     */
    private ErmEntity entity;

    /**
     * Ctor with given name and position.
     *
     * @param name name of the entity
     * @param pos  position of the entity
     */
    GuiEntity(String name, Point pos) {
        super(pos);
        entity = new ErmEntity(name);
    }

    /**
     * Constructs an entity from a given XML element.
     *
     * @param element the XML element
     */
    GuiEntity(Element element) {
        super(element);
        entity = new ErmEntity(element.getElementsByTagName("name").item(0).getTextContent());
        loadAttributes(element);
    }

    /**
     * Returns the ERM equivalent of this GuiEntity.
     *
     * @return the ErmEntity equivalent
     */
    public ErmEntity getEntity() {
        return entity;
    }

    @Override
    ErmObject getErmObject() {
        return entity;
    }

    /**
     * Returns the XML equivalent of this GuiEntity
     *
     * @param doc the XMLDocument
     * @param element the base XMLElement from which the entity XMLElement will be created
     * @return the corresponding XMLElement
     */
    Element getXML(Document doc, Element element) {
        Element entity = super.getXML(doc, element);
        for (Element e : getAttributesLinesXML(doc)) {
            entity.appendChild(e);
        }
        return entity;
    }
}
