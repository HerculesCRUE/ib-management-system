package es.um.asio.service.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import es.um.asio.service.service.MessageService;

/**
 * General message listener
 */
@Component
public class GeneralListener {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(GeneralListener.class);

    /**
     * Service to handle message entity related operations
     */
    @Autowired
    private MessageService messageService;

    /**
     * Method listening input topic name
     * 
     * @param message
     */
    @KafkaListener(topics = "#{'${app.kafka.general-topic-name}'.split(',')}")
    public void listen(final String message) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Received message: {}", message);
        }

        // Cuando el mensaje sea recibido es preciso procesarlo
        this.messageService.save(message);
    }
}
