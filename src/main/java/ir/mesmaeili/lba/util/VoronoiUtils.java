package ir.mesmaeili.lba.util;

import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.util.*;

public class VoronoiUtils {
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public List<Coordinate> generatePoints(int count, int spaceWith, int spaceHeight) {
        return generateRandomCoordinates(count, spaceWith, spaceHeight);
    }

    public Coordinate generateRndPoint(int spaceWith, int spaceHeight) {
        return generateRandomCoordinates(1, spaceWith, spaceHeight).get(0);
    }

    public Geometry generateDiagram(List<Coordinate> coordinates) {
        VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
        builder.setSites(coordinates);
        return builder.getDiagram(geometryFactory);
    }

    public List<Coordinate> getVoronoiCenters(Geometry voronoiDiagram) {
        List<Coordinate> centers = new ArrayList<>();
        for (int i = 0; i < voronoiDiagram.getNumGeometries(); i++) {
            Geometry cell = voronoiDiagram.getGeometryN(i);
            Point center = cell.getCentroid();
            centers.add(center.getCoordinate());
        }
        return centers;
    }

    public Geometry getRegion(Geometry voronoiDiagram, EdgeServer server) {
        return getRegion(voronoiDiagram, server.getLocation());
    }

    public Geometry getRegion(Geometry voronoiDiagram, Coordinate location) {
        Point point = geometryFactory.createPoint(location);
        for (int i = 0; i < voronoiDiagram.getNumGeometries(); i++) {
            Geometry cell = voronoiDiagram.getGeometryN(i);
            if (cell.contains(point)) {
                return cell;
            }
        }
        return null;
    }

    public List<EdgeServer> getRegionServers(Geometry voronoiDiagram, Collection<EdgeServer> servers) {
        List<EdgeServer> targetServers = new ArrayList<>();
        for (EdgeServer server : servers) {
            if (getRegion(voronoiDiagram, server.getLocation()) != null) {
                targetServers.add(server);
            }
        }
        return targetServers;
    }

    public List<Task> getRegionTasks(Geometry region, Collection<Task> tasks) {
        List<Task> targetTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (getRegion(region, task.getLocation()) != null) {
                targetTasks.add(task);
            }
        }
        return targetTasks;
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
