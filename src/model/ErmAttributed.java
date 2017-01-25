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

package model;

import java.util.HashSet;
import java.util.Set;

import action.UndoableAction;

/**
 * An abstract class to summarize attribute management.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public abstract class ErmAttributed extends ErmObject {
    /**
     * set of lines connecting the owned attributes.
     */
    private Set<ErmLine<ErmAttribute>> attributes = new HashSet<>();

    /**
     * Ctor to pass on name.
     *
     * @param name the name
     */
    ErmAttributed(String name) {
        super(name);
    }

    /**
     * Adds an attribute.
     *
     * @param attribute the attribute to be added
     * @return an undoable/redoable action
     */
    public UndoableAction addAttribute(ErmLine<ErmAttribute> attribute) {
        attributes.add(attribute);

        return new UndoableAction() {
            @Override
            public void undo() {
                attributes.remove(attribute);
            }

            @Override
            public void redo() {
                attributes.add(attribute);
            }
        };
    }

    /**
     * Removes an attribute.
     *
     * @param attribute the attribute to be removed
     * @return an undoable/redoable action
     */
    public UndoableAction removeAttribute(ErmLine<ErmAttribute> attribute) {
        attributes.remove(attribute);

        return new UndoableAction() {
            @Override
            public void undo() {
                attributes.add(attribute);
            }

            @Override
            public void redo() {
                attributes.remove(attribute);
            }
        };
    }

    /**
     * Returns the set of attributes.
     *
     * @return set of attributes
     */
    public Set<ErmLine<ErmAttribute>> getAttributes() {
        return attributes;
    }

}