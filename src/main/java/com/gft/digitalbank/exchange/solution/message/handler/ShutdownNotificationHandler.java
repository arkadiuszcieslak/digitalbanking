package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.gft.digitalbank.exchange.solution.message.BrokerMessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Handler for processing ShutdownNotification messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class ShutdownNotificationHandler implements MessageHandler<ShutdownNotification> {

    @Override
    public void handleMessage(TransactionEngine transactionEngine, BrokerMessageProcessor processor, ShutdownNotification brokerMessage) {
        processor.stop();
        transactionEngine.onBrokerMessage(brokerMessage);
    }

    @Override
    public Class<ShutdownNotification> getMessageClass() {
        return ShutdownNotification.class;
    }
}
