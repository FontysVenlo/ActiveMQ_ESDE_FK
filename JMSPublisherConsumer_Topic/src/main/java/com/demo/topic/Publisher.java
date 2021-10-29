package com.demo.topic;

import javax.jms.*;

import javafx.scene.paint.Color;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Publisher {

    // Message property keys
    private static String USER = "User";
    private static String TIME = "Time";
    private static String MESSAGE = "Message";

    public static void main(String[] args) {
        ConnectionFactory factory = new ActiveMQConnectionFactory("admin", "admin",
                "tcp://localhost:61616");

        try {
            Connection connection = factory.createConnection();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createTopic("Topo-Remoto-testo");

            MessageProducer producer = session.createProducer(destination);

            Message message = session.createMessage();
            message.setObjectProperty(TIME, returnCurrentLocalDateTimeAsString());
            message.setObjectProperty(USER, "Test-USer");
            message.setObjectProperty(MESSAGE, "Heio");

            producer.send(message);

            System.out.println("Message published to topic");

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private static String returnCurrentLocalDateTimeAsString () {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String timeSent = dateTime.format(format);
        return timeSent;
    }

}
