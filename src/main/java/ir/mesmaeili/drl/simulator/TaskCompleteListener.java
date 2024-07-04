package ir.mesmaeili.drl.simulator;

import ir.mesmaeili.drl.model.EdgeServer;

public interface TaskCompleteListener {
    void onTaskComplete(EdgeServer server);
}

