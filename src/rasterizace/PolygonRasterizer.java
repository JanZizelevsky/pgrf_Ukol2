package rasterizace;

import model.Line;
import model.Polygon;

public class PolygonRasterizer {
    private LineRasterizer lineRasterizer;
    public PolygonRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void setLineRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void rasterize(Polygon polygon) {

        if (polygon.getSize() < 3) {
            return;
        }

        for (int i = 0; i < polygon.getSize(); i++) {
            int indexA = i;
            int indexB = (i + 1) % polygon.getSize();

            Line line = new Line(polygon.getPoint(indexA), polygon.getPoint(indexB));
            lineRasterizer.rasterize(line);
        }


    }

}
