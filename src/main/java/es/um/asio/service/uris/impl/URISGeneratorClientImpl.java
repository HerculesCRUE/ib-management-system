package es.um.asio.service.uris.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import es.um.asio.abstractions.constants.Constants;
import es.um.asio.service.uris.URISGeneratorClient;
import es.um.asio.service.uris.URISGeneratorClientCache;
import es.um.asio.service.util.RDFUtil;

@Service
@ConditionalOnProperty(prefix = "app.generator-uris.mockup", name = "enabled", havingValue = "false", matchIfMissing = true)
public class URISGeneratorClientImpl implements URISGeneratorClient {
	
	@Value("${app.generator-uris.endpoint-root-uri}")
    private String rootURIEndpoint;
	
	@Value("${app.generator-uris.endpoint-resource-id}")
    private String resourceIdEndpoint;
	
	@Value("${app.generator-uris.endpoint-property}")
    private String propertyEndpoint;
	
	@Value("${app.generator-uris.endpoint-resource-type}")
    private String resourceTypeEndpoint;
	
	
	@Autowired
	private URISGeneratorClientCache uRISGeneratorClientCache;

	/**
     * Rest Template
     */
    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

	/**
	 * Creates the resource ID.
	 *
	 * @param input the input
	 * @return the string
	 */
	@Override
	public String createResourceID(Object obj) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(resourceIdEndpoint)
		        .queryParam(Constants.DOMAIN, Constants.DOMAIN_VALUE)
		        .queryParam(Constants.LANG, Constants.SPANISH_LANGUAGE)
		        .queryParam(Constants.SUBDOMAIN, Constants.SUBDOMAIN_VALUE);
		
		Map response = restTemplate.postForObject(builder.toUriString(), obj, Map.class);
		
		String result = response != null ? (String)response.get(Constants.CANONICAL_LANGUAGE_URI): null; 
		
		return result;
	}
	
	/**
	 * Creates the resource ID.
	 *
	 * @param input the input
	 * @param lang the lang
	 * @return the string
	 */
	@Override
	public String createResourceID(Object obj, String lang) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(resourceIdEndpoint)
		        .queryParam(Constants.DOMAIN, Constants.DOMAIN_VALUE)
		        .queryParam(Constants.LANG, StringUtils.isNotBlank(lang) ? lang : Constants.SPANISH_LANGUAGE)
		        .queryParam(Constants.SUBDOMAIN, Constants.SUBDOMAIN_VALUE);
		
		Map response = restTemplate.postForObject(builder.toUriString(), obj, Map.class);
		
		String result = response != null ? (String)response.get(Constants.CANONICAL_LANGUAGE_URI): null; 
		
		return result;
	}
	
	/**
	 * Creates the resource ID.
	 *
	 * @param input the input
	 * @return the map
	 */
	@Override
	public Map<String, String> mapResourceID(Object obj) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(resourceIdEndpoint)
		        .queryParam(Constants.DOMAIN, Constants.DOMAIN_VALUE)
		        .queryParam(Constants.LANG, Constants.SPANISH_LANGUAGE)
		        .queryParam(Constants.SUBDOMAIN, Constants.SUBDOMAIN_VALUE);
		
		Map<String, String> response = (Map<String, String>) restTemplate.postForObject(builder.toUriString(), obj, Map.class);
		
		return response;
	}
	
	/**
	 * Creates the resource ID.
	 *
	 * @param input the input
	 * @param lang the lang
	 * @return the map
	 */
	@Override
	public Map<String, String> mapResourceID(Object obj, String lang) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(resourceIdEndpoint)
		        .queryParam(Constants.DOMAIN, Constants.DOMAIN_VALUE)
		        .queryParam(Constants.LANG, StringUtils.isNotBlank(lang) ? lang : Constants.SPANISH_LANGUAGE)
		        .queryParam(Constants.SUBDOMAIN, Constants.SUBDOMAIN_VALUE);
		
		Map<String, String> response = (Map<String, String>) restTemplate.postForObject(builder.toUriString(), obj, Map.class);
		
		return response;
	}

	/**
	 * Creates the property URI.
	 *
	 * @param input the input
	 * @return the stringc
	 */
	@Override
	public String createPropertyURI(Object obj, String property) {
		String result = uRISGeneratorClientCache.find(property, Constants.CACHE_PROPERTIES);
		
		if(StringUtils.isBlank(result)) {
			HashMap input = new HashMap<>();
			input.put(Constants.OBJECT, obj);
			input.put(Constants.CLASS, obj.getClass().getName());
			input.put(Constants.PROPERTY, property);
			
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(propertyEndpoint)
					.queryParam(Constants.DOMAIN, Constants.DOMAIN_VALUE)
					.queryParam(Constants.LANG, Constants.SPANISH_LANGUAGE)
					.queryParam(Constants.SUBDOMAIN, Constants.SUBDOMAIN_VALUE);
			
			Map response = restTemplate.postForObject(builder.toUriString(), input, Map.class);
			
			result = response != null ? RDFUtil.getNameSpaceFromPath((String)response.get(Constants.CANONICAL_LANGUAGE_URI)): null; 
			
			// we save the result in cache
			uRISGeneratorClientCache.saveInCache(property, result, Constants.CACHE_PROPERTIES);
		}
		
		return result;
	}
	
	/**
	 * Creates the property URI.
	 *
	 * @param input the input
	 * @param lang the lang
	 * @return the stringc
	 */
	@Override
	public String createPropertyURI(Object obj, String property, String lang) {
		String result = uRISGeneratorClientCache.find(property, Constants.CACHE_PROPERTIES);
		
		if(StringUtils.isBlank(result)) {
			HashMap input = new HashMap<>();
			input.put(Constants.OBJECT, obj);
			input.put(Constants.CLASS, obj.getClass().getName());
			input.put(Constants.PROPERTY, property);
			
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(propertyEndpoint)
					.queryParam(Constants.DOMAIN, Constants.DOMAIN_VALUE)
					.queryParam(Constants.LANG, StringUtils.isNotBlank(lang) ? lang : Constants.SPANISH_LANGUAGE)
					.queryParam(Constants.SUBDOMAIN, Constants.SUBDOMAIN_VALUE);
			
			Map response = restTemplate.postForObject(builder.toUriString(), input, Map.class);
			
			result = response != null ? RDFUtil.getNameSpaceFromPath((String)response.get(Constants.CANONICAL_LANGUAGE_URI)): null; 
			
			// we save the result in cache
			uRISGeneratorClientCache.saveInCache(property, result, Constants.CACHE_PROPERTIES);
		}
		
		return result;
	}

	/**
	 * Creates the resource type URI.
	 *
	 * @param className the class name
	 * @return the string
	 */
	@Override
	public String createResourceTypeURI(String className) {
		String result = uRISGeneratorClientCache.find(className, Constants.CACHE_ENTITIES);
		
		if(StringUtils.isBlank(result)) {
			HashMap input = new HashMap<>();
			input.put(Constants.CLASS, className);
			
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(resourceTypeEndpoint)
					.queryParam(Constants.DOMAIN, Constants.DOMAIN_VALUE)
					.queryParam(Constants.LANG, Constants.SPANISH_LANGUAGE)
					.queryParam(Constants.SUBDOMAIN, Constants.SUBDOMAIN_VALUE);
			
			Map response = restTemplate.postForObject(builder.toUriString(), input, Map.class);
			
			result = response != null ? (String)response.get(Constants.CANONICAL_LANGUAGE_URI): null; 	
			
			// we save the result in cache
			uRISGeneratorClientCache.saveInCache(className, result, Constants.CACHE_ENTITIES);
		}
		
		return result;
	}
	
	/**
	 * Creates the resource type URI.
	 *
	 * @param className the class name
	 * @param lang the lang
	 * @return the string
	 */
	@Override
	public String createResourceTypeURI(String className, String lang) {
		String result = uRISGeneratorClientCache.find(className, Constants.CACHE_ENTITIES);
		
		if(StringUtils.isBlank(result)) {
			HashMap input = new HashMap<>();
			input.put(Constants.CLASS, className);
			
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(resourceTypeEndpoint)
					.queryParam(Constants.DOMAIN, Constants.DOMAIN_VALUE)
					.queryParam(Constants.LANG, StringUtils.isNotBlank(lang) ? lang : Constants.SPANISH_LANGUAGE)
					.queryParam(Constants.SUBDOMAIN, Constants.SUBDOMAIN_VALUE);
			
			Map response = restTemplate.postForObject(builder.toUriString(), input, Map.class);
			
			result = response != null ? (String)response.get(Constants.CANONICAL_LANGUAGE_URI): null; 	
			
			// we save the result in cache
			uRISGeneratorClientCache.saveInCache(className, result, Constants.CACHE_ENTITIES);
		}
		
		return result;
	}

	@Override
	public String rootUri() {
		return Constants.ROOT_URI; 
	}

}
