package com.gft.digitalbank.exchange.solution;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.gft.digitalbank.exchange.Exchange;
import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.solution.listener.BrokerShutdownListener;
import com.gft.digitalbank.exchange.solution.message.MessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

import lombok.extern.log4j.Log4j;

/**
 * Implementation of Exchange interface.
 * 
 * @author Arkadiusz Cieslak
 */
@Log4j
public class StockExchange implements Exchange {
    
    /** Listener called at the end of processing */
    private ProcessingListener processingListener;
    
    /** List of broker destinations */
    private List<String> destinations;
    
    /** Factory for establishing connection with JMS broker */
    private ConnectionFactory connectionFactory;
    
    /** MessageProcessor reference */
    private MessageProcessor messageProcessor;

    /** TransactionEngine reference */
    private TransactionEngine transactionEngine;
    
    /** Broker shutdown listener */
    private BrokerShutdownListener brokerShutdownListener; 
    
    /** Executor pool */
    private ExecutorService executor;
    
    @Override
    public void register(ProcessingListener processingListener) {
        this.processingListener = processingListener;
    }

    @Override
    public void setDestinations(List<String> list) {
        this.destinations = list;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void start() {
        setUpConnectionFactory();
        setUpExecutor();
        setUpTransactionEngine();
        setUpBrokerShutdownListener();
        setUpMessageProcessor();
        
        messageProcessor.start();
    }
    
    /**
     * Shutdown the broker.
     * 
     * @param brokerName name of the broker
     */
    public void shutdownBroker(String brokerName) {
        destinations.remove(brokerName);
        
        if (destinations.isEmpty()) {
            SolutionResult solution = transactionEngine.createSolutionResult();

            transactionEngine.deleteObserver(brokerShutdownListener);
            transactionEngine.shutdown();
            messageProcessor.stop();
            executor.shutdown();
            processingListener.processingDone(solution);
        }
    }
    
    /**
     * Method looks up for ConnectionFactory instance.
     */
    private void setUpConnectionFactory() {
        if (connectionFactory != null) {
            return;
        }
        
        try {
            Context context = new InitialContext();
            
            connectionFactory = (ConnectionFactory) context.lookup("ConnectionFactory");
        } catch(NamingException e) {
            log.error("ConnectionFactory lookup failed: ", e);
        }
    }
    
    /**
     * Method creates executor instance.
     */
    private void setUpExecutor() {
        if (executor == null) {
            executor = Executors.newCachedThreadPool();
        }
    }
    
    /**
     * Method creates TransactionEngine instance.
     */
    private void setUpTransactionEngine() {
        transactionEngine = new TransactionEngine(executor);
    }
    
    private void setUpBrokerShutdownListener() {
        brokerShutdownListener = new BrokerShutdownListener();
        
        brokerShutdownListener.setStockExchange(this);
        transactionEngine.addObserver(brokerShutdownListener);
    }
    
    /**
     * Method creates MessageProcessor instance.
     */
    private void setUpMessageProcessor() {
        messageProcessor = new MessageProcessor();
        
        messageProcessor.setConnectionFactory(connectionFactory);
        messageProcessor.setExecutor(executor);
        messageProcessor.setTransactionEngine(transactionEngine);
        messageProcessor.setDestinations(destinations);
    }
}
