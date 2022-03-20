package com.ezypay.subscription.service;

import com.ezypay.subscription.entity.InvoiceDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

public interface InvoiceService {
    public ResponseEntity generateInvoice(InvoiceDTO invoice);
}
