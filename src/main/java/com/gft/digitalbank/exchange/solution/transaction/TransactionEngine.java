package com.gft.digitalbank.exchange.solution.transaction;

import java.util.Collection;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.model.Transaction;
import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.gft.digitalbank.exchange.solution.util.MessageUtils;

import lombok.EqualsAndHashCode;

/**
 * Class reperesents transaction engine for all products and brokers.
 * 
 * @author Arkadiusz Cieslak
 */
public class TransactionEngine extends Observable {

    /** List of transactions */
    private Queue<Transaction> transactions = new ConcurrentLinkedQueue<>();

    /** Map of product transaction engines (identified by product name) */
    private Map<String, ProductTransactionEngine> productEngines = new ConcurrentHashMap<>();

    /** Index of position orders (identified by OrderId) */
    private Map<OrderId, PositionOrder> positionOrderIdx = new ConcurrentHashMap<>();

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
     * Shutdowns engine.
     */
    public void shutdown() {
        productEngines.values()
            .stream()
            .forEach(pte -> pte.shutdown());

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
        ProductTransactionEngine pte = getProductTransactionEngine(message.getProduct());

        pte.onPositionOrder(message);

        addIndexPositionOrder(message);
    }

    /**
     * Method called when CancellationOrder message arrives.
     * 
     * @param message CancellationOrder message
     */
    public void onBrokerMessage(final CancellationOrder message) {
        PositionOrder order = getIndexPositionOrder(new OrderId(message.getCancelledOrderId(), message.getBroker()));

        if (MessageUtils.sameBroker(order, message)) {
            ProductTransactionEngine pte = getProductTransactionEngine(order.getProduct());

            pte.onCancellOrder(order);

            removeIndexPositionOrder(order);
        }
    }

    /**
     * Method called when ModificationOrder message arrives.
     * 
     * @param message ModificationOrder message
     */
    public void onBrokerMessage(final ModificationOrder message) {
        PositionOrder order = getIndexPositionOrder(new OrderId(message.getModifiedOrderId(), message.getBroker()));

        if (MessageUtils.sameBroker(order, message)) {
            ProductTransactionEngine pte = getProductTransactionEngine(order.getProduct());
            PositionOrder newOrder = MessageUtils.modifyPositionOrderDetails(order, message.getDetails());

            pte.onModifyOrder(order, newOrder);
            
            removeIndexPositionOrder(order);
            addIndexPositionOrder(newOrder);
        }
    }

    /**
     * Method called when ShutdownNotification message arrives.
     * 
     * @param message ShutdownNotification message
     */
    public void onBrokerMessage(final ShutdownNotification message) {
        setChanged();
        notifyObservers(message.getBroker());
        clearChanged();
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
            
            pte.setTransactionEngine(this);
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
