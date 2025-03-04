package com.system.megacitycab.Payment.dto;

import lombok.Data;

@Data
public class CreatePaymentRequest {
    private Long amount;
    private String currency;
}
