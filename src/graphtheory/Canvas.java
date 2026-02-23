package graphtheory;

/**
 *
 * @author mk
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeListener;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Vector;

public class Canvas {

    public JFrame frame;
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private CanvasPane canvas;
    private Graphics2D graphic;
    private Color backgroundColour;
    private Image canvasImage,  canvasImage2;
    private int selectedTool;
    private int selectedWindow;
    private Dimension screenSize;
    public int width,  height;
    private int clickedVertexIndex;
    private int clickedEdgeIndex;
    private FileManager fileManager = new FileManager();
    /* Directed Edges */
    private boolean isDirectedMode = false;
    /* Weighted Edges */
    private boolean isWeightedMode = false;

    // Toolbar tool buttons (kept as fields for active-highlight management)
    private JButton btnAddVertex;
    private JButton btnAddEdge;
    private JButton btnGrab;
    private JButton btnRemove;
    // Toolbar toggle buttons
    private JToggleButton btnDirected;
    private JToggleButton btnWeighted;
    // View switch buttons (only one visible at a time)
    private JButton btnViewGraph;
    private JButton btnViewProps;

    // Colour constants for toolbar button states
    private static final Color TOOL_ACTIVE_BG   = new Color(180, 210, 255);
    private static final Color TOOL_INACTIVE_BG = UIManager.getColor("Button.background") != null
            ? UIManager.getColor("Button.background") : new Color(238, 238, 238);

    private Vector<Vertex> vertexList;
    private Vector<Edge> edgeList;
    private GraphProperties gP = new GraphProperties();

    // ── Style defaults (applied to every newly created vertex / edge) ──────────
    private Color defaultNodeOuter  = Color.BLACK;
    private Color defaultNodeInner  = Color.WHITE;
    private int   defaultNodeSize   = 40;
    private Color defaultEdgeColor  = Color.BLACK;
    private float defaultEdgeStroke = 2.0f;

    // ── Selection tracking ────────────────────────────────────────────────────
    private Vertex selectedVertex = null;
    private Edge   selectedEdge   = null;

    // Dynamic controls in the "Selected" card panel (updated by refreshSelectionPanel)
    private JPanel  selectionCards;
    private JLabel  selectionTitle;
    private JButton selVertexOuterBtn, selVertexInnerBtn;
    private JLabel  selVertexSizeLabel;
    private JSlider selVertexSizeSlider;
    private JButton selEdgeColorBtn;
    private JLabel  selEdgeStrokeLabel;
    private JSlider selEdgeStrokeSlider;

    public Canvas(String title, int width, int height, Color bgColour) {
        frame = new JFrame();
        frame.setTitle(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        // ── Menu bar ──────────────────────────────────────────────────────────
        menuBar = new JMenuBar();
        JMenu menuOptions  = new JMenu("Tools");
        JMenu menuOptions1 = new JMenu("File");
        JMenu menuOptions2 = new JMenu("Extras");
        JMenu menuOptions3 = new JMenu("Window");

        JMenuItem item = new JMenuItem("Add Vertex");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        item.addActionListener(new MenuListener());
        menuOptions.add(item);

        item = new JMenuItem("Toggle Directed Edges");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
        item.addActionListener(e -> {
            isDirectedMode = !isDirectedMode;
            btnDirected.setSelected(isDirectedMode);
            System.out.println("Directed mode: " + isDirectedMode);
        });
        menuOptions.add(item);

        JMenuItem weightedToggle = new JMenuItem("Toggle Weighted Mode");
        weightedToggle.addActionListener(e -> {
            isWeightedMode = !isWeightedMode;
            btnWeighted.setSelected(isWeightedMode);
            refresh();
        });
        menuOptions.add(weightedToggle);

        item = new JMenuItem("Open File");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        item.addActionListener(new MenuListener());
        menuOptions1.add(item);

        item = new JMenuItem("Save to File");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        item.addActionListener(new MenuListener());
        menuOptions1.add(item);

        item = new JMenuItem("Add Edges");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
        item.addActionListener(new MenuListener());
        menuOptions.add(item);

        item = new JMenuItem("Grab Tool");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
        item.addActionListener(new MenuListener());
        menuOptions.add(item);

        item = new JMenuItem("Remove Tool");
        item.addActionListener(new MenuListener());
        menuOptions.add(item);

        item = new JMenuItem("Auto Arrange Vertices");
        item.addActionListener(new MenuListener());
        menuOptions2.add(item);

        item = new JMenuItem("Remove All");
        item.addActionListener(new MenuListener());
        menuOptions2.add(item);

        item = new JMenuItem("Graph");
        item.addActionListener(new MenuListener());
        menuOptions3.add(item);

        item = new JMenuItem("Properties");
        item.addActionListener(new MenuListener());
        menuOptions3.add(item);

        menuBar.add(menuOptions1);
        menuBar.add(menuOptions);
        menuBar.add(menuOptions2);
        menuBar.add(menuOptions3);
        frame.setJMenuBar(menuBar);

        // ── Toolbar ───────────────────────────────────────────────────────────
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(3, 4, 3, 4)));

        // File group
        JButton btnOpen = makeToolButton("Open", "open.png", "Open graph file (Ctrl+O)");
        btnOpen.setActionCommand("Open File");   // match MenuListener
        btnOpen.addActionListener(new MenuListener());
        toolBar.add(btnOpen);

        JButton btnSave = makeToolButton("Save", "save.png", "Save graph to file (Ctrl+S)");
        btnSave.setActionCommand("Save to File"); // match MenuListener
        btnSave.addActionListener(new MenuListener());
        toolBar.add(btnSave);

        toolBar.addSeparator();

        // Tool group
        btnAddVertex = makeToolButton("Add Vertex", "add_vertex.png", "Add Vertex tool (Ctrl+A)");
        btnAddVertex.addActionListener(e -> setActiveTool(1));
        toolBar.add(btnAddVertex);

        btnAddEdge = makeToolButton("Add Edge", "add_edge.png", "Add Edge tool (Ctrl+E)");
        btnAddEdge.addActionListener(e -> setActiveTool(2));
        toolBar.add(btnAddEdge);

        btnGrab = makeToolButton("Grab", "grab.png", "Grab / Move tool (Ctrl+G)");
        btnGrab.addActionListener(e -> setActiveTool(3));
        toolBar.add(btnGrab);

        btnRemove = makeToolButton("Remove", "remove.png", "Remove element tool");
        btnRemove.addActionListener(e -> setActiveTool(4));
        toolBar.add(btnRemove);

        toolBar.addSeparator();

        // Toggle group
        btnDirected = makeToggleButton("Directed", "directed.png", "Toggle directed edges (Ctrl+D)");
        btnDirected.addActionListener(e -> {
            isDirectedMode = btnDirected.isSelected();
            System.out.println("Directed mode: " + isDirectedMode);
        });
        toolBar.add(btnDirected);

        btnWeighted = makeToggleButton("Weighted", "weighted.png", "Toggle weighted edge mode");
        btnWeighted.addActionListener(e -> {
            isWeightedMode = btnWeighted.isSelected();
            refresh();
        });
        toolBar.add(btnWeighted);

        toolBar.addSeparator();

        // Extras group
        JButton btnArrange = makeToolButton("Auto Arrange", "auto_arrange.png", "Auto-arrange vertices in a circle");
        btnArrange.addActionListener(e -> {
            arrangeVertices();
            erase();
            refresh();
        });
        toolBar.add(btnArrange);

        JButton btnRemoveAll = makeToolButton("Remove All", "remove_all.png", "Clear all vertices and edges");
        btnRemoveAll.addActionListener(e -> {
            edgeList.removeAllElements();
            vertexList.removeAllElements();
            clickedVertexIndex = 0;
            erase();
            refresh();
        });
        toolBar.add(btnRemoveAll);

        toolBar.addSeparator();

        // View group
        btnViewGraph = makeToolButton("Graph", "graph.png", "Switch to Graph view");
        btnViewGraph.addActionListener(new MenuListener());
        btnViewGraph.setVisible(false);   // hidden on startup (we begin in graph view)
        toolBar.add(btnViewGraph);

        btnViewProps = makeToolButton("Properties", "properties.png", "Switch to Properties view");
        btnViewProps.addActionListener(new MenuListener());
        toolBar.add(btnViewProps);

        // ── Layout ────────────────────────────────────────────────────────────
        backgroundColour = bgColour;   // must be set before buildRightPanel()
        frame.setLayout(new BorderLayout());
        frame.add(toolBar, BorderLayout.NORTH);

        canvas = new CanvasPane();
        InputListener inputListener = new InputListener();
        canvas.addMouseListener(inputListener);
        canvas.addMouseMotionListener(inputListener);
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(buildRightPanel(), BorderLayout.EAST);

        this.width = width;
        this.height = height;
        canvas.setPreferredSize(new Dimension(width, height));

        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(screenSize.width / 2 - width / 2, screenSize.height / 2 - height / 2, width, height);
        frame.pack();
        setVisible(true);

        vertexList = new Vector<Vertex>();
        edgeList   = new Vector<Edge>();
    }

    // ── Toolbar helper factories ───────────────────────────────────────────────

    /**
     * Creates a plain JButton for the toolbar. It tries to load an icon from
     * the icons/ resource folder. If the icon file is missing the button falls
     * back to its text label so placeholders work immediately.
     */
    private JButton makeToolButton(String text, String iconFile, String tooltip) {
        JButton btn = new JButton();
        ImageIcon icon = loadIcon(iconFile);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setToolTipText(tooltip + "  [" + text + "]");
        } else {
            btn.setText(text);
            btn.setToolTipText(tooltip);
        }
        btn.setFocusPainted(false);
        btn.setActionCommand(text);
        return btn;
    }

    /**
     * Same as makeToolButton but returns a JToggleButton (stays pressed
     * when active — used for Directed / Weighted mode toggles).
     */
    private JToggleButton makeToggleButton(String text, String iconFile, String tooltip) {
        JToggleButton btn = new JToggleButton();
        ImageIcon icon = loadIcon(iconFile);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setToolTipText(tooltip + "  [" + text + "]");
        } else {
            btn.setText(text);
            btn.setToolTipText(tooltip);
        }
        btn.setFocusPainted(false);
        return btn;
    }

    /**
     * Loads an ImageIcon from the icons/ resource folder beside this class.
     * Returns null if the file doesn't exist, so callers can fall back to text.
     */
    private ImageIcon loadIcon(String filename) {
        URL url = getClass().getResource("icons/" + filename);
        if (url == null) return null;
        return new ImageIcon(url);
    }

    /**
     * Sets the active tool and updates button highlight to show which tool
     * is currently selected.
     */
    private void setActiveTool(int tool) {
        selectedTool = tool;
        JButton[] toolBtns = { btnAddVertex, btnAddEdge, btnGrab, btnRemove };
        int[]     toolIds  = { 1, 2, 3, 4 };
        for (int i = 0; i < toolBtns.length; i++) {
            boolean active = (toolIds[i] == tool);
            toolBtns[i].setBackground(active ? TOOL_ACTIVE_BG : TOOL_INACTIVE_BG);
            toolBtns[i].setOpaque(active);
            toolBtns[i].setBorder(active
                    ? BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(80, 140, 220), 1),
                            new EmptyBorder(3, 6, 3, 6))
                    : UIManager.getBorder("Button.border"));
        }
    }

    class InputListener implements MouseListener, MouseMotionListener {

        @Override
        public void mouseClicked(MouseEvent e) {

            if (selectedWindow == 0) {
                switch (selectedTool) {
                    case 1: {
                        Vertex v = new Vertex("" + vertexList.size(), e.getX(), e.getY());
                        v.outerColor = defaultNodeOuter;
                        v.innerColor = defaultNodeInner;
                        v.nodeSize   = defaultNodeSize;
                        vertexList.add(v);
                        v.draw(graphic);
                        // Select the new vertex in the style pane
                        selectedVertex = v;
                        selectedEdge   = null;
                        refreshSelectionPanel();
                        break;
                    }
                    case 4: {

                        /* for (Vertex v : vertexList) {
                        if (v.hasIntersection(e.getX(), e.getY())) {
                        {
                        for (Edge d : edgeList) {
                        if (d.vertex1 == v || d.vertex2 == v) {
                        edgeList.remove(d);
                        }
                        }
                        for (Vertex x : vertexList) {
                        if (x.connectedToVertex(v)) {
                        x.connectedVertices.remove(v);
                        }
                        }
                        vertexList.remove(v);
                        }
                        }
                        }*/ break;
                    }
                }
            //refresh();
            }


        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (selectedWindow == 0 && vertexList.size() > 0) {
                switch (selectedTool) {
                    case 2: {
                        for (Vertex v : vertexList) {
                            if (v.hasIntersection(e.getX(), e.getY())) {
                                v.wasClicked = true;
                                clickedVertexIndex = vertexList.indexOf(v);
                            } else {
                                v.wasClicked = false;
                            }
                        }
                        break;
                    }
                    case 3: {
                        // Check edges first, vertex click overrides if both hit
                        selectedVertex = null;
                        selectedEdge   = null;
                        for (Edge d : edgeList) {
                            if (d.hasIntersection(e.getX(), e.getY())) {
                                d.wasClicked = true;
                                clickedEdgeIndex = edgeList.indexOf(d);
                                selectedEdge = d;
                            } else {
                                d.wasClicked = false;
                            }
                        }
                        for (Vertex v : vertexList) {
                            if (v.hasIntersection(e.getX(), e.getY())) {
                                v.wasClicked = true;
                                clickedVertexIndex = vertexList.indexOf(v);
                                selectedVertex = v;      // vertex wins over edge
                                selectedEdge   = null;
                            } else {
                                v.wasClicked = false;
                            }
                        }
                        refreshSelectionPanel();
                        break;
                    }
                }
            }

        }

        @Override
        /* Changes made here */
        public void mouseReleased(MouseEvent e) {
            if (selectedWindow == 0 && vertexList.size() > 0) {
                switch (selectedTool) {
                    case 2: {
                        Vertex parentV = vertexList.get(clickedVertexIndex);
                        for (Vertex v : vertexList) {
                            if (v.hasIntersection(e.getX(), e.getY()) && v != parentV) {              //System.out.println(clickedVertexIndex+" "+vertexList.indexOf(v));
                                // For undirected mode, prevent duplicates
                                if (!isDirectedMode && v.connectedToVertex(parentV)) {
                                    continue; //skip if already connected
                                }
                                /* Allows directed edge creation*/
                                Edge edge = new Edge(parentV, v, isDirectedMode);
                                edge.lineColor   = defaultEdgeColor;
                                edge.strokeWidth = defaultEdgeStroke;
                                //////////
                                if (!isDirectedMode) {
                                    v.addVertex(parentV);
                                    parentV.addVertex(v);
                                }   else {
                                    // for directed graphs, only add connection in one direction
                                    parentV.addVertex(v);
                                }
                                    
                                v.wasClicked = false;
                                parentV.wasClicked = false;
                                edgeList.add(edge);
                                // Select the new edge in the style pane
                                selectedEdge   = edge;
                                selectedVertex = null;
                                refreshSelectionPanel();
                            } else {
                                v.wasClicked = false;
                            }
                        }
                        // Handle Weight assignment for edges
                        if (isWeightedMode) {
                            for (Edge edge : edgeList) {
                                if (edge.hasIntersection(e.getX(), e.getY())) {
                                    String input = javax.swing.JOptionPane.showInputDialog("Enter weight for this edge:");
                                    try {
                                        int newWeight = Integer.parseInt(input);
                                        edge.weight = newWeight;
                                        refresh();
                                    } catch (NumberFormatException ex) {
                                        javax.swing.JOptionPane.showMessageDialog(null, "Invalid input. Please enter an integer.");
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case 3: {
                        vertexList.get(clickedVertexIndex).wasClicked = false;
                        break;
                    }
                }
            }
            erase();
            refresh();
        }

        @Override
        public void mouseDragged(MouseEvent e) {

            if (selectedWindow == 0 && vertexList.size() > 0) {
                erase();
                switch (selectedTool) {
                    case 2: {
                        graphic.setColor(Color.RED);
                        drawLine(vertexList.get(clickedVertexIndex).location.x, vertexList.get(clickedVertexIndex).location.y, e.getX(), e.getY());
                        break;

                    }
                    case 3: {
                        if (vertexList.get(clickedVertexIndex).wasClicked) {
                            vertexList.get(clickedVertexIndex).location.x = e.getX();
                            vertexList.get(clickedVertexIndex).location.y = e.getY();
                        }
                        break;
                    }
                }
                refresh();
            }

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (selectedWindow == 0) {
                // adjusted for hover weights
                for (Edge d : edgeList) {
                    if (d.hasIntersection(e.getX(), e.getY())) {
                        d.wasHovered = true;
                    } else {
                        d.wasHovered = false;
                    }
                }
                for (Vertex v : vertexList) {
                    if (v.hasIntersection(e.getX(), e.getY())) {
                        v.wasFocused = true;
                    } else {
                        v.wasFocused = false;
                    }
                }
                refresh();
            }

        }
    }

    class MenuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("Add Vertex")) {
                setActiveTool(1);
            } else if (command.equals("Add Edges")) {
                setActiveTool(2);
            } else if (command.equals("Grab Tool")) {
                setActiveTool(3);
            } else if (command.equals("Remove Tool")) {
                setActiveTool(4);
            } else if (command.equals("Auto Arrange Vertices")) {
                arrangeVertices();
                erase();
            } else if (command.equals("Remove All")) {
                edgeList.removeAllElements();
                vertexList.removeAllElements();
                clickedVertexIndex = 0;
                erase();
            } else if (command.equals("Open File")) {
                int returnValue = fileManager.jF.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    loadFile(fileManager.loadFile(fileManager.jF.getSelectedFile()));
                    System.out.println(fileManager.jF.getSelectedFile());
                    selectedWindow = 0;
                    btnViewGraph.setVisible(false);
                    btnViewProps.setVisible(true);
                }
            } else if (command.equals("Save to File")) {
                int returnValue = fileManager.jF.showSaveDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    fileManager.saveFile(vertexList,fileManager.jF.getSelectedFile());
                    System.out.println(fileManager.jF.getSelectedFile());
                }
            } else if (command.equals("Graph")) {
                selectedWindow = 0;
                btnViewGraph.setVisible(false);
                btnViewProps.setVisible(true);
                erase();
            } else if (command.equals("Properties")) {
                selectedWindow = 1;
                btnViewGraph.setVisible(true);
                btnViewProps.setVisible(false);
                if (vertexList.size() > 0) {
                    //adjacency list
                    int[][] matrix = gP.generateAdjacencyMatrix(vertexList, edgeList);

                    //connectivity
                    Vector<Vertex> tempList = gP.vertexConnectivity(vertexList);
                    for (Vertex v : tempList) {
                        vertexList.get(vertexList.indexOf(v)).wasClicked = true;
                    }
                    reloadVertexConnections(matrix, vertexList);

                    //distance
                    gP.generateDistanceMatrix(vertexList);

                    //VD paths
                    gP.displayContainers(vertexList);
                //gP.drawNWideDiameter();
                }
                erase();
            }

            refresh();
        }
    }

    private void arrangeVertices() {
        double deg2rad = Math.PI / 180;
        double radius = height / 5;
        double centerX = width / 2;
        double centerY = height / 2;
        int interval = 360 / vertexList.size();


        for (int i = 0; i < vertexList.size(); i++) {
            double degInRad = i * deg2rad * interval;
            double x = centerX + (Math.cos(degInRad) * radius);
            double y = centerY + (Math.sin(degInRad) * radius);
            int X = (int) x;
            int Y = (int) y;
            vertexList.get(i).location.x = X;
            vertexList.get(i).location.y = Y;
        }

    }

    private void reloadVertexConnections(int[][] aMatrix, Vector<Vertex> vList) {
        for (Vertex v : vList) {
            v.connectedVertices.clear();
        }

        for (int i = 0; i < aMatrix.length; i++) {
            for (int j = 0; j < aMatrix.length; j++) {
                if (aMatrix[i][j] == 1) {
                    vList.get(i).addVertex(vList.get(j));
                }
            }
        }

    }

    private void loadFile(Vector<Vector> File) {
        vertexList = File.firstElement();
        edgeList = File.lastElement();
        erase();
    }

    public void refresh() {
        for (Edge e : edgeList) {
            e.draw(graphic);
        }
        for (Vertex v : vertexList) {
            v.draw(graphic);
        }

        canvas.repaint();
    }

    public void setVisible(boolean visible) {
        if (graphic == null) {
            Dimension size = canvas.getSize();
            canvasImage = canvas.createImage(size.width, size.height);
            canvasImage2 = canvas.createImage(size.width, size.height);
            graphic = (Graphics2D) canvasImage.getGraphics();
            graphic.setColor(backgroundColour);
            graphic.fillRect(0, 0, size.width, size.height);
            graphic.setColor(Color.black);
        }
        frame.setVisible(visible);
    }

    public boolean isVisible() {
        return frame.isVisible();
    }

    public void erase() {
        // Fill with backgroundColour so canvas color changes are visible
        graphic.setColor(backgroundColour);
        graphic.fillRect(0, 0, width, height);
    }

    public void erase(int x, int y, int x1, int y2) {
        graphic.clearRect(x, y, x1, y2);
    }

    public void drawString(String text, int x, int y, float size) {
        Font orig = graphic.getFont();
        graphic.setFont(graphic.getFont().deriveFont(1, size));
        graphic.drawString(text, x, y);
        graphic.setFont(orig);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        graphic.drawLine(x1, y1, x2, y2);
    }

    private class CanvasPane extends JPanel {

        public void paint(Graphics g) {
            switch (selectedWindow) {
                case 0: {   //graph window
                    graphic.drawString("Vertex Count=" + vertexList.size() +
                            "  Edge Count=" + edgeList.size() +
                            "  Selected Tool=" + selectedTool, 50, height / 2 + (height * 2) / 5);
                            
                    /* Shows if toggle for direction is on/off */
                    graphic.drawString(
                        "Directed Mode=" + isDirectedMode,
                        50, height / 2 + (height * 2) / 5 + 20
                    );

                    /* SHows if toggle for weight is on/off */
                    graphic.drawString("Weighted Mode=" + isWeightedMode,
                        50, height / 2 + (height * 2) / 5 + 40);


                    g.drawImage(canvasImage, 0, 0, null); //layer 1
                    g.setColor(Color.black);
                    break;
                }
                case 1: {   //properties window
                    canvasImage2.getGraphics().clearRect(0, 0, width, height); //clear
                    gP.drawAdjacencyMatrix(canvasImage2.getGraphics(), vertexList, width / 2 + 50, 50);//draw adjacency matrix
                    gP.drawDistanceMatrix(canvasImage2.getGraphics(), vertexList, width / 2 + 50, height / 2 + 50);//draw distance matrix
                    g.drawImage(canvasImage2, 0, 0, null); //layer 1
                    drawString("Graph disconnects when nodes in color red are removed.", 100, height - 30, 20);
                    g.drawString("See output console for Diameter of Graph", 100, height / 2 + 50);
                    g.drawImage(canvasImage.getScaledInstance(width / 2, height / 2, Image.SCALE_SMOOTH), 0, 0, null); //layer 1
                    g.draw3DRect(0, 0, width / 2, height / 2, true);
                    g.setColor(Color.black);

                    break;
                }
            }

        }
    }


    // ── Right-side Style Pane ──────────────────────────────────────────────────

    private JScrollPane buildRightPanel() {

        // ── Canvas section ────────────────────────────────────────────────────
        JPanel canvasSection = createSection("Canvas");
        JButton bgBtn = makeColorBtn("Background", backgroundColour);
        bgBtn.addActionListener(ev -> {
            Color c = JColorChooser.showDialog(frame, "Canvas Background", backgroundColour);
            if (c != null) { backgroundColour = c; styleColorBtn(bgBtn, c); erase(); refresh(); }
        });
        canvasSection.add(bgBtn);
        canvasSection.add(Box.createVerticalStrut(2));

        // ── Selected section (CardLayout: none / vertex / edge) ───────────────
        JPanel selectedSection = createSection("Selected");

        selectionTitle = new JLabel("Nothing selected");
        selectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectionTitle.setFont(selectionTitle.getFont().deriveFont(Font.BOLD, 11f));
        selectionTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        selectedSection.add(selectionTitle);
        selectedSection.add(Box.createVerticalStrut(3));

        selectionCards = new JPanel(new CardLayout());
        selectionCards.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Card: nothing selected
        JPanel noneCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        JLabel noneLabel = new JLabel("Click a node or edge");
        noneLabel.setForeground(Color.GRAY);
        noneLabel.setFont(noneLabel.getFont().deriveFont(11f));
        noneCard.add(noneLabel);
        selectionCards.add(noneCard, "none");

        // Card: vertex selected
        JPanel vertexCard = new JPanel();
        vertexCard.setLayout(new BoxLayout(vertexCard, BoxLayout.Y_AXIS));

        selVertexOuterBtn = makeColorBtn("Outer Color", defaultNodeOuter);
        selVertexOuterBtn.addActionListener(ev -> {
            if (selectedVertex == null) return;
            Color c = JColorChooser.showDialog(frame, "Node Outer Color", selectedVertex.outerColor);
            if (c != null) { selectedVertex.outerColor = c; styleColorBtn(selVertexOuterBtn, c); erase(); refresh(); }
        });
        vertexCard.add(selVertexOuterBtn);

        selVertexInnerBtn = makeColorBtn("Inner Fill", defaultNodeInner);
        selVertexInnerBtn.addActionListener(ev -> {
            if (selectedVertex == null) return;
            Color c = JColorChooser.showDialog(frame, "Node Inner Fill", selectedVertex.innerColor);
            if (c != null) { selectedVertex.innerColor = c; styleColorBtn(selVertexInnerBtn, c); erase(); refresh(); }
        });
        vertexCard.add(selVertexInnerBtn);

        selVertexSizeLabel = new JLabel("Size: " + defaultNodeSize + " px");
        selVertexSizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selVertexSizeLabel.setFont(selVertexSizeLabel.getFont().deriveFont(11f));
        selVertexSizeLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        vertexCard.add(Box.createVerticalStrut(3));
        vertexCard.add(selVertexSizeLabel);

        selVertexSizeSlider = new JSlider(16, 80, defaultNodeSize);
        selVertexSizeSlider.setMajorTickSpacing(16);
        selVertexSizeSlider.setPaintTicks(true);
        selVertexSizeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        selVertexSizeSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        selVertexSizeSlider.addChangeListener(ev -> {
            if (selectedVertex == null) return;
            selectedVertex.nodeSize = selVertexSizeSlider.getValue();
            selVertexSizeLabel.setText("Size: " + selectedVertex.nodeSize + " px");
            erase(); refresh();
        });
        vertexCard.add(sliderWrap(selVertexSizeSlider));
        vertexCard.add(Box.createVerticalStrut(2));
        selectionCards.add(vertexCard, "vertex");

        // Card: edge selected
        JPanel edgeCard = new JPanel();
        edgeCard.setLayout(new BoxLayout(edgeCard, BoxLayout.Y_AXIS));

        selEdgeColorBtn = makeColorBtn("Color", defaultEdgeColor);
        selEdgeColorBtn.addActionListener(ev -> {
            if (selectedEdge == null) return;
            Color c = JColorChooser.showDialog(frame, "Edge Color", selectedEdge.lineColor);
            if (c != null) { selectedEdge.lineColor = c; styleColorBtn(selEdgeColorBtn, c); erase(); refresh(); }
        });
        edgeCard.add(selEdgeColorBtn);

        selEdgeStrokeLabel = new JLabel("Stroke: " + (int) defaultEdgeStroke + " px");
        selEdgeStrokeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selEdgeStrokeLabel.setFont(selEdgeStrokeLabel.getFont().deriveFont(11f));
        selEdgeStrokeLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        edgeCard.add(Box.createVerticalStrut(3));
        edgeCard.add(selEdgeStrokeLabel);

        selEdgeStrokeSlider = new JSlider(1, 12, (int) defaultEdgeStroke);
        selEdgeStrokeSlider.setMajorTickSpacing(3);
        selEdgeStrokeSlider.setPaintTicks(true);
        selEdgeStrokeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        selEdgeStrokeSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        selEdgeStrokeSlider.addChangeListener(ev -> {
            if (selectedEdge == null) return;
            selectedEdge.strokeWidth = selEdgeStrokeSlider.getValue();
            selEdgeStrokeLabel.setText("Stroke: " + (int) selectedEdge.strokeWidth + " px");
            erase(); refresh();
        });
        edgeCard.add(sliderWrap(selEdgeStrokeSlider));
        edgeCard.add(Box.createVerticalStrut(2));
        selectionCards.add(edgeCard, "edge");

        ((CardLayout) selectionCards.getLayout()).show(selectionCards, "none");
        selectedSection.add(selectionCards);
        selectedSection.add(Box.createVerticalStrut(2));

        // ── Defaults section ──────────────────────────────────────────────────
        JPanel defaultsSection = createSection("Defaults");

        JLabel nodeDefaultsLabel = new JLabel("Nodes (new)");
        nodeDefaultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nodeDefaultsLabel.setFont(nodeDefaultsLabel.getFont().deriveFont(Font.BOLD, 11f));
        nodeDefaultsLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        defaultsSection.add(nodeDefaultsLabel);

        JButton nodeOuterBtn = makeColorBtn("Outer Color", defaultNodeOuter);
        nodeOuterBtn.addActionListener(ev -> {
            Color c = JColorChooser.showDialog(frame, "Default Node Outer", defaultNodeOuter);
            if (c != null) { defaultNodeOuter = c; styleColorBtn(nodeOuterBtn, c); }
        });
        defaultsSection.add(nodeOuterBtn);

        JButton nodeInnerBtn = makeColorBtn("Inner Fill", defaultNodeInner);
        nodeInnerBtn.addActionListener(ev -> {
            Color c = JColorChooser.showDialog(frame, "Default Node Inner", defaultNodeInner);
            if (c != null) { defaultNodeInner = c; styleColorBtn(nodeInnerBtn, c); }
        });
        defaultsSection.add(nodeInnerBtn);

        JLabel nodeSzLabel = new JLabel("Size: " + defaultNodeSize + " px");
        nodeSzLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nodeSzLabel.setFont(nodeSzLabel.getFont().deriveFont(11f));
        nodeSzLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        defaultsSection.add(Box.createVerticalStrut(2));
        defaultsSection.add(nodeSzLabel);
        JSlider nodeSzSlider = new JSlider(16, 80, defaultNodeSize);
        nodeSzSlider.setMajorTickSpacing(16);
        nodeSzSlider.setPaintTicks(true);
        nodeSzSlider.addChangeListener(ev -> {
            defaultNodeSize = nodeSzSlider.getValue();
            nodeSzLabel.setText("Size: " + defaultNodeSize + " px");
        });
        defaultsSection.add(sliderWrap(nodeSzSlider));

        defaultsSection.add(Box.createVerticalStrut(4));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        defaultsSection.add(sep);
        defaultsSection.add(Box.createVerticalStrut(4));

        JLabel edgeDefaultsLabel = new JLabel("Edges (new)");
        edgeDefaultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        edgeDefaultsLabel.setFont(edgeDefaultsLabel.getFont().deriveFont(Font.BOLD, 11f));
        edgeDefaultsLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        defaultsSection.add(edgeDefaultsLabel);

        JButton edgeColorBtn = makeColorBtn("Color", defaultEdgeColor);
        edgeColorBtn.addActionListener(ev -> {
            Color c = JColorChooser.showDialog(frame, "Default Edge Color", defaultEdgeColor);
            if (c != null) { defaultEdgeColor = c; styleColorBtn(edgeColorBtn, c); }
        });
        defaultsSection.add(edgeColorBtn);

        JLabel strokeDefLabel = new JLabel("Stroke: " + (int) defaultEdgeStroke + " px");
        strokeDefLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        strokeDefLabel.setFont(strokeDefLabel.getFont().deriveFont(11f));
        strokeDefLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        defaultsSection.add(Box.createVerticalStrut(2));
        defaultsSection.add(strokeDefLabel);
        JSlider strokeDefSlider = new JSlider(1, 12, (int) defaultEdgeStroke);
        strokeDefSlider.setMajorTickSpacing(3);
        strokeDefSlider.setPaintTicks(true);
        strokeDefSlider.addChangeListener(ev -> {
            defaultEdgeStroke = strokeDefSlider.getValue();
            strokeDefLabel.setText("Stroke: " + (int) defaultEdgeStroke + " px");
        });
        defaultsSection.add(sliderWrap(strokeDefSlider));
        defaultsSection.add(Box.createVerticalStrut(2));

        // ── Assemble ─────────────────────────────────────────────────────────
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBorder(new EmptyBorder(4, 6, 4, 6));
        outer.add(canvasSection);
        outer.add(Box.createVerticalStrut(5));
        outer.add(selectedSection);
        outer.add(Box.createVerticalStrut(5));
        outer.add(defaultsSection);
        outer.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(outer);
        scroll.setPreferredSize(new Dimension(215, 100));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));
        return scroll;
    }

    /** Refreshes the "Selected" card based on the current selection. */
    private void refreshSelectionPanel() {
        if (selectionCards == null) return;
        CardLayout cl = (CardLayout) selectionCards.getLayout();
        if (selectedVertex != null) {
            selectionTitle.setText("Node: " + selectedVertex.name);
            styleColorBtn(selVertexOuterBtn, selectedVertex.outerColor);
            styleColorBtn(selVertexInnerBtn, selectedVertex.innerColor);
            selVertexSizeLabel.setText("Size: " + selectedVertex.nodeSize + " px");
            selVertexSizeSlider.setValue(selectedVertex.nodeSize);
            cl.show(selectionCards, "vertex");
        } else if (selectedEdge != null) {
            String label = selectedEdge.vertex1.name + (selectedEdge.directed ? " \u2192 " : " \u2014 ")
                           + selectedEdge.vertex2.name;
            selectionTitle.setText("Edge: " + label);
            styleColorBtn(selEdgeColorBtn, selectedEdge.lineColor);
            selEdgeStrokeLabel.setText("Stroke: " + (int) selectedEdge.strokeWidth + " px");
            selEdgeStrokeSlider.setValue((int) selectedEdge.strokeWidth);
            cl.show(selectionCards, "edge");
        } else {
            selectionTitle.setText("Nothing selected");
            cl.show(selectionCards, "none");
        }
    }

    /** Wraps a JSlider in a BorderLayout panel to constrain its width in BoxLayout. */
    private JPanel sliderWrap(JSlider slider) {
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        wrap.add(slider, BorderLayout.CENTER);
        return wrap;
    }

    /** Creates a titled section panel with BoxLayout Y_AXIS. */
    private JPanel createSection(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /** Creates a color-swatch button (icon + label, system button background). */
    private JButton makeColorBtn(String label, Color initial) {
        JButton btn = new JButton(label, colorSwatch(initial));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        btn.setPreferredSize(new Dimension(180, 26));
        btn.setMargin(new Insets(1, 4, 1, 4));
        btn.setFont(btn.getFont().deriveFont(11f));
        btn.setFocusPainted(false);
        return btn;
    }

    /** Updates the color-swatch icon on a button. */
    private void styleColorBtn(JButton btn, Color c) {
        btn.setIcon(colorSwatch(c));
    }

    /** 16x16 filled square with a 1px gray border. */
    private Icon colorSwatch(Color c) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(c);
        g2.fillRect(0, 0, 16, 16);
        g2.setColor(Color.GRAY);
        g2.drawRect(0, 0, 15, 15);
        g2.dispose();
        return new ImageIcon(img);
    }

    /** Pushes current node style defaults to every vertex then repaints. */
    private void applyNodeStyleToAll() {
        for (Vertex v : vertexList) {
            v.outerColor = defaultNodeOuter;
            v.innerColor = defaultNodeInner;
            v.nodeSize   = defaultNodeSize;
        }
        erase();
        refresh();
    }

    /** Pushes current edge style defaults to every edge then repaints. */
    private void applyEdgeStyleToAll() {
        for (Edge e : edgeList) {
            e.lineColor   = defaultEdgeColor;
            e.strokeWidth = defaultEdgeStroke;
        }
        erase();
        refresh();
    }
}
