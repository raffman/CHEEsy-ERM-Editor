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

/**
 * An abstract class to summarize general functionality of ERM objects.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public abstract class ErmObject {

    private String name;

    /**
     * Constructs a new ERM object with a given name.
     *
     * @param name name of the object
     */
    public ErmObject(String name) {
        this.name = name;
    }

    /**
     * Sets the name of the object. Generalizations will ignore it.
     * Note: UndoableAction for this has to be generated outside.
     *
     * @param nameToSet new name
     */
    public void setName(String nameToSet) {
        name = nameToSet;
    }

    /**
     * Returns the name of the object.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}