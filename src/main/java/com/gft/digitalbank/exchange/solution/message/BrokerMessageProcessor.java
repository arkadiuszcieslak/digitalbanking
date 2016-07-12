package com.gft.digitalbank.exchange.solution.message;

import java.util.List;
import java.util.stream.Collectors;

import javax.jms.Session;

import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.solution.message.handler.AbstractMessageHandler;
import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Class processes orders coming from single JMS Broker.
 * 
 * @author Arkadiusz Cieslak
 */
public class BrokerMessageProcessor extends AbstractProcessor {

    /** Predefined list of message handlers */
    @Setter
    private List<AbstractMessageHandler<? extends BrokerMessage>> messageHandlers;

    /** Destination name */
    @Getter
    private String destinationName;

    /** List of message listeners */
    private List<DefaultMessageListenerContainer> listeners;

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
        Preconditions.checkNotNull(messageHandlers, "MessageHandlers not set");
        
        listeners = messageHandlers
                .stream()
                .map(this::createMessageListener)
                .collect(Collectors.toList());
    }

    @Override
    protected void doStop() {
        listeners.forEach(l -> l.stop());
    }

    /**
     * Creates DefaultMessageListenerContainer and starts it based on message
     * handler object.
     * 
     * @param h
     *            message handler
     * 
     * @return DefaultMessageListenerContainer created based on message handler
     *         object
     */
    @SuppressWarnings("rawtypes")
    private DefaultMessageListenerContainer createMessageListener(AbstractMessageHandler h) {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.setDestinationName(destinationName);
        container.setMessageSelector(String.format("messageType = '%s'", h.getAcceptedMessageType()));
        container.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        container.setMessageListener(h);
        
        if(executor != null)
            container.setTaskExecutor(executor);
        
        container.initialize();
        container.start();

        return container;
    }
}
