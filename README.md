# Graph Theory MP - README

# =============================

# 

# This document summarizes the modifications made to the project files

# to support directed edges, weighted edges, and hover-based interaction.

# 

# ------------------------------------------------------------

# 1\. Edge.java

# ------------------------------------------------------------

# \- Added new fields:

# &nbsp; - public int weight;        // stores edge weight

# &nbsp; - public boolean wasHovered; // flag for hover highlighting

# 

# \- Constructors updated:

# &nbsp; - Edge(Vertex v1, Vertex v2) → defaults to undirected, weight = 1

# &nbsp; - Edge(Vertex v1, Vertex v2, boolean directed) → defaults to weight = 1

# &nbsp; - Edge(Vertex v1, Vertex v2, boolean directed, int weight) → main constructor

# 

# \- draw(Graphics g) changes:

# &nbsp; - Trimmed both start and end points so lines do not pierce nodes.

# &nbsp; - Arrowhead tip placed just outside target node.

# &nbsp; - Midpoint calculated for weight label placement.

# &nbsp; - If directed, labels offset perpendicular to edge to avoid overlap

# &nbsp;   in bidirectional edges.

# &nbsp; - Weight label color changes to RED when hovered (wasHovered = true).

# 

# \- drawArrowHead(Graphics g) unchanged except for integration with trimmed line.

# 

# \- hasIntersection(int x, int y) rewritten:

# &nbsp; - Now checks proximity to edge midpoint instead of slope-based line detection.

# &nbsp; - Uses tolerance (20px) around midpoint for easier clicking/hovering.

# 

# ------------------------------------------------------------

# 2\. Canvas.java

# ------------------------------------------------------------

# \- Added new toggle flag:

# &nbsp; - private boolean isWeightedMode = false;

# 

# \- Added menu item or shortcut to toggle Weighted Mode:

# &nbsp; - "Toggle Weighted Mode" flips isWeightedMode and updates GUI.

# 

# \- In mouseReleased(MouseEvent e):

# &nbsp; - After edge creation (case 2), if isWeightedMode is true:

# &nbsp;   - Loop through edgeList.

# &nbsp;   - If mouse click is near edge midpoint (hasIntersection):

# &nbsp;     - Prompt user with JOptionPane for weight input.

# &nbsp;     - Parse integer and assign to edge.weight.

# &nbsp;     - Call refresh() to redraw canvas.

# 

# \- Added MouseMotionListener:

# &nbsp; - On mouseMoved, checks each edge with hasIntersection.

# &nbsp; - Sets edge.wasHovered = true if mouse near midpoint, else false.

# &nbsp; - Calls refresh() to update hover highlighting.

# 

# ------------------------------------------------------------

# 3\. GUI Feedback

# ------------------------------------------------------------

# \- Weighted Mode status displayed in GUI (similar to Directed Mode).

# \- Hovering over edge midpoint highlights weight label in RED.

# \- Clicking in Weighted Mode opens input dialog to change weight.

# \- Bidirectional edges show weights on opposite sides of the line.

# 

# ------------------------------------------------------------

# 4\. Conceptual Notes

# ------------------------------------------------------------

# \- Each directed edge object stores its own weight, so bidirectional

# &nbsp; edges can have different values (e.g., A→B = 5, B→A = 2).

# \- Midpoint-based detection makes editing weights intuitive and

# &nbsp; user-friendly compared to slope-based line detection.

# 

# ------------------------------------------------------------

# End of README

