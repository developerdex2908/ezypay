package com.ezypay.subscription.exception;

public class InvoiceMonthException extends Exception{
    public InvoiceMonthException(){
        super("Invalid day of month");
    }
}
