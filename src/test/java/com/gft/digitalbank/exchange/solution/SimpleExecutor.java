package com.gft.digitalbank.exchange.solution;

import java.util.concurrent.Executor;

/**
 * Simple executor implementation used in tests. It executes command in calling thread.
 * 
 * @author Arkadiusz Cieslak
 */
public class SimpleExecutor implements Executor {

    @Override
    public void execute(Runnable command) {
        command.run();
    }

}
