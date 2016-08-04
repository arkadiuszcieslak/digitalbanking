package com.gft.digitalbank.exchange.solution.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.Transaction;
import com.gft.digitalbank.exchange.model.orders.MessageType;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;

/**
 * Unit tests for class MessageUtils.
 * 
 * @author Arkadiusz Cieslak
 */
public class MessageUtilsTest {
    private PositionOrder o1;
    private PositionOrder o2;
    private PositionOrder o3;
    private PositionOrder o4;

    @Before
    public void setUp() {
        o1 = PositionOrder.builder().id(1).broker("B1").client("C1").product("P").side(Side.BUY).timestamp(1)
                .details(OrderDetails.builder().amount(10).price(100).build()).build();
        o2 = PositionOrder.builder().id(2).broker("B2").client("C2").product("P").side(Side.SELL).timestamp(2)
                .details(OrderDetails.builder().amount(10).price(100).build()).build();
        o3 = PositionOrder.builder().id(3).broker("B3").client("C3").product("P").side(Side.SELL).timestamp(3)
                .details(OrderDetails.builder().amount(10).price(110).build()).build();
        o4 = PositionOrder.builder().id(4).broker("B4").client("C4").product("P").side(Side.SELL).timestamp(4)
                .details(OrderDetails.builder().amount(10).price(90).build()).build();
    }

    @Test
    public void testSameBroker() {
        Assert.assertTrue(MessageUtils.sameBroker(o1, o1));
        Assert.assertTrue(MessageUtils.sameBroker(o2, o2));
        Assert.assertTrue(MessageUtils.sameBroker(o3, o3));
        Assert.assertTrue(MessageUtils.sameBroker(o4, o4));
        Assert.assertFalse(MessageUtils.sameBroker(o1, o2));
        Assert.assertFalse(MessageUtils.sameBroker(o1, o3));
        Assert.assertFalse(MessageUtils.sameBroker(o1, o4));
    }

    @Test
    public void testTryCreateTransaction() {
        AtomicInteger atomicInt = new AtomicInteger(0);
        Transaction t1 = MessageUtils.tryCreateTransaction(atomicInt, o1, o2);
        Transaction t2 = MessageUtils.tryCreateTransaction(atomicInt, o1, o3);
        Transaction t3 = MessageUtils.tryCreateTransaction(atomicInt, o1, o4);

        Assert.assertNotNull(t1);
        Assert.assertEquals(t1.getId(), 1);
        Assert.assertEquals(t1.getBrokerBuy(), "B1");
        Assert.assertEquals(t1.getBrokerSell(), "B2");
        Assert.assertEquals(t1.getClientBuy(), "C1");
        Assert.assertEquals(t1.getClientSell(), "C2");
        Assert.assertEquals(t1.getProduct(), "P");
        Assert.assertEquals(t1.getAmount(), 10);
        Assert.assertEquals(t1.getPrice(), 100);

        Assert.assertNull(t2);

        Assert.assertEquals(t3.getId(), 2);
        Assert.assertEquals(t3.getBrokerBuy(), "B1");
        Assert.assertEquals(t3.getBrokerSell(), "B4");
        Assert.assertEquals(t3.getClientBuy(), "C1");
        Assert.assertEquals(t3.getClientSell(), "C4");
        Assert.assertEquals(t3.getProduct(), "P");
        Assert.assertEquals(t3.getAmount(), 10);
        Assert.assertEquals(t3.getPrice(), 100);
    }

    @Test
    public void testModifyPositionOrderDetails() {
        PositionOrder mo = MessageUtils.modifyPositionOrderDetails(o1,
                ModificationOrder.builder().id(5).modifiedOrderId(1).timestamp(5).broker("B1")
                        .details(OrderDetails.builder().amount(11).price(110).build()).build());

        Assert.assertNotEquals(o1, mo);
        Assert.assertEquals(mo.getId(), 1);
        Assert.assertEquals(mo.getBroker(), "B1");
        Assert.assertEquals(mo.getClient(), "C1");
        Assert.assertEquals(mo.getProduct(), "P");
        Assert.assertEquals(mo.getMessageType(), MessageType.ORDER);
        Assert.assertEquals(mo.getTimestamp(), 5);
        Assert.assertEquals(mo.getSide(), Side.BUY);
        Assert.assertEquals(mo.getDetails().getAmount(), 11);
        Assert.assertEquals(mo.getDetails().getPrice(), 110);
    }

    @Test
    public void testModifyPositionOrderAmount() {
        PositionOrder mo = MessageUtils.modifyPositionOrderAmount(o1, 1);

        Assert.assertNotEquals(o1, mo);
        Assert.assertEquals(mo.getId(), 1);
        Assert.assertEquals(mo.getBroker(), "B1");
        Assert.assertEquals(mo.getClient(), "C1");
        Assert.assertEquals(mo.getProduct(), "P");
        Assert.assertEquals(mo.getMessageType(), MessageType.ORDER);
        Assert.assertEquals(mo.getTimestamp(), 1);
        Assert.assertEquals(mo.getSide(), Side.BUY);
        Assert.assertEquals(mo.getDetails().getAmount(), 9);
        Assert.assertEquals(mo.getDetails().getPrice(), 100);
    }
}
