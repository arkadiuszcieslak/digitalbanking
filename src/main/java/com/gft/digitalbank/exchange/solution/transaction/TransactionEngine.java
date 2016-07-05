package com.gft.digitalbank.exchange.solution.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.model.Transaction;

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
    
    public SolutionResult createSolutionResult() {
        return null;
    }

}
