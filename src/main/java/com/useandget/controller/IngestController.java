package com.useandget.controller;

import com.useandget.dto.FileProcessingResult;
import com.useandget.entity.Customer;
import com.useandget.repository.CustomerRepository;
import com.useandget.service.FileIngestionService;
import com.useandget.service.OfferService;
import com.useandget.util.CsvFileReader;
import com.useandget.util.PhoneNumberValidator;
import com.useandget.util.RowParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Entry point for the customer file pipeline: ingest the file (Team A), then
 * create an offer for every customer who is eligible (Team B) - a customer
 * currently in an active cooldown is skipped and only re-considered the next
 * time they appear in a file, after the cooldown has expired.
 */
@RestController
@RequestMapping("/api/ingest")
public class IngestController {

    private static final Logger log = LoggerFactory.getLogger(IngestController.class);

    private final FileIngestionService fileIngestionService;
    private final CustomerRepository customerRepository;
    private final OfferService offerService;

    public IngestController(
            FileIngestionService fileIngestionService,
            CustomerRepository customerRepository,
            OfferService offerService
    ) {
        this.fileIngestionService = fileIngestionService;
        this.customerRepository = customerRepository;
        this.offerService = offerService;
    }

    @PostMapping
    public FileProcessingResult ingest(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file: " + e.getMessage());
        }

        FileProcessingResult result;
        try {
            result = fileIngestionService.ingest(new ByteArrayInputStream(content));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to parse uploaded file: " + e.getMessage());
        }

        createOffersForIngestedCustomers(content);

        return result;
    }

    private void createOffersForIngestedCustomers(byte[] content) {
        try {
            CsvFileReader.parse(new ByteArrayInputStream(content), this::mapPhoneNumber)
                    .getRows()
                    .forEach(phoneNumber -> customerRepository.findByPhoneNumber(phoneNumber)
                            .ifPresent(this::createOfferIfEligible));
        } catch (IOException e) {
            log.error("Failed to re-read uploaded file while creating offers: {}", e.getMessage());
        }
    }

    private void createOfferIfEligible(Customer customer) {
        offerService.createOfferIfEligible(customer)
                .ifPresentOrElse(
                        offer -> log.info("Created offer {} for customer {}", offer.getOfferId(), customer.getPhoneNumber()),
                        () -> log.info("Customer {} is not eligible for a new offer right now", customer.getPhoneNumber())
                );
    }

    private String mapPhoneNumber(String[] columns) throws RowParseException {
        if (columns.length == 0) {
            throw new RowParseException("Row has no columns");
        }
        String phoneNumber = PhoneNumberValidator.normalize(columns[0]);
        if (!PhoneNumberValidator.isValid(phoneNumber)) {
            throw new RowParseException("Invalid phone number format: " + columns[0]);
        }
        return phoneNumber;
    }
}
