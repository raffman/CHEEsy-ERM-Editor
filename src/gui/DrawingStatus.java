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

package gui;

/**
 * This enumeration lists the various states the DrawingPanel can be in.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public enum DrawingStatus {
    /**
     * no special state, can select things
     */
    NONE,
    /**
     * adding new entity to the diagram
     */
    NEW_ENTITY,
    /**
     * adding new entity to a relation or generalization
     */
    NEW_ENTITY_TO_CONNECTION,
    /**
     * adding new generalization to the diagram
     */
    NEW_GENERALIZATION,
    /**
     * adding new relation to the diagram
     */
    NEW_RELATION,
    /**
     * adding new attribute to the diagram
     */
    NEW_ATTRIBUTE,
    /**
     * connect existing entity to a relation
     */
    ADD_ENTITY_TO_RELATION,
    /**
     * connect existing entity to a generalization
     */
    ADD_SUBTYPE,
    /**
     * select a supertype for a generalization
     */
    SELECT_SUPERTYPE,
    /**
     * dragging object(s) around
     */
    DRAGGING,
    /**
     * transforming a line
     */
    TRANSFORM_LINE
}
