package es.um.asio.service.controller;

import javax.jms.Topic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.um.asio.abstractions.domain.ManagementBusEvent;

@RestController
@RequestMapping("/api")
public class MessageController {

	@Autowired
	private Topic queue;

	@Autowired
	private JmsTemplate jmsTemplate;

	@GetMapping("message/{message}")
	public ResponseEntity<String> publish(@PathVariable("message") final String message) {

		final ManagementBusEvent obj = new ManagementBusEvent();
		obj.setModel("myModel");
		obj.setClassName("myTestClass");

		jmsTemplate.convertAndSend(queue, obj);
		return new ResponseEntity<>(message, HttpStatus.OK);
	}
}
