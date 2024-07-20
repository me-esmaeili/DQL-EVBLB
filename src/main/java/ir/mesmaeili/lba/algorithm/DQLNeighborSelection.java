package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.model.EdgeServer;

import java.util.*;

public class DQLNeighborSelection extends EvblbBaseNeighborSelection {
    private static final double EPSILON = 0.01;
    private static final double PSI = 5.;
    private static final double S = 0.1;
    private static final double NU = 5.;

    private int radius;

    @Override
    public List<EdgeServer> findNeighbors(EdgeServer server, List<EdgeServer> allServers) {
        List<EdgeServer> N_i = new ArrayList<>();
        N_i.add(server);
        double RD_n = EPSILON;
        int radius = this.getNeighborSelectionRadius();
        for (int c = 1; c <= radius; c++) {
            N_i = super.updateNeighbors(server, RD_n, allServers);
            RD_n = Math.min(PSI, Math.max(0, S * (NU - N_i.size())));
        }
        return N_i;
    }

    @Override
    public int getNeighborSelectionRadius() {
        return radius;
    }

    public int updateRadius(int newRadius) {
        return radius = newRadius;
    }
}
