package com.gft.digitalbank.exchange.solution.message.handler;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.listener.SessionAwareMessageListener;

import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.model.orders.MessageType;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import lombok.Getter;

/**
 * Abstract class for handling specific broker messages of type T.
 * Implementations of this class should be stateless and can be used in multiple
 * threads.
 * 
 * @author Arkadiusz Cieslak
 */
public abstract class AbstractMessageHandler<T extends BrokerMessage> implements SessionAwareMessageListener<TextMessage> {

    /** Accepted message type */
    @Getter
    private MessageType acceptedMessageType;

    /** Class of accepted message type */
    private final Class<T> messageClass;

    /**
     * Handles brokerMessage.
     * 
     * @param brokerMessage
     *            message to handle
     */
    protected abstract void handleMessage(T brokerMessage);

    /**
     * Constructor.
     * 
     * @param acceptedMessageType
     *            accepted message type
     */
    public AbstractMessageHandler(MessageType acceptedMessageType, Class<T> messageClass) {
        this.acceptedMessageType = acceptedMessageType;
        this.messageClass = messageClass;
    }

    /**
     * Method for deserialization o object T from Json string representation.
     * 
     * @param serializedObj
     *            string representation of object T
     * 
     * @return deserialized object of class T
     */
    protected T deserializeBrokerMessage(String serializedObj) {
        return new Gson().fromJson(serializedObj, messageClass);
    }

    @Override
    public void onMessage(TextMessage message, Session session) throws JMSException {
        T bm = deserializeBrokerMessage(message.getText());

        Preconditions.checkNotNull(bm, "BrokerMessage is null");
        Preconditions.checkState(bm.getMessageType() == acceptedMessageType, "onMessage: Invalid message type in message id = %d (expected: %s, present: %s)", bm.getId(),
                acceptedMessageType, bm.getMessageType());
        
        handleMessage(bm);
        message.acknowledge();
    }
}
