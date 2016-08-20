package com.gft.digitalbank.exchange.solution.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit tests for class SerialExecutor.
 * 
 * @author Arkadiusz Cieslak
 */
public class SerialExecutorTest {

    @Test
    public void testSerialExecutor() {
        try {
            Executor executor = new SerialExecutor(Executors.newFixedThreadPool(16));
            final int[] expected = IntStream.rangeClosed(1, 1000000).toArray();
            final List<Integer> result = new ArrayList<>(expected.length);
            CountDownLatch stopSignal = new CountDownLatch(expected.length);

            for (int i : expected) {
                executor.execute(() -> {
                    result.add(i);
                    stopSignal.countDown();
                });
            }

            stopSignal.await();

            int i = 0;
            for (Integer ii : result) {
                Assert.assertEquals(ii.intValue(), expected[i]);
                i++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
