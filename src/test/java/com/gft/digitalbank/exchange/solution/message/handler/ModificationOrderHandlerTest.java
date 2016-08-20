package com.gft.digitalbank.exchange.solution.message.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.solution.message.BrokerMessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Unit tests for class ModificationOrderHandler.
 * 
 * @author Arkadiusz Cieslak
 */
public class ModificationOrderHandlerTest {

    private ModificationOrderHandler handler;

    @Mock
    private TransactionEngine transactionEngine;

    @Mock
    private BrokerMessageProcessor processor;

    @Mock
    private ModificationOrder message;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        handler = new ModificationOrderHandler();
    }

    @Test
    public void testGetMessageClass() {
        Assert.assertTrue(ModificationOrder.class == handler.getMessageClass());
    }

    @Test
    public void testHandleMessage() {
        handler.handleMessage(transactionEngine, processor, message);

        Mockito.verify(transactionEngine).onBrokerMessage(message);
    }
}
