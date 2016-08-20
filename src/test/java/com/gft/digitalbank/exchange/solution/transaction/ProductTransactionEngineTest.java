package com.gft.digitalbank.exchange.solution.transaction;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;
import com.gft.digitalbank.exchange.solution.SimpleExecutor;

/**
 * Unit tests for class ProductTransactionEngine.
 * 
 * @author Arkadiusz Cieslak
 */
public class ProductTransactionEngineTest {

    private ProductTransactionEngine productTransactionEngine;

    private Executor executor = Mockito.spy(new SimpleExecutor());

    @Mock
    private TransactionEngine transactionEngine;

    @Before
    public void setUp() {
        final String productName = "p1";

        productTransactionEngine = Mockito.spy(new ProductTransactionEngine(productName, transactionEngine, executor));
    }

    @Test
    public void testOnPositionOrder() {
        PositionOrder order = PositionOrder.builder().id(1).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                .details(OrderDetails.builder().amount(1).price(1).build()).build();

        productTransactionEngine.onPositionOrder(order);

        Mockito.verify(executor, Mockito.atLeastOnce()).execute(Matchers.any());
    }

    @Test
    public void testOnCancelOrder() {
        PositionOrder order = PositionOrder.builder().id(1).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                .details(OrderDetails.builder().amount(1).price(1).build()).build();

        productTransactionEngine.onCancelOrder(order);

        Mockito.verify(executor, Mockito.atLeastOnce()).execute(Matchers.any());
    }

    @Test
    public void testOnModifyOrder() {
        PositionOrder oldOrder = PositionOrder.builder().id(1).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                .details(OrderDetails.builder().amount(1).price(1).build()).build();
        PositionOrder newOrder = PositionOrder.builder().id(1).broker("b1").client("c1").product("p1").side(Side.BUY).timestamp(1)
                .details(OrderDetails.builder().amount(10).price(1).build()).build();

        productTransactionEngine.onModifyOrder(oldOrder, newOrder);

        Mockito.verify(executor, Mockito.atLeastOnce()).execute(Matchers.any());
    }

    @Test
    public void testOnShutdown() {
        CountDownLatch doneSignal = new CountDownLatch(1);

        productTransactionEngine.onShutdown(doneSignal);

        Mockito.verify(executor, Mockito.atLeastOnce()).execute(Matchers.any());
        Assert.assertEquals(doneSignal.getCount(), 0);
    }
}
