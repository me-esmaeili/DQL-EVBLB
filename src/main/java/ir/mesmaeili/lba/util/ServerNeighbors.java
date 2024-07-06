package ir.mesmaeili.lba.util;

import ir.mesmaeili.lba.model.EdgeServer;

import java.util.ArrayList;
import java.util.List;

public class ServerNeighbors {

    private static final double EPSILON = 0.01;
    private static final double PSI = 5.;
    private static final int C_MAX = 50;
    private static final double S = 0.1;
    private static final double NU = 5.;
    private final EdgeServer edgeServer;

    public ServerNeighbors(EdgeServer edgeServer) {
        this.edgeServer = edgeServer;
    }

    public List<EdgeServer> findNeighbors(List<EdgeServer> allEdgeServers) {
        List<EdgeServer> N_i = new ArrayList<>();
        N_i.add(this.edgeServer);
        double RD_n = EPSILON;
        for (int c = 1; c <= C_MAX; c++) {
            N_i = updateNeighbors(N_i, RD_n, allEdgeServers);
            RD_n = Math.min(PSI, Math.max(0, S * (NU - N_i.size())));
        }
        return N_i;
    }

    private List<EdgeServer> updateNeighbors(List<EdgeServer> currentNeighbors, double RD_n, List<EdgeServer> allEdgeServers) {
        List<EdgeServer> updatedNeighbors = new ArrayList<>();
        for (EdgeServer e_j : allEdgeServers) {
            if (dist(this.edgeServer, e_j) <= RD_n) {
                updatedNeighbors.add(e_j);
            }
        }
        return updatedNeighbors;
    }

    private double dist(EdgeServer e_i, EdgeServer e_j) {
        return Math.hypot(e_i.getLocation().getX() - e_j.getLocation().getX(),
                e_i.getLocation().getY() - e_j.getLocation().getY());
    }
}
