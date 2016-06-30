package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.MessageType;

import lombok.extern.java.Log;

/**
 * Handler for processing CancellationOrder messages.
 * 
 * @author Arkadiusz Cieslak
 */
@Log
public class CancellationOrderHandler extends AbstractMessageHandler<CancellationOrder> {
    
    public CancellationOrderHandler() {
        super(MessageType.CANCEL, CancellationOrder.class);
    }

    @Override
    protected void handleMessage(CancellationOrder brokerMessage) {
        log.info(brokerMessage.toString());
    }
}
