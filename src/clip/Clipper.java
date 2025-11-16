package clip;

import model.Point;

import java.util.ArrayList;
import java.util.List;

public class Clipper {
    /**
     * Ořízne polygon pomocí Sutherland-Hodgman algoritmu.
     * Funguje i pro nekonvexní a sebeprotínající se polygony.
     * 
     * @param clipperPoints body ořezávacího polygonu (musí být konvexní)
     * @param pointsToClip body polygonu k oříznutí
     * @return seznam bodů oříznutého polygonu
     */
    public List<Point> clip(List<Point> clipperPoints, List<Point> pointsToClip) {
        if (clipperPoints.size() < 3 || pointsToClip.size() < 3) {
            return new ArrayList<>();
        }

        // Zajistíme správnou orientaci ořezávacího polygonu (proti směru hodinových ručiček)
        List<Point> orientedClipper = ensureCounterClockwise(clipperPoints);
        
        List<Point> output = new ArrayList<>(pointsToClip);

        // Pro každou hranu ořezávacího polygonu
        for (int i = 0; i < orientedClipper.size(); i++) {
            Point clipStart = orientedClipper.get(i);
            Point clipEnd = orientedClipper.get((i + 1) % orientedClipper.size());

            if (output.isEmpty()) {
                break;
            }

            List<Point> input = new ArrayList<>(output);
            output.clear();

            if (input.isEmpty()) {
                break;
            }

            Point s = input.get(input.size() - 1);

            // Pro každou hranu polygonu k oříznutí
            for (Point e : input) {
                if (isInside(e, clipStart, clipEnd)) {
                    if (!isInside(s, clipStart, clipEnd)) {
                        // Přidáme průsečík
                        output.add(intersect(s, e, clipStart, clipEnd));
                    }
                    output.add(e);
                } else if (isInside(s, clipStart, clipEnd)) {
                    // Přidáme průsečík
                    output.add(intersect(s, e, clipStart, clipEnd));
                }
                s = e;
            }
        }

        return output;
    }

    /**
     * Zjistí, zda je bod uvnitř hrany (na levé straně při procházení hranou proti směru hodinových ručiček).
     * Pro konvexní polygon: pokud jsou všechny body uvnitř všech hran, je bod uvnitř polygonu.
     */
    private boolean isInside(Point p, Point clipStart, Point clipEnd) {
        // Vektor hrany (od clipStart k clipEnd)
        int dx = clipEnd.getX() - clipStart.getX();
        int dy = clipEnd.getY() - clipStart.getY();
        
        // Vektor od začátku hrany k bodu
        int px = p.getX() - clipStart.getX();
        int py = p.getY() - clipStart.getY();
        
        // Křížový součin: dx * py - dy * px
        // Pokud je kladný, bod je vlevo od hrany (při procházení proti směru hodinových ručiček)
        // Pokud je záporný, bod je vpravo od hrany
        // Pro konvexní polygon orientovaný proti směru hodinových ručiček: >= 0 znamená uvnitř
        int crossProduct = dx * py - dy * px;
        return crossProduct >= 0;
    }

    /**
     * Spočítá průsečík dvou úseček.
     */
    private Point intersect(Point s, Point e, Point clipStart, Point clipEnd) {
        // Parametrické rovnice:
        // P = s + t * (e - s)
        // Q = clipStart + u * (clipEnd - clipStart)
        
        int x1 = s.getX();
        int y1 = s.getY();
        int x2 = e.getX();
        int y2 = e.getY();
        int x3 = clipStart.getX();
        int y3 = clipStart.getY();
        int x4 = clipEnd.getX();
        int y4 = clipEnd.getY();

        int denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        
        if (denom == 0) {
            // Rovnoběžné úsečky, vrátíme střed
            return new Point((x1 + x2) / 2, (y1 + y2) / 2);
        }

        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / (double) denom;

        int x = (int) Math.round(x1 + t * (x2 - x1));
        int y = (int) Math.round(y1 + t * (y2 - y1));

        return new Point(x, y);
    }

    /**
     * Zajistí, aby polygon byl orientovaný proti směru hodinových ručiček.
     * Pokud není, otočí pořadí bodů.
     */
    private List<Point> ensureCounterClockwise(List<Point> points) {
        if (points.size() < 3) {
            return points;
        }

        // Spočítáme plochu polygonu pomocí shoelace formula
        // Pokud je plocha záporná, polygon je orientovaný po směru hodinových ručiček
        long area = 0;
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % points.size());
            area += (long) p1.getX() * p2.getY() - (long) p2.getX() * p1.getY();
        }

        // Pokud je plocha záporná, otočíme pořadí bodů
        if (area < 0) {
            List<Point> reversed = new ArrayList<>();
            for (int i = points.size() - 1; i >= 0; i--) {
                reversed.add(points.get(i));
            }
            return reversed;
        }

        return points;
    }
}
