package com.gft.digitalbank.exchange.solution.message;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

/**
 * Class processes orders coming from single JMS Broker.
 * 
 * @author Arkadiusz Cieslak
 */
@Log4j
public class BrokerMessageProcessor extends AbstractProcessor {

    /** Destination name */
    @Getter
    private String destinationName;
    
    /** Reference to JSM connection object */
    private Connection connection;
    
    /** Reference to JSM session object */
    private Session session;

    /**
     * Constructor.
     * 
     * @param destinationName name of the destination
     */
    public BrokerMessageProcessor(@NonNull String destinationName) {
        super("BrokerMessageProcessor-" + destinationName);
        
        this.destinationName = destinationName;
    }

    @Override
    protected void doStart() {
        try {
            connection = connectionFactory.createConnection();
            
            connection.start();
            
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Destination     destination = session.createQueue(destinationName);
            MessageConsumer consumer    = session.createConsumer(destination);
            
            consumer.setMessageListener(new OrderMessageListener(transactionEngine, this, executor));
        } catch (JMSException e) {
            log.error("JMSException in method doStart", e);
        }
    }

    @Override
    protected void doStop() {
        try {
            session.close();
            connection.close();
        } catch (JMSException e) {
            log.error("JMSException in method doStop", e);
        }
        
    }
}
