package com.gft.digitalbank.exchange.solution.message;

import javax.jms.Session;

import org.springframework.jms.listener.DefaultMessageListenerContainer;

import lombok.Getter;
import lombok.NonNull;

/**
 * Class processes orders coming from single JMS Broker.
 * 
 * @author Arkadiusz Cieslak
 */
public class BrokerMessageProcessor extends AbstractProcessor {

    /** Destination name */
    @Getter
    private String destinationName;

    /** Message listener container */
    private DefaultMessageListenerContainer messageListenerContainer;

    /**
     * Constructor.
     * 
     * @param destinationName name of the destination
     */
    public BrokerMessageProcessor(@NonNull String destinationName) {
        super("BrokerMessageProcessor-" + destinationName);
        
        this.destinationName = destinationName;
    }

    @Override
    protected void doStart() {
        messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setDestinationName(destinationName);
        messageListenerContainer.setAutoStartup(false);
        messageListenerContainer.setConcurrency("1-1");
        messageListenerContainer.setReceiveTimeout(1000L);
        messageListenerContainer.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        messageListenerContainer.setMessageListener(new OrderMessageListener(transactionEngine, this));
        
        if(executor != null)
            messageListenerContainer.setTaskExecutor(executor);
        
        messageListenerContainer.initialize();
        messageListenerContainer.start();
    }

    @Override
    protected void doStop() {
        messageListenerContainer.stop();
    }
}
