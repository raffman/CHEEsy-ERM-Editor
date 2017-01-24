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

package gui;

import gui.model.GuiAttribute;
import gui.model.GuiLine;
import gui.model.GuiObject;
import model.ErmAttribute;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * This class describes a list item in the list of attributes on the properties panel.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
class AttributeListItem extends GeneralizationListItem<GuiAttribute, ErmAttribute> {
    /**
     * the primary key checkbox
     */
    private JCheckBox chkPrimary = new JCheckBox();

    /**
     * Ctor with given owner and the line connecting the attribute to the owner.
     *
     * @param owner owner of attribute
     * @param line  the line connecting the attribute to the owner
     */
    AttributeListItem(GuiObject owner, GuiLine<GuiAttribute, ErmAttribute> line) {
        super(owner, line, new Dimension(144, 25));
        chkPrimary.setSelected(line.getDestination().getPrimary());
        add(chkPrimary);
        chkPrimary.setToolTipText("(Part of) Primary Key");
    }

    /**
     * Adds a listener to the primary checkbox.
     *
     * @param l the listener
     */
    void addPrimaryListener(ActionListener l) {
        chkPrimary.addActionListener(l);
    }

    /**
     * Returns the current state of the primary checkbox.
     *
     * @return true if ticked as primary key.
     */
    boolean getPrimary() {
        return chkPrimary.isSelected();
    }
}

