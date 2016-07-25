package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Handler for processing ModificationOrder messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class ModificationOrderHandler implements MessageHandler<ModificationOrder> {
    
    @Override
    public void handleMessage(TransactionEngine transactionEngine, ModificationOrder brokerMessage) {
        transactionEngine.onBrokerMessage(brokerMessage);
    }

    @Override
    public Class<ModificationOrder> getMessageClass() {
        return ModificationOrder.class;
    }
}
