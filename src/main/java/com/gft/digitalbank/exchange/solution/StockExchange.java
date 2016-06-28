package com.gft.digitalbank.exchange.solution;

import java.util.List;

import com.gft.digitalbank.exchange.Exchange;
import com.gft.digitalbank.exchange.listener.ProcessingListener;

/**
 * Implementation of Exchange interface.
 * 
 * @author Arkadiusz Cieslak
 */
public class StockExchange implements Exchange {
    
    private ProcessingListener processingListener;
    
    private List<String> destinations;
    
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
    }
}
