package es.um.asio.service.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidRdfSchemaValidationException extends RuntimeException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8674172047347273987L;
	
	private String errors;
	
	private String executionId;

	public InvalidRdfSchemaValidationException(String errors) {
		super();
		this.errors = errors;
	}

	public InvalidRdfSchemaValidationException(String errors, String executionId) {
		super();
		this.errors = errors;
		this.executionId = executionId;
	}
}
