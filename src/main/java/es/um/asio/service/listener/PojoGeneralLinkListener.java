package es.um.asio.service.listener;

import java.util.LinkedHashMap;

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
import es.um.asio.abstractions.domain.Operation;
import es.um.asio.domain.PojoLinkData;
import es.um.asio.service.model.GeneralBusEvent;
import es.um.asio.service.model.ModelWrapper;
import es.um.asio.service.notification.service.NotificationService;
import es.um.asio.service.rdf.RDFPojoLinkBuilderService;
import es.um.asio.service.rdf.RDFService;

/**
 * General message listener for Pojo
 */
@Profile("!unit-test")
@Component
public class PojoGeneralLinkListener {

	/**
	 * Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(PojoGeneralLinkListener.class);

	/** The queue. */
	@Autowired
	private Topic topic;

	/** The jms template. */
	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private RDFService rdfService;

	@Autowired
	private RDFPojoLinkBuilderService rDFPojoLinkingBuilderService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

	private Integer totalItems;

	/**
	 * Instantiates a new pojo general link listener.
	 */
	public PojoGeneralLinkListener() {
		super();
		this.totalItems = 0;
	}

	/**
	 * Method listening input topic name
	 * 
	 * public void listen(ConsumerRecord<?, ?> cr) {
	 * 
	 * @param message
	 */
	@KafkaListener(id = "pojoLinkKafkaListenerContainerFactory", topics = "#{'${app.kafka.general-link-topic-name}'.split(',')}", autoStartup = "false", containerFactory = "pojoLinkKafkaListenerContainerFactory", properties = {
			"spring.json.value.default.type:es.um.asio.domain.PojoLinkData" })
	public void listen(final PojoLinkData message) {

		this.logger.warn("Pojo General Link item {}", message.getData());

		if (this.logger.isDebugEnabled()) {
			this.logger.error("Received message: {}", message);
		}

		final ManagementBusEvent managementBusEvent = this.rdfService
				.createRDF(new GeneralBusEvent<PojoLinkData>(message));

		if (managementBusEvent != null) {
			// we send the element to activeMQ
			this.jmsTemplate.convertAndSend(this.topic, managementBusEvent);

			this.totalItems++;
		}
	}

	@EventListener(condition = "event.listenerId.startsWith('pojoLinkKafkaListenerContainerFactory-')")
	public void eventHandler(final ListenerContainerIdleEvent event) {
		this.logger.warn("POJO-LINK-GENERAL No messages received for {} milliseconds", event.getIdleTime());
		this.logger.warn("Total processed items: {}", this.totalItems);

		final MessageListenerContainer listenerLinkContainer = this.kafkaListenerEndpointRegistry
				.getListenerContainer(Constants.POJO_LINK_FACTORY);
		final boolean isLinkRunning = listenerLinkContainer.isRunning();

		if (isLinkRunning && (this.totalItems > 0)) {
			this.logger.warn("POJO-LINK-GENERAL Send Last Pojo and stop listener");
			try {

				LinkedHashMap<String, Object> message = new LinkedHashMap<>();
				PojoLinkData data = new PojoLinkData(Operation.FINAL, null);

				LinkedHashMap<String, Object> gi = new LinkedHashMap<String, Object>();
				gi.put("id", "1");
				gi.put("@class", "grupoInvestigacion");
				message.put("grupoInvestigacion", gi);
				ModelWrapper model = rDFPojoLinkingBuilderService.createRDF(message);
				data.setData(model);

				final ManagementBusEvent managementBusEvent = this.rdfService
						.createRDF(new GeneralBusEvent<PojoLinkData>(data));
				if (managementBusEvent != null) {
					// we send the element to activeMQ
					this.jmsTemplate.convertAndSend(this.topic, managementBusEvent);

					this.totalItems++;
				}
			} catch (Exception e) {
				this.logger.error("ERROR FINAL COLA KAFKA, error: " + e.getMessage());
			}

			this.notificationService.stopPojoGeneralListener();
			this.notificationService.stopPojoGeneralLinkListener();
			// this.notificationService.stopDiscoveryLinkListener();
			this.totalItems = 0;
		}
	}
}
