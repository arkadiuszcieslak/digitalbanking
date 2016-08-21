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
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.model.Transaction;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;
import com.gft.digitalbank.exchange.solution.SimpleExecutor;

/**
 * Unit tests for class TransactionEngine.
 * 
 * @author Arkadiusz Cieslak
 */
public class TransactionEngineTest {

    private TransactionEngine transactionEngine;

    private Transaction transaction;

    private Executor executor = new SimpleExecutor();

    @Before
    public void setUp() {
        transaction = Transaction.builder().id(1).price(1).amount(1).product("p1").brokerBuy("b1").brokerSell("b2").clientBuy("c1")
                .clientSell("c2").build();
        transactionEngine = new TransactionEngine(executor, Arrays.asList("d1"));

        transactionEngine.addExecutedTransaction(transaction);
    }

    @Test
    public void testExecutedTransactions() {
        SolutionResult result = transactionEngine.createSolutionResult();

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getOrderBooks());
        Assert.assertEquals(result.getOrderBooks().size(), 0);
        Assert.assertNotNull(result.getTransactions());
        Assert.assertEquals(result.getTransactions().size(), 1);
        Assert.assertTrue(result.getTransactions().contains(transaction));
    }

    @Test
    public void testShutdown() {
        transactionEngine.shutdown();

        SolutionResult result = transactionEngine.createSolutionResult();

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getOrderBooks());
        Assert.assertEquals(result.getOrderBooks().size(), 0);
        Assert.assertNotNull(result.getTransactions());
        Assert.assertEquals(result.getTransactions().size(), 0);
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

        TransactionEngine te = Mockito.spy(transactionEngine);
        
        final List<PositionOrder> shuffeledOrders = new ArrayList<>(orders);
        final List<PositionOrder> executedOrders = new ArrayList<>();

        Collections.shuffle(shuffeledOrders);

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                executedOrders.add((PositionOrder) invocation.getArguments()[0]);
                
                return null;
            }
        }).when(te).processPositionOrder(Mockito.any(PositionOrder.class));

        shuffeledOrders.stream().forEach((o) -> te.onBrokerMessage(o));

        Assert.assertEquals(orders, executedOrders);
    }
}
