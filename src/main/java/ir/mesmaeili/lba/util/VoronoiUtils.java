package ir.mesmaeili.lba.util;

import ir.mesmaeili.lba.model.EdgeServer;
import ir.mesmaeili.lba.model.Task;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class VoronoiUtils {
    private final static GeometryFactory geometryFactory = new GeometryFactory();

    public static List<ir.mesmaeili.lba.model.Point> generatePoints(int count, int spaceWith, int spaceHeight) {
        return generateRandomCoordinates(count, spaceWith, spaceHeight);
    }

    public static ir.mesmaeili.lba.model.Point generateRndPoint(int spaceWith, int spaceHeight) {
        return generateRandomCoordinates(1, spaceWith, spaceHeight).get(0);
    }

    public static Geometry generateDiagram(int width, int height, List<ir.mesmaeili.lba.model.Point> coordinates) {
        VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
        builder.setSites(coordinates.stream().map(c -> new Coordinate(c.getX(), c.getY())).collect(Collectors.toList()));
        Envelope clipEnvelope = new Envelope(-(width / 2.0), width / 2.0, -(height / 2.), height / 2.);
        builder.setClipEnvelope(clipEnvelope);
        return builder.getDiagram(geometryFactory);
    }

    public static List<Coordinate> getVoronoiCenters(Geometry voronoiDiagram) {
        List<Coordinate> centers = new ArrayList<>();
        for (int i = 0; i < voronoiDiagram.getNumGeometries(); i++) {
            Geometry cell = voronoiDiagram.getGeometryN(i);
            Point center = cell.getCentroid();
            centers.add(center.getCoordinate());
        }
        return centers;
    }

    public static Geometry getRegion(Geometry voronoiDiagram, EdgeServer server) {
        return getRegion(voronoiDiagram, server.getLocation());
    }

    public static Geometry getRegion(Geometry voronoiDiagram, ir.mesmaeili.lba.model.Point location) {
        Point point = geometryFactory.createPoint(new Coordinate(location.getX(), location.getY()));
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

    public static List<Task> getRegionTasks(Geometry region, Collection<Task> tasks) {
        List<Task> targetTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (getRegion(region, task.getLocation()) != null) {
                targetTasks.add(task);
            }
        }
        return targetTasks;
    }

    private static List<ir.mesmaeili.lba.model.Point> generateRandomCoordinates(int numberOfPoints, int width, int height) {
        List<ir.mesmaeili.lba.model.Point> coordinates = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numberOfPoints; i++) {
            coordinates.add(new ir.mesmaeili.lba.model.Point(NumberUtil.generateRandomFloat(width), NumberUtil.generateRandomFloat(height)));
        }
        return coordinates;
    }
}
