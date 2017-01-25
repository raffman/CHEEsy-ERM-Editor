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

import action.UndoableAction;
import action.UndoableList;
import action.UndoableListener;
import gui.model.*;
import model.ErmAttribute;
import model.ErmCardinality;
import model.ErmEntity;
import plugin.ErmChen;

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.ResolutionSyntax;
import javax.print.attribute.standard.PrinterResolution;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is a (terribly long) class for the CHEEsy main frame.
 *
 * @author Raffael Lorup
 * @author Ary Obenholzner
 * @author Robert Pinnisch
 * @author William Wang
 */
public class MainWindow extends JFrame implements UndoableListener, WindowListener {

    //root panels
    private JPanel pnlRoot;
    private JSplitPane pnlRootSplit;
    //main
    private JPanel pnlMain;
    private JScrollPane pnlDiagramScroll;
    private DrawingPanel pnlDiagram;
    //quick menu
    private JPanel pnlQuickmenu;
    private JButton btnSave;
    private JButton btnUndo;
    private JButton btnRedo;
    private JButton btnZoomin;
    private JButton btnZoomout;
    private JButton btnZoomdef;
    private JButton btnFontZoomin;
    private JButton btnFontZoomout;
    //ERM text mode
    private JPanel pnlText;
    private JPanel pnlEntities;
    private JPanel pnlRelations;
    private JTextArea txtEntities;
    private JTextArea txtRelations;
    //toolbox
    private JPanel pnlToolbox;
    //tools
    private JPanel pnlTools;
    private JButton btnNewEntity;
    private JButton btnNewRelation;
    private JButton btnNewGeneralization;
    //properties
    private JPanel pnlProperties;
    //properties - model
    private JPanel pnlPropertiesModel;
    //properties - entity
    private JPanel pnlPropertiesEntity;
    private JTextField txtNameEntity;
    private JButton btnAddAttributeEntity;
    private JButton btnDeleteEntity;
    private JPanel pnlEntityAttributes;
    //properties - attribute
    private JPanel pnlPropertiesAttribute;
    private JTextField txtNameAttribute;
    private JCheckBox chkPrimary;
    private JButton btnDeleteAttribute;
    //properties - generalization
    private JPanel pnlPropertiesRelation;
    private JPanel pnlPropertiesGeneralization;
    private JTextField txtNameGeneralization;
    private JTextField txtSupertype;
    private JButton btnSelectSupertype;
    private JButton btnAddSubtype;
    private JPanel pnlGeneralizationEntities;
    private JButton btnDeleteGeneralization;
    //properties - relation
    private JTextField txtNameRelation;
    private JButton btnAddAttributeRelation;
    private JButton btnAddEntity;
    private JPanel pnlRelationEntities;
    private JPanel pnlRelationAttributes;
    private JButton btnDeleteRelation;
    private JLabel lbStatus;
    private JPanel pnlRelEntitylist;
    private JPanel pnlRelAttributlist;
    private JMenuItem menNew, menOpen, menSave, menSaveAs, menPrint, menQuit;
    private JMenuItem menUndo, menRedo;
    private JMenuItem menFitView, menZoomIn, menZoomOut;
    private JMenuItem menAbout, menManual;
    private JCheckBoxMenuItem menGridSnap, menGrabSnap, menGridShow, menShowText;
    //icons
    static final ImageIcon iconDelete = loadIcon("/icons/delete.png");
    private static final ImageIcon iconApp = loadIcon("/icons/appicon.png");
    private int fontSize = 14;

    private CardLayout propertyCard;

    //Model
    private GuiModel model;
    private UndoableList undolist = new UndoableList();
    private float zoomstep = 0.75f;
    private JFileChooser fileChooser = new JFileChooser();
    private File curFile = null;
    private static final String WINDOWTITLE = "CHEEsy - Chen Erm Editor (super yummy)";

    //listeners
    private ActionListener newListener, saveListener, saveAsListener, openListener, printListener, quitListener;
    private ActionListener undoListener, redoListener;
    private ActionListener fitViewListener, zoomInListener, zoomOutListener, gridSnapListener, showTextListener, gridShowListener;
    private ActionListener fontSizeIncreaseListener, fontSizeDecreaseListener;
    private boolean fileSaved = false;
    private boolean ctrlDown = false, shiftDown = false;

    //object
    private TextNameListener nameEntityListener, nameRelationListener, nameAttributeListener, nameSupertypeListener;

    /**
     * Attempts to loads an icon.
     *
     * @param path
     * @return
     */
    private static ImageIcon loadIcon(String path) {
        //try path within the JAR file.
        try {
            return new ImageIcon(ImageIO.read(MainWindow.class.getResource(path)));
        } catch (Exception ignored) {
            System.out.println("icon " + path + " not found => attempting dev path");
        }
        //try path we use for developing.
        try {
            return new ImageIcon(ImageIO.read(MainWindow.class.getResource(".." + path)));
        } catch (Exception ignored) {
            System.out.println("icon .." + path + " not found => shit");
        }
        //return empty ImageIcon (still pretty useless due to NullPointerException because of no image)
        return new ImageIcon();
    }

    /*******************************************************************************************************************
     * constructor and ctor methods
     ******************************************************************************************************************/
    /**
     * Ctor for default main frame window
     */
    public MainWindow() {
        super();
        setIconImage(iconApp.getImage());
        setContentPane(pnlRoot);
        createUIComponents();
        setProperties();
        setSizes();
        enableTextMode(false);
        setListeners();
        setupModel(new GuiModel(new ErmChen()));
        setFileSaved(true);
    }

    /**
     * sets up references to a GuiModel
     *
     * @param m the new GuiModel
     */
    private void setupModel(GuiModel m) {
        model = m;
        model.setPanel(pnlDiagram);
    }

    /**
     * Creates the rest of the interface
     */
    private void createUIComponents() {
        pnlDiagram = new DrawingPanel(this, pnlDiagramScroll, undolist);
        pnlDiagramScroll.setViewportView(pnlDiagram);
        pnlDiagramScroll.getViewport().setBackground(Color.WHITE);
//        pnlDiagram.setPreferredSize(new Dimension(2000, 1000));
        //menubar
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        //diagram menu
        JMenu menuDiagram = new JMenu("Diagram");
        menubar.add(menuDiagram);
        menNew = new JMenuItem("New             [Ctrl+N]");
        menuDiagram.add(menNew);
        menOpen = new JMenuItem("Open            [Ctrl+O]");
        menuDiagram.add(menOpen);
        menSave = new JMenuItem("Save            [Ctrl+S]");
        menuDiagram.add(menSave);
        menSaveAs = new JMenuItem("Save as   [Ctrl+Shift+S]");
        menuDiagram.add(menSaveAs);
        menPrint = new JMenuItem("Print           [Ctrl+P]");
        menuDiagram.add(menPrint);
        menQuit = new JMenuItem("Quit               [Ctrl+Q]");
        menuDiagram.add(menQuit);
        //edit menu
        JMenu menuEdit = new JMenu("Edit");
        menubar.add(menuEdit);
        menUndo = new JMenuItem("Undo    [Ctrl+Z]");
        menuEdit.add(menUndo);
        menRedo = new JMenuItem("Redo    [Ctrl+Y]");
        menuEdit.add(menRedo);
        menuEdit.add(new JSeparator());
        menGridSnap = new JCheckBoxMenuItem("Snap to Grid [Ctrl+Shift+G]", true);
        menGridSnap.setToolTipText("dragged objects or added new ones will snap to the grid");
        menuEdit.add(menGridSnap);
        menGrabSnap = new JCheckBoxMenuItem("Grab Snap", true);
        menGrabSnap.setToolTipText("when dragging the mouse will snap to the center of objects");
//        menGrabSnap.setToolTipText("when dragging the mouse will snap to center, corners or borders of objects");
//        menuEdit.add(menGrabSnap);
        //view menu
        JMenu menuView = new JMenu("View");
        menubar.add(menuView);
        menFitView = new JMenuItem("Fit View              [Ctrl+W]");
        menuView.add(menFitView);
        menZoomIn = new JMenuItem("Zoom in             [Ctrl+'+']");
        menuView.add(menZoomIn);
        menZoomOut = new JMenuItem("Zoom out            [Ctrl+'-']");
        menuView.add(menZoomOut);
        menuView.add(new JSeparator());
        menGridShow = new JCheckBoxMenuItem("Show Grid [Ctrl+G]", true);
        menuView.add(menGridShow);
        menShowText = new JCheckBoxMenuItem("Show/Hide text field  [Ctrl+H]", false);
        menuView.add(menShowText);
        //help menu
        JMenu menuHelp = new JMenu("Help");
        menubar.add(menuHelp);
        menAbout = new JMenuItem("About");
        menuHelp.add(menAbout);
        menManual = new JMenuItem("Manual");
        menuHelp.add(menManual);
        //setting up properties panel
        propertyCard = (CardLayout) pnlProperties.getLayout();
        pnlEntityAttributes.setLayout(new GridLayout(0, 1));
        pnlRelationAttributes.setLayout(new GridLayout(0, 1));
        pnlRelationEntities.setLayout(new GridLayout(0, 1));
        pnlGeneralizationEntities.setLayout(new GridLayout(0, 1));
        pnlDiagramScroll.setAutoscrolls(true);
    }

    /**
     * Sets properties after object construction.
     */
    private void setProperties() {
        setVisible(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        onClear();
        FileFilter ff = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".cheese");
            }

            @Override
            public String getDescription() {
                return "CHEESE - ERM Diagram File";
            }
        };
        fileChooser.setFileFilter(ff);
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "PNG - Portable Network Graphics";
            }
        });
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".jpeg");
            }

            @Override
            public String getDescription() {
                return "JPG, JPEG - Joint Photographic Experts Group";
            }
        });
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".bmp");
            }

            @Override
            public String getDescription() {
                return "BMP - Windows Bitmap";
            }
        });
    }

    /**
     * Set the sizes.
     */
    private void setSizes() {
        setMinimumSize(new Dimension(1080, 675));
        pnlTools.setMinimumSize(new Dimension(200/*btnGeneralization.getWidth()*/, -1));
        //pnlText.setMinimumSize(new Dimension(-1, 60));
        setDefaultSize(fontSize);
        setMenuBarFont(getJMenuBar(), new Font("Arial", Font.PLAIN, fontSize));
    }

    /**
     * Setup the listeners for the UI.
     */
    private void setListeners() {
        //menu listeners
        newListener = e -> {
            if (!fileSaved) {
                switch (JOptionPane.showConfirmDialog(MainWindow.this, "Do you wish to save before starting a new diagram?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    case JOptionPane.YES_OPTION:
                        saveListener.actionPerformed(null);
                        if (!fileSaved) {
                            break;
                        }
                    case JOptionPane.NO_OPTION:
                        curFile = null;
                        setupModel(new GuiModel(model.getPlugin()));
                        undolist.clear();
                }
            } else {
                curFile = null;
                setupModel(new GuiModel(model.getPlugin()));
                undolist.clear();
            }
        };
        saveAsListener = e -> {
            if (Math.random() < 0.1) {
                fileChooser.setDialogTitle("Store the Cheese");
            } else {
                fileChooser.setDialogTitle("Save to File");
            }
            if (fileChooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    String tmp = file.getName().toLowerCase();
                    //if no extension => append correct one
                    if (!tmp.endsWith(".cheese") && !tmp.endsWith(".png") && !tmp.endsWith(".jpg") && !tmp.endsWith(".jpeg") && !tmp.endsWith(".bmp")) {
                        FileFilter filter = fileChooser.getFileFilter();
                        if (filter.getDescription().startsWith("PNG")) {
                            file = new File(file.toString() + ".png");
                        } else if (filter.getDescription().startsWith("JPG")) {
                            file = new File(file.toString() + ".jpg");
                        } else if (filter.getDescription().startsWith("BMP")) {
                            file = new File(file.toString() + ".bmp");
                        } else {
                            file = new File(file.toString() + ".cheese");
                        }
                    }
                    if (file.exists()) {
                        if (JOptionPane.showConfirmDialog(MainWindow.this, file.toString() + " already exists.\nDo you want to overwrite it?", "Overwrite?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                            saveToFile(file);
                        }
                    } else {
                        saveToFile(file);
                    }
                } catch (IOException | TransformerException e1) {
                    JOptionPane.showMessageDialog(MainWindow.this, e1.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        saveListener = e -> {
            if (curFile == null) {
                saveAsListener.actionPerformed(null);
            } else {
                try {
                    saveToFile(curFile);
                } catch (IOException | TransformerException e1) {
                    JOptionPane.showMessageDialog(MainWindow.this, e1.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        openListener = e -> {
            if (Math.random() < 0.1) {
                fileChooser.setDialogTitle("Choose your Cheese");
            } else {
                fileChooser.setDialogTitle("Load from File");
            }
            boolean proceed = false;
            if (!fileSaved) {
                switch (JOptionPane.showConfirmDialog(MainWindow.this, "Do you wish to save before opening another diagram?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    case JOptionPane.YES_OPTION:
                        saveListener.actionPerformed(null);
                        if (!fileSaved) {
                            break;
                        }
                    case JOptionPane.NO_OPTION:
                        proceed = true;
                }
            } else {
                proceed = true;
            }
            if (proceed) {
                if (fileChooser.showOpenDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                    GuiModel m = new GuiModel(model.getPlugin());
                    try {
                        m.readXML(fileChooser.getSelectedFile());
                        curFile = fileChooser.getSelectedFile();
                        setupModel(m);
                        undolist.clear();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        printListener = e -> {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                pageFormat.setOrientation(PageFormat.LANDSCAPE);
                boolean gs = model.getGridShow();
                model.showGrid(false);

                Graphics2D g2d = (Graphics2D) graphics;
                RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHints(rh);

                model.setPhantomPoint(null, -1, false);
                model.setPhantom(DrawingStatus.NONE);

                double xScale = pageFormat.getImageableWidth() / (model.calcViewDimension().getValue().getX() - model.calcViewDimension().getKey().getX());
                double yScale = pageFormat.getImageableHeight() / (model.calcViewDimension().getValue().getY() - model.calcViewDimension().getKey().getY());

                double scale = Math.min(xScale, yScale);

                g2d.scale(scale, scale);
                g2d.translate(Math.max(pageFormat.getImageableX(), 30), Math.max(pageFormat.getImageableY(), 30)); // Space between border and graphics (at least 30px)

                model.drawBoard(g2d, new HashSet<>(), (int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableWidth(), 0, 0);

                model.showGrid(gs);

                return Printable.PAGE_EXISTS;
            });
            boolean ok = printJob.printDialog();
//            printJob.pageDialog(printJob.defaultPage());
            if (ok) {
                try {
                    HashPrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
                    PrinterResolution pr = new PrinterResolution((int) (600), (int) (600), ResolutionSyntax.DPI);
                    set.add(pr);
                    printJob.print();
                } catch (PrinterException ex) {
                    ex.printStackTrace();
                }
            }
        };
        quitListener = e -> windowClosing(null);
        undoListener = e -> undolist.undo();
        redoListener = e -> undolist.redo();
        fitViewListener = e -> pnlDiagram.fitZoom();
        zoomInListener = e -> model.changeZoom(true, zoomstep);
        zoomOutListener = e -> model.changeZoom(false, zoomstep);
        fontSizeIncreaseListener = e -> {
            model.increaseFontSize();

        };
        fontSizeDecreaseListener = e -> {
            model.decreaseFontSize();
        };
        gridSnapListener = e -> {
            pnlDiagram.setGridsnap(menGridSnap.getState());
            pnlDiagram.setGrabsnap(menGridSnap.getState());
        };
        showTextListener = e -> {
            JOptionPane.showMessageDialog(this, "This menu item is only a placeholder,\nsince the feature hasn't been implemented yet!", "Show/Hide Textmode", JOptionPane.INFORMATION_MESSAGE, iconApp);
            menShowText.setState(false);
        };
        gridShowListener = e -> model.showGrid(menGridShow.getState());
        //diagram menu
        menNew.addActionListener(newListener);
        menOpen.addActionListener(openListener);
        menSave.addActionListener(saveListener);
        menPrint.addActionListener(printListener);
        menSaveAs.addActionListener(saveAsListener);
        menQuit.addActionListener(quitListener);
        //edit
        menUndo.addActionListener(undoListener);
        menRedo.addActionListener(redoListener);
        menGridSnap.addActionListener(gridSnapListener);
        menGrabSnap.addActionListener(e -> pnlDiagram.setGrabsnap(menGrabSnap.getState()));
        //view
        menFitView.addActionListener(fitViewListener);
        menZoomIn.addActionListener(zoomInListener);
        menZoomOut.addActionListener(zoomOutListener);
        menShowText.addActionListener(showTextListener);
        menGridShow.addActionListener(gridShowListener);
        //help
        menAbout.addActionListener(e -> JOptionPane.showMessageDialog(this, new MessageWithLink("<h1>CHEEsy - Chen Erm Editor (super yummy)</h1>Version 1.0<h2>A Fachhochschule-Technikum-Wien IT Project</h2><br><p>&#169; Copyright 2017<br>Project Owner:<br>Dipl.-Ing. Dr. Gerd Holweg<br>Project Team:<br>Ary \"(Prison) Designer\" Obenholzner<br>Robert \"Austria's Top Model(ler)\" Pinnisch<br>William \"G.U.I.\" Wang<br>Raffael \"Some guy who annoyed the hell outta everyone else...\" Lorup</p><p>This program is published under the GNU General Public Licence v3.<br>For more information visit <a href=\"https://www.gnu.org/licenses/\">https://www.gnu.org/licenses/</a></p><p>View Source on <a href=\"https://github.com/raffman/CHEEsy-ERM-Editor\">GitHub</a></p><p>Special Thanks to Contributers:<br>Robert Harder, Nathan Blomquist: <a href=\"http://www.iharder.net/current/java/filedrop\">FileDrop</a></p>"), "About", JOptionPane.PLAIN_MESSAGE, iconApp));
        menManual.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create("manual.pdf"));
            } catch (IOException | IllegalArgumentException e1) {
                //e1.printStackTrace();
                try {
                    Desktop.getDesktop().open(new File("manual.pdf"));
                } catch (IOException | IllegalArgumentException e2) {
//                    e1.printStackTrace();
//                    e2.printStackTrace();
                    JOptionPane.showMessageDialog(MainWindow.this, "Could not open file.\nMake sure manual.pdf is located in the same directory as the program.", "No Manual", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        //quickmenu
        btnSave.addActionListener(saveListener);
        btnUndo.addActionListener(undoListener);
        btnRedo.addActionListener(redoListener);
        btnZoomdef.addActionListener(fitViewListener);
        btnZoomin.addActionListener(zoomInListener);
        btnZoomout.addActionListener(zoomOutListener);
        btnFontZoomin.addActionListener(fontSizeIncreaseListener);
        btnFontZoomout.addActionListener(fontSizeDecreaseListener);

        NumberFormat amountFormat = NumberFormat.getNumberInstance();
        amountFormat.setMinimumIntegerDigits(1);
        amountFormat.setMaximumIntegerDigits(3);
        amountFormat.setMaximumFractionDigits(0);
        //tool box listeners
        btnNewEntity.addActionListener(e -> pnlDiagram.setStatus(DrawingStatus.NEW_ENTITY));
        btnNewRelation.addActionListener(e -> pnlDiagram.setStatus(DrawingStatus.NEW_RELATION));
        btnNewGeneralization.addActionListener(e -> pnlDiagram.setStatus(DrawingStatus.NEW_GENERALIZATION));
        //properties panel listeners
        //entity properties
        nameEntityListener = new TextNameListener(txtNameEntity);
        btnAddAttributeEntity.addActionListener(e -> pnlDiagram.setStatus(DrawingStatus.NEW_ATTRIBUTE));
        btnDeleteEntity.addActionListener(e -> pnlDiagram.deleteSelection());
        //relation properties
        nameRelationListener = new TextNameListener(txtNameRelation);
        btnAddAttributeRelation.addActionListener(e -> pnlDiagram.setStatus(DrawingStatus.NEW_ATTRIBUTE));
        btnAddEntity.addActionListener(e -> pnlDiagram.setStatus(DrawingStatus.ADD_ENTITY_TO_RELATION));
        btnDeleteRelation.addActionListener(e -> pnlDiagram.deleteSelection());
        //generalization properties
        nameSupertypeListener = new TextNameListener(txtSupertype);
        btnSelectSupertype.addActionListener(e -> pnlDiagram.setStatus(DrawingStatus.SELECT_SUPERTYPE));
        btnAddSubtype.addActionListener(e -> pnlDiagram.setStatus(DrawingStatus.ADD_SUBTYPE));
        btnDeleteGeneralization.addActionListener(e -> pnlDiagram.deleteSelection());
        //attribute properties
        nameAttributeListener = new TextNameListener(txtNameAttribute);
        chkPrimary.addActionListener(e -> pnlDiagram.setPrimary(chkPrimary.isSelected()));
        btnDeleteAttribute.addActionListener(e -> pnlDiagram.deleteSelection());
        //other listeners
        addWindowListener(this);
        undolist.addListener(this);
        new FileDrop(pnlDiagram, files -> {
            boolean proceed = false;
            if (!fileSaved) {
                switch (JOptionPane.showConfirmDialog(MainWindow.this, "Do you wish to save before opening another diagram?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    case JOptionPane.YES_OPTION:
                        saveListener.actionPerformed(null);
                        if (!fileSaved) {
                            break;
                        }
                    case JOptionPane.NO_OPTION:
                        proceed = true;
                }
            } else {
                proceed = true;
            }
            if (proceed) {

                GuiModel m = new GuiModel(model.getPlugin());
                try {
                    m.readXML(files[0]);
                    curFile = files[0];
                    setupModel(m);
                    undolist.clear();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        });
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            switch (e.getID()) {
                case KeyEvent.KEY_PRESSED:
                    keyPressed(e.getKeyCode());
                    break;
                case KeyEvent.KEY_RELEASED:
                    keyReleased(e.getKeyCode());
            }
            return false;
        });
    }

    /**
     * Returns whether Ctrl is pressed.
     *
     * @return true if ctrl is pressed
     */
    boolean isCtrlDown() {
        return ctrlDown;
    }

    /**
     * Returns whether Shift is pressed.
     *
     * @return true if shift is pressed
     */
    boolean isShiftDown() {
        return shiftDown;
    }

    /**
     * Called by the KeyboardFocusManager to handle key combinations when pressing keys.
     *
     * @param code the key code
     */
    private void keyPressed(int code) {
        switch (code) {
            case KeyEvent.VK_CONTROL:
                ctrlDown = true;
                break;
            case KeyEvent.VK_SHIFT:
                shiftDown = true;
                break;
        }
    }

    /**
     * Called by the KeyboardFocusManager to handle key combinations when releasing keys.
     *
     * @param code the key code
     */
    private void keyReleased(int code) {
        if (code == KeyEvent.VK_CONTROL) {
            ctrlDown = false;
        } else if (code == KeyEvent.VK_SHIFT) {
            shiftDown = false;
        } else if (ctrlDown) {
            switch (code) {
                case KeyEvent.VK_S:
                    if (shiftDown) {
                        if (menSaveAs.isEnabled()) {
                            saveAsListener.actionPerformed(null);
                        }
                    } else {
                        if (menSave.isEnabled()) {
                            saveListener.actionPerformed(null);
                        }
                    }
                    break;
                case KeyEvent.VK_O:
                    if (menOpen.isEnabled()) {
                        openListener.actionPerformed(null);
                    }
                    break;
                case KeyEvent.VK_N:
                    if (menNew.isEnabled()) {
                        newListener.actionPerformed(null);
                    }
                    break;
                case KeyEvent.VK_Z:
                    if (menUndo.isEnabled()) {
                        undoListener.actionPerformed(null);
                    }
                    break;
                case KeyEvent.VK_Y:
                    if (menRedo.isEnabled()) {
                        redoListener.actionPerformed(null);
                    }
                    break;
                case KeyEvent.VK_PLUS:
                    if (menZoomIn.isEnabled()) {
                        zoomInListener.actionPerformed(null);
                    }
                    break;
                case KeyEvent.VK_MINUS:
                    if (menZoomOut.isEnabled()) {
                        zoomOutListener.actionPerformed(null);
                    }
                    break;
                case KeyEvent.VK_G:
                    if ((shiftDown ? menGridSnap : menGridShow).isEnabled()) {
                        if (shiftDown) {
                            menGridSnap.setState(!menGridSnap.getState());
                            gridSnapListener.actionPerformed(null);
                        } else {
                            menGridShow.setState(!menGridShow.getState());
                            gridShowListener.actionPerformed(null);
                        }
                    }
                    break;
                case KeyEvent.VK_H:
                    menShowText.setState(!menShowText.getState());
                    showTextListener.actionPerformed(null);
                    break;
                case KeyEvent.VK_P:
                    if (menPrint.isEnabled()) {
                        printListener.actionPerformed(null);
                    }
                    break;
                case KeyEvent.VK_Q:
                    if (menQuit.isEnabled()) {
                        quitListener.actionPerformed(null);
                    }
                    break;
                case KeyEvent.VK_W:
                    if (menFitView.isEnabled()) {
                        fitViewListener.actionPerformed(null);
                    }
            }
        }
    }

    /**
     * Determines the file format and saves to a file.
     *
     * @param file the save file
     * @throws IOException          thrown if any IOExceptions occur
     * @throws TransformerException thrown if any XML Transformer Exceptions occur
     */
    private void saveToFile(File file) throws IOException, TransformerException {
        String tmp = file.getName().toLowerCase();
        int fileFormat = 0;
        if (tmp.endsWith(".png")) {
            fileFormat = 1;
        } else if (tmp.endsWith(".jpg") || tmp.endsWith(".jpeg")) {
            fileFormat = 2;
        } else if (tmp.endsWith(".bmp")) {
            fileFormat = 3;
        }
        boolean completeDiagram = false;
        if (fileFormat > 0) {
            switch (JOptionPane.showConfirmDialog(this, "Do you wish to export the complete diagram\n(otherwise only the viewport will be exported)?", "Complete Diagram", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                case JOptionPane.CANCEL_OPTION:
                    fileFormat = -1;
                    break;
                case JOptionPane.YES_OPTION:
                    completeDiagram = true;
            }
        }
        switch (fileFormat) {
            case 0:
                model.writeXML(file);
                curFile = file;
                undolist.setSavedIndex();
                setFileSaved(true);
                break;
            case 1:
                ImageIO.write(pnlDiagram.getImage(completeDiagram, 5), "PNG", file);
                break;
            case 2:
                ImageIO.write(pnlDiagram.getImage(completeDiagram, 5), "JPEG", file);
                break;
            case 3:
                ImageIO.write(pnlDiagram.getImage(completeDiagram, 5), "BMP", file);
        }
    }

    /*******************************************************************************************************************
     * settings methods
     ******************************************************************************************************************/
    /**
     * Enables the ERM TextMode. The GUI supports this mode but the functionality is not implemented.
     *
     * @param mode true if should show TextMode
     */
    private void enableTextMode(boolean mode) {
        pnlText.setVisible(mode);
        pnlRootSplit.setEnabled(mode);
        pnlRootSplit.setDividerSize(mode ? 10 : 0);
    }

    /**
     * Sets some font sizes of the GUI. Experimental :)
     *
     * @param size the new font size
     */
    private static void setDefaultSize(int size) {

        Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
        Object[] keys = keySet.toArray(new Object[keySet.size()]);

        for (Object key : keys) {

            if (key != null && key.toString().toLowerCase().contains("font")) {
                Font font = UIManager.getDefaults().getFont(key);
                if (font != null) {
                    font = font.deriveFont((float) size);
                    UIManager.put(key, font);
                }

            }

        }

    }

    /**
     * Sets the font of a given menu bar.
     *
     * @param bar  a menu bar
     * @param font new font
     */
    private static void setMenuBarFont(JMenuBar bar, Font font) {
        for (int i = 0; i < bar.getMenuCount(); i++) {
            setMenuBarFont(bar.getMenu(i), font);
        }

    }

    /**
     * Recursive method for setting the font of menus.
     *
     * @param menu a menu
     * @param font new font
     */
    private static void setMenuBarFont(MenuElement menu, Font font) {
        menu.getComponent().setFont(font);
        for (int k = 0; k < menu.getSubElements().length; k++) {
            setMenuBarFont(menu.getSubElements()[k], font);

        }
    }

    /**
     * Very, very, very ... very important method for managing the properties panel.
     *
     * @param selection  the currently selected GuiObject
     * @param selectText whether the text of the name TextField should be selected
     */
    void showProperties(GuiObject selection, boolean selectText) {
        if (selection == null) {
            propertyCard.show(pnlProperties, "model");
        } else if (selection instanceof GuiEntity) {
            nameEntityListener.setEnabled(false);
            nameEntityListener.setObject(selection);
            propertyCard.show(pnlProperties, "entity");

            txtNameEntity.setText(selection.getName());
            if (selectText) {
                txtNameEntity.requestFocus();
                txtNameEntity.setSelectionStart(0);
                txtNameEntity.setSelectionEnd(txtNameEntity.getText().length());
            }
            showPropertiesAttributes(pnlEntityAttributes, (GuiAttributed) selection);
            nameEntityListener.setEnabled(true);
        } else if (selection instanceof GuiRelation) {
            nameRelationListener.setEnabled(false);
            nameRelationListener.setObject(selection);
            propertyCard.show(pnlProperties, "relation");
            txtNameRelation.setText(selection.getName());
            if (selectText) {
                txtNameRelation.requestFocus();
                txtNameRelation.setSelectionStart(0);
                txtNameRelation.setSelectionEnd(txtNameEntity.getText().length());
            }
            pnlRelationEntities.removeAll();
            GuiRelation rel = (GuiRelation) selection;
            Map<GuiLine<GuiEntity, ErmEntity>, ErmCardinality> map = rel.getConnections(new HashMap<>());
            for (GuiLine<GuiEntity, ErmEntity> ent : map.keySet()) {
                RelationListItem item = new RelationListItem(rel, ent, map.get(ent));
                item.addCardinalityListener(e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        undolist.add(model.setCardinality((GuiRelation) item.getOwner(), item.getLine(), ((RelationListItem.CardinalityItem) e.getItem()).getCardinality()));
                    }
                });
                new TextNameListener(item.getNameField(), ent.getDestination());
                item.addDeleteListener(e -> undolist.add(model.removeConnection((GuiConnection) item.getOwner(), item.getLine())));
                pnlRelationEntities.add(item);
            }
            showPropertiesAttributes(pnlRelationAttributes, rel);
            nameRelationListener.setEnabled(true);
        } else if (selection instanceof GuiGeneralization) {
            propertyCard.show(pnlProperties, "generalization");
            pnlGeneralizationEntities.removeAll();
            GuiGeneralization gen = (GuiGeneralization) selection;
            nameSupertypeListener.setEnabled(false);
            if (gen.hasSuperline()) {
                txtSupertype.setEnabled(true);
                txtSupertype.setText(gen.getSuperline().getDestination().getName());
                nameSupertypeListener.setObject(gen.getSuperline().getDestination());
            } else {
                txtSupertype.setEnabled(false);
                txtSupertype.setText("");
            }
            nameSupertypeListener.setEnabled(true);
            Map<GuiLine<GuiEntity, ErmEntity>, ErmCardinality> map = gen.getConnections(new HashMap<>());
            for (GuiLine<GuiEntity, ErmEntity> ent : map.keySet()) {
                GeneralizationListItem<GuiEntity, ErmEntity> item = new GeneralizationListItem<>(gen, ent);
                item.addDeleteListener(e -> undolist.add(model.removeConnection((GuiConnection) item.getOwner(), item.getLine())));
                new TextNameListener(item.getNameField(), ent.getDestination());
                pnlGeneralizationEntities.add(item);
            }
        } else if (selection instanceof GuiAttribute) {
            nameAttributeListener.setEnabled(false);
            nameAttributeListener.setObject(selection);
            propertyCard.show(pnlProperties, "attribute");
            txtNameAttribute.setText(selection.getName());
            if (selectText) {
                txtNameAttribute.requestFocus();
                txtNameAttribute.setSelectionStart(0);
                txtNameAttribute.setSelectionEnd(txtNameEntity.getText().length());
            }
            chkPrimary.setSelected(((GuiAttribute) selection).getPrimary());
            nameAttributeListener.setEnabled(true);
        }
        pnlProperties.revalidate();
        pnlProperties.repaint();
    }

    /**
     * Method for avoiding code replication in showProperties().
     *
     * @param pnl   the panel to create a list of attributes on
     * @param atted the owner of the attributes
     */
    private void showPropertiesAttributes(JPanel pnl, GuiAttributed atted) {
        pnl.removeAll();
        for (GuiLine<GuiAttribute, ErmAttribute> att : atted.getAttributes()) {
            AttributeListItem item = new AttributeListItem(atted, att);
            item.addDeleteListener(e -> undolist.add(model.remove(att.getDestination())));
            new TextNameListener(item.getNameField(), att.getDestination());
            item.addPrimaryListener(e -> undolist.add(model.setPrimary(att.getDestination(), item.getPrimary())));
            pnl.add(item);
        }
    }

    /**
     * Sets the text of the status message label.
     *
     * @param msg the text to be displayed
     */
    void showStatusMessage(String msg) {
        lbStatus.setText(msg);
    }

    /**
     * Updates GUI elements to show whether the current state of the diagram is the same as the save file's.
     *
     * @param saved true if state of diagram and save file are the same
     */
    private void setFileSaved(boolean saved) {
        fileSaved = saved;
        menSave.setEnabled(!saved);
        btnSave.setEnabled(!saved);
        if (curFile != null) {
            if (saved) {
                setTitle(curFile.getName() + " - " + WINDOWTITLE);
            } else {
                setTitle(curFile.getName() + "*" + " - " + WINDOWTITLE);
            }
        } else {
            if (saved) {
                setTitle("untitled.cheese - " + WINDOWTITLE);
            } else {
                setTitle("untitled.cheese* - " + WINDOWTITLE);
            }
        }
    }

    /*******************************************************************************************************************
     * listener methods
     ******************************************************************************************************************/
    @Override
    public void onUndo() {
        setFileSaved(undolist.isSavedIndex());
        btnRedo.setEnabled(true);
        menRedo.setEnabled(true);
        if (!undolist.canUndo()) {
            btnUndo.setEnabled(false);
            menUndo.setEnabled(false);
        }
    }

    @Override
    public void onRedo() {
        setFileSaved(undolist.isSavedIndex());
        menUndo.setEnabled(true);
        btnUndo.setEnabled(true);
        if (!undolist.canRedo()) {
            btnRedo.setEnabled(false);
            menRedo.setEnabled(false);
        }
    }

    @Override
    public void cutBranch() {
        setFileSaved(undolist.isSavedIndex());
        btnUndo.setEnabled(true);
        menUndo.setEnabled(true);
        btnRedo.setEnabled(false);
        menRedo.setEnabled(false);
    }

    @Override
    public void onClear() {
        setFileSaved(true);
        btnUndo.setEnabled(false);
        menUndo.setEnabled(false);
        btnRedo.setEnabled(false);
        menRedo.setEnabled(false);
    }

    @Override
    public void onSave() {
        setFileSaved(true);
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (!fileSaved) {
            switch (JOptionPane.showConfirmDialog(MainWindow.this, "Do you wish to save before exiting?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                case JOptionPane.YES_OPTION:
                    saveListener.actionPerformed(null);
                    if (!fileSaved) {
                        break;
                    }
                case JOptionPane.NO_OPTION:
                    dispose();
            }
        } else {
            dispose();
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    /**
     * Inner class to summarize several listeners for the name TextFields on the properties panel.
     */
    private class TextNameListener implements DocumentListener, FocusListener, ActionListener {
        /**
         * the text field to observe
         */
        private JTextField txt;
        /**
         * the GuiObject who's name is currently managed by the TextField
         */
        private GuiObject object;
        /**
         * the previous name of the object
         */
        private String prevName;

        /**
         * Ctor with a given TextField
         *
         * @param txt the TextField to observe
         */
        private TextNameListener(JTextField txt) {
            this(txt, null);
        }

        /**
         * Ctor with a given TextField and the GuiObject who's name should be managed.
         *
         * @param txt the TextField to observe
         * @param obj the GuiObject who's name should be managed
         */
        private TextNameListener(JTextField txt, GuiObject obj) {
            this.txt = txt;
            txt.getDocument().addDocumentListener(this);
            txt.addActionListener(this);
            txt.addFocusListener(this);
            object = obj;
            if (obj != null) {
                prevName = obj.getName();
            }
        }

        /**
         * Enables/Disables all listener functionality.
         *
         * @param b the state
         */
        private void setEnabled(boolean b) {
            if (b) {
                txt.getDocument().addDocumentListener(this);
                txt.addActionListener(this);
                txt.addFocusListener(this);
            } else {
                txt.getDocument().removeDocumentListener(this);
                txt.removeActionListener(this);
                txt.removeFocusListener(this);
            }
        }

        /**
         * Sets the GuiObject who's name should be managed
         *
         * @param obj the GuiObject to be managed
         */
        private void setObject(GuiObject obj) {
            object = obj;
            prevName = obj.getName();
        }

        /**
         * The actual renaming happens here
         */
        private void rename() {
            //TODO: check name maybe?
            model.setName(object, txt.getText());
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            rename();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            rename();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            rename();
        }

        @Override
        public void focusGained(FocusEvent e) {
            txt.setSelectionStart(0);
            txt.setSelectionEnd(txt.getText().length());
        }

        @Override
        public void focusLost(FocusEvent e) {
            actionPerformed(null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!prevName.equals(txt.getText())) {
                undolist.add(model.setName(new UndoableAction() {
                    private String prev = prevName;
                    private GuiObject obj = object;
                    private String post = txt.getText();

                    @Override
                    public void undo() {
                        model.setName(obj, prev);
                    }

                    @Override
                    public void redo() {
                        model.setName(obj, post);
                    }
                }));
            }
        }
    }
}
