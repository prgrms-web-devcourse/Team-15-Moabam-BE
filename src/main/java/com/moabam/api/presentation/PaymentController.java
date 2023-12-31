package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.payment.PaymentService;
import com.moabam.api.dto.payment.ConfirmPaymentRequest;
import com.moabam.api.dto.payment.PaymentRequest;
import com.moabam.api.dto.payment.RequestConfirmPaymentResponse;
import com.moabam.global.auth.annotation.Auth;
import com.moabam.global.auth.model.AuthMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/{paymentId}")
	@ResponseStatus(HttpStatus.OK)
	public void request(@Auth AuthMember member, @PathVariable Long paymentId,
		@Valid @RequestBody PaymentRequest request) {
		paymentService.request(member.id(), paymentId, request);
	}

	@PostMapping("/confirm")
	@ResponseStatus(HttpStatus.OK)
	public void confirm(@Auth AuthMember member, @Valid @RequestBody ConfirmPaymentRequest request) {
		RequestConfirmPaymentResponse response = paymentService.requestConfirm(member.id(), request);
		paymentService.confirm(member.id(), response.payment(), response.paymentKey());
	}
}
