package es.um.asio.service.rdfvalidator.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import es.um.asio.service.domain.Validator;
import es.um.asio.service.exception.InvalidRdfSchemaValidationException;
import es.um.asio.service.model.ModelWrapper;
import es.um.asio.service.rdfvalidator.RdfValidatorService;
import es.um.asio.service.repository.ExternalValidatorRepository;
import es.um.asio.service.repository.ValidatorRepository;
import es.um.asio.service.util.RDFUtil;
import fr.inria.lille.shexjava.schema.Label;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.parsing.GenParser;

@Service
public class RdfValidatorServiceImpl implements RdfValidatorService{
	
	private final Logger logger = LoggerFactory.getLogger(RdfValidatorServiceImpl.class);

	@Autowired
	private ValidatorRepository validatorRepository;
	
	@Autowired
	private ExternalValidatorRepository externalValidatorRepository;
	
	@Value("${app.shape-validator.enabled}")
	private boolean enabled;
	
	@Override
	public void validate(ModelWrapper modelWrapper) {
		
		if (!enabled) {
			return;
		}
		
		if (modelWrapper.getModelType() == null) {
			return;
		}
		
		Validator validator = validatorRepository.findFirstByEntity(modelWrapper.getModelType());
		
		if (validator == null) {
			return;
		}
		
		ShexSchema schema = generateShexSchema(validator.getValidator());
		
		if (schema == null) {
			return;
		}
		
		try {
			externalValidatorRepository.validate(RDFUtil.toString(modelWrapper.getModel()), validator.getValidator(), generateShapeMap(modelWrapper.getModelId(), schema));
		}catch (InvalidRdfSchemaValidationException e) {
			e.setExecutionId(modelWrapper.getExecutionId());
			throw e;
		}
	}
	
	private String generateShapeMap(String modelId, ShexSchema shexSchema) {		
		return Optional.ofNullable(getEntityValidator(shexSchema))
				.map(key -> { 
					return new StringBuilder("<").append(modelId).append(">")
							.append("@")
							.append("<").append(key).append(">")
							.toString();
				})
				.orElse(null);				
	}
	
	private ShexSchema generateShexSchema (String shapeEx) {
		try {
			Path tempFile = Files.createTempFile("shex", ".shex");
			Files.write(tempFile, Arrays.asList(shapeEx), StandardOpenOption.APPEND);
			return GenParser.parseSchema(tempFile);			
		}catch (Exception e) {
			logger.error(String.format("generateShexSchema - Invalid shapEx. %s", shapeEx), e);
		}
		return null;
	}
	
	private String getEntityValidator(ShexSchema shexSchema) {
		try {		
			return ((Label)shexSchema.getRules().keySet().toArray()[0]).stringValue();
		}catch (Exception e) {
			logger.error("getEntityValidator - Unknown error getting schema key", e);
		}
		return null;
	}
}
