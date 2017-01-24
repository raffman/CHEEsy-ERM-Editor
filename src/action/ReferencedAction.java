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

package action;

/**
 * This class allows for additional code to be executed after undo/redo of an UndoableAction.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public abstract class ReferencedAction implements UndoableAction {
    /**
     * The actual UndoableAction
     */
    private UndoableAction referenced;

    /**
     * Constructs a new ReferencedAction around a given UndoableAction.
     *
     * @param ref the actual UndoableAction
     */
    protected ReferencedAction(UndoableAction ref) {
        referenced = ref;
    }

    @Override
    public void undo() {
        prior(true);
        referenced.undo();
        after(true);
    }

    @Override
    public void redo() {
        prior(false);
        referenced.redo();
        after(false);
    }

    /**
     * Code that will be executed before undo/redo.
     *
     * @param isUndo true if executed before undo, false if before redo
     */
    public void prior(boolean isUndo) {
    }

    /**
     * Code that will be executed after undo/redo.
     *
     * @param isUndo true if executed before undo, false if before redo
     */
    public abstract void after(boolean isUndo);
}
