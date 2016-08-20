package com.gft.digitalbank.exchange.solution.util;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * Class implements Executor interface. It serializes execution of many tasks.
 * 
 * @author Arkadiusz Cieslak
 */
public class SerialExecutor implements Executor {
    /** Queue of task to execute */
    private final Queue<Runnable> tasks = new ArrayDeque<Runnable>();

    /** Real executor which this class wraps */
    private final Executor executor;

    /** Active task */
    private Runnable activeTask;

    /**
     * Constructor.
     * 
     * @param executor executor which this class wraps
     */
    public SerialExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public synchronized void execute(final Runnable r) {
        tasks.offer(() -> {
            try {
                r.run();
            } finally {
                scheduleNext();
            }
        });

        if (activeTask == null) {
            scheduleNext();
        }
    }

    /**
     * Execute next task from queue.
     */
    private synchronized void scheduleNext() {
        if ((activeTask = tasks.poll()) != null) {
            executor.execute(activeTask);
        }
    }

}
