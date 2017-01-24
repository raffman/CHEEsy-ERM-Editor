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

package model;

import action.UndoableAction;

/**
 * This interface describes how relations and generalizations can manage their lines to entities.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public interface ErmConnection {
    /**
     * Adds a given line towards an entity. This method should check for internal inconsistencies and correct accordingly.
     * e.g. adding a line, that is already the line to the superentity, to a generalization should set its superline to null.
     *
     * @param entity      line to an entity (must not be null)
     * @param cardinality cardinality (will be ignored by generalizations)
     * @return an undoable/redoable action
     */
    UndoableAction addConnection(ErmLine<ErmEntity> entity, ErmCardinality cardinality);

    /**
     * Removes a line to an entity. This method should check if the line was the line to the superentity and correct accordingly.
     *
     * @param entity the line to be removed (must not be null)
     * @return an undoable/redoable action
     */
    UndoableAction removeConnection(ErmLine<ErmEntity> entity);

    /**
     * Removes all lines to a given entity (including superline of generalizations).
     *
     * @param entity the entity to be disconnected (must not be null)
     * @return an undoable/redoable action
     */
    UndoableAction removeAllConnections(ErmEntity entity);
}