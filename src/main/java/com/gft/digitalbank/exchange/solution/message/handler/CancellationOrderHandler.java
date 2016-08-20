package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.solution.message.BrokerMessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;

/**
 * Handler for processing CancellationOrder messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class CancellationOrderHandler implements MessageHandler<CancellationOrder> {
    
    @Override
    public void handleMessage(TransactionEngine transactionEngine, BrokerMessageProcessor processor, CancellationOrder brokerMessage) {
        transactionEngine.onBrokerMessage(brokerMessage);
    }

    @Override
    public Class<CancellationOrder> getMessageClass() {
        return CancellationOrder.class;
    }
}
