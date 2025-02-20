package com.system.megacitycab.dto;

import lombok.Data;

@Data
public class CreatePaymentRequest {
    private Long amount;
    private String currency;
}
