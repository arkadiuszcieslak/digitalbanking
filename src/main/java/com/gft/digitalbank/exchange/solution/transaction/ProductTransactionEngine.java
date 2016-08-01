package com.gft.digitalbank.exchange.solution.transaction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.apache.commons.collections.comparators.ComparatorChain;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.OrderEntry;
import com.gft.digitalbank.exchange.model.Transaction;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.solution.util.MessageUtils;
import com.gft.digitalbank.exchange.solution.util.SerialExecutor;

import lombok.Getter;
import lombok.Setter;

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

    /** Reference to transaction engine */
    @Setter
    private TransactionEngine transactionEngine;

    /** Sorted set of buy orders */
    @SuppressWarnings("unchecked")
    private SortedSet<PositionOrder> buyOrders = new TreeSet<>(
            new ComparatorChain(Arrays.asList(COMPARE_BY_PRICE.reversed(), COMPARE_BY_TIMESTAMP)));

    /** Sorted set of sell orders */
    @SuppressWarnings("unchecked")
    private SortedSet<PositionOrder> sellOrders = new TreeSet<>(new ComparatorChain(Arrays.asList(COMPARE_BY_PRICE, COMPARE_BY_TIMESTAMP)));

    /** Serial executor which queues tasks in order of submitions */
    private Executor executor;
    
    /** Flag indicates that engine is active */
    private boolean active = true;
    
    /** OrderBook build on buy and sell enries */
    @Getter
    private OrderBook orderBook;

    /**
     * Constructor.
     * 
     * @param productName name of the product
     * @param executor provided executor
     */
    public ProductTransactionEngine(final String productName, Executor executor) {
        this.productName = productName;
        this.executor = new SerialExecutor(executor);
    }

    /**
     * Submits in executor processing of PositionOrder.
     * 
     * @param order PositionOrder
     */
    public void onPositionOrder(final PositionOrder order) {
        if(! active) {
            return;
        }

        executor.execute(() -> {
            addPositionOrder(order);
            processTransactions();
        });
    }

    /**
     * Submits in executor processing of PositionOrder cancellation.
     * 
     * @param order PositionOrder
     */
    public void onCancellOrder(final PositionOrder order) {
        if(! active) {
            return;
        }

        executor.execute(() -> {
            removePositionOrder(order);
            processTransactions();
        });
    }

    /**
     * Submits in executor processing of PositionOrder modification.
     * 
     * @param oldOrder old PositionOrder
     * @param newOrder modified PositionOrder
     */
    public void onModifyOrder(final PositionOrder oldOrder, final PositionOrder newOrder) {
        if(! active) {
            return;
        }

        executor.execute(() -> {
            removePositionOrder(oldOrder);
            addPositionOrder(newOrder);
            processTransactions();
        });
    }
    
    /**
     * Submits in executor processing of Shutdown notification.
     * 
     * @param doneSignal synchronizing object for all engines
     */
    public void onShutdown(final CountDownLatch doneSignal) {
        executor.execute(() -> {
            toOrderBook();
            buyOrders.clear();
            sellOrders.clear();
            active = false;
            
            doneSignal.countDown();
        });
    }

    /**
     * Adds PositionOrder to proper sorted collection based on order side.
     * 
     * @param order added order
     */
    private void addPositionOrder(final PositionOrder order) {
        switch (order.getSide()) {
        case BUY:
            buyOrders.add(order);
            break;
        case SELL:
            sellOrders.add(order);
            break;
        }
    }

    /**
     * Removes PositionOrder from proper sorted collection based on order side.
     * 
     * @param order removed order
     */
    private void removePositionOrder(final PositionOrder order) {
        switch (order.getSide()) {
        case BUY:
            buyOrders.remove(order);
            break;
        case SELL:
            sellOrders.remove(order);
            break;
        }
    }

    /**
     * Try to process transactions based on orders on lists.
     */
    private void processTransactions() {
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            PositionOrder buy = buyOrders.first();
            PositionOrder sell = sellOrders.first();

            Transaction t = MessageUtils.tryCreateTransaction(buy, sell);

            if (t == null) {
                break;
            }

            transactionEngine.addExecutedTransaction(t);

            buyOrders.remove(buy);
            sellOrders.remove(sell);

            PositionOrder newBuy = MessageUtils.modifyPositionOrderAmount(buy, t.getAmount());
            PositionOrder newSell = MessageUtils.modifyPositionOrderAmount(sell, t.getAmount());

            if (newBuy != null) {
                buyOrders.add(newBuy);
            }

            if (newSell != null) {
                sellOrders.add(newSell);
            }
        }
    }

    /**
     * Method converts buy and sell entries to order book.
     */
    private void toOrderBook() {
        List<OrderEntry> buyEntries = toOrderEntries(buyOrders);
        List<OrderEntry> sellEntries = toOrderEntries(sellOrders);

        if (buyEntries.isEmpty() && sellEntries.isEmpty()) {
            return;
        }

        orderBook = new OrderBook(getProductName(), buyEntries, sellEntries);
    }

    /**
     * Converts collection of PositionOrders to list of OrderEntries. Method synchronizes on the orders collection.
     * 
     * @param orders collection of PositionOrders
     * 
     * @return list of OrderEntries
     */
    private List<OrderEntry> toOrderEntries(Collection<PositionOrder> orders) {
        return orders.stream().map(o -> OrderEntry.builder().id(o.getId()).broker(o.getBroker()).client(o.getClient())
                .amount(o.getDetails().getAmount()).price(o.getDetails().getPrice()).build()).collect(Collectors.toList());
    }
}
