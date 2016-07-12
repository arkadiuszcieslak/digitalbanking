package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.MessageType;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Handler for processing ShutdownNotification messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class ShutdownNotificationHandler extends AbstractMessageHandler<ShutdownNotification> {

    /**
     * Constructor.
     * 
     * @param transactionEngine ref to TransactionEngine
     */
    public ShutdownNotificationHandler(TransactionEngine transactionEngine) {
        super(transactionEngine, MessageType.SHUTDOWN_NOTIFICATION, ShutdownNotification.class);
    }

    @Override
    protected void handleMessage(ShutdownNotification brokerMessage) {
        TransactionEngine.getInstance().onBrokerMessage(brokerMessage);
    }
}
