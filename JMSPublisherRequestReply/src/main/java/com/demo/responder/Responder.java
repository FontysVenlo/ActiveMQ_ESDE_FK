package com.demo.responder;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.concurrent.TimeUnit;

/**
 * Class which represents the responder application. The responder receives a message and sends a response to the arrived destination
 */
public class Responder implements MessageListener {

    private ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer requestListener;
    private MessageProducer responder;

    /**
     * Sets up the JMS objects and start the connection
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        this.factory = new ActiveMQConnectionFactory("admin", "admin",
                "tcp://localhost:61616");
        this.connection = this.factory.createConnection();
        this.session = this.connection.createSession(
                false, Session.AUTO_ACKNOWLEDGE);
        this.destination = this.session.createQueue("REQUEST.QUEUE");
        this.responder = this.session.createProducer(null);
        this.requestListener = this.session.createConsumer(destination);
        this.requestListener.setMessageListener(this);
        this.connection.start();
    }

    /**
     * Shuts down the resources
     *
     * @throws JMSException
     */
    private void shutDown() throws JMSException {
        this.requestListener.close();
        this.responder.close();
        this.session.close();
        this.connection.close();
    }

    /**
     * Implementation of the listener onMessage method
     *
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        try {
            // retrieve the destination that the responder must reply to
            Destination respondTo = message.getJMSReplyTo();
            if (respondTo != null) {
                // receive the message and print it
                TextMessage textMessage = (TextMessage) message;
                System.out.println(textMessage.getText());

                // if it receives the shutdown message, close the resources
                if (textMessage.getText().contains("Shutdown my")) {
                    textMessage.acknowledge();
                    // shut down the system
                    shutDown();
                    return;
                }

                if (textMessage.getText().contains(":")) {
                    String[] splitMessage = textMessage.getText().split(":");
                    String job = splitMessage[1];
                    // Create response message
                    Message response =
                            this.session.createTextMessage("Message was received and job: " + job + " was completed");
                    // set the correlation id of the response message
                    response.setJMSCorrelationID(
                            message.getJMSCorrelationID());
                    // send the message
                    this.responder.send(respondTo, response);
                }
            }
        } catch (JMSException e) {
            System.out.println("Error: " + e.getStackTrace());
        }
    }
}
