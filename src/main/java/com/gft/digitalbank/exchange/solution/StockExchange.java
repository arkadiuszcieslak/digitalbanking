package com.gft.digitalbank.exchange.solution;

import java.util.List;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.gft.digitalbank.exchange.Exchange;
import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.solution.message.MessageProcessorEngine;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Implementation of Exchange interface.
 * 
 * @author Arkadiusz Cieslak
 */
@CommonsLog
public class StockExchange implements Exchange {
    
    private ProcessingListener processingListener;
    
    private List<String> destinations;
    
    private ConnectionFactory connectionFactory;

    public StockExchange() {
        lookupConnectionFactory();
    }
    
    @Override
    public void register(ProcessingListener processingListener) {
        this.processingListener = processingListener;
    }

    @Override
    public void setDestinations(List<String> list) {
        this.destinations = list;
    }

    @Override
    public void start() {
        new MessageProcessorEngine(connectionFactory, destinations).start();
    }
    
    /**
     * Method looks up for ConnectionFactory instance.
     */
    private void lookupConnectionFactory() {
        try {
            Context context = new InitialContext();
            
            connectionFactory = (ConnectionFactory) context.lookup("ConnectionFactory");
        } catch(NamingException e) {
            log.error("ConnectionFactory lookup failed: ", e);
        }
    }
}
