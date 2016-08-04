package com.gft.digitalbank.exchange.solution.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.model.Transaction;
import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.gft.digitalbank.exchange.solution.util.MessageUtils;
import com.gft.digitalbank.exchange.solution.util.OrderId;

import lombok.extern.log4j.Log4j;

/**
 * Class reperesents transaction engine for all products and brokers.
 * 
 * @author Arkadiusz Cieslak
 */
@Log4j
public class TransactionEngine extends Observable {
    
    /** Provided executor */
    private final Executor executor;

    /** List of transactions */
    private List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

    /** Map of product transaction engines (identified by product name) */
    private Map<String, ProductTransactionEngine> productEngines = Collections.synchronizedMap(new HashMap<>());

    /** Index of position orders (identified by OrderId) */
    private Map<OrderId, PositionOrder> positionOrderIdx = new ConcurrentHashMap<>();
    
    /** Set of active destinations */
    private Set<String> activeDestinations = new HashSet<>();
    
    /**
     * Constructor.
     * 
     * @param executor provided executor
     */
    public TransactionEngine(Executor executor, Collection<String> destinations) {
        this.executor = executor;
        
        if (destinations != null) {
            this.activeDestinations.addAll(destinations);
        }
    }

    /**
     * Creates and returns solution result based on transactions and messages.
     * 
     * @return solution result build based on transactions and messages
     */
    public SolutionResult createSolutionResult() {
        return SolutionResult.builder()
            .orderBooks(createOrderBooks())
            .transactions(transactions)
            .build();
    }

    
    /**
     * Shutdowns engine.
     */
    public void shutdown() {
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
        PositionOrder order = getIndexPositionOrder(message.getCancelledOrderId(), message.getBroker());

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
        PositionOrder order = getIndexPositionOrder(message.getModifiedOrderId(), message.getBroker());

        PositionOrder newOrder = MessageUtils.modifyPositionOrderDetails(order, message);
        
        if (newOrder != null) {
            ProductTransactionEngine pte = getProductTransactionEngine(order.getProduct());
    
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
        activeDestinations.remove(message.getBroker());
        
        if (activeDestinations.isEmpty()) {
            executor.execute(() -> {
                CountDownLatch doneSignal = new CountDownLatch(productEngines.keySet().size());

                shutdownAllProductEngines(doneSignal);
                waitForTermination(doneSignal);
            }); 
        }
    }
    
    /**
     * Send shutdown signal to all product engines.
     * 
     * @param doneSignal synchronizing object for all engines
     */
    private void shutdownAllProductEngines(CountDownLatch doneSignal) {
        productEngines
            .values()
            .stream()
            .forEach(pte -> pte.onShutdown(doneSignal));
    }
    
    /**
     * Method waits for termination of product engines and shutdowns stock exchange.
     * 
     * @param doneSignal synchronizing object for all engines
     */
    private void waitForTermination(CountDownLatch doneSignal) {
        try {
            doneSignal.await();
            
            setChanged();
            notifyObservers();
            clearChanged();
        } catch (Exception e) {
            log.error("Error in method waitForTermination: ", e);
        }
    }

    /**
     * Returns ProductTransactionEngine. If not available than creates one and stores for later use.
     * 
     * @param productName name of product
     * 
     * @return ProductTransactionEngine identified by product name
     */
    private ProductTransactionEngine getProductTransactionEngine(final String productName) {
        return productEngines.computeIfAbsent(productName, k -> new ProductTransactionEngine(k, this, executor));
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
     * @param broker name of broker
     * 
     * @return PositionOrder from index or null if not present
     */
    private PositionOrder getIndexPositionOrder(final int orderId, String broker) {
        return positionOrderIdx.get(new OrderId(orderId, broker));
    }
    
    /**
     * Method creates collection of order books based on transactions.
     * 
     * @return collection of order books based on transactions
     */
    private Collection<? extends OrderBook> createOrderBooks() {
        return productEngines.values()
                .stream()
                .map(ProductTransactionEngine::getOrderBook)
                .filter(o -> o != null)
                .collect(Collectors.toSet());
    }
}
