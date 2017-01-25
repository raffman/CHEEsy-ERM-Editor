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

package plugin;

import javafx.util.Pair;
import model.ErmCardinality;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.List;

/**
 * This is the default plugin for CHEEsy.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class ErmChen implements ErmPlugin {
    //colors
    private final static Color color = new Color(253, 254, 85); // Gelb, wie in Unterlagen
    private final static Color attrColor = new Color(254, 254, 166);  // Gelb, wie in Unterlagen
    private final static Color textColor = Color.BLACK;                         // Schwarz
    private final static Color lineColor = Color.BLACK;                         // Schwarz
    private final static Color borderColor = Color.BLACK;                       // Schwarz
    private final static Color selectedColor = new Color(255, 190, 85);
    //line thickness
    private final static int selectedThickness = 3;
    private final static int notSelectedThickness = 1;
    //entity dimensions
    private final static int entityWidth = 200;
    private final static int entityHeight = 60;
    //attribute dimensions
    private final static int attrWidth = 200;
    private final static int attrHeight = 30;
    //generalization side length
    private final static int genLength = 40;
    //offset of cardinality numbers
    private final static int cardOffset = 20;
    //the default Font to be used
    private Font font = new Font("Arial", Font.PLAIN, 12);

    @Override
    public void setFont(Font f) {
        font = f;
    }

    private void setSelected(Graphics2D g, boolean selected, Color normalColor) {
        if (selected) {
            g.setStroke(new BasicStroke(selectedThickness));
            g.setColor(selectedColor);
        } else {
            g.setStroke(new BasicStroke(notSelectedThickness));
            g.setColor(normalColor);
        }
    }

    private void resetTickness(Graphics2D g) {
        g.setStroke(new BasicStroke(notSelectedThickness));
    }

    private int getStringPosX(int posX, int width, int stringWidth, float scale) {
        return posX + (int) (((width * scale) - stringWidth) / 2);
    }

    private int getStringPosY(int posY, int height, int stringHeight, float scale) {
        return posY + stringHeight / 2 + (int) (height * scale / 2);
    }

    @Override
    public Pair<Point, Point> areaEntity(Point p, float scale) {
        return new Pair<>(new Point(p.x - (int) (entityWidth * scale / 2), p.y - (int) (entityHeight * scale / 2)), new Point(p.x + (int) (entityWidth * scale / 2), p.y + (int) (entityHeight * scale / 2)));
    }

    @Override
    public void drawEntity(Graphics2D g, Point p, float scale, String name, boolean selected) {
        g.setFont(font.deriveFont(font.getSize() * scale));

        setSelected(g, selected, color);

        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int nameWidth = metrics.stringWidth(name);
        int nameHeight = metrics.getAscent();

        g.fillRect(p.x - (int) (entityWidth * scale / 2), p.y - (int) (entityHeight * scale / 2), (int) (entityWidth * scale), (int) (entityHeight * scale));

        g.setColor(borderColor);
        g.drawRect(p.x - (int) (entityWidth * scale / 2), p.y - (int) (entityHeight * scale / 2), (int) (entityWidth * scale), (int) (entityHeight * scale));          // Draw border
        if (name.length() > 0) {
            g.setColor(textColor);
            g.drawString(name, getStringPosX(p.x - (int) (entityWidth * scale / 2), entityWidth, nameWidth, scale), getStringPosY(p.y - (int) (entityHeight * scale / 2), entityHeight, nameHeight, scale));
        }

        resetTickness(g);
    }

    @Override
    public Pair<Point, Point> areaAttribute(Point p, float scale) {
        return new Pair<>(new Point(p.x - (int) (attrWidth * scale / 2), p.y - (int) (attrHeight * scale / 2)), new Point(p.x + (int) (attrWidth * scale / 2), p.y + (int) (attrHeight * scale / 2)));
    }

    @Override
    public void drawAttribute(Graphics2D g, Point p, float scale, String name, boolean primary, boolean selected) {

        setSelected(g, selected, attrColor);

        g.fillOval(p.x - (int) (attrWidth * scale / 2), p.y - (int) (attrHeight * scale / 2), (int) (attrWidth * scale), (int) (attrHeight * scale));

        g.setColor(borderColor);
        g.drawOval(p.x - (int) (attrWidth * scale / 2), p.y - (int) (attrHeight * scale / 2), (int) (attrWidth * scale), (int) (attrHeight * scale));              // Draw border

        if (name.length() > 0) {
            AttributedString attrName = new AttributedString(name);
            g.setFont(font.deriveFont(font.getSize() * scale * 2 / 3));
            attrName.addAttribute(TextAttribute.SIZE, (int) (font.getSize() * scale * 2 / 3));

            FontMetrics metrics = g.getFontMetrics(g.getFont());
            int nameWidth = metrics.stringWidth(name);
            int nameHeight = metrics.getAscent();
            if (primary) {
                attrName.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, name.length());
            }

            if (name.length() > 0) {
                g.setColor(textColor);
                g.drawString(attrName.getIterator(), getStringPosX(p.x - (int) (attrWidth * scale / 2), attrWidth, nameWidth, scale), getStringPosY(p.y - (int) (attrHeight * scale / 2), attrHeight, nameHeight, scale));
            }
        }
        resetTickness(g);
    }

    @Override
    public Pair<Point, Point> areaGeneralization(Point p, float scale) {
        return new Pair<>(new Point(p.x - (int) (genLength * scale / 2), p.y - (int) ((Math.sqrt(3) / 2) * (genLength * scale / 2))), new Point(p.x + (int) (genLength * scale / 2), p.y + (int) ((Math.sqrt(3) / 2) * (genLength * scale / 2))));
    }

    @Override
    public void drawGeneralization(Graphics2D g, Point p, float scale, boolean selected) {

        setSelected(g, selected, color);

        int[] xCor = new int[3];
        int[] yCor = new int[3];

        xCor[0] = p.x;
        yCor[0] = p.y - (int) ((Math.sqrt(3) / 2) * (genLength * scale / 2));
        xCor[1] = p.x + (int) (genLength * scale / 2);
        yCor[1] = p.y + (int) ((Math.sqrt(3) / 2) * (genLength * scale / 2));
        xCor[2] = p.x - (int) (genLength * scale / 2);
        yCor[2] = yCor[1];

        g.fillPolygon(xCor, yCor, xCor.length);

        g.setColor(borderColor);
        g.drawPolygon(xCor, yCor, xCor.length);
        resetTickness(g);
    }

    @Override
    public Pair<Point, Point> areaRelation(Point p, float scale) {
        return new Pair<>(new Point(p.x - (int) (entityWidth * scale / 2), p.y - (int) (entityHeight * scale / 2)), new Point(p.x + (int) (entityWidth * scale / 2), p.y + (int) (entityHeight * scale / 2)));
    }

    @Override
    public void drawRelation(Graphics2D g, Point p, float scale, String name, boolean selected) {
        g.setFont(font.deriveFont(font.getSize() * scale));

        setSelected(g, selected, color);

        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int nameWidth = metrics.stringWidth(name);
        int nameHeight = metrics.getAscent();

        int[] xCor = new int[4];
        int[] yCor = new int[4];

        xCor[0] = p.x;
        xCor[1] = p.x + (int) (entityWidth * scale / 2);
        xCor[2] = xCor[0];
        xCor[3] = p.x - (int) (entityWidth * scale / 2);

        yCor[0] = p.y - (int) (entityHeight * scale / 2);
        yCor[1] = p.y;
        yCor[2] = p.y + (int) (entityHeight * scale / 2);
        yCor[3] = yCor[1];

        g.fillPolygon(xCor, yCor, xCor.length);

        g.setColor(borderColor);
        g.drawPolygon(xCor, yCor, xCor.length);                 // Draw border

        if (name.length() > 0) {
            g.setColor(textColor);
            g.drawString(name, getStringPosX(p.x - (int) (entityWidth * scale / 2), entityWidth, nameWidth, scale), getStringPosY(p.y - (int) (entityHeight * scale / 2), entityHeight, nameHeight, scale));
        }
        resetTickness(g);
    }

    @Override
    public void areaLine(List<Point> pointList) {
        //points are drawn exactly where they are => do not change anything
    }

    @Override
    public void drawLine(Graphics2D g, List<Point> pointList) {
        g.setColor(lineColor);

        int[] xCor = new int[pointList.size()];
        int[] yCor = new int[pointList.size()];

        int i = 0;
        for (Point pt : pointList) {
            xCor[i] = (int) pt.getX();
            yCor[i] = (int) pt.getY();
            i++;
        }

        g.drawPolyline(xCor, yCor, xCor.length);
    }

    @Override
    public void drawLine(Graphics2D g, List<Point> pointList, float scale, ErmCardinality cardinality) {
        String cardinal = null;

        switch (cardinality) {
            case ONE:
                cardinal = "1";
                break;
            case OPTIONAL:
                cardinal = "c";
                break;
            case MULTIPLE:
                cardinal = "m";
                break;
            case MULT_OPT:
                cardinal = "cm";
                break;
        }


        drawLine(g, pointList);
        g.setFont(font.deriveFont(font.getSize() * scale));
        g.setColor(textColor);

        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int nameHeight = metrics.getAscent();
        int nameWidth = metrics.stringWidth(cardinal);

        double xBegin = pointList.get(0).getX();
        double yBegin = pointList.get(0).getY();
        double xEnd = pointList.get(1).getX();
        double yEnd = pointList.get(1).getY();

        double xMid = xBegin + (xEnd - xBegin) / 2;
        double yMid = yBegin + (yEnd - yBegin) / 2;

        double length = Math.sqrt(((xEnd - xBegin) * (xEnd - xBegin)) + ((yEnd - yBegin) * (yEnd - yBegin)));


        double xCor = xMid - ((yEnd - yBegin) / length) * cardOffset * scale - (nameWidth / 2);
        double yCor = yMid + ((xEnd - xBegin) / length) * cardOffset * scale + (nameHeight / 2);

        g.drawString(cardinal, (int) xCor, (int) yCor);
    }
}