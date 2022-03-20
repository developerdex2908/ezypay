package com.ezypay.subscription.exception;

public class InvoiceInvalidDateRangeException extends Exception{
    public InvoiceInvalidDateRangeException(String error){
        super(error);
    }
}
