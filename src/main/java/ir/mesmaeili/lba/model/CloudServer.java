package ir.mesmaeili.lba.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudServer implements Serializable {
    private final Queue<Task> taskQueue = new LinkedList<>();
    private final long maxQueueSize = Long.MAX_VALUE;

    public void addTask(Task task) {
        taskQueue.add(task);
    }
}
