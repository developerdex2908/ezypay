package com.ezypay.subscription.service.impl;

import com.ezypay.subscription.entity.Invoice;
import com.ezypay.subscription.entity.InvoiceDTO;
import com.ezypay.subscription.entity.InvoiceException;
import com.ezypay.subscription.entity.SubscriptionType;
import com.ezypay.subscription.exception.*;
import com.ezypay.subscription.service.InvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@Service
public class InvoiceServiceImpl implements InvoiceService {
    @Override
    public ResponseEntity generateInvoice(InvoiceDTO invoice) {
        LocalDate startOfSubscription = null;
        LocalDate endOfSubscription = null;
        Object responseEntity = new Object();
        HttpStatus httpStatus = null;
        List<Invoice> invoices = null;

        try {
            startOfSubscription = convertSubscriptionDate(invoice.getStartDate());
            endOfSubscription = convertSubscriptionDate(invoice.getEndDate());

            validateSubscriptionDate(startOfSubscription, endOfSubscription);
            if (invoice.getSubscriptionType().equals(SubscriptionType.MONTHLY)) {
                validateDayOfMonth(invoice.getBillDay(), startOfSubscription.getYear());
                invoices = generateMonthlyInvoice(invoice.getBillDay(), invoice.getAmount(),
                        startOfSubscription, endOfSubscription);
            } else if (invoice.getSubscriptionType().equals(SubscriptionType.WEEKLY)) {
                validateDayOfWeek(invoice.getBillDay());
                invoices = generateWeeklyInvoice(invoice.getBillDay(), invoice.getAmount(),
                        startOfSubscription, endOfSubscription);
            } else {
                invoices = generateDailyInvoice(invoice.getAmount(), startOfSubscription, endOfSubscription);
            }

            responseEntity = invoices;
            httpStatus = HttpStatus.OK;
        } catch (InvoiceInvalidDateRangeException ex) {
            responseEntity = new InvoiceException("subscription date",
                                    String.format("startDate:%s endDate:%s",
                                            invoice.getStartDate(),
                                            invoice.getEndDate()), ex.getMessage());

            httpStatus = HttpStatus.BAD_REQUEST;
        }
        catch (InvoiceExceedMonthException ex) {
            responseEntity = new InvoiceException("billDay", invoice.getBillDay(), ex.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
        } catch (InvoiceSubscriptionDateException ex) {
            InvoiceException invoiceException = new InvoiceException();

            invoiceException.setErrorMessage(ex.getMessage());
            if(startOfSubscription == null) {
                invoiceException.setParameterName("startDate");
                invoiceException.setParameterValue(invoice.getStartDate());
            }
            else {
                invoiceException.setParameterName("endDate");
                invoiceException.setParameterValue(invoice.getStartDate());
            }

            responseEntity = invoiceException;
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        catch (InvoiceWeekException ex) {
            responseEntity = new InvoiceException("billDay", invoice.getBillDay(), ex.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        catch (InvoiceMonthException ex) {
            responseEntity = new InvoiceException("billDay", invoice.getBillDay(), ex.getMessage());
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        finally {
            return new ResponseEntity<>(responseEntity, httpStatus);
        }
    }

    private List<Invoice> generateDailyInvoice(Long amount, LocalDate startDate, LocalDate endDate)
            throws InvoiceSubscriptionDateException, InvoiceInvalidDateRangeException {
        LocalDate invoiceDate = null;
        List<Invoice> invoices = new ArrayList<>();
        String strInvoiceDate = "";
        long days = ChronoUnit.DAYS.between(startDate, endDate);

        invoiceDate = startDate;
        for(int day = 0; day <= days; day++){
            strInvoiceDate = String.format("%s/%s/%s", String.format("%02d", invoiceDate.getDayOfMonth()),
                                                       String.format("%02d", invoiceDate.getMonth().getValue()),
                                                       String.format("%02d", invoiceDate.getYear()));

            invoices.add(new Invoice(amount, strInvoiceDate));
            invoiceDate = invoiceDate.plusDays(1);
        }
        return invoices;
    }

    private List<Invoice> generateMonthlyInvoice(String billDay, Long amount, LocalDate startDate, LocalDate endDate)
            throws InvoiceSubscriptionDateException, InvoiceInvalidDateRangeException {
        List<Invoice> invoices = new ArrayList<>();
        String strInvoiceDate = "";
        int monthsDiff = Period.between(startDate, endDate).getMonths();

        LocalDate invoiceDate = convertSubscriptionDate(
                                    String.format("%s/%s/%s", String.format("%02d", Integer.parseInt(billDay)),
                                            String.format("%02d", startDate.getMonth().getValue()),
                                            startDate.getYear()));

        if(ChronoUnit.DAYS.between(startDate, invoiceDate) < 0){
            throw new InvoiceInvalidDateRangeException("Invoice date cannot be earlier than start date");
        }

        for(int months = 0; months < monthsDiff; months++){
            strInvoiceDate = String.format("%s/%s/%s", String.format("%02d", invoiceDate.getDayOfMonth()),
                                                       String.format("%02d", invoiceDate.getMonth().getValue()),
                                                       String.format("%02d", invoiceDate.getYear()));

            invoices.add(new Invoice(amount, strInvoiceDate));
            invoiceDate = invoiceDate.plusMonths(1);

            if(ChronoUnit.DAYS.between(endDate, invoiceDate) > 0){
                throw new InvoiceInvalidDateRangeException("Invoice date cannot be later than end date");
            }
        }
        return invoices;
    }

    private List<Invoice> generateWeeklyInvoice(String billDay, Long amount, LocalDate startDate, LocalDate endDate)
            throws InvoiceSubscriptionDateException, InvoiceInvalidDateRangeException {

        List<Invoice> invoices = new ArrayList<>();
        String strInvoiceDate = "";

        LocalDate invoiceDate = setDateToDayOfWeek(billDay, startDate, endDate);
        invoiceDate = convertSubscriptionDate(
                            String.format("%s/%s/%s", String.format("%02d", invoiceDate.getDayOfMonth()),
                                                      String.format("%02d", invoiceDate.getMonth().getValue()),
                                                      invoiceDate.getYear()));

        do{
            strInvoiceDate = String.format("%s/%s/%s", String.format("%02d", invoiceDate.getDayOfMonth()),
                                                       String.format("%02d", invoiceDate.getMonth().getValue()),
                                                       invoiceDate.getYear());

            invoices.add(new Invoice(amount, strInvoiceDate));
            invoiceDate = invoiceDate.plusDays(7);
        } while(ChronoUnit.DAYS.between(endDate, invoiceDate) <= 0);

        return invoices;
    }

    private LocalDate setDateToDayOfWeek(String billDay, LocalDate startDate, LocalDate endDate)
            throws InvoiceInvalidDateRangeException {
        while(startDate.getDayOfWeek() != DayOfWeek.valueOf(billDay)){
            startDate = startDate.plusDays(1);
        }

        if(ChronoUnit.DAYS.between(startDate, endDate) < 0){
            throw new InvoiceInvalidDateRangeException("End date cannot be earlier than start date");
        }
        return startDate;
    }

    private LocalDate convertSubscriptionDate(String strSubscriptionDate) throws InvoiceSubscriptionDateException {
        LocalDate subscriptionDate = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");

        try {
            subscriptionDate = LocalDate.parse(strSubscriptionDate, formatter);
        } catch (DateTimeParseException e) {
            throw new InvoiceSubscriptionDateException();
        }
        return subscriptionDate;
    }

    private void validateSubscriptionDate(LocalDate startDate, LocalDate endDate)
            throws InvoiceExceedMonthException, InvoiceInvalidDateRangeException {
        int monthsDiff = Period.between(endDate, startDate).getMonths();
        long days = ChronoUnit.DAYS.between(startDate, endDate);

        if(days <= 0) {
            throw new InvoiceInvalidDateRangeException("End date cannot be earlier than start date");
        } else {
            if (monthsDiff > 3) {
                throw new InvoiceExceedMonthException("Maximum subscription is only 3 months");
            } else if (startDate.getYear() != endDate.getYear()) {
                throw new InvoiceExceedMonthException("Subscription date not the same year");
            }
        }
    }

    private void validateDayOfWeek(String strDayOfWeek) throws InvoiceWeekException {
        DayOfWeek[] dayOfWeeks = DayOfWeek.values();

        for (DayOfWeek dayOfWeek : dayOfWeeks) {
            if (dayOfWeek.name().equalsIgnoreCase(strDayOfWeek)) {
                return;
            }
        }

        throw new InvoiceWeekException("Invalid day of week");
    }

    private void validateDayOfMonth(String day, int year) throws InvoiceMonthException, InvoiceWeekException {
        Integer intDay = 0;
        final int IS_LEAP_YEAR_MAX_DAY_OF_MONTH = 29;
        final int NOT_LEAP_YEAR_MAX_DAY_OF_MONTH = 28;
        boolean isLeapYear = false;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        isLeapYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;

        try {
            intDay = Integer.parseInt(day);
            if (isLeapYear) {
                if (intDay > IS_LEAP_YEAR_MAX_DAY_OF_MONTH) {
                    throw new InvoiceMonthException();
                }
            } else {
                if (intDay > NOT_LEAP_YEAR_MAX_DAY_OF_MONTH) {
                    throw new InvoiceMonthException();
                }
            }
        }
        catch (NumberFormatException ex){
            throw new InvoiceWeekException("Day of month must be number");
        }
    }
}
