package com.gft.digitalbank.exchange.solution.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.model.Transaction;
import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;

import lombok.Getter;

/**
 * Class reperesents transaction engine for all products and brokers.
 * 
 * @author Arkadiusz Cieslak
 */
public class TransactionEngine {
    
    /** List of transactions */
    @Getter
    private Queue<Transaction> transactions = new ConcurrentLinkedQueue();
    
    /** Map of product transaction engines identified by product name */
    private Map<String, ProductTransactionEngine> productEngines = new HashMap<>();

    /** Map of processed messages identified by message id */
    private Map<Integer, BrokerMessage> processedMessages = new HashMap<>();
    
    private Observable shutdownMessageObservable = new Observable();
    
    /** Static instance of TransactionEngine */
    private static final TransactionEngine INSTANCE = new TransactionEngine();
    
    /**
     * Private constructor.
     */
    private TransactionEngine() {}
    
    /**
     * Factory method to get singleton instance.
     * It is thread-safe.
     * 
     * @return singleton instance of TransactionEngine
     */
    public static TransactionEngine getInstance() {
        return TransactionEngine.INSTANCE;
    }
    
    public SolutionResult createSolutionResult() {
        return null;
    }

    /**
     * Method called when PositionOrder message arrives.
     * 
     * @param message PositionOrder message
     */
    public void onBrokerMessage(PositionOrder message) {
        ProductTransactionEngine pte = getProductTransactionEngine(message.getProduct());
    }
    
    /**
     * Method called when CancellationOrder message arrives.
     * 
     * @param message CancellationOrder message
     */
    public void onBrokerMessage(CancellationOrder message) {
        BrokerMessage bm = getBrokerMessage(message.getCancelledOrderId());
    }
    
    /**
     * Method called when ModificationOrder message arrives.
     * 
     * @param message ModificationOrder message
     */
    public void onBrokerMessage(ModificationOrder message) {
        BrokerMessage bm = getBrokerMessage(message.getModifiedOrderId());
    }
    
    /**
     * Method called when ShutdownNotification message arrives.
     * 
     * @param message ShutdownNotification message
     */
    public void onBrokerMessage(ShutdownNotification message) {
    }
    
    private ProductTransactionEngine getProductTransactionEngine(String productName) {
        return productEngines.get(productName);
    }
    
    private BrokerMessage getBrokerMessage(int messageId) {
        return processedMessages.get(messageId);
    }
}
