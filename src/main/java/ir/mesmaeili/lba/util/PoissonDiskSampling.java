package ir.mesmaeili.lba.util;

import ir.mesmaeili.lba.model.Point;
import org.joml.sampling.PoissonSampling;

import java.util.ArrayList;
import java.util.List;

public class PoissonDiskSampling {
    private static final int K = 30; // Number of attempts before rejection

    public static List<Point> generatePoints(float minDist, int width, int height) {
        List<Point> points = new ArrayList<>();
        PoissonSampling.Disk sampler = new PoissonSampling.Disk(3423423452345234L, 50, minDist, K, (x, y) -> {
            if (x <= width && y <= height) {
                points.add(new Point(x, y));
            }
        });

        return points;
    }
}