package es.um.asio.service.model;

import org.apache.commons.beanutils.PropertyUtils;

import es.um.asio.abstractions.domain.Operation;
import es.um.asio.domain.InputData;
import es.um.asio.domain.PojoData;
import es.um.asio.domain.PojoLinkedToData;
import es.um.asio.service.error.ManagementException;
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
public class GeneralBusEvent<T> {
	/**
	 * Data set data.
	 */
	private T data;

	@SuppressWarnings("unchecked")
	public T retrieveInnerObj() {

		try {
			return (T) PropertyUtils.getProperty(data, "data");
		} catch (Exception e) {
			throw new ManagementException("Not found data property in " + data, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public T retrieveInnerObj(String property) {

		try {
			return (T) PropertyUtils.getProperty(data, property);
		} catch (Exception e) {
			throw new ManagementException("Not found data property in " + data, e);
		}
	}

	/**
	 * Retrieve operation.
	 *
	 * @return the operation
	 */
	public Operation retrieveOperation() {
		Operation result = Operation.INSERT;

		if (data instanceof InputData) {
			result = Operation.INSERT;
		}

		if (data instanceof PojoData) {
			result = ((PojoData) data).getOperation();
		}
		
		if (data instanceof PojoLinkedToData) {
			result = ((PojoLinkedToData) data).getAction();
		}
		
		return result;
	}
}
