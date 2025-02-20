package com.system.megacitycab.controller;

import com.stripe.model.PaymentIntent;
import com.system.megacitycab.dto.CreatePaymentRequest;
import com.system.megacitycab.dto.CreatePaymentResponse;
import com.system.megacitycab.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<CreatePaymentResponse> createPaymentIntent(@RequestBody CreatePaymentRequest request){
        PaymentIntent intent = paymentService.createPaymentIntent(request.getAmount(), request.getCurrency());


        CreatePaymentResponse response = new CreatePaymentResponse(intent.getClientSecret());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentIntent> confirmPayment(@PathVariable String paymentIntentId){
        PaymentIntent confirmedIntent = paymentService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(confirmedIntent);
    }

}
