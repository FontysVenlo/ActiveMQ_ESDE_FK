package service;

import javax.jms.*;

/**
 * Interface create holds the methods for creating JMS Objects
 */
public interface MQService {

    public Connection createConnection() throws JMSException;
    public Session createSession(Connection connection, int sessionMode) throws JMSException;
    public Topic createTopic(Session session, String topicName) throws JMSException;
    public Destination createDestination(Session session, String topicName) throws JMSException;
    public MessageConsumer createMessageConsumer(Session session, Topic topic, String subscriberName) throws JMSException;
    public MessageProducer createMessageProducer(Session session, Destination destination) throws JMSException;
}