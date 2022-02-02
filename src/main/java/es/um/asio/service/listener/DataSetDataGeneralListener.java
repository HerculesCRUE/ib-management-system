package es.um.asio.service.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import es.um.asio.abstractions.domain.ManagementBusEvent;
import es.um.asio.domain.DataSetData;
import es.um.asio.domain.InputData;
import es.um.asio.service.kafka.KafkaService;
import es.um.asio.service.model.GeneralBusEvent;
import es.um.asio.service.rdf.RDFService;

/**
 * General message listener for DataSetData
 */
@Profile("!unit-test")
@Component
public class DataSetDataGeneralListener {
	/**
	 * Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(DataSetDataGeneralListener.class);

	/**
	 * Service to handle message entity related operations
	 */
	@Autowired
	private KafkaService kafkaService;

	@Autowired
	private RDFService rdfService;

	/**
	 * Method listening input topic name
	 * 
	 * @param message
	 */
	@KafkaListener(id = "dataSetDataKafkaListenerContainerFactory", topics = "#{'${app.kafka.general-contingency-topic-name}'.split(',')}", containerFactory = "dataSetDataKafkaListenerContainerFactory")
	public void listen(final InputData<DataSetData> message) {

		// INSERT operation by default

		this.logger.info("Received message: {}", message);

		ManagementBusEvent managementBusEvent = rdfService
				.createRDF(new GeneralBusEvent<InputData<DataSetData>>(message));

		if (managementBusEvent != null) {
			this.kafkaService.send(managementBusEvent);
		}
	}
}
