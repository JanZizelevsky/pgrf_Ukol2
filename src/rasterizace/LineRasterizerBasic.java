package rasterizace;

import raster.RasterBufferedImage;

public class LineRasterizerBasic extends LineRasterizer {

    private final int color;

    public LineRasterizerBasic(RasterBufferedImage raster, int color) {
        super(raster);
        this.color = color;
    }

    @Override
    public void rasterize(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;

        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        if (steps == 0) {
            raster.setPixel(x1, y1, color);
            return;
        }

        float xInc = dx / (float) steps;
        float yInc = dy / (float) steps;

        float x = x1;
        float y = y1;

        for (int i = 0; i <= steps; i++) {
            raster.setPixel(Math.round(x), Math.round(y), color);
            x += xInc;
            y += yInc;
        }
    }
}

