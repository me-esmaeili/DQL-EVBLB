package ir.mesmaeili.lba.algorithm;

import ir.mesmaeili.lba.model.EdgeServer;

import java.util.List;

public interface NeighborSelector {
    List<EdgeServer> findNeighbors(EdgeServer server, List<EdgeServer> allServers);
}
