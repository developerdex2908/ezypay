package com.ezypay.subscription.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class InvoiceDTO extends Object{
    Long amount;
    SubscriptionType subscriptionType;
    String billDay;
    String startDate;
    String endDate;

    @Override
    public String toString() {
        return "{ \"amount\":" + amount + "," +
               "  \"subscription\":\"" + subscriptionType + "\"," +
               "  \"billDay\":" + billDay + "," +
               "  \"startDate\":\"" + startDate + "\"," +
               "  \"endDate\":\"" + endDate + "\"" +
               "}";
    }
}
