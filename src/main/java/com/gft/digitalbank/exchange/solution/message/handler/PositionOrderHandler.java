package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.MessageType;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Handler for processing PositionOrder messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class PositionOrderHandler extends AbstractMessageHandler<PositionOrder> {

    /**
     * Constructor.
     * 
     * @param transactionEngine ref to TransactionEngine
     */
    public PositionOrderHandler(TransactionEngine transactionEngine) {
        super(transactionEngine, MessageType.ORDER, PositionOrder.class);
    }

    @Override
    protected void handleMessage(PositionOrder brokerMessage) {
        transactionEngine.onBrokerMessage(brokerMessage);
    }
}
