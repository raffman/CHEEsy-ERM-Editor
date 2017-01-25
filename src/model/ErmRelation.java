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

import java.util.*;

import action.UndoableAction;
import javafx.util.Pair;

/**
 * This class describes ERM relations. It manages connections to entities with corresponding cardinalites.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class ErmRelation extends ErmAttributed implements ErmConnection {
    private HashMap<ErmLine<ErmEntity>, ErmCardinality> entities;

    /**
     * Constructs a new relation with a given name and a map of entities and cardinalities.
     * This map of entities and cardinalites should not be changed outside of ErmRelation!
     *
     * @param name name of relation
     * @param ents entities and cardinalities
     */
    public ErmRelation(String name, HashMap<ErmLine<ErmEntity>, ErmCardinality> ents) {
        super(name);
        entities = ents;
    }

    /**
     * Sets the cardinality of a relation to an entity.
     *
     * @param entity         the line to the entity
     * @param newcardinality the new cardinality
     * @return an undoable/redoable action
     */
    public UndoableAction setCardinality(ErmLine<ErmEntity> entity, ErmCardinality newcardinality) {
        UndoableAction temp = new UndoableAction() {
            private ErmCardinality oldcardinality = entities.get(entity);

            @Override
            public void undo() {
                entities.put(entity, oldcardinality);
            }

            @Override
            public void redo() {
                entities.put(entity, newcardinality);
            }
        };
        temp.redo();
        return temp;
    }

    @Override
    public UndoableAction addConnection(ErmLine<ErmEntity> entity, ErmCardinality cardinality) {
        UndoableAction temp = new UndoableAction() {
            @Override
            public void undo() {
                entities.remove(entity);
            }

            @Override
            public void redo() {
                entities.put(entity, cardinality);
            }
        };
        temp.redo();
        return temp;
    }

    /**
     * @see ErmConnection#removeConnection(ErmLine entity)
     */
    public UndoableAction removeConnection(ErmLine<ErmEntity> entity) {
        UndoableAction temp = new UndoableAction() {
            private ErmCardinality oldcardinality = entities.get(entity);

            @Override
            public void undo() {
                entities.put(entity, oldcardinality);
            }

            @Override
            public void redo() {
                entities.remove(entity);
            }
        };
        temp.redo();
        return temp;
    }

    @Override
    public UndoableAction removeAllConnections(ErmEntity entity) {
        final ArrayList<Pair<ErmLine<ErmEntity>, ErmCardinality>> list = new ArrayList<>();
        //gather all lines to this entity and cache them
        Set<ErmLine<ErmEntity>> keys = entities.keySet();
        for (ErmLine<ErmEntity> k : keys) {
            if (k.getDestination().equals(entity)) {
                list.add(new Pair<>(k, entities.get(k)));
            }
        }
        UndoableAction temp = new UndoableAction() {
            @Override
            public void undo() {
                for (Pair<ErmLine<ErmEntity>, ErmCardinality> p : list) {
                    entities.put(p.getKey(), p.getValue());
                }
            }

            @Override
            public void redo() {
                for (Pair<ErmLine<ErmEntity>, ErmCardinality> p : list) {
                    entities.remove(p.getKey());
                }
            }
        };
        temp.redo();
        return temp;
    }

    /**
     * Returns the cardinality of a relation to an entity.
     *
     * @param entity the unique line to an entity
     * @return the corresponding cardinality
     */
    public ErmCardinality getCardinality(ErmLine<ErmEntity> entity) {
        return entities.get(entity);
    }


    /**
     * Returns the Map of entities and cardinalities.
     * This map must not be changed outside of ErmRelation!
     *
     * @return the map of entities and cardinalities
     */
    public Map<ErmLine<ErmEntity>, ErmCardinality> getEntities() {
        return entities;
    }
}