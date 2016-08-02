package com.gft.digitalbank.exchange.solution;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.gft.digitalbank.exchange.Exchange;
import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.solution.message.MessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

import lombok.extern.log4j.Log4j;

/**
 * Implementation of Exchange interface.
 * 
 * @author Arkadiusz Cieslak
 */
@Log4j
public class StockExchange implements Exchange, Observer {
    
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
        setUpTransactionEngineShutdownListener();
        setUpMessageProcessor();
        
        messageProcessor.start();
    }
    
    @Override
    public void update(Observable o, Object arg) {
        SolutionResult solution = transactionEngine.createSolutionResult();

        transactionEngine.deleteObserver(this);
        transactionEngine.shutdown();
        messageProcessor.stop();
        executor.shutdownNow();
        processingListener.processingDone(solution);
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
        transactionEngine = new TransactionEngine(executor, destinations);
    }
    
    private void setUpTransactionEngineShutdownListener() {
        transactionEngine.addObserver(this);
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
