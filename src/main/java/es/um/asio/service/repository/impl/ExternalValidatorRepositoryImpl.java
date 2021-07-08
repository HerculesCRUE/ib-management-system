package es.um.asio.service.repository.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import es.um.asio.service.exception.InvalidRdfSchemaValidationException;
import es.um.asio.service.repository.ExternalValidatorRepository;

@Repository
public class ExternalValidatorRepositoryImpl implements ExternalValidatorRepository {
	
	private final Logger logger = LoggerFactory.getLogger(ExternalValidatorRepositoryImpl.class);

	private static final String SCHEMA_VALIDATION_PATH = "/schema/validate";

	private static final String VALIDATION_RDF_PARAM = "data";
	
	private static final String VALIDATION_RDF_FORMAT_PARAM = "dataFormat";
	
	private static final String VALIDATION_RDF_FORMAT_PARAM_DEFAULT = "rdf/xml";

	private static final String VALIDATION_SCHEMA_PARAM = "schema";

	private static final String VALIDATION_MAP_PARAM = "shapeMap";

	@Value("${app.shape-validator.endpoint}")
	private String validatorBasePath;

	private final RestTemplate restTemplate;

	public ExternalValidatorRepositoryImpl() {
		this.restTemplate = new RestTemplate();
	}

	@Override
	public void validate(String rdf, String shema, String shapeMap) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add(VALIDATION_RDF_PARAM, rdf);
		map.add(VALIDATION_SCHEMA_PARAM, shema);
		map.add(VALIDATION_MAP_PARAM, shapeMap);
		map.add(VALIDATION_RDF_FORMAT_PARAM, VALIDATION_RDF_FORMAT_PARAM_DEFAULT);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		String errors = null;
		try {
			ResponseEntity<String> response = restTemplate.postForEntity(
					new StringBuilder(validatorBasePath).append(SCHEMA_VALIDATION_PATH).toString(), request,
					String.class);

			HttpStatus statusCode = response.getStatusCode();
			if (statusCode == HttpStatus.OK) {
				JSONObject jsonObject = new JSONObject(response.getBody());
				JSONObject result = jsonObject.getJSONArray("shapeMap").getJSONObject(0);
				String shapeMapStatus = result.getString("status");
				if ("nonconformant".equals(shapeMapStatus)) {
					errors = result.toString();
				}
			}
		} catch (Exception e) {
			logger.error(String.format("validate - Unknown error. url: %s", validatorBasePath), e);
		}

		if (errors != null) {
			throw new InvalidRdfSchemaValidationException(errors);
		}
	}
}
