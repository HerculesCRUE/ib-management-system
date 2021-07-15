package es.um.asio.service.rdf.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.um.asio.abstractions.constants.Constants;
import es.um.asio.abstractions.domain.ManagementBusEvent;
import es.um.asio.abstractions.domain.Operation;
import es.um.asio.abstractions.perfomance.WatchDog;
import es.um.asio.domain.PojoData;
import es.um.asio.service.model.GeneralBusEvent;
import es.um.asio.service.model.ModelWrapper;
import es.um.asio.service.rdf.RDFPojoBuilderService;
import es.um.asio.service.rdf.RDFPojoLinkBuilderService;
import es.um.asio.service.rdfvalidator.RdfValidatorService;
import es.um.asio.service.uris.URISGeneratorClient;
import es.um.asio.service.util.RDFUtil;

/**
 * The Class RDFPojoBuilderServiceImpl.
 */
@Service
public class RDFPojoBuilderServiceImpl implements RDFPojoBuilderService {
	
	private static final String SPANISH_LANGUAGE_BY_DEFAULT = "es";

	/** The Constant ETL_POJO_CLASS. */
	private static final String ETL_POJO_CLASS = "@class";

	/** The Constant ETL_POJO_ID. */
	private static final String ETL_POJO_ID = "id";


	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(RDFPojoBuilderServiceImpl.class);

	/** The uris generator client. */
	@Autowired
	private URISGeneratorClient urisGeneratorClient;
	
	/** The RDF pojo linking builder service. */
	@Autowired
	private RDFPojoLinkBuilderService rDFPojoLinkingBuilderService;
	
	@Autowired
	private RDFServiceUtils rdfServiceUtils;
			
	private Boolean changeOperation;
	
	@Autowired
	private RdfValidatorService rdfValidatorService;

	/**
	 * Creates the.
	 *
	 * @param input the input
	 * @return the model
	 */
	@Override
	public ManagementBusEvent inkoveBuilder(final GeneralBusEvent<?> input) {
		ManagementBusEvent result = null;
		this.changeOperation = false;
		
		if (input.getData() instanceof PojoData) {
			final ModelWrapper model = this.createRDF(input.retrieveInnerObj());
			
			rdfValidatorService.validate(model);
			
			if (Operation.INSERT.equals(((PojoData) input.getData()).getOperation()) && this.changeOperation) {
				((PojoData) input.getData()).setOperation(Operation.UPDATE);
			}
			
			result = new ManagementBusEvent(model.getModelId(), RDFUtil.toString(model.getModel()),
					StringUtils.EMPTY, this.getClass(input.retrieveInnerObj()), input.retrieveOperation());
		} else {
			result = this.nextBuilder(input);
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
	public ManagementBusEvent nextBuilder(final GeneralBusEvent<?> input) {
		return rDFPojoLinkingBuilderService.inkoveBuilder(input);
	}

	/**
	 * Creates the RDF.
	 *
	 * @param obj the obj
	 * @return the model wrapper
	 */
	@Override
	public ModelWrapper createRDF(final Object obj) {
		WatchDog urisWatchDog = new WatchDog();
		WatchDog createRDFWatchDog = new WatchDog();
		

		final ModelWrapper result = new ModelWrapper();				
		final Model model = ModelFactory.createDefaultModel();
		
		urisWatchDog.reset();
		model.createProperty(urisGeneratorClient.rootUri());
		urisWatchDog.takeTime("rootUri");

		try {
			final LinkedHashMap<String,Object> inputPojo = ((LinkedHashMap<String,Object>) obj);
			
			final String lang = inputPojo.get(Constants.LANG) != null ? inputPojo.get(Constants.LANG).toString() : Constants.SPANISH_LANGUAGE;
			
			// 1. create the resource
			final String className = (String) PropertyUtils.getProperty(obj, RDFPojoBuilderServiceImpl.ETL_POJO_CLASS);
			final String objectId = this.safetyCheck(PropertyUtils.getProperty(obj, RDFPojoBuilderServiceImpl.ETL_POJO_ID));
			
			if (StringUtils.isBlank(objectId)) {
				throw new Exception("Pojo without identity");
			}
			
			urisWatchDog.reset();
			final Map<String, String> resourceMap = urisGeneratorClient.mapResourceID(obj, lang);
			
			final String modelId = resourceMap != null ? (String) resourceMap.get(Constants.CANONICAL_LANGUAGE_URI): null;
			urisWatchDog.takeTime("createResourceID");
			
			if (resourceMap != null && resourceMap.containsKey("similarity")) {
				this.changeOperation = true;
			}
			
			final Resource resourceProperties = model.createResource(modelId);
			
			// 2. create the properties						
			String key = null;
			String value = null;
			for(Map.Entry<String, Object> entry: inputPojo.entrySet()) {
				key = entry.getKey();
				value = entry.getValue() == null ? StringUtils.EMPTY : StringUtils.defaultString(entry.getValue().toString());
				
				if (Constants.EXECUTION_ID.equalsIgnoreCase(key)) {
					result.setExecutionId(value);
				} else if (!RDFPojoBuilderServiceImpl.ETL_POJO_CLASS.equalsIgnoreCase(key)
						&& !Constants.LANG.equalsIgnoreCase(key)) {
					urisWatchDog.reset();
					final Property property = model
							.createProperty(urisGeneratorClient.createPropertyURI(obj, key, lang), key);
					urisWatchDog.takeTime("createPropertyURI");

					// simple property
					resourceProperties.addProperty(property, value, StringUtils.isNotBlank(lang) ? lang.split("-")[0]
							: RDFPojoBuilderServiceImpl.SPANISH_LANGUAGE_BY_DEFAULT);
				}				
			}
			
			// 3. we set the type
			urisWatchDog.reset();
			String modelType = urisGeneratorClient.createResourceTypeURI(className, lang);
			final Resource resourceClass = model.createResource(modelType);
			urisWatchDog.takeTime("createResourceTypeURI");
			
			model.add(resourceProperties, RDF.type, resourceClass);
			result.setModelType(modelType);

			// 4. we build the result model
			result.setModelId(modelId);
			result.setModel(model);						
			
			// 5. print out
			if(this.logger.isDebugEnabled()) {
				this.logger.debug("************************************** TURTLE ************************************************");
				RDFDataMgr.write(System.out, model, Lang.TURTLE);
				this.logger.debug("************************************** RDF_XML ************************************************");
				RDFDataMgr.write(System.out, model, Lang.RDFXML);
				this.logger.debug("************************************** RDF_JSON ************************************************");
				RDFDataMgr.write(System.out, model, Lang.RDFJSON);
				this.logger.debug("************************************** OTHERS ************************************************");
			}
					
		} catch (final Exception e) {
			this.logger.error("Error creating resource from input: " + obj);
			this.logger.error("Error cause " + e.getMessage());
			logger.error("createRDF",e);
			
			// we sent import error to kafka error topic
			this.rdfServiceUtils.sendImportError(e, result.getExecutionId());
		}
		
		createRDFWatchDog.takeTime("createRDF");
		
		// we print the watchdog results
		this.logger.warn("-----------------------------------------------------------------------");
		createRDFWatchDog.printnResults(this.logger);
		urisWatchDog.printnResults(this.logger);
		this.logger.warn("-----------------------------------------------------------------------");
		
		return result;
	}
	
	/**
	 * Gets the class.
	 *
	 * @param obj the obj
	 * @return the class
	 */
	private String getClass(Object obj) {
		String result = StringUtils.EMPTY;
		try {
			result = (String) PropertyUtils.getProperty(obj, RDFPojoBuilderServiceImpl.ETL_POJO_CLASS);
		} catch (Exception e) {
			logger.error("Unknown class in object " + obj.toString());
		} 
		return result;
	}
	
	/**
	 * Safety check.
	 *
	 * @param obj the obj
	 * @return the string
	 */
	private String safetyCheck(Object obj) {
		String result = StringUtils.EMPTY;
		if( obj == null) {
			return result;
		}
		if (obj instanceof Number) {
			return ((Number) obj).toString();
		}
		if(obj instanceof String) {
			return (String) obj;
		}
		
		return result;
	}

}
