package com.gft.digitalbank.exchange.solution.message;

import java.util.concurrent.Executor;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.MessageType;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.gft.digitalbank.exchange.model.orders.Side;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;
import com.google.gson.Gson;

/**
 * Unit tests for class OrderMessageListener.
 * 
 * @author Arkadiusz Cieslak
 */
public class OrderMessageListenerTest {

    private OrderMessageListener listener;

    private Executor executor = (r) -> r.run();

    @Mock
    private BrokerMessageProcessor processor;

    @Mock
    private TransactionEngine transactionEngine;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        listener = new OrderMessageListener(transactionEngine, processor, executor);
    }

    @Test(expected = NullPointerException.class)
    public void testOnMessageWithNoType() {
        TextMessage message = Mockito.mock(TextMessage.class);

        listener.onMessage(message);
    }

    @Test
    public void testOnPositionMessage() {
        try {
            TextMessage message = Mockito.mock(TextMessage.class);

            PositionOrder order = PositionOrder.builder().id(101).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                    .details(OrderDetails.builder().amount(1).price(1).build()).build();

            Mockito.when(message.getStringProperty(OrderMessageListener.MESSAGE_TYPE_PROPERTY_NAME)).thenReturn(MessageType.ORDER.name());
            Mockito.when(message.getText()).thenReturn(toJsonText(order));

            listener.onMessage(message);

            Mockito.verify(transactionEngine).onBrokerMessage(Mockito.eq(order));
        } catch (JMSException e) {
            Assert.assertTrue("JMSException thrown", false);
        }
    }
    
    @Test
    public void testOnCancelMessage() {
        try {
            TextMessage message = Mockito.mock(TextMessage.class);
            
            CancellationOrder order = CancellationOrder.builder().id(101).broker("b1").cancelledOrderId(1).messageType(MessageType.CANCEL)
                    .timestamp(1).build();
            
            Mockito.when(message.getStringProperty(OrderMessageListener.MESSAGE_TYPE_PROPERTY_NAME)).thenReturn(MessageType.CANCEL.name());
            Mockito.when(message.getText()).thenReturn(toJsonText(order));
            
            listener.onMessage(message);
            
            Mockito.verify(transactionEngine).onBrokerMessage(Mockito.eq(order));
        } catch (JMSException e) {
            Assert.assertTrue("JMSException thrown", false);
        }
    }

    @Test
    public void testOnModificationMessage() {
        try {
            TextMessage message = Mockito.mock(TextMessage.class);

            ModificationOrder order = ModificationOrder.builder().id(101).broker("b1").modifiedOrderId(1)
                    .details(OrderDetails.builder().amount(1).price(10).build()).timestamp(1).build();

            Mockito.when(message.getStringProperty(OrderMessageListener.MESSAGE_TYPE_PROPERTY_NAME))
                    .thenReturn(MessageType.MODIFICATION.name());
            Mockito.when(message.getText()).thenReturn(toJsonText(order));

            listener.onMessage(message);

            Mockito.verify(transactionEngine).onBrokerMessage(Mockito.eq(order));
        } catch (JMSException e) {
            Assert.assertTrue("JMSException thrown", false);
        }
    }

    @Test
    public void testOnShutdownMessage() {
        try {
            TextMessage message = Mockito.mock(TextMessage.class);

            ShutdownNotification order = ShutdownNotification.builder().id(101).broker("b1").timestamp(1).build();

            Mockito.when(message.getStringProperty(OrderMessageListener.MESSAGE_TYPE_PROPERTY_NAME))
                    .thenReturn(MessageType.SHUTDOWN_NOTIFICATION.name());
            Mockito.when(message.getText()).thenReturn(toJsonText(order));

            listener.onMessage(message);

            Mockito.verify(transactionEngine).onBrokerMessage(Mockito.eq(order));
            Mockito.verify(processor).stop();
        } catch (JMSException e) {
            Assert.assertTrue("JMSException thrown", false);
        }
    }

    private String toJsonText(BrokerMessage bm) {
        Gson gson = new Gson();
        return gson.toJson((Object) bm);
    }
}
