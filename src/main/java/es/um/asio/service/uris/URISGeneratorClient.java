package es.um.asio.service.uris;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface URISGeneratorClient.
 */
public interface URISGeneratorClient {

	/**
	 * Creates the resource ID.
	 *
	 * @param input the input
	 * @return the string
	 */
	String createResourceID(Object input);

	/**
	 * Creates the resource ID.
	 *
	 * @param input the input
	 * @return the string
	 */
	String createResourceID(Object input,boolean useDiscovery);
	
	/**
	 * Creates the resource ID.
	 *
	 * @param input the input
	 * @param lang the lang
	 * @return the string
	 */
	String createResourceID(Object input, String lang);
	
	/**
	 * Creates the resource ID.
	 *
	 * @param input the input
	 * @return the map
	 */
	Map<String, String> mapResourceID(Object input);
	
	/**
	 * Creates the resource ID.
	 *
	 * @param input the input
	 * @param lang the lang
	 * @return the map
	 */
	Map<String, String> mapResourceID(Object input, String lang);
	
	
	/**
	 * Creates the property URI.
	 *
	 * @param input the input
	 * @param property the property
	 * @return the string
	 */
	String createPropertyURI(Object input, String property);
	
	/**
	 * Creates the property URI.
	 *
	 * @param input the input
	 * @param property the property
	 * @param lang the lang
	 * @return the string
	 */
	String createPropertyURI(Object input, String property, String lang);
	
	/**
	 * Creates the resource type URI.
	 *
	 * @param className the class name
	 * @return the string
	 */
	String createResourceTypeURI(String className);
	
	/**
	 * Creates the resource type URI.
	 *
	 * @param className the class name
	 * @param lang the lang
	 * @return the string
	 */
	String createResourceTypeURI(String className, String lang);
	
	
	/**
	 * Root uri.
	 *
	 * @return the string
	 */
	String rootUri();
	
}
