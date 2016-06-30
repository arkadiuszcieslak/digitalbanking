package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.MessageType;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;

import lombok.extern.java.Log;

/**
 * Handler for processing PositionOrder messages.
 * 
 * @author Arkadiusz Cieslak
 */
@Log
public class PositionOrderHandler extends AbstractMessageHandler<PositionOrder> {

    public PositionOrderHandler() {
        super(MessageType.ORDER, PositionOrder.class);
    }

    @Override
    protected void handleMessage(PositionOrder brokerMessage) {
        log.info(brokerMessage.toString());
    }
}
