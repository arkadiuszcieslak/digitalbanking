package com.gft.digitalbank.exchange.solution.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
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

import lombok.extern.log4j.Log4j;

/**
 * Class reperesents transaction engine for all products and brokers.
 * 
 * @author Arkadiusz Cieslak
 */
@Log4j
public class TransactionEngine extends Observable implements BrokerMessageListener {
    
    /** Provided executor */
    private final Executor executor;

    /** List of transactions */
    private List<Transaction> transactions = new ArrayList<>();

    /** Map of product transaction engines (identified by product name) */
    private Map<String, ProductTransactionEngine> productEngines = new HashMap<>();

    /** Index of position orders (identified by Order id) */
    private Map<Integer, PositionOrder> positionOrderIdx = new HashMap<>();
    
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

    @Override
    public void onBrokerMessage(PositionOrder message) {
        ProductTransactionEngine pte = getProductTransactionEngine(message.getProduct());

        pte.onPositionOrder(message);

        addIndexPositionOrder(message);
    }

    @Override
    public void onBrokerMessage(CancellationOrder message) {
        PositionOrder order = getIndexPositionOrder(message.getCancelledOrderId());

        if (MessageUtils.sameBroker(order, message)) {
            ProductTransactionEngine pte = getProductTransactionEngine(order.getProduct());

            pte.onCancelOrder(order);

            removeIndexPositionOrder(order);
        }
    }

    @Override
    public void onBrokerMessage(ModificationOrder message) {
        PositionOrder order = getIndexPositionOrder(message.getModifiedOrderId());
        PositionOrder newOrder = MessageUtils.modifyPositionOrderDetails(order, message);
        
        if (newOrder != null) {
            ProductTransactionEngine pte = getProductTransactionEngine(order.getProduct());
    
            pte.onModifyOrder(order, newOrder);
            
            removeIndexPositionOrder(order);
            addIndexPositionOrder(newOrder);
        }
    }

    @Override
    public void onBrokerMessage(final ShutdownNotification message) {
        activeDestinations.remove(message.getBroker());
        
        if (activeDestinations.isEmpty()) {
            CountDownLatch doneSignal = new CountDownLatch(productEngines.keySet().size());

            shutdownAllProductEngines(doneSignal);
            waitForTermination(doneSignal);
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
        positionOrderIdx.put(order.getId(), order);
    }

    /**
     * Remove object from index.
     * 
     * @param order position order
     */
    private void removeIndexPositionOrder(final PositionOrder order) {
        positionOrderIdx.remove(order.getId());
    }

    /**
     * Get object from index.
     * 
     * @param orderId order id
     * 
     * @return PositionOrder from index or null if not present
     */
    private PositionOrder getIndexPositionOrder(final int orderId) {
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
                .map(ProductTransactionEngine::getOrderBook)
                .filter(o -> o != null)
                .collect(Collectors.toSet());
    }
}
