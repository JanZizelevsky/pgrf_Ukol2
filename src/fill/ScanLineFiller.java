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
        
        if (polygon.getSize() < 3) {
            System.out.println("Polygon musí mít alespoň 3 vrcholy.");
            return;
        }

        ArrayList<Edge> edges = new ArrayList<>();
        for (int i = 0; i < polygon.getSize(); i++) {
            int indexA = i;
            int indexB = i + 1;

            if (indexB == polygon.getSize())
                indexB = 0;

            Point a = polygon.getPoint(indexA);
            Point b = polygon.getPoint(indexB);

            Edge edge;
            if (a.getY() <= b.getY()) {
                edge = new Edge(a, b);
            } else {
                edge = new Edge(b, a);
            }
            
            if (!edge.isHorizontal()) {
                edges.add(edge);
            }
        }

        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;

        for (int i = 0; i < polygon.getSize(); i++) {
            Point p = polygon.getPoint(i);
            yMin = Math.min(yMin, p.getY());
            yMax = Math.max(yMax, p.getY());
        }

        for (int y = yMin; y <= yMax; y++) {

            ArrayList<Integer> intersections = new ArrayList<>();


            for(Edge edge : edges) {

                if(!edge.isIntersection(y))
                    continue;

                int x = edge.getIntersection(y);

                intersections.add(x);
            }


            Collections.sort(intersections);


            for (int i = 0; i + 1 < intersections.size(); i += 2) {
                int xStart = intersections.get(i);
                int xEnd = intersections.get(i + 1);
                lineRasterizer.rasterize(xStart, y, xEnd, y);
            }
        }

        polygonRasterizer.rasterize(polygon);
    }
}
