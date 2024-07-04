package ir.mesmaeili.drl.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VoronoiDiagram {
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private Geometry voronoiDiagram;

    public List<Coordinate> generatePoints(int count, int spaceWith, int spaceHeight) {
        return generateRandomCoordinates(10, spaceWith, spaceHeight);
    }

    public void generateDiagram(List<Coordinate> coordinates) {
        VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
        builder.setSites(coordinates);
        this.voronoiDiagram = builder.getDiagram(geometryFactory);
    }

    public Integer getRegion(double x, double y) {
        Point point = geometryFactory.createPoint(new Coordinate(x, y));
        for (int i = 0; i < voronoiDiagram.getNumGeometries(); i++) {
            Geometry cell = voronoiDiagram.getGeometryN(i);
            if (cell.contains(point)) {
                return i;
            }
        }
        return null;
    }

    private static List<Coordinate> generateRandomCoordinates(int numberOfPoints, int width, int height) {
        List<Coordinate> coordinates = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numberOfPoints; i++) {
            coordinates.add(new Coordinate(random.nextDouble() * width, random.nextDouble() * height));
        }
        return coordinates;
    }
}
