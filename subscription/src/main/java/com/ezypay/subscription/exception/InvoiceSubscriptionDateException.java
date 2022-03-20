package com.ezypay.subscription.exception;

public class InvoiceSubscriptionDateException extends Exception{
    public InvoiceSubscriptionDateException(){
        super("Invalid date format. Only dd/mm/yyyy is accepted");
    }
}
