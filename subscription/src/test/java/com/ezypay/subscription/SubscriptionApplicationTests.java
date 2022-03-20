package com.ezypay.subscription;

import com.ezypay.subscription.entity.InvoiceDTO;
import com.ezypay.subscription.entity.SubscriptionType;
import com.ezypay.subscription.exception.InvoiceInvalidDateRangeException;
import com.ezypay.subscription.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class SubscriptionApplicationTests {

	@Autowired
	InvoiceService invoiceService;

	@Test
	void contextLoads() {
	}

	private InvoiceDTO validMonthlySubscription() {
		return new InvoiceDTO(100L,
				SubscriptionType.MONTHLY,
				"20",
				"20/01/2022",
				"20/02/2022");
	}

	private InvoiceDTO invalidMonthlySubscription() {
		return new InvoiceDTO(100L,
				SubscriptionType.MONTHLY,
				"19",
				"20/01/2022",
				"20/03/2022");
	}

	private InvoiceDTO validWeeklySubscription() {
		return new InvoiceDTO(100L,
				SubscriptionType.WEEKLY,
				"MONDAY",
				"20/01/2022",
				"20/02/2022");
	}

	private InvoiceDTO invalidWeeklySubscription() {
		return new InvoiceDTO(100L,
				SubscriptionType.WEEKLY,
				"MONDAY",
				"18/03/2022",
				"20/03/2022");
	}

	private InvoiceDTO validDailySubscription() {
		return new InvoiceDTO(100L,
				SubscriptionType.DAILY,
				"",
				"20/01/2022",
				"25/01/2022");
	}

	private InvoiceDTO invalidDailySubscription() {
		return new InvoiceDTO(100L,
				SubscriptionType.WEEKLY,
				"MONDAY",
				"20/01/2022",
				"10/01/2022");
	}

	@Test
	void shouldReturnMonthlyInvoice() {
		ResponseEntity responseEntity = invoiceService.generateInvoice(validMonthlySubscription());
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldFailForMonthlyInvoice() {
		ResponseEntity responseEntity = invoiceService.generateInvoice(invalidMonthlySubscription());
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnWeeklyInvoice() {
		ResponseEntity responseEntity = invoiceService.generateInvoice(validWeeklySubscription());
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldFailForWeeklyInvoice() {
		ResponseEntity responseEntity = invoiceService.generateInvoice(invalidWeeklySubscription());
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnDailyInvoice() {
		ResponseEntity responseEntity = invoiceService.generateInvoice(validDailySubscription());
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldFailForDailyInvoice() {
		ResponseEntity responseEntity = invoiceService.generateInvoice(invalidDailySubscription());
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}
}
