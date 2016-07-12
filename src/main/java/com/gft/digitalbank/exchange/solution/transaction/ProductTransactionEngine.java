package com.gft.digitalbank.exchange.solution.transaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections.comparators.ComparatorChain;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.OrderEntry;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;

import lombok.Getter;

/**
 * Transaction engine for single product.
 * 
 * @author Arkadiusz Cieslak
 */
public class ProductTransactionEngine {

    /** Comparator for comparing PositionOrders by price in ascending order */
    private static final Comparator<PositionOrder> COMPARE_BY_PRICE = (o1, o2) -> Integer.compare(o1.getDetails().getPrice(),
            o2.getDetails().getPrice());

    /** Comparator for comparing PositionOrders by timestamp in ascending order */
    private static final Comparator<PositionOrder> COMPARE_BY_TIMESTAMP = (o1, o2) -> Long.compare(o1.getTimestamp(), o2.getTimestamp());

    /** Name of the product */
    @Getter
    private String productName;

    /** Sorted set of buy orders */
    @SuppressWarnings("unchecked")
    @Getter
    private Set<PositionOrder> buyOrders = Collections
            .synchronizedSortedSet(new TreeSet<>(new ComparatorChain(Arrays.asList(COMPARE_BY_PRICE.reversed(), COMPARE_BY_TIMESTAMP))));

    /** Sorted set of sell orders */
    @SuppressWarnings("unchecked")
    @Getter
    private Set<PositionOrder> sellOrders = Collections
            .synchronizedSortedSet(new TreeSet<>(new ComparatorChain(Arrays.asList(COMPARE_BY_PRICE, COMPARE_BY_TIMESTAMP))));

    /**
     * Constructor.
     * 
     * @param productName name of the product
     */
    public ProductTransactionEngine(final String productName) {
        this.productName = productName;
    }

    public void onPositionOrder(final PositionOrder order) {
        switch (order.getSide()) {
        case BUY:
            buyOrders.add(order);
            break;
        case SELL:
            sellOrders.add(order);
            break;
        }
    }

    public void onCancellOrder(final PositionOrder order) {
        switch (order.getSide()) {
        case BUY:
            buyOrders.remove(order);
            break;
        case SELL:
            sellOrders.remove(order);
            break;
        }
    }

    public void onModifyOrder(final PositionOrder order, final ModificationOrder message) {
        switch (order.getSide()) {
        case BUY:
            buyOrders.remove(order);
            break;
        case SELL:
            sellOrders.remove(order);
            break;
        }
    }

    public OrderBook toOrderBook() {
        List<OrderEntry> buyEntries = getBuyOrders().stream().map(o -> OrderEntry.builder().id(o.getId()).broker(o.getBroker())
                .client(o.getClient()).amount(o.getDetails().getAmount()).price(o.getDetails().getPrice()).build())
                .collect(Collectors.toList());

        List<OrderEntry> sellEntries = getSellOrders().stream().map(o -> OrderEntry.builder().id(o.getId()).broker(o.getBroker())
                .client(o.getClient()).amount(o.getDetails().getAmount()).price(o.getDetails().getPrice()).build())
                .collect(Collectors.toList());

        return new OrderBook(getProductName(), buyEntries, sellEntries);
    }
}
