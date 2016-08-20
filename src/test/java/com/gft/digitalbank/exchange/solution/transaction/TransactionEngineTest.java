package com.gft.digitalbank.exchange.solution.transaction;

import java.util.Arrays;
import java.util.concurrent.Executor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.model.Transaction;
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
}
