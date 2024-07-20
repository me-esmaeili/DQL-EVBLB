package ir.mesmaeili.lba.model;

import lombok.Getter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
public class CloudServer {
    private final Queue<Task> taskQueue = new LinkedList<>();
    private final long maxQueueSize = Long.MAX_VALUE;

    public void addTask(Task task) {
        taskQueue.add(task);
    }
}
