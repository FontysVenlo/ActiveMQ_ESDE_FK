package com.demo.requestor;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jms.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Class which represents the request application. The request application sends info to the responder and expects an answer
 */
public class Requestor implements MessageListener {

    private ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private final CountDownLatch done = new CountDownLatch(10);

    /**
     * Sets up the JMS objects and start the connection
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        this.factory = new ActiveMQConnectionFactory("admin", "admin",
                "tcp://localhost:61616");
        this.connection = this.factory.createConnection();
        this.connection.start();
        this.session = this.connection.createSession(
                false, Session.AUTO_ACKNOWLEDGE);
        this.destination = this.session.createQueue("REQUEST.QUEUE");
    }

    /**
     * Send the request messages
     *
     * @throws Exception
     */
    public void run() throws Exception {
        // create temporary response queue to read the responses
        TemporaryQueue temporaryResponseQueue = this.session.createTemporaryQueue();
        // create producer to send the messages
        MessageProducer requester =
                this.session.createProducer(this.destination);
        // create message listener using the temp queue
        MessageConsumer responseListener =
                this.session.createConsumer(temporaryResponseQueue);
        responseListener.setMessageListener(this);

        // send 10 messages
        for (int i = 0; i < 10; i++) {
            TextMessage requestMessage =
                    this.session.createTextMessage("I request you to complete this job: " + RandomStringUtils.randomAlphanumeric(8));
            requestMessage.setJMSReplyTo(temporaryResponseQueue);
            requestMessage.setJMSCorrelationID("REQUEST: " + i);
            requester.send(requestMessage);
        }
        // shut down everything if everything is received
        if (this.done.await(10, TimeUnit.MINUTES)) {
            // send the shutdown message to the responder
            TextMessage messageToShutDown = this.session.createTextMessage("Shutdown my friend");
            messageToShutDown.setJMSReplyTo(temporaryResponseQueue);
            messageToShutDown.setJMSCorrelationID("REQUEST: Shutdown");
            requester.send(messageToShutDown);

            System.out.println("My job is done I am shutting down");
            // shut down the resources
            requester.close();
            responseListener.close();
            this.session.close();
            this.connection.close();
        }
    }

    /**
     * Implementation of the listener method
     *
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        try {
            // get the jms correlation id
            String jmsCorrelation = message.getJMSCorrelationID();
            if (!jmsCorrelation.startsWith("REQUEST")) {
                System.out.println("Received an unexpected response: " + jmsCorrelation);
            }
            TextMessage txtResponse = (TextMessage) message;
            System.out.println(txtResponse.getText());
            this.done.countDown();
        } catch (JMSException e) {
            System.out.println("Error: " + e.getStackTrace());
        }

    }
}
