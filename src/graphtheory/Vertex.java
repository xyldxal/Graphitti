/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtheory;

import java.awt.Color;
import java.awt.Point;
import java.util.Vector;
import java.awt.Graphics;

/**
 *
 * @author mk
 */
public class Vertex implements Comparable {

    public String name;
    public Point location;
    public boolean wasFocused;
    public boolean wasClicked;
    public int nodeSize = 40;               // outer diameter
    public Color outerColor = Color.BLACK;  // ring / border color
    public Color innerColor = Color.WHITE;  // inner fill color
    public Vector<Vertex> connectedVertices;

    public Vertex(String name, int x, int y) {
        this.name = name;
        location = new Point(x, y);
        connectedVertices = new Vector<Vertex>();
    }

    public void addVertex(Vertex v) {
        connectedVertices.add(v);
    }

    public boolean hasIntersection(int x, int y) {
        double distance = Math.sqrt(Math.pow((x - location.x), 2) + Math.pow((y - location.y), 2));
        return distance <= nodeSize / 2.0;
    }

    public boolean connectedToVertex(Vertex v) {
        if (connectedVertices.contains(v)) {
            return true;
        }
        return false;
    }

    public int getDegree() {
        return connectedVertices.size();
    }

    public int compareTo(Object v) {
        if (((Vertex) v).getDegree() > getDegree()) {
            return 1;
        } else if (((Vertex) v).getDegree() < getDegree()) {
            return -1;
        } else {
            return 0;
        }
    }

    public void draw(Graphics g) {
        // Outer ring: red = clicked, blue = hovered, else custom color
        if (wasClicked) {
            g.setColor(Color.RED);
        } else if (wasFocused) {
            g.setColor(Color.BLUE);
        } else {
            g.setColor(outerColor);
        }
        g.fillOval(location.x - nodeSize / 2, location.y - nodeSize / 2, nodeSize, nodeSize);

        // Inner fill — proportionally 75% of outer
        int innerSize = (int)(nodeSize * 0.75);
        g.setColor(innerColor);
        g.fillOval(location.x - innerSize / 2, location.y - innerSize / 2, innerSize, innerSize);

        // Label centered on node
        g.setColor(Color.BLACK);
        int strW = g.getFontMetrics().stringWidth(name);
        g.drawString(name, location.x - strW / 2, location.y + 4);
    }
}
