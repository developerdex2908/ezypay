package com.ezypay.subscription.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Invoice {
    Long amount;
    String invoiceDate;
}
