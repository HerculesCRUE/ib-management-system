package es.um.asio.service.repository;

import org.apache.jena.rdf.model.Model;

import es.um.asio.abstractions.domain.ManagementBusEvent;



/**
 * Repository for messages.
 */
public interface KafkaRepository {
    
    void send(ManagementBusEvent<Model> message);
}
