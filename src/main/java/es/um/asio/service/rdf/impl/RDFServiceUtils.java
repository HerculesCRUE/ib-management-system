package es.um.asio.service.rdf.impl;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.um.asio.abstractions.constants.Constants;
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
			final LinkedHashMap<String,Object> inputPojo = ((LinkedHashMap<String,Object>) input);
			if (inputPojo.containsKey(Constants.EXECUTION_ID)) {
				importError.setJobExecutionId(inputPojo.get(Constants.EXECUTION_ID).toString());
				kafkaErrorRepository.send(importError);
			} else {
				logger.error(String.format("sendImportError - Non existing executionId. %s", input));
			}
		} catch (Exception e1) {
			logger.error(String.format("sendImportError - Error unknown generating import error. %s", input), e1);
		}			
	}
}
