package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.algorithm.NeighborSelector;
import ir.mesmaeili.lba.model.EdgeServer;

import java.util.ArrayList;
import java.util.List;

public class EvblbBaseNeighborSelection implements NeighborSelector {

    private static final double EPSILON = 0.01;
    private static final double PSI = 5.;
    private static final int C_MAX = 50;
    private static final double S = 0.1;
    private static final double NU = 5.;

    @Override
    public List<EdgeServer> findNeighbors(EdgeServer server, List<EdgeServer> allServers) {
        List<EdgeServer> N_i = new ArrayList<>();
        N_i.add(server);
        double RD_n = EPSILON;
        for (int c = 1; c <= C_MAX; c++) {
            N_i = updateNeighbors(server, RD_n, allServers);
            RD_n = Math.min(PSI, Math.max(0, S * (NU - N_i.size())));
        }
        return N_i;
    }

    private List<EdgeServer> updateNeighbors(EdgeServer server, double RD_n, List<EdgeServer> allEdgeServers) {
        List<EdgeServer> updatedNeighbors = new ArrayList<>();
        for (EdgeServer e_j : allEdgeServers) {
            if (dist(server, e_j) <= RD_n) {
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
