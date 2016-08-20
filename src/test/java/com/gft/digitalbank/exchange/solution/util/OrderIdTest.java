package com.gft.digitalbank.exchange.solution.util;

import org.junit.Assert;
import org.junit.Test;

public class OrderIdTest {

    @Test
    public void testOrderId() {
        OrderId oid1 = new OrderId(1, "B");
        OrderId oid2 = new OrderId(1, "B");
        OrderId oid3 = new OrderId(1, "C");

        Assert.assertEquals(oid1, oid2);
        Assert.assertEquals(oid1.hashCode(), oid2.hashCode());
        Assert.assertNotEquals(oid1, oid3);
        Assert.assertNotEquals(oid1.hashCode(), oid3.hashCode());
    }
}
