package com.kollu.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kollu.payment.dto.CustomerOrder;
import com.kollu.payment.dto.OrderEvent;
import com.kollu.payment.dto.PaymentEvent;
import com.kollu.payment.entity.Payment;
import com.kollu.payment.entity.PaymentRepository;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class PaymentController {
	
	@Autowired
	private PaymentRepository repository;

	@Autowired
	private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

	@Autowired
	private KafkaTemplate<String, OrderEvent> kafkaOrderTemplate;
	
	private static final String NEW_ORDERS_TOPIC ="new-order";
	private static final String NEW_ORDERS_GROUP ="orders-group";
	private static final String NEW_PAYMENTS_TOPIC ="new-payments";
	private static final String REVERSED_ORDERS_TOPIC ="reversed-orders";
	
	@KafkaListener(topics = NEW_ORDERS_TOPIC, groupId = NEW_ORDERS_GROUP)
	public void processPayment(String event) throws JsonMappingException, JsonProcessingException {
		System.out.println("Recieved event for payment " + event);
		log.info("processPayment --method start");
		OrderEvent orderEvent = new ObjectMapper().readValue(event, OrderEvent.class);

		CustomerOrder order = orderEvent.getOrder();
		Payment payment = new Payment();
		
		try {
			payment.setAmount(order.getAmount());
			payment.setMode(order.getPaymentMode());
			payment.setOrderId(order.getOrderId());
			payment.setStatus("SUCCESS");
			repository.save(payment);

			PaymentEvent paymentEvent = new PaymentEvent();
			paymentEvent.setOrder(orderEvent.getOrder());
			paymentEvent.setType("PAYMENT_CREATED");
			kafkaTemplate.send(NEW_PAYMENTS_TOPIC, paymentEvent);
			
			log.info("processPayment --method end");
		} catch (Exception e) {
			payment.setOrderId(order.getOrderId());
			payment.setStatus("FAILED");
			repository.save(payment);

			OrderEvent oe = new OrderEvent();
			oe.setOrder(order);
			oe.setType("ORDER_REVERSED");
			kafkaOrderTemplate.send(REVERSED_ORDERS_TOPIC, orderEvent);
		}
	}

}
