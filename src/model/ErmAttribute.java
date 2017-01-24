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
 * This class describes ERM attributes.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class ErmAttribute extends ErmObject {
    private boolean primaryKey = false;
    //reference to its Relation/Entity
    private ErmAttributed owner;

    /**
     * Constructs a new ErmAttribute with a given name and owner.
     *
     * @param name  the name of the attribute
     * @param owner the owner
     */
    public ErmAttribute(String name, ErmAttributed owner) {
        super(name);
        this.owner = owner;
    }

    /**
     * Sets whether this attribute is (part of) the primary key.
     *
     * @param primary primary key property
     * @return an undoable/redoable action
     */
    public UndoableAction setPrimary(boolean primary) {
        UndoableAction temp = new UndoableAction() {
            boolean prev = primaryKey;

            @Override
            public void undo() {
                primaryKey = prev;
            }

            @Override
            public void redo() {
                primaryKey = primary;
            }
        };
        temp.redo();
        return temp;
    }

    /**
     * Returns the reference to the owner of this attribute (relation or entity).
     *
     * @return either relation or entity
     */
    public ErmAttributed getOwner() {
        return owner;
    }

    /**
     * Returns whether this attribute is (part of) the primary key.
     *
     * @return primary key property
     */
    public boolean getPrimary() {
        return primaryKey;
    }
}