package es.um.asio.service.listener;

import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import es.um.asio.abstractions.domain.ManagementBusEvent;
import es.um.asio.domain.PojoLinkedToData;
import es.um.asio.service.model.GeneralBusEvent;
import es.um.asio.service.rdf.RDFService;

@Profile("!unit-test")
@Component
public class DiscoveryListener {

	/**
	 * Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(DiscoveryListener.class);

	@Autowired
	private Topic topic;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private RDFService rdfService;

	public DiscoveryListener() {
		super();
	}

	@KafkaListener(id = "discoveryKafkaListenerContainerFactory", topics = "#{'${app.kafka.discovery-action-topic-name}'.split(',')}", autoStartup = "true", containerFactory = "discoveryKafkaListenerContainerFactory", properties = {
			"spring.json.value.default.type:es.um.asio.domain.PojoLinkedToData" })
	public void listen(final PojoLinkedToData message) {

		this.logger.error("Received message: {}", message);

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Received message: {}", message);
		}

		GeneralBusEvent<PojoLinkedToData> a = new GeneralBusEvent<PojoLinkedToData>(message);

		final ManagementBusEvent managementBusEvent = this.rdfService
				.createRDF(new GeneralBusEvent<PojoLinkedToData>(message));

		if (managementBusEvent != null) {
			// we send the element to activeMQ
			this.jmsTemplate.convertAndSend(this.topic, managementBusEvent);
		}
	}
}
