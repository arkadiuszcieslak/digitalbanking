package com.gft.digitalbank.exchange.solution.message;

import java.util.concurrent.Executor;

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

import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Unit tests for class BrokerMessageProcessor.
 * 
 * @author Arkadiusz Cieslak
 */
public class BrokerMessageProcessorTest {

    private BrokerMessageProcessor processor;

    private Executor executor = (r) -> r.run();

    @Mock
    private TransactionEngine transactionEngine;

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

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        try {
            String destinationName = "d1";

            Mockito.when(connectionFactory.createConnection()).thenReturn(connection);
            Mockito.when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
            Mockito.when(session.createQueue(destinationName)).thenReturn(destination);
            Mockito.when(session.createConsumer(destination)).thenReturn(consumer);

            processor = new BrokerMessageProcessor(destinationName);

            processor.setConnectionFactory(connectionFactory);
            processor.setExecutor(executor);
            processor.setTransactionEngine(transactionEngine);
        } catch (JMSException e) {
        }
    }

    @Test
    public void testStartStop() {
        try {
            processor.start();
            processor.stop();

            Mockito.verify(connection).start();
            Mockito.verify(connection).close();
            Mockito.verify(consumer).setMessageListener(Matchers.any());
            Mockito.verify(session).close();
        } catch (JMSException e) {
            Assert.assertTrue("JMSException thrown", false);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testNotStarted() {
        processor.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleStart() {
        processor.start();
        processor.start();
    }

}
