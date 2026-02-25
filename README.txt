Graph Theory MP - README
=============================

This document summarizes the modifications made to the project files
to support directed edges, weighted edges, and hover-based interaction.

------------------------------------------------------------
1. Edge.java
------------------------------------------------------------
- Added new fields:
  - public int weight;        // stores edge weight
  - public boolean wasHovered; // flag for hover highlighting

- Constructors updated:
  - Edge(Vertex v1, Vertex v2) → defaults to undirected, weight = 1
  - Edge(Vertex v1, Vertex v2, boolean directed) → defaults to weight = 1
  - Edge(Vertex v1, Vertex v2, boolean directed, int weight) → main constructor

- draw(Graphics g) changes:
  - Trimmed both start and end points so lines do not pierce nodes.
  - Arrowhead tip placed just outside target node.
  - Midpoint calculated for weight label placement.
  - If directed, labels offset perpendicular to edge to avoid overlap
    in bidirectional edges.
  - Weight label color changes to RED when hovered (wasHovered = true).

- drawArrowHead(Graphics g) unchanged except for integration with trimmed line.

- hasIntersection(int x, int y) rewritten:
  - Now checks proximity to edge midpoint instead of slope-based line detection.
  - Uses tolerance (20px) around midpoint for easier clicking/hovering.

------------------------------------------------------------
2. Canvas.java
------------------------------------------------------------
- Added new toggle flag:
  - private boolean isWeightedMode = false;

- Added menu item or shortcut to toggle Weighted Mode:
  - "Toggle Weighted Mode" flips isWeightedMode and updates GUI.

- In mouseReleased(MouseEvent e):
  - After edge creation (case 2), if isWeightedMode is true:
    - Loop through edgeList.
    - If mouse click is near edge midpoint (hasIntersection):
      - Prompt user with JOptionPane for weight input.
      - Parse integer and assign to edge.weight.
      - Call refresh() to redraw canvas.

- Added MouseMotionListener:
  - On mouseMoved, checks each edge with hasIntersection.
  - Sets edge.wasHovered = true if mouse near midpoint, else false.
  - Calls refresh() to update hover highlighting.

------------------------------------------------------------
3. GUI Feedback
------------------------------------------------------------
- Weighted Mode status displayed in GUI (similar to Directed Mode).
- Hovering over edge midpoint highlights weight label in RED.
- Clicking in Weighted Mode opens input dialog to change weight.
- Bidirectional edges show weights on opposite sides of the line.

------------------------------------------------------------
4. Conceptual Notes
------------------------------------------------------------
- Each directed edge object stores its own weight, so bidirectional
  edges can have different values (e.g., A→B = 5, B→A = 2).
- Midpoint-based detection makes editing weights intuitive and
  user-friendly compared to slope-based line detection.

------------------------------------------------------------


PS: Changes are done inside the "-Graph-Theory-v0.5" folder and not the _MACOSX.


# Graphitti Project Updates

## Recent Additions (February, 25, 2026)
- **Undo/Redo Framework**
  - Implemented `ActionHistory` integration for vertices and edges.
  - Added lambdas to record state changes for vertex creation, removal, and edge creation.
  - Debugging prints included to trace undo/redo actions in the terminal.

- **Weight Assignment**
  - Added support for assigning integer weights to edges.
  - Undo/redo now correctly restores previous weights.

- **Canvas Refresh Improvements**
  - Introduced `erase()` method to clear the canvas before redraw.
  - Updated `refresh()` to always clear and redraw from `vertexList` and `edgeList`.
  - Prevents ghost vertices/edges from persisting after undo/redo.

## Known Issues
- Vertex undo/redo updates backend lists correctly but GUI redraw still needs refinement.
- Edge undo/redo works intermittently; further testing required.
- Debugging prints currently show object references (e.g., `graphtheory.Vertex@1234`); adding `toString()` methods in `Vertex` and `Edge` will improve readability.

## Next Steps
- Refine `Vertex` and `Edge` classes with `toString()` methods for clearer debug output.
- Ensure full GUI synchronization between backend state and canvas rendering.
- Continue testing edge cases for undo/redo (multiple vertices/edges, directed vs. undirected).
