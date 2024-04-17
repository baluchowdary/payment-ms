package com.kollu.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Payment_Table")
@Getter
@Setter
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "Payment_Mode")
	private String mode;

	@Column(name = "Payment_OrderId")
	private Long orderId;

	@Column(name = "Payment_Amount")
	private double amount;

	@Column(name = "Payment_Status")
	private String status;
}
