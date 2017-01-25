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

package gui;

import gui.model.GuiEntity;
import gui.model.GuiLine;
import gui.model.GuiObject;
import model.ErmCardinality;
import model.ErmEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;

/**
 * This class describes a list item in the list of entities
 * connected to a generalization on the properties panel.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
class RelationListItem extends GeneralizationListItem<GuiEntity, ErmEntity> {
    /**
     * the ComboBox for cardinalities.
     */
    private JComboBox<CardinalityItem> cmbCardinality = new JComboBox<>();

    /**
     * Ctor with given owner, line to the managed object and the cardinality to displayed in the ComboBox.
     * @param owner the owner of the managed object
     * @param line the line to the managed object
     * @param cardinality the cardinality of the connection
     */
    RelationListItem(GuiObject owner, GuiLine<GuiEntity, ErmEntity> line, ErmCardinality cardinality) {
        super(owner, line, new Dimension(115,25));
        add(cmbCardinality);
        cmbCardinality.addItem(new CardinalityItem("1", ErmCardinality.ONE));
        cmbCardinality.addItem(new CardinalityItem("c", ErmCardinality.OPTIONAL));
        cmbCardinality.addItem(new CardinalityItem("m", ErmCardinality.MULTIPLE));
        cmbCardinality.addItem(new CardinalityItem("cm", ErmCardinality.MULT_OPT));
        switch (cardinality) {
            case ONE:
                cmbCardinality.setSelectedIndex(0);
                break;
            case OPTIONAL:
                cmbCardinality.setSelectedIndex(1);
                break;
            case MULTIPLE:
                cmbCardinality.setSelectedIndex(2);
                break;
            case MULT_OPT:
                cmbCardinality.setSelectedIndex(3);
                break;
        }
    }

    /**
     * Adds a listener to the cardinality ComboBox.
     * @param l the listener
     */
    void addCardinalityListener(ItemListener l){
        cmbCardinality.addItemListener(l);
    }

    /**
     * Internal class to simplify the cardinality ComboBox items.
     */
    class CardinalityItem {
        /**
         * name of the cardinality
         */
        private String name;
        /**
         * the cardinality
         */
        private ErmCardinality card;

        /**
         * Ctor with given name and cardinality
         * @param n name
         * @param c cardinality
         */
        private CardinalityItem(String n, ErmCardinality c){
            name = n;
            card = c;
        }
        @Override
        public String toString(){
            return name;
        }

        /**
         * Returns the cardinality corresponding to the name.
         * @return the cardinality
         */
        ErmCardinality getCardinality(){
            return card;
        }
    }
}

