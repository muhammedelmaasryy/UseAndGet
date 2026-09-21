package com.useandget.service;

import com.useandget.dto.ConsumptionProcessingResult;
import com.useandget.dto.ConsumptionRow;
import com.useandget.dto.FileProcessingResult;
import com.useandget.dto.MatchedConsumption;
import com.useandget.entity.Offer;
import com.useandget.entity.State;
import com.useandget.repository.OfferRepository;
import com.useandget.util.CsvFileReader;
import com.useandget.util.CsvParseOutcome;
import com.useandget.util.PhoneNumberValidator;
import com.useandget.util.RowParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads a consumption CSV file (phoneNumber, consumedAmount) and matches each
 * row to the customer's currently active (state = OFFERED) offer. Rows with
 * no matching active offer are skipped - evaluation itself is EvaluationService's job.
 */
@Service
public class ConsumptionService {

    private static final Logger log = LoggerFactory.getLogger(ConsumptionService.class);

    private final OfferRepository offerRepository;

    public ConsumptionService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public ConsumptionProcessingResult process(InputStream fileStream) throws IOException {
        CsvParseOutcome<ConsumptionRow> outcome = CsvFileReader.parse(fileStream, this::mapRow);

        int processed = 0;
        int skipped = 0;
        int failed = outcome.getErrors().size();
        List<MatchedConsumption> matches = new ArrayList<>();

        for (ConsumptionRow row : outcome.getRows()) {
            String phoneNumber = PhoneNumberValidator.normalize(row.getPhoneNumber());

            if (!PhoneNumberValidator.isValid(phoneNumber)) {
                log.warn("Skipping consumption row - invalid phone number format: {}", row.getPhoneNumber());
                skipped++;
                continue;
            }

            Optional<Offer> offer = offerRepository.findByCustomer_PhoneNumberAndState(phoneNumber, State.OFFERED);
            if (offer.isEmpty()) {
                log.warn("Skipping consumption row - no active OFFERED offer for phone number: {}", phoneNumber);
                skipped++;
                continue;
            }

            matches.add(new MatchedConsumption(offer.get(), row.getConsumedAmount()));
            processed++;
        }

        log.info("Consumption file processing complete: total={}, matched={}, skipped={}, failed={}",
                outcome.getTotalRows(), processed, skipped, failed);

        FileProcessingResult summary = new FileProcessingResult(outcome.getTotalRows(), processed, skipped, failed);
        return new ConsumptionProcessingResult(summary, matches);
    }

    private ConsumptionRow mapRow(String[] columns) throws RowParseException {
        if (columns.length < 2) {
            throw new RowParseException("Expected 2 columns (phoneNumber,consumedAmount) but found " + columns.length);
        }

        String phoneNumber = columns[0];
        String rawAmount = columns[1];

        if (phoneNumber.isEmpty()) {
            throw new RowParseException("Phone number is empty");
        }

        double consumedAmount;
        try {
            consumedAmount = Double.parseDouble(rawAmount);
        } catch (NumberFormatException e) {
            throw new RowParseException("Consumed amount is not a valid number: '" + rawAmount + "'");
        }

        if (consumedAmount < 0) {
            throw new RowParseException("Consumed amount cannot be negative: " + consumedAmount);
        }

        return new ConsumptionRow(phoneNumber, consumedAmount);
    }
}
