package com.gft.digitalbank.exchange.solution.message.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.solution.message.BrokerMessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Unit tests for class PositionOrderHandler.
 * 
 * @author Arkadiusz Cieslak
 */
public class PositionOrderHandlerTest {

    private PositionOrderHandler handler;

    @Mock
    private TransactionEngine transactionEngine;

    @Mock
    private BrokerMessageProcessor processor;

    @Mock
    private PositionOrder message;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        handler = new PositionOrderHandler();
    }

    @Test
    public void testGetMessageClass() {
        Assert.assertTrue(PositionOrder.class == handler.getMessageClass());
    }

    @Test
    public void testHandleMessage() {
        handler.handleMessage(transactionEngine, processor, message);

        Mockito.verify(transactionEngine).onBrokerMessage(message);
    }
}
