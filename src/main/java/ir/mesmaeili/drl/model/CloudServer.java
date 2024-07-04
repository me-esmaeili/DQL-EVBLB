package ir.mesmaeili.drl.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
public class CloudServer {
    private final Queue<Task> taskQueue = new LinkedList<>();
    private final long maxQueueSize = Long.MAX_VALUE;

    public boolean addTask(Task task) {
        taskQueue.add(task);
        return true;
    }
}
