package com.gft.digitalbank.exchange.solution.message;

import java.util.concurrent.Executor;

import javax.jms.ConnectionFactory;

import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;
import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

/**
 * Abstract message processor.
 * 
 * @author Arkadiusz Cieslak
 */
@CommonsLog
public abstract class AbstractProcessor {
    
    /** Factory for establishing connection to JMS broker */
    @Setter @Getter
    protected ConnectionFactory connectionFactory;
    
    /** Reference to TransactionEngine */
    @Setter @Getter
    protected TransactionEngine transactionEngine;
    
    /** Optional executor which can be delivered in managed environments, i.e. JEE container */
    @Setter @Getter
    protected Executor executor;
    
    /** Is engine started? */
    protected boolean started;
    
    /** Name of the processor */
    protected final String processorName;
    
    /**
     * Method starts the processor.
     */
    protected abstract void doStart();

    /**
     * Method stops the processor.
     */
    protected abstract void doStop();
    
    public AbstractProcessor(String processorName) {
        this.processorName = processorName;
    }
    
    /**
     * Method starts processor. If processor run, it throws RuntimeException.
     * 
     * @param destinations destination list
     */
    public void start() {
        log.info("Starting " + processorName);
        
        Preconditions.checkNotNull(connectionFactory, "ConnectionFactory not set");
        Preconditions.checkNotNull(transactionEngine, "TransactionEngine not set");
        Preconditions.checkState(! started, processorName + " already started");
        
        doStart();
        
        started = true;
        
    }
    
    /**
     * Method stops order processors. If processor didn't run throws RuntimeException.
     */
    public void stop() {
        log.info("Stopping " + processorName);
        
        Preconditions.checkState(started, processorName + " not started");
        
        doStop();
        
        started = false;
    }

}
