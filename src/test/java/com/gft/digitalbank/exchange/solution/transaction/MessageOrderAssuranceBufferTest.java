package com.gft.digitalbank.exchange.solution.transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;
import com.gft.digitalbank.exchange.solution.SimpleExecutor;

/**
 * Unit and functional tests for class MessageOrderAssuranceBuffer.
 * 
 * @author Arkadiusz Cieslak
 */
public class MessageOrderAssuranceBufferTest {

    private MessageOrderAssuranceBuffer messageOrderAssuranceBuffer;

    private TransactionEngine transactionEngine;

    @Before
    public void setUp() {
        Executor executor = new SimpleExecutor();
        transactionEngine = Mockito.spy(new TransactionEngine(executor, Arrays.asList("d1")));
        messageOrderAssuranceBuffer = new MessageOrderAssuranceBuffer(transactionEngine, executor);
    }

    @Test
    public void testMessagesProcessingOrder() {
        List<PositionOrder> orders = Arrays.asList(
                PositionOrder.builder().id(1).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                        .details(OrderDetails.builder().amount(1).price(1).build()).build(),
                PositionOrder.builder().id(2).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                        .details(OrderDetails.builder().amount(1).price(1).build()).build(),
                PositionOrder.builder().id(3).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                        .details(OrderDetails.builder().amount(1).price(1).build()).build(),
                PositionOrder.builder().id(4).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                        .details(OrderDetails.builder().amount(1).price(1).build()).build(),
                PositionOrder.builder().id(5).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                        .details(OrderDetails.builder().amount(1).price(1).build()).build(),
                PositionOrder.builder().id(6).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                        .details(OrderDetails.builder().amount(1).price(1).build()).build(),
                PositionOrder.builder().id(7).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                        .details(OrderDetails.builder().amount(1).price(1).build()).build());

        final List<PositionOrder> shuffeledOrders = new ArrayList<>(orders);
        final List<PositionOrder> executedOrders = new ArrayList<>();

        Collections.shuffle(shuffeledOrders);

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                executedOrders.add((PositionOrder) invocation.getArguments()[0]);

                return null;
            }
        }).when(transactionEngine).onBrokerMessage(Mockito.any(PositionOrder.class));

        shuffeledOrders.stream().forEach((o) -> messageOrderAssuranceBuffer.onBrokerMessage(o));

        Assert.assertEquals(orders, executedOrders);
    }
}
