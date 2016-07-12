package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.MessageType;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Handler for processing CancellationOrder messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class CancellationOrderHandler extends AbstractMessageHandler<CancellationOrder> {
    
    /**
     * Constructor.
     * 
     * @param transactionEngine ref to TransactionEngine
     */
    public CancellationOrderHandler(TransactionEngine transactionEngine) {
        super(transactionEngine, MessageType.CANCEL, CancellationOrder.class);
    }

    @Override
    protected void handleMessage(CancellationOrder brokerMessage) {
        TransactionEngine.getInstance().onBrokerMessage(brokerMessage);
    }
}
