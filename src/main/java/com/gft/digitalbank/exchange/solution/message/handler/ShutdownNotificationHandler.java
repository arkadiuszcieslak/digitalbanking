package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.gft.digitalbank.exchange.solution.message.BrokerMessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.BrokerMessageListener;

/**
 * Handler for processing ShutdownNotification messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class ShutdownNotificationHandler implements MessageHandler<ShutdownNotification> {

    @Override
    public void handleMessage(BrokerMessageListener brokerMessageListener, BrokerMessageProcessor processor,
            ShutdownNotification brokerMessage) {
        processor.stop();
        brokerMessageListener.onBrokerMessage(brokerMessage);
    }

    @Override
    public Class<ShutdownNotification> getMessageClass() {
        return ShutdownNotification.class;
    }
}
