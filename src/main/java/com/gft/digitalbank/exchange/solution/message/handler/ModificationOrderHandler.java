package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.MessageType;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;

import lombok.extern.java.Log;

/**
 * Handler for processing ModificationOrder messages.
 * 
 * @author Arkadiusz Cieslak
 */
@Log
public class ModificationOrderHandler extends AbstractMessageHandler<ModificationOrder> {
    
    public ModificationOrderHandler() {
        super(MessageType.MODIFICATION, ModificationOrder.class);
    }

    @Override
    protected void handleMessage(ModificationOrder brokerMessage) {
        log.info(brokerMessage.toString());
    }
}
