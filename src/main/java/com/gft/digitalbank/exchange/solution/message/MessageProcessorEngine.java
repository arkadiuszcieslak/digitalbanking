package com.gft.digitalbank.exchange.solution.message;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jms.ConnectionFactory;

import com.google.common.base.Preconditions;

import lombok.NonNull;

/**
 * Engine for processing messages from defined brokers.
 * 
 * @author Arkadiusz Cieslak
 */
public class MessageProcessorEngine {
    
    /** Factory for establishing connection to JMS broker */
    private ConnectionFactory connectionFactory;
    
    /** List of broker destinations */
    private List<String> destinations;
    
    /** Map of broker processors identified by broker destination name */
    private Map<String, BrokerMessageProcessor> brokerProcessors;
    
    /** Is engine started? */
    private boolean started;
    
    public MessageProcessorEngine(@NonNull ConnectionFactory connectionFactory, @NonNull List<String> destinations) {
        this.connectionFactory = connectionFactory;
        this.destinations = destinations;
    }
    
    /**
     * Method creates and starts order processors based on list of destinations.
     * If processors are created and run, it throws RuntimeException.
     * 
     * @param destinations destination list
     */
    public void start() {
        Preconditions.checkState(! started, "MessageProcessorEngine already started");
        
        brokerProcessors = destinations
                .stream()
                .collect(Collectors.toMap(d -> d, this::createAndStartProcessor));
        
        started = true;
        
    }
    
    /**
     * Creates and starts message processor for single broker destination.
     * 
     * @param destination broker destination
     * 
     * @return created and started message processor
     */
    private BrokerMessageProcessor createAndStartProcessor(String destination) {
        BrokerMessageProcessor processor = new BrokerMessageProcessor(connectionFactory, destination);
        
        processor.start();
        
        return processor;
    }
    
    /**
     * Method stops order processors.
     */
    public void stop() {
        Preconditions.checkState(started, "MessageProcessorEngine not started");

        brokerProcessors
            .values()
            .forEach(p -> p.stop());
    }
}
