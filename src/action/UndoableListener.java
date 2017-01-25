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

/**
 * This interface describes what observable changes can occur within the UndoableList.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public interface UndoableListener {
    /**
     * Called when undo is called on the UndoableList.
     */
    void onUndo();

    /**
     * Called when redo is called on the UndoableList.
     */
    void onRedo();

    /**
     * Called when any future states are removed due to adding a new action.
     */
    void cutBranch();

    /**
     * Called when the list is cleared.
     */
    void onClear();

    /**
     * Called when the SavedIndex is set.
     */
    void onSave();
}
