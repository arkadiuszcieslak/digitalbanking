package com.gft.digitalbank.exchange.solution.util;

import lombok.EqualsAndHashCode;

/**
 * Helper class which stands for identification of PositionOrders.
 * 
 * @author Arkadiusz Cieslak
 */
@EqualsAndHashCode(callSuper = false, doNotUseGetters = false)
public final class OrderId {
    private final int id;
    private final String broker;
    
    public OrderId(int id, String broker) {
        this.id = id;
        this.broker = broker;
    }
}
