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

import java.util.HashSet;
import java.util.Set;

import action.UndoableAction;

/**
 * This class describes ERM generalizations. It has references to subentities and to a superentity.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class ErmGeneralization extends ErmObject implements ErmConnection {
    private Set<ErmLine<ErmEntity>> subentities = new HashSet<>();
    private ErmLine<ErmEntity> superentity;

    /**
     * Constructs a generalization and uses the Set of given subentities.
     * After construction the given Set should not be changed outside of ErmGeneralization!
     *
     * @param subentities a Set subentities
     */
    public ErmGeneralization(Set<ErmLine<ErmEntity>> subentities) {
        super("");
        this.subentities = subentities;
    }

    /**
     * Sets the superentity to a given entity. Checks if the entity is a subentity and removes it as such.
     * If the given entity is a subentity, setSuperentity() in GuiGeneralization needs to pass on the correct line.
     *
     * @param entity a unique line to the entity or null
     * @return an undoable/redoable action
     */
    public UndoableAction setSuperentity(ErmLine<ErmEntity> entity) {
        UndoableAction temp = new UndoableAction() {
            private ErmLine<ErmEntity> prev = superentity;
            private boolean wasSub = subentities.contains(entity);

            @Override
            public void undo() {
                if (wasSub) {
                    subentities.add(entity);
                }
                superentity = prev;
            }

            @Override
            public void redo() {
                if (wasSub) {
                    subentities.remove(entity);
                }
                superentity = entity;
            }
        };
        temp.redo();
        return temp;
    }

    /**
     * Returns the current superentity or null if none was specified.
     *
     * @return the superentity or null
     */
    public ErmEntity getSuperentity() {
        if (superentity == null) {
            return null;
        }
        return superentity.getDestination();
    }

    @Override
    public UndoableAction addConnection(ErmLine<ErmEntity> entity, ErmCardinality cardinality) {
        UndoableAction temp = new UndoableAction() {
            private boolean wasSup = entity.equals(superentity);

            @Override
            public void undo() {
                if (wasSup) {
                    superentity = entity;
                }
                subentities.remove(entity);
            }

            @Override
            public void redo() {
                if (wasSup) {
                    superentity = null;
                }
                subentities.add(entity);
            }
        };
        temp.redo();
        return temp;
    }

    @Override
    public UndoableAction removeConnection(ErmLine<ErmEntity> entity) {
        UndoableAction temp = new UndoableAction() {
            private boolean wasSup = entity.equals(superentity);

            @Override
            public void undo() {
                if (wasSup) {
                    superentity = entity;
                } else {
                    subentities.add(entity);
                }
            }

            @Override
            public void redo() {
                if (wasSup) {
                    superentity = null;
                } else {
                    subentities.remove(entity);
                }
            }
        };
        temp.redo();
        return temp;
    }


    @Override
    public UndoableAction removeAllConnections(ErmEntity entity) {
        if (superentity != null && entity.equals(superentity.getDestination())) {
            return removeConnection(superentity);
        }
        for (ErmLine<ErmEntity> k : subentities) {
            if (k.getDestination().equals(entity)) {
                return removeConnection(k);
            }
        }
        throw new IllegalArgumentException("ErmEntity is not connected to ErmGeneralization");
    }

    /**
     * Returns the Set of connected subentities. Do not change anything in this Set outside of ErmGeneralization!
     *
     * @return the set of connected subentities
     */
    public Set<ErmLine<ErmEntity>> getSubentities() {
        return subentities;
    }

}