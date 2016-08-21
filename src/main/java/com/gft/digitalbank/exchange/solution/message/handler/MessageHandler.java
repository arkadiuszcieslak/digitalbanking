package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.solution.message.BrokerMessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.BrokerMessageListener;

/**
 * Interface for handling specific broker messages of type T. Implementations of this class should be stateless and can be used in multiple
 * threads.
 * 
 * @author Arkadiusz Cieslak
 */
public interface MessageHandler<T extends BrokerMessage> {

    /**
     * Handles brokerMessage.
     * 
     * @param brokerMessageListener listener for broker messages
     * @param processor broker processor
     * @param brokerMessage message to handle
     */
    public void handleMessage(BrokerMessageListener brokerMessageListener, BrokerMessageProcessor processor, T brokerMessage);

    /**
     * Returns message class object.
     * 
     * @return message class object
     */
    public Class<T> getMessageClass();
}
