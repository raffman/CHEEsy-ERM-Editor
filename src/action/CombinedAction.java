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

import java.util.ArrayList;
import java.util.List;

/**
 * This class combines several UndoableActions into one.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class CombinedAction implements UndoableAction {
    /**
     * the internal list of UndoableActions
     */
    private List<UndoableAction> actions;

    /**
     * Constructs a new empty CombinedAction.
     */
    public CombinedAction() {
        actions = new ArrayList<>();
    }

    /**
     * Constructs a new CombinedAction with a given list of UndoableActions.
     *
     * @param actions list of UndoableActions
     */
    public CombinedAction(List<UndoableAction> actions) {
        this.actions = actions;
    }

    @Override
    public void undo() {
        for (UndoableAction a : actions) {
            a.undo();
        }
    }

    @Override
    public void redo() {
        for (int i = actions.size() - 1; i >= 0; i--) {
            actions.get(i).redo();
        }
    }

    /**
     * Adds an UndoableAction at the end of the list.
     *
     * @param action the action to be added
     */
    public void add(UndoableAction action) {
        actions.add(action);
    }

    /**
     * Reverses the order of the UndoableActions.
     */
    public void reverse() {
        for (int i = 0; i < actions.size() / 2; i++) {
            UndoableAction act = actions.get(i);
            actions.set(i, actions.get(actions.size() - 1 - i));
            actions.set(actions.size() - 1 - i, act);
        }
    }
}