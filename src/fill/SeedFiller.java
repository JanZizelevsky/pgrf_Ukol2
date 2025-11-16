package fill;

import raster.Raster;

import java.util.OptionalInt;

public class SeedFiller implements Filler {
    private Raster raster;
    private int fillColor;
    private int backgroundColor;
    private int startX, startY;

    public SeedFiller(Raster raster, int fillColor, int startX, int startY) {
        this.raster = raster;
        this.fillColor = fillColor;
        this.startX = startX;
        this.startY = startY;

        OptionalInt pixelColor = raster.getPixel(startX, startY);
        if(pixelColor.isPresent())
            this.backgroundColor = pixelColor.getAsInt();
    }

    @Override
    public void fill() {
        seedFill(startX, startY);
    }

    private void seedFill(int x, int y) {
        // bounds check
        if (x < 0 || y < 0 || x >= raster.getWidth() || y >= raster.getHeight())
            return;

        // načtu barvu pixelu, na který jsem klikl (startx, starty)
        OptionalInt pixelColor = raster.getPixel(x, y);
        if(pixelColor.isEmpty())
            return;

        // podmínka, jestli mám nebo nemám obarvit
        // pokud ne - končím
        int current = pixelColor.getAsInt();
        if (current != backgroundColor || current == fillColor)
            return;

        // obarvím pixel
        raster.setPixel(x, y, fillColor);

        // zavolám seedFill, pro svoje sousedy
        seedFill(x + 1, y);
        seedFill(x - 1, y);
        seedFill(x, y + 1);
        seedFill(x, y - 1);
    }
}

