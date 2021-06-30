package es.um.asio.service.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import es.um.asio.abstractions.domain.ManagementBusEvent;
import es.um.asio.domain.importer.ImportError;
import es.um.asio.service.repository.KafkaErrorRepository;

/**
 * Kafka repository implementation for import errors
 *
 */
@Component
public class KafkaErrorRepositoryImpl implements KafkaErrorRepository {

	 /**
     * Kafka template.
     */
    @Autowired
    private KafkaTemplate<String, ImportError> kafkaErrorTemplate;
    
    /**
     * Topic name
     */
    @Value("${app.kafka.import-error-topic-name}")
    private String importErrorTopicName;
	
	@Override
	public void send(ImportError error) {
		kafkaErrorTemplate.send(importErrorTopicName, error);
	}

}
