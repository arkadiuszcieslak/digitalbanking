package com.gft.digitalbank.exchange.solution.message.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.solution.message.BrokerMessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Unit tests for class CancellationOrderHandler.
 * 
 * @author Arkadiusz Cieslak
 */
public class CancellationOrderHandlerTest {

    private CancellationOrderHandler handler;

    @Mock
    private TransactionEngine transactionEngine;

    @Mock
    private BrokerMessageProcessor processor;

    @Mock
    private CancellationOrder message;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        handler = new CancellationOrderHandler();
    }

    @Test
    public void testGetMessageClass() {
        Assert.assertTrue(CancellationOrder.class == handler.getMessageClass());
    }

    @Test
    public void testHandleMessage() {
        handler.handleMessage(transactionEngine, processor, message);

        Mockito.verify(transactionEngine).onBrokerMessage(message);
    }
}
