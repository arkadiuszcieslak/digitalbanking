package com.gft.digitalbank.exchange.solution.transaction;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.gft.digitalbank.exchange.model.orders.PositionOrder;

import lombok.Getter;
import scala.annotation.meta.getter;

/**
 * Transaction engine for single product.
 * 
 * @author Arkadiusz Cieslak
 */
public class ProductTransactionEngine {
    
    /** Is engine enabled */
    private boolean enabled;
    
    /** Name of the product */
    private String productName;
    
    /** Sorted set of buy orders */
    @Getter
    private Set<PositionOrder> buyOrders = new TreeSet<>();

    /** Sorted set of sell orders */
    @Getter
    private Set<PositionOrder> sellOrders = new TreeSet<>();
    
    /** Unsorted FIFO orders waiting for processing */
    private List<PositionOrder> pendingOrders = new LinkedList<>();

    /**
     * Constructor.
     * 
     * @param productName name of the product
     */
    public ProductTransactionEngine(String productName) {
        this.productName = productName;
    }
}
