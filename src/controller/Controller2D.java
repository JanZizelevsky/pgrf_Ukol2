package controller;


import clip.Clipper;
import fill.ScanLineFiller;
import fill.SeedFiller;
import model.Line;
import model.Point;
import model.Polygon;
import model.Rectangle;
import rasterizace.*;
import view.Panel;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Controller2D {
    private final Panel panel;

    private static final int MODE_LINE = 0;
    private static final int MODE_POLYGON = 1;
    private static final int MODE_FILL = 2;
    private static final int MODE_SCANLINE_FILL = 3;
    private static final int MODE_CLIPPER = 4;
    private static final int MODE_RECTANGLE = 5;
    private int currentMode = MODE_LINE;

    private ArrayList<Line> usecky = new ArrayList<>();
    private ArrayList<Rectangle> rectangles = new ArrayList<>();
    private int startX, startY;
    private boolean isLineStartSet;
    private boolean shiftPressed = false;

    // Pro kreslení obdélníka
    private Point rectangleBaseStart;
    private Point rectangleBaseEnd;
    private int rectangleStep = 0; // 0 = čeká na začátek základny, 1 = čeká na konec základny, 2 = čeká na výšku

    private Polygon polygon = new Polygon();
    private Polygon clipperPolygon = new Polygon();
    private Clipper clipper = new Clipper();
    private LineRasterizer lineRasterizer;
    private PolygonRasterizer polygonRasterizer;
    private LineRasterizer lineRasterizerPlain;
    private LineRasterizer lineRasterizerFill;
    private LineRasterizer lineRasterizerClipper;

    private static final int SEED_FILL_COLOR = 0x00ff00;
    private static final int SCANLINE_FILL_COLOR = 0x0000ff;
    private static final int CLIPPER_COLOR = 0xffff00; // Žlutá pro ořezávací polygon


    public Controller2D(Panel panel) {
        this.panel = panel;
        lineRasterizer = new LineRasterizerColorTransition(panel.getRaster());
        lineRasterizerPlain = new LineRasterizerBasic(panel.getRaster(), 0xffffff);
        lineRasterizerFill = new LineRasterizerBasic(panel.getRaster(), SCANLINE_FILL_COLOR);
        lineRasterizerClipper = new LineRasterizerBasic(panel.getRaster(), CLIPPER_COLOR);
        polygonRasterizer = new PolygonRasterizer(lineRasterizerPlain);
        initListeners();
    }

    private void initListeners() {

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentMode == MODE_LINE) {
                    polygon.clearPoints();
                    startX = e.getX();
                    startY = e.getY();
                    isLineStartSet = true;
                } else if (currentMode == MODE_POLYGON) {
                    Point p = new Point(e.getX(), e.getY());
                    polygon.addPoint(p);
                    drawScene();
                } else if (currentMode == MODE_CLIPPER) {
                    Point p = new Point(e.getX(), e.getY());
                    clipperPolygon.addPoint(p);
                    drawScene();
                } else if (currentMode == MODE_RECTANGLE) {
                    handleRectangleClick(e.getX(), e.getY());
                } else if (currentMode == MODE_FILL) {
                    seedFill(e.getX(), e.getY());
                } else if (currentMode == MODE_SCANLINE_FILL) {
                    scanlineFill();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentMode == MODE_LINE && isLineStartSet) {
                    int endX = e.getX();
                    int endY = e.getY();

                    if (shiftPressed) {
                        int[] snapped = LineSnap.snapTo45Degrees(startX, startY, endX, endY);
                        endX = snapped[0];
                        endY = snapped[1];
                    }

                    Line line = new Line(startX, startY, endX, endY);
                    usecky.add(line);
                    isLineStartSet = false;
                    drawScene();
                }
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentMode == MODE_LINE && isLineStartSet) {
                    int endX = e.getX();
                    int endY = e.getY();

                    if (shiftPressed) {
                        int[] snapped = LineSnap.snapTo45Degrees(startX, startY, endX, endY);
                        endX = snapped[0];
                        endY = snapped[1];
                    }

                    drawScene();
                    lineRasterizer.rasterize(startX, startY, endX, endY);
                    panel.repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentMode == MODE_RECTANGLE && rectangleStep > 0) {
                    // Aktualizujeme náhled obdélníka
                    if (rectangleStep == 1 && rectangleBaseStart != null) {
                        rectangleBaseEnd = new Point(e.getX(), e.getY());
                        drawScene();
                        panel.repaint();
                    } else if (rectangleStep == 2 && rectangleBaseStart != null && rectangleBaseEnd != null) {
                        // Zobrazíme náhled obdélníka s aktuální pozicí myši jako třetím bodem
                        Point previewPoint = new Point(e.getX(), e.getY());
                        Rectangle previewRect = new Rectangle(rectangleBaseStart, rectangleBaseEnd, previewPoint);
                        drawScene();
                        drawRectangle(previewRect);
                        panel.repaint();
                    }
                }
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    panel.getRaster().clear();
                    panel.repaint();
                    polygon.clearPoints();
                    clipperPolygon.clearPoints();
                    usecky.clear();
                    rectangles.clear();
                    rectangleStep = 0;
                }
                if (e.getKeyCode() == KeyEvent.VK_L) {
                    currentMode = MODE_LINE;
                    System.out.println("Přepnuto na kreslení úsečky");
                    polygon.clearPoints();
                }
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    currentMode = MODE_POLYGON;
                    System.out.println("Přepnuto na kreslení polygonu");
                    usecky.clear();
                    drawScene();
                }
                if (e.getKeyCode() == KeyEvent.VK_F) {
                    currentMode = MODE_FILL;
                    System.out.println("Přepnuto na vyplňování seed fill");
                }
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    currentMode = MODE_SCANLINE_FILL;
                    System.out.println("Přepnuto na vyplňování scanline fill");
                }
                if (e.getKeyCode() == KeyEvent.VK_K) {
                    currentMode = MODE_CLIPPER;
                    System.out.println("Přepnuto na kreslení ořezávacího polygonu");
                    rectangleStep = 0;
                    drawScene();
                }
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    currentMode = MODE_RECTANGLE;
                    System.out.println("Přepnuto na kreslení obdélníku");
                    rectangleStep = 0;
                    drawScene();
                }
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftPressed = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftPressed = false;
                }
            }
        });
    }

    private void drawScene() {
        panel.getRaster().clear();
        
        // Vykreslíme úsečky
        for (Line line : usecky) {
            lineRasterizer.rasterize(line.getX1(), line.getY1(), line.getX2(), line.getY2());
        }
        
        // Vykreslíme obdélníky
        for (Rectangle rect : rectangles) {
            drawRectangle(rect);
        }
        
        // Vykreslíme základnu obdélníka, který se právě kreslí
        if (currentMode == MODE_RECTANGLE && rectangleStep == 1 && rectangleBaseStart != null && rectangleBaseEnd != null) {
            lineRasterizerPlain.rasterize(rectangleBaseStart.getX(), rectangleBaseStart.getY(), 
                                         rectangleBaseEnd.getX(), rectangleBaseEnd.getY());
        }
        
        // Vykreslíme ořezávací polygon (žlutě)
        if (clipperPolygon.getSize() >= 3) {
            PolygonRasterizer clipperRasterizer = new PolygonRasterizer(lineRasterizerClipper);
            clipperRasterizer.rasterize(clipperPolygon);
        }
        
        // Ořízneme a vykreslíme hlavní polygon
        if (polygon.getSize() >= 3) {
            if (clipperPolygon.getSize() >= 3) {
                // Ořízneme polygon
                List<Point> clippedPoints = clipper.clip(clipperPolygon.getPoints(), polygon.getPoints());
                
                if (!clippedPoints.isEmpty() && clippedPoints.size() >= 3) {
                    // Vytvoříme oříznutý polygon a vykreslíme ho
                    Polygon clippedPolygon = new Polygon();
                    for (Point p : clippedPoints) {
                        clippedPolygon.addPoint(p);
                    }
                    polygonRasterizer.rasterize(clippedPolygon);
                } else {
                    // Ořezání vrátilo prázdný seznam nebo málo bodů
                    // Zobrazíme původní polygon šedě, aby bylo vidět, že existuje, ale není oříznutý
                    LineRasterizer debugRasterizer = new LineRasterizerBasic(panel.getRaster(), 0x808080);
                    PolygonRasterizer debugPolygonRasterizer = new PolygonRasterizer(debugRasterizer);
                    debugPolygonRasterizer.rasterize(polygon);
                }
            } else {
                // Bez ořezání - vykreslíme normálně
                polygonRasterizer.rasterize(polygon);
            }
        }

        panel.repaint();
    }

    private void seedFill(int x, int y) {
        if (panel.getRaster() == null) {
            return;
        }

        if (x < 0 || y < 0 || x >= panel.getRaster().getWidth() || y >= panel.getRaster().getHeight()) {
            return;
        }

        SeedFiller filler = new SeedFiller(panel.getRaster(), SEED_FILL_COLOR, x, y);
        filler.fill();
        panel.repaint();
    }

    private void scanlineFill() {
        if (polygon.getSize() < 3) {
            System.out.println("Polygon musí mít alespoň 3 vrcholy pro scanline fill.");
            return;
        }

        ScanLineFiller filler = new ScanLineFiller(lineRasterizerFill, polygonRasterizer, polygon);
        filler.fill();
        panel.repaint();
    }

    private void handleRectangleClick(int x, int y) {
        Point p = new Point(x, y);
        
        if (rectangleStep == 0) {
            // První klik - začátek základny
            rectangleBaseStart = p;
            rectangleStep = 1;
        } else if (rectangleStep == 1) {
            // Druhý klik - konec základny
            rectangleBaseEnd = p;
            rectangleStep = 2;
            drawScene();
        } else if (rectangleStep == 2) {
            // Třetí klik - bod určující výšku
            Rectangle rect = new Rectangle(rectangleBaseStart, rectangleBaseEnd, p);
            rectangles.add(rect);
            rectangleStep = 0;
            rectangleBaseStart = null;
            rectangleBaseEnd = null;
            drawScene();
        }
    }

    private void drawRectangle(Rectangle rect) {
        Point[] corners = rect.getCorners();
        if (corners.length == 4) {
            // Vykreslíme čtyři hrany obdélníka
            lineRasterizerPlain.rasterize(corners[0].getX(), corners[0].getY(), 
                                         corners[1].getX(), corners[1].getY());
            lineRasterizerPlain.rasterize(corners[1].getX(), corners[1].getY(), 
                                         corners[2].getX(), corners[2].getY());
            lineRasterizerPlain.rasterize(corners[2].getX(), corners[2].getY(), 
                                         corners[3].getX(), corners[3].getY());
            lineRasterizerPlain.rasterize(corners[3].getX(), corners[3].getY(), 
                                         corners[0].getX(), corners[0].getY());
        }
    }
}


