package ir.mesmaeili.drl.alg;

import ir.mesmaeili.drl.model.CloudServer;
import ir.mesmaeili.drl.model.EdgeServer;
import ir.mesmaeili.drl.model.Task;
import ir.mesmaeili.drl.util.ServerNeighbors;
import ir.mesmaeili.drl.util.VoronoiDiagram;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

public class EVBLBAlgorithm {
    private final List<EdgeServer> edgeServers;
    private final CloudServer cloudServer;
    private final List<Task> tasks;
    private final double alpha;
    private final double beta;
    private final double gamma;
    private final double deltaT;
    private int spacePointCount;
    private int spaceX;
    private int spaceY;

    // Constructor
    public EVBLBAlgorithm(List<EdgeServer> edgeServers, CloudServer cloudServer, List<Task> tasks,
                          double alpha, double beta, double gamma, double deltaT) {
        this.edgeServers = edgeServers;
        this.cloudServer = cloudServer;
        this.tasks = tasks;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.deltaT = deltaT;
    }

    // Main method to assign tasks
    public void assignTasks() {
        double Mc = getMaxCpuResource(edgeServers);
        double Mm = getMaxMemResource(edgeServers);
        double Md = getMaxDiskResource(edgeServers);
        // Assign tasks to cloud server if they exceed max resources of edge servers
        for (Task task : tasks) {
            if (task.getCpu() > Mc || task.getMemory() > Mm || task.getDisk() > Md) {
                assignToLeastLoadedCloudServer(task);
            }
        }
        // Compute the Voronoi Tessellation (VT)
        VoronoiDiagram vd = new VoronoiDiagram();
        List<Coordinate> points = vd.generatePoints(spacePointCount, spaceX, spaceY);
        Geometry voronoiTesselation = vd.generateDiagram(points);

        // Assign remaining tasks to edge servers
        ServerNeighbors serverNeighbors;
        for (EdgeServer e_i : edgeServers) {
            serverNeighbors = new ServerNeighbors(e_i);
            List<EdgeServer> neighbors = serverNeighbors.findNeighbors(edgeServers);
            EdgeServer e_k = getServerWithMaxRemainingResource(neighbors);
            assignTasksInRegionToServer(vd.getRegion(voronoiTesselation, e_i), e_k);
        }
    }

    private double getMaxCpuResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getCpu).max().getAsDouble();
    }

    private double getMaxMemResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getMemory).max().getAsDouble();
    }

    private double getMaxDiskResource(List<EdgeServer> servers) {
        return servers.stream().mapToDouble(EdgeServer::getDisk).max().getAsDouble();
    }

    private void assignToLeastLoadedCloudServer(Task task) {
        this.cloudServer.addTask(task);
    }

    private EdgeServer getServerWithMaxRemainingResource(List<EdgeServer> servers) {
        if (servers.isEmpty()) {
            return null;
        }

        EdgeServer serverWithMaxResource = servers.get(0);
        double maxResource = serverWithMaxResource.calculateRemainingResource(alpha, beta, gamma, deltaT);

        // Start from the second server since we already have the value for the first
        for (int i = 1; i < servers.size(); i++) {
            EdgeServer edgeServer = servers.get(i);
            double currentResource = edgeServer.calculateRemainingResource(alpha, beta, gamma, deltaT);
            if (currentResource > maxResource) {
                serverWithMaxResource = edgeServer;
                maxResource = currentResource;
            }
        }
        return serverWithMaxResource;
    }

    private void assignTasksInRegionToServer(Geometry region, EdgeServer server) { /* ... */ }
}
