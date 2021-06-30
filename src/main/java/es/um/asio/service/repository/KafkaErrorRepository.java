package es.um.asio.service.repository;

import es.um.asio.domain.importer.ImportError;

/**
 * Repository for import error.
 */
public interface KafkaErrorRepository {

	void send(ImportError error);
}
