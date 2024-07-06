package ir.mesmaeili.lba.simulator;

import ir.mesmaeili.lba.model.EdgeServer;

public interface TaskCompleteListener {
    void onTaskComplete(EdgeServer server);
}

