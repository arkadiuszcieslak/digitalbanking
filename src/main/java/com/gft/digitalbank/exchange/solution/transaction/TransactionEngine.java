package com.gft.digitalbank.exchange.solution.transaction;

import java.util.Collection;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.model.Transaction;
import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;

import lombok.EqualsAndHashCode;
import lombok.Setter;

/**
 * Class reperesents transaction engine for all products and brokers.
 * 
 * @author Arkadiusz Cieslak
 */
public class TransactionEngine {

    /** Executor pool */
    @Setter
    private Executor executor;

    /** List of transactions */
    private Queue<Transaction> transactions = new ConcurrentLinkedQueue<>();

    /** Map of product transaction engines (identified by product name) */
    private Map<String, ProductTransactionEngine> productEngines = new ConcurrentHashMap<>();

    // Indexes
    /** Index of position orders (identified by OrderId) */
    private Map<OrderId, PositionOrder> positionOrderIdx = new ConcurrentHashMap<>();

    private Observable shutdownMessageObservable = new Observable();

    /**
     * Creates and returns solution result based on transactions and messages.
     * 
     * @return solution result build based on transactions and messages
     */
    public SolutionResult createSolutionResult() {
        return SolutionResult.builder()
            .transactions(transactions)
            .orderBooks(createOrderBooks()) 
            .build();
    }

    /**
     * Clear all collections and indexes used by class.
     */
    public void clearAll() {
        transactions.clear();
        productEngines.clear();
        positionOrderIdx.clear();
    }

    /**
     * Adds executed transaction to internal list.
     * 
     * @param transaction executed transaction
     */
    public void addExecutedTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    /**
     * Method called when PositionOrder message arrives.
     * 
     * @param message PositionOrder message
     */
    public void onBrokerMessage(final PositionOrder message) {
        executor.execute(() -> {
            ProductTransactionEngine pte = getProductTransactionEngine(message.getProduct());

            pte.onPositionOrder(message);

            addIndexPositionOrder(message);
        });
    }

    /**
     * Method called when CancellationOrder message arrives.
     * 
     * @param message CancellationOrder message
     */
    public void onBrokerMessage(final CancellationOrder message) {
        executor.execute(() -> {
            PositionOrder order = getIndexPositionOrder(new OrderId(message.getCancelledOrderId(), message.getBroker()));

            if (order != null) {
                ProductTransactionEngine pte = getProductTransactionEngine(order.getProduct());

                pte.onCancellOrder(order);

                removeIndexPositionOrder(order);
            }
        });
    }

    /**
     * Method called when ModificationOrder message arrives.
     * 
     * @param message ModificationOrder message
     */
    public void onBrokerMessage(final ModificationOrder message) {
        executor.execute(() -> {
            PositionOrder order = getIndexPositionOrder(new OrderId(message.getModifiedOrderId(), message.getBroker()));

            if (order != null) {
                ProductTransactionEngine pte = getProductTransactionEngine(order.getProduct());

                pte.onModifyOrder(order, message);
            }
        });
    }

    /**
     * Method called when ShutdownNotification message arrives.
     * 
     * @param message ShutdownNotification message
     */
    public void onBrokerMessage(final ShutdownNotification message) {
    }

    /**
     * Returns ProductTransactionEngine. If not available than creates one and stores for later use.
     * 
     * @param productName name of product
     * 
     * @return ProductTransactionEngine identified by product name
     */
    private ProductTransactionEngine getProductTransactionEngine(final String productName) {
        ProductTransactionEngine pte = productEngines.get(productName);

        if (pte == null) {
            pte = new ProductTransactionEngine(productName);

            productEngines.put(productName, pte);
        }

        return pte;
    }

    /**
     * Add object to index.
     * 
     * @param order position order
     */
    private void addIndexPositionOrder(final PositionOrder order) {
        positionOrderIdx.put(new OrderId(order.getId(), order.getBroker()), order);
    }

    /**
     * Remove object from index.
     * 
     * @param order position order
     */
    private void removeIndexPositionOrder(final PositionOrder order) {
        positionOrderIdx.remove(new OrderId(order.getId(), order.getBroker()));
    }

    /**
     * Get object from index.
     * 
     * @param orderId order id
     * 
     * @return PositionOrder from index or null if not present
     */
    private PositionOrder getIndexPositionOrder(final OrderId orderId) {
        return positionOrderIdx.get(orderId);
    }
    
    /**
     * Method creates collection of order books based on transactions.
     * 
     * @return collection of order books based on transactions
     */
    private Collection<? extends OrderBook> createOrderBooks() {
        return productEngines.values()
                .stream()
                .map(ProductTransactionEngine::toOrderBook)
                .collect(Collectors.toSet());
    }

    @EqualsAndHashCode
    private final class OrderId {
        private final int id;
        private final String broker;
        
        public OrderId(int id, String broker) {
            this.id = id;
            this.broker = broker;
        }
    }
}
