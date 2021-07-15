package es.um.asio.service.model;

import org.apache.jena.rdf.model.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ModelWrapper {
	
	/** The model id. */
	private String modelId;
	
	/** The model. */
	private Model model;
	
	/** The nested objects. */
	private Object linkedModel;
	
	
	private String executionId;
	
	private String modelType;
}
