package com.useandget.service;

import com.useandget.dto.ConsumptionProcessingResult;
import com.useandget.dto.MatchedConsumption;
import com.useandget.entity.ConsumptionType;
import com.useandget.entity.Customer;
import com.useandget.entity.Gift;
import com.useandget.entity.Offer;
import com.useandget.entity.Segment;
import com.useandget.entity.State;
import com.useandget.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConsumptionServiceTest {

    private OfferRepository offerRepository;
    private ConsumptionService consumptionService;

    @BeforeEach
    void setUp() {
        offerRepository = mock(OfferRepository.class);
        consumptionService = new ConsumptionService(offerRepository);
    }

    private static InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private Offer offeredOffer() {
        Gift gift = new Gift(1, "MB", 500);
        Segment segment = new Segment(1, "Data Power", ConsumptionType.MB, 500, 3, 7, gift);
        Customer customer = new Customer(1, "01001234567", "Test Customer", segment);
        return new Offer(10, State.OFFERED, 0, null, null, null, null, customer, segment);
    }

    @Test
    void process_matchesRowToActiveOfferedOffer() throws IOException {
        Offer offer = offeredOffer();
        when(offerRepository.findByCustomer_PhoneNumberAndState("01001234567", State.OFFERED))
                .thenReturn(Optional.of(offer));

        String csv = "01001234567,600\n";
        ConsumptionProcessingResult result = consumptionService.process(stream(csv));

        assertEquals(1, result.getSummary().getTotalRows());
        assertEquals(1, result.getSummary().getProcessed());
        assertEquals(0, result.getSummary().getSkipped());
        assertEquals(1, result.getMatches().size());

        MatchedConsumption match = result.getMatches().get(0);
        assertEquals(offer, match.getOffer());
        assertEquals(600.0, match.getConsumedAmount());
    }

    @Test
    void process_skipsRow_whenNoActiveOfferedOfferExists() throws IOException {
        when(offerRepository.findByCustomer_PhoneNumberAndState("01001234567", State.OFFERED))
                .thenReturn(Optional.empty());

        String csv = "01001234567,600\n";
        ConsumptionProcessingResult result = consumptionService.process(stream(csv));

        assertEquals(1, result.getSummary().getSkipped());
        assertEquals(0, result.getMatches().size());
    }

    @Test
    void process_skipsRow_whenPhoneNumberIsInvalid() throws IOException {
        String csv = "123,600\n";
        ConsumptionProcessingResult result = consumptionService.process(stream(csv));

        assertEquals(1, result.getSummary().getSkipped());
        verifyNoInteractions(offerRepository);
    }

    @Test
    void process_countsRow_asFailed_whenConsumedAmountIsNotNumeric() throws IOException {
        String csv = "01001234567,notanumber\n";
        ConsumptionProcessingResult result = consumptionService.process(stream(csv));

        assertEquals(1, result.getSummary().getFailed());
        assertEquals(0, result.getMatches().size());
    }
}
