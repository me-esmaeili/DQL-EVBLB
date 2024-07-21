package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.model.EdgeServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class EvblbBaseNeighborSelection implements NeighborSelector {

    private static final double EPSILON = 0.01;
    private static final int C_MAX = 50;
    private static final double S = 2.0;
    private static final double NU = 5.;

    @Override
    public Set<EdgeServer> findNeighbors(EdgeServer server, Collection<EdgeServer> allServers, int radius) {
        Set<EdgeServer> N_i = new HashSet<>();
        N_i.add(server);
        double RD_n = EPSILON;
        for (int c = 1; c <= C_MAX; c++) {
            N_i = updateNeighbors(server, RD_n, allServers);
            double newRD_N = RD_n + Math.min(radius, Math.max(0, S * (NU - N_i.size())));
            if (newRD_N == RD_n) {
                break;
            }
            RD_n = newRD_N;
        }

        for (EdgeServer neighbor : N_i) {
            log.info("Select Server {} as neighbor of server {}", neighbor.getId(), server.getId());
        }
        return N_i;
    }

    protected Set<EdgeServer> updateNeighbors(EdgeServer server, double RD_n, Collection<EdgeServer> allEdgeServers) {
        Set<EdgeServer> updatedNeighbors = new HashSet<>();
        for (EdgeServer e_j : allEdgeServers) {
            if (dist(server, e_j) <= RD_n) {
                updatedNeighbors.add(e_j);
            }
        }
        return updatedNeighbors;
    }

    protected double dist(EdgeServer e_i, EdgeServer e_j) {
        return Math.hypot(e_i.getLocation().getX() - e_j.getLocation().getX(),
                e_i.getLocation().getY() - e_j.getLocation().getY());
    }

}
