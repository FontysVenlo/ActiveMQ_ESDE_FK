package com.demo.topic;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Random;

public class Consumer {

	public static void main(String[] args) {
		ConnectionFactory factory = new ActiveMQConnectionFactory("admin", "admin", 
				"tcp://192.168.0.60:61616");
		
		try {
			Random rand = new Random();
			Connection connection = factory.createConnection();
			connection.setClientID(String.valueOf(rand.nextInt(100)));
			connection.start();
			
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			Topic topic = session.createTopic("Topo-Remoto-testo");
			
			MessageConsumer consumer = session.createDurableSubscriber(topic, "Consumer-" + rand.nextInt(200));
			consumer.setMessageListener(new MessageListener() {
				
				public void onMessage(Message message) {
					TextMessage textMessage = (TextMessage) message;
					try {
						System.out.println(textMessage.getText());
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
