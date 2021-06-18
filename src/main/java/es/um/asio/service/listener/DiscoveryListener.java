package es.um.asio.service.listener;

import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import es.um.asio.abstractions.constants.Constants;
import es.um.asio.abstractions.domain.ManagementBusEvent;
import es.um.asio.domain.PojoLinkedToData;
import es.um.asio.service.model.GeneralBusEvent;
import es.um.asio.service.notification.service.NotificationService;
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

	@Autowired
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

	@Autowired
	private NotificationService notificationService;

	private Integer totalItems = 0;

	public DiscoveryListener() {
		super();
		this.totalItems = 0;
	}

	@KafkaListener(id = "discoveryKafkaListenerContainerFactory", topics = "#{'${app.kafka.discovery-action-topic-name}'.split(',')}", autoStartup = "true", containerFactory = "discoveryKafkaListenerContainerFactory", properties = {
			"spring.json.value.default.type:es.um.asio.domain.PojoLinkedToData" })
	public void listen(final PojoLinkedToData message) {

		this.logger.error("Received message: {}", message);

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Received message: {}", message);
		}

		final ManagementBusEvent managementBusEvent = this.rdfService
				.createRDF(new GeneralBusEvent<PojoLinkedToData>(message));

		// we send the element to activeMQ
		this.jmsTemplate.convertAndSend(this.topic, managementBusEvent);

		this.totalItems++;
	}

	@EventListener(condition = "event.listenerId.startsWith('discoveryKafkaListenerContainerFactory-')")
	public void eventHandler(final ListenerContainerIdleEvent event) {
		this.logger.warn("POJO-DISCOVERY No messages received for {} milliseconds", event.getIdleTime());
		this.logger.warn("Total processed items: {}", this.totalItems);

		final MessageListenerContainer listenerPlainContainer = this.kafkaListenerEndpointRegistry
				.getListenerContainer(Constants.DISCOVERY_FACTORY);
		final boolean isPlainRunning = listenerPlainContainer.isRunning();

		if (isPlainRunning && (this.totalItems > 0)) {
			this.notificationService.stopPojoGeneralListener();
			this.notificationService.stopPojoGeneralLinkListener();
			this.notificationService.startDiscoveryLinkListener();
			this.totalItems = 0;
		}
	}
}
