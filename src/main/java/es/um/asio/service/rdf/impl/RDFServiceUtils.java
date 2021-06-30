package es.um.asio.service.rdf.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.um.asio.domain.importer.ImportError;
import es.um.asio.service.repository.KafkaErrorRepository;

@Component
public class RDFServiceUtils {
	
	/** Logger. */
	private final Logger logger = LoggerFactory.getLogger(RDFServiceUtils.class);
	
	@Autowired
	private KafkaErrorRepository kafkaErrorRepository;

	/**
	 * Method to send the error to kafka error topic
	 * @param e
	 * @param input
	 */
	public void sendImportError(Exception e, Object input) {
		ImportError importError = new ImportError();
		importError.setDescription(e.getMessage());
		
		try {
			//TODO retrieve executionId from input object. The ETL should add executionId field.
			
			// importError.setJobExecutionId((String) PropertyUtils.getProperty(input, "executionId"));
		} catch (Exception e1) {
			this.logger.error("Missing executionId field in {}", input);
		} 
		
		// we send importError object to 'import-error' topic
		this.kafkaErrorRepository.send(importError);
	}
}
