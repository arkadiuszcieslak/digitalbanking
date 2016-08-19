package com.gft.digitalbank.exchange.solution;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.gft.digitalbank.exchange.listener.ProcessingListener;

/**
 * Unit tests for class StockExchange.
 * 
 * @author Arkadiusz Cieslak
 */
public class StockExchangeTest {

    private StockExchange stockExchange;

    private ExecutorService executor = Executors.newCachedThreadPool();

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Connection connection;

    @Mock
    private Session session;

    @Mock
    private Queue destination;

    @Mock
    private MessageConsumer consumer;

    @Mock
    private ProcessingListener processingListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        try {
            String destinationName = "d1";

            Mockito.when(connectionFactory.createConnection()).thenReturn(connection);
            Mockito.when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
            Mockito.when(session.createQueue(destinationName)).thenReturn(destination);
            Mockito.when(session.createConsumer(destination)).thenReturn(consumer);

            stockExchange = new StockExchange();

            stockExchange.register(processingListener);
            stockExchange.setConnectionFactory(connectionFactory);
            stockExchange.setDestinations(Arrays.asList(destinationName));
            stockExchange.setExecutor(executor);
            stockExchange.start();
        } catch (JMSException e) {
        }
    }

    @Test
    public void testUpdate() {
        stockExchange.update(null, null);

        Assert.assertTrue(executor.isShutdown());
        Mockito.verify(processingListener).processingDone(Matchers.any());
    }

}
