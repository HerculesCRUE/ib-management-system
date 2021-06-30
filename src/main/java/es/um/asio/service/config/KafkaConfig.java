package es.um.asio.service.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import es.um.asio.abstractions.domain.ManagementBusEvent;
import es.um.asio.domain.DataSetData;
import es.um.asio.domain.InputData;
import es.um.asio.domain.PojoData;
import es.um.asio.domain.PojoLinkData;
import es.um.asio.domain.PojoLinkedToData;
import es.um.asio.domain.importer.ImportError;
import es.um.asio.service.error.KafkaErrorHandler;
import es.um.asio.service.util.CustomJsonSerializer;

/**
 * Kafka related configuration.
 */
@EnableKafka
@Profile("!unit-test")
@Configuration
public class KafkaConfig {
	/**
	 * Kafka properties.
	 */
	@Autowired
	private KafkaProperties kafkaProperties;

	////////////////////////////////////////////////////// DATASET ////////////////////////////////////////////////////////////////////////////////
	/**
	 * Data set data consumer factory.
	 *
	 * @return the consumer factory
	 */
	public ConsumerFactory<String, InputData<DataSetData>> dataSetDataConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(this.getKafkaConfiguration());
	}

	/**
	 * Data set data kafka listener container factory.
	 *
	 * @return the concurrent kafka listener container factory
	 */
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, InputData<DataSetData>> dataSetDataKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, InputData<DataSetData>> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(this.dataSetDataConsumerFactory());
		return factory;
	}

	////////////////////////////////////////////////////// POJO ////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Pojo consumer factory.
	 *
	 * @return the consumer factory
	 */
	public ConsumerFactory<String, PojoData> pojoConsumerFactory() {
		// ErrorHandlingDeserializer2 avoid infinite loop when the input is wrong built
		return new DefaultKafkaConsumerFactory<>(this.getKafkaConfiguration(), new StringDeserializer(),
				new ErrorHandlingDeserializer2<>(new JsonDeserializer<>(PojoData.class)));
	}

	/**
	 * Pojo kafka listener container factory.
	 *
	 * @return the concurrent kafka listener container factory
	 */
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PojoData> pojoKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, PojoData> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(this.pojoConsumerFactory());
		factory.setErrorHandler(new KafkaErrorHandler());
		factory.getContainerProperties().setIdleEventInterval(3000L);
		return factory;
	}
	
    ////////////////////////////////////////////////////// POJO link ////////////////////////////////////////////////////////////////////////////////

	/**
	 * Pojo link consumer factory.
	 *
	 * @return the consumer factory
	 */
	public ConsumerFactory<String, PojoLinkData> pojoLinkConsumerFactory() {
		// ErrorHandlingDeserializer2 avoid infinite loop when the input is wrong built
		return new DefaultKafkaConsumerFactory<>(this.getKafkaConfiguration(), new StringDeserializer(),
				new ErrorHandlingDeserializer2<>(new JsonDeserializer<>(PojoLinkData.class)));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PojoLinkData> pojoLinkKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, PojoLinkData> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(this.pojoLinkConsumerFactory());
		factory.setErrorHandler(new KafkaErrorHandler());
		factory.getContainerProperties().setIdleEventInterval(3000L);
		return factory;
	}
	
    ////////////////////////////////////////////////////// DISCOVERY ////////////////////////////////////////////////////////////////////////////////

	/**
	 * Discovery consumer factory.
	 *
	 * @return the consumer factory
	 */
	public ConsumerFactory<String, PojoLinkedToData> discoveryConsumerFactory() {
		// ErrorHandlingDeserializer2 avoid infinite loop when the input is wrong built
		return new DefaultKafkaConsumerFactory<>(this.getKafkaConfiguration(), new StringDeserializer(),
				new ErrorHandlingDeserializer2<>(new JsonDeserializer<>(PojoLinkedToData.class)));
	}

	/**
	 * Discovery kafka listener container factory.
	 *
	 * @return the concurrent kafka listener container factory
	 */
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PojoLinkedToData> discoveryKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, PojoLinkedToData> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(this.discoveryConsumerFactory());
		factory.setErrorHandler(new KafkaErrorHandler());
		factory.getContainerProperties().setIdleEventInterval(3000L);
		return factory;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets the kafka configuration.
	 * customerConfigMap.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "1000");
	 * customerConfigMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 2);
	 * customerConfigMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
	 * customerConfigMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
	 * 
	 * @return the kafka configuration
	 */
	private Map<String, Object> getKafkaConfiguration() {
		Map<String, Object> customerConfigMap = this.kafkaProperties.getConsumer().buildProperties();
		customerConfigMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		customerConfigMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		customerConfigMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		customerConfigMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		customerConfigMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		return customerConfigMap;
	}

	// PRODUCTOR

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Bean
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomJsonSerializer.class);
		props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10000000);
		return props;
	}

    //////////////////////////////////////////////////////// MANAGEMENT BUS EVENT PRODUCER /////////////////////////////////////////////////////////////////////////////////////
	@Bean
	public ProducerFactory<String, ManagementBusEvent> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	public KafkaTemplate<String, ManagementBusEvent> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

    //////////////////////////////////////////////////////// IMPORT ERROR PRODUCER /////////////////////////////////////////////////////////////////////////////////////
	@Bean
	public ProducerFactory<String, ImportError> producerErrorFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	public KafkaTemplate<String, ImportError> kafkaErrorTemplate() {
		return new KafkaTemplate<>(producerErrorFactory());
	}

}
