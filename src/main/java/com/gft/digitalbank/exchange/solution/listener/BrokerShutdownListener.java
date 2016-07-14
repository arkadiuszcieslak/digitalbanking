package com.gft.digitalbank.exchange.solution.listener;

import java.util.Observable;
import java.util.Observer;

import com.gft.digitalbank.exchange.solution.StockExchange;

import lombok.Setter;

/**
 * Method listens for shutdown messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class BrokerShutdownListener implements Observer {
    
    /** Reference to StockExchange object */
    @Setter
    private StockExchange stockExchange;

    @Override
    public void update(Observable o, Object broker) {
        if (broker != null && broker instanceof String) {
            String brokerName = (String) broker;
            
            stockExchange.shutdownBroker(brokerName);
        }
        
    }
}
