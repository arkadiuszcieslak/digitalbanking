package com.gft.digitalbank.exchange.solution.message.handler;

import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.solution.message.BrokerMessageProcessor;
import com.gft.digitalbank.exchange.solution.transaction.BrokerMessageListener;

/**
 * Handler for processing ModificationOrder messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class ModificationOrderHandler implements MessageHandler<ModificationOrder> {
    
    @Override
    public void handleMessage(BrokerMessageListener brokerMessageListener, BrokerMessageProcessor processor,
            ModificationOrder brokerMessage) {
        brokerMessageListener.onBrokerMessage(brokerMessage);
    }

    @Override
    public Class<ModificationOrder> getMessageClass() {
        return ModificationOrder.class;
    }
}
