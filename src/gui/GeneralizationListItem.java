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

import gui.model.GuiLine;
import gui.model.GuiObject;
import model.ErmObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * This class describes a list item in the list of entities
 * connected to a relation on the properties panel.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
class GeneralizationListItem<O extends GuiObject, D extends ErmObject> extends JPanel {
    /**
     * the button to remove the connection to this entity
     */
    private JButton btnDelete = new JButton(new ImageIcon(MainWindow.iconDelete.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
    /**
     * the name TextField
     */
    private JTextField txtName;
    /**
     * the owner of the managed object
     */
    private GuiObject owner;
    /**
     * the line connecting the managed object to the owner
     */
    private GuiLine<O, D> line;

    /**
     * Ctor with given owner, line to managed object and dimension of this list item.
     *
     * @param owner owner of managed object
     * @param line  line to managed object
     * @param dim   dimension of this list item
     */
    GeneralizationListItem(GuiObject owner, GuiLine<O, D> line, Dimension dim) {
        super(new GridBagLayout());
        this.setMinimumSize(new Dimension(100, 25));
        this.setMaximumSize(new Dimension(160, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0;
        gbc.weighty = 0;
        //this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.owner = owner;
        this.line = line;
        add(btnDelete, gbc);
        btnDelete.setPreferredSize(new Dimension(25, 25));
        txtName = new JTextField();
        add(txtName, gbc);
        txtName.setText(line.getDestination().getName());
        txtName.setToolTipText(line.getDestination().getName());
        txtName.setPreferredSize(dim);
        txtName.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Ctor with given owner and line and default dimension.
     *
     * @param owner the owner of the managed object
     * @param line  the line to the managed object
     */
    GeneralizationListItem(GuiObject owner, GuiLine<O, D> line) {
        this(owner, line, new Dimension(160, 25));
    }

    /**
     * Returns the owner of the managed object.
     *
     * @return the owner
     */
    GuiObject getOwner() {
        return owner;
    }

    /**
     * Returns the line to the managed object.
     *
     * @return the line
     */
    GuiLine<O, D> getLine() {
        return line;
    }

    /**
     * Adds a listener to the delete button.
     *
     * @param l the listener
     */
    void addDeleteListener(ActionListener l) {
        btnDelete.addActionListener(l);
    }

    /**
     * Returns the TextField for the name of the managed object.
     *
     * @return the name TextField
     */
    JTextField getNameField() {
        return txtName;
    }
}


