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

import action.CombinedAction;
import action.UndoableAction;
import action.UndoableList;
import gui.model.*;
import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/**
 * The second large GUI class besides the MainWindow.
 * This class describes the panel on which to draw the diagram.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class DrawingPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener, ComponentListener, MouseWheelListener {
    //references
    private MainWindow main;
    private GuiModel model;
    private JScrollPane owner;
    private JPopupMenu menuEntity = new JPopupMenu(), menuRelation = new JPopupMenu(), menuGeneralization = new JPopupMenu(), menuAttribute = new JPopupMenu(), menuNone = new JPopupMenu();
    private JMenuItem menDeleteAll;
    //rendering stuff
    private boolean loadedgraphics = false;
    //    private Dimension size;
    private RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private BufferedImage bi;
    private Graphics2D big;
    private Point cachepoint = new Point();
    //selection and status
    private DrawingStatus status = DrawingStatus.NONE;
    private Set<GuiObject> selection = new HashSet<>(), oldSelection = new HashSet<>();
    private GuiObject prevSelection, rightClickSelection;
    //editing and moving
    private int xMouse, yMouse, xMouseDown, yMouseDown;
    private boolean mouseDown = false;
    private UndoableList undoList;
    private GuiModel.PointInfo pointInfo;
    //suuming and schkrolling schtuff
    private float ZOOMSPEED = 0.75f;
    private int MOUSEMOVEERROR = 5;
    private int SCROLLSPEED = 50;
    //scrolling stuff
    private float horiOldRatio, vertOldRatio;
    private float horiRatio, vertRatio;
    private boolean dragScrolling;
    private int horiDragScroll, vertDragScroll;
    private int xScrollDown, yScrollDown;
    private JCheckBoxMenuItem menPrimary = new JCheckBoxMenuItem("Primary");
    private Point snappoint = new Point();
    private boolean gridsnap = true, grabsnap = true;

    /*******************************************************************************************************************
     * constructor
     ******************************************************************************************************************/
    DrawingPanel(MainWindow main, JScrollPane owner, UndoableList list) {
        this.main = main;
        this.owner = owner;
        undoList = list;
        setBackground(Color.WHITE);
        setFocusable(true);
        //right click menus
        //--right clicked nothing
        buildMenu(menuNone, "New Entity", e -> setStatus(DrawingStatus.NEW_ENTITY));
        buildMenu(menuNone, "New Relation", e -> setStatus(DrawingStatus.NEW_RELATION));
        buildMenu(menuNone, "New Generalization", e -> setStatus(DrawingStatus.NEW_GENERALIZATION));
        menuNone.add(new JSeparator());
        buildMenu(menuNone, "Selected Entities to new Relation", e -> setStatus(DrawingStatus.NEW_RELATION, false));
        buildMenu(menuNone, "Selected Entities to new Generalization", e -> setStatus(DrawingStatus.NEW_GENERALIZATION, false));
        buildMenu(menuNone, "New Entity to selected Relat./General.", e -> setStatus(DrawingStatus.NEW_ENTITY_TO_CONNECTION, false));
        menuNone.add(new JSeparator());
        menDeleteAll = buildMenu(menuNone, "Delete", e -> deleteSelection());
        //--right clicked entity
        buildMenu(menuEntity, "Rename", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            processSelection(true);
        });
        buildMenu(menuEntity, "New Attribute", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            processSelection(false);
            setStatus(DrawingStatus.NEW_ATTRIBUTE);
        });
        buildMenu(menuEntity, "Add to new Relation", e -> {
            setStatus(DrawingStatus.NEW_RELATION);
            selection.add(rightClickSelection);
            processSelection(false);
        });
        buildMenu(menuEntity, "Add to new Generalization", e -> {
            setStatus(DrawingStatus.NEW_GENERALIZATION);
            selection.add(rightClickSelection);
            processSelection(false);
        });
        buildMenu(menuEntity, "Delete", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            deleteSelection();
        });
        //--right clicked relation
        buildMenu(menuRelation, "Rename", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            processSelection(true);
        });
        buildMenu(menuRelation, "New Attribute", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            processSelection(false);
            setStatus(DrawingStatus.NEW_ATTRIBUTE);
        });
        buildMenu(menuRelation, "Add existing Entity", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            processSelection(false);
            setStatus(DrawingStatus.ADD_ENTITY_TO_RELATION);
        });
        buildMenu(menuRelation, "Add new Entity", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            processSelection(false);
            setStatus(DrawingStatus.NEW_ENTITY_TO_CONNECTION);
        });
        buildMenu(menuRelation, "Delete", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            deleteSelection();
        });
        //--right clicked generalization
        buildMenu(menuGeneralization, "Set Super Entity", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            processSelection(false);
            setStatus(DrawingStatus.SELECT_SUPERTYPE);
        });
        buildMenu(menuGeneralization, "Add existing Entity", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            processSelection(false);
            setStatus(DrawingStatus.ADD_SUBTYPE);
        });
        buildMenu(menuGeneralization, "Add new Entity", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            processSelection(false);
            setStatus(DrawingStatus.NEW_ENTITY_TO_CONNECTION);
        });
        buildMenu(menuGeneralization, "Delete", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            deleteSelection();
        });
        //--right clicked attribute
        buildMenu(menuAttribute, "Rename", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            processSelection(true);
        });
        menPrimary.addActionListener(e -> {
            selection.clear();
            selection.add(rightClickSelection);
            setPrimary(menPrimary.getState());
        });
        menuAttribute.add(menPrimary);
        buildMenu(menuAttribute, "Delete", e -> {
            selection.clear();
            selection.add(rightClickSelection);
            deleteSelection();
        });
        //listener
        owner.getHorizontalScrollBar().addAdjustmentListener(e -> {
            JScrollBar bar = owner.getHorizontalScrollBar();
            horiOldRatio = horiRatio;
            horiRatio = (float) (bar.getValue() - bar.getMinimum()) / (bar.getMaximum() - bar.getMinimum());
        });
        owner.getVerticalScrollBar().addAdjustmentListener(e -> {
            JScrollBar bar = owner.getVerticalScrollBar();
            vertOldRatio = vertRatio;
            vertRatio = (float) (bar.getValue() - bar.getMinimum()) / (bar.getMaximum() - bar.getMinimum());
        });
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addComponentListener(this);
        //increase scrolling speed (default is way too slow)
//        owner.getHorizontalScrollBar().setUnitIncrement((int) (SCROLLSPEED * model.getZoom()));
//        owner.getVerticalScrollBar().setUnitIncrement((int) (SCROLLSPEED * model.getZoom()));
        owner.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "actionWhenKeyUp");
        owner.getActionMap().put("actionWhenKeyUp",
                new AbstractAction("keyUpAction") {
                    public void actionPerformed(ActionEvent e) {
                        final JScrollBar bar = owner.getVerticalScrollBar();
                        int currentValue = bar.getValue();
                        bar.setValue(currentValue - (int) (SCROLLSPEED * model.getZoom()));
                    }
                }
        );
        owner.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "actionWhenKeyDown");
        owner.getActionMap().put("actionWhenKeyDown",
                new AbstractAction("keyDownAction") {
                    public void actionPerformed(ActionEvent e) {
                        final JScrollBar bar = owner.getVerticalScrollBar();
                        int currentValue = bar.getValue();
                        bar.setValue(currentValue + (int) (SCROLLSPEED * model.getZoom()));
                    }
                }
        );
        owner.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "actionWhenKeyLeft");
        owner.getActionMap().put("actionWhenKeyLeft",
                new AbstractAction("keyLeftAction") {
                    public void actionPerformed(ActionEvent e) {
                        final JScrollBar bar = owner.getHorizontalScrollBar();
                        int currentValue = bar.getValue();
                        bar.setValue(currentValue - (int) (SCROLLSPEED * model.getZoom()));
                    }
                }
        );
        owner.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "actionWhenKeyRight");
        owner.getActionMap().put("actionWhenKeyRight",
                new AbstractAction("keyRightAction") {
                    public void actionPerformed(ActionEvent e) {
                        final JScrollBar bar = owner.getHorizontalScrollBar();
                        int currentValue = bar.getValue();
                        bar.setValue(currentValue + (int) (SCROLLSPEED * model.getZoom()));
                    }
                }
        );
    }

    /**
     * Small method to simplify building up the right click menus.
     *
     * @param m        the menu
     * @param menuItem the name to be added as a menu item
     * @param l        listener for the menu item
     * @return the new menu item
     */
    private JMenuItem buildMenu(JPopupMenu m, String menuItem, ActionListener l) {
        JMenuItem item = new JMenuItem(menuItem);
        item.addActionListener(l);
        m.add(item);
        return item;
    }

    /*******************************************************************************************************************
     * editing methods
     ******************************************************************************************************************/
    /**
     * Sets whether dragging, placing or line transformation should snap to the grid.
     *
     * @param b true if should snap to grid
     */
    void setGridsnap(boolean b) {
        gridsnap = b;
    }

    /**
     * Sets whether objects should snap to the mouse when dragging or placing.
     *
     * @param b true if should snap to mouse
     */
    void setGrabsnap(boolean b) {
        grabsnap = b;
    }

    /**
     * Sets the model drawn/managed by this DrawingPanel.
     *
     * @param m the GuiModel
     */
    public void setModel(GuiModel m) {
        model = m;
        selection.clear();
        setStatus(DrawingStatus.NONE);
        processSelection(false);
        revalidate();
        repaint();
    }

    /**
     * Sets the current status of the DrawingPanel. See the states in {@link DrawingStatus}.
     *
     * @param st the new status
     */
    void setStatus(DrawingStatus st) {
        setStatus(st, true);
    }

    /**
     * Sets the current status of the DrawingPanel and clears the selection if requested.
     * NOTE: not every state supports clearing of selection
     *
     * @param st             the new state
     * @param clearSelection whether selection should be cleared
     */
    private void setStatus(DrawingStatus st, boolean clearSelection) {
        status = st;
        switch (st) {
            case NONE:
                main.showStatusMessage("");
                break;
            case NEW_ENTITY:
                main.showStatusMessage("Place the entity.");
                break;
            case NEW_ENTITY_TO_CONNECTION:
                main.showStatusMessage("Place the entity.");
                break;
            case NEW_GENERALIZATION:
                if (clearSelection) {
                    selection.clear();
                }
                main.showStatusMessage("Place the generalization.");
                break;
            case NEW_RELATION:
                if (clearSelection) {
                    selection.clear();
                }
                main.showStatusMessage("Place the relation.");
                break;
            case NEW_ATTRIBUTE:
                prevSelection = selection.iterator().next();
                main.showStatusMessage("Place the attribute.");
                break;
            case ADD_ENTITY_TO_RELATION:
                prevSelection = selection.iterator().next();
                main.showStatusMessage("Select an entity.");
                break;
            case ADD_SUBTYPE:
                prevSelection = selection.iterator().next();
                main.showStatusMessage("Select an entity.");
                break;
            case SELECT_SUPERTYPE:
                prevSelection = selection.iterator().next();
                main.showStatusMessage("Select an entity.");
                break;
            case DRAGGING:
                main.showStatusMessage("Drag and drop to desired location.");
                break;
            case TRANSFORM_LINE:
                main.showStatusMessage("Drag and drop to desired location.");
        }
        model.setPhantom(st);
    }

    /**
     * Sets the status message without changing the status.
     * Useful for error messages.
     *
     * @param msg the new message
     */
    private void setStatus(String msg) {
        main.showStatusMessage(msg);
    }

    /**
     * Deletes the current selection.
     */
    void deleteSelection() {
        undoList.add(model.remove(selection));
        selection.clear();
        processSelection(false);
    }

    /**
     * Sets the currently selected attribute as a (non) primary attribute.
     * NOTE: only call this when an attribute is selected.
     *
     * @param primary (non) primary property
     */
    void setPrimary(boolean primary) {
        undoList.add(model.setPrimary((GuiAttribute) selection.iterator().next(), primary));
    }

    /**
     * Updates the properties panel of the MainWindow.
     *
     * @param selectText whether the text in the Name TextField should be selected.
     */
    private void processSelection(boolean selectText) {
        if (selection.isEmpty() || selection.size() > 1) {
//            prevSelection = null;
            main.showProperties(null, false);
        } else {
//            prevSelection = selection.iterator().next();
            main.showProperties(selection.iterator().next(), selectText);
        }
    }

    /**
     * Fits the zoom of the model to fit the size of the viewport.
     */
    void fitZoom() {
        model.fitZoom(owner.getViewport().getWidth(), owner.getViewport().getHeight());
    }

    /*******************************************************************************************************************
     * paint method
     ******************************************************************************************************************/
    /**
     * temporary dimension object
     */
    private Pair<Point, Point> dim = new Pair<>(new Point(), new Point());

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!loadedgraphics) {
            loadedgraphics = true;
            bi = (BufferedImage) createImage(getWidth(), getHeight());
            big = bi.createGraphics();
            big.setRenderingHints(rh);
        } else if (model != null) {
            /*Pair<Point, Point> newDim = model.calcViewDimension();
            if(newDim.getValue().x - newDim.getKey().x != dim.getValue().x - dim.getKey().x || newDim.getValue().y - newDim.getKey().y != dim.getValue().y - dim.getKey().y) {
                dim.getKey().x = newDim.getKey().x;
                dim.getKey().y = newDim.getKey().y;
                dim.getValue().x = newDim.getValue().x;
                dim.getValue().y = newDim.getValue().y;
                bi = (BufferedImage) createImage(Math.max((int) ((dim.getValue().x - dim.getKey().x) * model.getZoom()), owner.getViewport().getWidth()), Math.max((int) ((dim.getValue().y - dim.getKey().y) * model.getZoom()), owner.getViewport().getHeight()));
                big = bi.createGraphics();
                big.setRenderingHints(rh);
            }*/
            model.drawBoard(big, selection, getWidth(), getHeight(), status == DrawingStatus.DRAGGING ? xMouse - xMouseDown - (grabsnap ? snappoint.x : 0) : 0, status == DrawingStatus.DRAGGING ? yMouse - yMouseDown - (grabsnap ? snappoint.y : 0) : 0);
            if (mouseDown && status != DrawingStatus.DRAGGING && status != DrawingStatus.TRANSFORM_LINE) {
                big.setColor(Color.GRAY);
                big.drawRect(Math.min(xMouse, xMouseDown), Math.min(yMouse, yMouseDown), Math.abs(xMouse - xMouseDown), Math.abs(yMouse - yMouseDown));
            }
            g.drawImage(bi, 0, 0, this);
        }
    }

    /**
     * Returns an image of the diagram.
     *
     * @param completeDiagram whether the complete diagram or only the current viewport should be drawn
     * @param resultionScale  a factor for the resolution
     * @return the image
     */
    BufferedImage getImage(boolean completeDiagram, double resultionScale) {
        boolean gs = model.getGridShow();
        model.showGrid(false);
        BufferedImage img = (BufferedImage) createImage((int) (getWidth() * resultionScale), (int) (getHeight() * resultionScale));
        Graphics2D imgGraphics = img.createGraphics();
        imgGraphics.setRenderingHints(rh);
        imgGraphics.scale(resultionScale, resultionScale);
        model.setPhantomPoint(null, -1, false);
        model.setPhantom(DrawingStatus.NONE);
        setStatus(DrawingStatus.NONE);
        model.drawBoard(imgGraphics, new HashSet<>(), getWidth(), getHeight(), 0, 0);
        if (!completeDiagram) {
            Rectangle rec = getVisibleRect();
            img = img.getSubimage((int) (rec.x * resultionScale), (int) (rec.y * resultionScale), (int) (rec.width * resultionScale), (int) (rec.height * resultionScale));
        }
        model.showGrid(gs);
        return img;
    }

    /**
     * Resizes the buffered image to fit the zoom of the model if necessary.
     *
     * @return whether resizing was necessary
     */
    private boolean resizeDimension() {
        Pair<Point, Point> newDim = model.calcViewDimension();
        if (newDim.getValue().x - newDim.getKey().x != dim.getValue().x - dim.getKey().x || newDim.getValue().y - newDim.getKey().y != dim.getValue().y - dim.getKey().y || model.hasZoomed()) {
            dim.getKey().x = newDim.getKey().x;
            dim.getKey().y = newDim.getKey().y;
            dim.getValue().x = newDim.getValue().x;
            dim.getValue().y = newDim.getValue().y;
            bi = (BufferedImage) createImage(Math.max((int) (model.getZoom() * (dim.getValue().x - dim.getKey().x)), owner.getViewport().getWidth()), Math.max((int) (model.getZoom() * (dim.getValue().y - dim.getKey().y)), owner.getViewport().getHeight()));
            big = bi.createGraphics();
            big.setRenderingHints(rh);
            return true;
        }
        return false;
    }


    /*******************************************************************************************************************
     * listener methods
     ******************************************************************************************************************/
    /**
     * Checks whether the selected object still exists if requested and updates the properties panel.
     *
     * @param checkSelection whether existence of selection should be checked
     */
    public void notifyProperties(boolean checkSelection) {
        if (checkSelection && !model.exists(selection)) {
            selection.clear();
        }
        processSelection(false);
    }

    /**
     * VERY important method to notify the panel to redraw the diagram
     */
    public void notifyDraw() {
        if (model != null && resizeDimension()) {
            setPreferredSize(new Dimension(Math.max((int) (model.getZoom() * (dim.getValue().x - dim.getKey().x)), owner.getViewport().getWidth()), Math.max((int) (model.getZoom() * (dim.getValue().y - dim.getKey().y)), owner.getViewport().getHeight())));
        }
        revalidate();
        repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (model != null) {
            resizeDimension();
        }
        revalidate();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }


    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DELETE:
                if (selection.size() > 0) {
                    deleteSelection();
                }
                break;
            case KeyEvent.VK_ESCAPE:
                if (selection.size() > 0) {
                    selection.clear();
                    processSelection(false);
                    notifyDraw();
                }
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1 && selection.size() == 1) {
            processSelection(true);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            if (status == DrawingStatus.NONE) {
                rightClickSelection = model.getSelected(e.getX(), e.getY(), false, null);
                if (rightClickSelection == null) {
                    menDeleteAll.setVisible(selection.size() > 0);
                    menuNone.show(this, e.getX(), e.getY());
                } else if (rightClickSelection instanceof GuiEntity) {
                    menuEntity.show(this, e.getX(), e.getY());
                } else if (rightClickSelection instanceof GuiRelation) {
                    menuRelation.show(this, e.getX(), e.getY());
                } else if (rightClickSelection instanceof GuiGeneralization) {
                    menuGeneralization.show(this, e.getX(), e.getY());
                } else if (rightClickSelection instanceof GuiAttribute) {
                    menPrimary.setState(((GuiAttribute) rightClickSelection).getPrimary());
                    menuAttribute.show(this, e.getX(), e.getY());
                }
            }
            setStatus(DrawingStatus.NONE);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!mouseDown && e.getButton() == MouseEvent.BUTTON1) {
            xMouseDown = e.getX();
            yMouseDown = e.getY();
            xMouse = e.getX();
            yMouse = e.getY();
            mouseDown = true;
            //select something that's already selected => dragging
            if (status == DrawingStatus.NONE) {
                if (!main.isCtrlDown()) {
                    //first check if pressed to move a point (NOT adding one)
                    pointInfo = model.getSelectedPoint(e.getX(), e.getY());
                    if (pointInfo != null && !pointInfo.newPoint) {
                        model.setPhantomPoint(pointInfo.line, pointInfo.index, false);
                        model.setPhantomPoint(e.getX(), e.getY());
                        setStatus(DrawingStatus.TRANSFORM_LINE);
                    } else {
                        //next check if pressed on something to drag it
                        GuiObject tmp = model.getSelected(e.getX(), e.getY(), false, snappoint);
                        if (selection.contains(tmp)) {
                            setStatus(DrawingStatus.DRAGGING);
                        } else if (tmp != null) {
                            selection.clear();
                            selection.add(tmp);
                            processSelection(false);
                            setStatus(DrawingStatus.DRAGGING);
                        } else if (pointInfo != null) {   //last check if pressed on line to add new point
                            model.setPhantomPoint(pointInfo.line, pointInfo.index, pointInfo.newPoint);
                            model.setPhantomPoint(e.getX(), e.getY());
                            setStatus(DrawingStatus.TRANSFORM_LINE);
                        }
                    }
                } else {
                    oldSelection.clear();
                    oldSelection.addAll(selection);
                }
            }
        } else if (!dragScrolling && e.getButton() == MouseEvent.BUTTON2) {
            xScrollDown = e.getX();
            yScrollDown = e.getY();
            horiDragScroll = getVisibleRect().x;
            vertDragScroll = getVisibleRect().y;
            dragScrolling = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (mouseDown && e.getButton() == MouseEvent.BUTTON1) {
            mouseDragged(e);
            //select by clicking vs select by rectangle
            switch (status) {
                case NONE:
                    processSelection(e.getClickCount() > 1);
                    break;
                case NEW_ENTITY:
                    undoList.add(model.newEntity("Entity", xMouse, yMouse));
                    selection.clear();
                    selection.add(model.getLastCreated());
                    processSelection(true);
                    setStatus(DrawingStatus.NONE);
                    break;
                case NEW_GENERALIZATION:
                    undoList.add(model.newGeneralization(xMouse, yMouse, selection));
                    selection.clear();
                    selection.add(model.getLastCreated());
                    processSelection(true);
                    setStatus(DrawingStatus.NONE);
                    break;
                case NEW_RELATION:
                    undoList.add(model.newRelation("", xMouse, yMouse, selection));
                    selection.clear();
                    selection.add(model.getLastCreated());
                    processSelection(true);
                    setStatus(DrawingStatus.NONE);
                    break;
                case NEW_ATTRIBUTE:
                    undoList.add(model.newAttribute("Attribute", (GuiAttributed) prevSelection, xMouse, yMouse));
                    selection.clear();
                    selection.add(model.getLastCreated());
                    processSelection(true);
                    setStatus(DrawingStatus.NONE);
                    break;
                case ADD_SUBTYPE:
                case ADD_ENTITY_TO_RELATION: {
                    GuiConnection connection = (GuiConnection) prevSelection;
                    if (selection.size() > 0) {
                        undoList.add(model.addConnection(connection, selection));
                        selection.clear();
                        selection.add(prevSelection);
                        processSelection(false);
                        setStatus(DrawingStatus.NONE);
                    } else {
                        setStatus("Please select one or more entities.");
                    }
                    break;
                }
                case NEW_ENTITY_TO_CONNECTION: {
                    undoList.add(model.newEntity("Entity", xMouse, yMouse, selection));
                    selection.clear();
                    selection.add(model.getLastCreated());
                    processSelection(true);
                    setStatus(DrawingStatus.NONE);
                    break;
                }
                case SELECT_SUPERTYPE: {
                    GuiGeneralization gen = (GuiGeneralization) prevSelection;
                    if (selection.size() == 1) {
                        GuiEntity ent = (GuiEntity) selection.iterator().next();
                        if (!gen.isSuperentity(ent)) {
                            undoList.add(model.setSupertype((GuiGeneralization) prevSelection, ent));
                            selection.clear();
                            selection.add(prevSelection);
                            processSelection(false);
                            setStatus(DrawingStatus.NONE);
                        } else {
                            setStatus("This entity is already the super type of this generalization. Select another.");
                        }
                    } else {
                        setStatus("Please select an entity.");
                    }
                    break;

                }
                case DRAGGING:
                    if (Math.abs(xMouse - xMouseDown - (grabsnap ? snappoint.x : 0)) > MOUSEMOVEERROR || Math.abs(yMouse - yMouseDown - (grabsnap ? snappoint.y : 0)) > MOUSEMOVEERROR) {
                        undoList.add(model.shiftPos(selection, xMouse - xMouseDown - (grabsnap ? snappoint.x : 0), yMouse - yMouseDown - (grabsnap ? snappoint.y : 0)));
                    }
                    setStatus(DrawingStatus.NONE);
                    break;
                case TRANSFORM_LINE:
                    if (!pointInfo.newPoint) {    //moving existing point
                        CombinedAction actions = null;
                        cachepoint.setLocation(xMouse, yMouse);
                        //check if moved point, neighbour1 and their next neighbour (farneighbour1) form a line and delete neighbour1 if unneeded
                        if (pointInfo.farneighbour1 != null && GuiModel.isOnLine(pointInfo.neighbour1.x, pointInfo.neighbour1.y, pointInfo.farneighbour1, cachepoint)) {
                            actions = new CombinedAction();
                            actions.add(model.removePoint(pointInfo.line, pointInfo.index - 1));
                            pointInfo.index--;
                        }
                        //check if moved point, neighbour2 and their next neighbour (farneighbour2) form a line and delete neighbour2 if unneeded
                        if (pointInfo.farneighbour2 != null && GuiModel.isOnLine(pointInfo.neighbour2.x, pointInfo.neighbour2.y, cachepoint, pointInfo.farneighbour2)) {
                            if (actions == null) {
                                actions = new CombinedAction();
                            }
                            actions.add(model.removePoint(pointInfo.line, pointInfo.index + 1));
                        }
                        UndoableAction act;
                        //check if point and neighbours form a line
                        if (GuiModel.isSamePoint(xMouse, yMouse, pointInfo.neighbour1.x, pointInfo.neighbour1.y, GuiModel.POINTCLICKDISTANCE)
                                || GuiModel.isSamePoint(xMouse, yMouse, pointInfo.neighbour2.x, pointInfo.neighbour2.y, GuiModel.POINTCLICKDISTANCE)
                                || GuiModel.isOnLine(xMouse, yMouse, pointInfo.neighbour1, pointInfo.neighbour2)) {
                            //point and both neighbours form a line => delete unneeded point
                            act = model.removePoint(pointInfo.line, pointInfo.index);
                        } else {
                            //move point
                            act = model.setPoint(pointInfo.line, xMouse, yMouse, pointInfo.index);
                        }
                        if (actions != null) {
                            actions.add(act);
                            actions.reverse();
                            undoList.add(actions);
                        } else {
                            undoList.add(act);
                        }
                    } else {  //adding new point
                        if (!GuiModel.isSamePoint(xMouse, yMouse, pointInfo.neighbour1.x, pointInfo.neighbour1.y, GuiModel.POINTCLICKDISTANCE)
                                && !GuiModel.isSamePoint(xMouse, yMouse, pointInfo.neighbour2.x, pointInfo.neighbour2.y, GuiModel.POINTCLICKDISTANCE)
                                && !GuiModel.isOnLine(xMouse, yMouse, pointInfo.neighbour1, pointInfo.neighbour2)) {
                            //only add new point if not redundant
                            undoList.add(model.addPoint(pointInfo.line, new Point(xMouse, yMouse), pointInfo.index));
                        }

                    }
                    model.setPhantomPoint(null, -1, false);
                    pointInfo = null;
                    setStatus(DrawingStatus.NONE);
            }
            mouseDown = false;
        } else if (e.getButton() == MouseEvent.BUTTON2) {
            dragScrolling = false;
        }
        snappoint.setLocation(0, 0);
        notifyDraw();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mouseDown) {
            xMouse = e.getX();
            yMouse = e.getY();
            boolean filterEntities = false, isOnLine = false;
            switch (status) {
                case ADD_SUBTYPE:
                case ADD_ENTITY_TO_RELATION:
                case SELECT_SUPERTYPE:
                    filterEntities = true;
                case NONE:
                    //select by clicking vs select by rectangle
                    if (Math.abs(xMouse - xMouseDown) <= MOUSEMOVEERROR && Math.abs(yMouse - yMouseDown) <= MOUSEMOVEERROR) {
                        GuiObject tmp = model.getSelected(xMouseDown + (xMouse - xMouseDown) / 2, yMouseDown + (yMouse - yMouseDown) / 2, filterEntities, null);
                        selection.clear();
                        if (main.isCtrlDown()) {
                            selection.addAll(oldSelection);
                        }
                        if (tmp != null) {
                            selection.add(tmp);
                        }
                    } else {
                        model.getSelected(Math.min(xMouseDown, xMouse), Math.min(yMouseDown, yMouse), Math.max(xMouseDown, xMouse), Math.max(yMouseDown, yMouse), selection, filterEntities);
                        if (main.isCtrlDown()) {
                            selection.addAll(oldSelection);
                        }
                    }
                    break;
                case NEW_ENTITY:
                case NEW_GENERALIZATION:
                case NEW_RELATION:
                case NEW_ATTRIBUTE:
                case NEW_ENTITY_TO_CONNECTION:
                    //dragging a new object onto the DrawingArea
                    if (gridsnap) {
                        xMouse = model.snapToGridX(xMouse);
                        yMouse = model.snapToGridY(yMouse);
                    }
                    model.setPhantomPos(xMouse, yMouse);
                    break;
                case TRANSFORM_LINE:
                    isOnLine = GuiModel.isOnLine(xMouse, yMouse, pointInfo.neighbour1, pointInfo.neighbour2);
                case DRAGGING:
                    if (isOnLine) {
                        xMouse = pointInfo.neighbour2.x;
                        yMouse = pointInfo.neighbour2.y;
                    } else if (gridsnap) {
                        xMouse = model.snapToGridX(xMouse + (grabsnap ? snappoint.x : 0));
                        yMouse = model.snapToGridY(yMouse + (grabsnap ? snappoint.y : 0));
                    }
                    //setting the phantom point position is irrelevant but also without effect for dragging
                    model.setPhantomPoint(xMouse, yMouse);
            }
            revalidate();
            repaint();
        }
        if (dragScrolling) {
            Rectangle rect = new Rectangle(horiDragScroll + (xScrollDown - e.getX()), vertDragScroll + (yScrollDown - e.getY()), owner.getWidth(), owner.getHeight());
            scrollRectToVisible(rect);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        xMouse = e.getX();
        yMouse = e.getY();
        switch (status) {
            case NEW_ENTITY:
            case NEW_GENERALIZATION:
            case NEW_RELATION:
            case NEW_ATTRIBUTE:
            case NEW_ENTITY_TO_CONNECTION:
                if (gridsnap) {
                    xMouse = model.snapToGridX(xMouse);
                    yMouse = model.snapToGridY(yMouse);
                }
                model.setPhantomPos(xMouse, yMouse);
            case ADD_SUBTYPE:
            case ADD_ENTITY_TO_RELATION:
            case SELECT_SUPERTYPE:
                revalidate();
                repaint();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        grabFocus();
        if (main.isCtrlDown()) {
            if (model.changeZoom(e.getPreciseWheelRotation() < 0, ZOOMSPEED)) {
                JScrollBar horiBar = owner.getHorizontalScrollBar();
                JScrollBar vertBar = owner.getVerticalScrollBar();
                horiBar.setValue((int) ((horiBar.getMaximum() - horiBar.getMinimum()) * horiOldRatio) + horiBar.getMinimum());
                vertBar.setValue((int) ((vertBar.getMaximum() - vertBar.getMinimum()) * vertOldRatio) + vertBar.getMinimum());
            }
        } else {
            if (e.isShiftDown()) {
                owner.getHorizontalScrollBar().setValue(owner.getHorizontalScrollBar().getValue() + (int) (Math.signum(e.getPreciseWheelRotation()) * SCROLLSPEED * model.getZoom()));
            } else {
                owner.getVerticalScrollBar().setValue(owner.getVerticalScrollBar().getValue() + (int) (Math.signum(e.getPreciseWheelRotation()) * SCROLLSPEED * model.getZoom()));
            }
        }
    }
}

