package fill;

import model.Edge;
import model.Point;
import model.Polygon;
import rasterizace.LineRasterizer;
import rasterizace.PolygonRasterizer;

import java.util.ArrayList;
import java.util.Collections;


public class ScanLineFiller implements Filler {
    private LineRasterizer lineRasterizer;
    private PolygonRasterizer polygonRasterizer;
    private Polygon polygon;

    public ScanLineFiller(LineRasterizer lineRasterizer, PolygonRasterizer polygonRasterizer, Polygon polygon) {
        this.lineRasterizer = lineRasterizer;
        this.polygonRasterizer = polygonRasterizer;
        this.polygon = polygon;
    }

    @Override
    public void fill() {
        // Nechci vyplnit polygon, který má méně, jak 3 vrcholy
        if (polygon.getSize() < 3) {
            System.out.println("Polygon musí mít alespoň 3 vrcholy.");
            return;
        }

        // Potřebujeme vytvořit seznam hran
        ArrayList<Edge> edges = new ArrayList<>();
        for (int i = 0; i < polygon.getSize(); i++) {
            int indexA = i;
            int indexB = i + 1;

            if (indexB == polygon.getSize())
                indexB = 0;

            Point a = polygon.getPoint(indexA);
            Point b = polygon.getPoint(indexB);

            // Create edge with proper orientation (y1 <= y2) since Edge fields are final
            Edge edge;
            if (a.getY() <= b.getY()) {
                edge = new Edge(a, b);
            } else {
                edge = new Edge(b, a);
            }
            
            // Nechceme přidat horizontální hrany
            if (!edge.isHorizontal()) {
                edges.add(edge);
            }
        }

        // Najít yMin a yMax
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;
        // Projít všechny pointy polygonu a najít min a max
        for (int i = 0; i < polygon.getSize(); i++) {
            Point p = polygon.getPoint(i);
            yMin = Math.min(yMin, p.getY());
            yMax = Math.max(yMax, p.getY());
        }

        for (int y = yMin; y <= yMax; y++) {
            // vytvořím seznam průsečíků
            ArrayList<Integer> intersections = new ArrayList<>();

            // Prokaždou hranu:
            for(Edge edge : edges) {
                // zeptám se, jestli existuje průsečík
                if(!edge.isIntersection(y))
                    continue;
                // pokud ano, tak ho spočítám
                int x = edge.getIntersection(y);
                // uložím do seznamu průsečíků
                intersections.add(x);
            }

            // Seřadit průsečíky od min po max
            Collections.sort(intersections);

            // Spojím (obarvím) průsečíky, 0 - 1, 2 - 3, 4 - 5, 6 - 7
            for (int i = 0; i + 1 < intersections.size(); i += 2) {
                int xStart = intersections.get(i);
                int xEnd = intersections.get(i + 1);
                lineRasterizer.rasterize(xStart, y, xEnd, y);
            }
        }

        // Vykreslím hranici polygonu
        polygonRasterizer.rasterize(polygon);
    }
}
