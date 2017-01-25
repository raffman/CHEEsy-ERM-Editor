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
import model.ErmCardinality;
import model.ErmEntity;

import java.util.Map;
import java.util.Set;

/**
 * This interface describes how relations and generalizations can manage their lines to entities for the GUI.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public interface GuiConnection {
    /**
     * Adds a given entity towards an entity. This method should check for internal inconsistencies and correct accordingly.
     * e.g. adding a line, that is already the line to the superentity, to a generalization should set its superline to null.
     *
     * @param entity      line to an entity (must not be null)
     * @param cardinality cardinality (will be ignored by generalizations)
     * @return an undoable/redoable action
     */
    UndoableAction addConnection(GuiEntity entity, ErmCardinality cardinality);

    /**
     * Removes a line to an entity. This method should check if the line was the line to the superentity and correct accordingly.
     *
     * @param entity line to an entity (must not be null)
     * @return an undoable/redoable action
     */
    UndoableAction removeConnection(GuiLine<GuiEntity, ErmEntity> entity);

    /**
     * Removes all lines to a given entity (including superline of generalizations).
     *
     * @param entity the entity to be disconnected (must not be null)
     * @return an undoable/redoable action
     */
    UndoableAction removeAllConnections(GuiEntity entity);

    /**
     * Empties and fills the given map with the connected entities and corresponding cardinalities.
     *
     * @param map the caching map
     * @return the filled map
     */
    Map<GuiLine<GuiEntity, ErmEntity>, ErmCardinality> getConnections(Map<GuiLine<GuiEntity, ErmEntity>, ErmCardinality> map);

    /**
     * Checks whether the relation/generalization has a connection to the given entity. The superentity of generalizations should NOT fulfill this condition!
     *
     * @param entity the entity in question
     * @return true if connected
     */
    boolean hasConnection(GuiEntity entity);

    /**
     * Checks whether the relation/generalization has a connection to ALL the ENTITIES in the given Set. The superentity of generalizations should NOT fulfill this condition!
     *
     * @param selection the Set of objects to be checked
     * @return true if all entities are connected
     */
    boolean hasConnection(Set<GuiObject> selection);
}
