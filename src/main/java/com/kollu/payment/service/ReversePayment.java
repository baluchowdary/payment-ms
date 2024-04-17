package com.kollu.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kollu.payment.dto.CustomerOrder;
import com.kollu.payment.dto.OrderEvent;
import com.kollu.payment.dto.PaymentEvent;
import com.kollu.payment.entity.Payment;
import com.kollu.payment.entity.PaymentRepository;

@Component
public class ReversePayment {
	
	@Autowired
	private PaymentRepository repository;

	@Autowired
	private KafkaTemplate<String, OrderEvent> kafkaTemplate;
	
	private static final String REVERSED_PAYMENTS_TOPIC = "reversed-payments";
	private static final String REVERSED_PAYMENTS_GROUP = "payments-group";
	private static final String REVERSED_ORDERS_TOPIC = "reversed-orders";

	@KafkaListener(topics = REVERSED_PAYMENTS_TOPIC, groupId = REVERSED_PAYMENTS_GROUP)
	public void reversePayment(String event) {
		System.out.println("Inside reverse payment for order "+event);
		
		try {
			PaymentEvent paymentEvent = new ObjectMapper().readValue(event, PaymentEvent.class);

			CustomerOrder order = paymentEvent.getOrder();

			Iterable<Payment> payments = this.repository.findByOrderId(order.getOrderId());

			payments.forEach(p -> {
				p.setStatus("FAILED");
				repository.save(p);
			});

			OrderEvent orderEvent = new OrderEvent();
			orderEvent.setOrder(paymentEvent.getOrder());
			orderEvent.setType("ORDER_REVERSED");
			kafkaTemplate.send(REVERSED_ORDERS_TOPIC, orderEvent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
