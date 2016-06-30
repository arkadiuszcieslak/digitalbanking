package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.MessageType;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;

import lombok.extern.java.Log;

/**
 * Handler for processing ShutdownNotification messages.
 * 
 * @author Arkadiusz Cieslak
 */
@Log
public class ShutdownNotificationHandler extends AbstractMessageHandler<ShutdownNotification> {

    public ShutdownNotificationHandler() {
        super(MessageType.SHUTDOWN_NOTIFICATION, ShutdownNotification.class);
    }

    @Override
    protected void handleMessage(ShutdownNotification brokerMessage) {
        log.info(brokerMessage.toString());
    }
}
