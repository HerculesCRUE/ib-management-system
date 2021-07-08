package es.um.asio.service.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

import es.um.asio.domain.util.JpaConstants;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The class of representing generic data set data.
 */
@MappedSuperclass
@Getter
@Setter
@ToString(includeFieldNames = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class DomainDataBase implements DomainData, Serializable {

	private static final long serialVersionUID = 8037539991352137993L;

	/**
	 * The id.
	 */
	@Id
	@GeneratedValue(generator = JpaConstants.HIBERNATE_UUID_GENERATOR_NAME)
	@GenericGenerator(name = JpaConstants.HIBERNATE_UUID_GENERATOR_NAME, strategy = JpaConstants.HIBERNATE_UUID_GENERATOR_STRATEGY)
	@Column(name = Columns.ENTITY_ID)
	@EqualsAndHashCode.Include
	private String entityId;

	/**
	 * Column name constants.
	 */
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	static final class Columns {
		/**
		 * ID column.
		 */
		protected static final String ENTITY_ID = "ENTITY_ID";

	}
}
