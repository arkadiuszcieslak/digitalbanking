package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.solution.message.BrokerMessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Handler for processing PositionOrder messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class PositionOrderHandler implements MessageHandler<PositionOrder> {

    @Override
    public void handleMessage(TransactionEngine transactionEngine, BrokerMessageProcessor processor, PositionOrder brokerMessage) {
        transactionEngine.onBrokerMessage(brokerMessage);
    }

    @Override
    public Class<PositionOrder> getMessageClass() {
        return PositionOrder.class;
    }
}
