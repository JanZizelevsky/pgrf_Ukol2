package model;

import java.util.ArrayList;

public class Polygon extends Shape {

    private final ArrayList<Point> points;

    public Polygon() {
        this.points = new ArrayList<>();
    }

    public void addPoint(Point p) {
        this.points.add(p);
    }

    public Point getPoint(int i) {
        return points.get(i);
    }

    public int getSize() {
        return points.size();
    }

    public void clearPoints() {
        points.clear();
    }

    public ArrayList<Point> getPoints() {
        return new ArrayList<>(points);
    }
}
