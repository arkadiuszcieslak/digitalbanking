package com.gft.digitalbank.exchange.solution.util;

import java.util.concurrent.atomic.AtomicInteger;

import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.Transaction;
import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;

/**
 * Class contains utils methods for messages.
 * 
 * @author Arkadiusz Cieslak
 */
public final class MessageUtils {

    /**
     * Private constructor.
     */
    private MessageUtils() {
    }

    /**
     * Method checks if both messages have the same broker.
     * 
     * @param m1 first message
     * @param m2 second message
     * 
     * @return true if m1 != null && m2 != null && m1.broker != null && m1.broker.equals( m2.broker)
     */
    public static boolean sameBroker(BrokerMessage m1, BrokerMessage m2) {
        if (m1 == null || m2 == null) {
            return false;
        }

        if (m1.getBroker() == null || m2.getBroker() == null) {
            return false;
        }

        return m1.getBroker().equals(m2.getBroker());
    }

    /**
     * Method tries to create transaction based on buy and sell order.
     * 
     * @param idGenerator transaction id generator
     * @param buyOrder buy order
     * @param sellOrder sell order
     * 
     * @return new transaction or null if buy and sell orders didn't match for transaction
     */
    public static Transaction tryCreateTransaction(AtomicInteger idGenerator, PositionOrder buyOrder, PositionOrder sellOrder) {
        if (buyOrder == null || sellOrder == null) {
            return null;
        }

        OrderDetails buyDetails = buyOrder.getDetails();
        OrderDetails sellDetails = sellOrder.getDetails();

        if (buyDetails == null || sellDetails == null) {
            return null;
        }

        if (buyDetails.getPrice() < sellDetails.getPrice()) {
            return null;
        }

        int price = buyOrder.getTimestamp() <= sellOrder.getTimestamp() ? buyDetails.getPrice() : sellDetails.getPrice();
        int amount = Integer.min(buyDetails.getAmount(), sellDetails.getAmount());

        return Transaction.builder().id(idGenerator.incrementAndGet())
                .amount(amount).brokerBuy(buyOrder.getBroker()).brokerSell(sellOrder.getBroker()).clientBuy(buyOrder.getClient())
                .clientSell(sellOrder.getClient()).price(price).product(buyOrder.getProduct()).build();
    }

    /**
     * Returns new PositionOrder based on order object with new details.
     * 
     * @param order order as base for new PositionOrder object
     * @param newDetails order new details
     * 
     * @return new order object with new details
     */
    public static PositionOrder modifyPositionOrderDetails(final PositionOrder order, final ModificationOrder modificationOrder) {
        if (order == null || !MessageUtils.sameBroker(order, modificationOrder)) {
            return null;
        }

        return PositionOrder.builder().id(order.getId()).broker(order.getBroker()).client(order.getClient()).product(order.getProduct())
                .side(order.getSide()).timestamp(modificationOrder.getTimestamp()).details(modificationOrder.getDetails()).build();
    }

    /**
     * Method subtracts given amount from order and returns new PositionOrder if amount > 0.
     * 
     * @param order order to modify
     * @param subtractAmount amount to subtract
     * 
     * @return new PositionOrder (if amount after subtraction id greater than 0) or null (if amount after subtraction is 0 or less).
     */
    public static PositionOrder modifyPositionOrderAmount(PositionOrder order, int subtractAmount) {
        if (order == null || order.getDetails() == null) {
            return null;
        }

        if (order.getDetails().getAmount() <= subtractAmount) {
            return null;
        }

        return PositionOrder.builder().id(order.getId()).broker(order.getBroker()).client(order.getClient()).product(order.getProduct())
                .side(order.getSide()).timestamp(order.getTimestamp()).details(OrderDetails.builder()
                        .amount(order.getDetails().getAmount() - subtractAmount).price(order.getDetails().getPrice()).build())
                .build();

    }
}
