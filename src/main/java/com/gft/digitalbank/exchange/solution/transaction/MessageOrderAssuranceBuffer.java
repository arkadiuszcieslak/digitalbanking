package com.gft.digitalbank.exchange.solution.transaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.gft.digitalbank.exchange.solution.util.SerialExecutor;

/**
 * Buffer to assure message order. It suppose that messages are ordered by id.
 * 
 * @author Arkadiusz Cieslak
 */
public class MessageOrderAssuranceBuffer implements BrokerMessageListener {

    /** Reference to wrapped BrokerMessageListener */
    private final BrokerMessageListener wrappedListener;

    /** Serial executor */
    private final Executor executor;

    /** Index of expected order id */
    private int expectedOrderId = 1;

    /** Buffer for messages used to assure order of messages (map of messages identified by message id) */
    private Map<Integer, BrokerMessage> messageBuffer = Collections.synchronizedMap(new HashMap<>(1024));

    /**
     * Constructor.
     * 
     * @param wrappedListener reference to transaction engine
     * @param executor provided executor
     */
    public MessageOrderAssuranceBuffer(BrokerMessageListener wrappedListener, Executor executor) {
        this.wrappedListener = wrappedListener;
        this.executor = new SerialExecutor(executor);
    }

    @Override
    public void onBrokerMessage(PositionOrder message) {
        onBrokerMessage((BrokerMessage) message);
    }

    @Override
    public void onBrokerMessage(CancellationOrder message) {
        onBrokerMessage((BrokerMessage) message);
    }

    @Override
    public void onBrokerMessage(ModificationOrder message) {
        onBrokerMessage((BrokerMessage) message);
    }

    @Override
    public void onBrokerMessage(final ShutdownNotification message) {
        executor.execute(() -> wrappedListener.onBrokerMessage(message));
    }

    /**
     * Method called when BrokerMessage message arrives. It puts the message in buffer.
     * 
     * @param message PositionOrder message
     */
    private void onBrokerMessage(final BrokerMessage message) {
        messageBuffer.put(message.getId(), message);

        executor.execute(() -> processMessages());
    }

    /**
     * Process messages from buffer.
     */
    private void processMessages() {
        while (messageBuffer.size() > 0) {
            BrokerMessage message = messageBuffer.remove(expectedOrderId);

            if (message != null) {
                if (message instanceof PositionOrder) {
                    wrappedListener.onBrokerMessage((PositionOrder) message);
                } else if (message instanceof CancellationOrder) {
                    wrappedListener.onBrokerMessage((CancellationOrder) message);
                } else if (message instanceof ModificationOrder) {
                    wrappedListener.onBrokerMessage((ModificationOrder) message);
                }

                expectedOrderId++;
            } else {
                break;
            }
        }
    }
}
