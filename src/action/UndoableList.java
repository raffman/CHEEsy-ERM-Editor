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

package action;

import java.util.LinkedList;
import java.util.List;

/**
 * Manages a list of undoable/redoable actions.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class UndoableList {
    /**
     * Current index within the list.
     */
    private int undoIndex = 0;

    /**
     * The internal list of actions.
     */
    private LinkedList<UndoableAction> list = new LinkedList<>();

    /**
     * A list of object listening to changes wihtin this UndoableList.
     */
    private List<UndoableListener> listeners = new LinkedList<>();

    /**
     * The index of the action where the diagram was saved.
     */
    private int savedindex = 0;

    /**
     * Adds a new UndoableAction to the end of the list.
     *
     * @param action the action to be added
     */
    public void add(UndoableAction action) {
        if (action == null) {
            return;
        }
        while (undoIndex < list.size()) {
            list.removeLast();
        }
        list.add(action);
        undoIndex++;
        //notify listeners
        for (UndoableListener l : listeners) {
            l.cutBranch();
        }
    }

    /**
     * Clears the list.
     */
    public void clear() {
        list.clear();
        undoIndex = 0;
        savedindex = 0;
        //notify listeners
        for (UndoableListener l : listeners) {
            l.onClear();
        }
    }

    /**
     * Returns whether there are any prior states to revert to.
     *
     * @return true if can undo
     */
    public boolean canUndo() {
        return undoIndex > 0;
    }

    /**
     * Reverts to the next prior state.
     */
    public void undo() {
        if (canUndo()) {
            undoIndex--;
            list.get(undoIndex).undo();
            for (UndoableListener l : listeners) {
                l.onUndo();
            }
        }
    }

    /**
     * Returns whether there are any future states to revisit.
     *
     * @return true if can redo
     */
    public boolean canRedo() {
        return undoIndex < list.size();
    }

    /**
     * Revisits the next future state.
     */
    public void redo() {
        if (canRedo()) {
            list.get(undoIndex).redo();
            undoIndex++;
            for (UndoableListener l : listeners) {
                l.onRedo();
            }
        }
    }

    /**
     * Adds a listener to the list.
     *
     * @param l the listener to be added
     */
    public void addListener(UndoableListener l) {
        listeners.add(l);
    }

    /**
     * Returns whether the current action is where the saved file is at.
     *
     * @return true if saved at current action
     */
    public boolean isSavedIndex() {
        return savedindex == undoIndex;
    }

    /**
     * Sets the current action as the state of the file.
     */
    public void setSavedIndex() {
        savedindex = undoIndex;
        for (UndoableListener l : listeners) {
            l.onSave();
        }
    }
}
