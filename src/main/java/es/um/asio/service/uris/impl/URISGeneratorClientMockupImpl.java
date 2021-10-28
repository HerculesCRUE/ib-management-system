package es.um.asio.service.uris.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import es.um.asio.abstractions.constants.Constants;
import es.um.asio.service.rdf.RDFGeneratorIDService;
import es.um.asio.service.uris.URISGeneratorClient;

@Service
@ConditionalOnProperty(prefix = "app.generator-uris.mockup", name = "enabled", havingValue = "true", matchIfMissing = true)
public class URISGeneratorClientMockupImpl implements URISGeneratorClient {
	
	@Override
	public String createResourceTypeURI(String className) {
		return Constants.ROOT_URI + "/" + className;
	}
	
	/** The generator ID service. */
	@Autowired
	private RDFGeneratorIDService generatorIDService;

	@Override
	public String rootUri() {
		return Constants.ROOT_URI;
	}
	
	@Override
	public String createResourceID(Object input) {
		return generatorIDService.generateResourceID(input);
	}

	@Override
	public String createResourceID(Object input, boolean useDiscovery) {
		return generatorIDService.generateResourceID(input);
	}

	@Override
	public String createPropertyURI(Object input, String property) {
		return "http://www.w3.org/2001/asio-rdf/3.0#";
	}

	@Override
	public Map<String, String> mapResourceID(Object input) {
		return null;
	}

	@Override
	public String createResourceID(Object input, String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> mapResourceID(Object input, String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createPropertyURI(Object input, String property, String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createResourceTypeURI(String className, String lang) {
		// TODO Auto-generated method stub
		return null;
	}
}
