package com.system.megacitycab.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripApiKey;

    public PaymentIntent createPaymentIntent(Long amount, String currency) {
        Stripe.apiKey = stripApiKey;

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();
            return PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Error creating payment intent", e);
        }
    }

    public PaymentIntent confirmPayment(String paymentIntentId) {
        Stripe.apiKey = stripApiKey;

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return paymentIntent.confirm();
        } catch (StripeException e) {
            throw new RuntimeException("Error confirming payment intent", e);
        }
    }

}
