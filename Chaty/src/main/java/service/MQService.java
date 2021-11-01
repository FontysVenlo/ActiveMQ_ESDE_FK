package service;

import javax.jms.*;

/**
 * Interface create holds the methods for creating JMS Objects
 */
public interface MQService {

     Connection createConnection() throws JMSException;
     Session createSession(Connection connection, int sessionMode) throws JMSException;
     Topic createTopic(Session session, String topicName) throws JMSException;
     Destination createDestination(Session session, String topicName) throws JMSException;
     MessageConsumer createMessageConsumer(Session session, Topic topic, String subscriberName) throws JMSException;
     MessageProducer createMessageProducer(Session session, Destination destination) throws JMSException;

     Destination createAdvisoryDestination(Session session,String topicName) throws JMSException;
     MessageConsumer createAdvisoryMessageConsumer(Session session, Destination destination) throws JMSException;
}
