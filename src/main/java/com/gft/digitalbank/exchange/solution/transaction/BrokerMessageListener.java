package com.gft.digitalbank.exchange.solution.transaction;

import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;

/**
 * Interface listens for BrokerMessages.
 * 
 * @author Arkadiusz Cieslak
 */
public interface BrokerMessageListener {

    /**
     * Method called when PositionOrder message arrives.
     * 
     * @param message PositionOrder message
     */
    public void onBrokerMessage(final PositionOrder message);

    /**
     * Method called when CancellationOrder message arrives.
     * 
     * @param message CancellationOrder message
     */
    public void onBrokerMessage(final CancellationOrder message);

    /**
     * Method called when ModificationOrder message arrives.
     * 
     * @param message ModificationOrder message
     */
    public void onBrokerMessage(final ModificationOrder message);

    /**
     * Method called when ShutdownNotification message arrives.
     * 
     * @param message ShutdownNotification message
     */
    public void onBrokerMessage(final ShutdownNotification message);
}
