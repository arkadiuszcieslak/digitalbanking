package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.MessageType;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Handler for processing ModificationOrder messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class ModificationOrderHandler extends AbstractMessageHandler<ModificationOrder> {
    
    /**
     * Constructor.
     * 
     * @param transactionEngine ref to TransactionEngine
     */
    public ModificationOrderHandler(TransactionEngine transactionEngine) {
        super(transactionEngine, MessageType.MODIFICATION, ModificationOrder.class);
    }

    @Override
    protected void handleMessage(ModificationOrder brokerMessage) {
        TransactionEngine.getInstance().onBrokerMessage(brokerMessage);
    }
}
