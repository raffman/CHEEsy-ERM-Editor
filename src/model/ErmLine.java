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

/**
 * This class describes a unique connection between ERM objects. This is especially necessary for relations and cardinalities.
 * e.g. entity - attribute, or relation - entity
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class ErmLine<D extends ErmObject> {
    private D destination;

    /**
     * Constructs a new line to an ERM object. Lines should always go from relations/generalizations to entities
     * or from entities/relations to attributes. Lines are saved in the origin anyway...
     *
     * @param destination the destination of the line
     */
    public ErmLine(D destination) {
        this.destination = destination;
    }

    /**
     * The destination of the line.
     *
     * @return the destination
     */
    public D getDestination() {
        return destination;
    }
}
