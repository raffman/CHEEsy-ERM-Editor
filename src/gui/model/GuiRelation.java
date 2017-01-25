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
import model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.*;

/**
 * This class describes relations to the GUI.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class GuiRelation extends GuiAttributed implements GuiConnection {
    /**
     * the ErmRelation equivalent of this GuiRelation object
     */
    private ErmRelation relation;
    /**
     * the set of lines connecting entities
     * NOTE: the cardinalities are saved in the ErmRelation object
     */
    private Set<GuiLine<GuiEntity, ErmEntity>> lines = new HashSet<>();

    /**
     * Constructs a new relation with a given name and position, as well as a list of related entities.
     * The given list will be copied and can be reused outside.
     *
     * @param name     name of the relation
     * @param pos      position of the entity
     * @param entities the map of related entities
     */
    GuiRelation(String name, Point pos, Map<GuiEntity, ErmCardinality> entities) {
        super(pos);
        HashMap<ErmLine<ErmEntity>, ErmCardinality> map = new HashMap<>();
        for (GuiEntity ent : entities.keySet()) {
            ErmLine<ErmEntity> ermLine = new ErmLine<>(ent.getEntity());
            map.put(ermLine, entities.get(ent));
            lines.add(new GuiLine<>(ermLine, this, ent));
        }
        relation = new ErmRelation(name, map);
    }

    /**
     * Constructs a relation from a given XML element and a (?) string-entity-map
     *
     * @param element   the XML element
     * @param entityMap a map
     */
    GuiRelation(Element element, Map<String, GuiEntity> entityMap) {
        super(element);
        HashMap<ErmLine<ErmEntity>, ErmCardinality> map = new HashMap<>();
        NodeList nodes = element.getElementsByTagName("entity");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element entity = (Element) nodes.item(i);
            GuiEntity guiEntity = entityMap.get(entity.getElementsByTagName("entityID").item(0).getTextContent());
            ErmLine<ErmEntity> ermLine = new ErmLine<>(guiEntity.getEntity());
            map.put(ermLine, ErmCardinality.valueOf(entity.getAttribute("cardinality")));
            GuiLine<GuiEntity, ErmEntity> guiLine = new GuiLine<>(ermLine, this, guiEntity);
            loadLine(entity.getElementsByTagName("line").item(0), guiLine.getPoints());
            lines.add(guiLine);
        }
        relation = new ErmRelation(element.getElementsByTagName("name").item(0).getTextContent(), map);
        loadAttributes(element);
    }

    /**
     * Sets the cardinality of a line.
     *
     * @param entity      the line to an entity
     * @param cardinality the new cardinality
     * @return an undoable/redoable action
     */
    UndoableAction setCardinality(GuiLine<GuiEntity, ErmEntity> entity, ErmCardinality cardinality) {
        return relation.setCardinality(entity.getErmline(), cardinality);
    }

    /**
     * Returns the cardinality for a line.
     *
     * @param entity the line to an entity
     * @return the corresponding cardinality
     */
    ErmCardinality getCardinality(GuiLine<GuiEntity, ErmEntity> entity) {
        return relation.getCardinality(entity.getErmline());
    }

    /**
     * Returns the ErmRelation equivalent to this GuiRelation.
     *
     * @return the ErmRelation equivalent
     */
    ErmRelation getRelation() {
        return relation;
    }

    /**
     * Returns an iterator over the connections.
     *
     * @return an iterator
     */
    Iterable<GuiLine<GuiEntity, ErmEntity>> getConnections() {
        return lines;
    }

    @Override
    UndoableAction shiftPos(int x, int y, Set<GuiObject> selection) {
        CombinedAction actions = null;
        for (GuiLine<GuiEntity, ErmEntity> l : lines) {
            if (selection.contains(l.getDestination())) {
                if (actions == null) {
                    actions = new CombinedAction();
                }
                actions.add(l.shiftPoints(x, y));
            }
        }
        if (actions != null) {
            actions.add(super.shiftPos(x, y, selection));
            return actions;
        }
        return super.shiftPos(x, y, selection);
    }

    @Override
    public UndoableAction addConnection(GuiEntity entity, ErmCardinality cardinality) {
        CombinedAction actions = new CombinedAction();
        //update Erm Object
        ErmLine<ErmEntity> ermLine = new ErmLine<>(entity.getEntity());
        actions.add(relation.addConnection(ermLine, cardinality));
        //UndoableAction for Gui
        UndoableAction temp = new UndoableAction() {
            private GuiLine<GuiEntity, ErmEntity> guiLine = new GuiLine<>(ermLine, GuiRelation.this, entity);

            @Override
            public void undo() {
                lines.remove(guiLine);
            }

            @Override
            public void redo() {
                lines.add(guiLine);
            }
        };
        actions.add(temp);
        //update Gui Object
        temp.redo();
        return actions;
    }

    @Override
    public UndoableAction removeConnection(GuiLine<GuiEntity, ErmEntity> entity) {
        CombinedAction actions = new CombinedAction();
        //update Erm Object
        actions.add(relation.removeConnection(entity.getErmline()));
        //UndoableAction for Gui
        UndoableAction temp = new UndoableAction() {
            private GuiLine<GuiEntity, ErmEntity> oldEntity = entity;

            @Override
            public void undo() {
                lines.add(oldEntity);
            }

            @Override
            public void redo() {
                lines.remove(oldEntity);
            }
        };
        actions.add(temp);
        //update Gui Object
        temp.redo();
        return actions;
    }

    @Override
    public UndoableAction removeAllConnections(GuiEntity entity) {
        CombinedAction actions = new CombinedAction();
        //update Erm Object
        actions.add(relation.removeAllConnections(entity.getEntity()));
        //find all correct GuiLine
        ArrayList<GuiLine<GuiEntity, ErmEntity>> oldLines = new ArrayList<>();
        for (GuiLine<GuiEntity, ErmEntity> l : lines) {
            if (l.getDestination().equals(entity)) {
                oldLines.add(l);
            }
        }
        //UndoableAction for Gui
        UndoableAction temp = new UndoableAction() {
            @Override
            public void undo() {
                lines.addAll(oldLines);
            }

            @Override
            public void redo() {
                lines.removeAll(oldLines);
            }
        };
        actions.add(temp);
        //update Gui Object
        temp.redo();
        return actions;
    }

    @Override
    public Map<GuiLine<GuiEntity, ErmEntity>, ErmCardinality> getConnections(Map<GuiLine<GuiEntity, ErmEntity>, ErmCardinality> map) {
        map.clear();
        for (GuiLine<GuiEntity, ErmEntity> l : lines) {
            map.put(l, relation.getCardinality(l.getErmline()));
        }
        return map;
    }

    @Override
    public boolean hasConnection(GuiEntity entity) {
        for (GuiLine<GuiEntity, ErmEntity> l : lines) {
            if (l.getDestination().equals(entity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasConnection(Set<GuiObject> selection) {
        for (GuiObject obj : selection) {
            if (obj instanceof GuiEntity && !hasConnection((GuiEntity) obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the XML equivalent of this GuiEntity
     *
     * @param doc the XMLDocument
     * @param element the base XMLElement from which the relation XMLElement will be created
     * @return the corresponding XMLElement
     */
    public Element getXML(Document doc, Element element) {
        Element relation = super.getXML(doc, element);

        for (GuiLine<GuiEntity, ErmEntity> guiLine : lines) {
            Element entity = doc.createElement("entity");
            entity.setAttribute("cardinality", String.valueOf(getCardinality(guiLine)));
            Element entityID = doc.createElement("entityID");
            entityID.appendChild(doc.createTextNode(String.valueOf(guiLine.getDestination().getEntity().hashCode())));
            entity.appendChild(entityID);
            Element line = getLineXML(guiLine.getPoints(), doc);
            if (line != null) {
                entity.appendChild(line);
            }
            relation.appendChild(entity);
        }

        for (Element e : getAttributesLinesXML(doc)) {
            relation.appendChild(e);
        }

        return relation;
    }

    @Override
    public ErmObject getErmObject() {
        return relation;
    }
}
