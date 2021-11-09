package service;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import utils.BrokerUtils;

import javax.jms.*;

/**
 * Concrete implementation of {@link MQService}
 */
public class ActiveMQService implements MQService {

    /**
     * 1. Implement the ActiveMQService methods which create JMS Objects that are need for sending and receiving messages
     */


    /**
     * 1.1 Create a new connection using a ConnectionFactory - Hint: Two lines of code
     */
    /**
     * Creates a JMS {@link Connection} using the fields of the {@link BrokerUtils} and a JMS {@link ConnectionFactory} of type
     * {@link ActiveMQConnectionFactory} in order to create a connection to the given ActiveMQ broker
     *
     * @return a JMS {@link Connection}
     * @throws JMSException
     */
    @Override
    public Connection createConnection() throws JMSException {
        // 1.1.1 create a ConnectionFactory of type ActiveMQConnectionFactory using the QUEUE_USERNAME, QUEUE_PASSWORD and QUEUE_LOCATION
        // fields in the BrokerUtils class
        ConnectionFactory factory = new ActiveMQConnectionFactory(BrokerUtils.QUEUE_USERNAME, BrokerUtils.QUEUE_PASSWORD,
                BrokerUtils.QUEUE_LOCATION);
        // 1.1.2 return a connection object from this factory
        return factory.createConnection();
    }

    /**
     * 1.2 Create a new Session using the given connection and the session mode - Hint: One line of code
     */

    /**
     * Creates a JMS {@link Session} using the passed connection and session mode
     *
     * @param connection  JMS Connection
     * @param sessionMode
     * @return
     * @throws JMSException
     */
    @Override
    public Session createSession(Connection connection, int sessionMode) throws JMSException {
        // 1.2.1 Create the session using the given parameters
        return connection.createSession(false, sessionMode);
    }


    /**
     * 1.3 Create a new Topic using the given connection and the topic name - Hint: One line of code
     */

    /**
     * Create a JMS {@link Topic} object using the passed JMS {@link Session} and topic name
     *
     * @param session   {@link Session}
     * @param topicName the topic name
     * @return a {@link Topic} object
     * @throws JMSException
     */
    @Override
    public Topic createTopic(Session session, String topicName) throws JMSException {
        // 1.3.1 create a new topic
        return session.createTopic(topicName);
    }


    /**
     * 1.4 Create a new Destination using the given connection and the topic name - Hint: One line of code - same as line 73
     */

    /**
     * Creates a new JMS {@link Destination} using the passed JMS {@link Session} and topic name
     *
     * @param session   {@link Session}
     * @param topicName the topic name
     * @return JMS {@link Destination}
     * @throws JMSException
     */
    @Override
    public Destination createDestination(Session session, String topicName) throws JMSException {
        // 1.4.1 create a destination
        return session.createTopic(topicName);
    }


    /**
     * 1.5 Create a new MessageConsumer using the given session, topic and subscriber name - Hint: One line of code
     */

    /**
     * Create a {@link MessageConsumer} using the given {@link Session} and {@link Topic} and a subscriber name
     *
     * @param session        {@link Session}
     * @param topic          {@link Topic} object
     * @param subscriberName String subscriber name
     * @return a new {@link MessageConsumer}
     * @throws JMSException
     */
    @Override
    public MessageConsumer createMessageConsumer(Session session, Topic topic, String subscriberName) throws JMSException {
        // 1.5.1 create a message consumer using the session's create durable subscriber method
        return session.createDurableSubscriber(topic, subscriberName);
    }

    /**
     * 1.6 Create a new MessageProducer using the given session and destination - Hint: One line of code
     */


    /**
     * Creates a {@link MessageProducer} which is used to send {@link Message} using
     * the passed {@link Session} and {@link Destination}
     *
     * @param session     {@link Session}
     * @param destination JMS {@link Destination}
     * @return a new {@link MessageProducer}
     * @throws JMSException
     */
    @Override
    public MessageProducer createMessageProducer(Session session, Destination destination) throws JMSException {
        // 1.6.1 create a producer
        return session.createProducer(destination);
    }

    /**
     * Create AdvisoryDestination for a specific topic
     *
     * @param session     {@link Session}
     * @param topicName the topic name
     * @return an advisory destination {@link Destination}
     * @throws JMSException
     */
    @Override
    public Destination createAdvisoryDestination(Session session, String topicName) throws JMSException {
        Destination monitored = session.createTopic(topicName);

        return session.createTopic(
                AdvisorySupport.getConsumerAdvisoryTopic(monitored).getPhysicalName() + "," +
                        AdvisorySupport.getProducerAdvisoryTopic(monitored).getPhysicalName());
    }

    /**
     * Creates an advisory Message Consumer for a specific topic
     *
     * @param session     {@link Session}
     * @param destination {@link Destination}
     * @return an advisory message consumer
     * @throws JMSException
     */
    @Override
    public MessageConsumer createAdvisoryMessageConsumer(Session session, Destination destination) throws JMSException {
        return session.createConsumer(destination);
    }


}
