package com.ezypay.subscription.controller;

import com.ezypay.subscription.entity.InvoiceDTO;
import com.ezypay.subscription.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "invoices")
public class InvoiceController {
    @Autowired
    InvoiceService invoiceService;

    @GetMapping
    String getInvoices() {
        return "get invoice";
    }

    @PostMapping
    ResponseEntity generateInvoice(@RequestBody InvoiceDTO invoice) {
        System.out.println("Invoice " + invoice.toString());
        return invoiceService.generateInvoice(invoice);
    }
}
