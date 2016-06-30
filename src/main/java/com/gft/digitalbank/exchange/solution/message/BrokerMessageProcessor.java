package com.gft.digitalbank.exchange.solution.message;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.gft.digitalbank.exchange.solution.message.handler.AbstractMessageHandler;
import com.gft.digitalbank.exchange.solution.message.handler.CancellationOrderHandler;
import com.gft.digitalbank.exchange.solution.message.handler.ModificationOrderHandler;
import com.gft.digitalbank.exchange.solution.message.handler.PositionOrderHandler;
import com.gft.digitalbank.exchange.solution.message.handler.ShutdownNotificationHandler;
import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;

/**
 * Class processes orders coming from single JMS Broker.
 * 
 * @author Arkadiusz Cieslak
 */
@CommonsLog
public class BrokerMessageProcessor {

    /** Predefined list of message handlers */
    @SuppressWarnings("rawtypes")
    private static final List<AbstractMessageHandler> HANDLERS = Arrays.asList(new CancellationOrderHandler(), new ModificationOrderHandler(),
            new PositionOrderHandler(), new ShutdownNotificationHandler());

    /** Factory for establishing JMS Connection */
    private ConnectionFactory connectionFactory;

    /** Destination name */
    @Getter
    private String destinationName;

    /** List of message listeners */
    private List<DefaultMessageListenerContainer> listeners;
    
    /** Is processor started? */
    private boolean started;

    /**
     * Constructor.
     * 
     * @param connectionFactory
     *            factory for establishing JMS Connection
     * @param destinationName
     *            destination name
     */
    public BrokerMessageProcessor(@NonNull ConnectionFactory connectionFactory, @NonNull String destinationName) {
        this.connectionFactory = connectionFactory;
        this.destinationName = destinationName;
    }

    /**
     * Method starts processor for single broker.
     */
    public void start() {
        Preconditions.checkState(! started, "Processor already started");
        
        log.debug("Starting BrokerMessageProcessor for destination: " + destinationName);

        listeners = BrokerMessageProcessor.HANDLERS
                .stream()
                .map(this::createMessageListener)
                .collect(Collectors.toList());
        
        started = true;
    }

    public void stop() {
        Preconditions.checkState(started, "Processor not started");
        
        log.debug("Stoping BrokerMessageProcessor for destination: " + destinationName);

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
        container.initialize();
        container.start();

        return container;
    }
}
