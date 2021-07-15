package es.um.asio.service.rdf.impl;

import org.apache.commons.lang3.StringUtils;
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

	public void sendImportError(Exception error, String executionId) {
		sendImportError(error.getMessage(), executionId);
	}

	/**
	 * Method to send the error to kafka error topic
	 * 
	 * @param e
	 * @param input
	 */
	public void sendImportError(String error, String executionId) {
		if (StringUtils.isBlank(executionId)) {
			logger.error(String.format("sendImportError - Non existing executionId."));
			return;
		}

		try {
			ImportError importError = new ImportError();
			importError.setDescription(error);
			importError.setJobExecutionId(executionId);
			kafkaErrorRepository.send(importError);
		} catch (Exception e) {
			logger.error(
					String.format("sendImportError - Error unknown generating import error. executionId: %s, error: %s",
							executionId, error), e);
		}
	}
}
