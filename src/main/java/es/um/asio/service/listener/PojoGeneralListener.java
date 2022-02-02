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
import es.um.asio.domain.PojoData;
import es.um.asio.service.model.GeneralBusEvent;
import es.um.asio.service.notification.service.NotificationService;
import es.um.asio.service.rdf.RDFService;

/**
 * General message listener for Pojo
 */
@Profile("!unit-test")
@Component
public class PojoGeneralListener {

	/**
	 * Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(PojoGeneralListener.class);

	/** The topic. */
	@Autowired
	private Topic topic;

	/** The jms template. */
	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private RDFService rdfService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

	private Integer totalItems = 0;

	/**
	 * Instantiates a new pojo general listener.
	 */
	public PojoGeneralListener() {
		super();
		this.totalItems = 0;
	}

	/**
	 * Method listening input topic name
	 *
	 * @param message
	 */
	@KafkaListener(id = "pojoKafkaListenerContainerFactory", topics = "#{'${app.kafka.general-topic-name}'.split(',')}", autoStartup = "false", containerFactory = "pojoKafkaListenerContainerFactory", properties = {
			"spring.json.value.default.type:es.um.asio.domain.PojoData" })
	public void listen(final PojoData message) {
		// don't remove this line public void listen(ConsumerRecord<?, ?> cr)

		this.logger.info("Received message: {}", message);

		final ManagementBusEvent managementBusEvent = this.rdfService.createRDF(new GeneralBusEvent<PojoData>(message));

		if (managementBusEvent != null) {
			logger.info("RDF creado " + managementBusEvent.toString());
			// we send the element to activeMQ
			try {
				this.jmsTemplate.convertAndSend(this.topic, managementBusEvent);
			} catch (Exception e) {
				logger.error("convertAndSend error:" + e.getMessage());
			}

			this.totalItems++;
		}
	}

	@EventListener(condition = "event.listenerId.startsWith('pojoKafkaListenerContainerFactory-')")
	public void eventHandler(final ListenerContainerIdleEvent event) {
		this.logger.warn("POJO-GENERAL No messages received for {} milliseconds", event.getIdleTime());
		this.logger.warn("Total processed items: {}", this.totalItems);

		final MessageListenerContainer listenerPlainContainer = this.kafkaListenerEndpointRegistry
				.getListenerContainer(Constants.POJO_FACTORY);
		final boolean isPlainRunning = listenerPlainContainer.isRunning();

		if (isPlainRunning && (this.totalItems > 0)) {
			this.notificationService.stopPojoGeneralListener();
			// this.notificationService.stopDiscoveryLinkListener();
			this.notificationService.startPojoGeneralLinkListener();
			this.totalItems = 0;
		}
	}
}
