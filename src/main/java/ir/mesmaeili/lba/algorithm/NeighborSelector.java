package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.model.EdgeServer;

import java.util.Collection;
import java.util.Set;

public interface NeighborSelector {
    Set<EdgeServer> findNeighbors(EdgeServer server, Collection<EdgeServer> allServers, int radius);
}
