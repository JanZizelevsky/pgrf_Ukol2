package rasterizace;

import raster.RasterBufferedImage;

import java.awt.*;

public class LineRasterizerColorTransition extends LineRasterizer {

    public LineRasterizerColorTransition(RasterBufferedImage raster) {
        super(raster);
    }

    // vykreslení úsečky pomocí DDA algoritmu

    @Override
    public void rasterize(int x1, int y1, int x2, int y2) {
        Color c1 = Color.RED;
        Color c2 = Color.BLUE;

        float[] colorComponentsC1 = c1.getColorComponents(null);
        float[] colorComponentsC2 = c2.getColorComponents(null);

        int dx = x2 - x1;
        int dy = y2 - y1;

        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        // vykresli 1 pixel při nulové délce
        if (steps == 0) {
            interpolarization(x1, colorComponentsC1, colorComponentsC2, y1, 0f);
            return;
        }

        // vypočet změny x a y při každém kroku
        float xInc = dx / (float) steps;
        float yInc = dy / (float) steps;

        // cyklus vykreslení
        float x = x1;
        float y = y1;

        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            interpolarization(Math.round(x), colorComponentsC1, colorComponentsC2, Math.round(y), t);
            x += xInc;
            y += yInc;
        }
    }


    private void interpolarization(int x1, float[] colorComponentsC1, float[] colorComponentsC2, int y, float t) {
        int r = Math.round(((1 - t) * colorComponentsC1[0] + t * colorComponentsC2[0]) * 255);
        int g = Math.round(((1 - t) * colorComponentsC1[1] + t * colorComponentsC2[1]) * 255);
        int b = Math.round(((1 - t) * colorComponentsC1[2] + t * colorComponentsC2[2]) * 255);

        int rgb = (r << 16) | (g << 8) | b;

        raster.setPixel(x1, y, rgb);
    }
}


