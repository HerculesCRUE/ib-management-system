package es.um.asio.service.rdf.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
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
import es.um.asio.abstractions.perfomance.WatchDog;
import es.um.asio.domain.PojoLinkedToData;
import es.um.asio.service.model.GeneralBusEvent;
import es.um.asio.service.model.ModelWrapper;
import es.um.asio.service.rdf.RDFDiscoveryService;
import es.um.asio.service.uris.URISGeneratorClient;
import es.um.asio.service.util.RDFUtil;

@Service
public class RDFDiscoveryServiceImpl implements RDFDiscoveryService {
	
	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(RDFDiscoveryServiceImpl.class);
	
	private static final String SPANISH_LANGUAGE_BY_DEFAULT = "es";

	/** The Constant ETL_POJO_CLASS. */
	private static final String ETL_POJO_CLASS = "className";

	/** The Constant ETL_POJO_ID. */
	private static final String ETL_POJO_ID = "entityId";
	
	/** The uris generator client. */
	@Autowired
	private URISGeneratorClient urisGeneratorClient;

	@Override
	public ManagementBusEvent inkoveBuilder(GeneralBusEvent<?> input) {
		ManagementBusEvent result = null;
		
		if (input.getData() instanceof PojoLinkedToData) {
			final ModelWrapper model = this.createRDF(input.retrieveInnerObj("linkedTo"));
			
			result = new ManagementBusEvent(model.getModelId(), RDFUtil.toString(model.getModel()), model.getLinkedModel(), 
					this.getClass(model.getLinkedModel()), input.retrieveOperation());
		} else {
			result = this.nextBuilder(input);
		}

		return result;
	}

	@Override
	public ManagementBusEvent nextBuilder(GeneralBusEvent<?> input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModelWrapper createRDF(Object obj) {
		WatchDog urisWatchDog = new WatchDog();
		WatchDog createRDFWatchDog = new WatchDog();
		
		final ModelWrapper result = new ModelWrapper();
		final Model model = ModelFactory.createDefaultModel();

		try {
			final String className = (String) PropertyUtils.getProperty(obj, RDFDiscoveryServiceImpl.ETL_POJO_CLASS);
			
			// model ID
			LinkedHashMap<String, Object> linkedObj = (LinkedHashMap<String, Object>) obj;			
			String objectId = this.safetyCheck(linkedObj.get(RDFDiscoveryServiceImpl.ETL_POJO_ID));
			
			linkedObj.put("@class", className);
			
			urisWatchDog.reset();
			final String modelId = urisGeneratorClient.createResourceID(obj);
			urisWatchDog.takeTime("createResourceID");
			
			// 1. create the resource
			
			final Resource resourceProperties = model.createResource(modelId);
			
			// 2. create the properties
						
			String key = null;
			String value = null;
			
			LinkedHashMap<String, Object> attributes = (LinkedHashMap<String, Object>) linkedObj.get("attributes");
			
			for(Map.Entry<String, Object> entry: attributes.entrySet()) {
				key = entry.getKey();
				
				// we skip the clase field
				if (!RDFDiscoveryServiceImpl.ETL_POJO_CLASS.equalsIgnoreCase(key)) {
					urisWatchDog.reset();
					final Property property = model.createProperty(urisGeneratorClient.createPropertyURI(obj, key), key);
					urisWatchDog.takeTime("createPropertyURI");
					
					// simple property						
					value = entry.getValue() == null ? StringUtils.EMPTY : StringUtils.defaultString(entry.getValue().toString());
					resourceProperties.addProperty(property, value, RDFDiscoveryServiceImpl.SPANISH_LANGUAGE_BY_DEFAULT);
				}
			}
			
			// 3. we set the type
			urisWatchDog.reset();
			final Resource resourceClass = model.createResource(urisGeneratorClient.createResourceTypeURI(className));
			urisWatchDog.takeTime("createResourceTypeURI");
			
			model.add(resourceProperties, RDF.type, resourceClass);
			
			
			result.setModelId(objectId);
			result.setModel(model);
			
			// nested object
			result.setLinkedModel(linkedObj);

		} catch (Exception e) {
			this.logger.error("Error creating resource from linking input: " + obj);
			this.logger.error("Error cause " + e.getMessage());
			logger.error("createRDF",e);
		}
		
		createRDFWatchDog.takeTime("createRDF");
		
		// we print the watchdog results
		this.logger.warn("-----------------------------------------------------------------------");
		createRDFWatchDog.printnResults(this.logger);
		urisWatchDog.printnResults(this.logger);
		this.logger.warn("-----------------------------------------------------------------------");

		return result;
	}

	private String getClass(Object obj) {
		String result = StringUtils.EMPTY;
		try {
			result = (String) PropertyUtils.getProperty(obj, RDFDiscoveryServiceImpl.ETL_POJO_CLASS);
		} catch (Exception e) {
			logger.error("Unknown class in object " + obj.toString());
		} 
		return result;
	}
	
	private String safetyCheck(Object obj) {
		String result = StringUtils.EMPTY;
		
		if( obj == null) {
			return result;
		} else if (obj instanceof Number) {
			return ((Number) obj).toString();
		} else if(obj instanceof String) {
			return (String) obj;
		}
		
		return result;
	}
}
