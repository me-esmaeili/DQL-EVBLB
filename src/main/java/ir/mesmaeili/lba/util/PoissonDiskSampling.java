package ir.mesmaeili.lba.util;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PoissonDiskSampling {
    private static final int K = 30; // Number of attempts before rejection

    public static List<Coordinate> generatePoints(int numberOfPoints,
                                                  double minDistance,
                                                  int width, int height) {
        List<Coordinate> coordinates = new ArrayList<>();
        List<Coordinate> activeList = new ArrayList<>();
        Random random = new Random();
        // Initial point
        Coordinate initialPoint = new Coordinate(random.nextDouble() * width, random.nextDouble() * height);
        coordinates.add(initialPoint);
        activeList.add(initialPoint);

        while (coordinates.size() < numberOfPoints) {
            if (activeList.isEmpty()) {
                Coordinate newPoint = new Coordinate(random.nextDouble() * width, random.nextDouble() * height);
                if (isValid(newPoint, minDistance, width, height, coordinates)) {
                    coordinates.add(newPoint);
                    activeList.add(newPoint);
                }
                continue;
            }

            int index = random.nextInt(activeList.size());
            Coordinate point = activeList.get(index);
            boolean found = false;

            for (int i = 0; i < K; i++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                double radius = minDistance + random.nextDouble() * minDistance;
                double newX = point.getX() + radius * Math.cos(angle);
                double newY = point.getY() + radius * Math.sin(angle);
                Coordinate newPoint = new Coordinate(newX, newY);

                if (isValid(newPoint, minDistance, width, height, coordinates)) {
                    coordinates.add(newPoint);
                    activeList.add(newPoint);
                    found = true;
                    break;
                }
            }

            if (!found) {
                activeList.remove(index);
            }
        }
        return coordinates;
    }

    private static boolean isValid(Coordinate point,
                                   double minDistance,
                                   int width,
                                   int height,
                                   List<Coordinate> coordinates) {
        if (point.getX() < 0 || point.getX() >= width || point.getY() < 0 || point.getY() >= height) {
            return false;
        }
        for (Coordinate coord : coordinates) {
            if (point.distance(coord) < minDistance) {
                return false;
            }
        }
        return true;
    }
}