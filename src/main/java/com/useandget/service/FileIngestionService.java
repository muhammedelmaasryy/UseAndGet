package com.useandget.service;

import com.useandget.dto.CustomerFileRow;
import com.useandget.dto.FileProcessingResult;
import com.useandget.entity.Customer;
import com.useandget.entity.Segment;
import com.useandget.repository.CustomerRepository;
import com.useandget.repository.SegmentRepository;
import com.useandget.util.CsvFileReader;
import com.useandget.util.CsvParseOutcome;
import com.useandget.util.PhoneNumberValidator;
import com.useandget.util.RowParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Reads a customer CSV file (phoneNumber, name, segmentName), validates each
 * row and creates or updates the corresponding Customer. Does not create
 * offers - that is OfferService's responsibility once a row is accepted here.
 */
@Service
public class FileIngestionService {

    private static final Logger log = LoggerFactory.getLogger(FileIngestionService.class);

    private final CustomerRepository customerRepository;
    private final SegmentRepository segmentRepository;

    public FileIngestionService(CustomerRepository customerRepository, SegmentRepository segmentRepository) {
        this.customerRepository = customerRepository;
        this.segmentRepository = segmentRepository;
    }

    public FileProcessingResult ingest(InputStream fileStream) throws IOException {
        CsvParseOutcome<CustomerFileRow> outcome = CsvFileReader.parse(fileStream, this::mapRow);

        int processed = 0;
        int skipped = 0;
        int failed = outcome.getErrors().size();
        Set<String> seenPhoneNumbers = new HashSet<>();

        for (CustomerFileRow row : outcome.getRows()) {
            String phoneNumber = PhoneNumberValidator.normalize(row.getPhoneNumber());

            if (!PhoneNumberValidator.isValid(phoneNumber)) {
                log.warn("Skipping row - invalid phone number format: {}", row.getPhoneNumber());
                skipped++;
                continue;
            }

            if (!seenPhoneNumbers.add(phoneNumber)) {
                log.warn("Skipping row - duplicate phone number within file: {}", phoneNumber);
                skipped++;
                continue;
            }

            if (row.getSegmentName() == null || row.getSegmentName().isBlank()) {
                log.warn("Skipping row - missing segment name for phone number: {}", phoneNumber);
                skipped++;
                continue;
            }

            Optional<Segment> segment = segmentRepository.findByNameIgnoreCase(row.getSegmentName().trim());
            if (segment.isEmpty()) {
                log.warn("Skipping row - unknown segment '{}' for phone number: {}", row.getSegmentName(), phoneNumber);
                skipped++;
                continue;
            }

            try {
                upsertCustomer(phoneNumber, row.getName(), segment.get());
                processed++;
            } catch (Exception e) {
                log.error("Failed to persist customer {}: {}", phoneNumber, e.getMessage());
                failed++;
            }
        }

        log.info("Customer file ingestion complete: total={}, processed={}, skipped={}, failed={}",
                outcome.getTotalRows(), processed, skipped, failed);

        return new FileProcessingResult(outcome.getTotalRows(), processed, skipped, failed);
    }

    private void upsertCustomer(String phoneNumber, String name, Segment segment) {
        Customer customer = customerRepository.findByPhoneNumber(phoneNumber).orElse(null);
        if (customer == null) {
            customer = new Customer(null, phoneNumber, name, segment);
        } else {
            customer.setName(name);
            customer.setSegment(segment);
        }
        customerRepository.save(customer);
    }

    private CustomerFileRow mapRow(String[] columns) throws RowParseException {
        if (columns.length < 3) {
            throw new RowParseException("Expected 3 columns (phoneNumber,name,segmentName) but found " + columns.length);
        }

        String phoneNumber = columns[0];
        String name = columns[1];
        String segmentName = columns[2];

        if (phoneNumber.isEmpty()) {
            throw new RowParseException("Phone number is empty");
        }
        if (name.isEmpty()) {
            throw new RowParseException("Name is empty");
        }

        return new CustomerFileRow(phoneNumber, name, segmentName);
    }
}
