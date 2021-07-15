package es.um.asio.service.repository;

public interface ExternalValidatorRepository {

	void validate(String rdf, String schema, String shapeMap);
	
}
