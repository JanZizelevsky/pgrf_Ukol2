package rasterizace;

public class LineSnap {

    public static int[] snapTo45Degrees(int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;

        double angle = Math.atan2(dy, dx); // úhel v radiánech
        double snappedAngle = Math.round(angle / (Math.PI / 4)) * (Math.PI / 4); // nejbližší násobek 45°

        double length = Math.hypot(dx, dy); // délka původní úsečky

        int snappedX = (int) Math.round(x1 + Math.cos(snappedAngle) * length);
        int snappedY = (int) Math.round(y1 + Math.sin(snappedAngle) * length);

        return new int[]{snappedX, snappedY};
    }
}

