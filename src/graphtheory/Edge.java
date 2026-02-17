/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtheory;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author mk
 */
public class Edge {

    public Vertex vertex1;
    public Vertex vertex2;
    public boolean wasFocused;
    public boolean wasClicked;
    public boolean directed; // for directed graphs
    public int weight; //default weight for edges
    public boolean wasHovered; // new flag for hover higlight in weights

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
        if (wasClicked) {
            g.setColor(Color.red);
        } else if (wasFocused) {
            g.setColor(Color.blue);
        } else {
            g.setColor(Color.black);
        }

        int x1 = vertex1.location.x;
        int y1 = vertex1.location.y;
        int x2 = vertex2.location.x;
        int y2 = vertex2.location.y;

        double dx = x2 - x1;
        double dy = y2 - y1;
        double theta = Math.atan2(dy, dx);

        /* Shorten line so it doesn't pierce the node */
        int nodeRadius = 20; // taken from Vertex.java
        int buffer = 5; //smallgap so arrowhead doesn't touch node
        
        // Trim start point
        int startX = (int)(x1 + (nodeRadius + buffer) * Math.cos(theta));
        int startY = (int)(y1 + (nodeRadius + buffer) * Math.sin(theta));


        // Arrowhead tip sits just outside the node
        int arrowTipX  = (int)(x2 - (nodeRadius + buffer) * Math.cos(theta));
        int arrowTipY  = (int)(y2 - (nodeRadius + buffer) * Math.sin(theta));

        // Midpoint of the edge for label placement
        int midX = (startX + arrowTipX) / 2;
        int midY = (startY + arrowTipY) / 2;

        // If bidirectional, offset labels so they don't overlap
        if (directed) {
            // Shift weight label perpendicular to the edge direction
            double perpTheta = theta + Math.PI / 2;
            int offset = 10; // distance to offset the label
            midX += (int)(offset * Math.cos(perpTheta));
            midY += (int)(offset * Math.sin(perpTheta));
        }

        //Draw weight as text
        g.setColor(Color.BLACK);

        int lineTrim = 10; // trim distance so line doesn't poke the node
        int lineEndX = (int)(arrowTipX - lineTrim * Math.cos(theta));
        int lineEndY = (int)(arrowTipY - lineTrim * Math.sin(theta));

        g.drawLine(startX, startY, lineEndX, lineEndY);

        if (directed) {
            drawArrowHead(g, lineEndX, lineEndY, arrowTipX, arrowTipY);
        }

        if (wasHovered) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.BLACK);
        }
        g.drawString(String.valueOf(weight), midX, midY);
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
    /* Debugger */
    System.out.println("Arrow from ("+x1+","+y1+") to ("+xTip+","+yTip+")");
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
