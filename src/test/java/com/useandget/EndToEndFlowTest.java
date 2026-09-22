package com.useandget;

import com.useandget.dto.ConsumptionProcessingResult;
import com.useandget.dto.EvaluationResult;
import com.useandget.dto.FileProcessingResult;
import com.useandget.entity.ConsumptionType;
import com.useandget.entity.Customer;
import com.useandget.entity.Gift;
import com.useandget.entity.Offer;
import com.useandget.entity.Segment;
import com.useandget.entity.State;
import com.useandget.repository.CustomerRepository;
import com.useandget.repository.OfferRepository;
import com.useandget.repository.SegmentRepository;
import com.useandget.service.ConsumptionService;
import com.useandget.service.CooldownEligibility;
import com.useandget.service.CooldownService;
import com.useandget.service.EvaluationService;
import com.useandget.service.FileIngestionService;
import com.useandget.service.OfferService;
import com.useandget.service.RewardService;
import com.useandget.service.reward.RewardGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Wires every team's service together (mocked repositories, no real database)
 * and walks through the full file-driven flow described in the plan: ingest a
 * customer file, create offers, evaluate a consumption file, and confirm the
 * resulting REWARDED / COOLDOWN outcomes, the cooldown skip on re-ingestion,
 * and eligibility again once the cooldown window has passed.
 */
@ExtendWith(MockitoExtension.class)
class EndToEndFlowTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 21, 9, 0);
    private static final Clock CLOCK = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SegmentRepository segmentRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private RewardGateway rewardGateway;

    private FileIngestionService fileIngestionService;
    private OfferService offerService;
    private CooldownService cooldownService;
    private ConsumptionService consumptionService;
    private EvaluationService evaluationService;

    private Segment dataPowerSegment;

    private static InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    void setUp() {
        dataPowerSegment = new Segment(1, "Data Power", ConsumptionType.MB, 500, 3, 7, new Gift(1, "MB", 500));

        fileIngestionService = new FileIngestionService(customerRepository, segmentRepository);
        cooldownService = new CooldownService(offerRepository, CLOCK);
        offerService = new OfferService(customerRepository, offerRepository, cooldownService, CLOCK);
        consumptionService = new ConsumptionService(offerRepository);
        RewardService rewardService = new RewardService(rewardGateway);
        evaluationService = new EvaluationService(offerRepository, rewardService, CLOCK);
    }

    @Test
    void happyPath_rewardedCustomer_andCooldownCustomer() throws IOException {
        // 1. Ingest a customer file with two phone numbers in the Data Power segment.
        when(segmentRepository.findByNameIgnoreCase("Data Power")).thenReturn(Optional.of(dataPowerSegment));
        when(customerRepository.findByPhoneNumber(any())).thenReturn(Optional.empty());

        String customerCsv = "01001234567,Customer One,Data Power\n01009876543,Customer Two,Data Power\n";
        FileProcessingResult ingestResult = fileIngestionService.ingest(stream(customerCsv));

        assertThat(ingestResult.getTotalRows()).isEqualTo(2);
        assertThat(ingestResult.getProcessed()).isEqualTo(2);

        Customer rewardedCustomer = new Customer(1, "01001234567", "Customer One", dataPowerSegment);
        Customer cooldownCustomer = new Customer(2, "01009876543", "Customer Two", dataPowerSegment);

        // 2. No prior offers -> both customers are eligible -> create OFFERED offers.
        when(offerRepository.findTopByCustomerOrderByOfferedAtDescOfferIdDesc(rewardedCustomer))
                .thenReturn(Optional.empty());
        when(offerRepository.findTopByCustomerOrderByOfferedAtDescOfferIdDesc(cooldownCustomer))
                .thenReturn(Optional.empty());
        java.util.concurrent.atomic.AtomicInteger nextOfferId = new java.util.concurrent.atomic.AtomicInteger(100);
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> {
            Offer offer = invocation.getArgument(0);
            if (offer.getOfferId() == null) {
                offer.setOfferId(nextOfferId.incrementAndGet());
            }
            return offer;
        });

        Offer rewardedOffer = offerService.createOffer(rewardedCustomer);
        Offer cooldownOffer = offerService.createOffer(cooldownCustomer);

        assertThat(rewardedOffer.getState()).isEqualTo(State.OFFERED);
        assertThat(cooldownOffer.getState()).isEqualTo(State.OFFERED);

        // 3. Consumption file: first customer meets the threshold, second doesn't.
        when(offerRepository.findByCustomer_PhoneNumberAndState("01001234567", State.OFFERED))
                .thenReturn(Optional.of(rewardedOffer));
        when(offerRepository.findByCustomer_PhoneNumberAndState("01009876543", State.OFFERED))
                .thenReturn(Optional.of(cooldownOffer));

        String consumptionCsv = "01001234567,600\n01009876543,200\n";
        ConsumptionProcessingResult consumptionResult = consumptionService.process(stream(consumptionCsv));

        assertThat(consumptionResult.getSummary().getProcessed()).isEqualTo(2);
        assertThat(consumptionResult.getMatches()).hasSize(2);

        // 4. Evaluate: 600 >= 500 -> REWARDED (gift granted); 200 < 500, retries left -> COOLDOWN.
        List<EvaluationResult> evaluations = evaluationService.evaluateAll(consumptionResult.getMatches());

        assertThat(rewardedOffer.getState()).isEqualTo(State.REWARDED);
        assertThat(rewardedOffer.getRewardedAt()).isEqualTo(NOW);
        assertThat(cooldownOffer.getState()).isEqualTo(State.COOLDOWN);
        assertThat(cooldownOffer.getCooldownUntil()).isEqualTo(NOW.plusDays(7));

        assertThat(evaluations).extracting(EvaluationResult::getNewState)
                .containsExactlyInAnyOrder(State.REWARDED, State.COOLDOWN);

        verify(rewardGateway).issue(argThat(result -> result.getOfferId().equals(rewardedOffer.getOfferId())));

        // 5. Cooldown customer reappears in a new file before cooldown expires -> not eligible.
        when(offerRepository.findTopByCustomerOrderByOfferedAtDescOfferIdDesc(cooldownCustomer))
                .thenReturn(Optional.of(cooldownOffer));

        CooldownEligibility stillCoolingDown = cooldownService.checkEligibility(cooldownCustomer);
        assertThat(stillCoolingDown.eligible()).isFalse();

        // 6. After the cooldown window passes, the same customer becomes eligible again.
        cooldownOffer.setCooldownUntil(NOW.minusSeconds(1));
        CooldownEligibility eligibleAgain = cooldownService.checkEligibility(cooldownCustomer);
        assertThat(eligibleAgain.eligible()).isTrue();
        assertThat(eligibleAgain.nextRetryCount()).isEqualTo(1);
    }
}
