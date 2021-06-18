package es.um.asio.service.activemq;

import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

@Configuration
@EnableJms
public class JmsConfig {

	@Value("${app.activemq.queue-name:default-queue-name}")
	private String queueName;

	@Bean
	public Topic topic() {
		return new ActiveMQTopic(queueName);
	}
}
