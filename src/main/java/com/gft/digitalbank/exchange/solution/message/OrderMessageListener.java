package com.gft.digitalbank.exchange.solution.message;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.listener.SessionAwareMessageListener;

import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.model.orders.MessageType;
import com.gft.digitalbank.exchange.solution.message.handler.CancellationOrderHandler;
import com.gft.digitalbank.exchange.solution.message.handler.MessageHandler;
import com.gft.digitalbank.exchange.solution.message.handler.ModificationOrderHandler;
import com.gft.digitalbank.exchange.solution.message.handler.PositionOrderHandler;
import com.gft.digitalbank.exchange.solution.message.handler.ShutdownNotificationHandler;
import com.gft.digitalbank.exchange.solution.transaction.TransactionEngine;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;

/**
 * Message listener for processing broker messages.
 * 
 * @author Arkadiusz Cieslak
 */
public class OrderMessageListener implements SessionAwareMessageListener<TextMessage> {

    /** Reference to TransactionEngine */
    private final TransactionEngine transactionEngine;

    private final BrokerMessageProcessor brokerMessageProcessor;

    /** Map of MessageHandlers identified by handled MessageType */
    private static final Map<MessageType, MessageHandler<? extends BrokerMessage>> MESSAGE_HANDLERS = new HashMap<>();

    static {
        MESSAGE_HANDLERS.put(MessageType.CANCEL, new CancellationOrderHandler());
        MESSAGE_HANDLERS.put(MessageType.MODIFICATION, new ModificationOrderHandler());
        MESSAGE_HANDLERS.put(MessageType.ORDER, new PositionOrderHandler());
        MESSAGE_HANDLERS.put(MessageType.SHUTDOWN_NOTIFICATION, new ShutdownNotificationHandler());
    }

    /**
     * Constructor.
     */
    public OrderMessageListener(TransactionEngine transactionEngine, BrokerMessageProcessor processor) {
        this.transactionEngine = transactionEngine;
        this.brokerMessageProcessor = processor;
    }

    @Override
    public void onMessage(TextMessage message, Session session) throws JMSException {
        MessageType mt = MessageType.valueOf(message.getStringProperty("messageType"));
        Preconditions.checkNotNull(mt, "MessageType is null");

        @SuppressWarnings("unchecked")
        MessageHandler<BrokerMessage> handler = (MessageHandler<BrokerMessage>) MESSAGE_HANDLERS.get(mt);
        Preconditions.checkNotNull(handler, "MessageHandler is null");

        BrokerMessage bm = deserializeBrokerMessage(message.getText(), handler.getMessageClass());
        Preconditions.checkNotNull(bm, "BrokerMessage is null");

        handler.handleMessage(transactionEngine, brokerMessageProcessor, bm);
    }

    private <T extends BrokerMessage> T deserializeBrokerMessage(String serializedObj, Class<T> messageClass) {
        return new Gson().fromJson(serializedObj, messageClass);
    }
}
