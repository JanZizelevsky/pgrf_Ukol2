package model;

public class Rectangle extends Shape {
    private Point baseStart;
    private Point baseEnd;
    private Point heightPoint;

    public Rectangle(Point baseStart, Point baseEnd, Point heightPoint) {
        this.baseStart = baseStart;
        this.baseEnd = baseEnd;
        this.heightPoint = heightPoint;
    }

    public Point getBaseStart() {
        return baseStart;
    }

    public Point getBaseEnd() {
        return baseEnd;
    }

    public Point getHeightPoint() {
        return heightPoint;
    }


    public Point[] getCorners() {
        // Vektor základny
        int baseDx = baseEnd.getX() - baseStart.getX();
        int baseDy = baseEnd.getY() - baseStart.getY();

        // Vektor od baseStart k heightPoint
        int heightDx = heightPoint.getX() - baseStart.getX();
        int heightDy = heightPoint.getY() - baseStart.getY();

        // Projekce heightPoint na základnu
        double baseLengthSq = baseDx * baseDx + baseDy * baseDy;
        if (baseLengthSq == 0) {
            // Základna má nulovou délku
            return new Point[]{baseStart, baseEnd, heightPoint, heightPoint};
        }

        double t = (heightDx * baseDx + heightDy * baseDy) / baseLengthSq;
        int projX = (int) Math.round(baseStart.getX() + t * baseDx);
        int projY = (int) Math.round(baseStart.getY() + t * baseDy);

        // Vektor výšky (kolmý na základnu) - od projekce heightPoint na základnu k heightPoint
        int heightVecX = heightPoint.getX() - projX;
        int heightVecY = heightPoint.getY() - projY;

        // Čtyři rohy obdélníka
        Point corner1 = baseStart;
        Point corner2 = baseEnd;
        Point corner3 = new Point(baseEnd.getX() + heightVecX, baseEnd.getY() + heightVecY);
        Point corner4 = new Point(baseStart.getX() + heightVecX, baseStart.getY() + heightVecY);

        return new Point[]{corner1, corner2, corner3, corner4};
    }
}

