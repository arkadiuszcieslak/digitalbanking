package com.gft.digitalbank.exchange.solution.message;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import com.gft.digitalbank.exchange.solution.util.SerialExecutor;
import com.google.common.base.Preconditions;

import lombok.Setter;

/**
 * Class for processing messages from defined brokers.
 * 
 * @author Arkadiusz Cieslak
 */
public class MessageProcessor extends AbstractProcessor {
    
    /** List of broker destinations */
    @Setter
    private List<String> destinations;
    
    /** Map of broker processors identified by broker destination name */
    private Map<String, BrokerMessageProcessor> brokerProcessors;
    
    private Executor serialExecutor;
    
    /**
     * Default constructor.
     */
    public MessageProcessor() {
        super("MessageProcessor");
    }
    
    @Override
    protected void doStart() {
        Preconditions.checkNotNull(destinations, "Destinations not set");
        
        serialExecutor = new SerialExecutor(executor);
        
        brokerProcessors = destinations
                .stream()
                .collect(Collectors.toMap(d -> d, this::createAndStartProcessor));
        
    }
    
    /**
     * Creates and starts message processor for single broker destination.
     * 
     * @param destination broker destination
     * 
     * @return created and started message processor
     */
    private BrokerMessageProcessor createAndStartProcessor(String destination) {
        BrokerMessageProcessor processor = new BrokerMessageProcessor(destination);
        
        processor.setConnectionFactory(connectionFactory);
        processor.setBrokerMessageListener(brokerMessageListener);
        processor.setExecutor(serialExecutor);
        processor.start();
        
        return processor;
    }
    
    @Override
    protected void doStop() {
        brokerProcessors.clear();
        serialExecutor = null;
    }
}
