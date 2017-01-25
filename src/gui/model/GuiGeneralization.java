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
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 * This class describes generalizations to the GUI.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class GuiGeneralization extends GuiObject implements GuiConnection {
    /**
     * the ERM equivalent of this GuiGeneralization
     */
    private ErmGeneralization generalization;
    /**
     * the line to the super entity
     */
    private GuiLine<GuiEntity, ErmEntity> superline;
    /**
     * the set of lines connecting the sub entities
     */
    private Set<GuiLine<GuiEntity, ErmEntity>> sublines = new HashSet<>();

    /**
     * Ctor with given position and a set of subtypes.
     *
     * @param pos      position of the generalization
     * @param sublines the subtypes
     */
    GuiGeneralization(Point pos, Set<GuiEntity> sublines) {
        super(pos);
        Set<ErmLine<ErmEntity>> subErmLines = new HashSet<>();
        for (GuiEntity ent : sublines) {
            ErmLine<ErmEntity> l = new ErmLine<>(ent.getEntity());
            subErmLines.add(l);
            this.sublines.add(new GuiLine<>(l, this, ent));
        }
        generalization = new ErmGeneralization(subErmLines);
    }

    /**
     * Constructs a generalization from an XML element and a (?) string-entity-map
     *
     * @param element   the XML element
     * @param entityMap a map
     */
    GuiGeneralization(Element element, Map<String, GuiEntity> entityMap) {
        super(element);
        Set<ErmLine<ErmEntity>> subErmLines = new HashSet<>();
        NodeList nodes = element.getElementsByTagName("subentity");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element entity = (Element) nodes.item(i);
            GuiEntity guiEntity = entityMap.get(entity.getElementsByTagName("entityID").item(0).getTextContent());
            ErmLine ermLine = new ErmLine<ErmEntity>(guiEntity.getEntity());
            subErmLines.add(ermLine);
            GuiLine guiLine = new GuiLine<GuiEntity, ErmEntity>(ermLine, this, guiEntity);
            loadLine(entity.getElementsByTagName("line").item(0), guiLine.getPoints());
            subErmLines.add(ermLine);
            sublines.add(guiLine);

        }
        try {
            Element superentity = (Element) element.getElementsByTagName("superentity").item(0);
            ErmLine ermLine = new ErmLine<ErmEntity>(entityMap.get(superentity.getElementsByTagName("entityID").item(0).getTextContent()).getEntity());
            superline = new GuiLine<GuiEntity, ErmEntity>(ermLine, this, entityMap.get(superentity.getElementsByTagName("entityID").item(0).getTextContent()));
            loadLine(superentity.getElementsByTagName("line").item(0), superline.getPoints());
        } catch (NullPointerException e) {
            System.out.println("No supertype was set");
        }

        generalization = new ErmGeneralization(subErmLines);
    }

    /**
     * Sets the supertype of the generalization.
     * NOTE: if the entity is a subtype it will be removed as such.
     *
     * @param entity the new super entity
     * @return an undoable/redoable action
     */
    UndoableAction setSupertype(GuiEntity entity) {
        CombinedAction actions = new CombinedAction();
        boolean wasSub = false;
        GuiLine<GuiEntity, ErmEntity> osl = null;
        if (entity != null) {
            for (GuiLine<GuiEntity, ErmEntity> line : sublines) {
                if (line.getDestination().equals(entity)) {
                    wasSub = true;
                    osl = line;
                    break;
                }
            }
        }
        final GuiLine<GuiEntity, ErmEntity> oldsubline = osl;
        //update Erm Object
        UndoableAction temp;
        if (wasSub) {
            actions.add(generalization.setSuperentity(oldsubline.getErmline()));
            temp = new UndoableAction() {
                private GuiLine<GuiEntity, ErmEntity> oldsuperline = superline;
                private GuiLine<GuiEntity, ErmEntity> newsuperline = oldsubline;

                @Override
                public void undo() {
                    superline = oldsuperline;
                    sublines.add(newsuperline);
                }

                @Override
                public void redo() {
                    superline = newsuperline;
                    sublines.remove(newsuperline);
                }
            };
        } else {
            final ErmLine<ErmEntity> newermline = (entity == null) ? null : new ErmLine<>(entity.getEntity());
            actions.add(generalization.setSuperentity(newermline));
            temp = new UndoableAction() {
                private GuiLine<GuiEntity, ErmEntity> oldsuperline = superline;
                private GuiLine<GuiEntity, ErmEntity> newsuperline = (newermline == null) ? null : new GuiLine<>(newermline, GuiGeneralization.this, entity);

                @Override
                public void undo() {
                    superline = oldsuperline;
                }

                @Override
                public void redo() {
                    superline = newsuperline;
                }
            };
        }
        //UndoableAction for Gui
        actions.add(temp);
        //update Gui Object
        temp.redo();
        return actions;
    }

    @Override
    UndoableAction shiftPos(int x, int y, Set<GuiObject> selection) {
        CombinedAction actions = null;
        if (superline != null && selection.contains(superline.getDestination())) {
            actions = new CombinedAction();
            actions.add(superline.shiftPoints(x, y));
        }
        for (GuiLine<GuiEntity, ErmEntity> l : sublines) {
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
        boolean wasSup = superline != null && superline.getDestination().equals(entity);
        UndoableAction temp;
        if (wasSup) {
            //update Erm Object
            actions.add(generalization.addConnection(superline.getErmline(), cardinality));
            //UndoableAction for Gui
            temp = new UndoableAction() {
                private GuiLine<GuiEntity, ErmEntity> newsubline = superline;

                @Override
                public void undo() {
                    sublines.remove(newsubline);
                    superline = newsubline;
                }

                @Override
                public void redo() {
                    sublines.add(newsubline);
                    superline = null;
                }
            };
        } else {
            //update Erm Object
            ErmLine<ErmEntity> newermsubline = new ErmLine<>((entity.getEntity()));
            actions.add(generalization.addConnection(newermsubline, cardinality));
            //UndoableAction for Gui
            temp = new UndoableAction() {
                private GuiLine<GuiEntity, ErmEntity> newsubline = new GuiLine<>(newermsubline, GuiGeneralization.this, entity);

                @Override
                public void undo() {
                    sublines.remove(newsubline);
                }

                @Override
                public void redo() {
                    sublines.add(newsubline);
                }
            };
        }
        actions.add(temp);
        //update Gui Object
        temp.redo();
        return actions;
    }

    @Override
    public UndoableAction removeConnection(GuiLine<GuiEntity, ErmEntity> entity) {
        CombinedAction actions = new CombinedAction();
        //update Erm Object
        actions.add(generalization.removeConnection(entity.getErmline()));
        //UndoableAction for Gui
        UndoableAction temp = new UndoableAction() {
            private GuiLine<GuiEntity, ErmEntity> oldentity = entity;

            @Override
            public void undo() {
                sublines.add(oldentity);
            }

            @Override
            public void redo() {
                sublines.remove(oldentity);
            }
        };
        actions.add(temp);
        //update Gui Object
        temp.redo();
        return actions;
    }

    @Override
    public UndoableAction removeAllConnections(GuiEntity entity) {
        //find correct GuiLine
        for (GuiLine<GuiEntity, ErmEntity> l : sublines) {
            if (l.getDestination().equals(entity)) {
                return removeConnection(l);
            }
        }
        throw new IllegalArgumentException("GuiEntity is not connected to GuiGeneralization");
    }

    @Override
    public Map<GuiLine<GuiEntity, ErmEntity>, ErmCardinality> getConnections(Map<GuiLine<GuiEntity, ErmEntity>, ErmCardinality> map) {
        map.clear();
        for (GuiLine<GuiEntity, ErmEntity> l : sublines) {
            map.put(l, ErmCardinality.ONE);
        }
        return map;
    }

    @Override
    public boolean hasConnection(GuiEntity entity) {
        for (GuiLine l : sublines) {
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
     * Returns an iterator for the sub types (lines).
     *
     * @return an iterator
     */
    Iterable<GuiLine<GuiEntity, ErmEntity>> getConnections() {
        return sublines;
    }

    /**
     * Returns the super type (line).
     *
     * @return the line to the super entity
     */
    public GuiLine<GuiEntity, ErmEntity> getSuperline() {
        return superline;
    }

    /**
     * Checks if a super type has been set.
     *
     * @return whether has a super type
     */
    public boolean hasSuperline() {
        return superline != null;
    }

    @Override
    ErmObject getErmObject() {
        return generalization;
    }

    /**
     * Checks whether an entity is the super entity of this generalization.
     *
     * @param entity a entity
     * @return if given entity is super entity
     */
    public boolean isSuperentity(GuiEntity entity) {
        return superline != null && superline.getDestination().equals(entity);
    }

    /**
     * Returns the XML equivalent of this GuiGeneralization
     *
     * @param doc the XMLDocument
     * @param element the base XMLElement from which the generalization XMLElement will be created
     * @return the corresponding XMLElement
     */
    @Override
    Element getXML(Document doc, Element element) {
        Element generalization = super.getXML(doc, element);
        if (superline != null) {
            Element superentity = doc.createElement("superentity");
            Element superID = doc.createElement("entityID");
            superID.appendChild(doc.createTextNode(String.valueOf(superline.getDestination().getEntity().hashCode())));
            superentity.appendChild(superID);
            Element superLine = getLineXML(superline.getPoints(), doc);
            if (superLine != null) {
                superentity.appendChild(superLine);
            }
            generalization.appendChild(superentity);
        }

        for (GuiLine<GuiEntity, ErmEntity> guiLine : sublines) {
            Element subentity = doc.createElement("subentity");
            Element subID = doc.createElement("entityID");
            subID.appendChild(doc.createTextNode(String.valueOf(guiLine.getDestination().getEntity().hashCode())));
            subentity.appendChild(subID);
            Element subLine = getLineXML(guiLine.getPoints(), doc);
            if (subLine != null) {
                subentity.appendChild(subLine);
            }
            generalization.appendChild(subentity);
        }

        return generalization;
    }
}
