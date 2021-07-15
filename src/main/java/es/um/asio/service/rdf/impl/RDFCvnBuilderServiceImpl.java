package es.um.asio.service.rdf.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import es.um.asio.domain.cvn.CvnBean;
import es.um.asio.domain.cvn.CvnRootBean;
import es.um.asio.service.model.GeneralBusEvent;
import es.um.asio.service.model.ModelWrapper;
import es.um.asio.service.rdf.RDFCvnBuilderService;
import es.um.asio.service.rdf.RDFDatasetBuilderService;
import es.um.asio.service.rdfvalidator.RdfValidatorService;
import es.um.asio.service.uris.URISGeneratorClient;
import es.um.asio.service.util.RDFUtil;

@Service
public class RDFCvnBuilderServiceImpl implements RDFCvnBuilderService {

	/**
	 * Logger.
	 */
	private final Logger logger = LoggerFactory.getLogger(RDFCvnBuilderServiceImpl.class);

	/**
	 * The rdf dataset builder service.
	 */
	@Autowired
	private RDFDatasetBuilderService rdfDatasetBuilderService;

	/**
	 * The uris generator client.
	 */
	@Autowired
	private URISGeneratorClient urisGeneratorClient;

	@Autowired
	private RDFServiceUtils rdfServiceUtils;

	@Autowired
	private RdfValidatorService rdfValidatorService;
	
	/**
	 * The property uri cache.
	 */
	Map<String, String> propertyUriCache = new HashMap<String, String>();

	/**
	 * Inkove builder.
	 *
	 * @param input the input
	 * @return the management bus event
	 */
	public ManagementBusEvent inkoveBuilder(GeneralBusEvent<?> input) {
		if (canBeProcessed(input)) {
			ModelWrapper model = this.createRDF(input.retrieveInnerObj());
			rdfValidatorService.validate(model);
			return new ManagementBusEvent(model.getModelId(), RDFUtil.toString(model.getModel()), StringUtils.EMPTY,
					input.retrieveInnerObj().getClass().getSimpleName(), input.retrieveOperation());
		}

		return nextBuilder(input);
	}

	/**
	 * Next builder.
	 *
	 * @param input the input
	 * @return the management bus event
	 */
	@Override
	public ManagementBusEvent nextBuilder(GeneralBusEvent<?> input) {
		return rdfDatasetBuilderService.inkoveBuilder(input);
	}

	/**
	 * Can be processed.
	 *
	 * @param input the input
	 * @return the boolean
	 */
	private Boolean canBeProcessed(GeneralBusEvent<?> input) {
		return input.getData() instanceof InputData
				&& ((InputData<?>) input.getData()).getData() instanceof CvnRootBean;
	}

	/**
	 * Creates the RDF.
	 *
	 * @param obj the obj
	 * @return the model wrapper
	 */
	public ModelWrapper createRDF(Object obj) {
		ModelWrapper result = new ModelWrapper();

		// 0. create Cvn model
		Model model = ModelFactory.createDefaultModel();
		model.createProperty(urisGeneratorClient.rootUri());

		try {

			result.setExecutionId((String)getFieldValue(obj, Constants.EXECUTION_ID));
			
			// 1. create the resource
			String modelId = urisGeneratorClient.createResourceID(obj);
			Resource resourceProperties = createResource(model, obj, modelId);

			// 2. we set the type
			String modelType = urisGeneratorClient.createResourceTypeURI(obj.getClass().getName());
			Resource resourceClass = model
					.createResource(modelType);
			model.add(resourceProperties, RDF.type, resourceClass);
			result.setModelType(modelType);

			// 4. we build the result model
			result.setModelId(modelId);
			result.setModel(model);			

		} catch (Exception e) {
			logger.error("Error creating resource from input: " + obj);
			logger.error("createRDF", e);

			// we sent import error to kafka error topic
			this.rdfServiceUtils.sendImportError(e, result.getExecutionId());
		}

		return result;
	}

	/**
	 * Creates the resource from cvn bean.
	 *
	 * @param model      the model
	 * @param obj        the obj
	 * @param resourceID the resource ID
	 * @return the resource
	 * @throws IllegalAccessException    the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws NoSuchMethodException     the no such method exception
	 */
	public Resource createResource(Model model, Object obj, String resourceID)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		Resource resource = StringUtils.isEmpty(resourceID) ? model.createResource() : model.createResource(resourceID);

		List<Field> fields = this.getFields(obj.getClass());
		for (Field field : fields) {
			// field.setAccessible(true); // watch out with this line
			Object value = field.get(obj);
			if (value == null || Constants.EXECUTION_ID.equalsIgnoreCase(field.getName())) {
				continue;
			}

			Property property = model.createProperty(this.createPropertyUri(obj, field.getName()), field.getName());

			if (value instanceof CvnBean) {
				resource.addProperty(property, createResource(model, value, StringUtils.EMPTY));
				continue;
			}
			if (value instanceof Collection) {
				Collection<?> internalCvnBeans = (Collection<?>) value;

				if (internalCvnBeans != null && !internalCvnBeans.isEmpty()) {
					var bag = model.createBag();
					for (Object internalCvnBean : internalCvnBeans) {
						bag.addProperty(property, createResource(model, internalCvnBean, StringUtils.EMPTY));
					}
					resource.addProperty(property, bag);
				}
				continue;
			}

			String propertyValue = BeanUtils.getSimpleProperty(obj, field.getName());
			if (StringUtils.isEmpty(propertyValue)) {
				propertyValue = StringUtils.EMPTY;
			}
			resource.addProperty(property, propertyValue);
		}

		return resource;
	}
	
	private Object getFieldValue(Object obj, String fieldName) {
		try {
			List<Field> fields = this.getFields(obj.getClass());
			for (Field field : fields) {
				if (fieldName.equalsIgnoreCase(field.getName())) {
					return field.get(obj);
				}
			}
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * Gets the fields.
	 *
	 * @param clazz the clazz
	 * @return the fields
	 */
	@SuppressWarnings("unchecked")
	private List<Field> getFields(Class<?> clazz) {
		ArrayList<Field> fields = new ArrayList<>();

		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		if (CvnBean.class.isAssignableFrom(clazz.getSuperclass())) {
			fields.addAll(getFields((Class<CvnBean>) clazz.getSuperclass()));
		}

		return fields;
	}

	/**
	 * Creates the property uri.
	 *
	 * @param obj      the obj
	 * @param property the property
	 * @return the string
	 */
	private String createPropertyUri(Object obj, String property) {
		var propertyUri = propertyUriCache.get(property);
		if (propertyUri == null) {
			propertyUri = urisGeneratorClient.createPropertyURI(obj, property);
			propertyUriCache.put(property, propertyUri);
		}
		return propertyUri;
	}

}