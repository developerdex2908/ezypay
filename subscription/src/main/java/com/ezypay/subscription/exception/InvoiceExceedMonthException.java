package com.ezypay.subscription.exception;

public class InvoiceExceedMonthException extends Exception{
    public InvoiceExceedMonthException(String error){
        super(error);
    }
}
