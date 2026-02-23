/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtheory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 *
 * @author mk
 */
public class Edge {

    public Vertex vertex1;
    public Vertex vertex2;
    public boolean wasFocused;
    public boolean wasClicked;
    public boolean directed;      // for directed graphs
    public int weight;            // default weight for edges
    public boolean wasHovered;    // hover highlight for weight labels
    public Color lineColor   = Color.BLACK; // edge line color
    public float strokeWidth = 2.0f;        // line thickness in pixels

    public Edge(Vertex v1, Vertex v2) {
        this(v1, v2, false, 1); // default undirected, weight = 1
    }

    public Edge(Vertex v1, Vertex v2, boolean directed) {
        this(v1, v2, directed, 1); // default weight = 1
    }

    /*Look out for this as this code block may cause issues*/
    public Edge(Vertex v1, Vertex v2, boolean directed, int weight) {
        vertex1 = v1;
        vertex2 = v2;
        this.directed = directed;
        this.weight = weight;
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Stroke origStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (wasClicked) {
            g2.setColor(Color.RED);
        } else if (wasFocused) {
            g2.setColor(Color.BLUE);
        } else {
            g2.setColor(lineColor);
        }

        int x1 = vertex1.location.x;
        int y1 = vertex1.location.y;
        int x2 = vertex2.location.x;
        int y2 = vertex2.location.y;

        double dx = x2 - x1;
        double dy = y2 - y1;
        double theta = Math.atan2(dy, dx);

        // Trim endpoints so the line doesn't pierce node circles
        int nodeRadius = vertex1.nodeSize / 2;
        int buffer = 5;

        int startX    = (int)(x1 + (nodeRadius + buffer) * Math.cos(theta));
        int startY    = (int)(y1 + (nodeRadius + buffer) * Math.sin(theta));
        int arrowTipX = (int)(x2 - (nodeRadius + buffer) * Math.cos(theta));
        int arrowTipY = (int)(y2 - (nodeRadius + buffer) * Math.sin(theta));

        // Weight label position (midpoint, offset perpendicularly for directed edges)
        int midX = (startX + arrowTipX) / 2;
        int midY = (startY + arrowTipY) / 2;
        if (directed) {
            double perpTheta = theta + Math.PI / 2;
            midX += (int)(10 * Math.cos(perpTheta));
            midY += (int)(10 * Math.sin(perpTheta));
        }

        int lineTrim = 10;
        int lineEndX = (int)(arrowTipX - lineTrim * Math.cos(theta));
        int lineEndY = (int)(arrowTipY - lineTrim * Math.sin(theta));

        g2.drawLine(startX, startY, lineEndX, lineEndY);

        if (directed) {
            drawArrowHead(g2, lineEndX, lineEndY, arrowTipX, arrowTipY);
        }

        // Weight label
        if (wasHovered) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(Color.BLACK);
        }
        g2.drawString(String.valueOf(weight), midX, midY);

        // Restore original stroke
        g2.setStroke(origStroke);
    }

    private void drawArrowHead(Graphics g, int x1, int y1, int xTip, int yTip) {
    double phi = Math.toRadians(40); // arrow angle
    int barb = 30; // arrow size

    double dy = yTip - y1;
    double dx = xTip - x1;
    double theta = Math.atan2(yTip - y1, xTip - x1);

    double rho = theta + phi;
    for (int j = 0; j < 2; j++) {
        int x = (int)(xTip - barb * Math.cos(rho));
        int y = (int)(yTip - barb * Math.sin(rho));
        g.drawLine(xTip, yTip, x, y);
        rho = theta - phi;
    }

}

    // Changed entirely for hover weights
    public boolean hasIntersection(int x, int y) {
        // Midpoint between two vertices
        int midX = (vertex1.location.x + vertex2.location.x) / 2;
        int midY = (vertex1.location.y + vertex2.location.y) / 2;

        // Tolerance area around midpoint
        int tolerance = 20;
        return Math.abs(x - midX) <= tolerance && Math.abs(y - midY) <= tolerance;
    }
}
