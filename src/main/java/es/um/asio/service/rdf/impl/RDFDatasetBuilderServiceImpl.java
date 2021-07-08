package es.um.asio.service.rdf.impl;

import java.lang.reflect.Field;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.um.asio.abstractions.constants.Constants;
import es.um.asio.abstractions.domain.ManagementBusEvent;
import es.um.asio.domain.InputData;
import es.um.asio.service.model.GeneralBusEvent;
import es.um.asio.service.model.ModelWrapper;
import es.um.asio.service.rdf.RDFDatasetBuilderService;
import es.um.asio.service.rdf.RDFPojoBuilderService;
import es.um.asio.service.rdfvalidator.RdfValidatorService;
import es.um.asio.service.uris.URISGeneratorClient;
import es.um.asio.service.util.RDFUtil;

/**
 * The Class RDFDatasetBuilderServiceImpl.
 */
@Service
public class RDFDatasetBuilderServiceImpl  implements RDFDatasetBuilderService {
	/** Logger. */
	private final Logger logger = LoggerFactory.getLogger(RDFDatasetBuilderServiceImpl.class);

	/** The rdf pojo builder service. */
	@Autowired
	private RDFPojoBuilderService rdfPojoBuilderService;

	@Autowired
	private URISGeneratorClient urisGeneratorClient;
	
	@Autowired
	private RDFServiceUtils rdfServiceUtils;
	
	@Autowired
	private RdfValidatorService rdfValidatorService;
	
	/**
	 * Creates the.
	 *
	 * @param input the input
	 * @return the model
	 */
	public ManagementBusEvent inkoveBuilder(GeneralBusEvent<?> input) {
		ManagementBusEvent result = null;
		if (input.getData() instanceof InputData) {
			ModelWrapper model = this.createRDF(input.retrieveInnerObj());
			rdfValidatorService.validate(model);
			result = new ManagementBusEvent(model.getModelId(), RDFUtil.toString(model.getModel()), StringUtils.EMPTY, input.retrieveInnerObj().getClass().getSimpleName(), input.retrieveOperation());
		} else {
			result = nextBuilder(input);
		}
		
		return result;
	}

	/**
	 * Next builder.
	 *
	 * @param input the input
	 * @return the model
	 */
	@Override
	public ManagementBusEvent nextBuilder(GeneralBusEvent<?> input) {
		return rdfPojoBuilderService.inkoveBuilder(input);
	}

		
	/**
	 * Creates the RDF.
	 *
	 * @param obj the obj
	 * @return the model
	 */
	public ModelWrapper createRDF(Object obj) {
		ModelWrapper result = new ModelWrapper();
		
		Model model = ModelFactory.createDefaultModel();
		model.createProperty(urisGeneratorClient.rootUri());

		try {
			// 1. create the resource
			RDFUtil.skipFields(obj);
			String modelId = urisGeneratorClient.createResourceID(obj);
			
			Resource resourceProperties = model.createResource(modelId);
			
			// 2. only the own fields
			Field[] fields = obj.getClass().getDeclaredFields();
			
			String propertyValue;
			for (Field field : fields) {
										
				propertyValue = StringUtils.isEmpty(BeanUtils.getSimpleProperty(obj, field.getName())) ? StringUtils.EMPTY : BeanUtils.getSimpleProperty(obj, field.getName());							
				
				if (Constants.EXECUTION_ID.equalsIgnoreCase(field.getName())) {
					result.setExecutionId(propertyValue);					
				} else {	
					Property property = model.createProperty(urisGeneratorClient.createPropertyURI(obj, field.getName()), field.getName());
					
					if(StringUtils.EMPTY.equals(propertyValue)) {
						logger.warn("Null property: " + field.getName() + " in object : " + obj.toString());
					}
					
					resourceProperties.addProperty(property, propertyValue);
				}
			}

			// 3. we set the type
			String modelType = urisGeneratorClient.createResourceTypeURI(obj.getClass().getName());
			Resource resourceClass = model.createResource(modelType);
			model.add(resourceProperties, RDF.type, resourceClass);
			result.setModelType(modelType);

			// 4. we build the result model
			result.setModelId(modelId);
			result.setModel(model);
			
			// TODO don't remove these lines they are used for testing
			// logger.info("########## Generated RDF:*************** ");
			// model.write(System.out);
			
		} catch (Exception e) {
			logger.error("Error creating resource from input: " + obj);
			logger.error("createRDF",e);
			
			// we sent import error to kafka error topic
			this.rdfServiceUtils.sendImportError(e, result.getExecutionId());
		}
		
		return result;
	}
}
