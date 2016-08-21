package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.solution.message.BrokerMessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.BrokerMessageListener;

/**
 * Handler for processing CancellationOrder messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class CancellationOrderHandler implements MessageHandler<CancellationOrder> {
    
    @Override
    public void handleMessage(BrokerMessageListener brokerMessageListener, BrokerMessageProcessor processor,
            CancellationOrder brokerMessage) {
        brokerMessageListener.onBrokerMessage(brokerMessage);
    }

    @Override
    public Class<CancellationOrder> getMessageClass() {
        return CancellationOrder.class;
    }
}
